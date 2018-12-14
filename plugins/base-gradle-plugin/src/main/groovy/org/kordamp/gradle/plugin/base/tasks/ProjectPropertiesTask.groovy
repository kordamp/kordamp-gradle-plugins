/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kordamp.gradle.plugin.base.tasks

import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

/**
 * @author Andres Almiray
 * @since 0.11.0
 */
class ProjectPropertiesTask extends AbstractReportingTask {
    private String section

    @Option(option = 'section', description = 'The section to generate the report for.')
    void setSection(String section) {
        this.section = section
    }

    @TaskAction
    void report() {
        Map<String, Object> map = resolveProperties()

        if (section) {
            printSection(map, section)
        } else {
            doPrint(map, 0)
        }
    }

    private void printSection(Map<String, Object> map, String section) {
        if (map.containsKey(section)) {
            println "${section}:"
            doPrint(map[section], 1)
        } else {
            throw new IllegalStateException("Unknown section '$section'")
        }
    }

    private Map<String, Map<String, Object>> resolveProperties() {
        Map<String, Map<String, Object>> props = [:]
        props.project = [
            name        : project.name,
            version     : project.version,
            group       : project.group,
            path        : project.path,
            description : project.description,
            displayName : project.displayName,
            projectDir  : project.projectDir,
            buildFile   : project.buildFile.absolutePath,
            buildDir    : project.buildDir,
            defaultTasks: project.defaultTasks
        ]

        props.ext = new TreeMap<>()
        project.extensions.findByType(ExtraPropertiesExtension).properties.each { key, value ->
            if (key == 'mergedConfiguration') {
                return
            }
            props.ext[key] = value
        }

        props
    }
}
