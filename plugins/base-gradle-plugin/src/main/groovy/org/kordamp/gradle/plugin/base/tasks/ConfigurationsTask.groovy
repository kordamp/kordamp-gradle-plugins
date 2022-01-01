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
package org.kordamp.gradle.plugin.base.tasks

import groovy.transform.CompileStatic
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.TaskAction

/**
 * @author Andres Almiray
 * @since 0.24.0
 */
@CompileStatic
class ConfigurationsTask extends AbstractReportingTask {
    @TaskAction
    void report() {
        Map<String, Map<String, ?>> map = [:]

        project.configurations.eachWithIndex { Configuration configuration, int index ->
            map.putAll(ConfigurationsTask.doReport(configuration, index))
        }

        println('Total configurations: ' + console.cyan((map.size()).toString()) + '\n')
        doPrint(map, 0)
    }

    private static Map<String, Map<String, ?>> doReport(Configuration configuration, int index) {
        Map<String, ?> map = [:]

        map.name = configuration.name

        new LinkedHashMap<>([('configuration ' + index): map])
    }
}
