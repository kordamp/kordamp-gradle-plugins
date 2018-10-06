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
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.model.impl.GroovydocOptions

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
@CompileStatic
@Canonical
@EqualsAndHashCode(excludes = ['projects', 'groovydocTasks', 'groovydocJarTasks'])
@ToString(includeNames = true, excludes = ['projects', 'groovydocTasks', 'groovydocJarTasks'])
class Groovydoc {
    boolean enabled = true
    Set<String> excludes = new LinkedHashSet<>()
    Set<String> includes = new LinkedHashSet<>()
    final GroovydocOptions options = new GroovydocOptions()

    private final Map<String, Project> projects = [:]
    private final Map<String, Task> groovydocTasks = [:]
    private final Map<String, Task> groovydocJarTasks = [:]

    private boolean enabledSet

    Groovydoc(Project project) {
        options.use            = true
        options.windowTitle    = "${project.name} ${project.version}"
        options.docTitle       = "${project.name} ${project.version}"
        options.header         = "${project.name} ${project.version}"
        options.includePrivate = false
        options.link 'http://docs.oracle.com/javase/8/docs/api/', 'java.', 'org.xml.', 'javax.', 'org.w3c.'
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled
        this.enabledSet = true
    }

    boolean isEnabledSet() {
        this.enabledSet
    }

    void options(Action<? super GroovydocOptions> action) {
        action.execute(options)
    }

    void options(@DelegatesTo(GroovydocOptions) Closure action) {
        ConfigureUtil.configure(action, options)
    }

    void copyInto(Groovydoc copy) {
        copy.@enabled = enabled
        copy.@enabledSet = enabledSet
        copy.excludes.addAll(excludes)
        copy.includes.addAll(includes)
        options.copyInto(copy.options)
    }

    @CompileDynamic
    void merge(Groovydoc o1, Groovydoc o2) {
        setEnabled(o1?.enabledSet ? o1.enabled : o2?.enabled)
        excludes.addAll(((o1?.excludes ?: []) + (o2?.excludes ?: [])).unique())
        includes.addAll(((o1?.includes ?: []) + (o2?.includes ?: [])).unique())
        options.merge(o1?.options, o2?.options)
    }

    Map<String, Project> projects() {
        projects
    }

    Map<String, Task> groovydocTasks() {
        groovydocTasks
    }

    Map<String, Task> groovydocJarTasks() {
        groovydocJarTasks
    }
}
