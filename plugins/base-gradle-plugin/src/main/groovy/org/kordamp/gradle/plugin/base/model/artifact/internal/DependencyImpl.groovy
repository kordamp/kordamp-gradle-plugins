/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2023 Andres Almiray.
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
package org.kordamp.gradle.plugin.base.model.artifact.internal

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.kordamp.gradle.plugin.base.model.artifact.Dependency

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
@PackageScope
class DependencyImpl implements Dependency {
    final String name
    final String groupId
    final String artifactId
    final String version
    final Set<String> modules = new TreeSet<>()
    final Set<String> moduleNames = new TreeSet<>()

    DependencyImpl(String name, String groupId, String artifactId, String version, Set<String> moduleNames) {
        this.name = name
        this.groupId = groupId
        this.artifactId = artifactId
        this.version = version
        if (moduleNames) {
            this.moduleNames.addAll(moduleNames)
            this.modules.addAll(moduleNames.collect { "${groupId}:${it}:${version}".toString() })
        }
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = [
            groupId   : groupId,
            artifactId: artifactId,
            version   : version,
            modules   : modules
        ]
        new LinkedHashMap<String, Map<String, Object>>([
            (name): map
        ])
    }

    @Override
    String toString() {
        asGav()
    }

    @Override
    String asGav() {
        "${groupId}:${artifactId}:${version}".toString()
    }

    @Override
    String getGav() {
        asGav()
    }

    @Override
    String asGa() {
        "${groupId}:${artifactId}".toString()
    }

    @Override
    String getGa() {
        asGa()
    }

    @Override
    String asGav(String moduleName) {
        if (moduleNames.contains(moduleName)) {
            return "${groupId}:${moduleName}:${version}".toString()
        }
        throw new IllegalArgumentException("Dependency '${name}' does not define module '${moduleName}'.")
    }

    @Override
    String gav(String moduleName) {
        asGav(moduleName)
    }

    @Override
    String asGa(String moduleName) {
        if (moduleNames.contains(moduleName)) {
            return "${groupId}:${moduleName}".toString()
        }
        throw new IllegalArgumentException("Dependency '${name}' does not define module '${moduleName}'.")
    }

    @Override
    String ga(String moduleName) {
        asGa(moduleName)
    }
}
