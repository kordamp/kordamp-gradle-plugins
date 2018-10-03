/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 the original author or authors.
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
package org.kordamp.gradle.model

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
@Canonical
@EqualsAndHashCode(excludes = ['projects'])
@ToString(includeNames = true, excludes = ['projects'])
class Jacoco {
    boolean skip = false
    File mergeExecFile
    File mergeReportHtmlFile
    File mergeReportXmlFile

    private final List<Project> projects = []
    private final List<Task> testTasks = []
    private final List<Task> reportTasks = []

    Jacoco(Project project) {
        File destinationDir = project.file("${project.buildDir}/reports/jacoco/root")
        mergeExecFile = project.file("${project.buildDir}/jacoco/root.exec")
        mergeReportHtmlFile = project.file("${destinationDir}/html")
        mergeReportXmlFile = project.file("${destinationDir}/jacocoTestReport.xml")
    }

    void copyInto(Jacoco copy) {
        copy.skip = skip
        copy.mergeExecFile = mergeExecFile
        copy.mergeReportHtmlFile = mergeReportHtmlFile
        copy.mergeReportXmlFile = mergeReportXmlFile
    }

    void merge(Jacoco o1, Jacoco o2) {
        skip = o1?.skip ?: o2?.skip
        mergeExecFile = o1?.mergeExecFile ?: o2?.mergeExecFile
        mergeReportHtmlFile = o1?.mergeReportHtmlFile ?: o2?.mergeReportHtmlFile
        mergeReportXmlFile = o1?.mergeReportXmlFile ?: o2?.mergeReportXmlFile
    }

    List<Project> projects() {
        projects
    }

    List<Task> testTasks() {
        testTasks
    }

    List<Task> reportTasks() {
        reportTasks
    }
}
