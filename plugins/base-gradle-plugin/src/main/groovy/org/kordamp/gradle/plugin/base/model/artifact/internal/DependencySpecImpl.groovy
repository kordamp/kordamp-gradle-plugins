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
package org.kordamp.gradle.plugin.base.model.artifact.internal

import groovy.transform.CompileStatic
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.model.artifact.Dependency
import org.kordamp.gradle.plugin.base.model.artifact.DependencySpec

import static org.kordamp.gradle.StringUtils.isBlank
import static org.kordamp.gradle.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
class DependencySpecImpl extends HasModulesSpecImpl implements DependencySpec {
    final String name
    String groupId
    String artifactId
    String version
    boolean platform

    DependencySpecImpl(String name) {
        this.name = name
    }

    static DependencySpecImpl parsePartial(Project rootProject, String notation) {
        if (isBlank(notation)) {
            throw new IllegalArgumentException('Empty dependency notation.')
        }

        DependencySpecImpl spec = null

        String[] parts = notation.trim().split(':')
        switch (parts.length) {
            case 0:
                throw new IllegalArgumentException('Empty dependency notation.')
            case 1:
                if (isNotBlank(parts[0])) {
                    if (rootProject.name == parts[0].trim()) {
                        spec = new DependencySpecImpl(parts[0])
                        spec.groupId = rootProject.group.toString()
                        spec.artifactId = parts[0]
                        spec.version = rootProject.version.toString()
                    } else {
                        Project p = rootProject.findProject(parts[0].trim())
                        if (p) {
                            spec = new DependencySpecImpl(parts[0])
                            spec.groupId = p.group.toString()
                            spec.artifactId = parts[0]
                            spec.version = p.version.toString()
                        } else {
                            spec = new DependencySpecImpl(notation.trim())
                        }
                    }
                } else {
                    throw new IllegalArgumentException('Empty dependency notation.')
                }
                break
            case 2:
                if (isBlank(parts[0]) && isNotBlank(parts[1])) {
                    if (rootProject.name == parts[1].trim()) {
                        spec = new DependencySpecImpl(parts[1])
                        spec.groupId = rootProject.group.toString()
                        spec.artifactId = parts[1]
                        spec.version = rootProject.version.toString()
                    } else {
                        Project p = rootProject.findProject(parts[1].trim())
                        if (p) {
                            spec = new DependencySpecImpl(parts[1])
                            spec.groupId = p.group.toString()
                            spec.artifactId = parts[1]
                            spec.version = p.version.toString()
                        } else {
                            spec = new DependencySpecImpl(notation.trim())
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Invalid dependency '${notation}'.")
                }
                break
            case 3:
                if (isBlank(parts[0]) || isBlank(parts[1]) || isBlank(parts[2])) {
                    throw new IllegalArgumentException("Invalid dependency '${notation}'.")
                }
                spec = new DependencySpecImpl(parts[1])
                spec.groupId = parts[0]
                spec.artifactId = parts[1]
                spec.version = parts[2]
        }

        spec
    }

    static DependencySpecImpl parse(Project rootProject, String notation) {
        if (isBlank(notation)) {
            throw new IllegalArgumentException('Empty dependency notation.')
        }

        DependencySpecImpl spec = null

        String[] parts = notation.trim().split(':')
        switch (parts.length) {
            case 0:
                throw new IllegalArgumentException("Project '${notation}' does not exist.")
            case 1:
                if (isNotBlank(parts[0])) {
                    if (rootProject.name == parts[0].trim()) {
                        spec = new DependencySpecImpl(parts[0])
                        spec.groupId = rootProject.group.toString()
                        spec.artifactId = parts[0]
                        spec.version = rootProject.version.toString()
                    } else {
                        Project p = rootProject.findProject(parts[0].trim())
                        if (p) {
                            spec = new DependencySpecImpl(parts[0])
                            spec.groupId = p.group.toString()
                            spec.artifactId = parts[0]
                            spec.version = p.version.toString()
                        } else {
                            throw new IllegalArgumentException("Project '${notation}' does not exist.")
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Project '${notation}' does not exist.")
                }
                break
            case 2:
                if (isBlank(parts[0]) && isNotBlank(parts[1])) {
                    if (rootProject.name == parts[1].trim()) {
                        spec = new DependencySpecImpl(parts[1])
                        spec.groupId = rootProject.group.toString()
                        spec.artifactId = parts[1]
                        spec.version = rootProject.version.toString()
                    } else {
                        Project p = rootProject.findProject(parts[1].trim())
                        if (p) {
                            spec = new DependencySpecImpl(parts[1])
                            spec.groupId = p.group.toString()
                            spec.artifactId = parts[1]
                            spec.version = p.version.toString()
                        } else {
                            throw new IllegalArgumentException("Project '${notation}' does not exist.")
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Project '${notation}' does not exist.")
                }
                break
            case 3:
                if (isBlank(parts[0]) || isBlank(parts[1]) || isBlank(parts[2])) {
                    throw new IllegalArgumentException("Invalid dependency '${notation}'.")
                }
                spec = new DependencySpecImpl(parts[1])
                spec.groupId = parts[0]
                spec.artifactId = parts[1]
                spec.version = parts[2]
        }

        spec
    }

    static DependencySpecImpl parse(Project rootProject, String name, String notation) {
        if (isBlank(notation)) {
            throw new IllegalArgumentException('Empty dependency notation.')
        }

        DependencySpecImpl spec = new DependencySpecImpl(name)

        String[] parts = notation.trim().split(':')
        switch (parts.length) {
            case 0:
                throw new IllegalArgumentException("Project '${notation}' does not exist.")
            case 1:
                if (isNotBlank(parts[0])) {
                    if (rootProject.name == parts[0].trim()) {
                        spec.groupId = rootProject.group.toString()
                        spec.artifactId = parts[0]
                        spec.version = rootProject.version.toString()
                    } else {
                        Project p = rootProject.findProject(parts[0].trim())
                        if (p) {
                            spec.groupId = p.group.toString()
                            spec.artifactId = parts[0]
                            spec.version = p.version.toString()
                        } else {
                            throw new IllegalArgumentException("Project '${notation}' does not exist.")
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Project '${notation}' does not exist.")
                }
                break
            case 2:
                if (isBlank(parts[0]) && isNotBlank(parts[1])) {
                    if (rootProject.name == parts[1].trim()) {
                        spec.groupId = rootProject.group.toString()
                        spec.artifactId = parts[1]
                        spec.version = rootProject.version.toString()
                    } else {
                        Project p = rootProject.findProject(parts[1].trim())
                        if (p) {
                            spec.groupId = p.group.toString()
                            spec.artifactId = parts[1]
                            spec.version = p.version.toString()
                        } else {
                            throw new IllegalArgumentException("Project '${notation}' does not exist.")
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Project '${notation}' does not exist.")
                }
                break
            case 3:
                if (isBlank(parts[0]) || isBlank(parts[1]) || isBlank(parts[2])) {
                    throw new IllegalArgumentException("Invalid dependency '${notation}'.")
                }
                spec.groupId = parts[0]
                spec.artifactId = parts[1]
                spec.version = parts[2]
        }

        spec
    }

    void validate(Project project) {
        List<String> errors = []

        if (isBlank(groupId)) {
            errors.add("Dependency '${name}' is missing groupId.".toString())
        }
        if (isBlank(artifactId)) {
            errors.add("Dependency '${name}' is missing artifactId.".toString())
        }
        if (isBlank(version)) {
            errors.add("Dependency '${name}' is missing version.".toString())
        }

        if (errors) {
            for (String error : errors) {
                project.logger.error(error)
            }
            throw new GradleException("Project ${project.name} has not been properly configured.")
        }
    }

    Dependency asDependency() {
        new DependencyImpl(name, groupId.trim(), artifactId.trim(), version.trim(), platform, modules)
    }
}
