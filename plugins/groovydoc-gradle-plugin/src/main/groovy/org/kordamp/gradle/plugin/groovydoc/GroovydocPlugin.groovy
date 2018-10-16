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
package org.kordamp.gradle.plugin.groovydoc

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.GroovyBasePlugin
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Groovydoc
import org.gradle.api.tasks.javadoc.Javadoc
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.javadoc.JavadocPlugin

import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * Configures {@code groovydoc} and {@code groovydocJar} tasks.
 *
 * @author Andres Almiray
 * @since 0.4.0
 */
class GroovydocPlugin implements Plugin<Project> {
    static final String GROOVYDOC_TASK_NAME = 'groovydoc'
    static final String GROOVYDOC_JAR_TASK_NAME = 'groovydocJar'

    private static final String VISITED = GroovydocPlugin.class.name.replace('.', '_') + '_VISITED'

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
        if (!project.plugins.findPlugin(GroovydocPlugin)) {
            project.plugins.apply(GroovydocPlugin)
        }
    }

    private void configureProject(Project project) {
        String visitedPropertyName = VISITED + '_' + project.name
        if (project.findProperty(visitedPropertyName)) {
            return
        }
        project.ext[visitedPropertyName] = true

        BasePlugin.applyIfMissing(project)
        // apply first then we can be certain javadoc tasks can be located on time
        JavadocPlugin.applyIfMissing(project)

        project.afterEvaluate {
            ProjectConfigurationExtension mergedConfiguration = project.ext.mergedConfiguration

            if (!mergedConfiguration.groovydoc.enabled) {
                return
            }

            project.plugins.withType(GroovyBasePlugin) {
                Javadoc javadoc = project.tasks.findByName(JavadocPlugin.JAVADOC_TASK_NAME)
                if (!javadoc) return

                Task groovydoc = createGroovydocTaskIfNeeded(project, javadoc)
                if (!groovydoc) return
                mergedConfiguration.groovydoc.groovydocTasks() << groovydoc

                Task groovydocJar = createGroovydocJarTask(project, groovydoc)
                mergedConfiguration.groovydoc.groovydocJarTasks() << groovydocJar

                mergedConfiguration.groovydoc.projects() << project
            }

            project.tasks.withType(Groovydoc) { task ->
                mergedConfiguration.groovydoc.applyTo(task)
                task.footer = "Copyright &copy; ${mergedConfiguration.info.copyrightYear} ${mergedConfiguration.info.resolveAuthors().join(', ')}. All rights reserved."
            }
        }
    }

    private Task createGroovydocTaskIfNeeded(Project project, Javadoc javadoc) {
        String taskName = GROOVYDOC_TASK_NAME

        Groovydoc groovydocTask = project.tasks.findByName(taskName)
        Task classesTask = project.tasks.findByName('classes')

        if (classesTask && !groovydocTask) {
            groovydocTask = project.tasks.create(taskName, Groovydoc) {
                dependsOn classesTask
                group JavaBasePlugin.DOCUMENTATION_GROUP
                description 'Generates Groovydoc API documentation'
                source project.sourceSets.main.allSource
                destinationDir project.file("${project.buildDir}/docs/groovydoc")
            }
        }

        ProjectConfigurationExtension mergedConfiguration = project.ext.mergedConfiguration
        groovydocTask.configure {
            classpath = javadoc.classpath
            include(mergedConfiguration.groovydoc.includes)
            exclude(mergedConfiguration.groovydoc.excludes)
        }

        groovydocTask
    }

    private Task createGroovydocJarTask(Project project, Task groovydoc) {
        String taskName = GROOVYDOC_JAR_TASK_NAME

        Task groovydocJarTask = project.tasks.findByName(taskName)

        if (!groovydocJarTask) {
            groovydocJarTask = project.tasks.create(taskName, Jar) {
                dependsOn groovydoc
                group JavaBasePlugin.DOCUMENTATION_GROUP
                description 'An archive of the Groovydoc API docs'
                classifier 'groovydoc'
                from groovydoc.destinationDir
            }
        }

        ProjectConfigurationExtension mergedConfiguration = project.ext.mergedConfiguration
        if (mergedConfiguration.groovydoc.replaceJavadoc) {
            groovydocJarTask.classifier = 'javadoc'
            project.tasks.findByName(JavadocPlugin.JAVADOC_JAR_TASK_NAME)?.enabled = false
        }

        groovydocJarTask
    }
}
