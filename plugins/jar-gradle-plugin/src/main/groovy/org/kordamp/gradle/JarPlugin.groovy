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
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar

/**
 * Creates a {@code Jar} task per {@code SourceSet}.
 * Configures Manifest and MetaInf entries if the {@code projectConfiguration.release} is enabled.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
class JarPlugin implements Plugin<Project> {
    static final String VISITED = JarPlugin.class.name.replace('.', '_') + '_VISITED'

    Project project

    void apply(Project project) {
        this.project = project

        if (isRootProject(project)) {
            createJarTaskIfCompatible(project)
            project.childProjects.values().each { prj ->
                createJarTaskIfCompatible(prj)
            }
        } else {
            createJarTaskIfCompatible(project)
        }
    }

    static boolean isRootProject(Project project) {
        project == project.rootProject
    }

    private void createJarTaskIfCompatible(Project project) {
        String visitedPropertyName = VISITED + '_' + project.name
        if (project.findProperty(visitedPropertyName)) {
            return
        }
        project.ext[visitedPropertyName] = true

        BasePlugin.applyIfMissing(project)
        BuildInfoPlugin.applyIfMissing(project)
        MinPomPlugin.applyIfMissing(project)

        List<Task> jarTasks = []

        project.afterEvaluate { Project prj ->
            prj.plugins.withType(JavaBasePlugin) {
                prj.sourceSets.each { SourceSet ss ->
                    // skip generating/updating a jar task for SourceSets that may contain tests
                    if (!ss.name.toLowerCase().contains('test')) {
                        jarTasks << createJarTaskIfNeeded(prj, ss)
                    }
                }
            }

            if (jarTasks) {
                project.tasks.create('allJars', DefaultTask) {
                    dependsOn jarTasks
                    group 'Build'
                    description "Triggers all jar tasks for project ${project.name}"
                }
            }
        }
    }

    private Task createJarTaskIfNeeded(Project project, SourceSet sourceSet) {
        String taskName = sourceSet.name == 'main' ? 'jar' : sourceSet.name + 'Jar'

        Task jarTask = project.tasks.findByName(taskName)

        if (!jarTask) {
            String archiveBaseName = sourceSet.name == 'main' ? project.name : project.name + '-' + sourceSet.name

            jarTask = project.tasks.create(taskName, Jar) {
                dependsOn sourceSet.output
                group 'Build'
                description "Assembles a jar archive [sourceSet ${sourceSet.name}]"
                baseName = archiveBaseName
                from sourceSet.output
                destinationDir project.file("${project.buildDir}/libs")
            }
        }

        ProjectConfigurationExtension extension = project.extensions.findByType(ProjectConfigurationExtension)
        if (extension.minpom && extension.release) {
            jarTask.configure {
                dependsOn MinPomPlugin.resolveMinPomTaskName(sourceSet)
                manifest {
                    attributes(
                        'Created-By': project.rootProject.buildCreatedBy,
                        'Built-By': project.rootProject.buildBy,
                        'Build-Jdk': project.rootProject.buildJdk,
                        'Build-Date': project.rootProject.buildDate,
                        'Build-Time': project.rootProject.buildTime,
                        'Build-Revision': project.rootProject.buildRevision,
                        'Specification-Title': project.name,
                        'Specification-Version': project.version,
                        'Specification-Vendor': project.name
                    )
                }

                metaInf {
                    from(project.rootProject.file('.')) {
                        include 'LICENSE*'
                    }
                    from(MinPomPlugin.resolveMinPomDestinationDir(project, sourceSet)) {
                        into "maven/${project.group}/${project.name}"
                    }
                }
            }
        }

        jarTask
    }
}
