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
class Source extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.source-jar'

    final Aggregate aggregate

    Source(ProjectConfigurationExtension config, Project project) {
        super(config, project)
        aggregate = new Aggregate(config, project)
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        if (isRoot()) {
            map.putAll(aggregate.toMap())
        }

        new LinkedHashMap<>('source': map)
    }

    void aggregate(Action<? super Aggregate> action) {
        action.execute(aggregate)
    }

    void aggregate(@DelegatesTo(Aggregate) Closure action) {
        ConfigureUtil.configure(action, aggregate)
    }

    void copyInto(Source copy) {
        super.copyInto(copy)
        aggregate.copyInto(copy.aggregate)
    }

    static void merge(Source o1, Source o2) {
        AbstractFeature.merge(o1, o2)
        o1.aggregate.merge(o2.aggregate)
    }

    @CompileStatic
    static class Aggregate {
        Boolean enabled
        private final Set<Project> excludedProjects = new LinkedHashSet<>()

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

        Set<Project> excludedProjects() {
            excludedProjects
        }
    }
}
