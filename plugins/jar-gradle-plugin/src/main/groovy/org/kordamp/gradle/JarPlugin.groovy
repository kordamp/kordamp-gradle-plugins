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
import org.kordamp.gradle.model.Information

import static org.kordamp.gradle.BasePlugin.isRootProject

/**
 * Creates a {@code Jar} task per {@code SourceSet}.
 * Configures Manifest and MetaInf entries if the {@code config.release} is enabled.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
class JarPlugin implements Plugin<Project> {
    static final String ALL_JARS_TASK_NAME = 'allJars'

    private static final String VISITED = JarPlugin.class.name.replace('.', '_') + '_VISITED'

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

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(JarPlugin)) {
            project.plugins.apply(JarPlugin)
        }
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

        project.plugins.withType(JavaBasePlugin) {
            if (!project.plugins.findPlugin(MavenPublishPlugin)) {
                project.plugins.apply(MavenPublishPlugin)
            }
        }

        List<Task> jarTasks = []

        project.afterEvaluate { Project prj ->
            prj.plugins.withType(JavaBasePlugin) {
                prj.sourceSets.each { SourceSet ss ->
                    // skip generating/updating a jar task for SourceSets that may contain tests
                    if (!ss.name.toLowerCase().contains('test')) {
                        Task jar = createJarTaskIfNeeded(prj, ss)
                        jarTasks << jar
                        updatePublications(prj, ss, jar)
                    }
                }
            }

            if (jarTasks) {
                project.tasks.create(ALL_JARS_TASK_NAME, DefaultTask) {
                    dependsOn jarTasks
                    group org.gradle.api.plugins.BasePlugin.BUILD_GROUP
                    description "Triggers all jar tasks for project ${project.name}"
                }
            }
        }
    }

    private Task createJarTaskIfNeeded(Project project, SourceSet sourceSet) {
        String taskName = resolveJarTaskName(sourceSet)

        Task jarTask = project.tasks.findByName(taskName)

        if (!jarTask) {
            String archiveBaseName = resolveArchiveBaseName(project, sourceSet)

            jarTask = project.tasks.create(taskName, Jar) {
                dependsOn sourceSet.output
                group org.gradle.api.plugins.BasePlugin.BUILD_GROUP
                description "Assembles a jar archive [sourceSet ${sourceSet.name}]"
                baseName = archiveBaseName
                from sourceSet.output
                destinationDir project.file("${project.buildDir}/libs")
            }
        }

        jarTask.configure {
            metaInf {
                from(project.rootProject.file('.')) {
                    include 'LICENSE*'
                }
            }
        }

        ProjectConfigurationExtension mergedConfiguration = project.rootProject.ext.mergedConfiguration

        if (mergedConfiguration.release) {
            jarTask.configure {
                Map<String, String> attributesMap = [
                    'Created-By'    : project.rootProject.buildinfo.buildCreatedBy,
                    'Built-By'      : project.rootProject.buildinfo.buildBy,
                    'Build-Jdk'     : project.rootProject.buildinfo.buildJdk,
                    'Build-Date'    : project.rootProject.buildinfo.buildDate,
                    'Build-Time'    : project.rootProject.buildinfo.buildTime,
                    'Build-Revision': project.rootProject.buildinfo.buildRevision
                ]

                if (mergedConfiguration.info.specification.enabled) {
                    attributesMap.'Specification-Title' = mergedConfiguration.info.specification.title
                    attributesMap.'Specification-Version' = mergedConfiguration.info.specification.version
                    if (mergedConfiguration.info.specification.vendor) attributesMap.'Specification-Vendor' = mergedConfiguration.info.specification.vendor
                }

                if (mergedConfiguration.info.implementation.enabled) {
                    attributesMap.'Implementation-Title' = mergedConfiguration.info.implementation.title
                    attributesMap.'Implementation-Version' = mergedConfiguration.info.implementation.version
                    if (mergedConfiguration.info.implementation.vendor) attributesMap.'Implementation-Vendor' = mergedConfiguration.info.implementation.vendor
                }

                manifest {
                    attributes(attributesMap)
                }
            }
        }

        if (mergedConfiguration.minpom.enabled) {
            jarTask.configure {
                dependsOn MinPomPlugin.resolveMinPomTaskName(sourceSet)
                metaInf {
                    from(MinPomPlugin.resolveMinPomDestinationDir(project, sourceSet)) {
                        into "maven/${project.group}/${project.name}"
                    }
                }
            }
        }

        jarTask
    }

    static String resolveArchiveBaseName(Project project, SourceSet sourceSet) {
        return sourceSet.name == 'main' ? project.name : project.name + '-' + sourceSet.name
    }

    private void updatePublications(Project project, SourceSet sourceSet, Task jar) {
        project.publishing {
            publications {
                "${sourceSet.name}"(MavenPublication) {
                    from project.components.java
                    // artifact jar
                }
            }
        }

        project.artifacts {
            jar
        }
    }

    static String resolveJarTaskName(SourceSet sourceSet) {
        return sourceSet.name == 'main' ? 'jar' : sourceSet.name + 'Jar'
    }
}
