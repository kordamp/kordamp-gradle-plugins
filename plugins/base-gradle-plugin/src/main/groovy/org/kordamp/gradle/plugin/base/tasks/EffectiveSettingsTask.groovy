/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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

import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
class EffectiveSettingsTask extends AbstractReportingTask {
    private String section
    private Set<String> sections

    @Option(option = 'section', description = 'The section to generate the report for.')
    void setSection(String section) {
        this.section = section
    }

    @Option(option = 'sections', description = 'The sections to generate the report for.')
    void setSections(String sections) {
        if (!isBlank(sections)) {
            this.sections = (sections.split(',').collect { it.trim() }) as Set
        }
    }

    @TaskAction
    void displayEffectiveSettings() {
        Map<String, Object> map = project.extensions.findByName(ProjectConfigurationExtension.EFFECTIVE_CONFIG_NAME).toMap()

        if (sections) {
            sections.each { s ->
                EffectiveSettingsTask.printSection(map, s)
            }
        } else if (section) {
            printSection(map, section)
        } else {
            doPrint(map, 0)
        }
    }

    private static void printSection(Map<String, Object> map, String section) {
        if (map.containsKey(section)) {
            println "${section}:"
            doPrint(map[section], 1)
        } else {
            throw new IllegalStateException("Unknown section '$section'")
        }
    }
}
