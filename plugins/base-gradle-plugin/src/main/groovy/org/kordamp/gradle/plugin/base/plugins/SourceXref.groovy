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
import groovy.transform.ToString
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * @author Andres Almiray
 * @since 0.7.0
 */
@CompileStatic
@Canonical
@EqualsAndHashCode(excludes = ['projects', 'xrefTasks'])
@ToString(includeNames = true, excludes = ['projects ', ' xrefTasks '])
class SourceXref {
    boolean enabled = true
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

    private boolean enabledSet

    void setEnabled(boolean enabled) {
        this.enabled = enabled
        this.enabledSet = true
    }

    boolean isEnabledSet() {
        this.enabledSet
    }

    void setJavaVersion(String javaVersion) {
        this.javaVersion = JavaVersion.toVersion(javaVersion)
    }

    void copyInto(SourceXref copy) {
        copy.@enabled = enabled
        copy.@enabledSet = enabledSet
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
        o1.setEnabled((boolean) (o1.enabledSet ? o1.enabled : o2.enabled))
        o1.templateDir = o1.templateDir ?: o2?.templateDir
        o1.inputEncoding = o1.inputEncoding ?: o2?.inputEncoding
        o1.outputEncoding = o1.outputEncoding ?: o2?.outputEncoding
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
