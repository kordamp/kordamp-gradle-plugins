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
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.kordamp.gradle.plugin.base.plugins.Codenarc
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedCodenarc

import static org.kordamp.gradle.PropertyUtils.booleanProvider
import static org.kordamp.gradle.PropertyUtils.fileProvider
import static org.kordamp.gradle.PropertyUtils.intProvider
import static org.kordamp.gradle.PropertyUtils.setProvider
import static org.kordamp.gradle.PropertyUtils.stringProvider

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@PackageScope
@CompileStatic
class CodenarcImpl extends AbstractQualityFeature implements Codenarc {
    final RegularFileProperty configFile
    final Property<Integer> maxPriority1Violations
    final Property<Integer> maxPriority2Violations
    final Property<Integer> maxPriority3Violations

    private ResolvedCodenarc resolved

    CodenarcImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
        super(project, ownerConfig, parentConfig)

        configFile = project.objects.fileProperty()
        maxPriority1Violations = project.objects.property(Integer)
        maxPriority2Violations = project.objects.property(Integer)
        maxPriority3Violations = project.objects.property(Integer)
    }

    ResolvedCodenarc asResolved() {
        if (!resolved) {
            resolved = new ResolvedCodenarcImpl(project.providers,
                parentConfig?.asResolved()?.quality?.codenarc,
                this)
        }
        resolved
    }

    @PackageScope
    @CompileStatic
    static class ResolvedCodenarcImpl extends AbstractResolvedFeature implements ResolvedCodenarc {
        final Provider<Boolean> enabled
        final Provider<Boolean> ignoreFailures
        final Provider<String> toolVersion
        final Provider<Set<String>> excludedSourceSets
        final Provider<File> configFile
        final Provider<Integer> maxPriority1Violations
        final Provider<Integer> maxPriority2Violations
        final Provider<Integer> maxPriority3Violations

        private final ResolvedCodenarc parent
        private final CodenarcImpl self
        private ResolvedAggregate aggregate

        ResolvedCodenarcImpl(ProviderFactory providers, ResolvedCodenarc parent, CodenarcImpl self) {
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
                '1.5')

            excludedSourceSets = setProvider(providers,
                parent?.excludedSourceSets,
                self.excludedSourceSets,
                [] as Set)

            configFile = fileProvider(providers,
                parent?.configFile,
                self.configFile,
                resolveFile())

            maxPriority1Violations = intProvider(providers,
                parent?.maxPriority1Violations,
                self.maxPriority1Violations,
                0)

            maxPriority2Violations = intProvider(providers,
                parent?.maxPriority2Violations,
                self.maxPriority2Violations,
                0)

            maxPriority3Violations = intProvider(providers,
                parent?.maxPriority3Violations,
                self.maxPriority3Violations,
                0)
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
                configFile            : configFile.get(),
                maxPriority1Violations: maxPriority1Violations.get(),
                maxPriority2Violations: maxPriority2Violations.get(),
                maxPriority3Violations: maxPriority3Violations.get()
            ])

            if (isRoot()) {
                map.aggregate = getAggregate().toMap()
            }

            new LinkedHashMap<>('codenarc': map)
        }

        private File resolveFile() {
            File file = project.rootProject.file("config/codenarc/${project.name}.xml")
            if (!file.exists()) {
                file = project.rootProject.file("config/codenarc/codenarc.xml")
            }
            file
        }
    }
}
