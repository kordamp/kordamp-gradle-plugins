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
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoReport

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
@Canonical
@EqualsAndHashCode(excludes = ['project', 'projects', 'testTasks', 'reportTasks'])
@ToString(includeNames = true, excludes = ['project', 'projects', 'testTasks', 'reportTasks'])
class Jacoco {
    boolean enabled = true
    File mergeExecFile
    File mergeReportHtmlFile
    File mergeReportXmlFile

    private final Set<Project> projects = new LinkedHashSet<>()
    private final Set<Test> testTasks = new LinkedHashSet<>()
    private final Set<JacocoReport> reportTasks = new LinkedHashSet<>()

    private boolean enabledSet

    Jacoco(Project project) {
        File destinationDir = project.file("${project.buildDir}/reports/jacoco/root")
        mergeExecFile = project.file("${project.buildDir}/jacoco/root.exec")
        mergeReportHtmlFile = project.file("${destinationDir}/html")
        mergeReportXmlFile = project.file("${destinationDir}/jacocoTestReport.xml")
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled
        this.enabledSet = true
    }

    boolean isEnabledSet() {
        this.enabledSet
    }

    void copyInto(Jacoco copy) {
        copy.@enabled = enabled
        copy.@enabledSet = enabledSet
        copy.mergeExecFile = mergeExecFile
        copy.mergeReportHtmlFile = mergeReportHtmlFile
        copy.mergeReportXmlFile = mergeReportXmlFile
    }

    static void merge(Jacoco o1, Jacoco o2) {
        o1.setEnabled((boolean) (o1.enabledSet ? o1.enabled : o2.enabled))
        o1.mergeExecFile = o1.mergeExecFile ?: o2.mergeExecFile
        o1.mergeReportHtmlFile = o1.mergeReportHtmlFile ?: o2.mergeReportHtmlFile
        o1.mergeReportXmlFile = o1.mergeReportXmlFile ?: o2.mergeReportXmlFile
        o1.projects().addAll(o2.projects())
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
