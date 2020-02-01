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

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.32.0
 */
@CompileStatic
@Canonical
class Sonar extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.sonar'

    Boolean ignoreFailures
    String hostUrl = 'https://sonarcloud.io'
    String username
    String projectKey
    Map<String, Object> configProperties = [:]
    Set<String> excludes = new LinkedHashSet<>()
    final Set<Project> excludedProjects = new LinkedHashSet<>()

    Sonar(ProjectConfigurationExtension config, Project project) {
        super(config, project)
        doSetEnabled(project.plugins.findPlugin(PLUGIN_ID) != null)
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(
            enabled: enabled
        )

        if (isRoot()) {
            populateRootMapDescription(map)
        }

        new LinkedHashMap<>(sonar: map)
    }

    protected void populateRootMapDescription(Map<String, Object> map) {
        map.hostUrl = this.hostUrl
        map.username = this.username
        map.projectKey = this.projectKey
        map.ignoreFailures = getIgnoreFailures()
        map.configProperties = this.configProperties
        map.excludes = this.excludes
        map.excludedProjects = excludedProjects
    }

    boolean getIgnoreFailures() {
        this.ignoreFailures == null || this.ignoreFailures
    }

    protected boolean isIgnoreFailuresSet() {
        this.ignoreFailures != null
    }

    void normalize() {
        if (null == projectKey) {
            projectKey = username + '_' + project.rootProject.name
        }

        if (!enabledSet) {
            if (isRoot()) {
                if (project.childProjects.isEmpty()) {
                    setEnabled(project.pluginManager.hasPlugin('java-base') && isApplied())
                } else {
                    setEnabled(project.childProjects.values().any { p -> p.pluginManager.hasPlugin('java-base') && isApplied(p) })
                }
            } else {
                setEnabled(project.pluginManager.hasPlugin('java-base') && isApplied())
            }
        }
    }

    List<String> validate(ProjectConfigurationExtension extension) {
        List<String> errors = []

        if (enabled && isBlank(username)) {
            errors << "[${project.name}] Sonar username is blank".toString()
        }

        errors
    }

    void exclude(String str) {
        excludes << str
    }

    void excludeProject(Project p) {
        if (null != p) {
            excludedProjects << p
        }
    }

    void copyInto(Sonar copy) {
        super.copyInto(copy)
        copy.@hostUrl = hostUrl
        copy.@projectKey = projectKey
        copy.@username = username
        copy.@excludes.addAll(excludes)
        copy.excludedProjects.addAll(excludedProjects)
        copy.@configProperties.putAll(configProperties)
        copy.@ignoreFailures = ignoreFailures
    }

    static void merge(Sonar o1, Sonar o2) {
        AbstractQualityFeature.merge(o1, o2)
        o1.hostUrl = o1.hostUrl ?: o2.hostUrl
        o1.projectKey = o1.projectKey ?: o2.projectKey
        o1.username = o1.username ?: o2.username
        CollectionUtils.merge(o1.configProperties, o2?.configProperties)
        CollectionUtils.merge(o1.excludes, o2?.excludes)
    }
}
