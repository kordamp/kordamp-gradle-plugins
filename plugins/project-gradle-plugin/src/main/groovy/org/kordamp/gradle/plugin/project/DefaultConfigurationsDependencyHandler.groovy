/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2024 Andres Almiray.
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
package org.kordamp.gradle.plugin.project

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.artifacts.Dependency

/**
 * Applies platforms on default configurations.
 *
 * The following configurations will be used:
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
 * @since 0.41.0
 */
@CompileStatic
interface DefaultConfigurationsDependencyHandler {
    /**
     * Declares a dependency on a platform.
     * Valid values for {@code notation} are:
     * <ul>
     *     <li>a {@code java.lang.CharSequence} with format groupId:artifactId:version</li>
     *     <li>a {@code java.util.Map} with group, artifactId, version keys</li>
     *     <li>a {@code java.lang.CharSequence} with the name of a platform declared in {@code config.dependencies}</li>
     * </ul>
     *
     * @param notation - the coordinates of the platform
     */
    void platform(Object notation)

    /**
     * Declares a dependency on a platform.
     * Valid values for {@code notation} are:
     * <ul>
     *     <li>a {@code java.lang.CharSequence} with format groupId:artifactId:version</li>
     *     <li>a {@code java.util.Map} with group, artifactId, version keys</li>
     *     <li>a {@code java.lang.CharSequence} with the name of a platform declared in {@code config.dependencies}</li>
     * </ul>
     *
     * @param notation - the coordinates of the platform
     * @param action the dependency configuration block
     */
    void platform(Object notation, Action<? super Dependency> action)

    /**
     * Declares and configures a dependency on an enforced platform.
     * Valid values for {@code notation} are:
     * <ul>
     *     <li>a {@code java.lang.CharSequence} with format groupId:artifactId:version</li>
     *     <li>a {@code java.util.Map} with group, artifactId, version keys</li>
     *     <li>a {@code java.lang.CharSequence} with the name of a platform declared in {@code config.dependencies}</li>
     * </ul>
     *
     * @param notation - the coordinates of the platform
     */
    void enforcedPlatform(Object notation)

    /**
     * Declares and configures a dependency on an enforced platform.
     * Valid values for {@code notation} are:
     * <ul>
     *     <li>a {@code java.lang.CharSequence} with format groupId:artifactId:version</li>
     *     <li>a {@code java.util.Map} with group, artifactId, version keys</li>
     *     <li>a {@code java.lang.CharSequence} with the name of a platform declared in {@code config.dependencies}</li>
     * </ul>
     *
     * @param notation - the coordinates of the platform
     * @param action the dependency configuration block
     */
    void enforcedPlatform(Object notation, Action<? super Dependency> action)
}
