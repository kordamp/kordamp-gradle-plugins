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
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.plugin.devel.PluginDeclaration
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.util.CollectionUtils

import static org.kordamp.gradle.util.StringUtils.getPropertyNameForLowerCaseHyphenSeparatedName
import static org.kordamp.gradle.util.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.16.0
 */
@CompileStatic
class Plugin extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.plugin'

    String id
    String implementationClass
    List<String> tags = []

    private final String pluginName

    Plugin(ProjectConfigurationExtension config, Project project) {
        super(config, project, PLUGIN_ID)

        pluginName = getPropertyNameForLowerCaseHyphenSeparatedName(project.name - '-gradle' - 'gradle-' - '-plugin')
    }

    @Override
    protected AbstractFeature getParentFeature() {
        return project.rootProject.extensions.getByType(ProjectConfigurationExtension).plugin
    }

    String getPluginName() {
        pluginName
    }

    void setId(String id) {
        this.id = id

        GradlePluginDevelopmentExtension gpde = project.extensions.findByType(GradlePluginDevelopmentExtension)
        if (gpde) {
            PluginDeclaration pd = gpde.plugins.maybeCreate(pluginName)
            pd.id = id
        }
    }

    void setImplementationClass(String implementationClass) {
        this.implementationClass = implementationClass

        GradlePluginDevelopmentExtension gpde = project.extensions.findByType(GradlePluginDevelopmentExtension)
        if (gpde) {
            PluginDeclaration pd = gpde.plugins.maybeCreate(pluginName)
            pd.implementationClass = implementationClass
        }
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        map.pluginName = pluginName
        map.id = id
        map.implementationClass = implementationClass
        map.tags = tags
        map.'gradle-plugin' = "${id}:${project.group}:${project.name}".toString()

        new LinkedHashMap<>('plugin': map)
    }

    static void merge(Plugin o1, Plugin o2) {
        AbstractFeature.merge(o1, o2)
        if (!o1.id && o2?.id) {
            o1.id = o2.id
        }
        if (!o1.implementationClass && o2?.implementationClass) {
            o1.implementationClass = o2.implementationClass
        }
        CollectionUtils.merge(o1.tags, o2?.tags)
    }

    void normalize() {
        if (!enabledSet) {
            setEnabled(isApplied())
        }
        setVisible(isApplied())
    }

    List<String> validate(ProjectConfigurationExtension extension) {
        List<String> errors = []

        if (!enabled) return errors

        if (isBlank(id)) {
            errors << "[${project.name}] Plugin id is blank".toString()
        }
        if (isBlank(implementationClass)) {
            errors << "[${project.name}] Plugin implementationClass is blank".toString()
        }
        if (!tags && !extension.info.tags) {
            errors << "[${project.name}] Plugin has no tags defined".toString()
        }

        errors
    }

    List<String> resolveTags(ProjectConfigurationExtension extension) {
        tags ?: extension.info.tags
    }
}
