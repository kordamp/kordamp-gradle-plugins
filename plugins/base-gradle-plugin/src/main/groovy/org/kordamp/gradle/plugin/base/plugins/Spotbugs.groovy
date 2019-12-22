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
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.CollectionUtils
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

/**
 * @author Andres Almiray
 * @since 0.31.0
 */
@CompileStatic
@Canonical
class Spotbugs extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.spotbugs'

    String toolVersion = '3.1.12'
    File includeFilterFile
    File excludeFilterFile
    File excludeBugsFilterFile
    String effort = 'max'
    String reportLevel = 'high'
    String report = 'html'
    List<String> visitors = []
    List<String> omitVisitors = []
    List<String> extraArgs = []
    List<String> jvmArgs = []
    boolean ignoreFailures = true
    boolean showProgress = true
    Set<String> excludes = new LinkedHashSet<>()
    Set<String> includes = new LinkedHashSet<>()
    final Aggregate aggregate

    private boolean showProgressSet
    private boolean ignoreFailuresSet

    Spotbugs(ProjectConfigurationExtension config, Project project) {
        super(config, project)
        doSetEnabled(project.plugins.findPlugin(PLUGIN_ID) != null)
        aggregate = new Aggregate(config, project)
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(
            enabled: enabled,
            includeFilterFile: includeFilterFile,
            excludeFilterFile: excludeFilterFile,
            excludeBugsFilterFile: excludeBugsFilterFile,
            excludes: excludes,
            includes: includes,
            effort: effort,
            reportLevel: reportLevel,
            report: report,
            visitors: visitors,
            omitVisitors: omitVisitors,
            extraArgs: extraArgs,
            jvmArgs: jvmArgs,
            showProgress: showProgress,
            ignoreFailures: ignoreFailures,
            toolVersion: toolVersion
        )
        if (isRoot()) {
            map.putAll(aggregate.toMap())
        }
        new LinkedHashMap<>('spotbugs': map)
    }

    void normalize() {
        if (null == includeFilterFile) {
            File file = project.rootProject.file("config/spotbugs/${project.name}-includeFilter.xml")
            if (!file.exists()) {
                file = project.rootProject.file('config/spotbugs/includeFilter.xml')
            }
            includeFilterFile = file
        }
        if (null == excludeFilterFile) {
            File file = project.rootProject.file("config/spotbugs/${project.name}-excludeFilter.xml")
            if (!file.exists()) {
                file = project.rootProject.file('config/spotbugs/excludeFilter.xml')
            }
            excludeFilterFile = file
        }
        if (null == excludeBugsFilterFile) {
            File file = project.rootProject.file("config/spotbugs/${project.name}-excludeBugsFilter.xml")
            if (!file.exists()) {
                file = project.rootProject.file('config/spotbugs/excludeBugsFilter.xml')
            }
            excludeBugsFilterFile = file
        }

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

    void setShowProgress(boolean showProgress) {
        this.showProgress = showProgress
        this.showProgressSet = true
    }

    boolean isShowProgressSet() {
        this.showProgressSet
    }

    void setIgnoreFailures(boolean ignoreFailures) {
        this.ignoreFailures = ignoreFailures
        this.ignoreFailuresSet = true
    }

    boolean isIgnoreFailuresSet() {
        this.ignoreFailuresSet
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

    void copyInto(Spotbugs copy) {
        super.copyInto(copy)
        copy.@showProgress = showProgress
        copy.@showProgressSet = showProgressSet
        copy.@ignoreFailures = ignoreFailures
        copy.@ignoreFailuresSet = ignoreFailuresSet
        copy.excludes.addAll(excludes)
        copy.includes.addAll(includes)
        copy.visitors.addAll(visitors)
        copy.omitVisitors.addAll(omitVisitors)
        copy.extraArgs.addAll(extraArgs)
        copy.jvmArgs.addAll(jvmArgs)
        copy.effort = effort
        copy.reportLevel = reportLevel
        copy.report = report
        copy.includeFilterFile = includeFilterFile
        copy.excludeFilterFile = excludeFilterFile
        copy.excludeBugsFilterFile = excludeBugsFilterFile
        copy.toolVersion = toolVersion
        aggregate.copyInto(copy.aggregate)
    }

    static void merge(Spotbugs o1, Spotbugs o2) {
        AbstractFeature.merge(o1, o2)
        o1.setShowProgress((boolean) (o1.showProgressSet ? o1.showProgress : o2.showProgress))
        o1.setIgnoreFailures((boolean) (o1.ignoreFailuresSet ? o1.ignoreFailures : o2.ignoreFailures))
        CollectionUtils.merge(o1.excludes, o2?.excludes)
        CollectionUtils.merge(o1.includes, o2?.includes)
        CollectionUtils.merge(o1.visitors, o2?.visitors)
        CollectionUtils.merge(o1.omitVisitors, o2?.omitVisitors)
        CollectionUtils.merge(o1.extraArgs, o2?.extraArgs)
        CollectionUtils.merge(o1.jvmArgs, o2?.jvmArgs)
        o1.includeFilterFile = o1.includeFilterFile ?: o2.includeFilterFile
        o1.excludeFilterFile = o1.excludeFilterFile ?: o2.excludeFilterFile
        o1.excludeBugsFilterFile = o1.excludeBugsFilterFile ?: o2.excludeBugsFilterFile
        o1.effort = o1.effort ?: o2.effort
        o1.reportLevel = o1.reportLevel ?: o2.reportLevel
        o1.report = o1.report ?: o2.report
        o1.toolVersion = o1.toolVersion ?: o2.toolVersion
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
