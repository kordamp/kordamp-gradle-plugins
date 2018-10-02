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
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar

import static org.kordamp.gradle.BasePlugin.isRootProject

/**
 * Configures a {@code sourceJar} for each {@code SourceSet}.
 * <strong>NOTE:</strong> any sources with the word "test" will be skipped.
 * Applies the {@code maven-publish} plugin if it has not been applied before.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
class SourceJarPlugin implements Plugin<Project> {
    static final String VISITED = SourceJarPlugin.class.name.replace('.', '_') + '_VISITED'

    Project project

    void apply(Project project) {
        this.project = project

        if (isRootProject(project)) {
            createSourceJarTaskIfCompatible(project)
            project.childProjects.values().each { prj ->
                createSourceJarTaskIfCompatible(prj)
            }
        } else {
            createSourceJarTaskIfCompatible(project)
        }
    }

    private void createSourceJarTaskIfCompatible(Project project) {
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
            ProjectConfigurationExtension extension = project.extensions.findByType(ProjectConfigurationExtension)
            if (!extension.sources) {
                return
            }

            List<Task> sourceJarTasks = []

            prj.plugins.withType(JavaBasePlugin) {
                prj.sourceSets.each { SourceSet ss ->
                    // skip generating a source task for SourceSets that may contain tests
                    if (!ss.name.toLowerCase().contains('test')) {
                        Task sourceJar = createSourceJarTask(prj, ss)
                        sourceJarTasks << sourceJar
                        updatePublications(prj, ss, sourceJar)
                    }
                }
            }

            if (sourceJarTasks) {
                project.tasks.create('allSourceJars', DefaultTask) {
                    dependsOn sourceJarTasks
                    group 'Build'
                    description "Triggers all sourceJar tasks for project ${project.name}"
                }
            }
        }
    }

    private Task createSourceJarTask(Project project, SourceSet sourceSet) {
        String taskName = sourceSet.name == 'main' ? 'sourceJar' : sourceSet.name + 'SourceJar'

        Task sourceJarTask = project.tasks.findByName(taskName)

        if (!sourceJarTask) {
            String classesTaskName = sourceSet.name == 'main' ? 'classes' : sourceSet.name + 'Classes'
            String archiveBaseName = sourceSet.name == 'main' ? project.name : project.name + '-' + sourceSet.name

            sourceJarTask = project.tasks.create(taskName, Jar) {
                dependsOn project.tasks.getByName(classesTaskName)
                group 'Build'
                description "An archive of the source code [sourceSet ${sourceSet.name}]"
                baseName = archiveBaseName
                classifier 'sources'
                from sourceSet.allSource
            }
        }

        sourceJarTask
    }

    private void updatePublications(Project project, SourceSet sourceSet, Task sourceJar) {
        project.publishing {
            publications {
                "${sourceSet.name}"(MavenPublication) {
                    artifact sourceJar
                }
            }
        }

        project.artifacts {
            sourceJar
        }
    }
}
