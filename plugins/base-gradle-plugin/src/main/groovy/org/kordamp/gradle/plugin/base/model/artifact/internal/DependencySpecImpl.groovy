/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2024 Andres Almiray.
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
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.model.artifact.Dependency
import org.kordamp.gradle.plugin.base.model.artifact.DependencySpec

import static org.kordamp.gradle.util.StringUtils.isBlank
import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
@PackageScope
class DependencySpecImpl implements DependencySpec {
    final String name
    String groupId
    String artifactId
    String version

    Set<String> modules = [] as Set

    DependencySpecImpl(String name) {
        this.name = name
    }

    @Override
    void module(String module) {
        if (isNotBlank(module)) {
            modules << module.trim()
        }
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

    @Override
    Dependency asDependency() {
        new DependencyImpl(name, groupId.trim(), artifactId.trim(), version.trim(), modules)
    }
}
