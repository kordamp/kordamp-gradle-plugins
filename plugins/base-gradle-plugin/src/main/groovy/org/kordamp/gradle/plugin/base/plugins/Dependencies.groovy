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
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.artifact.Dependency
import org.kordamp.gradle.plugin.base.model.artifact.DependencySpec
import org.kordamp.gradle.plugin.base.model.artifact.HasModulesSpec
import org.kordamp.gradle.plugin.base.model.artifact.internal.DependencySpecImpl
import org.kordamp.gradle.util.CollectionUtils
import org.kordamp.gradle.util.ConfigureUtil

import static org.kordamp.gradle.util.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
class Dependencies {
    final Project project
    protected final ProjectConfigurationExtension config
    private final Map<String, Dependency> dependencies = [:]

    Dependencies(ProjectConfigurationExtension config, Project project) {
        this.config = config
        this.project = project
    }

    Map<String, Dependency> getDependencies() {
        Collections.unmodifiableMap(dependencies)
    }

    @CompileDynamic
    Map<String, Collection<Object>> toMap() {
        [dependencies: dependencies.values()*.toMap()]
    }

    static void merge(Dependencies o1, Dependencies o2) {
        CollectionUtils.merge(o1.@dependencies, o2.@dependencies)
    }

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
    Dependency dependency(String gavNotation) {
        if (isBlank(gavNotation)) {
            throw new IllegalArgumentException('Dependency notation cannot be blank.')
        }
        DependencySpecImpl d = DependencySpecImpl.parsePartial(project.rootProject, gavNotation)
        d.validate(project)
        dependencies[d.name] = d.asDependency()
    }

