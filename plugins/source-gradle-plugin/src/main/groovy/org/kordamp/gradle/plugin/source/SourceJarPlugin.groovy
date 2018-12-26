/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kordamp.gradle.plugin.source

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.bundling.Jar
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * Configures a {@code sourceJar} task.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
class SourceJarPlugin extends AbstractKordampPlugin {
    static final String SOURCE_JAR_TASK_NAME = 'sourceJar'

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
        if (!project.plugins.findPlugin(SourceJarPlugin)) {
            project.plugins.apply(SourceJarPlugin)
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
            setEnabled(effectiveConfig.source.enabled)

            if (!enabled) {
                return
            }

            project.plugins.withType(JavaBasePlugin) {
                createSourceJarTask(project)
            }
        }
    }

    private Task createSourceJarTask(Project project) {
        String taskName = SOURCE_JAR_TASK_NAME

        Task sourceJarTask = project.tasks.findByName(taskName)
        Task classesTask = project.tasks.findByName('classes')

        if (classesTask && !sourceJarTask) {
            sourceJarTask = project.tasks.create(SOURCE_JAR_TASK_NAME, Jar) {
                dependsOn classesTask
                group org.gradle.api.plugins.BasePlugin.BUILD_GROUP
                description 'An archive of the source code'
                classifier 'sources'
                from project.sourceSets.main.allSource
            }
        }

        project.tasks.findByName(org.gradle.api.plugins.BasePlugin.ASSEMBLE_TASK_NAME).dependsOn(sourceJarTask)

        sourceJarTask
    }
}
