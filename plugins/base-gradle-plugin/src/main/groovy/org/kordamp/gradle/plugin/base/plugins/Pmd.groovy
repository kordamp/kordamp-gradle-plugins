/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2025 Andres Almiray.
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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.quality.TargetJdk
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.internal.DefaultVersions

/**
 * @author Andres Almiray
 * @since 0.31.0
 */
@CompileStatic
class Pmd extends AbstractQualityFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.pmd'

    FileCollection ruleSetFiles
    boolean incrementalAnalysis
    int rulePriority = 5

    private boolean incrementalAnalysisSet
    private boolean ruleSetFilesSet

    Pmd(ProjectConfigurationExtension config, Project project) {
        super(config, project, PLUGIN_ID, 'pmd')
        toolVersion = DefaultVersions.INSTANCE.pmdVersion
    }

    @Override
    protected AbstractFeature getParentFeature() {
        return project.rootProject.extensions.getByType(ProjectConfigurationExtension).quality.pmd
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
            this.@ruleSetFiles = files
        }

        super.normalize()
    }

    void setRuleSetFiles(FileCollection ruleSetFiles) {
        this.ruleSetFiles = ruleSetFiles
        this.ruleSetFilesSet = true
    }

    boolean isRuleSetFilesSet() {
        return this.ruleSetFilesSet
    }

    void setIncrementalAnalysis(boolean incrementalAnalysis) {
        this.incrementalAnalysis = incrementalAnalysis
        this.incrementalAnalysisSet = true
    }

    boolean isIncrementalAnalysisSet() {
        this.incrementalAnalysisSet
    }

    static void merge(Pmd o1, Pmd o2) {
        AbstractQualityFeature.merge(o1, o2)
        o1.setIncrementalAnalysis((boolean) (o1.incrementalAnalysisSet ? o1.incrementalAnalysis : o2.incrementalAnalysis))
        o1.rulePriority = o1.rulePriority ?: o2.rulePriority
        if (!o1.ruleSetFilesSet) {
            if (o2.ruleSetFilesSet) o1.ruleSetFiles = o2.ruleSetFiles
        }
    }

    @CompileDynamic
    void applyTo(org.gradle.api.plugins.quality.Pmd pmdTask) {
        String sourceSetName = (pmdTask.name - 'pmd').uncapitalize()
        sourceSetName = sourceSetName == 'allPmd' ? project.name : sourceSetName
        sourceSetName = sourceSetName == 'aggregatePmd' ? 'aggregate' : sourceSetName
        FileCollection specificRuleSetFiles = resolveRuleSetFiles(ruleSetFiles, ruleSetFilesSet, sourceSetName)
        pmdTask.enabled = enabled && !specificRuleSetFiles.empty && specificRuleSetFiles.files.every { it.exists() }
        pmdTask.targetJdk = TargetJdk.VERSION_1_7
        pmdTask.ruleSetFiles = specificRuleSetFiles
        pmdTask.ignoreFailures = getIgnoreFailures()
        pmdTask.incrementalAnalysis.set(incrementalAnalysis)
        pmdTask.rulesMinimumPriority.set(rulePriority)
        pmdTask.reports.html.required.set(true)
        pmdTask.reports.xml.required.set(true)
        pmdTask.reports.html.outputLocation.set(project.layout.buildDirectory.file("reports/pmd/${sourceSetName}.html").get().asFile)
        pmdTask.reports.xml.outputLocation.set(project.layout.buildDirectory.file("reports/pmd/${sourceSetName}.xml").get().asFile)
    }

    private FileCollection resolveRuleSetFiles(FileCollection baseFiles, boolean fileSet, String sourceSetName) {
        if (sourceSetName == project.name || sourceSetName == 'aggregate') {
            return baseFiles
        }

        if (fileSet) {
            List<File> files = []
            for (File file : baseFiles.files) {
                String filePath = file.absolutePath[0..-5]
                File configFile = new File("${filePath}-${sourceSetName}.xml")
                if (configFile.exists()) {
                    files.add(configFile)
                }
            }
            files.addAll(baseFiles.files)
            return project.files(files)
        }

        List<File> files = []
        for (String path : [
            "config/pmd/${project.name}-${sourceSetName}.xml",
            "config/pmd/${project.name}.xml",
            "config/pmd/pmd-${sourceSetName}.xml",
            "config/pmd/pmd.xml"]) {
            File file = project.rootProject.file(path)
            if (file.exists()) {
                files << file
            }
        }

        files ? project.files(files) : baseFiles
    }
}
