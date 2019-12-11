/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
class Guide extends AbstractFeature {
    final Publish publish

    Guide(ProjectConfigurationExtension config, Project project) {
        super(config, project)
        publish = new Publish(config)
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        if (enabled) {
            map.publish = publish.toMap()
        }

        new LinkedHashMap<>(['guide': map])
    }

    void copyInto(Guide copy) {
        super.copyInto(copy)
        publish.copyInto(copy.publish)
    }

    static void merge(Guide o1, Guide o2) {
        AbstractFeature.merge(o1, o2)
        Publish.merge(o1.publish, o2.publish)
    }

    void publish(Action<? super Publish> action) {
        action.execute(publish)
    }

    void publish(@DelegatesTo(Publish) Closure action) {
        ConfigureUtil.configure(action, publish)
    }

    void normalize() {
        publish.normalize()
    }

    @CompileStatic
    @Canonical
    static class Publish {
        String branch = 'gh-pages'
        String message

        private boolean enabled = false
        final Project project

        Publish(ProjectConfigurationExtension config) {
            project = config.project
            message = "Publish guide for ${config.project.version}"
        }

        boolean isEnabled() {
            this.@enabled
        }

        void normalize() {
            enabled = project.pluginManager.hasPlugin('org.ajoberstar.git-publish')
        }

        void copyInto(Publish copy) {
            copy.@enabled = this.enabled
            copy.@branch = this.branch
            copy.@message = this.message
        }

        static void merge(Publish o1, Publish o2) {
            o1.branch = o1.branch ?: o2.branch
            o1.message = o1.message ?: o2.message
        }

        Map<String, Object> toMap() {
            new LinkedHashMap<String, Object>(
                enabled: enabled,
                branch: branch,
                message: message)
        }
    }
}
