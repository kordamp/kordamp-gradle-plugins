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
package org.kordamp.gradle.plugin.project.java.internal

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.kordamp.gradle.plugin.project.java.PlatformHandler

import static java.util.Arrays.asList

/**
 *
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
class PlatformHandlerImpl implements PlatformHandler {
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

    PlatformHandlerImpl(Project project) {
        this.project = project
    }

    @Override
    void platform(Object platformGav) {
        platform(platformGav, DEFAULT_CONFIGURATIONS, null)
    }

    @Override
    void platform(Object platformGav, Action<? super Dependency> action) {
        platform(platformGav, DEFAULT_CONFIGURATIONS, action)
    }

    @Override
    void platform(Object platformGav, String... configurations) {
        platform(platformGav, configurations ? asList(configurations) : DEFAULT_CONFIGURATIONS, null)
    }

    @Override
    void platform(Object platformGav, List<String> configurations) {
        platform(platformGav, configurations, null)
    }

    @CompileDynamic
    @Override
    void platform(Object platformGav, List<String> configurations, Action<? super Dependency> action) {
        List<String> confs = configurations ?: DEFAULT_CONFIGURATIONS
        for (String configuration : confs) {
            Configuration c = project.configurations.findByName(configuration)
            if (c) {
                project.logger.debug("Configuring platform '${platformGav}' in ${configuration}")
                DependencyHandler dh = project.dependencies
                project.dependencies {
                    if (action) {
                        "${configuration}"(dh.platform(platformGav), action)
                    } else {
                        "${configuration}"(dh.platform(platformGav))
                    }
                }
            }
        }
    }

    @Override
    void enforcedPlatform(Object platformGav) {
        enforcedPlatform(platformGav, DEFAULT_CONFIGURATIONS, null)
    }

    @Override
    void enforcedPlatform(Object platformGav, Action<? super Dependency> action) {
        enforcedPlatform(platformGav, DEFAULT_CONFIGURATIONS, action)
    }

    @Override
    void enforcedPlatform(Object platformGav, String... configurations) {
        enforcedPlatform(platformGav, configurations ? asList(configurations) : DEFAULT_CONFIGURATIONS, null)
    }

    @Override
    void enforcedPlatform(Object platformGav, List<String> configurations) {
        enforcedPlatform(platformGav, configurations, null)
    }

    @CompileDynamic
    @Override
    void enforcedPlatform(Object platformGav, List<String> configurations, Action<? super Dependency> action) {
        List<String> confs = configurations ?: DEFAULT_CONFIGURATIONS
        for (String configuration : confs) {
            Configuration c = project.configurations.findByName(configuration)
            if (c) {
                project.logger.debug("Configuring enforced platform '${platformGav}' in ${configuration}")
                DependencyHandler dh = project.dependencies
                project.dependencies {
                    if (action) {
                        "${configuration}"(dh.enforcedPlatform(platformGav), action)
                    } else {
                        "${configuration}"(dh.enforcedPlatform(platformGav))
                    }
                }
            }
        }
    }
}
