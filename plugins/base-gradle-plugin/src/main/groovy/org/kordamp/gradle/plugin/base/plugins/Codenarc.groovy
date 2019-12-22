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
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.quality.CodeNarc
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

/**
 * @author Andres Almiray
 * @since 0.31.0
 */
@CompileStatic
@Canonical
class Codenarc extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.codenarc'

    File configFile
    int maxPriority1Violations
    int maxPriority2Violations
    int maxPriority3Violations
    String toolVersion = '1.5'
    boolean ignoreFailures = true
    final Aggregate aggregate

    private boolean ignoreFailuresSet

    Codenarc(ProjectConfigurationExtension config, Project project) {
        super(config, project)
        aggregate = new Aggregate(config, project)
        doSetEnabled(project.plugins.findPlugin(PLUGIN_ID) != null)
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(
            enabled: enabled,
            configFile: configFile,
            maxPriority1Violations: maxPriority1Violations,
            maxPriority2Violations: maxPriority2Violations,
            maxPriority3Violations: maxPriority3Violations,
            ignoreFailures: ignoreFailures,
            toolVersion: toolVersion,
        )
        if (isRoot()) {
            map.putAll(aggregate.toMap())
        }
        new LinkedHashMap<>('codenarc': map)
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
                    setEnabled(project.pluginManager.hasPlugin('groovy') && isApplied())
                } else {
                    setEnabled(project.childProjects.values().any { p -> p.pluginManager.hasPlugin('groovy') && isApplied()})
                }
            } else {
                setEnabled(project.pluginManager.hasPlugin('groovy') && isApplied())
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

    void aggregate(Action<? super Aggregate> action) {
        action.execute(aggregate)
    }

    void aggregate(@DelegatesTo(Aggregate) Closure action) {
        ConfigureUtil.configure(action, aggregate)
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
        aggregate.copyInto(copy.aggregate)
    }

    static void merge(Codenarc o1, Codenarc o2) {
        AbstractFeature.merge(o1, o2)
        o1.setIgnoreFailures((boolean) (o1.ignoreFailuresSet ? o1.ignoreFailures : o2.ignoreFailures))
        o1.configFile = o1.configFile ?: o2.configFile
        o1.maxPriority1Violations = o1.maxPriority1Violations ?: o2.maxPriority1Violations
        o1.maxPriority2Violations = o1.maxPriority2Violations ?: o2.maxPriority2Violations
        o1.maxPriority3Violations = o1.maxPriority3Violations ?: o2.maxPriority3Violations
        o1.toolVersion = o1.toolVersion ?: o2.toolVersion
        o1.aggregate.merge(o2.aggregate)
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

    @CompileStatic
    static class Aggregate {
        Boolean enabled
        private final Set<Project> excludedProjects = new LinkedHashSet<>()

        private final ProjectConfigurationExtension config
        private final Project project

        Aggregate(ProjectConfigurationExtension config, Project project) {
            this.config = config
            this.project = project
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<String, Object>()

            map.enabled = getEnabled()
            map.excludedProjects = excludedProjects

            new LinkedHashMap<>('aggregate': map)
        }

        boolean getEnabled() {
            this.@enabled == null || this.@enabled
        }

        void copyInto(Aggregate copy) {
            copy.@enabled = this.@enabled
            copy.excludedProjects.addAll(excludedProjects)
        }

        Aggregate copyOf() {
            Aggregate copy = new Aggregate(config, project)
            copyInto(copy)
            copy
        }

        Aggregate merge(Aggregate other) {
            Aggregate copy = copyOf()
            copy.enabled = copy.@enabled != null ? copy.getEnabled() : other.getEnabled()
            copy
        }

        Set<Project> excludedProjects() {
            excludedProjects
        }
    }
}
