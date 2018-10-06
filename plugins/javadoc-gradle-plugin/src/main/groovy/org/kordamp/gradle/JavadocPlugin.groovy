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
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.kordamp.gradle.model.Information

import static org.kordamp.gradle.BasePlugin.isRootProject

/**
 * Configures a {@code javadocJar} for each {@code SourceSet}.
 * <strong>NOTE:</strong> any sources with the word "test" will be skipped.
 * Applies the {@code maven-publish} plugin if it has not been applied before.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
class JavadocPlugin implements Plugin<Project> {
    static final String ALL_JAVADOCS_TASK_NAME = 'allJavadocs'
    static final String ALL_JAVADOC_JARS_TASK_NAME = 'allJavadocJars'

    private static final String VISITED = JavadocPlugin.class.name.replace('.', '_') + '_VISITED'

    Project project

    void apply(Project project) {
        this.project = project

        if (isRootProject(project)) {
            createJavadocJarTaskIfCompatible(project)
            project.childProjects.values().each { prj ->
                createJavadocJarTaskIfCompatible(prj)
            }
        } else {
            createJavadocJarTaskIfCompatible(project)
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(JavadocPlugin)) {
            project.plugins.apply(JavadocPlugin)
        }
    }

    private void createJavadocJarTaskIfCompatible(Project project) {
        String visitedPropertyName = VISITED + '_' + project.name
        if (project.findProperty(visitedPropertyName)) {
            return
        }
        project.ext[visitedPropertyName] = true

        BasePlugin.applyIfMissing(project)

        project.plugins.withType(JavaBasePlugin) {
            if (!project.plugins.findPlugin(MavenPublishPlugin)) {
                project.plugins.apply(MavenPublishPlugin)
            }
        }

        project.afterEvaluate { Project prj ->
            ProjectConfigurationExtension mergedConfiguration = project.ext.mergedConfiguration

            if (!mergedConfiguration.javadoc.enabled) {
                return
            }

            prj.plugins.withType(JavaBasePlugin) {
                prj.sourceSets.each { SourceSet ss ->
                    // skip generating a javadoc task for SourceSets that may contain tests
                    if (!ss.name.toLowerCase().contains('test')) {
                        Task javadoc = createJavadocTaskIfNeeded(prj, ss)
                        mergedConfiguration.javadoc.javadocTasks().put(ss.name, javadoc)

                        Task javadocJar = createJavadocJarTask(prj, ss, javadoc)
                        mergedConfiguration.javadoc.javadocJarTasks().put(ss.name, javadocJar)

                        mergedConfiguration.javadoc.projects().put(ss.name, prj)

                        updatePublications(prj, ss, javadocJar)
                    }
                }
            }

            project.tasks.withType(Javadoc) { task ->
                mergedConfiguration.javadoc.applyTo(task)
                options.footer = "Copyright &copy; ${mergedConfiguration.info.copyrightYear} ${mergedConfiguration.info.resolveAuthors().join(', ')}. All rights reserved."

                if (JavaVersion.current().isJava8Compatible()) {
                    options.addStringOption('Xdoclint:none', '-quiet')
                }
            }

            if (mergedConfiguration.javadoc.javadocTasks()) {
                project.tasks.create(ALL_JAVADOCS_TASK_NAME, DefaultTask) {
                    dependsOn mergedConfiguration.javadoc.javadocTasks()
                    group JavaBasePlugin.DOCUMENTATION_GROUP
                    description "Triggers all javadoc tasks for project ${project.name}"
                }
            }

            if (mergedConfiguration.javadoc.javadocJarTasks()) {
                project.tasks.create(ALL_JAVADOC_JARS_TASK_NAME, DefaultTask) {
                    dependsOn mergedConfiguration.javadoc.javadocJarTasks()
                    group JavaBasePlugin.DOCUMENTATION_GROUP
                    description "Triggers all javadocJar tasks for project ${project.name}"
                }
            }
        }
    }

    private Task createJavadocTaskIfNeeded(Project project, SourceSet sourceSet) {
        String taskName = resolveJavadocTaskName(sourceSet)

        Task javadocTask = project.tasks.findByName(taskName)

        if (!javadocTask) {
            String classesTaskName = sourceSet.name == 'main' ? 'classes' : sourceSet.name + 'Classes'

            javadocTask = project.tasks.create(taskName, Javadoc) {
                dependsOn project.tasks.getByName(classesTaskName)
                group JavaBasePlugin.DOCUMENTATION_GROUP
                description "Generates Javadoc API documentation [sourceSet ${sourceSet.name}]"
                source sourceSet.allSource
                destinationDir project.file("${project.buildDir}/docs/${sourceSet.name}/javadoc")
            }
        }

        javadocTask
    }

    static String resolveJavadocTaskName(SourceSet sourceSet) {
        return sourceSet.name == 'main' ? 'javadoc' : sourceSet.name + 'Javadoc'
    }

    private Task createJavadocJarTask(Project project, SourceSet sourceSet, Task javadoc) {
        String taskName = resolveJavadocJarTaskName(sourceSet)

        Task javadocJarTask = project.tasks.findByName(taskName)

        if (!javadocJarTask) {
            String archiveBaseName = sourceSet.name == 'main' ? project.name : project.name + '-' + sourceSet.name

            javadocJarTask = project.tasks.create(taskName, Jar) {
                dependsOn javadoc
                group JavaBasePlugin.DOCUMENTATION_GROUP
                description "An archive of the API docs [sourceSet ${sourceSet.name}]"
                baseName = archiveBaseName
                classifier 'javadoc'
                from javadoc.destinationDir
            }
        }

        javadocJarTask
    }

    static String resolveJavadocJarTaskName(SourceSet sourceSet) {
        return sourceSet.name == 'main' ? 'javadocJar' : sourceSet.name + 'JavadocJar'
    }

    private void updatePublications(Project project, SourceSet sourceSet, Task javadocJar) {
        project.publishing {
            publications {
                "${sourceSet.name}"(MavenPublication) {
                    artifact javadocJar
                }
            }
        }

        project.artifacts {
            javadocJar
        }
    }
}
