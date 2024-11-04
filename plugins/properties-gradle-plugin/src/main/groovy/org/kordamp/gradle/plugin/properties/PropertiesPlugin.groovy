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
package org.kordamp.gradle.plugin.properties

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.kordamp.gradle.plugin.properties.internal.PropertiesExtensionImpl

import javax.inject.Inject

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
class PropertiesPlugin implements Plugin<Settings> {
    private final ObjectFactory objects

    @Inject
    PropertiesPlugin(ObjectFactory objects) {
        this.objects = objects
    }

    @Override
    void apply(Settings settings) {
        settings.extensions
            .create(PropertiesExtension, 'properties', PropertiesExtensionImpl, settings, objects)

        ExtraPropertiesExtension ext = settings.extensions.getByType(ExtraPropertiesExtension)

        boolean yamlEnabled = isEnabled('yaml.enabled')
        boolean tomlEnabled = isEnabled('toml.enabled')

        [
            settings.gradle.gradleHomeDir,
            settings.settingsDir,
            settings.gradle.gradleUserHomeDir
        ].each { dir ->
            if (yamlEnabled) {
                File file = new File(dir, 'gradle.yml')
                YamlPropertiesReader.readProperties(file, ext, true)
            }
            if (tomlEnabled) {
                File file = new File(dir, 'gradle.toml')
                TomlPropertiesReader.readProperties(file, ext, true)
            }
        }
    }

    private static boolean isEnabled(String key) {
        !System.hasProperty(key) || Boolean.getBoolean(key)
    }
}
