/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2020 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kordamp.gradle.plugin.minpom

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.kordamp.gradle.annotations.DependsOn
import org.kordamp.gradle.listener.ProjectEvaluatedListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

import javax.inject.Named

import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addProjectEvaluatedListener
import static org.kordamp.gradle.util.PluginUtils.resolveClassesTask
import static org.kordamp.gradle.util.PluginUtils.resolveConfig

/**
 * Configures a {@code minpom} task.
 * Calculates {@code pom.xml} and {@code pom.properties}.
 * These files should be packaged under {@code /META-INF/maven}.

 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class MinPomPlugin extends AbstractKordampPlugin {
    static final String MINPOM_TASK_NAME = 'minpom'

    Project project

    void apply(Project project) {
        this.project = project

        configureProject(project)
        project.childProjects.values().each {
            it.pluginManager.apply(MinPomPlugin)
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(MinPomPlugin)) {
            project.pluginManager.apply(MinPomPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)

        project.pluginManager.withPlugin('java-base') {
            createMinPomTask(project)

            addProjectEvaluatedListener(project, new MinpomProjectEvaluatedListener())
        }
    }

    @Named('minpom')
    @DependsOn(['base'])
    private class MinpomProjectEvaluatedListener implements ProjectEvaluatedListener {
        @Override
        void projectEvaluated(Project project) {
            ProjectConfigurationExtension config = resolveConfig(project)
            setEnabled(config.artifacts.minpom.enabled)

            if (!enabled) {
                return
            }

            configureMinPomTask(project)
        }
    }

    private TaskProvider<MinpomTask> createMinPomTask(Project project) {
        project.tasks.register(MINPOM_TASK_NAME, MinpomTask,
            new Action<MinpomTask>() {
                @Override
                void execute(MinpomTask t) {
                    t.enabled = false
                    t.group = org.gradle.api.plugins.BasePlugin.BUILD_GROUP
                    t.description = 'Generates a minimum POM file.'
                }
            })
    }

    private void configureMinPomTask(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)

        project.tasks.named('minpom', MinpomTask, new Action<MinpomTask>() {
            @Override
            void execute(MinpomTask t) {
                t.enabled = config.artifacts.minpom.enabled
                t.dependsOn resolveClassesTask(project)
            }
        })
    }

    static File resolveMinPomDestinationDir(Project project) {
        project.file("${project.buildDir}/pom/maven")
    }
}
