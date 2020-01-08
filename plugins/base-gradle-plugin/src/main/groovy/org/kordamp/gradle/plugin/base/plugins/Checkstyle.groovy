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

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.CollectionUtils
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

/**
 * @author Andres Almiray
 * @since 0.31.0
 */
@CompileStatic
@Canonical
class Checkstyle extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.checkstyle'

    File configFile
    Map<String, Object> configProperties = [:]
    int maxErrors
    int maxWarnings = Integer.MAX_VALUE
    boolean showViolations = true
    boolean ignoreFailures = true
    String toolVersion = '8.27'
    Set<String> excludes = new LinkedHashSet<>()
    Set<String> includes = new LinkedHashSet<>()
    final Aggregate aggregate

    private boolean showViolationsSet
    private boolean ignoreFailuresSet

    Checkstyle(ProjectConfigurationExtension config, Project project) {
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
            configProperties: configProperties,
            maxErrors: maxErrors,
            maxWarnings: maxWarnings,
            excludes: excludes,
            includes: includes,
            showViolations: showViolations,
            ignoreFailures: ignoreFailures,
            toolVersion: toolVersion
        )
        if (isRoot()) {
            map.putAll(aggregate.toMap())
        }

        new LinkedHashMap<>('checkstyle': map)
    }

    void normalize() {
        if (null == configFile) {
            File file = project.rootProject.file("config/checkstyle/${project.name}.xml")
            if (!file.exists()) {
                file = project.rootProject.file('config/checkstyle/checkstyle.xml')
            }
            configFile = file
        }

        if (!enabledSet) {
            if (isRoot()) {
                if (project.childProjects.isEmpty()) {
                    setEnabled(project.pluginManager.hasPlugin('java') && isApplied())
                } else {
                    setEnabled(project.childProjects.values().any { p -> p.pluginManager.hasPlugin('java') && isApplied()})
                }
            } else {
                setEnabled(project.pluginManager.hasPlugin('java') && isApplied())
            }
        }
    }

    void setShowViolations(boolean showViolations) {
        this.showViolations = showViolations
        this.showViolationsSet = true
    }

    boolean isShowViolationsSet() {
        this.showViolationsSet
    }

    void setIgnoreFailures(boolean ignoreFailures) {
        this.ignoreFailures = ignoreFailures
        this.ignoreFailuresSet = true
    }

    boolean isIgnoreFailuresSet() {
        this.ignoreFailuresSet
    }

    void include(String str) {
        includes << str
    }

    void exclude(String str) {
        excludes << str
    }

    void aggregate(Action<? super Aggregate> action) {
        action.execute(aggregate)
    }

    void aggregate(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Aggregate) Closure action) {
        ConfigureUtil.configure(action, aggregate)
    }

    void copyInto(Checkstyle copy) {
        super.copyInto(copy)
        copy.@showViolations = showViolations
        copy.@showViolationsSet = showViolationsSet
        copy.@ignoreFailures = ignoreFailures
        copy.@ignoreFailuresSet = ignoreFailuresSet
        copy.excludes.addAll(excludes)
        copy.includes.addAll(includes)
        copy.configFile = configFile
        copy.maxErrors = maxErrors
        copy.maxWarnings = maxWarnings
        copy.toolVersion = toolVersion
        copy.configProperties.putAll(configProperties)
        aggregate.copyInto(copy.aggregate)
    }

    static void merge(Checkstyle o1, Checkstyle o2) {
        AbstractFeature.merge(o1, o2)
        o1.setShowViolations((boolean) (o1.showViolationsSet ? o1.showViolations : o2.showViolations))
        o1.setIgnoreFailures((boolean) (o1.ignoreFailuresSet ? o1.ignoreFailures : o2.ignoreFailures))
        CollectionUtils.merge(o1.excludes, o2?.excludes)
        CollectionUtils.merge(o1.includes, o2?.includes)
        o1.configFile = o1.configFile ?: o2.configFile
        o1.maxErrors = o1.maxErrors ?: o2.maxErrors
        o1.maxWarnings = o1.maxWarnings ?: o2.maxWarnings
        o1.toolVersion = o1.toolVersion ?: o2.toolVersion
        CollectionUtils.merge(o1.configProperties, o2?.configProperties)
        o1.aggregate.merge(o2.aggregate)
    }

    @CompileDynamic
    void applyTo(org.gradle.api.plugins.quality.Checkstyle checkstyleTask) {
        String sourceSetName = (checkstyleTask.name - 'checkstyle').uncapitalize()
        sourceSetName = sourceSetName == 'allCheckstyle' ? project.name : sourceSetName
        sourceSetName = sourceSetName == 'aggregateCheckstyle' ? 'aggregate' : sourceSetName
        checkstyleTask.enabled = enabled && configFile.exists()
        checkstyleTask.includes.addAll(includes)
        checkstyleTask.excludes.addAll(excludes)
        checkstyleTask.configFile = configFile
        checkstyleTask.maxErrors = maxErrors
        checkstyleTask.maxWarnings = maxWarnings
        checkstyleTask.showViolations = showViolations
        checkstyleTask.ignoreFailures = ignoreFailures
        checkstyleTask.reports.html.enabled = true
        checkstyleTask.reports.xml.enabled = true
        checkstyleTask.reports.html.destination = project.layout.buildDirectory.file("reports/checkstyle/${sourceSetName}.html").get().asFile
        checkstyleTask.reports.xml.destination = project.layout.buildDirectory.file("reports/checkstyle/${sourceSetName}.xml").get().asFile
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
