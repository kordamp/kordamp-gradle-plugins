/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2021 Andres Almiray.
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
 * Configures dependencies on a given set of configurations.
 *
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
@Deprecated
interface DependencyHandler {
    /**
     * Declares a dependency on a platform.
     * Valid values for {@code notation} are:
     * <ul>
     *     <li>a {@code java.lang.CharSequence} with format groupId:artifactId:version</li>
     *     <li>a {@code java.util.Map} with group, artifactId, version keys</li>
     *     <li>a {@code java.lang.CharSequence} with the name of a platform declared in {@code config.dependencies}</li>
     * </ul>
     *
     * The following configurations will be used by default:
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
     * @param notation - the coordinates of the platform
     * @param action the dependency configuration block
     */
    @Deprecated
    void platform(Object notation, Action<? super Dependency> action)

    /**
     * Declares a dependency on a platform.
     * Valid values for {@code notation} are:
     * <ul>
     *     <li>a {@code java.lang.CharSequence} with format groupId:artifactId:version</li>
     *     <li>a {@code java.util.Map} with group, artifactId, version keys</li>
     *     <li>a {@code java.lang.CharSequence} with the name of a platform declared in {@code config.dependencies}</li>
     * </ul>
     *
     * The following configurations will be used by default if none are supplied:
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
     * @param notation - the coordinates of the platform
     * @param configurations - the set of configurations to use
     */
    @Deprecated
    void platform(Object notation, String... configurations)

    /**
     * Declares a dependency on a platform.
     * Valid values for {@code notation} are:
     * <ul>
     *     <li>a {@code java.lang.CharSequence} with format groupId:artifactId:version</li>
     *     <li>a {@code java.util.Map} with group, artifactId, version keys</li>
     *     <li>a {@code java.lang.CharSequence} with the name of a platform declared in {@code config.dependencies}</li>
     * </ul>
     *
     * The following configurations will be used by default if none are supplied:
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
     * @param notation - the coordinates of the platform
     * @param configurations - the set of configurations to use
     */
    @Deprecated
    void platform(Object notation, List<String> configurations)

    /**
     * Declares and configures a dependency on a platform.
     *
     * The following configurations will be used by default if none are supplied:
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
     * @param notation - the coordinates of the platform
     * @param configurations - the set of configurations to use
     * @param action the dependency configuration block
     */
    @Deprecated
    void platform(Object notation, List<String> configurations, Action<? super Dependency> action)

    /**
     * Declares and configures a dependency on an enforced platform.
     * Valid values for {@code notation} are:
     * <ul>
     *     <li>a {@code java.lang.CharSequence} with format groupId:artifactId:version</li>
     *     <li>a {@code java.util.Map} with group, artifactId, version keys</li>
     *     <li>a {@code java.lang.CharSequence} with the name of a platform declared in {@code config.dependencies}</li>
     * </ul>
     *
     * The following configurations will be used by default:
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
     * @param notation - the coordinates of the platform
     * @param action the dependency configuration block
     */
    @Deprecated
    void enforcedPlatform(Object notation, Action<? super Dependency> action)

    /**
     * Declares a dependency on an enforced platform.
     * Valid values for {@code notation} are:
     * <ul>
     *     <li>a {@code java.lang.CharSequence} with format groupId:artifactId:version</li>
     *     <li>a {@code java.util.Map} with group, artifactId, version keys</li>
     *     <li>a {@code java.lang.CharSequence} with the name of a platform declared in {@code config.dependencies}</li>
     * </ul>
     *
     * The following configurations will be used by default if none are supplied:
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
     * @param notation - the coordinates of the platform
     * @param configurations - the set of configurations to use
     */
    @Deprecated
    void enforcedPlatform(Object notation, String... configurations)

    /**
     * Declares a dependency on an enforced platform.
     * Valid values for {@code notation} are:
     * <ul>
     *     <li>a {@code java.lang.CharSequence} with format groupId:artifactId:version</li>
     *     <li>a {@code java.util.Map} with group, artifactId, version keys</li>
     *     <li>a {@code java.lang.CharSequence} with the name of a platform declared in {@code config.dependencies}</li>
     * </ul>
     *
     * The following configurations will be used by default if none are supplied:
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
     * @param notation - the coordinates of the platform
     * @param configurations - the set of configurations to use
     */
    @Deprecated
    void enforcedPlatform(Object notation, List<String> configurations)

    /**
     * Declares a dependency on an enforced platform.
     * Valid values for {@code notation} are:
     * <ul>
     *     <li>a {@code java.lang.CharSequence} with format groupId:artifactId:version</li>
     *     <li>a {@code java.util.Map} with group, artifactId, version keys</li>
     *     <li>a {@code java.lang.CharSequence} with the name of a platform declared in {@code config.dependencies}</li>
     * </ul>
     *
     * The following configurations will be used by default if none are supplied:
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
     * @param notation - the coordinates of the platform
     * @param configurations - the set of configurations to use
     * @param action the dependency configuration block
     */
    @Deprecated
    void enforcedPlatform(Object notation, List<String> configurations, Action<? super Dependency> action)

    /**
     * Declares a dependency on target configuration(s).
     * Dependencies must be declared using the {@code config.dependencies} block before using this method.
     *
     * @param nameOrGa the name or the {@code groupId:artifactId} of the dependency found in {@code config.dependencies}.
     * @param configuration the target configuration, e.g, <tt>api</tt>.
     * @param configurations additional configurations (if any).
     */
    @Deprecated
    void dependency(String nameOrGa, String configuration, String... configurations)

    /**
     * Declares a dependency on target a configuration.
     * Dependencies must be declared using the {@code config.dependencies} block before using this method.
     *
     * @param nameOrGa the name or the {@code groupId:artifactId} of the dependency found in {@code config.dependencies}.
     * @param configuration the target configuration, e.g, <tt>api</tt>.
     * @param configurer the closure to use to configure the dependency.
     */
    @Deprecated
    void dependency(String nameOrGa, String configuration, Closure configurer)

    /**
     * Declares a module dependency on target configuration(s).
     * Dependencies must be declared using the {@code config.dependencies} block before using this method.
     *
     * @param nameOrGa the name or the {@code groupId:artifactId} of the dependency found in {@code config.dependencies}.
     * @param configuration the target configuration, e.g, <tt>api</tt>.
     * @param configurations additional configurations (if any).
     */
    @Deprecated
    void module(String nameOrGa, String moduleName, String configuration, String... configurations)

    /**
     * Declares a module dependency on target a configuration.
     * Dependencies must be declared using the {@code config.dependencies} block before using this method.
     *
     * @param nameOrGa the name or the {@code groupId:artifactId} of the dependency found in {@code config.dependencies}.
     * @param configuration the target configuration, e.g, <tt>api</tt>.
     * @param configurer the closure to use to configure the dependency.
     */
    @Deprecated
    void module(String nameOrGa, String moduleName, String configuration, Closure configurer)
}
