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
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
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

    private boolean failOnErrorsSet
    private boolean failOnExceptionSet
    private boolean semverSet

    private final Set<Project> projects = new LinkedHashSet<>()
    private final Set<TaskProvider<? extends Task>> clirrTasks = new LinkedHashSet<>()

    Clirr(ProjectConfigurationExtension config, Project project) {
        super(config, project)
        doSetEnabled(project.plugins.findPlugin(PLUGIN_ID) != null)
    }

    void normalize() {
        if (!enabledSet && isRoot()) {
            setEnabled(project.plugins.findPlugin(PLUGIN_ID) != null)
        }
    }

    @Override
    String toString() {
        isRoot() ? toMap().toString() : ''
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        if (enabled) {
            map.baseline = baseline
            map.filterFile = filterFile
            map.failOnErrors = failOnErrors
            map.failOnException = failOnException
            map.semver = semver
            map.filter = (filter != null)
        }

        new LinkedHashMap<>('clirr': map)
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
    }

    static void merge(Clirr o1, Clirr o2) {
        AbstractFeature.merge(o1, o2)
        o1.setFailOnErrors((boolean) (o1.failOnErrorsSet ? o1.failOnErrors : o2.failOnErrors))
        o1.setFailOnException((boolean) (o1.failOnExceptionSet ? o1.failOnException : o2.failOnException))
        o1.setSemver((boolean) (o1.semverSet ? o1.semver : o2.semver))
        o1.projects().addAll(o2.projects())
        o1.baseline = o1.baseline ?: o2.baseline
        o1.filterFile = o1.filterFile ?: o2.filterFile
        o1.filter = o1.filter ?: o2.filter
    }

    Set<Project> projects() {
        projects
    }

    Set<TaskProvider<? extends Task>> clirrTasks() {
        clirrTasks
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
}
