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
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.plugins.quality.CodeNarc
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

/**
 * @author Andres Almiray
 * @since 0.31.0
 */
@CompileStatic
@Canonical
class Codenarc extends AbstractFeature {
    File configFile
    int maxPriority1Violations
    int maxPriority2Violations
    int maxPriority3Violations
    String toolVersion = '1.5'
    boolean ignoreFailures = true

    private boolean ignoreFailuresSet

    Codenarc(ProjectConfigurationExtension config, Project project) {
        super(config, project)
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        new LinkedHashMap<>('codenarc': new LinkedHashMap<String, Object>(
            enabled: enabled,
            configFile: configFile,
            maxPriority1Violations: maxPriority1Violations,
            maxPriority2Violations: maxPriority2Violations,
            maxPriority3Violations: maxPriority3Violations,
            ignoreFailures: ignoreFailures,
            toolVersion: toolVersion,
        ))
    }

    void normalize() {
        if (null == configFile) {
            File file = project.rootProject.file("config/codenarc/${project.name}.xml")
            if (!file.exists()) {
                file = project.rootProject.file('config/codenarc/codenarc.xml')
            }
            configFile = file
        }

        if (!enabledSet) {
            if (isRoot()) {
                if (project.childProjects.isEmpty()) {
                    enabled = project.pluginManager.hasPlugin('groovy')
                } else {
                    enabled = project.childProjects.values().any { p -> p.pluginManager.hasPlugin('groovy') }
                }
            } else {
                enabled = project.pluginManager.hasPlugin('groovy')
            }
        }
    }

    void setIgnoreFailures(boolean ignoreFailures) {
        this.ignoreFailures = ignoreFailures
        this.ignoreFailuresSet = true
    }

    boolean isIgnoreFailuresSet() {
        this.ignoreFailuresSet
    }

    void copyInto(Codenarc copy) {
        super.copyInto(copy)
        copy.@ignoreFailures = ignoreFailures
        copy.@ignoreFailuresSet = ignoreFailuresSet
        copy.configFile = configFile
        copy.maxPriority1Violations = maxPriority1Violations
        copy.maxPriority2Violations = maxPriority2Violations
        copy.maxPriority3Violations = maxPriority3Violations
        copy.toolVersion = toolVersion
    }

    static void merge(Codenarc o1, Codenarc o2) {
        AbstractFeature.merge(o1, o2)
        o1.setIgnoreFailures((boolean) (o1.ignoreFailuresSet ? o1.ignoreFailures : o2.ignoreFailures))
        o1.configFile = o1.configFile ?: o2.configFile
        o1.maxPriority1Violations = o1.maxPriority1Violations ?: o2.maxPriority1Violations
        o1.maxPriority2Violations = o1.maxPriority2Violations ?: o2.maxPriority2Violations
        o1.maxPriority3Violations = o1.maxPriority3Violations ?: o2.maxPriority3Violations
        o1.toolVersion = o1.toolVersion ?: o2.toolVersion
    }

    @CompileDynamic
    void applyTo(CodeNarc codenarcTask) {
        String sourceSetName = (codenarcTask.name - 'codenarc').uncapitalize()
        sourceSetName = sourceSetName == 'allCodenarc' ? project.name : sourceSetName
        sourceSetName = sourceSetName == 'aggregateCodenarc' ? 'aggregate' : sourceSetName
        codenarcTask.enabled = enabled && configFile.exists()
        codenarcTask.configFile = configFile
        codenarcTask.maxPriority1Violations = maxPriority1Violations
        codenarcTask.maxPriority2Violations = maxPriority2Violations
        codenarcTask.maxPriority3Violations = maxPriority3Violations
        codenarcTask.ignoreFailures = ignoreFailures
        codenarcTask.reports.html.enabled = true
        codenarcTask.reports.xml.enabled = true
        codenarcTask.reports.html.destination = project.layout.buildDirectory.file("reports/codenarc/${sourceSetName}.html").get().asFile
        codenarcTask.reports.xml.destination = project.layout.buildDirectory.file("reports/codenarc/${sourceSetName}.xml").get().asFile
    }
}
