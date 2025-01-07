/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2025 Andres Almiray.
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
import static org.kordamp.gradle.property.PropertyUtils.longProvider

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
final class SimpleLongState implements LongState {
    private static final String ERROR_TASK_NULL = "Argument 'task' must not be null."
    private static final String ERROR_PROJECT_NULL = "Argument 'project' must not be null."

    final Property<Long> property
    final Provider<Long> provider

    private final Project project

    @Override
    long getValue() {
        longProvider(project.providers, property, provider, 0L).get()
    }

    SimpleLongState(Project project, Property<Long> property, Provider<Long> provider) {
        this.project = requireNonNull(project, ERROR_PROJECT_NULL)
        this.property = requireNonNull(property, "Argument 'property' must not be null.")
        this.provider = requireNonNull(provider, "Argument 'provider' must not be null.")
    }

    static SimpleLongState of(Task task, String key) {
        of(task, key, 0L)
    }

    static SimpleLongState of(Task task, String key, long defaultValue) {
        requireNonNull(task, ERROR_TASK_NULL)
        of(task.project, task, key, defaultValue)
    }

    static SimpleLongState of(Task task, String key, Order order) {
        requireNonNull(task, ERROR_TASK_NULL)
        of(task.project, task, key, order)
    }

    static SimpleLongState of(Task task, String key, Order order, Path path, long defaultValue) {
        requireNonNull(task, ERROR_TASK_NULL)
        of(task.project, task, key, order, path, defaultValue)
    }

    static SimpleLongState of(Task task, String envKey, String propertyKey, Order order, Path path, long defaultValue) {
        requireNonNull(task, ERROR_TASK_NULL)
        of(task.project, task, envKey, propertyKey, order, path, defaultValue)
    }

    static SimpleLongState of(Project project, Object owner, String key) {
        requireNonNull(project, ERROR_PROJECT_NULL)

        Property<Long> property = project.objects.property(Long).convention(Providers.<Long>notDefined())

        Provider<Long> provider = longProvider(
            key,
            property,
            project,
            owner)

        new SimpleLongState(project, property, provider)
    }

    static SimpleLongState of(Project project, Object owner, String key, long defaultValue) {
        requireNonNull(project, ERROR_PROJECT_NULL)

        Property<Long> property = project.objects.property(Long).convention(Providers.<Long>notDefined())

        Provider<Long> provider = longProvider(
            key,
            property,
            project,
            owner,
            defaultValue)

        new SimpleLongState(project, property, provider)
    }

    static SimpleLongState of(Project project, Object owner, String key, Order order) {
        requireNonNull(project, ERROR_PROJECT_NULL)

        Property<Long> property = project.objects.property(Long).convention(Providers.<Long>notDefined())

        Provider<Long> provider = longProvider(
            key,
            property,
            order,
            project,
            owner)

        new SimpleLongState(project, property, provider)
    }

    static SimpleLongState of(Project project, Object owner, String key, Order order, Path path, long defaultValue) {
        requireNonNull(project, ERROR_PROJECT_NULL)

        Property<Long> property = project.objects.property(Long).convention(Providers.<Long>notDefined())

        Provider<Long> provider = longProvider(
            key,
            property,
            order,
            path,
            project,
            owner,
            defaultValue)

        new SimpleLongState(project, property, provider)
    }

    static SimpleLongState of(Project project, Object owner, String envKey, String propertyKey, Order order, Path path, long defaultValue) {
        requireNonNull(project, ERROR_PROJECT_NULL)

        Property<Long> property = project.objects.property(Long).convention(Providers.<Long>notDefined())

        Provider<Long> provider = longProvider(
            envKey,
            propertyKey,
            property,
            order,
            path,
            project,
            owner,
            defaultValue)

        new SimpleLongState(project, property, provider)
    }
}