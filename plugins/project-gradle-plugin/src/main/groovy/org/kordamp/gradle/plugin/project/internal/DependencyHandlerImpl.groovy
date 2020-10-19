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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.artifact.Dependency as KDependency
import org.kordamp.gradle.plugin.base.model.artifact.Platform
import org.kordamp.gradle.plugin.base.model.artifact.internal.DependencyManagementImpl
import org.kordamp.gradle.plugin.project.ConfigurationsDependencyHandler
import org.kordamp.gradle.plugin.project.DefaultConfigurationsDependencyHandler
import org.kordamp.gradle.plugin.project.DependencyHandlerSpec

import javax.inject.Inject

import static org.kordamp.gradle.util.PluginUtils.resolveConfig
import static org.kordamp.gradle.util.StringUtils.isBlank

/**
 *
 * @author Andres Almiray
 * @since 0.41.0
 */
@CompileStatic
class DependencyHandlerImpl {
    private static final Set<String> DEFAULT_CONFIGURATIONS = [
        'api',
        'implementation',
        'annotationProcessor',
        'testImplementation',
        'testAnnotationProcessor',
        'compileOnly',
        'testCompileOnly',
        'runtimeOnly',
        'testRuntimeOnly'
    ] as Set

    private ConfigurationsDependencyHandlerImpl cdh
    private DefaultConfigurationsDependencyHandlerImpl dcdh

    final Project project
    final List<Map<String, Object>> platforms = []

    @Inject
    DependencyHandlerImpl(Project project) {
        this.project = project
    }

    ConfigurationsDependencyHandler configurationsDependencyHandler() {
        cdh ? cdh : (cdh = new ConfigurationsDependencyHandlerImpl(this))
    }

    DefaultConfigurationsDependencyHandler defaultConfigurationsDependencyHandler() {
        dcdh ? dcdh : (dcdh = new DefaultConfigurationsDependencyHandlerImpl(this))
    }

    static class ConfigurationsDependencyHandlerImpl implements ConfigurationsDependencyHandler {
        final DependencyHandlerImpl owner

        ConfigurationsDependencyHandlerImpl(DependencyHandlerImpl owner) {
            this.owner = owner
        }

        List<Map<String, Object>> getPlatforms() {
            owner.platforms
        }

        @Override
        DependencyHandlerSpec configuration(String configuration) {
            if (isBlank(configuration)) {
                throw new IllegalArgumentException('Target configuration must not be blank')
            }
            return new DependencyHandlerSpecImpl(owner, [configuration] as Set)
        }

        @Override
        DependencyHandlerSpec configurations(String configuration, String... configurations) {
            if (isBlank(configuration)) {
                throw new IllegalArgumentException('Target configuration must not be blank')
            }

            Set<String> confs = [configuration] as Set
            if (configurations) confs.addAll(configurations.toList())
            return new DependencyHandlerSpecImpl(owner, confs)
        }

        @Override
        DependencyHandlerSpec configurations(Set<String> configurations) {
            if (!configurations) {
                throw new IllegalArgumentException('Target configurations must not be empty')
            }
            return new DependencyHandlerSpecImpl(owner, configurations)
        }

        @Override
        DependencyHandlerSpec c(String configuration) {
            this.configuration(configuration)
        }

        @Override
        DependencyHandlerSpec c(String configuration, String... configurations) {
            this.configurations(configuration, configurations)
        }

        @Override
        DependencyHandlerSpec c(Set<String> configurations) {
            this.configurations(configurations)
        }
    }

    @CompileDynamic
    private static class DefaultConfigurationsDependencyHandlerImpl extends DependencyHandlerSpecImpl implements DefaultConfigurationsDependencyHandler {
        DefaultConfigurationsDependencyHandlerImpl(DependencyHandlerImpl owner) {
            super(owner, DEFAULT_CONFIGURATIONS)
        }
    }

    private static class DependencyHandlerSpecImpl implements DependencyHandlerSpec {
        final DependencyHandlerImpl owner
        final Set<String> configurations = [] as Set

        DependencyHandlerSpecImpl(DependencyHandlerImpl owner, Set<String> configurations) {
            this.owner = owner
            this.configurations.addAll(configurations)
        }

        @Override
        void platform(Object notation) {
            configurePlatform(notation, configurations, null)
        }

        @Override
        void platform(Object notation, Action<? super Dependency> action) {
            configurePlatform(notation, configurations, action)
        }

        @Override
        void enforcedPlatform(Object notation) {
            configureEnforcedPlatform(notation, configurations, null)
        }

        @Override
        void enforcedPlatform(Object notation, Action<? super Dependency> action) {
            configureEnforcedPlatform(notation, configurations, action)
        }

        @Override
        void dependency(String nameOrGa) {
            configureDependency(nameOrGa, configurations, null)
        }

        @Override
        void dependency(String nameOrGa, Closure configurer) {
            configureDependency(nameOrGa, configurations, configurer)
        }

        @Override
        void module(String nameOrGa, String moduleName) {
            configureDependency(nameOrGa, moduleName, configurations, null)
        }

