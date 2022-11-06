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
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.Plugin
import org.kordamp.gradle.util.ConfigureUtil

/**
 * @author Andres Almiray
 * @since 0.39.0
 */
@CompileStatic
@Canonical
class Plugins extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.plugins'

    final Map<String, Plugin> plugins = [:]

    Plugins(ProjectConfigurationExtension config, Project project) {
        super(config, project, PLUGIN_ID)
    }

    @Override
    protected AbstractFeature getParentFeature() {
        return project.rootProject.extensions.getByType(ProjectConfigurationExtension).plugins
    }

    void plugin(Action<? super Plugin> action) {
        Plugin plugin = new Plugin()
        action.execute(plugin)
        plugins[plugin.name] = plugin
    }

    void plugin(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Plugin) Closure action) {
        Plugin plugin = new Plugin()
        ConfigureUtil.configure(action, plugin)
        plugins[plugin.name] = plugin
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        if (enabled) {
            plugins.values().each {
                map.putAll(it.toMap())
            }
        }

        new LinkedHashMap<>('plugins': map)
    }

    static void merge(Plugins o1, Plugins o2) {
        AbstractFeature.merge(o1, o2)

        Map<String, org.kordamp.gradle.plugin.base.model.Plugin> a = [:]
        Map<String, org.kordamp.gradle.plugin.base.model.Plugin> b = [:]
        a.putAll(o1.plugins)
        b.putAll(o2.plugins)

        a.each { k, plugin ->
            org.kordamp.gradle.plugin.base.model.Plugin.merge(plugin, b.remove(k))
        }
        a.putAll(b)
        o1.plugins.clear()
        o1.plugins.putAll(a)
    }

    @Override
    void normalize() {
        setVisible(isApplied())
    }

    @Override
    protected void normalizeEnabled() {
        if (!enabledSet) {
            setEnabled(isApplied())
        }
    }

    List<String> validate(ProjectConfigurationExtension extension) {
        List<String> errors = []

        if (!enabled) return errors

        List<String> allErrors = (List<String>) plugins.values().collect { it.validate(extension) }
            .flatten()
        errors.addAll(allErrors)

        errors
    }
}
