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
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

import java.util.function.Predicate

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
class Clirr extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.clirr'

    String baseline
    File filterFile
    Predicate<? super Difference> filter
    boolean failOnErrors = true
    boolean failOnException = false
    boolean semver = true
    final Aggregate aggregate

    private boolean failOnErrorsSet
    private boolean failOnExceptionSet
    private boolean semverSet

    Clirr(ProjectConfigurationExtension config, Project project) {
        super(config, project)
        aggregate = new Aggregate(config, project)
        doSetEnabled(project.plugins.findPlugin(PLUGIN_ID) != null)
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

    @Override
    String toString() {
        isRoot() ? toMap().toString() : ''
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled,
            baseline: baseline,
            filterFile: filterFile,
            failOnErrors: failOnErrors,
            failOnException: failOnException,
            semver: semver,
            filter: (filter != null))

        if (isRoot()) {
            map.putAll(aggregate.toMap())
        }

        new LinkedHashMap<>('clirr': map)
    }

    void aggregate(Action<? super Aggregate> action) {
        action.execute(aggregate)
    }

    void aggregate(@DelegatesTo(Aggregate) Closure action) {
        ConfigureUtil.configure(action, aggregate)
    }

    void copyInto(Clirr copy) {
        super.copyInto(copy)
        copy.@failOnErrors = this.failOnErrors
        copy.@failOnErrorsSet = this.failOnErrorsSet
        copy.@failOnException = this.failOnException
        copy.@failOnExceptionSet = this.failOnExceptionSet
        copy.@semver = this.semver
        copy.@semverSet = this.semverSet
        copy.baseline = baseline
        copy.filterFile = filterFile
        copy.filter = filter
        aggregate.copyInto(copy.aggregate)
    }

    static void merge(Clirr o1, Clirr o2) {
        AbstractFeature.merge(o1, o2)
        o1.setFailOnErrors((boolean) (o1.failOnErrorsSet ? o1.failOnErrors : o2.failOnErrors))
        o1.setFailOnException((boolean) (o1.failOnExceptionSet ? o1.failOnException : o2.failOnException))
        o1.setSemver((boolean) (o1.semverSet ? o1.semver : o2.semver))
        o1.baseline = o1.baseline ?: o2.baseline
        o1.filterFile = o1.filterFile ?: o2.filterFile
        o1.filter = o1.filter ?: o2.filter
        o1.aggregate.merge(o2.aggregate)
    }

    @Canonical
    static class Difference implements Comparable<Difference> {
        String classname
        String severity
        String identifier
        String message

        @Override
        int compareTo(Difference o) {
            return classname <=> o.classname
        }
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
