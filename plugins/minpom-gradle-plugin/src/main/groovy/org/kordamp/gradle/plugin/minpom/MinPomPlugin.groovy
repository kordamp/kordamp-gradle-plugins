/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
import org.gradle.api.Task
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

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

        if (isRootProject(project)) {
            if (project.childProjects.size()) {
                project.childProjects.values().each {
                    configureProject(it)
                }
            } else {
                configureProject(project)
            }
        } else {
            configureProject(project)
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

        project.afterEvaluate {
            ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
            setEnabled(effectiveConfig.minpom.enabled)

            if (!enabled) {
                return
            }

            project.pluginManager.withPlugin('java-base') {
                createMinPomTask(project)
            }
        }
    }

    private void createMinPomTask(Project project) {
        Task classesTask = project.tasks.findByName('classes')

        if (classesTask) {
            project.tasks.register(MINPOM_TASK_NAME, MinpomTask,
                new Action<MinpomTask>() {
                    @Override
                    void execute(MinpomTask t) {
                        t.dependsOn classesTask
                        t.group = org.gradle.api.plugins.BasePlugin.BUILD_GROUP
                        t.description = 'Generates a minimum POM file.'
                    }
                })
        }
    }

    static File resolveMinPomDestinationDir(Project project) {
        project.file("${project.buildDir}/pom/maven")
    }
}
