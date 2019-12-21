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
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
@EqualsAndHashCode(excludes = ['additionalSourceDirs', 'additionalClassDirs'])
class Jacoco extends AbstractFeature {
    File aggregateExecFile
    File aggregateReportHtmlFile
    File aggregateReportXmlFile
    String toolVersion = '0.8.5'

    private final Set<Project> projects = new LinkedHashSet<>()
    private final Set<Test> testTasks = new LinkedHashSet<>()
    private final Set<JacocoReport> reportTasks = new LinkedHashSet<>()

    final ConfigurableFileCollection additionalSourceDirs
    final ConfigurableFileCollection additionalClassDirs

    Jacoco(ProjectConfigurationExtension config, Project project) {
        super(config, project)
        File destinationDir = project.layout.buildDirectory.file('reports/jacoco/aggregate').get().asFile
        aggregateExecFile = project.layout.buildDirectory.file('jacoco/aggregate.exec').get().asFile
        aggregateReportHtmlFile = project.file("${destinationDir}/html")
        aggregateReportXmlFile = project.file("${destinationDir}/jacocoTestReport.xml")
        additionalSourceDirs = project.files()
        additionalClassDirs = project.files()
    }

    @Deprecated
    File getMergeExecFile() {
        println("Property jacoco.mergeExecFile is deprecated and will be removed in the future. Use jacoco.aggregateExecFile instead")
        aggregateExecFile
    }

    @Deprecated
    File getMergeReportHtmlFile() {
        println("Property jacoco.mergeReportHtmlFile is deprecated and will be removed in the future. Use jacoco.aggregateReportHtmlFile instead")
        aggregateReportHtmlFile
    }

    @Deprecated
    File getMergeReportXmlFile() {
        println("Property jacoco.mergeReportXmlFile is deprecated and will be removed in the future. Use jacoco.aggregateReportXmlFile instead")
        aggregateReportXmlFile
    }

    @Deprecated
    void setMergeExecFile(File f) {
        println("Property jacoco.mergeExecFile is deprecated and will be removed in the future. Use jacoco.aggregateExecFile instead")
        aggregateExecFile = f
    }

    @Deprecated
    void setMergeReportHtmlFile(File f) {
        println("Property jacoco.mergeReportHtmlFile is deprecated and will be removed in the future. Use jacoco.aggregateReportHtmlFile instead")
        aggregateReportHtmlFile = f
    }

    @Deprecated
    void setMergeReportXmlFile(File f) {
        println("Property jacoco.mergeReportXmlFile is deprecated and will be removed in the future. Use jacoco.aggregateReportXmlFile instead")
        aggregateReportXmlFile = f
    }

    @Override
    String toString() {
        isRoot() ? toMap().toString() : ''
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        if (isRoot()) {
            map.aggregateExecFile = aggregateExecFile
            map.aggregateReportHtmlFile = aggregateReportHtmlFile
            map.aggregateReportXmlFile = aggregateReportXmlFile
        } else {
            map.additionalSourceDirs = additionalSourceDirs.files*.absolutePath
            map.additionalClassDirs = additionalClassDirs.files*.absolutePath
        }
        map.toolVersion = toolVersion

        new LinkedHashMap<>('jacoco': map)
    }

    void normalize() {
        if (!enabledSet) {
            if (isRoot()) {
                if (project.childProjects.isEmpty()) {
                    enabled = hasTestSourceSets()
                }
            } else {
                enabled = hasTestSourceSets()
            }
        }
    }

    boolean hasTestSourceSets() {
        hasTestsAt(project.file('src/test')) ||
            hasTestsAt(project.file('src/integration-test')) ||
            hasTestsAt(project.file('src/functional-test'))
    }

    private static boolean hasTestsAt(File testDir) {
        testDir.exists() && testDir.listFiles().length
    }

    void copyInto(Jacoco copy) {
        super.copyInto(copy)
        copy.aggregateExecFile = aggregateExecFile
        copy.aggregateReportHtmlFile = aggregateReportHtmlFile
        copy.aggregateReportXmlFile = aggregateReportXmlFile
        copy.additionalSourceDirs.from(project.files(additionalSourceDirs))
        copy.additionalClassDirs.from(project.files(additionalClassDirs))
        copy.toolVersion = toolVersion
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
