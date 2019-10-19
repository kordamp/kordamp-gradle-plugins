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
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.CollectionUtils
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
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
    private final Set<TaskProvider<Jar>> groovydocJarTasks = new LinkedHashSet<>()

    private boolean replaceJavadocSet

    Groovydoc(ProjectConfigurationExtension config, Project project) {
        super(config, project)
        doSetEnabled(project.plugins.findPlugin('groovy') != null)

        options.use            = true
        options.windowTitle    = "${project.name} ${project.version}"
        options.docTitle       = "${project.name} ${project.version}"
        options.header         = "${project.name} ${project.version}"
        options.includePrivate = false
        options.link resolveJavadocLinks(project.findProperty('targetCompatibility')), 'java.', 'javax.', 'org.xml.', 'org.w3c.'
        options.link 'http://docs.groovy-lang.org/2.5.6/html/api/', 'groovy.', 'org.codehaus.groovy.', 'org.apache.groovy.'
    }

    private String resolveJavadocLinks(Object jv) {
        JavaVersion javaVersion = JavaVersion.current()

        if (jv instanceof JavaVersion) {
            javaVersion = (JavaVersion) jv
        } else if (jv != null) {
            javaVersion = JavaVersion.toVersion(jv)
        }

        if (javaVersion.isJava11Compatible()) {
            return "https://docs.oracle.com/en/java/javase/${javaVersion.majorVersion}/docs/api/".toString()
        }
        return "https://docs.oracle.com/javase/${javaVersion.majorVersion}/docs/api/"
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        if (enabled) {
            List<Map<String, String>> links = []
            options.links.each { link ->
                links << [(link.url): link.packages.join(', ')]
            }

            map.replaceJavadoc = replaceJavadoc
            map.excludes = excludes
            map.includes = includes
            map.options = new LinkedHashMap<String, Object>([
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
            ])
        }

        new LinkedHashMap<>('groovydoc': map)
    }

    void normalize() {
        if (!enabledSet && isRoot()) {
            setEnabled(project.pluginManager.hasPlugin('groovy-base'))
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

    static void merge(Groovydoc o1, Groovydoc o2) {
        AbstractFeature.merge(o1, o2)
        o1.setReplaceJavadoc((boolean) (o1.replaceJavadocSet ? o1.replaceJavadoc : o2.replaceJavadoc))
        CollectionUtils.merge(o1.excludes, o2?.excludes)
        CollectionUtils.merge(o1.includes, o2?.includes)
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

    Set<TaskProvider<Jar>> groovydocJarTasks() {
        groovydocJarTasks
    }

    void applyTo(org.gradle.api.tasks.javadoc.Groovydoc groovydoc) {
        groovydoc.getIncludes().addAll(includes)
        groovydoc.getExcludes().addAll(excludes)
        options.applyTo(groovydoc)
    }
}