        @Override
        void module(String nameOrGa, String moduleName, Closure configurer) {
            configureDependency(nameOrGa, moduleName, configurations, configurer)
        }

        List<Map<String, Object>> getPlatforms() {
            owner.platforms
        }

        Project getProject() {
            owner.project
        }

        private Object maybeResolveGav(Object notation, boolean platform = false) {
            if (notation instanceof CharSequence) {
                String str = String.valueOf(notation)
                Project prj = project.rootProject.findProject(str)
                if (prj) return prj

                KDependency dependency = resolveConfig(project).dependencyManagement.findDependencyByName(str)
                if (dependency) {
                    prj = project.rootProject.findProject(dependency.artifactId)
                    if (prj) return prj
                    if (platform && !(dependency instanceof Platform)) {
                        throw new IllegalArgumentException("Target dependency is not a platform: ${notation}")
                    }
                    return dependency.gav
                }
            } else if (notation instanceof KDependency) {
                KDependency dependency = (KDependency) notation
                if (dependency) {
                    Project prj = project.rootProject.findProject(dependency.artifactId)
                    if (prj) return prj
                    if (platform && !(dependency instanceof Platform)) {
                        throw new IllegalArgumentException("Target dependency is not a platform: ${notation}")
                    }
                    return dependency.gav
                }
            }
            notation
        }

        private void configurePlatform(Object notation, Set<String> configurations, Action<? super Dependency> action) {
            Object gavOrNotation = maybeResolveGav(notation, true)

            Set<String> confs = configurations ?: DEFAULT_CONFIGURATIONS
            Set<String> actualConfs = []

            for (String configuration : confs) {
                if (isBlank(configuration)) {
                    throw new IllegalArgumentException('Target configuration must not be blank')
                }

                if (project.configurations.findByName(configuration)) {
                    project.logger.debug("Configuring platform '${gavOrNotation}' in ${configuration}")
                    DependencyHandler dh = project.dependencies
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
                } else {
                    throw new IllegalArgumentException("Target configuration '${configuration}' was not found")
                }
            }

            platforms << [
                platform      : notation,
                enforced      : false,
                configurations: actualConfs
            ]
        }

        private void configureEnforcedPlatform(Object notation, Set<String> configurations, Action<? super Dependency> action) {
            Object gavOrNotation = maybeResolveGav(notation, true)

            Set<String> confs = configurations ?: DEFAULT_CONFIGURATIONS
            Set<String> actualConfs = []

            for (String configuration : confs) {
                if (isBlank(configuration)) {
                    throw new IllegalArgumentException('Target configuration must not be blank')
                }

                if (project.configurations.findByName(configuration)) {
                    project.logger.debug("Configuring enforced platform '${gavOrNotation}' in ${configuration}")
                    DependencyHandler dh = project.dependencies
                    if (action) {
                        dh.add(configuration, dh.enforcedPlatform(gavOrNotation, action))
                    } else {
                        dh.add(configuration, dh.enforcedPlatform(gavOrNotation))
                    }
                    actualConfs << configuration
                } else {
                    throw new IllegalArgumentException("Target configuration '${configuration}' was not found")
                }
            }

            platforms << [
                platform      : notation,
                enforced      : true,
                configurations: actualConfs
            ]
        }

        private void configureDependency(String nameOrGa, Set<String> configurations, Closure configurer) {
            ProjectConfigurationExtension config = resolveConfig(project)

            for (String configuration : configurations) {
                if (isBlank(configuration)) {
                    throw new IllegalArgumentException('Target configuration must not be blank')
                }

                if (project.configurations.findByName(configuration)) {
                    if (configurer) {
                        project.dependencies.add(configuration, config.dependencyManagement.getDependency(nameOrGa).gav, configurer)
                    } else {
                        project.dependencies.add(configuration, config.dependencyManagement.getDependency(nameOrGa).gav)
                    }
                } else {
                    throw new IllegalArgumentException("Target configuration '${configuration}' was not found")
                }
            }
        }

        private void configureDependency(String nameOrGa, String moduleName, Set<String> configurations, Closure configurer) {
            ProjectConfigurationExtension config = resolveConfig(project)

            for (String configuration : configurations) {
                if (isBlank(configuration)) {
                    throw new IllegalArgumentException('Target configuration must not be blank')
                }

                Platform platform = config.dependencyManagement.findPlatform(nameOrGa)
                if (platform) {
                    ((DependencyManagementImpl) config.dependencyManagement).registerDeferredPlatformModule(project,
                        configuration, platform, moduleName, configurer)
                } else {
                    if (project.configurations.findByName(configuration)) {
                        if (configurer) {
                            project.dependencies.add(configuration, config.dependencyManagement.findDependency(nameOrGa).asGav(moduleName), configurer)
                        } else {
                            project.dependencies.add(configuration, config.dependencyManagement.findDependency(nameOrGa).asGav(moduleName))
                        }
                    } else {
                        throw new IllegalArgumentException("Target configuration '${configuration}' was not found")
                    }
                }
            }
        }
    }
}
