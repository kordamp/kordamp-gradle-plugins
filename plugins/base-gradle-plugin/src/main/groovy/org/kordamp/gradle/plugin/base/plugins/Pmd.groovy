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
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.quality.TargetJdk
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

/**
 * @author Andres Almiray
 * @since 0.31.0
 */
@CompileStatic
@Canonical
class Pmd extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.pmd'

    FileCollection ruleSetFiles
    boolean incrementalAnalysis
    boolean ignoreFailures = true
    int rulePriority = 5
    String toolVersion = '6.2.0'
    final Aggregate aggregate

    private boolean incrementalAnalysisSet
    private boolean ignoreFailuresSet

    Pmd(ProjectConfigurationExtension config, Project project) {
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
            ruleSetFiles: ruleSetFiles,
            incrementalAnalysis: incrementalAnalysis,
            ignoreFailures: ignoreFailures,
            rulePriority: rulePriority,
            toolVersion: toolVersion,
        )
        if (isRoot()) {
            map.putAll(aggregate.toMap())
        }
        new LinkedHashMap<>('pmd': map)
    }

    void normalize() {
        if (null == ruleSetFiles) {
            FileCollection files = project.rootProject.files("config/pmd/${project.name}.xml")
            if (!files.empty) {
                files = project.rootProject.files('config/pmd/pmd.xml')
            }
            ruleSetFiles = files
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

    void setIncrementalAnalysis(boolean incrementalAnalysis) {
        this.incrementalAnalysis = incrementalAnalysis
        this.incrementalAnalysisSet = true
    }

    boolean isIncrementalAnalysisSet() {
        this.incrementalAnalysisSet
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

    void aggregate(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Aggregate) Closure action) {
        ConfigureUtil.configure(action, aggregate)
    }

    void copyInto(Pmd copy) {
        super.copyInto(copy)
        copy.ruleSetFiles = ruleSetFiles
        copy.@incrementalAnalysis = incrementalAnalysis
        copy.@incrementalAnalysisSet = incrementalAnalysisSet
        copy.@ignoreFailures = ignoreFailures
        copy.@ignoreFailuresSet = ignoreFailuresSet
        copy.rulePriority = rulePriority
        copy.toolVersion = toolVersion
        aggregate.copyInto(copy.aggregate)
    }

    static void merge(Pmd o1, Pmd o2) {
        AbstractFeature.merge(o1, o2)
        o1.setIncrementalAnalysis((boolean) (o1.incrementalAnalysisSet ? o1.incrementalAnalysis : o2.incrementalAnalysis))
        o1.setIgnoreFailures((boolean) (o1.ignoreFailuresSet ? o1.ignoreFailures : o2.ignoreFailures))
        o1.ruleSetFiles = o1.ruleSetFiles ?: o2.ruleSetFiles
        o1.rulePriority = o1.rulePriority ?: o2.rulePriority
        o1.toolVersion = o1.toolVersion ?: o2.toolVersion
        o1.aggregate.merge(o2.aggregate)
    }

    @CompileDynamic
    void applyTo(org.gradle.api.plugins.quality.Pmd pmdTask) {
        String sourceSetName = (pmdTask.name - 'pmd').uncapitalize()
        sourceSetName = sourceSetName == 'allPmd' ? project.name : sourceSetName
        sourceSetName = sourceSetName == 'aggregatePmd' ? 'aggregate' : sourceSetName
        pmdTask.enabled = enabled && !ruleSetFiles.empty && ruleSetFiles.files.every { it.exists() }
        pmdTask.targetJdk = TargetJdk.VERSION_1_7
        pmdTask.ruleSetFiles = ruleSetFiles
        pmdTask.ignoreFailures = ignoreFailures
        pmdTask.getIncrementalAnalysis().set(incrementalAnalysisSet)
        pmdTask.rulePriority = rulePriority
        pmdTask.reports.html.enabled = true
        pmdTask.reports.xml.enabled = true
        pmdTask.reports.html.destination = project.layout.buildDirectory.file("reports/pmd/${sourceSetName}.html").get().asFile
        pmdTask.reports.xml.destination = project.layout.buildDirectory.file("reports/pmd/${sourceSetName}.xml").get().asFile
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
