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
package org.kordamp.gradle.plugin.jar

import org.gradle.BuildAdapter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.bundling.Jar
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.buildinfo.BuildInfoPlugin
import org.kordamp.gradle.plugin.minpom.MinPomPlugin

import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * Configures a {@code jar} task.
 * Configures Manifest and MetaInf entries if the {@code config.release} is enabled.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
class JarPlugin implements Plugin<Project> {
    private static final String VISITED = JarPlugin.class.name.replace('.', '_') + '_VISITED'

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
        if (!project.plugins.findPlugin(JarPlugin)) {
            project.plugins.apply(JarPlugin)
        }
    }

    private void configureProject(Project project) {
        String visitedPropertyName = VISITED + '_' + project.name
        if (project.findProperty(visitedPropertyName)) {
            return
        }
        project.ext[visitedPropertyName] = true

        BasePlugin.applyIfMissing(project)
        BuildInfoPlugin.applyIfMissing(project)
        MinPomPlugin.applyIfMissing(project)

        project.afterEvaluate {
            project.plugins.withType(JavaBasePlugin) {
                createJarTaskIfNeeded(project)
            }
        }

        project.rootProject.gradle.addBuildListener(new BuildAdapter() {
            @Override
            void projectsEvaluated(Gradle gradle) {
                project.tasks.withType(Jar) { Jar jarTask ->
                    if(jarTask.name == 'jar') configureJarMetainf(project, jarTask)
                    configureJarManifest(project, jarTask)
                }
            }
        })
    }

    private void createJarTaskIfNeeded(Project project) {
        if (!project.sourceSets.findByName('main')) return

        String taskName = 'jar'

        Task jarTask = project.tasks.findByName(taskName)

        if (!jarTask) {
            jarTask = project.tasks.create(taskName, Jar) {
                dependsOn project.sourceSets.main.output
                group org.gradle.api.plugins.BasePlugin.BUILD_GROUP
                description 'Assembles a jar archive'
                from project.sourceSets.main.output
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
    }

    private void configureJarMetainf(Project project, Jar jarTask) {
        ProjectConfigurationExtension mergedConfiguration = project.rootProject.ext.mergedConfiguration

        if (mergedConfiguration.minpom.enabled) {
            jarTask.configure {
                dependsOn MinPomPlugin.MINPOM_TASK_NAME
                metaInf {
                    from(MinPomPlugin.resolveMinPomDestinationDir(project)) {
                        into "maven/${project.group}/${project.name}"
                    }
                }
            }
        }
    }

    private void configureJarManifest(Project project, Jar jarTask) {
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
    }
}
