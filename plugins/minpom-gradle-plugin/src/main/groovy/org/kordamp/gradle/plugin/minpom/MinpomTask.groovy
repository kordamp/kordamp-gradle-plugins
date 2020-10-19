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

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import groovy.xml.MarkupBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.ExcludeRule
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.artifact.Dependency
import org.kordamp.gradle.plugin.base.model.artifact.Platform

import static org.kordamp.gradle.util.PluginUtils.resolveConfig
import static org.kordamp.gradle.util.PluginUtils.supportsApiConfiguration
import static org.kordamp.gradle.util.StringUtils.isBlank
import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @sinde 0.6.0
 */
@CacheableTask
class MinpomTask extends DefaultTask {
    @Optional
    @Input
    String projectGroupId
    @Optional
    @Input
    String projectArtifactId
    @Optional
    @Input
    String projectVersion

    @Optional
    @OutputDirectory
    File destinationDir

    MinpomTask() {
        projectGroupId = project.group
        projectArtifactId = project.name
        projectVersion = project.version
        destinationDir = project.file("${project.buildDir}/pom/maven")
    }

    @TaskAction
    void generateFiles() {
        Closure<Boolean> filter = { org.gradle.api.artifacts.Dependency d ->
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
            runtimeDependencies.putAll(project.configurations.findByName('implementation')
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

        ProjectConfigurationExtension config = resolveConfig(project)

        Map<String, String> versionExpressions = [:]
        Set<Platform> platforms = [] as Set
        Set<DependencyPair> processedDependencies = []

        versionExpressions.putAll(config.publishing.pom.properties)

        // platforms.each { Dependency dependency ->
        //     String versionKey = dependency.name + '.version'
        //     versionExpressions.put(versionKey, dependency.version)
        //     dependency.version = '${' + versionKey + '}'
        // }

        compileDependencies.values().each { org.gradle.api.artifacts.Dependency dep ->
            processDependency(dep, config, versionExpressions, platforms, processedDependencies, 'compile')
        }
        runtimeDependencies.values().each { org.gradle.api.artifacts.Dependency dep ->
            processDependency(dep, config, versionExpressions, platforms, processedDependencies, 'runtime')
        }
        providedDependencies.values().each { org.gradle.api.artifacts.Dependency dep ->
            processDependency(dep, config, versionExpressions, platforms, processedDependencies, 'provided')
        }
        testDependencies.values().each { org.gradle.api.artifacts.Dependency dep ->
            processDependency(dep, config, versionExpressions, platforms, processedDependencies, 'test')
        }

        boolean hasParent = isNotBlank(config.publishing.pom.parent)

        StringWriter writer = new StringWriter()
        writer.write('<?xml version="1.0" encoding="UTF-8"?>\n')
        MarkupBuilder builder = new MarkupBuilder(writer)
        builder.project(xmlns: 'http://maven.apache.org/POM/4.0.0',
            'xsi:schemaLocation': 'http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd',
            'xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance') {
            modelVersion('4.0.0')

            if (hasParent) {
                ParsedDependency parentPom = ParsedDependency.parseDependency(config.project, config.publishing.pom.parent, true)
                parent {
                    groupId(parentPom.groupId)
                    artifactId(parentPom.artifactId)
                    version(parentPom.version)
                }
            }

            groupId(projectGroupId)
            artifactId(projectArtifactId)
            version(projectVersion)

            if (versionExpressions) {
                properties {
                    versionExpressions.each { versionKey, versionVal ->
                        "${versionKey}"(versionVal)
                    }
                }
            }

            if (platforms && !config.publishing.flattenPlatforms) {
                dependencyManagement {
                    dependencies {
                        platforms.each { Platform dep ->
                            dependency {
                                groupId(dep.groupId)
                                artifactId(dep.artifactId)
                                version(dep.version)
                                scope('import')
                                type('pom')
                            }
                        }
                    }
                }
            }

            if (processedDependencies) {
                dependencies {
                    processedDependencies.each { DependencyPair dep ->
                        MinpomTask.configureDependency(builder, project, dep)
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


    private static void processDependency(org.gradle.api.artifacts.Dependency dep,
                                          ProjectConfigurationExtension config,
                                          Map<String, String> versionExpressions,
                                          Set<Platform> platforms,
                                          Set<DependencyPair> processedDependencies,
                                          String scope) {
        String versionExp = dep.version

        if (config.publishing.useVersionExpressions) {
            Dependency dependency = config.dependencyManagement.findDependencyByGA(dep.group, dep.name)
            if (dependency) {
                if (config.publishing.flattenPlatforms) {
                    if (dependency instanceof Platform) {
                        if (dependency.artifactId == dep.name) {
                            platforms << (Platform) dependency
                            return
                        } else {
                            String versionKey = dependency.name + '.version'
                            versionExp = '${' + versionKey + '}'
                            versionExpressions.put(versionKey, dependency.version)
                        }
                    } else if (versionExp == dependency.version || !versionExp) {
                        String versionKey = dependency.name + '.version'
                        versionExp = '${' + versionKey + '}'
                        versionExpressions.put(versionKey, dependency.version)
                    }
                } else {
                    if (dependency instanceof Platform) {
                        if (dependency.artifactId == dep.name) {
                            platforms << (Platform) dependency
                            return
                        } else {
                            versionExp = ''
                        }
                    } else if (versionExp == dependency.version || !versionExp) {
                        String versionKey = dependency.name + '.version'
                        versionExp = '${' + versionKey + '}'
                        versionExpressions.put(versionKey, dependency.version)
                    }
                }
            }
        } else {
            Dependency dependency = config.dependencyManagement.findDependencyByGA(dep.group, dep.name)
            if (dependency) {
                if (config.publishing.flattenPlatforms) {
                    versionExp = dependency.version
                    if (dependency instanceof Platform && dependency.artifactId == dep.name) {
                        platforms << (Platform) dependency
                        return
                    }
                } else {
                    if (dependency instanceof Platform) {
                        if (dependency.artifactId == dep.name) {
                            platforms << (Platform) dependency
                            return
                        } else {
                            versionExp = ''
                        }
                    } else {
                        versionExp = dependency.version
                    }
                }
            }
        }

        processedDependencies << new DependencyPair(
            new ParsedDependency(dep.group, dep.name, versionExp),
            dep, scope)
    }

    private static void configureDependency(MarkupBuilder builder, Project project, DependencyPair dep) {
        builder.dependency {
            groupId(dep.parsed.groupId)
            artifactId(dep.parsed.artifactId)
            if (dep.parsed.version) version(dep.parsed.version)
            if (dep.scope != 'compile') scope(dep.scope)
            if (MinpomTask.isOptional(project, dep.gradle)) {
                optional(true)
            }

            if (dep.gradle instanceof ModuleDependency) {
                ModuleDependency mdep = (ModuleDependency) dep.gradle
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

    private static boolean isOptional(Project project, org.gradle.api.artifacts.Dependency dependency) {
        project.findProperty('optionalDeps') && project.optionalDeps.contains(dependency)
    }

    @CompileStatic
    @Canonical
    private static class DependencyPair {
        final ParsedDependency parsed
        final org.gradle.api.artifacts.Dependency gradle
        final String scope

    }

    @CompileStatic
    @Canonical
    @TupleConstructor
    private static class ParsedDependency {
        final String groupId
        final String artifactId
        final String version

        static ParsedDependency parseDependency(Project project, String str) {
            parseDependency(project, str, false)
        }

        static ParsedDependency parseDependency(Project project, String str, boolean expandCoords) {
            String[] parts = str.trim().split(':')
            switch (parts.length) {
                case 0:
                    throw new IllegalStateException("Project '${str}' does not exist")
                case 1:
                    if (isNotBlank(parts[0]) &&
                        (project.rootProject.name == parts[0] || project.rootProject.subprojects.find { it.name == parts[0] })) {
                        return new ParsedDependency(
                            project.group.toString(),
                            parts[0],
                            expandCoords ? project.version.toString() : '${project.version}')
                    }
                    throw new IllegalStateException("Project '${str}' does not exist")
                case 2:
                    if (isBlank(parts[0]) &&
                        isNotBlank(parts[1]) &&
                        (project.rootProject.name == parts[1] || project.rootProject.subprojects.find { it.name == parts[1] })) {
                        return new ParsedDependency(
                            project.group.toString(),
                            parts[1],
                            expandCoords ? project.version.toString() : '${project.version}')
                    }
                    throw new IllegalStateException("Project '${str}' does not exist")
                case 3:
                    if (isBlank(parts[0]) || isBlank(parts[1]) || isBlank(parts[2])) {
                        throw new IllegalStateException("Invalid BOM dependency '${str}'")
                    }
                    return new ParsedDependency(parts[0], parts[1], parts[2])
            }
        }
    }
}
