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
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

/**
 * @author Andres Almiray
 * @since 0.31.0
 */
@CompileStatic
class Coveralls extends AbstractTestingFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.coveralls'

    Coveralls(ProjectConfigurationExtension config, Project project) {
        super(config, project, PLUGIN_ID)
    }

    @Override
    protected AbstractFeature getParentFeature() {
        return project.rootProject.extensions.getByType(ProjectConfigurationExtension).coverage.coveralls
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        if (!isRoot()) return [:]

        new LinkedHashMap<>('coveralls': new LinkedHashMap<>(
            enabled: enabled
        ))
    }

    @Override
    protected void normalizeEnabled() {
        if (!enabledSet) {
            if (isRoot()) {
                if (project.childProjects.size() == 0) {
                    enabled = project.extensions.getByType(ProjectConfigurationExtension).coverage.jacoco.enabled
                } else {
                    enabled = project.childProjects.values().any { hasTestSourceSets(it) }
                }
            } else {
                enabled = false
            }
        }
    }

    protected void normalizeVisible() {
        setVisible(isRoot() ? isApplied() : false)
    }

    static void merge(Coveralls o1, Coveralls o2) {
        AbstractFeature.merge(o1, o2)
    }
}
