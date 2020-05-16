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
class DependencySpecImpl implements DependencySpec {
    final String name
    String groupId
    String artifactId
    String version
    boolean platform
    Set<String> modules = []

    DependencySpecImpl(String name) {
        this.name = name
    }

    void parse(Project rootProject, String str) {
        if (isBlank(str)) {
            throw new IllegalArgumentException('Empty dependency notation.')
        }
        String[] parts = str.trim().split(':')
        switch (parts.length) {
            case 0:
                throw new IllegalArgumentException("Project '${str}' does not exist.")
            case 1:
                if (isNotBlank(parts[0])) {
                    if (rootProject.name == parts[0].trim()) {
                        groupId = rootProject.group.toString()
                        artifactId = parts[0]
                        version = rootProject.version.toString()
                    } else {
                        Project p = rootProject.findProject(parts[0].trim())
                        if (p) {
                            groupId = p.group.toString()
                            artifactId = parts[0]
                            version = p.version.toString()
                        } else {
                            throw new IllegalArgumentException("Project '${str}' does not exist.")
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Project '${str}' does not exist.")
                }
                break
            case 2:
                if (isBlank(parts[0]) && isNotBlank(parts[1])) {
                    if (rootProject.name == parts[1].trim()) {
                        groupId = rootProject.group.toString()
                        artifactId = parts[1]
                        version = rootProject.version.toString()
                    } else {
                        Project p = rootProject.findProject(parts[1].trim())
                        if (p) {
                            groupId = p.group.toString()
                            artifactId = parts[1]
                            version = p.version.toString()
                        } else {
                            throw new IllegalArgumentException("Project '${str}' does not exist.")
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Project '${str}' does not exist.")
                }
                break
            case 3:
                if (isBlank(parts[0]) || isBlank(parts[1]) || isBlank(parts[2])) {
                    throw new IllegalArgumentException("Invalid dependency '${str}'.")
                }
                groupId = parts[0]
                artifactId = parts[1]
                version = parts[2]
        }
    }

    void validate(Project project) {
        List<String> errors = []

        if (isBlank(groupId)) {
            errors.add("Dependency'${name}' is missing groupId.".toString())
        }
        if (isBlank(artifactId)) {
            errors.add("Dependency'${name}' is missing artifactId.".toString())
        }
        if (isBlank(version)) {
            errors.add("Dependency'${name}' is missing version.".toString())
        }

        if (errors) {
            for (String error : errors) {
                project.logger.error(error)
            }
            throw new GradleException("Project ${project.name} has not been properly configured.")
        }
    }

    void module(String module) {
        if (isNotBlank(module)) {
            modules << module.trim()
        }
    }

    Dependency asDependency() {
        new DependencyImpl(name, groupId.trim(), artifactId.trim(), version.trim(), platform, modules)
    }
}
