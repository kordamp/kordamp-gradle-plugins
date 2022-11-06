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
package org.kordamp.gradle.plugin.base.model.artifact.internal

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.model.artifact.Dependency
import org.kordamp.gradle.plugin.base.model.artifact.DependencySpec
import org.kordamp.gradle.plugin.base.model.artifact.Platform
import org.kordamp.gradle.plugin.base.model.artifact.PlatformSpec

import static org.kordamp.gradle.util.StringUtils.isBlank
import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.41.0
 */
@CompileStatic
class DependencyUtils {
    private static final String ERROR_EMPTY_DEPENDENCY_NOTATION = 'Empty dependency notation.'
    private static final String ERROR_EMPTY_PLATFORM_NOTATION = 'Empty platformm notation.'

    static Dependency dependency(String name, String groupId, String artifactId, String version) {
        dependency(name, groupId, artifactId, version, [] as Set<String>)
    }

    static Dependency dependency(String name, String groupId, String artifactId, String version, Set<String> moduleNames) {
        new DependencyImpl(name, groupId, artifactId, version, moduleNames)
    }

    static Platform platform(String name, String groupId, String artifactId, String version) {
        new PlatformImpl(name, groupId, artifactId, version)
    }

    static DependencySpec parsePartialDependency(Project rootProject, String notation) {
        if (isBlank(notation)) {
            throw new IllegalArgumentException(ERROR_EMPTY_DEPENDENCY_NOTATION)
        }

        DependencySpecImpl spec = null

        String[] parts = notation.trim().split(':')
        switch (parts.length) {
            case 0:
                throw new IllegalArgumentException(ERROR_EMPTY_DEPENDENCY_NOTATION)
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
                    throw new IllegalArgumentException(ERROR_EMPTY_DEPENDENCY_NOTATION)
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

    static DependencySpec parseDependency(Project rootProject, String notation) {
        if (isBlank(notation)) {
            throw new IllegalArgumentException(ERROR_EMPTY_DEPENDENCY_NOTATION)
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

    static DependencySpec parseDependency(Project rootProject, String name, String notation) {
        if (isBlank(notation)) {
            throw new IllegalArgumentException(ERROR_EMPTY_DEPENDENCY_NOTATION)
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

    static PlatformSpec parsePartialPlatform(Project rootProject, String notation) {
        if (isBlank(notation)) {
            throw new IllegalArgumentException(ERROR_EMPTY_PLATFORM_NOTATION)
        }

        PlatformSpecImpl spec = null

        String[] parts = notation.trim().split(':')
        switch (parts.length) {
            case 0:
                throw new IllegalArgumentException(ERROR_EMPTY_PLATFORM_NOTATION)
            case 1:
                if (isNotBlank(parts[0])) {
                    if (rootProject.name == parts[0].trim()) {
                        spec = new PlatformSpecImpl(parts[0])
                        spec.groupId = rootProject.group.toString()
                        spec.artifactId = parts[0]
                        spec.version = rootProject.version.toString()
                    } else {
                        Project p = rootProject.findProject(parts[0].trim())
                        if (p) {
                            spec = new PlatformSpecImpl(parts[0])
                            spec.groupId = p.group.toString()
                            spec.artifactId = parts[0]
                            spec.version = p.version.toString()
                        } else {
                            spec = new PlatformSpecImpl(notation.trim())
                        }
                    }
                } else {
                    throw new IllegalArgumentException(ERROR_EMPTY_PLATFORM_NOTATION)
                }
                break
            case 2:
                if (isBlank(parts[0]) && isNotBlank(parts[1])) {
                    if (rootProject.name == parts[1].trim()) {
                        spec = new PlatformSpecImpl(parts[1])
                        spec.groupId = rootProject.group.toString()
                        spec.artifactId = parts[1]
                        spec.version = rootProject.version.toString()
                    } else {
                        Project p = rootProject.findProject(parts[1].trim())
                        if (p) {
                            spec = new PlatformSpecImpl(parts[1])
                            spec.groupId = p.group.toString()
                            spec.artifactId = parts[1]
                            spec.version = p.version.toString()
                        } else {
                            spec = new PlatformSpecImpl(notation.trim())
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Invalid platform '${notation}'.")
                }
                break
            case 3:
                if (isBlank(parts[0]) || isBlank(parts[1]) || isBlank(parts[2])) {
                    throw new IllegalArgumentException("Invalid platform '${notation}'.")
                }
                spec = new PlatformSpecImpl(parts[1])
                spec.groupId = parts[0]
                spec.artifactId = parts[1]
                spec.version = parts[2]
        }

        spec
    }

    static PlatformSpec parsePlatform(Project rootProject, String notation) {
        if (isBlank(notation)) {
            throw new IllegalArgumentException(ERROR_EMPTY_PLATFORM_NOTATION)
        }

        PlatformSpecImpl spec = null

        String[] parts = notation.trim().split(':')
        switch (parts.length) {
            case 0:
                throw new IllegalArgumentException("Project '${notation}' does not exist.")
            case 1:
                if (isNotBlank(parts[0])) {
                    if (rootProject.name == parts[0].trim()) {
                        spec = new PlatformSpecImpl(parts[0])
                        spec.groupId = rootProject.group.toString()
                        spec.artifactId = parts[0]
                        spec.version = rootProject.version.toString()
                    } else {
                        Project p = rootProject.findProject(parts[0].trim())
                        if (p) {
                            spec = new PlatformSpecImpl(parts[0])
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
                        spec = new PlatformSpecImpl(parts[1])
                        spec.groupId = rootProject.group.toString()
                        spec.artifactId = parts[1]
                        spec.version = rootProject.version.toString()
                    } else {
                        Project p = rootProject.findProject(parts[1].trim())
                        if (p) {
                            spec = new PlatformSpecImpl(parts[1])
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
                    throw new IllegalArgumentException("Invalid platform '${notation}'.")
                }
                spec = new PlatformSpecImpl(parts[1])
                spec.groupId = parts[0]
                spec.artifactId = parts[1]
                spec.version = parts[2]
        }

        spec
    }

    static PlatformSpec parsePlatform(Project rootProject, String name, String notation) {
        if (isBlank(notation)) {
            throw new IllegalArgumentException(ERROR_EMPTY_PLATFORM_NOTATION)
        }

        PlatformSpecImpl spec = new PlatformSpecImpl(name)

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
                    throw new IllegalArgumentException("Invalid platform '${notation}'.")
                }
                spec.groupId = parts[0]
                spec.artifactId = parts[1]
                spec.version = parts[2]
        }

        spec
    }
}
