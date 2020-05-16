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
import org.kordamp.gradle.CollectionUtils
import org.kordamp.gradle.ConfigureUtil
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.artifact.Dependency
import org.kordamp.gradle.plugin.base.model.artifact.DependencySpec
import org.kordamp.gradle.plugin.base.model.artifact.internal.DependencySpecImpl

import static org.kordamp.gradle.StringUtils.isBlank

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

    Dependencies copyOf() {
        Dependencies copy = new Dependencies(config, project)
        copyInto(copy)
        copy
    }

    void copyInto(Dependencies copy) {
        copy.@dependencies.putAll(dependencies)
    }

    static void merge(Dependencies o1, Dependencies o2) {
        CollectionUtils.merge(o1.@dependencies, o2.@dependencies)
    }

    Dependency dependency(String name, String notation) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Dependency name cannot be blank.')
        }
        DependencySpecImpl d = new DependencySpecImpl(name.trim())
        d.parse(project.rootProject, notation)
        d.validate(project)
        dependencies[d.name] = d.asDependency()
    }

    Dependency dependency(String name, Action<? super DependencySpec> action) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Dependency name cannot be blank.')
        }
        DependencySpecImpl d = new DependencySpecImpl(name.trim())
        action.execute(d)
        d.validate(project)
        dependencies[d.name] = d.asDependency()
    }

    Dependency dependency(String name, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DependencySpec) Closure<Void> action) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Dependency name cannot be blank.')
        }
        DependencySpecImpl d = new DependencySpecImpl(name.trim())
        ConfigureUtil.configure(action, d)
        d.validate(project)
        dependencies[d.name] = d.asDependency()
    }

    Dependency dependency(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Dependency name cannot be blank.')
        }

        if (dependencies.containsKey(name)) {
            return dependencies.get(name)
        }
        throw new IllegalArgumentException("Undeclared dependency ${name}.")
    }

    Dependency findDependencyByName(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Dependency name cannot be blank.')
        }

        if (dependencies.containsKey(name)) {
            return dependencies.get(name)
        }
        null
    }

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
        null
    }

    String gav(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Dependency name cannot be blank.')
        }

        if (dependencies.containsKey(name)) {
            return dependencies.get(name).gav
        }
        throw new IllegalArgumentException("Undeclared dependency ${name}.")
    }

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
        throw new IllegalArgumentException("Undeclared depedency ${name}.")
    }

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
        throw new IllegalArgumentException("Undeclared depedency ${name}.")
    }
}
