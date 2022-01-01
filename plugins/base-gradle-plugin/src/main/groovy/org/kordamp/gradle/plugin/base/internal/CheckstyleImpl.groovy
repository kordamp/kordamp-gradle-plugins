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
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.SetProperty
import org.kordamp.gradle.plugin.base.plugins.Checkstyle
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedCheckstyle

import static org.kordamp.gradle.PropertyUtils.booleanProvider
import static org.kordamp.gradle.PropertyUtils.fileProvider
import static org.kordamp.gradle.PropertyUtils.intProvider
import static org.kordamp.gradle.PropertyUtils.mapProvider
import static org.kordamp.gradle.PropertyUtils.setProvider
import static org.kordamp.gradle.PropertyUtils.stringProvider
import static org.kordamp.gradle.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@PackageScope
@CompileStatic
class CheckstyleImpl extends AbstractQualityFeature implements Checkstyle {
    final RegularFileProperty configFile
    final MapProperty<String, Object> configProperties
    final Property<Integer> maxErrors
    final Property<Integer> maxWarnings
    final Property<Boolean> showViolations
    final SetProperty<String> excludes
    final SetProperty<String> includes

    private ResolvedCheckstyle resolved

    CheckstyleImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
        super(project, ownerConfig, parentConfig)

        configFile = project.objects.fileProperty()
        configProperties = project.objects.mapProperty(String, Object)
        maxErrors = project.objects.property(Integer)
        maxWarnings = project.objects.property(Integer)
        showViolations = project.objects.property(Boolean)
        excludes = project.objects.setProperty(String).convention([])
        includes = project.objects.setProperty(String).convention([])
    }

    ResolvedCheckstyle asResolved() {
        if (!resolved) {
            resolved = new ResolvedCheckstyleImpl(project.providers,
                parentConfig?.asResolved()?.quality?.checkstyle,
                this)
        }
        resolved
    }

    @Override
    void include(String str) {
        if (isNotBlank(str)) includes.add(str)
    }

    @Override
    void exclude(String str) {
        if (isNotBlank(str)) excludes.add(str)
    }

    @PackageScope
    @CompileStatic
    static class ResolvedCheckstyleImpl extends AbstractResolvedFeature implements ResolvedCheckstyle {
        final Provider<Boolean> enabled
        final Provider<Boolean> ignoreFailures
        final Provider<String> toolVersion
        final Provider<Set<String>> excludedSourceSets
        final Provider<File> configFile
        final Provider<Map<String, Object>> configProperties
        final Provider<Integer> maxErrors
        final Provider<Integer> maxWarnings
        final Provider<Boolean> showViolations
        final Provider<Set<String>> excludes
        final Provider<Set<String>> includes

        private final ResolvedCheckstyle parent
        private final CheckstyleImpl self
        private ResolvedAggregate aggregate

        ResolvedCheckstyleImpl(ProviderFactory providers, ResolvedCheckstyle parent, CheckstyleImpl self) {
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

            toolVersion = stringProvider(providers,
                parent?.toolVersion,
                self.toolVersion,
                '8.32')

            excludedSourceSets = setProvider(providers,
                parent?.excludedSourceSets,
                self.excludedSourceSets,
                [] as Set)

            configFile = fileProvider(providers,
                parent?.configFile,
                self.configFile,
                resolveFile())

            configProperties = mapProvider(providers,
                parent?.configProperties,
                self.configProperties,
                [:])

            maxErrors = intProvider(providers,
                parent?.maxErrors,
                self.maxErrors,
                0)

            maxWarnings = intProvider(providers,
                parent?.maxWarnings,
                self.maxWarnings,
                Integer.MAX_VALUE)

            showViolations = booleanProvider(providers,
                parent?.showViolations,
                self.showViolations,
                true)

            excludes = setProvider(providers,
                parent?.excludes,
                self.excludes,
                [] as Set)

            includes = setProvider(providers,
                parent?.includes,
                self.includes,
                [] as Set)
        }

        @Override
        ResolvedAggregate getAggregate() {
            if (!aggregate) {
                aggregate = new AbstractAggregatingFeature.ResolvedAggregateImpl(project.providers,
                    parent?.aggregate,
                    self.aggregate)
            }
            aggregate
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<>([
                enabled              : enabled.get(),
                ignoreFailures       : ignoreFailures.get(),
                toolVersion          : toolVersion.get(),
                excludedSourceSets   : excludedSourceSets.get(),
                configFile        : configFile.get(),
                configProperties  : configProperties.get(),
                maxErrors         : maxErrors.get(),
                maxWarnings       : maxWarnings.get(),
                excludes          : excludes.get(),
                includes          : includes.get(),
                showViolations    : showViolations.get()
            ])

            if (isRoot()) {
                map.aggregate = getAggregate().toMap()
            }

            new LinkedHashMap<>('checkstyle': map)
        }

        private File resolveFile() {
            File file = project.rootProject.file("config/checkstyle/${project.name}.xml")
            if (!file.exists()) {
                file = project.rootProject.file("config/checkstyle/checkstyle.xml")
            }
            file
        }
    }
}
