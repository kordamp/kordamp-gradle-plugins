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

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

/**
 * @author Andres Almiray
 * @since 0.32.0
 */
@CompileStatic
@Canonical
abstract class AbstractAggregateFeature extends AbstractFeature {
    final Aggregate aggregate

    private final String featureName

    AbstractAggregateFeature(ProjectConfigurationExtension config, Project project, String pluginId, String featureName) {
        super(config, project)
        aggregate = new Aggregate(config, project)
        this.featureName = featureName
        doSetEnabled(project.plugins.findPlugin(pluginId) != null)
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(
            enabled: enabled
        )

        populateMapDescription(map)

        if (isRoot()) {
            map.putAll(aggregate.toMap())
        }
        new LinkedHashMap<>((featureName): map)
    }

    protected abstract void populateMapDescription(Map<String, Object> map)

    void normalize() {
        if (!enabledSet) {
            if (isRoot()) {
                if (project.childProjects.isEmpty()) {
                    setEnabled(project.pluginManager.hasPlugin('java') && isApplied())
                } else {
                    setEnabled(project.childProjects.values().any { p -> p.pluginManager.hasPlugin('java') && isApplied(p) })
                }
            } else {
                setEnabled(project.pluginManager.hasPlugin('java') && isApplied())
            }
        }
    }

    void aggregate(Action<? super Aggregate> action) {
        action.execute(aggregate)
    }

    void aggregate(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Aggregate) Closure<Void> action) {
        ConfigureUtil.configure(action, aggregate)
    }

    void copyInto(AbstractAggregateFeature copy) {
        super.copyInto(copy)
        aggregate.copyInto(copy.aggregate)
    }

    static void merge(AbstractAggregateFeature o1, AbstractAggregateFeature o2) {
        AbstractFeature.merge(o1, o2)
        o1.aggregate.merge(o2.aggregate)
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

        void copyInto(Aggregate copy) {
            copy.@enabled = this.@enabled
            copy.excludedProjects.addAll(excludedProjects)
        }

        Aggregate copyOf() {
            Aggregate copy = new Aggregate(config, project)
            copyInto(copy)
            copy
        }

        Aggregate merge(Aggregate other) {
            Aggregate copy = copyOf()
            copy.enabled = copy.@enabled != null ? copy.getEnabled() : other.getEnabled()
            copy
        }

        void excludeProject(Project p) {
            if (null != p) {
                excludedProjects << p
            }
        }
    }
}
