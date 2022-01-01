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
package org.kordamp.gradle.plugin.base.internal

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.SetProperty
import org.kordamp.gradle.plugin.base.plugins.Sonar
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedSonar

import static org.kordamp.gradle.PropertyUtils.booleanProvider
import static org.kordamp.gradle.PropertyUtils.mapProvider
import static org.kordamp.gradle.PropertyUtils.setProvider
import static org.kordamp.gradle.PropertyUtils.stringProvider
import static org.kordamp.gradle.StringUtils.isBlank
import static org.kordamp.gradle.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@PackageScope
@CompileStatic
class SonarImpl extends AbstractFeature implements Sonar {
    final Property<Boolean> ignoreFailures
    final Property<String> hostUrl
    final Property<String> username
    final Property<String> projectKey
    final MapProperty<String, Object> configProperties
    final SetProperty<String> excludes
    final SetProperty<Project> excludedProjects

    private ResolvedSonar resolved

    SonarImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
        super(project, ownerConfig, parentConfig)

        ignoreFailures = project.objects.property(Boolean)
        hostUrl = project.objects.property(String)
        username = project.objects.property(String)
        projectKey = project.objects.property(String)
        configProperties = project.objects.mapProperty(String, Object)
        excludes = project.objects.setProperty(String)
        excludedProjects = project.objects.setProperty(Project)
    }

    @Override
    void normalize() {
        if (!enabled.present) {
            if (isRoot()) {
                if (project.childProjects.isEmpty()) {
                    enabled.set(project.pluginManager.hasPlugin('java-base') && isApplied())
                } else {
                    enabled.set(project.childProjects.values().any { p -> p.pluginManager.hasPlugin('java-base') && isApplied(p) })
                }
            } else {
                enabled.set(project.pluginManager.hasPlugin('java-base') && isApplied())
            }
        }
    }

    @Override
    void validate(List<String> errors) {
        if (enabled.get() && isBlank(username.orNull)) {
            errors << "[${project.name}] Sonar username is blank".toString()
        }
    }

    ResolvedSonar asResolved() {
        if (!resolved) {
            resolved = new ResolvedSonarImpl(project.providers,
                parentConfig?.asResolved()?.quality?.sonar,
                this)
        }
        resolved
    }

    @Override
    void exclude(String str) {
        if (isNotBlank(str)) excludes.add(str)
    }

    @Override
    void excludeProject(Project project) {
        if (project) excludedProjects.add(project)
    }

    @PackageScope
    @CompileStatic
    static class ResolvedSonarImpl extends AbstractResolvedFeature implements ResolvedSonar {
        final Provider<Boolean> enabled
        final Provider<Boolean> ignoreFailures
        final Provider<String> hostUrl
        final Provider<String> username
        final Provider<String> projectKey
        final Provider<Map<String, Object>> configProperties
        final Provider<Set<String>> excludes
        final Provider<Set<Project>> excludedProjects

        private ResolvedSonar parent
        private final SonarImpl self

        ResolvedSonarImpl(ProviderFactory providers, ResolvedSonar parent, SonarImpl self) {
            super(self.project)
            this.parent = parent
            this.self = self

            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                true)

            ignoreFailures = booleanProvider(providers,
                parent?.ignoreFailures,
                self.ignoreFailures,
                true)

            hostUrl = stringProvider(providers,
                parent?.hostUrl,
                self.hostUrl,
                'https://sonarcloud.io')

            username = stringProvider(providers,
                parent?.username,
                self.username,
                '')

            projectKey = stringProvider(providers,
                parent?.projectKey,
                self.projectKey,
                username.get() + '_' + project.rootProject.name)

            configProperties = mapProvider(providers,
                parent?.configProperties,
                self.configProperties,
                [:])

            excludes = setProvider(providers,
                parent?.excludes,
                self.excludes,
                [] as Set)

            excludedProjects = setProvider(providers,
                parent?.excludedProjects,
                self.excludedProjects,
                [] as Set)
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<>([
                enabled         : enabled.get(),
                ignoreFailures  : ignoreFailures.get(),
                hostUrl         : hostUrl.get(),
                username        : username.get(),
                projectKey      : projectKey.get(),
                configProperties: configProperties.get(),
                excludes        : excludes.get(),
                excludedProjects: excludedProjects.get()
            ])

            new LinkedHashMap<>('sonar': map)
        }
    }
}
