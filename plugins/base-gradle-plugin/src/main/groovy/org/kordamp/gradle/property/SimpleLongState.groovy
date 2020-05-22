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
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.kordamp.gradle.property.PropertyUtils.Order
import org.kordamp.gradle.property.PropertyUtils.Path

import static java.util.Objects.requireNonNull
import static org.kordamp.gradle.property.PropertyUtils.longProvider

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
final class SimpleLongState implements LongState {
    final Property<Long> property
    final Provider<Long> provider

    private final Project project

    @Override
    long getValue() {
        longProvider(project.providers, property, provider, 0L).get()
    }

    SimpleLongState(Project project, Property<Long> property, Provider<Long> provider) {
        this.project = requireNonNull(project, "Argument 'project' must not be null.")
        this.property = requireNonNull(property, "Argument 'property' must not be null.")
        this.provider = requireNonNull(provider, "Argument 'provider' must not be null.")
    }

    SimpleLongState(Project project, Object owner, String key) {
        this.project = requireNonNull(project, "Argument 'project' must not be null.")

        property = project.objects.property(Long)

        provider = longProvider(
            key,
            property,
            project,
            owner)
    }

    SimpleLongState(Project project, Object owner, String key, Order order) {
        this.project = requireNonNull(project, "Argument 'project' must not be null.")

        property = project.objects.property(Long)

        provider = longProvider(
            key,
            property,
            order,
            project,
            owner)
    }

    SimpleLongState(Project project, Object owner, String envKey, String propertyKey) {
        this.project = requireNonNull(project, "Argument 'project' must not be null.")

        property = project.objects.property(Long)

        provider = longProvider(
            envKey,
            propertyKey,
            property,
            project,
            owner)
    }

    SimpleLongState(Project project, Object owner, String key, Order order, Path path, long defaultValue) {
        this.project = requireNonNull(project, "Argument 'project' must not be null.")

        property = project.objects.property(Long)

        provider = longProvider(
            key,
            property,
            order,
            path,
            project,
            owner,
            defaultValue)
    }

    SimpleLongState(Project project, Object owner, String envKey, String propertyKey, Order order, Path path, long defaultValue) {
        this.project = requireNonNull(project, "Argument 'project' must not be null.")

        property = project.objects.property(Long)

        provider = longProvider(
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