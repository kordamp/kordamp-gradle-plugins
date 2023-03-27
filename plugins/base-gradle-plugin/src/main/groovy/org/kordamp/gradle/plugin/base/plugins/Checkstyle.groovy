/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2023 Andres Almiray.
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
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.internal.DefaultVersions
import org.kordamp.gradle.util.CollectionUtils

/**
 * @author Andres Almiray
 * @since 0.31.0
 */
@CompileStatic
class Checkstyle extends AbstractQualityFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.checkstyle'

    File configFile
    Map<String, Object> configProperties = [:]
    int maxErrors
    int maxWarnings = Integer.MAX_VALUE
    boolean showViolations = true
    Set<String> excludes = new LinkedHashSet<>()
    Set<String> includes = new LinkedHashSet<>()

    private boolean showViolationsSet
    private boolean configFileSet

    Checkstyle(ProjectConfigurationExtension config, Project project) {
        super(config, project, PLUGIN_ID, 'checkstyle')
        toolVersion = DefaultVersions.INSTANCE.checkstyleVersion
    }

    @Override
    protected AbstractFeature getParentFeature() {
        return project.rootProject.extensions.getByType(ProjectConfigurationExtension).quality.checkstyle
    }

    @Override
    protected void populateMapDescription(Map<String, Object> map) {
        super.populateMapDescription(map)
        map.configFile = this.configFile
        map.configProperties = this.configProperties
        map.maxErrors = this.maxErrors
        map.maxWarnings = this.maxWarnings
        map.excludes = this.excludes
        map.includes = this.includes
        map.showViolations = this.showViolations
    }

    @Override
    void normalize() {
        if (null == configFile) {
            File file = project.rootProject.file("config/checkstyle/${project.name}.xml")
            if (!file.exists()) {
                file = project.rootProject.file('config/checkstyle/checkstyle.xml')
            }
            this.@configFile = file
        }

        super.normalize()
    }

    void setConfigFile(File configFile) {
        this.configFile = configFile
        this.configFileSet = true
    }

    boolean isConfigFileSet() {
        return this.configFileSet
    }

    void setShowViolations(boolean showViolations) {
        this.showViolations = showViolations
        this.showViolationsSet = true
    }

    boolean isShowViolationsSet() {
        this.showViolationsSet
    }

    void include(String str) {
        includes << str
    }

    void exclude(String str) {
        excludes << str
    }

    static void merge(Checkstyle o1, Checkstyle o2) {
        AbstractQualityFeature.merge(o1, o2)
        if (!o1.configFileSet) {
            if (o2.configFileSet) o1.configFile = o2.configFile
        }
        o1.setShowViolations((boolean) (o1.showViolationsSet ? o1.showViolations : o2.showViolations))
        o1.excludes = CollectionUtils.merge(o1.excludes, o2.excludes, false)
        o1.includes = CollectionUtils.merge(o1.includes, o2.includes, false)
        o1.maxErrors = o1.maxErrors ?: o2.maxErrors
        o1.maxWarnings = o1.maxWarnings ?: o2.maxWarnings
        o1.configProperties = CollectionUtils.merge(o1.configProperties, o2?.configProperties, false)
    }

    @CompileDynamic
    void applyTo(org.gradle.api.plugins.quality.Checkstyle checkstyleTask) {
        String sourceSetName = (checkstyleTask.name - 'checkstyle').uncapitalize()
        sourceSetName = sourceSetName == 'allCheckstyle' ? project.name : sourceSetName
        sourceSetName = sourceSetName == 'aggregateCheckstyle' ? 'aggregate' : sourceSetName
        File specificConfigFile = resolveConfigFile(configFile, configFileSet, sourceSetName)
        checkstyleTask.enabled = enabled && specificConfigFile.exists()
        checkstyleTask.includes.addAll(includes)
        checkstyleTask.excludes.addAll(excludes)
        checkstyleTask.configFile = specificConfigFile
        checkstyleTask.maxErrors = maxErrors
        checkstyleTask.maxWarnings = maxWarnings
        checkstyleTask.showViolations = showViolations
        checkstyleTask.ignoreFailures = getIgnoreFailures()
        checkstyleTask.reports.html.required.set(true)
        checkstyleTask.reports.xml.required.set(true)
        checkstyleTask.reports.html.outputLocation.set(project.layout.buildDirectory.file("reports/checkstyle/${sourceSetName}.html").get().asFile)
        checkstyleTask.reports.xml.outputLocation.set(project.layout.buildDirectory.file("reports/checkstyle/${sourceSetName}.xml").get().asFile)
    }

    private File resolveConfigFile(File baseFile, boolean fileSet, String sourceSetName) {
        if (sourceSetName == project.name || sourceSetName == 'aggregate') {
            return baseFile
        }

        if (fileSet) {
            if (baseFile.name.endsWith('.xml')) {
                String filePath = baseFile.absolutePath[0..-5]
                File configFile = new File("${filePath}-${sourceSetName}.xml")
                if (configFile.exists()) {
                    return configFile
                }
            }
            return baseFile
        }

        for (String path : [
            "config/checkstyle/${project.name}-${sourceSetName}.xml",
            "config/checkstyle/${project.name}.xml",
            "config/checkstyle/checkstyle-${sourceSetName}.xml",
            "config/checkstyle/checkstyle.xml"]) {
            File file = project.rootProject.file(path)
            if (file.exists()) {
                return file
            }
        }

        baseFile
    }
}
