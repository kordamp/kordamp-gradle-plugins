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
package org.kordamp.gradle.plugin.project.java

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.artifacts.Dependency

/**
 * Configures platform dependencies on a given set of configurations.
 *
 * The following configurations will be used by default if no configurations are supplied:
 *
 * <ul>
 *   <li>api</li>
 *   <li>implementation</li>
 *   <li>annotationProcessor</li>
 *   <li>testImplementation</li>
 *   <li>testAnnotationProcessor</li>
 *   <li>compileOnly</li>
 *   <li>testCompileOnly</li>
 *   <li>runtimeOnly</li>
 *   <li>testRuntimeOnly</li>
 * </ul>
 *
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
interface PlatformHandler {
    /**
     * Declares a dependency on a platform.
     *
     * @param notation - the coordinates of the platform
     */
    void platform(Object notation)

    /**
     * Declares a dependency on a platform.
     *
     * @param notation - the coordinates of the platform
     * @action - the dependency configuration block
     */
    void platform(Object notation, Action<? super Dependency> action)

    /**
     * Declares a dependency on a platform.
     *
     * @param notation - the coordinates of the platform
     * @param configurations - the set of configurations to use
     */
    void platform(Object notation, String... configurations)

    /**
     * Declares a dependency on a platform.
     *
     * @param notation - the coordinates of the platform
     * @param configurations - the set of configurations to use
     */
    void platform(Object notation, List<String> configurations)

    /**
     * Declares a dependency on a platform.
     *
     * @param notation - the coordinates of the platform
     * @param configurations - the set of configurations to use
     * @action - the dependency configuration block
     */
    void platform(Object notation, List<String> configurations, Action<? super Dependency> action)

    /**
     * Declares a dependency on an enforced platform.
     *
     * @param notation - the coordinates of the platform
     */
    void enforcedPlatform(Object notation)

    /**
     * Declares a dependency on an enforced platform.
     *
     * @param notation - the coordinates of the platform
     * @action - the dependency configuration block
     */
    void enforcedPlatform(Object notation, Action<? super Dependency> action)

    /**
     * Declares a dependency on an enforced platform.
     *
     * @param notation - the coordinates of the platform
     * @param configurations - the set of configurations to use
     */
    void enforcedPlatform(Object notation, String... configurations)

    /**
     * Declares a dependency on an enforced platform.
     *
     * @param notation - the coordinates of the platform
     * @param configurations - the set of configurations to use
     */
    void enforcedPlatform(Object notation, List<String> configurations)

    /**
     * Declares a dependency on an enforced platform.
     *
     * @param notation - the coordinates of the platform
     * @param configurations - the set of configurations to use
     * @action - the dependency configuration block
     */
    void enforcedPlatform(Object notation, List<String> configurations, Action<? super Dependency> action)
}
