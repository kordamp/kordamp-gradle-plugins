/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2025 Andres Almiray.
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
import org.gradle.api.file.ConfigurableFileCollection
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.util.CollectionUtils

/**
 * @author Andres Almiray
 * @since 0.43.0
 */
@CompileStatic
class Reproducible extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.reproducible'

    final ConfigurableFileCollection additionalArtifacts
    Map<String, Object> additionalProperties = [:]

    Reproducible(ProjectConfigurationExtension config, Project project) {
        super(config, project, PLUGIN_ID)
        additionalArtifacts = project.objects.fileCollection()
    }

    @Override
    protected AbstractFeature getParentFeature() {
        return project.rootProject.extensions.getByType(ProjectConfigurationExtension).reproducible
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        map.additionalArtifacts = additionalArtifacts
        map.additionalProperties = this.additionalProperties

        new LinkedHashMap<>('reproducible': map)
    }

    static void merge(Reproducible o1, Reproducible o2) {
        AbstractFeature.merge(o1, o2)
        o1.additionalArtifacts.from(o2.additionalArtifacts)
        o1.additionalProperties = CollectionUtils.merge(o1.additionalProperties, o2?.additionalProperties, false)
    }

    List<String> validate(ProjectConfigurationExtension extension) {
        List<String> errors = []

        if (!enabled) return errors

        errors
    }
}
