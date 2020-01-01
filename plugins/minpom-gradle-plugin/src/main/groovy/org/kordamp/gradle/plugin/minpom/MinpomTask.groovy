/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2020 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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
import org.gradle.api.artifacts.ExcludeRule
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.PluginUtils.supportsApiConfiguration
import static org.kordamp.gradle.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @sinde 0.6.0
 */
@CacheableTask
class MinpomTask extends DefaultTask {
    @Optional @Input String projectGroupId
    @Optional @Input String projectArtifactId
    @Optional @Input String projectVersion

    @Optional @OutputDirectory File destinationDir

    MinpomTask() {
        projectGroupId = project.group
        projectArtifactId = project.name
        projectVersion = project.version
        destinationDir = project.file("${project.buildDir}/pom/maven")
    }

    @TaskAction
    void generateFiles() {
        Closure<Boolean> filter = { Dependency d ->
            d.name != 'unspecified'
        }

        Map<String, org.gradle.api.artifacts.Dependency> compileDependencies = project.configurations.findByName('compile')
            .allDependencies.findAll(filter)
            .collectEntries({ [("${it.group}:${it.name}:${it.version}"): it] })

        Map<String, org.gradle.api.artifacts.Dependency> runtimeDependencies = project.configurations.findByName('runtime')
            .allDependencies.findAll(filter)
            .collectEntries({ [("${it.group}:${it.name}:${it.version}"): it] })

        Map<String, org.gradle.api.artifacts.Dependency> testDependencies = project.configurations.findByName('testRuntime')
            .allDependencies.findAll(filter)
            .collectEntries({ [("${it.group}:${it.name}:${it.version}"): it] })

        Map<String, org.gradle.api.artifacts.Dependency> providedDependencies = project.configurations.findByName('compileOnly')
            .allDependencies.findAll(filter)
            .collectEntries({ [("${it.group}:${it.name}:${it.version}"): it] })

        if (supportsApiConfiguration(project)) {
            compileDependencies.putAll(project.configurations.findByName('api')
                ?.allDependencies.findAll(filter)
                ?.collectEntries({ [("${it.group}:${it.name}:${it.version}"): it] }))
            compileDependencies.putAll(project.configurations.findByName('implementation')
                ?.allDependencies.findAll(filter)
                ?.collectEntries({ [("${it.group}:${it.name}:${it.version}"): it] }))

            runtimeDependencies.putAll(project.configurations.findByName('runtimeOnly')
                ?.allDependencies.findAll(filter)
                ?.collectEntries({ [("${it.group}:${it.name}:${it.version}"): it] }))

            testDependencies.putAll(project.configurations.findByName('testImplementation')
                ?.allDependencies.findAll(filter)
                ?.collectEntries({ [("${it.group}:${it.name}:${it.version}"): it] }))
            testDependencies.putAll(project.configurations.findByName('testRuntimeOnly')
                ?.allDependencies.findAll(filter)
                ?.collectEntries({ [("${it.group}:${it.name}:${it.version}"): it] }))
        }

        compileDependencies.keySet().each { key ->
            runtimeDependencies.remove(key)
            testDependencies.remove(key)
        }
        runtimeDependencies.keySet().each { key ->
            testDependencies.remove(key)
        }

        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        boolean hasParent = isNotBlank(effectiveConfig.publishing.pom.parent)

        StringWriter writer = new StringWriter()
        writer.write('<?xml version="1.0" encoding="UTF-8"?>\n')
        MarkupBuilder builder = new MarkupBuilder(writer)
        builder.project(xmlns: 'http://maven.apache.org/POM/4.0.0',
            'xsi:schemaLocation': 'http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd',
            'xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance') {
            modelVersion('4.0.0')

            if (hasParent) {
                org.kordamp.gradle.plugin.base.model.Dependency parentPom = org.kordamp.gradle.plugin.base.model.Dependency.parseDependency(effectiveConfig.project, effectiveConfig.publishing.pom.parent, true)
                parent {
                    groupId(parentPom.groupId)
                    artifactId(parentPom.artifactId)
                    version(parentPom.version)
                }
            }

            groupId(projectGroupId)
            artifactId(projectArtifactId)
            version(projectVersion)

            if (compileDependencies || runtimeDependencies || testDependencies || providedDependencies) {
                dependencies {
                    compileDependencies.values().each { Dependency dep ->
                        MinpomTask.configureDependency(builder, project, dep, 'compile')
                    }
                    providedDependencies.values().each { Dependency dep ->
                        MinpomTask.configureDependency(builder, project, dep, 'provided')
                    }
                    runtimeDependencies.values().each { Dependency dep ->
                        MinpomTask.configureDependency(builder, project, dep, 'runtime')
                    }
                    testDependencies.values().each { Dependency dep ->
                        MinpomTask.configureDependency(builder, project, dep, 'test')
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

    static void configureDependency(MarkupBuilder builder, Project project, Dependency dep, String s) {
        builder.dependency {
            groupId(dep.group)
            artifactId(dep.name)
            version(dep.version)
            scope(s)
            if (MinpomTask.isOptional(project, dep)) {
                optional(true)
            }

            if (dep instanceof ModuleDependency) {
                ModuleDependency mdep = (ModuleDependency) dep
                if (mdep.excludeRules.size() > 0) {
                    exclusions {
                        mdep.excludeRules.each { ExcludeRule rule ->
                            exclusion {
                                groupId(rule.group)
                                artifactId(rule.module)
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isOptional(Project project, Dependency dependency) {
        project.findProperty('optionalDeps') && project.optionalDeps.contains(dependency)
    }
}
