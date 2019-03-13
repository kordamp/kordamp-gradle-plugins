/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kordamp.gradle.plugin.base.model

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import org.gradle.api.Project

import static org.kordamp.gradle.StringUtils.isBlank
import static org.kordamp.gradle.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.13.0
 */
@CompileStatic
@Canonical
@TupleConstructor
class Dependency {
    final String groupId
    final String artifactId
    final String version

    static Dependency parseDependency(Project project, String str) {
        parseDependency(project, str, false)
    }

    static Dependency parseDependency(Project project, String str, boolean expandCoords) {
        String[] parts = str.trim().split(':')
        switch (parts.length) {
            case 0:
                throw new IllegalStateException("Project '${str}' does not exist")
            case 1:
                if (isNotBlank(parts[0]) &&
                    (project.rootProject.name == parts[0] || project.rootProject.subprojects.find { it.name == parts[0] })) {
                    return new Dependency(
                        expandCoords ? project.group.toString() : '${project.groupId}',
                        parts[0],
                        expandCoords ? project.version.toString() : '${project.version}')
                }
                throw new IllegalStateException("Project '${str}' does not exist")
            case 2:
                if (isBlank(parts[0]) &&
                    isNotBlank(parts[1]) &&
                    (project.rootProject.name == parts[1] || project.rootProject.subprojects.find { it.name == parts[1] })) {
                    return new Dependency(
                        expandCoords ? project.group.toString() : '${project.groupId}',
                        parts[1],
                        expandCoords ? project.version.toString() : '${project.version}')
                }
                throw new IllegalStateException("Project '${str}' does not exist")
            case 3:
                if (isBlank(parts[0]) || isBlank(parts[1]) || isBlank(parts[2])) {
                    throw new IllegalStateException("Invalid BOM dependency '${str}'")
                }
                return new Dependency(parts[0], parts[1], parts[2])
        }
    }
}