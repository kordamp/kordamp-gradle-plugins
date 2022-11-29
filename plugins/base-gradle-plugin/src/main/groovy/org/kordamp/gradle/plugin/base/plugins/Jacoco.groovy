/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2022 Andres Almiray.
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

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.internal.DefaultVersions

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
class Jacoco extends AbstractTestingFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.jacoco'

    File aggregateExecFile
    File aggregateReportHtmlFile
    File aggregateReportXmlFile
    String toolVersion = DefaultVersions.INSTANCE.jacocoVersion
    Set<String> excludes = new LinkedHashSet<>()

    Boolean includeProjectDependencies

    private final Set<Project> projects = new LinkedHashSet<>()
    private final Set<Test> testTasks = new LinkedHashSet<>()
    private final Set<JacocoReport> reportTasks = new LinkedHashSet<>()

    final ConfigurableFileCollection additionalSourceDirs
    final ConfigurableFileCollection additionalClassDirs

    Jacoco(ProjectConfigurationExtension config, Project project) {
        super(config, project, PLUGIN_ID)
        File destinationDir = project.layout.buildDirectory.file('reports/jacoco/aggregate').get().asFile
        aggregateExecFile = project.layout.buildDirectory.file('jacoco/aggregate.exec').get().asFile
        aggregateReportHtmlFile = project.file("${destinationDir}/html")
        aggregateReportXmlFile = project.file("${destinationDir}/jacocoTestReport.xml")
        additionalSourceDirs = project.objects.fileCollection()
        additionalClassDirs = project.objects.fileCollection()
    }

    @Override
    protected AbstractFeature getParentFeature() {
        return project.rootProject.extensions.getByType(ProjectConfigurationExtension).coverage.jacoco
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        if (isRoot()) {
            map.aggregateExecFile = aggregateExecFile
            map.aggregateReportHtmlFile = aggregateReportHtmlFile
            map.aggregateReportXmlFile = aggregateReportXmlFile
        } else {
            if (!additionalSourceDirs.empty) map.additionalSourceDirs = additionalSourceDirs.files*.absolutePath
            if (!additionalClassDirs.empty) map.additionalClassDirs = additionalClassDirs.files*.absolutePath
        }
        map.toolVersion = toolVersion
        map.excludes = excludes
        map.includeProjectDependencies = getIncludeProjectDependencies()

        new LinkedHashMap<>('jacoco': map)
    }

    void exclude(String str) {
        excludes << str
    }

    boolean getIncludeProjectDependencies() {
        this.@includeProjectDependencies != null && this.@includeProjectDependencies
    }

    static void merge(Jacoco o1, Jacoco o2) {
        AbstractFeature.merge(o1, o2)
        o1.aggregateExecFile = o1.aggregateExecFile ?: o2.aggregateExecFile
        o1.aggregateReportHtmlFile = o1.aggregateReportHtmlFile ?: o2.aggregateReportHtmlFile
        o1.aggregateReportXmlFile = o1.aggregateReportXmlFile ?: o2.aggregateReportXmlFile
        o1.projects().addAll(o2.projects())
        o1.testTasks().addAll(o2.testTasks())
        o1.reportTasks().addAll(o2.reportTasks())
        o1.additionalSourceDirs.from(o2.additionalSourceDirs)
        o1.additionalClassDirs.from(o2.additionalClassDirs)
        o1.toolVersion = o1.toolVersion ?: o2.toolVersion
        o1.includeProjectDependencies = o1.@includeProjectDependencies != null ? o1.getIncludeProjectDependencies() : o2.getIncludeProjectDependencies()
    }

    Set<Project> projects() {
        projects
    }

    Set<Test> testTasks() {
        testTasks
    }

    Set<JacocoReport> reportTasks() {
        reportTasks
    }
}
