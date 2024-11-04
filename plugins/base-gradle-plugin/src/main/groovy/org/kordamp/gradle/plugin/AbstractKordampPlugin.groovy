/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2024 Andres Almiray.
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
package org.kordamp.gradle.plugin

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension

/**
 * @author Andres Almiray
 * @since 0.11.0
 */
@CompileStatic
abstract class AbstractKordampPlugin implements KordampPlugin {
    boolean enabled = true

    private final String visitedKey

    AbstractKordampPlugin(String pluginId) {
        visitedKey = 'VISITED_' + pluginId.replace('.', '_')
    }

    protected boolean hasBeenVisited(Project project) {
        return project.findProperty(visitedKey + '_' + project.path.replace(':','#'))
    }

    protected void setVisited(Project project, boolean visited) {
        ExtraPropertiesExtension ext = project.extensions.findByType(ExtraPropertiesExtension)
        ext.set(visitedKey + '_' + project.path.replace(':','#'), visited)
    }

    protected void setEnabled(boolean enabled) {
        this.enabled = enabled
    }
}
