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
package org.kordamp.gradle.plugin.properties.internal

import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.provider.Property
import org.kordamp.gradle.plugin.properties.PropertiesExtension
import org.kordamp.gradle.plugin.properties.TomlPropertiesReader
import org.kordamp.gradle.plugin.properties.YamlPropertiesReader
import org.kordamp.gradle.util.ConfigureUtil

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
class PropertiesExtensionImpl implements PropertiesExtension {
    final YamlSpecImpl yaml
    final TomlSpecImpl toml

    private final Settings settings

    PropertiesExtensionImpl(Settings settings, ObjectFactory objects) {
        this.settings = settings

        yaml = new YamlSpecImpl(objects)
        toml = new TomlSpecImpl(objects)

        settings.gradle.addBuildListener(new BuildAdapter() {
            @Override
            void projectsLoaded(Gradle gradle) {
                loadGradleHomeProperties(gradle)
                loadProjectProperties(gradle.rootProject)
                loadUserHomeProperties(gradle)
            }
        })
    }

    private void loadGradleHomeProperties(Gradle gradle) {
        if (yaml.enabled.get()) {
            File file = new File(gradle.gradleHomeDir, 'gradle.yml')
            YamlPropertiesReader.readProperties(file, gradle.rootProject.extensions.getByType(ExtraPropertiesExtension), yaml.overwrite.get())
        }
        if (toml.enabled.get()) {
            File file = new File(gradle.gradleHomeDir, 'gradle.toml')
            TomlPropertiesReader.readProperties(file, gradle.rootProject.extensions.getByType(ExtraPropertiesExtension), toml.overwrite.get())
        }
    }

    private void loadUserHomeProperties(Gradle gradle) {
        if (yaml.enabled.get()) {
            File file = new File(gradle.gradleUserHomeDir, 'gradle.yml')
            YamlPropertiesReader.readProperties(file, gradle.rootProject.extensions.getByType(ExtraPropertiesExtension), yaml.overwrite.get())
        }
        if (toml.enabled.get()) {
            File file = new File(gradle.gradleUserHomeDir, 'gradle.toml')
            TomlPropertiesReader.readProperties(file, gradle.rootProject.extensions.getByType(ExtraPropertiesExtension), toml.overwrite.get())
        }
    }

    private void loadProjectProperties(Project project) {
        if (yaml.enabled.get()) {
            File file = new File(project.projectDir, 'gradle.yml')
            YamlPropertiesReader.readProperties(file, project.extensions.getByType(ExtraPropertiesExtension), yaml.overwrite.get())
            for (Project p : project.childProjects.values()) {
                loadProjectProperties(p)
            }
        }
        if (toml.enabled.get()) {
            File file = new File(project.projectDir, 'gradle.toml')
            TomlPropertiesReader.readProperties(file, project.extensions.getByType(ExtraPropertiesExtension), toml.overwrite.get())
            for (Project p : project.childProjects.values()) {
                loadProjectProperties(p)
            }
        }
    }

    @Override
    void yaml(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = YamlSpec) Closure<Void> action) {
        ConfigureUtil.configure(action, yaml)
    }

    @Override
    void yaml(Action<? super YamlSpec> action) {
        action.execute(yaml)
    }

    @Override
    void toml(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = TomlSpec) Closure<Void> action) {
        ConfigureUtil.configure(action, toml)
    }

    @Override
    void toml(Action<? super TomlSpec> action) {
        action.execute(toml)
    }

    static class YamlSpecImpl implements YamlSpec {
        final Property<Boolean> enabled
        final Property<Boolean> overwrite

        YamlSpecImpl(ObjectFactory objects) {
            enabled = objects.property(Boolean).convention(true)
            overwrite = objects.property(Boolean).convention(true)
        }
    }

    static class TomlSpecImpl implements TomlSpec {
        final Property<Boolean> enabled
        final Property<Boolean> overwrite

        TomlSpecImpl(ObjectFactory objects) {
            enabled = objects.property(Boolean).convention(true)
            overwrite = objects.property(Boolean).convention(true)
        }
    }
}
