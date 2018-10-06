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

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.GroovyBasePlugin
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Groovydoc
import org.gradle.api.tasks.javadoc.Javadoc
import org.kordamp.gradle.model.Information

import static org.kordamp.gradle.BasePlugin.isRootProject

/**
 * Configures a {@code groovydocJar} for each {@code SourceSet}.
 * <strong>NOTE:</strong> any sources with the word "test" will be skipped.
 * Applies the {@code maven-publish} plugin if it has not been applied before.
 *
 * @author Andres Almiray
 * @since 0.4.0
 */
class GroovydocPlugin implements Plugin<Project> {
    static final String ALL_GROOVYDOCS_TASK_NAME = 'allGroovydocs'
    static final String ALL_GROOVYDOC_JARS_TASK_NAME = 'allGroovydocJars'

    private static final String VISITED = GroovydocPlugin.class.name.replace('.', '_') + '_VISITED'

    Project project

    void apply(Project project) {
        this.project = project

        if (isRootProject(project)) {
            createGroovydocJarTaskIfCompatible(project)
            project.childProjects.values().each { prj ->
                createGroovydocJarTaskIfCompatible(prj)
            }
        } else {
            createGroovydocJarTaskIfCompatible(project)
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(GroovydocPlugin)) {
            project.plugins.apply(GroovydocPlugin)
        }
    }

    private void createGroovydocJarTaskIfCompatible(Project project) {
        String visitedPropertyName = VISITED + '_' + project.name
        if (project.findProperty(visitedPropertyName)) {
            return
        }
        project.ext[visitedPropertyName] = true

        BasePlugin.applyIfMissing(project)
        // apply first then we can be certain javadoc tasks can be located on time
        JavadocPlugin.applyIfMissing(project)

        project.plugins.withType(GroovyBasePlugin) {
            if (!project.plugins.findPlugin(MavenPublishPlugin)) {
                project.plugins.apply(MavenPublishPlugin)
            }
        }

        project.afterEvaluate { Project prj ->
            Information info = prj.ext.mergedInfo

            if (!info.groovydoc.enabled) {
                return
            }

            prj.plugins.withType(GroovyBasePlugin) {
                prj.sourceSets.each { SourceSet ss ->
                    // skip generating a groovydoc task for SourceSets that may contain tests
                    if (!ss.name.toLowerCase().contains('test')) {
                        Javadoc javadoc = project.tasks.findByName(JavadocPlugin.resolveJavadocTaskName(ss))
                        // javadoc.enabled = false

                        Task groovydoc = createGroovydocTaskIfNeeded(prj, ss, javadoc)
                        info.groovydoc.groovydocTasks().put(ss.name, groovydoc)

                        // Jar javadocJar = project.tasks.findByName(JavadocPlugin.resolveJavadocJarTaskName(ss))
                        // javadocJar.enabled = false

                        Task groovydocJar = createGroovydocJarTask(prj, ss, groovydoc)
                        info.groovydoc.groovydocJarTasks().put(ss.name, groovydocJar)

                        info.groovydoc.projects().put(ss.name, prj)

                        updatePublications(prj, ss, groovydocJar)
                    }
                }
            }

            project.tasks.withType(Groovydoc) { task ->
                info.groovydoc.options.applyTo(task)
                task.footer = "Copyright &copy; ${info.copyrightYear} ${info.resolveAuthors()}. All rights reserved."
            }

            project.tasks.findByName(JavadocPlugin.ALL_JAVADOCS_TASK_NAME)?.enabled = false
            project.tasks.findByName(JavadocPlugin.ALL_JAVADOC_JARS_TASK_NAME)?.enabled = false

            if (info.groovydoc.groovydocTasks()) {
                project.tasks.create(ALL_GROOVYDOCS_TASK_NAME, DefaultTask) {
                    dependsOn info.groovydoc.groovydocTasks()
                    group JavaBasePlugin.DOCUMENTATION_GROUP
                    description "Triggers all groovydoc tasks for project ${project.name}"
                }
            }

            if (info.groovydoc.groovydocJarTasks()) {
                project.tasks.create(ALL_GROOVYDOC_JARS_TASK_NAME, DefaultTask) {
                    dependsOn info.groovydoc.groovydocJarTasks()
                    group JavaBasePlugin.DOCUMENTATION_GROUP
                    description "Triggers all groovydocJar tasks for project ${project.name}"
                }
            }
        }
    }

    private Task createGroovydocTaskIfNeeded(Project project, SourceSet sourceSet, Javadoc javadoc) {
        String taskName = resolveGroovydocTaskName(sourceSet)

        Task groovydocTask = project.tasks.findByName(taskName)

        if (!groovydocTask) {
            String classesTaskName = sourceSet.name == 'main' ? 'classes' : sourceSet.name + 'Classes'


            groovydocTask = project.tasks.create(taskName, Groovydoc) {
                dependsOn project.tasks.getByName(classesTaskName)
                group JavaBasePlugin.DOCUMENTATION_GROUP
                description "Generates Groovydoc API documentation [sourceSet ${sourceSet.name}]"
                source sourceSet.allSource
                destinationDir project.file("${project.buildDir}/docs/${sourceSet.name}/groovydoc")
            }
        }

        groovydocTask.configure {
            classpath = javadoc.classpath
        }

        groovydocTask
    }

    static String resolveGroovydocTaskName(SourceSet sourceSet) {
        return sourceSet.name == 'main' ? 'groovydoc' : sourceSet.name + 'Groovydoc'
    }

    private Task createGroovydocJarTask(Project project, SourceSet sourceSet, Task groovydoc) {
        String taskName = resolveGroovydocJarTaskName(sourceSet)

        Task groovydocJarTask = project.tasks.findByName(taskName)

        if (!groovydocJarTask) {
            String archiveBaseName = sourceSet.name == 'main' ? project.name : project.name + '-' + sourceSet.name

            groovydocJarTask = project.tasks.create(taskName, Jar) {
                dependsOn groovydoc
                group JavaBasePlugin.DOCUMENTATION_GROUP
                description "An archive of the API docs [sourceSet ${sourceSet.name}]"
                baseName = archiveBaseName
                classifier 'javadoc'
                from groovydoc.destinationDir
            }
        }

        groovydocJarTask
    }

    static String resolveGroovydocJarTaskName(SourceSet sourceSet) {
        return sourceSet.name == 'main' ? 'groovydocJar' : sourceSet.name + 'GroovydocJar'
    }

    private void updatePublications(Project project, SourceSet sourceSet, Task groovydocJar) {
        project.publishing {
            publications {
                "${sourceSet.name}"(MavenPublication) {
                    artifact groovydocJar
                }
            }
        }

        project.artifacts {
            groovydocJar
        }
    }
}
