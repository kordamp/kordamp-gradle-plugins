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
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

/**
 * @author Andres Almiray
 * @since 0.31.0
 */
@CompileStatic
class Coveralls extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.coveralls'

    Coveralls(ProjectConfigurationExtension config, Project project) {
        super(config, project)
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        new LinkedHashMap<>('coveralls': new LinkedHashMap<>(
            enabled: enabled
        ))
    }

    static void merge(Coveralls o1, Coveralls o2) {
        AbstractFeature.merge(o1, o2)
    }
}
