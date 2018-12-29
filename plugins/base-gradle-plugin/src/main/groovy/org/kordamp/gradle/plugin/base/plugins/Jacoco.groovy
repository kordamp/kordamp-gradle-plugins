/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import groovy.transform.EqualsAndHashCode
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoReport

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
@EqualsAndHashCode(excludes = ['project', 'projects', 'testTasks', 'reportTasks', 'additionalSourceDirs', 'additionalClassDirs'])
class Jacoco extends AbstractFeature {
    File mergeExecFile
    File mergeReportHtmlFile
    File mergeReportXmlFile

    private final Set<Project> projects = new LinkedHashSet<>()
    private final Set<Test> testTasks = new LinkedHashSet<>()
    private final Set<JacocoReport> reportTasks = new LinkedHashSet<>()

    final ConfigurableFileCollection additionalSourceDirs
    final ConfigurableFileCollection additionalClassDirs

    Jacoco(Project project) {
        super(project)
        File destinationDir = project.file("${project.buildDir}/reports/jacoco/root")
        mergeExecFile = project.file("${project.buildDir}/jacoco/root.exec")
        mergeReportHtmlFile = project.file("${destinationDir}/html")
        mergeReportXmlFile = project.file("${destinationDir}/jacocoTestReport.xml")
        additionalSourceDirs = project.files()
        additionalClassDirs = project.files()
    }

    @Override
    String toString() {
        isRoot() ? toMap().toString() : ''
    }

    @Override
    @CompileDynamic
    Map<String, Map<String, Object>> toMap() {
        Map map = [enabled: enabled]

        if (enabled) {
            if (isRoot()) {
                map.mergeExecFile = mergeExecFile
                map.mergeReportHtmlFile = mergeReportHtmlFile
                map.mergeReportXmlFile = mergeReportXmlFile
            } else {
                map.additionalSourceDirs = additionalSourceDirs.files.absolutePath
                map.additionalClassDirs = additionalClassDirs.files.absolutePath
            }
        }

        ['jacoco': map]
    }

    void copyInto(Jacoco copy) {
        super.copyInto(copy)
        copy.mergeExecFile = mergeExecFile
        copy.mergeReportHtmlFile = mergeReportHtmlFile
        copy.mergeReportXmlFile = mergeReportXmlFile
        copy.additionalSourceDirs.from(project.files(additionalSourceDirs))
        copy.additionalClassDirs.from(project.files(additionalClassDirs))
    }

    static void merge(Jacoco o1, Jacoco o2) {
        AbstractFeature.merge(o1, o2)
        o1.mergeExecFile = o1.mergeExecFile ?: o2.mergeExecFile
        o1.mergeReportHtmlFile = o1.mergeReportHtmlFile ?: o2.mergeReportHtmlFile
        o1.mergeReportXmlFile = o1.mergeReportXmlFile ?: o2.mergeReportXmlFile
        o1.projects().addAll(o2.projects())
        o1.testTasks().addAll(o2.testTasks())
        o1.reportTasks().addAll(o2.reportTasks())
        o1.additionalSourceDirs.from(o2.additionalSourceDirs)
        o1.additionalClassDirs.from(o2.additionalClassDirs)
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
