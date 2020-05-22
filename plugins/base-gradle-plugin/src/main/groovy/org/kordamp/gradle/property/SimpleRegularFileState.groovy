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
package org.kordamp.gradle.property

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.kordamp.gradle.property.PropertyUtils.Order
import org.kordamp.gradle.property.PropertyUtils.Path

import static java.util.Objects.requireNonNull
import static org.kordamp.gradle.property.PropertyUtils.fileProvider

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
final class SimpleRegularFileState implements RegularFileState {
    final RegularFileProperty property
    final Provider<RegularFile> provider

    private final Project project

    @Override
    RegularFile getValue() {
        fileProvider(project.providers, property, provider, null).get()
    }

    SimpleRegularFileState(Project project, RegularFileProperty property, Provider<RegularFile> provider) {
        this.project = requireNonNull(project, "Argument 'project' must not be null.")
        this.property = requireNonNull(property, "Argument 'property' must not be null.")
        this.provider = requireNonNull(provider, "Argument 'provider' must not be null.")
    }

    SimpleRegularFileState(Project project, Object owner, String key) {
        this.project = requireNonNull(project, "Argument 'project' must not be null.")

        property = project.objects.fileProperty()

        provider = fileProvider(
            key,
            property,
            project,
            owner)
    }

    SimpleRegularFileState(Project project, Object owner, String key, Order order) {
        this.project = requireNonNull(project, "Argument 'project' must not be null.")

        property = project.objects.fileProperty()

        provider = fileProvider(
            key,
            property,
            order,
            project,
            owner)
    }

    SimpleRegularFileState(Project project, Object owner, String envKey, String propertyKey) {
        this.project = requireNonNull(project, "Argument 'project' must not be null.")

        property = project.objects.fileProperty()

        provider = fileProvider(
            envKey,
            propertyKey,
            property,
            project,
            owner)
    }

    SimpleRegularFileState(Project project, Object owner, String key, Order order, Path path, RegularFile defaultValue) {
        this.project = requireNonNull(project, "Argument 'project' must not be null.")

        property = project.objects.fileProperty()

        provider = fileProvider(
            key,
            property,
            order,
            path,
            project,
            owner,
            defaultValue)
    }

    SimpleRegularFileState(Project project, Object owner, String envKey, String propertyKey, Order order, Path path, RegularFile defaultValue) {
        this.project = requireNonNull(project, "Argument 'project' must not be null.")

        property = project.objects.fileProperty()

        provider = fileProvider(
            envKey,
            propertyKey,
            property,
            order,
            path,
            project,
            owner,
            defaultValue)
    }
}