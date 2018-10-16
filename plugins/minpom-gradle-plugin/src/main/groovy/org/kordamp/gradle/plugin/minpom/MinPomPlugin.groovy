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
package org.kordamp.gradle.plugin.minpom


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * Configures a {@code minpom} task.
 * Calculates {@code pom.xml} and {@code pom.properties} for each {@code SourceSet}.
 * These files should be packaged under {@code /META-INF/maven}.

 * @author Andres Almiray
 * @since 0.1.0
 */
class MinPomPlugin implements Plugin<Project> {
    static final String MINPOM_TASK_NAME = 'minpom'

    private static final String VISITED = MinPomPlugin.class.name.replace('.', '_') + '_VISITED'

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
            project.plugins.apply(MinPomPlugin)
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
            if (!mergedConfiguration.minpom.enabled) {
                return
            }

            project.plugins.withType(JavaBasePlugin) {
                createMinPomTask(project)
            }
        }
    }

    private Task createMinPomTask(Project project) {
        String taskName = MINPOM_TASK_NAME

        Task minPomTask = project.tasks.findByName(taskName)
        Task classesTask = project.tasks.findByName('classes')

        if (classesTask && !minPomTask) {
            minPomTask = project.tasks.create(taskName, MinpomTask) {
                dependsOn classesTask
                group org.gradle.api.plugins.BasePlugin.BUILD_GROUP
                description 'Generates a minimum POM file'
            }
        }

        minPomTask
    }

    static File resolveMinPomDestinationDir(Project project) {
        project.file("${project.buildDir}/pom/maven")
    }
}
