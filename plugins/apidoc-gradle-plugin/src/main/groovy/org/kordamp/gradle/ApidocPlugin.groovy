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

import static org.kordamp.gradle.BasePlugin.isRootProject

/**
 * Configures a {@code javadocJar} for each {@code SourceSet}.
 * <strong>NOTE:</strong> any sources with the word "test" will be skipped.
 * Applies the {@code maven-publish} plugin if it has not been applied before.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
class ApidocPlugin implements Plugin<Project> {
    static final String ALL_JAVADOCS_TASK_NAME = 'allJavadocs'
    static final String ALL_JAVADOC_JARS_TASK_NAME = 'allJavadocJars'
    static final String AGGREGATE_JAVADOCS_TASK_NAME = 'aggregateJavadocs'
    static final String AGGREGATE_JAVADOCS_JAR_TASK_NAME = 'aggregateJavadocsJar'

    private static final String VISITED = ApidocPlugin.class.name.replace('.', '_') + '_VISITED'

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
        if (!project.plugins.findPlugin(ApidocPlugin)) {
            project.plugins.apply(ApidocPlugin)
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

        List<Task> aggregateTasks = []
        if (isRootProject(project) && !project.childProjects.isEmpty()) {
            aggregateTasks = createAggregateJavadocsTask(project)
        }

        project.afterEvaluate { Project prj ->
            ProjectConfigurationExtension extension = prj.extensions.findByType(ProjectConfigurationExtension)
            ProjectConfigurationExtension rootExtension = prj.rootProject.extensions.findByType(ProjectConfigurationExtension)

            if (!ProjectConfigurationExtension.apidocs(extension, rootExtension)) {
                return
            }

            List<Task> javadocTasks = []
            List<Task> javadocJarTasks = []

            prj.plugins.withType(JavaBasePlugin) {
                prj.sourceSets.each { SourceSet ss ->
                    // skip generating a javadoc task for SourceSets that may contain tests
                    if (!ss.name.toLowerCase().contains('test')) {
                        Task javadoc = createJavadocTaskIfNeeded(prj, ss)
                        javadocTasks << javadoc
                        Task javadocJar = createJavadocJarTask(prj, ss, javadoc)
                        javadocJarTasks << javadocJar
                        updatePublications(prj, ss, javadocJar)
                    }
                }
            }

            if (javadocTasks) {
                project.tasks.create(ALL_JAVADOCS_TASK_NAME, DefaultTask) {
                    dependsOn javadocTasks
                    group JavaBasePlugin.DOCUMENTATION_GROUP
                    description "Triggers all javadoc tasks for project ${project.name}"
                }
            }

            if (javadocJarTasks) {
                project.tasks.create(ALL_JAVADOC_JARS_TASK_NAME, DefaultTask) {
                    dependsOn javadocJarTasks
                    group JavaBasePlugin.DOCUMENTATION_GROUP
                    description "Triggers all javadocJar tasks for project ${project.name}"
                }
            }

            if (JavaVersion.current().isJava8Compatible()) {
                project.tasks.withType(Javadoc) {
                    options.addStringOption('Xdoclint:none', '-quiet')
                }
            }

            if (isRootProject(project) && !project.childProjects.isEmpty()) {
                project.evaluationDependsOnChildren()

                List<Javadoc> javadocs = []

                project.tasks.withType(Javadoc) { Javadoc javadoc -> if (javadoc.name != AGGREGATE_JAVADOCS_TASK_NAME) javadocs << javadoc }

                project.childProjects.values().each { Project p ->
                    p.tasks.withType(Javadoc) { Javadoc javadoc -> javadocs << javadoc }
                }

                javadocs = javadocs.unique()

                if(javadocs) {
                    aggregateTasks[0].configure {
                        enabled true
                        dependsOn javadocs
                        source javadocs.source
                        classpath = project.files(javadocs.classpath)
                    }
                    aggregateTasks[1].configure {
                        enabled true
                        from aggregateTasks[0].destinationDir
                    }
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

        project.tasks.withType(Javadoc) {
            options.use = true
            options.splitIndex = true
            options.encoding = 'UTF-8'
            options.author = true
            options.version = true
            options.windowTitle = "${project.name} ${project.version}"
            options.docTitle = "${project.name} ${project.version}"
            options.links 'https://docs.oracle.com/javase/8/docs/api/'
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

    private List<Task> createAggregateJavadocsTask(Project project) {
        Task aggregateJavadocs = project.tasks.create(AGGREGATE_JAVADOCS_TASK_NAME, Javadoc) {
            enabled false
            group JavaBasePlugin.DOCUMENTATION_GROUP
            description 'Aggregates API docs for all projects.'
            destinationDir project.file("${project.buildDir}/docs/javadoc")
        }

        Task aggregateJavadocsJar = project.tasks.create(AGGREGATE_JAVADOCS_JAR_TASK_NAME, Jar) {
            enabled false
            dependsOn aggregateJavadocs
            group JavaBasePlugin.DOCUMENTATION_GROUP
            description "An archive of the aggregate API docs"
            classifier 'javadoc'
        }

        [aggregateJavadocs, aggregateJavadocsJar]
    }
}
