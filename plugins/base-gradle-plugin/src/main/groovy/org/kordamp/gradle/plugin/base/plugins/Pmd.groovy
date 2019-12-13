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
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.quality.TargetJdk
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

/**
 * @author Andres Almiray
 * @since 0.31.0
 */
@CompileStatic
@Canonical
class Pmd extends AbstractFeature {
    FileCollection ruleSetFiles
    boolean incrementalAnalysis
    boolean ignoreFailures = true
    int rulePriority = 5
    String toolVersion = '6.2.0'

    private boolean incrementalAnalysisSet
    private boolean ignoreFailuresSet

    Pmd(ProjectConfigurationExtension config, Project project) {
        super(config, project)
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        new LinkedHashMap<>('pmd': new LinkedHashMap<String, Object>(
            enabled: enabled,
            ruleSetFiles: ruleSetFiles,
            incrementalAnalysis: incrementalAnalysis,
            ignoreFailures: ignoreFailures,
            rulePriority: rulePriority,
            toolVersion: toolVersion,
        ))
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
                    enabled = project.pluginManager.hasPlugin('java')
                } else {
                    enabled = project.childProjects.values().any { p -> p.pluginManager.hasPlugin('java') }
                }
            } else {
                enabled = project.pluginManager.hasPlugin('java')
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

    void copyInto(Pmd copy) {
        super.copyInto(copy)
        copy.ruleSetFiles = ruleSetFiles
        copy.@incrementalAnalysis = incrementalAnalysis
        copy.@incrementalAnalysisSet = incrementalAnalysisSet
        copy.@ignoreFailures = ignoreFailures
        copy.@ignoreFailuresSet = ignoreFailuresSet
        copy.rulePriority = rulePriority
        copy.toolVersion = toolVersion
    }

    static void merge(Pmd o1, Pmd o2) {
        AbstractFeature.merge(o1, o2)
        o1.setIncrementalAnalysis((boolean) (o1.incrementalAnalysisSet ? o1.incrementalAnalysis : o2.incrementalAnalysis))
        o1.setIgnoreFailures((boolean) (o1.ignoreFailuresSet ? o1.ignoreFailures : o2.ignoreFailures))
        o1.ruleSetFiles = o1.ruleSetFiles ?: o2.ruleSetFiles
        o1.rulePriority = o1.rulePriority ?: o2.rulePriority
        o1.toolVersion = o1.toolVersion ?: o2.toolVersion
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
}
