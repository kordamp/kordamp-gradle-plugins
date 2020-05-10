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

    final List<Map<String, Object>> platforms = []

    PlatformHandlerImpl(Project project) {
        this.project = project
    }

    @Override
    void platform(Object notation) {
        platform(notation, DEFAULT_CONFIGURATIONS, null)
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

    @CompileDynamic
    @Override
    void platform(Object notation, List<String> configurations, Action<? super Dependency> action) {
        List<String> confs = configurations ?: DEFAULT_CONFIGURATIONS
        List<String> actualConfs = []
        for (String configuration : confs) {
            Configuration c = project.configurations.findByName(configuration)
            if (c) {
                project.logger.debug("Configuring platform '${notation}' in ${configuration}")
                DependencyHandler dh = project.dependencies
                project.dependencies {
                    if (action) {
                        "${configuration}"(dh.platform(notation), action)
                    } else {
                        "${configuration}"(dh.platform(notation))
                    }
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
    void enforcedPlatform(Object notation) {
        enforcedPlatform(notation, DEFAULT_CONFIGURATIONS, null)
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

    @CompileDynamic
    @Override
    void enforcedPlatform(Object notation, List<String> configurations, Action<? super Dependency> action) {
        List<String> confs = configurations ?: DEFAULT_CONFIGURATIONS
        List<String> actualConfs = []
        for (String configuration : confs) {
            Configuration c = project.configurations.findByName(configuration)
            if (c) {
                project.logger.debug("Configuring enforced platform '${notation}' in ${configuration}")
                DependencyHandler dh = project.dependencies
                project.dependencies {
                    if (action) {
                        "${configuration}"(dh.enforcedPlatform(notation), action)
                    } else {
                        "${configuration}"(dh.enforcedPlatform(notation))
                    }
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
}
