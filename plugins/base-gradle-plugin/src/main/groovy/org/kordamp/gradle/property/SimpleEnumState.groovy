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
import org.gradle.api.Task
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.kordamp.gradle.property.PropertyUtils.Order
import org.kordamp.gradle.property.PropertyUtils.Path

import static java.util.Objects.requireNonNull
import static org.kordamp.gradle.property.PropertyUtils.enumProvider

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@CompileStatic
final class SimpleEnumState<E extends Enum<E>> implements EnumState<E> {
    private static final String ERROR_TASK_NULL = "Argument 'task' must not be null."
    private static final String ERROR_PROJECT_NULL = "Argument 'project' must not be null."

    final Class<E> enumType
    final Property<E> property
    final Provider<E> provider

    private final Project project

    @Override
    E getValue() {
        enumProvider(project.providers, property, provider, (E) null).get()
    }

    SimpleEnumState(Project project, Class<E> enumType, Property<E> property, Provider<E> provider) {
        this.project = requireNonNull(project, ERROR_PROJECT_NULL)
        this.enumType = requireNonNull(enumType, "Argument 'enumType' must not be null.")
        this.property = requireNonNull(property, "Argument 'property' must not be null.")
        this.provider = requireNonNull(provider, "Argument 'provider' must not be null.")
    }

    static <E extends Enum<E>> SimpleEnumState of(Task task, String key, Class<E> enumType) {
        of(task, key, enumType, (E) null)
    }

    static <E extends Enum<E>> SimpleEnumState of(Task task, String key, Class<E> enumType, E defaultValue) {
        requireNonNull(task, ERROR_TASK_NULL)
        of(task.project, task, key, enumType, defaultValue)
    }

    static <E extends Enum<E>> SimpleEnumState of(Task task, String key, Class<E> enumType, Order order) {
        requireNonNull(task, ERROR_TASK_NULL)
        of(task.project, task, key, enumType, order)
    }

    static <E extends Enum<E>> SimpleEnumState of(Task task, String key, Class<E> enumType, Order order, Path path, E defaultValue) {
        requireNonNull(task, ERROR_TASK_NULL)
        of(task.project, task, key, enumType, order, path, defaultValue)
    }

    static <E extends Enum<E>> SimpleEnumState of(Task task, String envKey, String propertyKey, Class<E> enumType, Order order, Path path, E defaultValue) {
        requireNonNull(task, ERROR_TASK_NULL)
        of(task.project, task, envKey, propertyKey, enumType, order, path, defaultValue)
    }

    static <E extends Enum<E>> SimpleEnumState of(Project project, Object owner, String key, Class<E> enumType) {
        requireNonNull(project, ERROR_PROJECT_NULL)

        Property<E> property = project.objects.property(enumType).convention(Providers.notDefined())

        Provider<E> provider = enumProvider(
            key,
            enumType,
            property,
            project,
            owner)

        new SimpleEnumState<>(project, enumType, property, provider)
    }

    static <E extends Enum<E>> SimpleEnumState of(Project project, Object owner, String key, Class<E> enumType, E defaultValue) {
        requireNonNull(project, ERROR_PROJECT_NULL)

        Property<E> property = project.objects.property(enumType).convention(Providers.notDefined())

        Provider<E> provider = enumProvider(
            key,
            enumType,
            property,
            project,
            owner,
            defaultValue)

        new SimpleEnumState<>(project, enumType, property, provider)
    }

    static <E extends Enum<E>> SimpleEnumState of(Project project, Object owner, String key, Class<E> enumType, Order order) {
        requireNonNull(project, ERROR_PROJECT_NULL)

        Property<E> property = project.objects.property(enumType).convention(Providers.notDefined())

        Provider<E> provider = enumProvider(
            key,
            enumType,
            property,
            order,
            project,
            owner)

        new SimpleEnumState<>(project, enumType, property, provider)
    }

    static <E extends Enum<E>> SimpleEnumState of(Project project, Object owner, String key, Class<E> enumType, Order order, Path path, E defaultValue) {
        requireNonNull(project, ERROR_PROJECT_NULL)

        Property<E> property = project.objects.property(enumType).convention(Providers.notDefined())

        Provider<E> provider = enumProvider(
            key,
            enumType,
            property,
            order,
            path,
            project,
            owner,
            defaultValue)

        new SimpleEnumState<>(project, enumType, property, provider)
    }

    static <E extends Enum<E>> SimpleEnumState of(Project project, Object owner, String envKey, String propertyKey, Class<E> enumType, Order order, Path path, E defaultValue) {
        requireNonNull(project, ERROR_PROJECT_NULL)

        Property<E> property = project.objects.property(enumType).convention(Providers.notDefined())

        Provider<E> provider = enumProvider(
            envKey,
            propertyKey,
            enumType,
            property,
            order,
            path,
            project,
            owner,
            defaultValue)

        new SimpleEnumState<>(project, enumType, property, provider)
    }
}