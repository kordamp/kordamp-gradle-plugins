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
package org.kordamp.gradle.plugins

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.model.impl.ExtStandardJavadocDocletOptions

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
@CompileStatic
@Canonical
@EqualsAndHashCode(excludes = ['project', 'projects', 'javadocTasks', 'javadocJarTasks'])
@ToString(includeNames = true, excludes = ['project', 'projects', 'javadocTasks', 'javadocJarTasks'])
class Javadoc {
    boolean enabled = true
    Set<String> excludes = new LinkedHashSet<>()
    Set<String> includes = new LinkedHashSet<>()
    String title
    final ExtStandardJavadocDocletOptions options = new ExtStandardJavadocDocletOptions()

    private final Set<Project> projects = new LinkedHashSet<>()
    private final Set<org.gradle.api.tasks.javadoc.Javadoc> javadocTasks = new LinkedHashSet<>()
    private final Set<Jar> javadocJarTasks = new LinkedHashSet<>()

    private boolean enabledSet

    private final Project project

    Javadoc(Project project) {
        this.project = project
        options.use         = true
        options.splitIndex  = true
        options.encoding    = 'UTF-8'
        options.author      = true
        options.version     = true
        options.windowTitle = "${project.name} ${project.version}"
        options.docTitle    = "${project.name} ${project.version}"
        options.header      = "${project.name} ${project.version}"
        options.links 'http://docs.oracle.com/javase/8/docs/api/'
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled
        this.enabledSet = true
    }

    boolean isEnabledSet() {
        this.enabledSet
    }

    void options(Action<? super ExtStandardJavadocDocletOptions> action) {
        action.execute(options)
    }

    void options(@DelegatesTo(ExtStandardJavadocDocletOptions) Closure action) {
        ConfigureUtil.configure(action, options)
    }

    void copyInto(Javadoc copy) {
        copy.@enabled = enabled
        copy.@enabledSet = enabledSet
        copy.excludes.addAll(excludes)
        copy.includes.addAll(includes)
        copy.title = title
        options.copyInto(copy.options)
    }

    @CompileDynamic
    static void merge(Javadoc o1, Javadoc o2) {
        o1.setEnabled((boolean) (o1.enabledSet ? o1.enabled : o2.enabled))
        o1.excludes.addAll(((o1.excludes ?: []) + (o2.excludes ?: [])).unique())
        o1.includes.addAll(((o1.includes ?: []) + (o2.includes ?: [])).unique())
        o1.title = o1.title ?: o2.title
        ExtStandardJavadocDocletOptions.merge(o1.options, o2.options)
        o1.projects().addAll(o2.projects())
        o1.javadocTasks().addAll(o2.javadocTasks())
        o1.javadocJarTasks().addAll(o2.javadocJarTasks())
    }

    Set<Project> projects() {
        projects
    }

    Set<org.gradle.api.tasks.javadoc.Javadoc> javadocTasks() {
        javadocTasks
    }

    Set<Jar> javadocJarTasks() {
        javadocJarTasks
    }

    void applyTo(org.gradle.api.tasks.javadoc.Javadoc javadoc) {
        javadoc.title = title
        javadoc.getIncludes().addAll(includes)
        javadoc.getExcludes().addAll(excludes)
        options.applyTo(javadoc.options)
    }
}
