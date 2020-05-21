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
package org.kordamp.gradle.plugin.project.internal

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.artifact.Dependency as KDependency
import org.kordamp.gradle.plugin.project.DependencyHandler

import static java.util.Arrays.asList
import static org.kordamp.gradle.util.PluginUtils.resolveConfig

/**
 *
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
class DependencyHandlerImpl implements DependencyHandler {
    private static final List<String> DEFAULT_CONFIGURATIONS = [
        'api',
        'implementation',
        'annotationProcessor',
        'testImplementation',
        'testAnnotationProcessor',
        'compileOnly',
        'testCompileOnly',
        'runtimeOnly',
        'testRuntimeOnly'
    ]

    private final Project project

    final List<Map<String, Object>> platforms = []

    DependencyHandlerImpl(Project project) {
        this.project = project
    }

    private Object maybeResolveGav(Object notation) {
        if (notation instanceof CharSequence) {
            String str = String.valueOf(notation)
            Project prj = project.rootProject.findProject(str)
            if (prj) return prj

            KDependency dependency = resolveConfig(project).dependencies.findDependencyByName(str)
            if (dependency) {
                prj = project.rootProject.findProject(dependency.artifactId)
                if (prj) return prj
                return dependency.gav
            }
        } else if (notation instanceof KDependency) {
            KDependency dependency = (KDependency) notation
            if (dependency) {
                Project prj = project.rootProject.findProject(dependency.artifactId)
                if (prj) return prj
                return dependency.gav
            }
        }
        notation
    }

    @Override
    void platform(Object notation, Action<? super Dependency> action) {
        platform(notation, DEFAULT_CONFIGURATIONS, action)
    }

    @Override
    void platform(Object notation, String... configurations) {
        platform(notation, configurations ? asList(configurations) : DEFAULT_CONFIGURATIONS, null)
    }

    @Override
    void platform(Object notation, List<String> configurations) {
        platform(notation, configurations, null)
    }

    @Override
    void platform(Object notation, List<String> configurations, Action<? super Dependency> action) {
        Object gavOrNotation = maybeResolveGav(notation)

        List<String> confs = configurations ?: DEFAULT_CONFIGURATIONS
        List<String> actualConfs = []

        for (String configuration : confs) {
            if (project.configurations.findByName(configuration)) {
                project.logger.debug("Configuring platform '${gavOrNotation}' in ${configuration}")
                org.gradle.api.artifacts.dsl.DependencyHandler dh = project.dependencies
                if (action) {
                    Dependency d = dh.platform(gavOrNotation, action)
                    dh.add(configuration, d)
                    notation = "${d.group}:${d.name}:${d.version}".toString()
                } else {
                    Dependency d = dh.platform(gavOrNotation)
                    dh.add(configuration, d)
                    notation = "${d.group}:${d.name}:${d.version}".toString()
                }
                actualConfs << configuration
            }
        }

        platforms << [
            platform      : notation,
            enforced      : false,
            configurations: actualConfs
        ]
    }

    @Override
    void enforcedPlatform(Object notation, Action<? super Dependency> action) {
        enforcedPlatform(notation, DEFAULT_CONFIGURATIONS, action)
    }

    @Override
    void enforcedPlatform(Object notation, String... configurations) {
        enforcedPlatform(notation, configurations ? asList(configurations) : DEFAULT_CONFIGURATIONS, null)
    }

    @Override
    void enforcedPlatform(Object notation, List<String> configurations) {
        enforcedPlatform(notation, configurations, null)
    }

    @Override
    void enforcedPlatform(Object notation, List<String> configurations, Action<? super Dependency> action) {
        Object gavOrNotation = maybeResolveGav(notation)
        List<String> confs = configurations ?: DEFAULT_CONFIGURATIONS
        List<String> actualConfs = []
        for (String configuration : confs) {
            if (project.configurations.findByName(configuration)) {
                project.logger.debug("Configuring enforced platform '${gavOrNotation}' in ${configuration}")
                org.gradle.api.artifacts.dsl.DependencyHandler dh = project.dependencies
                if (action) {
                    dh.add(configuration, dh.enforcedPlatform(gavOrNotation, action))
                } else {
                    dh.add(configuration, dh.enforcedPlatform(gavOrNotation))
                }
                actualConfs << configuration
            }
        }

        platforms << [
            platform      : notation,
            enforced      : true,
            configurations: actualConfs
        ]
    }

    @Override
    void dependency(String name, String configuration, String... configurations) {
        ProjectConfigurationExtension config = resolveConfig(project)
        if (project.configurations.findByName(configuration)) {
            project.dependencies.add(configuration, config.dependencies.getDependency(name).gav)
        }

        if (configurations) {
            for (String conf : configurations) {
                if (project.configurations.findByName(conf)) {
                    project.dependencies.add(conf, config.dependencies.getDependency(name).gav)
                }
            }
        }
    }

    @Override
    void dependency(String name, String configuration, Closure configurer) {
        ProjectConfigurationExtension config = resolveConfig(project)
        if (project.configurations.findByName(configuration)) {
            if (configurer) {
                project.dependencies.add(configuration, config.dependencies.getDependency(name).gav, configurer)
            } else {
                project.dependencies.add(configuration, config.dependencies.getDependency(name).gav)
            }
        }
    }

    @Override
    void module(String name, String moduleName, String configuration, String... configurations) {
        ProjectConfigurationExtension config = resolveConfig(project)
        if (project.configurations.findByName(configuration)) {
            project.dependencies.add(configuration, config.dependencies.getDependency(name).asGav(moduleName))
        }

        if (configurations) {
            for (String conf : configurations) {
                if (project.configurations.findByName(conf)) {
                    project.dependencies.add(conf, config.dependencies.getDependency(name).asGav(moduleName))
                }
            }
        }
    }

    @Override
    void module(String name, String moduleName, String configuration, Closure configurer) {
        ProjectConfigurationExtension config = resolveConfig(project)
        if (project.configurations.findByName(configuration)) {
            if (configurer) {
                project.dependencies.add(configuration, config.dependencies.getDependency(name).asGav(moduleName), configurer)
            } else {
                project.dependencies.add(configuration, config.dependencies.getDependency(name).asGav(moduleName))
            }
        }
    }
}
