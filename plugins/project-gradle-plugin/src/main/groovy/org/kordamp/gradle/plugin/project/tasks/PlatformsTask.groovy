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
package org.kordamp.gradle.plugin.project.tasks

import groovy.transform.CompileStatic
import org.gradle.api.tasks.TaskAction
import org.kordamp.gradle.plugin.base.tasks.AbstractReportingTask
import org.kordamp.gradle.plugin.project.ConfigurationsDependencyHandler
import org.kordamp.gradle.plugin.project.internal.DependencyHandlerImpl

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
class PlatformsTask extends AbstractReportingTask {
    @TaskAction
    void report() {
        ConfigurationsDependencyHandler dh = project.dependencies.extensions.findByType(ConfigurationsDependencyHandler)
        if (dh) {
            List<Map<String, Object>> platforms = ((DependencyHandlerImpl.ConfigurationsDependencyHandlerImpl) dh).platforms
            println('Total platforms: ' + console.cyan((platforms.size()).toString()) + '\n')
            platforms.eachWithIndex { Map<String, Object> platform, int index ->
                doPrintMapEntry("Platform $index", platform, 0)
            }
        } else {
            println('Total platforms: ' + console.cyan('0') + '\n')
        }
    }
}
