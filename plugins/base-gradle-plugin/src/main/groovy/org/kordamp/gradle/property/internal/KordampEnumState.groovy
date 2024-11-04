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
package org.kordamp.gradle.property.internal

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.kordamp.gradle.property.EnumState
import org.kordamp.gradle.property.PropertyUtils.Order
import org.kordamp.gradle.property.PropertyUtils.Path

import static java.util.Objects.requireNonNull
import static org.kordamp.gradle.property.PropertyUtils.enumProvider

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@CompileStatic
final class KordampEnumState<E extends Enum<E>> implements EnumState<E> {
    final Property<E> property
    final Provider<E> provider

    private final Project project

    @Override
    E getValue() {
        enumProvider(project.providers, property, provider, (E) null).get()
    }

    KordampEnumState(Project project, String key, Class<E> enumType, Provider<E> parent, E defaultValue) {
        this.project = requireNonNull(project, "Argument 'project' must not be null.")

        property = project.objects.property(enumType).convention(Providers.notDefined())

        provider = enumProvider(
            key,
            enumType,
            property,
            parent,
            Order.ENV_SYS_PROP,
            Path.PROJECT_OWNER,
            true,
            project,
            defaultValue)
    }

    KordampEnumState(Project project, String key, Class<E> enumType, Provider<E> parent, Provider<E> defaultValue) {
        this.project = requireNonNull(project, "Argument 'project' must not be null.")

        property = project.objects.property(enumType).convention(Providers.notDefined())

        provider = enumProvider(
            key,
            enumType,
            property,
            parent,
            Order.ENV_SYS_PROP,
            Path.PROJECT_OWNER,
            true,
            project,
            defaultValue)
    }
}