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

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc

import static org.kordamp.gradle.BasePlugin.isRootProject

/**
 * Configures {@code javadoc} and {@code javadocJar} tasks.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
class JavadocPlugin implements Plugin<Project> {
    static final String JAVADOC_TASK_NAME = 'javadoc'
    static final String JAVADOC_JAR_TASK_NAME = 'javadocJar'

    private static final String VISITED = JavadocPlugin.class.name.replace('.', '_') + '_VISITED'

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
        if (!project.plugins.findPlugin(JavadocPlugin)) {
            project.plugins.apply(JavadocPlugin)
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

            if (!mergedConfiguration.javadoc.enabled) {
                return
            }

            project.plugins.withType(JavaBasePlugin) {
                Task javadoc = createJavadocTaskIfNeeded(project)
                if (!javadoc) return
                mergedConfiguration.javadoc.javadocTasks() << javadoc

                Task javadocJar = createJavadocJarTask(project, javadoc)
                mergedConfiguration.javadoc.javadocJarTasks() << javadocJar

                mergedConfiguration.javadoc.projects() << project
            }

            project.tasks.withType(Javadoc) { task ->
                mergedConfiguration.javadoc.applyTo(task)
                options.footer = "Copyright &copy; ${mergedConfiguration.info.copyrightYear} ${mergedConfiguration.info.resolveAuthors().join(', ')}. All rights reserved."

                if (JavaVersion.current().isJava8Compatible()) {
                    options.addStringOption('Xdoclint:none', '-quiet')
                }
            }
        }
    }

    private Task createJavadocTaskIfNeeded(Project project) {
        String taskName = JAVADOC_TASK_NAME

        Task javadocTask = project.tasks.findByName(taskName)
        Task classesTask = project.tasks.findByName('classes')

        if (classesTask && !javadocTask) {
            javadocTask = project.tasks.create(taskName, Javadoc) {
                dependsOn classesTask
                group JavaBasePlugin.DOCUMENTATION_GROUP
                description 'Generates Javadoc API documentation'
                source project.sourceSets.main.allSource
                destinationDir project.file("${project.buildDir}/docs/main/javadoc")
            }
        }

        javadocTask
    }

    private Task createJavadocJarTask(Project project, Task javadoc) {
        String taskName = JAVADOC_JAR_TASK_NAME

        Task javadocJarTask = project.tasks.findByName(taskName)

        if (!javadocJarTask) {
            javadocJarTask = project.tasks.create(taskName, Jar) {
                dependsOn javadoc
                group JavaBasePlugin.DOCUMENTATION_GROUP
                description 'An archive of the Javadoc API docs'
                classifier 'javadoc'
                from javadoc.destinationDir
            }
        }

        javadocJarTask
    }
}
