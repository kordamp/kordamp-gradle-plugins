/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2022 Andres Almiray.
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
package org.kordamp.gradle.plugin.base.model.artifact

import groovy.transform.CompileStatic
import org.gradle.api.Action

/**
 * @author Andres Almiray
 * @since 0.41.0
 */
@CompileStatic
interface DependencyManagement {
    Dependency dependency(Dependency dependency)

    /**
     * Defines a dependency.</p>
     * Dependency name will be equal to parsed {@code artifactId}.</p>
     * Example:
     *
     * <pre>
     * dependency('com.googlecode.guava:guava:29.0-jre')
     * </pre>
     * @param gavNotation must be in the {@code groupId:artifactId:version} notation
     */
    Dependency dependency(String gavNotation)

    /**
     * Defines a dependency.</p>
     * Example:
     *
     * <pre>
     * dependency('guava', 'com.googlecode.guava:guava:29.0-jre')
     * </pre>
     *
     * @param name the logical name of the dependency
     * @param gavNotation must be in the {@code groupId:artifactId:version} notation
     */
    Dependency dependency(String name, String gavNotation)

    /**
     * Defines and configures a dependency.</p>
     * Example:
     *
     * <pre>
     * dependency('groovy', 'org.codehaus.groovy:groovy:3.0.6') {*     modules = [
     *         'groovy-test',
     *         'groovy-json',
     *         'groovy-xml'
     *     ]
     *}* </pre>
     *
     * @param name the logical name of the dependency
     * @param gavNotation must be in the {@code groupId:artifactId:version} notation
     */
    Dependency dependency(String name, String gavNotation, Action<? super DependencySpec> action)

