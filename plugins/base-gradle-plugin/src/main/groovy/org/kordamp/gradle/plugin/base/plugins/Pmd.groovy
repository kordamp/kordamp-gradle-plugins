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
class Pmd extends AbstractQualityFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.pmd'

    FileCollection ruleSetFiles
    boolean incrementalAnalysis
    int rulePriority = 5

    private boolean incrementalAnalysisSet

    Pmd(ProjectConfigurationExtension config, Project project) {
        super(config, project, PLUGIN_ID, 'pmd')
        toolVersion = '6.23.0'
    }

    @Override
    protected void populateMapDescription(Map<String, Object> map) {
        super.populateMapDescription(map)
        map.ruleSetFiles = this.ruleSetFiles
        map.incrementalAnalysis = this.incrementalAnalysis
        map.rulePriority = this.rulePriority
    }

    @Override
    void normalize() {
        if (null == ruleSetFiles) {
            FileCollection files = project.rootProject.files("config/pmd/${project.name}.xml")
            if (!files.empty) {
                files = project.rootProject.files('config/pmd/pmd.xml')
            }
            ruleSetFiles = files
        }

        super.normalize()
    }

    void setIncrementalAnalysis(boolean incrementalAnalysis) {
        this.incrementalAnalysis = incrementalAnalysis
        this.incrementalAnalysisSet = true
    }

    boolean isIncrementalAnalysisSet() {
        this.incrementalAnalysisSet
    }

    void copyInto(Pmd copy) {
        super.copyInto(copy)
        copy.ruleSetFiles = ruleSetFiles
        copy.@incrementalAnalysis = incrementalAnalysis
        copy.@incrementalAnalysisSet = incrementalAnalysisSet
        copy.rulePriority = rulePriority
    }

    static void merge(Pmd o1, Pmd o2) {
        AbstractQualityFeature.merge(o1, o2)
        o1.setIncrementalAnalysis((boolean) (o1.incrementalAnalysisSet ? o1.incrementalAnalysis : o2.incrementalAnalysis))
        o1.ruleSetFiles = o1.ruleSetFiles ?: o2.ruleSetFiles
        o1.rulePriority = o1.rulePriority ?: o2.rulePriority
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
