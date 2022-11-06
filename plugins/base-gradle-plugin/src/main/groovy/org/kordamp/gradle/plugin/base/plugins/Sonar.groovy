/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2022 Andres Almiray.
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
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.util.CollectionUtils

import static org.kordamp.gradle.util.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.32.0
 */
@CompileStatic
class Sonar extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.sonar'

    Boolean ignoreFailures
    String hostUrl = 'https://sonarcloud.io'
    String projectKey
    String organization
    String login
    String password
    Map<String, Object> configProperties = [:]
    Set<String> excludes = new LinkedHashSet<>()
    final Set<Project> excludedProjects = new LinkedHashSet<>()

    Sonar(ProjectConfigurationExtension config, Project project) {
        super(config, project, PLUGIN_ID)
    }

    @Override
    protected AbstractFeature getParentFeature() {
        return project.rootProject.extensions.getByType(ProjectConfigurationExtension).quality.sonar
    }

    @Override
    protected void normalizeEnabled() {
        if (!enabledSet) {
            setEnabled(isApplied())
        }
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(
            enabled: enabled
        )

        if (isRoot()) {
            map.hostUrl = this.hostUrl
            map.projectKey = this.projectKey
            map.organization = this.organization
            map.login = this.login
            map.password = this.password
            map.ignoreFailures = getIgnoreFailures()
            map.configProperties = this.configProperties
            map.excludes = this.excludes
            map.excludedProjects = excludedProjects
        }

        new LinkedHashMap<>(sonar: map)
    }

    boolean getIgnoreFailures() {
        this.ignoreFailures != null && this.ignoreFailures
    }

    protected boolean isIgnoreFailuresSet() {
        this.ignoreFailures != null
    }

    void normalize() {
        if (null == projectKey) {
            projectKey = String.valueOf(project.group) + ':' + project.name
        }

        super.normalize()
    }

    protected boolean hasBasePlugin(Project project) {
        project.pluginManager.hasPlugin('java-base')
    }

    List<String> validate(ProjectConfigurationExtension extension) {
        List<String> errors = []

        if (enabled) {
            String extHostUrl = System.getProperty('sonar.host.url')
            if (!isBlank(extHostUrl)) {
                setHostUrl(extHostUrl)
            }

            String extLogin = System.getProperty('sonar.login')
            if (!isBlank(extLogin)) {
                setLogin(extLogin)
            }

            if (getHostUrl().startsWith('https://sonarcloud.io') && isBlank(getOrganization())) {
                errors << "[${project.name}] Sonar organization is blank".toString()
            }
            if (isBlank(getLogin())) {
                errors << "[${project.name}] Sonar login is blank".toString()
            }
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

    static void merge(Sonar o1, Sonar o2) {
        AbstractQualityFeature.merge(o1, o2)
        o1.hostUrl = o1.hostUrl ?: o2.hostUrl
        o1.projectKey = o1.projectKey ?: o2.projectKey
        o1.organization = o1.organization ?: o2.organization
        o1.login = o1.login ?: o2.login
        o1.password = o1.password ?: o2.password
        o1.configProperties = CollectionUtils.merge(o1.configProperties, o2?.configProperties, false)
        o1.excludes = CollectionUtils.merge(o1.excludes, o2.excludes, false)
    }
}
