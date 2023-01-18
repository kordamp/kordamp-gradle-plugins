/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2023 Andres Almiray.
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
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.util.CollectionUtils

import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.32.0
 */
@CompileStatic
abstract class AbstractQualityFeature extends AbstractAggregateFeature {
    Boolean ignoreFailures
    String toolVersion
    final Set<String> excludedSourceSets = new LinkedHashSet<>()

    AbstractQualityFeature(ProjectConfigurationExtension config, Project project, String pluginId, String featureName) {
        super(config, project, pluginId, featureName)
    }

    protected void populateMapDescription(Map<String, Object> map) {
        map.ignoreFailures = getIgnoreFailures()
        map.toolVersion = toolVersion
        map.excludedSourceSets = excludedSourceSets
    }

    boolean getIgnoreFailures() {
        this.@ignoreFailures != null && this.@ignoreFailures
    }

    protected boolean isIgnoreFailuresSet() {
        this.@ignoreFailures != null
    }

    void excludeSourceSet(String s) {
        if (isNotBlank(s)) {
            excludedSourceSets << s
        }
    }

    static void merge(AbstractQualityFeature o1, AbstractQualityFeature o2) {
        AbstractAggregateFeature.merge(o1, o2)
        o1.ignoreFailures = o1.ignoreFailuresSet ? o1.getIgnoreFailures() : o2.getIgnoreFailures()
        o1.toolVersion = o1.toolVersion ?: o2.toolVersion
        CollectionUtils.merge(o1.@excludedSourceSets, o2?.excludedSourceSets)
    }
}