    /**
     * Defines and configures a dependency.</p>
     * Example:
     *
     * <pre>
     * dependency('groovy', 'org.codehaus.groovy:groovy:3.0.6') {*     modules = [
     *         'groovy-test',
     *         'groovy-json',
     *         'groovy-xml'
     *     ]
     *}* </pre>
     *
     * @param name the logical name of the dependency
     * @param gavNotation must be in the {@code groupId:artifactId:version} notation
     */
    Dependency dependency(String name, String gavNotation, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DependencySpec) Closure<Void> action)

    /**
     * Defines and configures a dependency by gavNotation.</p>
     * Example:
     *
     * <pre>
     * dependency('org.codehaus.groovy:groovy:3.0.6') {*     modules = [
     *         'groovy-test',
     *         'groovy-json',
     *         'groovy-xml'
     *     ]
     *}* </pre>
     *
     * @param gavNotation must be in the {@code groupId:artifactId:version} notation
     */
    Dependency dependency(String gavNotation, Action<? super DependencySpec> action)

    /**
     * Defines and configures a dependency by gavNotation.</p>
     * Example:
     *
     * <pre>
     * dependency('org.codehaus.groovy:groovy:3.0.6') {*     modules = [
     *         'groovy-test',
     *         'groovy-json',
     *         'groovy-xml'
     *     ]
     *}* </pre>
     *
     * @param gavNotation must be in the {@code groupId:artifactId:version} notation
     */
    Dependency dependency(String gavNotation, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DependencySpec) Closure<Void> action)

    /**
     * Defines a platform dependency.</p>
     * Dependency name will be equal to parsed {@code artifactId}.</p>
     * Example:
     *
     * <pre>
     * platform('io.micronaut:micronaut-bom:2.0.2')
     * </pre>
     *
     * @param gavNotation must be in the {@code groupId:artifactId:version} notation
     */
    Platform platform(String gavNotation)

    /**
     * Defines a platform dependency.</p>
     * Example:
     *
     * <pre>
     * platform('micronaut', 'io.micronaut:micronaut-bom:2.0.2')
     * </pre>
     *
     * @param name the logical name of the dependency
     * @param gavNotation must be in the {@code groupId:artifactId:version} notation
     */
    Platform platform(String name, String gavNotation)

    /**
     * Defines and configures a platform dependency.</p>
     * Example:
     *
     * <pre>
     * platform('micronaut', 'io.micronaut:micronaut-bom:2.0.2') {*     modules = [
     *         'micronaut-core',
     *         'micronaut-inject',
     *         'micronaut-validation'
     *     ]
     *}* </pre>
     *
     * @param name the logical name of the dependency
     * @param gavNotation must be in the {@code groupId:artifactId:version} notation
     */
    Platform platform(String name, String gavNotation, Action<? super PlatformSpec> action)

    /**
     * Defines and configures a platform dependency.</p>
     * Example:
     *
     * <pre>
     * platform('micronaut', 'io.micronaut:micronaut-bom:2.0.2') {*     modules = [
     *         'micronaut-core',
     *         'micronaut-inject',
     *         'micronaut-validation'
     *     ]
     *}* </pre>
     *
     * @param name the logical name of the dependency
     * @param gavNotation must be in the {@code groupId:artifactId:version} notation
     */
    Platform platform(String name, String gavNotation, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PlatformSpec) Closure<Void> action)

    /**
     * Retrieves a declared dependency by name.
     *
     * @param nameOrGa the logical name or the {@code groupId:artifactId} of the dependency to find
     * @return {@code org.kordamp.gradle.plugin.base.model.artifact.Dependency} instance when found.
     * @throws IllegalArgumentException if the named dependency is not found
     */
    Dependency getDependency(String nameOrGa)

    /**
     * Retrieves a declared dependency by name.
     *
     * @param nameOrGa the logical name or the {@code groupId:artifactId} of the dependency to find
     * @return {@code org.kordamp.gradle.plugin.base.model.artifact.Dependency} instance when found or {@code null}.
     */
    Dependency findDependency(String nameOrGa)

    /**
     * Retrieves a declared dependency by name.
     *
     * @param name the logical name of the dependency to find
     * @return {@code org.kordamp.gradle.plugin.base.model.artifact.Dependency} instance when found, {@code null} otherwise.
     * @throws IllegalArgumentException if the given name is blank
     */
    Dependency findDependencyByName(String name)
    /**
     * Retrieves a declared dependency by grouId and artifactId.
     *
     * @param groupId the groupId of the dependency to find
     * @param artifactId the artifactId of the dependency to find
     * @return {@code org.kordamp.gradle.plugin.base.model.artifact.Dependency} instance when found, {@code null} otherwise.
     * @throws IllegalArgumentException if the given groupId or artifactId are blank
     */
    Dependency findDependencyByGA(String groupId, String artifactId)

    /**
     * Retrieves a declared platform by name.
     *
     * @param nameOrGa the logical name or the {@code groupId:artifactId} of the platform to find
     * @return {@code org.kordamp.gradle.plugin.base.model.artifact.Platform} instance when found.
     * @throws IllegalArgumentException if the named platform is not found
     */
    Platform getPlatform(String nameOrGa)

    /**
     * Retrieves a declared platform by name.
     *
     * @param nameOrGa the logical name or the {@code groupId:artifactId} of the platform to find
     * @return {@code org.kordamp.gradle.plugin.base.model.artifact.Platform} instance when found or {@code null.
     */
    Platform findPlatform(String nameOrGa)

    /**
     * Retrieves a declared platform by name.
     *
     * @param name the logical name of the platform to find
     * @return {@code org.kordamp.gradle.plugin.base.model.artifact.Platform} instance when found, {@code null} otherwise.
     * @throws IllegalArgumentException if the given name is blank
     */
    Platform findPlatformByName(String name)

    /**
     * Retrieves a declared platform by grouId and artifactId.
     *
     * @param groupId the groupId of the platform to find
     * @param artifactId the artifactId of the platform to find
     * @return {@code org.kordamp.gradle.plugin.base.model.artifact.Platform} instance when found, {@code null} otherwise.
     * @throws IllegalArgumentException if the given groupId or artifactId are blank
     */
    Platform findPlatformByGA(String groupId, String artifactId)

    /**
     * Formats the named dependency in {@code groupId:artifactId:version} notation.
     * @param nameOrGa the logical name or the {@code groupId:artifactId} of the dependency to find
     * @throws IllegalArgumentException if the named dependency is not found
     */
    String gav(String nameOrGa)

    /**
     * Formats the named module dependency in {@code groupId:artifactId:version} notation.
     * @param nameOrGa the logical name or the {@code groupId:artifactId} of the dependency to find
     * @param moduleName the name of the module to find.
     * @throws IllegalArgumentException if the given args are blank or the named dependency is not found
     */
    String gav(String nameOrGa, String moduleName)

    /**
     * Formats the named module dependency in {@code groupId:artifactId} notation.
     * @param nameOrGa the logical name or the {@code groupId:artifactId} of the dependency to find
     * @param moduleName the name of the module to find.
     * @throws IllegalArgumentException if the given args are blank or the named dependency is not found
     */
    String ga(String nameOrGa, String moduleName)

    /**
     * A view of all registered dependencies and platforms, keyed by logical name.
     * The returned view is unmodifiable.
     */
    Map<String, Dependency> getDependencies()

    Map<String, Collection<Object>> toMap()

    void resolve()
}
