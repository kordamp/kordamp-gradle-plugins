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
import static org.kordamp.gradle.property.PropertyUtils.booleanProvider

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
final class SimpleBooleanState implements BooleanState {
    final Property<Boolean> property
    final Provider<Boolean> provider

    private final Project project

    @Override
    boolean getValue() {
        booleanProvider(project.providers, property, provider, false).get()
    }

    SimpleBooleanState(Project project, Property<Boolean> property, Provider<Boolean> provider) {
        this.project = requireNonNull(project, "Argument 'project' must not be null.")
        this.property = requireNonNull(property, "Argument 'property' must not be null.")
        this.provider = requireNonNull(provider, "Argument 'provider' must not be null.")
    }

    SimpleBooleanState(Project project, Object owner, String key) {
        this.project = requireNonNull(project, "Argument 'project' must not be null.")

        property = project.objects.property(Boolean)

        provider = booleanProvider(
            key,
            property,
            project,
            owner)
    }

    SimpleBooleanState(Project project, Object owner, String key, Order order) {
        this.project = requireNonNull(project, "Argument 'project' must not be null.")

        property = project.objects.property(Boolean)

        provider = booleanProvider(
            key,
            property,
            order,
            project,
            owner)
    }

    SimpleBooleanState(Project project, Object owner, String envKey, String propertyKey) {
        this.project = requireNonNull(project, "Argument 'project' must not be null.")

        property = project.objects.property(Boolean)

        provider = booleanProvider(
            envKey,
            propertyKey,
            property,
            project,
            owner)
    }

    SimpleBooleanState(Project project, Object owner, String key, Order order, Path path, boolean defaultValue) {
        this.project = requireNonNull(project, "Argument 'project' must not be null.")

        property = project.objects.property(Boolean)

        provider = booleanProvider(
            key,
            property,
            order,
            path,
            project,
            owner,
            defaultValue)
    }

    SimpleBooleanState(Project project, Object owner, String envKey, String propertyKey, Order order, Path path, boolean defaultValue) {
        this.project = requireNonNull(project, "Argument 'project' must not be null.")

        property = project.objects.property(Boolean)

        provider = booleanProvider(
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