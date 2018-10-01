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

/**
 * Calculates {@code pom.xml} and {@code pom.properties} for each {@code SourceSet}.
 * These files should be packaged under {@code /META-INF/maven}.

 * @author Andres Almiray
 */
class MinPomPlugin implements Plugin<Project> {
    static final String VISITED = MinPomPlugin.class.name.replace('.', '_') + '_VISITED'

    Project project

    void apply(Project project) {
        this.project = project

        if (isRootProject(project)) {
            createMinPomTaskIdCompatible(project)
            project.childProjects.values().each { prj ->
                createMinPomTaskIdCompatible(prj)
            }
        } else {
            createMinPomTaskIdCompatible(project)
        }
    }

    static boolean isRootProject(Project project) {
        project == project.rootProject
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(MinPomPlugin)) {
            project.plugins.apply(MinPomPlugin)
        }
    }

    private void createMinPomTaskIdCompatible(Project project) {
        String visitedPropertyName = VISITED + '_' + project.name
        if (project.findProperty(visitedPropertyName)) {
            return
        }
        project.ext[visitedPropertyName] = true

        BasePlugin.applyIfMissing(project)

        project.afterEvaluate { Project prj ->
            ProjectConfigurationExtension extension = project.extensions.findByType(ProjectConfigurationExtension)
            if (!extension.minpom) {
                return
            }

            prj.plugins.withType(JavaBasePlugin) {
                prj.sourceSets.each { SourceSet ss ->
                    // skip generating a task for SourceSets that may contain tests
                    if (!ss.name.toLowerCase().contains('test')) {
                        createMinPomTask(prj, ss)
                    }
                }
            }
        }
    }

    private Task createMinPomTask(Project project, SourceSet sourceSet) {
        String taskName = resolveMinPomTaskName(sourceSet)

        Task minPomTask = project.tasks.findByName(taskName)

        if (!minPomTask) {
            String classesTaskName = sourceSet.name == 'main' ? 'classes' : sourceSet.name + 'Classes'

            File minPomDestinationDir = resolveMinPomDestinationDir(project, sourceSet)
            File minPomfile = project.file("${minPomDestinationDir}/pom.xml")
            File minPomProps = project.file("${minPomDestinationDir}/pom.properties")

            minPomTask = project.tasks.create(taskName, DefaultTask) {
                dependsOn project.tasks.getByName(classesTaskName)
                group 'Build'
                description "Generates a minimum POM file [sourceSet ${sourceSet.name}]"
                outputs.dir(minPomDestinationDir)
                inputs.property('projectName', project.name)
                inputs.property('projectGroup', project.group)
                inputs.property('projectVersion', project.version)
            }

            minPomTask.doLast {
                String pomHeader = """|<?xml version="1.0" encoding="UTF-8"?>
                                      |<project xmlns="http://maven.apache.org/POM/4.0.0"
                                      |         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"
                                      |         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                                      |    <modelVersion>4.0.0</modelVersion>
                                      |    <groupId>${project.group}</groupId>
                                      |    <artifactId>${project.name}</artifactId>
                                      |    <version>${project.version}</version>
                                      |""".stripMargin('|')

                def dependencyTemplate = { dep ->
                    """|        <dependency>
                       |            <groupId>$dep.group</groupId>
                       |            <artifactId>$dep.name</artifactId>
                       |            <version>$dep.version</version>
                       |        </dependency>""".stripMargin('|')
                }

                def optionalDependencyTemplate = { dep ->
                    """|        <dependency>
                       |            <groupId>$dep.group</groupId>
                       |            <artifactId>$dep.name</artifactId>
                       |            <version>$dep.version</version>
                       |            <optional>true</optional>
                       |        </dependency>""".stripMargin('|')
                }

                def dependencyEntry = { dep ->
                    project.findProperty('optionalDeps') && project.optionalDeps.contains(dep) ? optionalDependencyTemplate(dep) : dependencyTemplate(dep)
                }

                String deps = project.configurations.runtime.allDependencies.findAll({ it.name != 'unspecified' })
                    .collect({ dep -> dependencyEntry(dep) }).join('\n')

                String pom = pomHeader
                if (deps) {
                    pom += "    <dependencies>\n$deps\n    </dependencies>\n"
                }
                pom += "</project>"

                minPomDestinationDir.mkdirs()
                minPomfile.text = pom
                minPomProps.text = """|# Generated by Gradle ${project.gradle.gradleVersion}
                                      |version=${project.version}
                                      |groupId=${project.group}
                                      |artifactId=${project.name}
                                      |""".stripMargin('|')
            }
        }

        minPomTask
    }

    static String resolveMinPomTaskName(SourceSet sourceSet) {
        sourceSet.name == 'main' ? 'minPom' : sourceSet.name + 'MinPom'
    }

    static File resolveMinPomDestinationDir(Project project, SourceSet sourceSet) {
        project.file("${project.buildDir}/pom/${sourceSet.name}/maven")
    }
}
