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

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.util.ConfigureUtil

/**
 * @author Andres Almiray
 * @since 0.32.0
 */
@CompileStatic
abstract class AbstractAggregateFeature extends AbstractFeature {
    final Aggregate aggregate

    private final String featureName

    AbstractAggregateFeature(ProjectConfigurationExtension config, Project project, String pluginId, String featureName) {
        super(config, project, pluginId)
        aggregate = new Aggregate(config, project)
        this.featureName = featureName
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(
            enabled: enabled
        )

        if (isVisible()) {
            populateMapDescription(map)
        }

        if (isRoot() && isVisible()) {
            map.putAll(aggregate.toMap())
        }
        new LinkedHashMap<>((featureName): map)
    }

    protected abstract void populateMapDescription(Map<String, Object> map)

    protected boolean hasBasePlugin(Project project) {
        project.pluginManager.hasPlugin('java')
    }

    void aggregate(Action<? super Aggregate> action) {
        action.execute(aggregate)
    }

    void aggregate(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Aggregate) Closure<Void> action) {
        ConfigureUtil.configure(action, aggregate)
    }

    static void merge(AbstractAggregateFeature o1, AbstractAggregateFeature o2) {
        AbstractFeature.merge(o1, o2)
    }

    @CompileStatic
    static class Aggregate {
        Boolean enabled
        final Set<Project> excludedProjects = new LinkedHashSet<>()

        private final ProjectConfigurationExtension config
        private final Project project

        Aggregate(ProjectConfigurationExtension config, Project project) {
            this.config = config
            this.project = project
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<String, Object>()

            map.enabled = getEnabled()
            map.excludedProjects = excludedProjects

            new LinkedHashMap<>('aggregate': map)
        }

        boolean getEnabled() {
            this.@enabled == null || this.@enabled
        }

        void excludeProject(Project p) {
            if (null != p) {
                excludedProjects << p
            }
        }
    }
}
