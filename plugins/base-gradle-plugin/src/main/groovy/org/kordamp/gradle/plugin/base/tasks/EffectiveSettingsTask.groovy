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
package org.kordamp.gradle.plugin.base.tasks

import groovy.transform.CompileStatic
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

import static org.kordamp.gradle.util.PluginUtils.resolveConfig
import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
class EffectiveSettingsTask extends AbstractReportingTask {
    private final List<String> COVERAGE = ['coveralls', 'jacoco'].asImmutable()
    private final List<String> ARTIFACTS = ['jar', 'source', 'minpom'].asImmutable()
    private final List<String> DOCS = ['guide', 'groovydoc', 'javadoc', 'kotlindoc', 'scaladoc', 'sourceHtml', 'sourceXref'].asImmutable()
    private final List<String> QUALITY = ['checkstyle', 'codenarc', 'detekt', 'errorprone', 'pmd', 'cpd', 'sonar', 'spotbugs'].asImmutable()
    private final List<String> SECTIONS = [
        'buildInfo',
        'bintray',
        'publishing',
        'bom',
        'licensing',
        'testing',
        'clirr',
        'plugin',
        'stats',
        'coverage',
        'artifacts',
        'docs',
        'quality'
    ] + COVERAGE + ARTIFACTS + DOCS + QUALITY

    private String section
    private Set<String> sections

    @Option(option = 'section', description = 'The section to generate the report for.')
    void setSection(String section) {
        this.section = section
    }

    @Option(option = 'sections', description = 'The sections to generate the report for.')
    void setSections(String sections) {
        if (isNotBlank(sections)) {
            this.sections = (sections.split(',').collect { it.trim() }) as Set
        }
    }

    @TaskAction
    void displayEffectiveSettings() {
        Map<String, Object> map = resolveConfig(project).toMap()

        if (sections) {
            sections.each { s ->
                printSection(map, s)
            }
        } else if (section) {
            printSection(map, section)
        } else {
            doPrint(map, 0)
        }
    }

    private void printSection(Map<String, Object> map, String section) {
        if (map.containsKey(section)) {
            println "${section}:"
            doPrint(map[section], 1)
        } else if (section in QUALITY) {
            printSection((Map<String, Object>) map.quality, section)
        } else if (section in DOCS) {
            printSection((Map<String, Object>) map.docs, section)
        } else if (section in ARTIFACTS) {
            printSection((Map<String, Object>) map.artifacts, section)
        } else if (section in COVERAGE) {
            printSection((Map<String, Object>) map.coverage, section)
        } else if (section in SECTIONS) {
            println("The plugin that enables section '${section}' has not been applied.")
        } else {
            throw new IllegalStateException("Unknown section '$section'")
        }
    }
}