    /**
     * Defines a dependency.</p>
     * Example:
     *
     * <pre>
     * dependency('guava', 'com.googlecode.guava:guava:29.0-jre')
     * </pre>
     *
     * @param name the logical name of the dependency
     * @param gavNotation  must be in the {@code groupId:artifactId:version} notation
     */
    Dependency dependency(String name, String gavNotation) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Dependency name cannot be blank.')
        }
        DependencySpecImpl d = DependencySpecImpl.parse(project.rootProject, name.trim(), gavNotation)
        d.validate(project)
        dependencies[d.name] = d.asDependency()
    }

    /**
     * Defines and configures a dependency.</p>
     * Example:
     *
     * <pre>
     * dependency('groovy', 'org.codehaus.groovy:groovy:3.0.6') {
     *     modules = [
     *         'groovy-test',
     *         'groovy-json',
     *         'groovy-xml'
     *     ]
     * }
     * </pre>
     *
     * @param name the logical name of the dependency
     * @param gavNotation  must be in the {@code groupId:artifactId:version} notation
     */
    Dependency dependency(String name, String gavNotation, Action<? super HasModulesSpec> action) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Dependency name cannot be blank.')
        }
        DependencySpecImpl d = DependencySpecImpl.parse(project.rootProject, name.trim(), gavNotation)
        action.execute(d)
        d.validate(project)
        dependencies[d.name] = d.asDependency()
    }

    /**
     * Defines and configures a dependency.</p>
     * Example:
     *
     * <pre>
     * dependency('groovy', 'org.codehaus.groovy:groovy:3.0.6') {
     *     modules = [
     *         'groovy-test',
     *         'groovy-json',
     *         'groovy-xml'
     *     ]
     * }
     * </pre>
     *
     * @param name the logical name of the dependency
     * @param gavNotation  must be in the {@code groupId:artifactId:version} notation
     */
    Dependency dependency(String name, String gavNotation, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DependencySpec) Closure<Void> action) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Dependency name cannot be blank.')
        }
        DependencySpecImpl d = DependencySpecImpl.parse(project.rootProject, name.trim(), gavNotation)
        ConfigureUtil.configure(action, d)
        d.validate(project)
        dependencies[d.name] = d.asDependency()
    }

    /**
     * Defines a platform dependency.</p>
     * Dependency name will be equal to parsed {@code artifactId}.</p>
     * Example:
     *
     * <pre>
     * platform('io.micronaut:micronaut-bom:2.0.2')
     * </pre>
     *
     * @param gavNotation  must be in the {@code groupId:artifactId:version} notation
     */
    Dependency platform(String gavNotation) {
        if (isBlank(gavNotation)) {
            throw new IllegalArgumentException('Platform notation cannot be blank.')
        }
        DependencySpecImpl d = DependencySpecImpl.parsePartial(project.rootProject, gavNotation)
        d.platform = true
        d.validate(project)
        dependencies[d.name] = d.asDependency()
    }

    /**
     * Defines a platform dependency.</p>
     * Example:
     *
     * <pre>
     * platform('micronaut', 'io.micronaut:micronaut-bom:2.0.2')
     * </pre>
     *
     * @param name the logical name of the dependency
     * @param gavNotation  must be in the {@code groupId:artifactId:version} notation
     */
    Dependency platform(String name, String gavNotation) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Platform name cannot be blank.')
        }
        DependencySpecImpl d = DependencySpecImpl.parse(project.rootProject, name.trim(), gavNotation)
        d.platform = true
        d.validate(project)
        dependencies[d.name] = d.asDependency()
    }

    /**
     * Defines and configures a platform dependency.</p>
     * Example:
     *
     * <pre>
     * platform('micronaut', 'io.micronaut:micronaut-bom:2.0.2') {
     *     modules = [
     *         'micronaut-core',
     *         'micronaut-inject',
     *         'micronaut-validation'
     *     ]
     * }
     * </pre>
     *
     * @param name the logical name of the dependency
     * @param gavNotation  must be in the {@code groupId:artifactId:version} notation
     */
    Dependency platform(String name, String gavNotation, Action<? super HasModulesSpec> action) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Platform name cannot be blank.')
        }
        DependencySpecImpl d = DependencySpecImpl.parse(project.rootProject, name.trim(), gavNotation)
        d.platform = true
        action.execute(d)
        d.validate(project)
        dependencies[d.name] = d.asDependency()
    }

    /**
     * Defines and configures a platform dependency.</p>
     * Example:
     *
     * <pre>
     * platform('micronaut', 'io.micronaut:micronaut-bom:2.0.2') {
     *     modules = [
     *         'micronaut-core',
     *         'micronaut-inject',
     *         'micronaut-validation'
     *     ]
     * }
     * </pre>
     *
     * @param name the logical name of the dependency
     * @param gavNotation  must be in the {@code groupId:artifactId:version} notation
     */
    Dependency platform(String name, String gavNotation, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DependencySpec) Closure<Void> action) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Platform name cannot be blank.')
        }
        DependencySpecImpl d = DependencySpecImpl.parse(project.rootProject, name.trim(), gavNotation)
        d.platform = true
        ConfigureUtil.configure(action, d)
        d.validate(project)
        dependencies[d.name] = d.asDependency()
    }

    /**
     * Defines and configures a dependency by gavNotation.</p>
     * Example:
     *
     * <pre>
     * dependency('org.codehaus.groovy:groovy:3.0.6') {
     *     modules = [
     *         'groovy-test',
     *         'groovy-json',
     *         'groovy-xml'
     *     ]
     * }
     * </pre>
     *
     * @param gavNotation  must be in the {@code groupId:artifactId:version} notation
     */
    Dependency dependency(String gavNotation, Action<? super DependencySpec> action) {
        if (isBlank(gavNotation)) {
            throw new IllegalArgumentException('Dependency gavNotation cannot be blank.')
        }
        DependencySpecImpl d = DependencySpecImpl.parsePartial(project.rootProject, gavNotation.trim())
        action.execute(d)
        d.validate(project)
        dependencies[d.name] = d.asDependency()
    }

    /**
     * Defines and configures a dependency by gavNotation.</p>
     * Example:
     *
     * <pre>
     * dependency('org.codehaus.groovy:groovy:3.0.6') {
     *     modules = [
     *         'groovy-test',
     *         'groovy-json',
     *         'groovy-xml'
     *     ]
     * }
     * </pre>
     *
     * @param gavNotation  must be in the {@code groupId:artifactId:version} notation
     */
    Dependency dependency(String gavNotation, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DependencySpec) Closure<Void> action) {
        if (isBlank(gavNotation)) {
            throw new IllegalArgumentException('Dependency gavNotation cannot be blank.')
        }
        DependencySpecImpl d = DependencySpecImpl.parsePartial(project.rootProject, gavNotation.trim())
        ConfigureUtil.configure(action, d)
        d.validate(project)
        dependencies[d.name] = d.asDependency()
    }

    /**
     * Retrieves a declared dependency by name.
     *
     * @param name the logical name of the dependency to find
     * @return {@code org.kordamp.gradle.plugin.base.model.artifact.Dependency} instance when found.
     * @throws IllegalArgumentException if the named dependency is not found
     */
    Dependency getDependency(String name) {
        Dependency dependency = findDependencyByName(name)
        if (dependency) {
            return dependency
        }
        throw new IllegalArgumentException("Undeclared dependency ${name}.")
    }

    /**
     * Retrieves a declared dependency by name.
     *
     * @param name the logical name of the dependency to find
     * @return {@code org.kordamp.gradle.plugin.base.model.artifact.Dependency} instance when found, {@code null} otherwise.
     * @throws IllegalArgumentException if the given name is blank
     */
    Dependency findDependencyByName(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Dependency name cannot be blank.')
        }

        if (dependencies.containsKey(name)) {
            return dependencies.get(name)
        }
        if (project != project.rootProject) {
            return project.rootProject.extensions
                .findByType(ProjectConfigurationExtension)
                .dependencies
                .findDependencyByName(name)
        }
        null
    }

    /**
     * Retrieves a declared dependency by grouId and artifactId.
     *
     * @param groupId the groupId of the dependency to find
     * @param artifactId the artifactId of the dependency to find
     * @return {@code org.kordamp.gradle.plugin.base.model.artifact.Dependency} instance when found, {@code null} otherwise.
     * @throws IllegalArgumentException if the given groupId or artifactId are blank
     */
    Dependency findDependencyByGA(String groupId, String artifactId) {
        if (isBlank(groupId)) {
            throw new IllegalArgumentException('Dependency groupId cannot be blank.')
        }
        if (isBlank(artifactId)) {
            throw new IllegalArgumentException('Dependency artifactId cannot be blank.')
        }

        for (Dependency dependency : dependencies.values()) {
            if (dependency.groupId == groupId &&
                (dependency.artifactId == artifactId || dependency.modules.contains(artifactId))) {
                return dependency
            }
        }
        if (project != project.rootProject) {
            return project.rootProject.extensions
                .findByType(ProjectConfigurationExtension)
                .dependencies
                .findDependencyByGA(groupId, artifactId)
        }
        null
    }

    /**
     * Formats the named dependency in {@code groupId:artifactId:version} notation.
     * @param name the logical name of the dependency to find
     * @throws IllegalArgumentException if the named dependency is not found
     */
    String gav(String name) {
        getDependency(name).gav
    }

    /**
     * Formats the named module dependency in {@code groupId:artifactId:version} notation.
     * @param name the logical name of the dependency to find
     * @param moduleName the name of the module to find.
     * @throws IllegalArgumentException if the given args are blank or the named dependency is not found
     */
    String gav(String name, String moduleName) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Dependency name cannot be blank.')
        }
        if (isBlank(moduleName)) {
            throw new IllegalArgumentException('Dependency moduleName cannot be blank.')
        }

        if (dependencies.containsKey(name)) {
            if (dependencies.get(name).artifactId == moduleName) {
                return dependencies.get(name).gav
            }
            return dependencies.get(name).gav(moduleName)
        }

        if (project != project.rootProject) {
            return project.rootProject.extensions
                .findByType(ProjectConfigurationExtension)
                .dependencies
                .gav(name, moduleName)
        }

        throw new IllegalArgumentException("Undeclared depedency ${name}.")
    }

    /**
     * Formats the named module dependency in {@code groupId:artifactId} notation.
     * @param name the logical name of the dependency to find
     * @param moduleName the name of the module to find.
     * @throws IllegalArgumentException if the given args are blank or the named dependency is not found
     */
    String ga(String name, String moduleName) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Dependency name cannot be blank.')
        }
        if (isBlank(moduleName)) {
            throw new IllegalArgumentException('Dependency moduleName cannot be blank.')
        }

        if (dependencies.containsKey(name)) {
            if (dependencies.get(name).artifactId == moduleName) {
                return dependencies.get(name).ga
            }
            return dependencies.get(name).ga(moduleName)
        }

        if (project != project.rootProject) {
            return project.rootProject.extensions
                .findByType(ProjectConfigurationExtension)
                .dependencies
                .ga(name, moduleName)
        }

        throw new IllegalArgumentException("Undeclared depedency ${name}.")
    }
}
