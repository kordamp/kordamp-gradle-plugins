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
package org.kordamp.gradle.plugin.base.tasks

import groovy.transform.CompileStatic
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

/**
 * @author Andres Almiray
 * @since 0.11.0
 */
@CompileStatic
class ProjectPropertiesTask extends AbstractReportingTask {
    private String section

    @Option(option = 'section', description = 'The section to generate the report for.')
    void setSection(String section) {
        this.section = section
    }

    @TaskAction
    void report() {
        Map<String, ?> map = resolveProperties()

        if (section) {
            printSection(map, section)
        } else {
            doPrint(map, 0)
        }
    }

    private void printSection(Map<String, ?> map, String section) {
        if (map.containsKey(section)) {
            println "${section}:"
            doPrint((Map<String, ?>) map[section], 1)
        } else {
            throw new IllegalStateException("Unknown section '$section'")
        }
    }

    private Map<String, Map<String, ?>> resolveProperties() {
        Map<String, Map<String, ?>> props = [:]
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
        props.ext.putAll(project.extensions.findByType(ExtraPropertiesExtension).properties)

        props
    }
}
