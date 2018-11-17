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
package org.kordamp.gradle.plugin.base.plugins.mutable

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.plugin.base.model.impl.ExtStandardJavadocDocletOptions
import org.kordamp.gradle.plugin.base.plugins.Javadoc

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
@EqualsAndHashCode(excludes = ['project', 'projects', 'javadocTasks', 'javadocJarTasks'])
class MutableJavadoc extends AbstractFeature implements Javadoc{
    Set<String> excludes = new LinkedHashSet<>()
    Set<String> includes = new LinkedHashSet<>()
    String title
    final ExtStandardJavadocDocletOptions options = new ExtStandardJavadocDocletOptions()

    private final Set<Project> projects = new LinkedHashSet<>()
    private final Set<org.gradle.api.tasks.javadoc.Javadoc> javadocTasks = new LinkedHashSet<>()
    private final Set<Jar> javadocJarTasks = new LinkedHashSet<>()

    MutableJavadoc(Project project) {
       super(project)
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

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    @CompileDynamic
    Map<String, Map<String, Object>> toMap() {
        Map map = [enabled: enabled]

        if (enabled) {
            List<String> links = []
            options.links.each { link ->
                links << link
            }

            map.title = title
            map.excludes = excludes
            map.includes = includes
            map.options = [
                windowTitle: options.windowTitle,
                docTitle   : options.docTitle,
                header     : options.header,
                encoding   : options.encoding,
                author     : options.author,
                version    : options.version,
                splitIndex : options.splitIndex,
                use        : options.use,
                links      : links
            ]
        }

        ['javadoc': map]
    }

    void include(String str) {
        includes << str
    }

    void exclude(String str) {
        excludes << str
    }

    void options(Action<? super ExtStandardJavadocDocletOptions> action) {
        action.execute(options)
    }

    void options(@DelegatesTo(ExtStandardJavadocDocletOptions) Closure action) {
        ConfigureUtil.configure(action, options)
    }

    void copyInto(MutableJavadoc copy) {
        super.copyInto(copy)
        copy.excludes.addAll(excludes)
        copy.includes.addAll(includes)
        copy.title = title
        options.copyInto(copy.options)
    }

    @CompileDynamic
    static void merge(MutableJavadoc o1, MutableJavadoc o2) {
        AbstractFeature.merge(o1, o2)
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
