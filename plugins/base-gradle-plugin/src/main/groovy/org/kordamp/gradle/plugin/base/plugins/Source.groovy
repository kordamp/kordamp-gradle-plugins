/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2021 Andres Almiray.
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
class Source extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.source-jar'

    Boolean empty
    final Aggregate aggregate

    Source(ProjectConfigurationExtension config, Project project) {
        super(config, project, PLUGIN_ID)
        aggregate = new Aggregate(config, project)
    }

    @Override
    protected AbstractFeature getParentFeature() {
        return project.rootProject.extensions.getByType(ProjectConfigurationExtension).artifacts.source
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        map['empty'] = getEmpty()

        if (isRoot()) {
            map.putAll(aggregate.toMap())
        }

        new LinkedHashMap<>(['source': map])
    }

    boolean getEmpty() {
        this.@empty != null && this.@empty
    }

    void aggregate(Action<? super Aggregate> action) {
        action.execute(aggregate)
    }

    void aggregate(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Aggregate) Closure<Void> action) {
        ConfigureUtil.configure(action, aggregate)
    }

    static void merge(Source o1, Source o2) {
        AbstractFeature.merge(o1, o2)
        o1.setEmpty(o1.@empty != null ? o1.getEmpty() : o2.getEmpty())
        Aggregate.merge(o1.aggregate, o2.aggregate)
    }

    @CompileStatic
    static class Aggregate {
        Boolean enabled
        Boolean empty
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
            map['empty'] = getEmpty()
            map.excludedProjects = excludedProjects

            new LinkedHashMap<>('aggregate': map)
        }

        boolean getEnabled() {
            this.@enabled == null || this.@enabled
        }

        boolean getEmpty() {
            this.@empty != null && this.@empty
        }
        static Aggregate merge(Aggregate o1, Aggregate o2) {
            o1.enabled = o1.@enabled != null ? o1.getEnabled() : o2.getEnabled()
            o1.empty = o1.@empty != null ? o1.getEmpty() : o2.getEmpty()
            o1
        }
    }
}
