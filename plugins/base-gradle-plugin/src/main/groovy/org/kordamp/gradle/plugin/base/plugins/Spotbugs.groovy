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
import org.gradle.api.Project
import org.kordamp.gradle.CollectionUtils
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

/**
 * @author Andres Almiray
 * @since 0.31.0
 */
@CompileStatic
@Canonical
class Spotbugs extends AbstractQualityFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.spotbugs'

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
    boolean showProgress = true
    Set<String> excludes = new LinkedHashSet<>()
    Set<String> includes = new LinkedHashSet<>()

    private boolean showProgressSet

    Spotbugs(ProjectConfigurationExtension config, Project project) {
        super(config, project, PLUGIN_ID, 'spotbugs')
        toolVersion = '3.1.12'
    }

    @Override
    protected void populateMapDescription(Map<String, Object> map) {
        super.populateMapDescription(map)
        map.includeFilterFile = this.includeFilterFile
        map.excludeFilterFile = this.excludeFilterFile
        map.excludeBugsFilterFile = this.excludeBugsFilterFile
        map.excludes = this.excludes
        map.includes = this.includes
        map.effort = this.effort
        map.reportLevel = this.reportLevel
        map.report = this.report
        map.visitors = this.visitors
        map.omitVisitors = this.omitVisitors
        map.extraArgs = this.extraArgs
        map.jvmArgs = this.jvmArgs
        map.showProgress = this.showProgress
    }

    @Override
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

        super.normalize()
    }

    void setShowProgress(boolean showProgress) {
        this.showProgress = showProgress
        this.showProgressSet = true
    }

    boolean isShowProgressSet() {
        this.showProgressSet
    }

    void include(String str) {
        includes << str
    }

    void exclude(String str) {
        excludes << str
    }

    void copyInto(Spotbugs copy) {
        super.copyInto(copy)
        copy.@showProgress = showProgress
        copy.@showProgressSet = showProgressSet
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
    }

    static void merge(Spotbugs o1, Spotbugs o2) {
        AbstractQualityFeature.merge(o1, o2)
        o1.setShowProgress((boolean) (o1.showProgressSet ? o1.showProgress : o2.showProgress))
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
    }
}
