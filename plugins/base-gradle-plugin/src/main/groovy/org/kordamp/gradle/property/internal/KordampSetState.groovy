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
package org.kordamp.gradle.property.internal

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.kordamp.gradle.property.PropertyUtils.Order
import org.kordamp.gradle.property.PropertyUtils.Path
import org.kordamp.gradle.property.SetState

import static java.util.Objects.requireNonNull
import static org.kordamp.gradle.property.PropertyUtils.setProvider

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
final class KordampSetState implements SetState {
    final SetProperty<String> property
    final Provider<Set<String>> provider

    private final Project project

    @Override
    Set<String> getValue() {
        setProvider(project.providers, property, provider, Collections.<String> emptySet()).get()
    }

    KordampSetState(Project project, String key, Provider<Set> parent, Set<String> defaultValue) {
        this.project = requireNonNull(project, "Argument 'project' must not be null.")

        property = project.objects.setProperty(String).convention(Providers.notDefined())

        provider = setProvider(
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