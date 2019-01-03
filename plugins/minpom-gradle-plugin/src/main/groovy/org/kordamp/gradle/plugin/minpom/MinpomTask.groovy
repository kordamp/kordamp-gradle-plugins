/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
package org.kordamp.gradle.plugin.minpom

import groovy.xml.MarkupBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * @author Andres Almiray
 * @sinde 0.6.0
 */
@CacheableTask
class MinpomTask extends DefaultTask {
    @Optional @Input String projectGroupId
    @Optional @Input String projectArtifactId
    @Optional @Input String projectVersion

    @PathSensitive(PathSensitivity.RELATIVE)
    @Optional @OutputDirectory File destinationDir

    MinpomTask() {
        projectGroupId = project.group
        projectArtifactId = project.name
        projectVersion = project.version
        destinationDir = project.file("${project.buildDir}/pom/maven")
    }

    @TaskAction
    void generateFiles() {
        Map<String, Dependency> compileDependencies = project.configurations.compile.allDependencies.findAll({
            it.name != 'unspecified'
        })
            .collectEntries({ [("${it.group}:${it.name}:${it.version}"): it] })
        Map<String, Dependency> runtimeDependencies = project.configurations.runtime.allDependencies.findAll({
            it.name != 'unspecified'
        })
            .collectEntries({ [("${it.group}:${it.name}:${it.version}"): it] })
        Map<String, Dependency> testDependencies = project.configurations.testRuntime.allDependencies.findAll({
            it.name != 'unspecified'
        })
            .collectEntries({ [("${it.group}:${it.name}:${it.version}"): it] })

        compileDependencies.keySet().each { key ->
            runtimeDependencies.remove(key)
            testDependencies.remove(key)
        }
        runtimeDependencies.keySet().each { key ->
            testDependencies.remove(key)
        }

        StringWriter writer = new StringWriter()
        writer.write('<?xml version="1.0" encoding="UTF-8"?>\n')
        new MarkupBuilder(writer).project(xmlns: 'http://maven.apache.org/POM/4.0.0',
            'xsi:schemaLocation': 'http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd',
            'xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance') {
            modelVersion('4.0.0')
            groupId(projectGroupId)
            artifactId(projectArtifactId)
            version(projectVersion)

            if (compileDependencies || runtimeDependencies || testDependencies) {
                dependencies {
                    compileDependencies.values().each { Dependency dep ->
                        dependency {
                            groupId(dep.group)
                            artifactId(dep.name)
                            version(dep.version)
                            scope('compile')
                            if (MinpomTask.isOptional(project, dep)) {
                                optional(true)
                            }
                        }
                    }
                    runtimeDependencies.values().each { Dependency dep ->
                        dependency {
                            groupId(dep.group)
                            artifactId(dep.name)
                            version(dep.version)
                            scope('runtime')
                            if (MinpomTask.isOptional(project, dep)) {
                                optional(true)
                            }
                        }
                    }
                    testDependencies.values().each { Dependency dep ->
                        dependency {
                            groupId(dep.group)
                            artifactId(dep.name)
                            version(dep.version)
                            scope('test')
                            if (MinpomTask.isOptional(project, dep)) {
                                optional(true)
                            }
                        }
                    }
                }
            }
        }

        destinationDir.mkdirs()

        File minPomfile = project.file("${destinationDir}/pom.xml")
        File minPomProps = project.file("${destinationDir}/pom.properties")

        minPomfile.text = writer.toString()

        minPomProps.text = """|# Generated by Gradle ${project.gradle.gradleVersion}
                              |version=${projectVersion}
                              |groupId=${projectGroupId}
                              |artifactId=${projectArtifactId}
                              |""".stripMargin('|')
    }

    private static boolean isOptional(Project project, Dependency dependency) {
        project.findProperty('optionalDeps') && project.optionalDeps.contains(dependency)
    }
}
