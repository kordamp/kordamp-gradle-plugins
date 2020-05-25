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
import org.gradle.api.Action
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.util.ConfigureUtil

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
class Guide extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.guide'

    final Publish publish

    Guide(ProjectConfigurationExtension config, Project project) {
        super(config, project, PLUGIN_ID)
        publish = new Publish(config)
    }

    @Override
    protected AbstractFeature getParentFeature() {
        return project.rootProject.extensions.getByType(ProjectConfigurationExtension).docs.guide
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        map.publish = publish.toMap()

        new LinkedHashMap<>(['guide': map])
    }

    static void merge(Guide o1, Guide o2) {
        AbstractFeature.merge(o1, o2)
        Publish.merge(o1.publish, o2.publish)
    }

    void publish(Action<? super Publish> action) {
        action.execute(publish)
    }

    void publish(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Publish) Closure<Void> action) {
        ConfigureUtil.configure(action, publish)
    }

    void normalize() {
        if (!enabledSet) {
            doSetEnabled(isApplied())
        }
        publish.normalize()
        setVisible(isApplied())
    }

    @CompileStatic
    static class Publish {
        String branch = 'gh-pages'
        String message

        private Boolean enabled
        final Project project

        Publish(ProjectConfigurationExtension config) {
            project = config.project
            message = "Publish guide for ${config.project.version}"
        }

        boolean getEnabled() {
            this.@enabled != null && this.@enabled
        }

        void normalize() {
            if (null == enabled) {
                enabled = project.pluginManager.hasPlugin('org.ajoberstar.git-publish')
            }
        }

        static void merge(Publish o1, Publish o2) {
            o1.@enabled = o1.@enabled != null ? o1.getEnabled() : o2.getEnabled()
            o1.branch = o1.branch ?: o2.branch
            o1.message = o1.message ?: o2.message
        }

        Map<String, Object> toMap() {
            new LinkedHashMap<String, Object>(
                enabled: getEnabled(),
                branch: branch,
                message: message)
        }
    }
}
