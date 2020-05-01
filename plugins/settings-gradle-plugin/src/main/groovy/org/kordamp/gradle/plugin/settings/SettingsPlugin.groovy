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
package org.kordamp.gradle.plugin.settings

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.model.ObjectFactory
import org.kordamp.gradle.plugin.settings.internal.ProjectsExtensionImpl

import javax.inject.Inject

/**
 * @author Andres Almiray
 * @since 0.15.0
 */
@CompileStatic
class SettingsPlugin implements Plugin<Settings> {
    private final ObjectFactory objects

    @Inject
    SettingsPlugin(ObjectFactory objects) {
        this.objects = objects
    }

    @Override
    void apply(Settings settings) {
        settings.extensions
            .create(ProjectsExtension, 'projects', ProjectsExtensionImpl, settings, objects)
    }
}
