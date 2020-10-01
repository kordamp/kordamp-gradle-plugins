/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2020 Andres Almiray.
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

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.scala.ScalaDoc
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.impl.ScaladocOptions
import org.kordamp.gradle.util.CollectionUtils
import org.kordamp.gradle.util.ConfigureUtil

/**
 * @author Andres Almiray
 * @since 0.15.0
 */
@CompileStatic
class Scaladoc extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.scaladoc'

    String title
    boolean replaceJavadoc = false
    Set<String> excludes = new LinkedHashSet<>()
    Set<String> includes = new LinkedHashSet<>()
    final ScaladocOptions options = new ScaladocOptions()
    final Aggregate aggregate

    private boolean replaceJavadocSet

    Scaladoc(ProjectConfigurationExtension config, Project project) {
        super(config, project, PLUGIN_ID)

        aggregate           = new Aggregate(config, project)
        title               = "${project.name} ${project.version}"
        options.windowTitle = "${project.name} ${project.version}"
        options.docTitle    = "${project.name} ${project.version}"
        options.header      = "${project.name} ${project.version}"
    }

    @Override
    protected AbstractFeature getParentFeature() {
        return project.rootProject.extensions.getByType(ProjectConfigurationExtension).docs.scaladoc
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        map.title = title
        map.replaceJavadoc = replaceJavadoc
        map.excludes = excludes
        map.includes = includes
        map.options = new LinkedHashMap<String, Object>([
            windowTitle         : options.windowTitle,
            docTitle            : options.docTitle,
            header              : options.header,
            footer              : options.footer,
            bottom              : options.bottom,
            top                 : options.top,
            deprecation         : options.deprecation,
            unchecked           : options.unchecked,
            additionalParameters: options.additionalParameters
        ])

        if (isRoot()) {
            map.putAll(aggregate.toMap())
        }

        new LinkedHashMap<>('scaladoc': map)
    }

    @Override
    protected boolean hasBasePlugin(Project project) {
        project.pluginManager.hasPlugin('scala')
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

    void options(Action<? super ScaladocOptions> action) {
        action.execute(options)
    }

    void options(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ScaladocOptions) Closure<Void> action) {
        ConfigureUtil.configure(action, options)
    }

    void aggregate(Action<? super Aggregate> action) {
        action.execute(aggregate)
    }

    void aggregate(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Aggregate) Closure<Void> action) {
        ConfigureUtil.configure(action, aggregate)
    }

    static void merge(Scaladoc o1, Scaladoc o2) {
        AbstractFeature.merge(o1, o2)
        o1.setTitle(o1.title ?: o2?.title)
        o1.setReplaceJavadoc((boolean) (o1.replaceJavadocSet ? o1.replaceJavadoc : o2.replaceJavadoc))
        o1.excludes = CollectionUtils.merge(o1.excludes, o2.excludes, false)
        o1.includes = CollectionUtils.merge(o1.includes, o2.includes, false)
        ScaladocOptions.merge(o1.options, o2.options)
        Aggregate.merge(o1.aggregate, o2.aggregate)
    }

    void applyTo(ScalaDoc scaladoc) {
        scaladoc.title = title
        scaladoc.getIncludes().addAll(includes)
        scaladoc.getExcludes().addAll(excludes)
        options.applyTo(scaladoc)
    }

    void postMerge() {
        if (replaceJavadoc) config.docs.javadoc.enabled = false
        super.postMerge()
    }

    @CompileStatic
    static class Aggregate {
        Boolean enabled
        Boolean fast
        Boolean replaceJavadoc
        final Set<Project> excludedProjects = new LinkedHashSet<>()

        private final ProjectConfigurationExtension config
        private final Project project

        Aggregate(ProjectConfigurationExtension config, Project project) {
            this.config = config
            this.project = project
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<String, Object>()

            map.enabled = getEnabled()
            map.fast = getFast()
            map.replaceJavadoc = getReplaceJavadoc()
            map.excludedProjects = excludedProjects

            new LinkedHashMap<>('aggregate': map)
        }

        boolean getEnabled() {
            this.@enabled == null || this.@enabled
        }

        boolean getFast() {
            this.@fast == null || this.@fast
        }

        boolean getReplaceJavadoc() {
            this.@replaceJavadoc != null && this.@replaceJavadoc
        }

        static Aggregate merge(Aggregate o1, Aggregate o2) {
            o1.enabled = o1.@enabled != null ? o1.getEnabled() : o2.getEnabled()
            o1.fast = o1.@fast != null ? o1.getFast() : o2.getFast()
            o1.replaceJavadoc = o1.@replaceJavadoc != null ? o1.getReplaceJavadoc() : o2.getReplaceJavadoc()
            o1
        }
    }
}
