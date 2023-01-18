/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2023 Andres Almiray.
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
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.Providers
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
    private static final String ERROR_TASK_NULL = "Argument 'task' must not be null."
    private static final String ERROR_PROJECT_NULL = "Argument 'project' must not be null."

    final RegularFileProperty property
    final Provider<RegularFile> provider

    private final Project project

    @Override
    RegularFile getValue() {
        fileProvider(project.providers, property, provider, (RegularFile) null).get()
    }

    SimpleRegularFileState(Project project, RegularFileProperty property, Provider<RegularFile> provider) {
        this.project = requireNonNull(project, ERROR_PROJECT_NULL)
        this.property = requireNonNull(property, "Argument 'property' must not be null.")
        this.provider = requireNonNull(provider, "Argument 'provider' must not be null.")
    }

    static SimpleRegularFileState of(Task task, String key) {
        of(task, key, (RegularFile) null)
    }

    static SimpleRegularFileState of(Task task, String key, RegularFile defaultValue) {
        requireNonNull(task, ERROR_TASK_NULL)
        of(task.project, task, key, defaultValue)
    }

    static SimpleRegularFileState of(Task task, String key, Order order) {
        requireNonNull(task, ERROR_TASK_NULL)
        of(task.project, task, key, order)
    }

    static SimpleRegularFileState of(Task task, String key, Order order, Path path, RegularFile defaultValue) {
        requireNonNull(task, ERROR_TASK_NULL)
        of(task.project, task, key, order, path, defaultValue)
    }

    static SimpleRegularFileState of(Task task, String envKey, String propertyKey, Order order, Path path, RegularFile defaultValue) {
        requireNonNull(task, ERROR_TASK_NULL)
        of(task.project, task, envKey, propertyKey, order, path, defaultValue)
    }

    static SimpleRegularFileState of(Project project, Object owner, String key) {
        requireNonNull(project, ERROR_PROJECT_NULL)

        RegularFileProperty property = project.objects.fileProperty().convention(Providers.<RegularFile>notDefined())

        Provider<RegularFile> provider = fileProvider(
            key,
            property,
            project,
            owner)

        new SimpleRegularFileState(project, property, provider)
    }

    static SimpleRegularFileState of(Project project, Object owner, String key, RegularFile defaultValue) {
        requireNonNull(project, ERROR_PROJECT_NULL)

        RegularFileProperty property = project.objects.fileProperty().convention(Providers.<RegularFile>notDefined())

        Provider<RegularFile> provider = fileProvider(
            key,
            property,
            project,
            owner,
            defaultValue)

        new SimpleRegularFileState(project, property, provider)
    }

    static SimpleRegularFileState of(Project project, Object owner, String key, Order order) {
        requireNonNull(project, ERROR_PROJECT_NULL)

        RegularFileProperty property = project.objects.fileProperty().convention(Providers.<RegularFile>notDefined())

        Provider<RegularFile> provider = fileProvider(
            key,
            property,
            order,
            project,
            owner)

        new SimpleRegularFileState(project, property, provider)
    }

    static SimpleRegularFileState of(Project project, Object owner, String key, Order order, Path path, RegularFile defaultValue) {
        requireNonNull(project, ERROR_PROJECT_NULL)

        RegularFileProperty property = project.objects.fileProperty().convention(Providers.<RegularFile>notDefined())

        Provider<RegularFile> provider = fileProvider(
            key,
            property,
            order,
            path,
            project,
            owner,
            defaultValue)

        new SimpleRegularFileState(project, property, provider)
    }

    static SimpleRegularFileState of(Project project, Object owner, String envKey, String propertyKey, Order order, Path path, RegularFile defaultValue) {
        requireNonNull(project, ERROR_PROJECT_NULL)

        RegularFileProperty property = project.objects.fileProperty().convention(Providers.<RegularFile>notDefined())

        Provider<RegularFile> provider = fileProvider(
            envKey,
            propertyKey,
            property,
            order,
            path,
            project,
            owner,
            defaultValue)

        new SimpleRegularFileState(project, property, provider)
    }
}