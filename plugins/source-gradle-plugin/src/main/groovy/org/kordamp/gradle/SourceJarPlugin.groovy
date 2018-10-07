/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 the original author or authors.
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
package org.kordamp.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.bundling.Jar

import static org.kordamp.gradle.BasePlugin.isRootProject

/**
 * Configures a {@code sourceJar} task.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
class SourceJarPlugin implements Plugin<Project> {
    static final String SOURCE_JAR_TASK_NAME = 'sourceJar'

    private static final String VISITED = SourceJarPlugin.class.name.replace('.', '_') + '_VISITED'

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
        String visitedPropertyName = VISITED + '_' + project.name
        if (project.findProperty(visitedPropertyName)) {
            return
        }
        project.ext[visitedPropertyName] = true

        BasePlugin.applyIfMissing(project)

        project.afterEvaluate {
            ProjectConfigurationExtension mergedConfiguration = project.ext.mergedConfiguration

            if (!mergedConfiguration.source.enabled) {
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

        sourceJarTask
    }
}
