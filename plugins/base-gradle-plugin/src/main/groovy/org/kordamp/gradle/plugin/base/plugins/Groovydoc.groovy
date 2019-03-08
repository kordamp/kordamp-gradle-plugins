/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.plugin.base.model.impl.GroovydocOptions

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
class Groovydoc extends AbstractFeature {
    boolean replaceJavadoc = false
    Set<String> excludes = new LinkedHashSet<>()
    Set<String> includes = new LinkedHashSet<>()
    final GroovydocOptions options = new GroovydocOptions()

    private final Set<Project> projects = new LinkedHashSet<>()
    private final Set<org.gradle.api.tasks.javadoc.Groovydoc> groovydocTasks = new LinkedHashSet<>()
    private final Set<Jar> groovydocJarTasks = new LinkedHashSet<>()

    private boolean replaceJavadocSet

    Groovydoc(Project project) {
        super(project)
        doSetEnabled(project.plugins.findPlugin('groovy') != null)

        options.use            = true
        options.windowTitle    = "${project.name} ${project.version}"
        options.docTitle       = "${project.name} ${project.version}"
        options.header         = "${project.name} ${project.version}"
        options.includePrivate = false
        options.link 'http://docs.oracle.com/javase/8/docs/api/', 'java.', 'org.xml.', 'javax.', 'org.w3c.'
        options.link 'http://docs.groovy-lang.org/2.5.2/html/api/', 'groovy.', 'org.codehaus.groovy.', 'org.apache.groovy.'
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
            List<Map<String, String>> links = []
            options.links.each { link ->
                links << [(link.url): link.packages.join(', ')]
            }

            map.replaceJavadoc = replaceJavadoc
            map.excludes = excludes
            map.includes = includes
            map.options = [
                windowTitle   : options.windowTitle,
                docTitle      : options.docTitle,
                header        : options.header,
                footer        : options.footer,
                overviewText  : options.overviewText?.asFile(),
                noTimestamp   : options.noTimestamp,
                noVersionStamp: options.noVersionStamp,
                includePrivate: options.includePrivate,
                use           : options.use,
                links         : links
            ]
        }

        ['groovydoc': map]
    }

    void normalize() {
        if (!enabledSet && isRoot()) {
            setEnabled(project.plugins.findPlugin('groovy') != null)
        }
    }

    void setReplaceJavadoc(boolean replaceJavadoc) {
        this.replaceJavadoc = replaceJavadoc
        this.replaceJavadocSet = true
    }

    boolean isReplaceJavadocSet() {
        this.replaceJavadocSet
    }

    void include(String str) {
        includes << str
    }

    void exclude(String str) {
        excludes << str
    }

    void options(Action<? super GroovydocOptions> action) {
        action.execute(options)
    }

    void options(@DelegatesTo(GroovydocOptions) Closure action) {
        ConfigureUtil.configure(action, options)
    }

    void copyInto(Groovydoc copy) {
        super.copyInto(copy)
        copy.@replaceJavadoc = replaceJavadoc
        copy.@replaceJavadocSet = replaceJavadocSet
        copy.excludes.addAll(excludes)
        copy.includes.addAll(includes)
        options.copyInto(copy.options)
    }

    @CompileDynamic
    static void merge(Groovydoc o1, Groovydoc o2) {
        AbstractFeature.merge(o1, o2)
        o1.setReplaceJavadoc((boolean) (o1.replaceJavadocSet ? o1.replaceJavadoc : o2.replaceJavadoc))
        o1.excludes.addAll(((o1.excludes ?: []) + (o2?.excludes ?: [])).unique())
        o1.includes.addAll(((o1.includes ?: []) + (o2?.includes ?: [])).unique())
        GroovydocOptions.merge(o1.options, o2.options)
        o1.projects().addAll(o2.projects())
        o1.groovydocTasks().addAll(o2.groovydocTasks())
        o1.groovydocJarTasks().addAll(o2.groovydocJarTasks())
    }

    Set<Project> projects() {
        projects
    }

    Set<org.gradle.api.tasks.javadoc.Groovydoc> groovydocTasks() {
        groovydocTasks
    }

    Set<Jar> groovydocJarTasks() {
        groovydocJarTasks
    }

    void applyTo(org.gradle.api.tasks.javadoc.Groovydoc groovydoc) {
        groovydoc.getIncludes().addAll(includes)
        groovydoc.getExcludes().addAll(excludes)
        options.applyTo(groovydoc)
    }
}
