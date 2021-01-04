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
package org.kordamp.gradle.plugin.base.model.artifact.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.PackageScope
import org.kordamp.gradle.plugin.base.model.artifact.Platform

/**
 * @author Andres Almiray
 * @since 0.41.0
 */
@CompileStatic
@PackageScope
@EqualsAndHashCode(includes = ['groupId', 'artifactId', 'version'])
class PlatformImpl implements Platform {
    final String name
    final String groupId
    final String artifactId
    final String version
    final Map<String, String> modules = new TreeMap<>()
    final Set<String> moduleNames = new TreeSet<>()

    PlatformImpl(String name, String groupId, String artifactId, String version) {
        this.name = name
        this.groupId = groupId
        this.artifactId = artifactId
        this.version = version
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = [
            groupId   : groupId,
            artifactId: artifactId,
            version   : version,
            modules   : getModules()
        ]
        new LinkedHashMap<String, Map<String, Object>>([
            (name): map
        ])
    }

    @PackageScope
    void setModules(Map<String, String> modules) {
        this.moduleNames.addAll(modules.keySet())
        this.modules.putAll(modules)
    }

    Set<String> getModules() {
        Collections.unmodifiableSet(this.modules.values() as Set)
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
            return modules.get(moduleName)
        }
        throw new IllegalArgumentException("Platform '${name}' does not define module '${moduleName}'.")
    }

    @Override
    String gav(String moduleName) {
        asGav(moduleName)
    }

    @Override
    String asGa(String moduleName) {
        if (moduleNames.contains(moduleName)) {
            String[] parts = modules.get(moduleName).split(':')
            return "${parts[0]}:${parts[1]}".toString()
        }
        throw new IllegalArgumentException("Platform '${name}' does not define module '${moduleName}'.")
    }

    @Override
    String ga(String moduleName) {
        asGa(moduleName)
    }
}
