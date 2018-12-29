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
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.Task

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
class SourceXref extends AbstractFeature {
    String templateDir
    String inputEncoding
    String outputEncoding
    String windowTitle
    String docTitle
    String bottom
    String stylesheet
    JavaVersion javaVersion
    Set<String> excludes = new LinkedHashSet<>()
    Set<String> includes = new LinkedHashSet<>()

    private final Set<Project> projects = new LinkedHashSet<>()
    private final Set<Task> xrefTasks = new LinkedHashSet<>()

    SourceXref(Project project) {
        super(project)
        windowTitle = "${project.name} ${project.version}"
        docTitle    = "${project.name} ${project.version}"
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    @CompileDynamic
    Map<String, Map<String, Object>> toMap() {
        Map map = [enabled: enabled]

        if (enabled) {
            map.templateDir = templateDir
            map.inputEncoding = getInputEncoding()
            map.outputEncoding = getOutputEncoding()
            map.windowTitle = windowTitle
            map.docTitle = docTitle
            map.bottom = bottom
            map.stylesheet = stylesheet
            map.javaVersion = javaVersion?.name() ?: JavaVersion.current().name()
            map.excludes = excludes
            map.includes = includes
        }

        ['sourceXref': map]
    }

    String getOutputEncoding() {
        outputEncoding ?: 'UTF-8'
    }

    String getInputEncoding() {
        !isBlank(inputEncoding) ? inputEncoding : System.getProperty('file.encoding')
    }

    void setJavaVersion(String javaVersion) {
        this.javaVersion = JavaVersion.toVersion(javaVersion)
    }

    void include(String str) {
        includes << str
    }

    void exclude(String str) {
        excludes << str
    }

    void copyInto(SourceXref copy) {
        super.copyInto(copy)
        copy.templateDir = templateDir
        copy.inputEncoding = inputEncoding
        copy.outputEncoding = outputEncoding
        copy.windowTitle = windowTitle
        copy.docTitle = docTitle
        copy.bottom = bottom
        copy.stylesheet = stylesheet
        copy.javaVersion = javaVersion
        copy.excludes.addAll(excludes)
        copy.includes.addAll(includes)
    }

    @CompileDynamic
    static void merge(SourceXref o1, SourceXref o2) {
        AbstractFeature.merge(o1, o2)
        o1.templateDir = o1.templateDir ?: o2?.templateDir
        o1.inputEncoding = o1.@inputEncoding ?: o2?.@inputEncoding
        o1.outputEncoding = o1.@outputEncoding ?: o2?.@outputEncoding
        o1.windowTitle = o1.windowTitle ?: o2?.windowTitle
        o1.docTitle = o1.docTitle ?: o2?.docTitle
        o1.bottom = o1.bottom ?: o2?.bottom
        o1.stylesheet = o1.stylesheet ?: o2?.stylesheet
        o1.javaVersion = o1.javaVersion ?: o2?.javaVersion
        o1.excludes.addAll(((o1.excludes ?: []) + (o2?.excludes ?: [])).unique())
        o1.includes.addAll(((o1.includes ?: []) + (o2?.includes ?: [])).unique())
        o1.projects().addAll(o2.projects())
        o1.xrefTasks().addAll(o2.xrefTasks())
    }

    Set<Project> projects() {
        projects
    }

    Set<Task> xrefTasks() {
        xrefTasks
    }
}
