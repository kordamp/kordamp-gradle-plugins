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

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.CollectionUtils
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

import static org.kordamp.gradle.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
class SourceXref extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.source-xref'

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
    final Aggregate aggregate

    SourceXref(ProjectConfigurationExtension config, Project project) {
        super(config, project)
        doSetEnabled(project.plugins.findPlugin(PLUGIN_ID) != null)
        aggregate = new Aggregate(config, project)
        windowTitle = "${project.name} ${project.version}"
        docTitle = "${project.name} ${project.version}"
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

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

        if (isRoot()) {
            map.putAll(aggregate.toMap())
        }

        new LinkedHashMap<>('sourceXref': map)
    }

    void normalize() {
        if (!enabledSet) {
            if (isRoot()) {
                if (project.childProjects.isEmpty()) {
                    setEnabled(project.pluginManager.hasPlugin('java') && isApplied())
                } else {
                    setEnabled(project.childProjects.values().any { p -> p.pluginManager.hasPlugin('java') && isApplied()})
                }
            } else {
                setEnabled(project.pluginManager.hasPlugin('java') && isApplied())
            }
        }
    }

    String getOutputEncoding() {
        outputEncoding ?: 'UTF-8'
    }

    String getInputEncoding() {
        isNotBlank(inputEncoding) ? inputEncoding : System.getProperty('file.encoding')
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

    void aggregate(Action<? super Aggregate> action) {
        action.execute(aggregate)
    }

    void aggregate(@DelegatesTo(Aggregate) Closure action) {
        ConfigureUtil.configure(action, aggregate)
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
        aggregate.copyInto(copy.aggregate)
    }

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
        CollectionUtils.merge(o1.excludes, o2?.excludes)
        CollectionUtils.merge(o1.includes, o2?.includes)
        o1.aggregate.merge(o2.aggregate)
    }

    @CompileStatic
    static class Aggregate {
        Boolean enabled
        private final Set<Project> excludedProjects = new LinkedHashSet<>()

        private final ProjectConfigurationExtension config
        private final Project project

        Aggregate(ProjectConfigurationExtension config, Project project) {
            this.config = config
            this.project = project
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<String, Object>()

            map.enabled = getEnabled()
            map.excludedProjects = excludedProjects

            new LinkedHashMap<>('aggregate': map)
        }

        boolean getEnabled() {
            this.@enabled == null || this.@enabled
        }

        void copyInto(Aggregate copy) {
            copy.@enabled = this.@enabled
            copy.excludedProjects.addAll(excludedProjects)
        }

        Aggregate copyOf() {
            Aggregate copy = new Aggregate(config, project)
            copyInto(copy)
            copy
        }

        Aggregate merge(Aggregate other) {
            Aggregate copy = copyOf()
            copy.enabled = copy.@enabled != null ? copy.getEnabled() : other.getEnabled()
            copy
        }

        Set<Project> excludedProjects() {
            excludedProjects
        }
    }
}
