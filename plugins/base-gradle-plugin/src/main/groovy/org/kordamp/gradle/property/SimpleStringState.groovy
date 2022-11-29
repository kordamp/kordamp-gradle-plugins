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
import static org.kordamp.gradle.property.PropertyUtils.stringProvider

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
final class SimpleStringState implements StringState {
    private static final String ERROR_TASK_NULL = "Argument 'task' must not be null."
    private static final String ERROR_PROJECT_NULL = "Argument 'project' must not be null."

    final Property<String> property
    final Provider<String> provider

    private final Project project

    @Override
    String getValue() {
        stringProvider(project.providers, property, provider, '').get()
    }

    SimpleStringState(Project project, Property<String> property, Provider<String> provider) {
        this.project = requireNonNull(project, ERROR_PROJECT_NULL)
        this.property = requireNonNull(property, "Argument 'property' must not be null.")
        this.provider = requireNonNull(provider, "Argument 'provider' must not be null.")
    }

    static SimpleStringState of(Task task, String key) {
        of(task, key, '')
    }

    static SimpleStringState of(Task task, String key, String defaultValue) {
        requireNonNull(task, ERROR_TASK_NULL)
        of(task.project, task, key, defaultValue)
    }

    static SimpleStringState of(Task task, String key, Order order) {
        requireNonNull(task, ERROR_TASK_NULL)
        of(task.project, task, key, order)
    }

    static SimpleStringState of(Task task, String key, Order order, Path path, String defaultValue) {
        requireNonNull(task, ERROR_TASK_NULL)
        of(task.project, task, key, order, path, defaultValue)
    }

    static SimpleStringState of(Task task, String envKey, String propertyKey, Order order, Path path, String defaultValue) {
        requireNonNull(task, ERROR_TASK_NULL)
        of(task.project, task, envKey, propertyKey, order, path, defaultValue)
    }

    static SimpleStringState of(Project project, Object owner, String key) {
        requireNonNull(project, ERROR_PROJECT_NULL)

        Property<String> property = project.objects.property(String).convention(Providers.notDefined())

        Provider<String> provider = stringProvider(
            key,
            property,
            project,
            owner)

        new SimpleStringState(project, property, provider)
    }

    static SimpleStringState of(Project project, Object owner, String key, String defaultValue) {
        requireNonNull(project, ERROR_PROJECT_NULL)

        Property<String> property = project.objects.property(String).convention(Providers.notDefined())

        Provider<String> provider = stringProvider(
            key,
            property,
            project,
            owner,
            defaultValue)

        new SimpleStringState(project, property, provider)
    }

    static SimpleStringState of(Project project, Object owner, String key, Order order) {
        requireNonNull(project, ERROR_PROJECT_NULL)

        Property<String> property = project.objects.property(String).convention(Providers.notDefined())

        Provider<String> provider = stringProvider(
            key,
            property,
            order,
            project,
            owner)

        new SimpleStringState(project, property, provider)
    }

    static SimpleStringState of(Project project, Object owner, String key, Order order, Path path, String defaultValue) {
        requireNonNull(project, ERROR_PROJECT_NULL)

        Property<String> property = project.objects.property(String).convention(Providers.notDefined())

        Provider<String> provider = stringProvider(
            key,
            property,
            order,
            path,
            project,
            owner,
            defaultValue)

        new SimpleStringState(project, property, provider)
    }

    static SimpleStringState of(Project project, Object owner, String envKey, String propertyKey, Order order, Path path, String defaultValue) {
        requireNonNull(project, ERROR_PROJECT_NULL)

        Property<String> property = project.objects.property(String).convention(Providers.notDefined())

        Provider<String> provider = stringProvider(
            envKey,
            propertyKey,
            property,
            order,
            path,
            project,
            owner,
            defaultValue)

        new SimpleStringState(project, property, provider)
    }
}