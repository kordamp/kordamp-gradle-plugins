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
import org.gradle.api.tasks.scala.ScalaDoc
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.plugin.base.model.impl.ScaladocOptions

/**
 * @author Andres Almiray
 * @since 0.15.0
 */
@CompileStatic
@Canonical
class Scaladoc extends AbstractFeature {
    String title
    Set<String> excludes = new LinkedHashSet<>()
    Set<String> includes = new LinkedHashSet<>()
    final ScaladocOptions options = new ScaladocOptions()

    private final Set<Project> excludedProjects = new LinkedHashSet<>()

    private final Set<Project> projects = new LinkedHashSet<>()
    private final Set<ScalaDoc> scaladocTasks = new LinkedHashSet<>()
    private final Set<Jar> scaladocJarTasks = new LinkedHashSet<>()

    Scaladoc(Project project) {
        super(project)
        doSetEnabled(project.plugins.findPlugin('scala') != null)

        title               = "${project.name} ${project.version}"
        options.windowTitle = "${project.name} ${project.version}"
        options.docTitle    = "${project.name} ${project.version}"
        options.header      = "${project.name} ${project.version}"
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    @CompileDynamic
    Map<String, Map<String, Object>> toMap() {
        Map map = [enabled: enabled]

        if (enabled && isRoot()) {
            map.excludedProjects = excludedProjects
        }

        if (enabled) {
            map.title = title
            map.excludes = excludes
            map.includes = includes
            map.options = [
                windowTitle         : options.windowTitle,
                docTitle            : options.docTitle,
                header              : options.header,
                footer              : options.footer,
                bottom              : options.bottom,
                top                 : options.top,
                deprecation         : options.deprecation,
                unchecked           : options.unchecked,
                additionalParameters: options.additionalParameters
            ]
        }

        ['scaladoc': map]
    }

    void normalize() {
        if (!enabledSet) {
            setEnabled(project.plugins.findPlugin('scala') != null)
        }
    }

    void include(String str) {
        includes << str
    }

    void exclude(String str) {
        excludes << str
    }

    void options(Action<? super ScaladocOptions> action) {
        action.execute(options)
    }

    void options(@DelegatesTo(ScaladocOptions) Closure action) {
        ConfigureUtil.configure(action, options)
    }

    void copyInto(Scaladoc copy) {
        super.copyInto(copy)
        copy.title = title
        copy.excludes.addAll(excludes)
        copy.includes.addAll(includes)
        options.copyInto(copy.options)
    }

    @CompileDynamic
    static void merge(Scaladoc o1, Scaladoc o2) {
        AbstractFeature.merge(o1, o2)
        o1.setTitle(o1.title ?: o2?.title)
        o1.excludes.addAll(((o1.excludes ?: []) + (o2?.excludes ?: [])).unique())
        o1.includes.addAll(((o1.includes ?: []) + (o2?.includes ?: [])).unique())
        ScaladocOptions.merge(o1.options, o2.options)
        o1.projects().addAll(o2.projects())
        o1.scaladocTasks().addAll(o2.scaladocTasks())
        o1.scaladocJarTasks().addAll(o2.scaladocJarTasks())
        o1.excludedProjects().addAll(o2.excludedProjects())
    }

    Set<Project> excludedProjects() {
        excludedProjects
    }

    Set<Project> projects() {
        projects
    }

    Set<ScalaDoc> scaladocTasks() {
        scaladocTasks
    }

    Set<Jar> scaladocJarTasks() {
        scaladocJarTasks
    }

    void applyTo(ScalaDoc scaladoc) {
        scaladoc.title = title
        scaladoc.getIncludes().addAll(includes)
        scaladoc.getExcludes().addAll(excludes)
        options.applyTo(scaladoc)
    }
}
