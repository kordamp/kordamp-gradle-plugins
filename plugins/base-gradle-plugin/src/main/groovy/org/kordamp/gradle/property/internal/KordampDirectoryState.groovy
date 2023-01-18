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
package org.kordamp.gradle.property.internal

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider
import org.kordamp.gradle.property.DirectoryState
import org.kordamp.gradle.property.PropertyUtils.Order
import org.kordamp.gradle.property.PropertyUtils.Path

import static java.util.Objects.requireNonNull
import static org.kordamp.gradle.property.PropertyUtils.directoryProvider

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
final class KordampDirectoryState implements DirectoryState {
    final DirectoryProperty property
    final Provider<Directory> provider

    private final Project project

    @Override
    Directory getValue() {
        directoryProvider(project.providers, property, provider, (Directory) null).get()
    }

    KordampDirectoryState(Project project, String key, Provider<Directory> parent, Directory defaultValue) {
        this.project = requireNonNull(project, "Argument 'project' must not be null.")

        property = project.objects.directoryProperty().convention(Providers.notDefined())

        provider = directoryProvider(
            key,
            property,
            parent,
            Order.ENV_SYS_PROP,
            Path.PROJECT_OWNER,
            true,
            project,
            defaultValue)
    }

    KordampDirectoryState(Project project, String key, Provider<Directory> parent, Provider<Directory> defaultValue) {
        this.project = requireNonNull(project, "Argument 'project' must not be null.")

        property = project.objects.directoryProperty().convention(Providers.notDefined())

        provider = directoryProvider(
            key,
            property,
            parent,
            Order.ENV_SYS_PROP,
            Path.PROJECT_OWNER,
            true,
            project,
            defaultValue)
    }
}