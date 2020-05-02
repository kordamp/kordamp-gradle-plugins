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
package org.kordamp.gradle.plugin.base.internal

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.kordamp.gradle.plugin.base.plugins.Detekt
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedDetekt

import static org.kordamp.gradle.PropertyUtils.booleanProvider
import static org.kordamp.gradle.PropertyUtils.fileProvider
import static org.kordamp.gradle.PropertyUtils.setProvider
import static org.kordamp.gradle.PropertyUtils.stringProvider

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@PackageScope
@CompileStatic
class DetektImpl extends AbstractQualityFeature implements Detekt {
    final RegularFileProperty configFile
    final RegularFileProperty baselineFile
    final Property<Boolean> parallel
    final Property<Boolean> buildUponDefaultConfig
    final Property<Boolean> disableDefaultRuleSets
    final Property<Boolean> failFast

    private ResolvedDetekt resolved

    DetektImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
        super(project, ownerConfig, parentConfig)

        configFile = project.objects.fileProperty()
        baselineFile = project.objects.fileProperty()
        parallel = project.objects.property(Boolean)
        buildUponDefaultConfig = project.objects.property(Boolean)
        disableDefaultRuleSets = project.objects.property(Boolean)
        failFast = project.objects.property(Boolean)
    }

    void normalize() {
        if (!enabled.present) {
            if (isRoot()) {
                if (project.childProjects.isEmpty()) {
                    enabled.set(project.pluginManager.hasPlugin(KOTLIN_JVM_PLUGIN_ID) && isApplied())
                } else {
                    enabled.set(project.childProjects.values().any { p -> p.pluginManager.hasPlugin(KOTLIN_JVM_PLUGIN_ID) && isApplied(p) })
                }
            } else {
                enabled.set(project.pluginManager.hasPlugin(KOTLIN_JVM_PLUGIN_ID) && isApplied())
            }
        }
    }

    ResolvedDetekt asResolved() {
        if (!resolved) {
            resolved = new ResolvedDetektImpl(project.providers,
                parentConfig?.asResolved()?.quality?.detekt,
                this)
        }
        resolved
    }

    @PackageScope
    @CompileStatic
    static class ResolvedDetektImpl extends AbstractResolvedFeature implements ResolvedDetekt {
        final Provider<Boolean> enabled
        final Provider<Boolean> ignoreFailures
        final Provider<String> toolVersion
        final Provider<Set<String>> excludedSourceSets
        final Provider<File> configFile
        final Provider<File> baselineFile
        final Provider<Boolean> parallel
        final Provider<Boolean> buildUponDefaultConfig
        final Provider<Boolean> disableDefaultRuleSets
        final Provider<Boolean> failFast

        private final ResolvedDetekt parent
        private final DetektImpl self
        private ResolvedAggregate aggregate

        ResolvedDetektImpl(ProviderFactory providers, ResolvedDetekt parent, DetektImpl self) {
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
                '1.8.0')

            excludedSourceSets = setProvider(providers,
                parent?.excludedSourceSets,
                self.excludedSourceSets,
                [] as Set)

            configFile = fileProvider(providers,
                parent?.configFile,
                self.configFile,
                resolveFile())

            baselineFile = fileProvider(providers,
                parent?.baselineFile,
                self.baselineFile,
                null)

            parallel = booleanProvider(providers,
                parent?.parallel,
                self.parallel,
                true)

            buildUponDefaultConfig = booleanProvider(providers,
                parent?.buildUponDefaultConfig,
                self.buildUponDefaultConfig,
                false)

            disableDefaultRuleSets = booleanProvider(providers,
                parent?.disableDefaultRuleSets,
                self.disableDefaultRuleSets,
                false)

            failFast = booleanProvider(providers,
                parent?.failFast,
                self.failFast,
                false)
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
                enabled               : enabled.get(),
                ignoreFailures        : ignoreFailures.get(),
                toolVersion           : toolVersion.get(),
                excludedSourceSets    : excludedSourceSets.get(),
                configFile            : configFile.get(),
                baselineFile          : baselineFile.get(),
                parallel              : parallel.get(),
                buildUponDefaultConfig: buildUponDefaultConfig.get(),
                disableDefaultRuleSets: disableDefaultRuleSets.get(),
                failFast              : failFast.get(),
            ])

            if (isRoot()) {
                map.aggregate = getAggregate().toMap()
            }

            new LinkedHashMap<>('detekt': map)
        }

        private File resolveFile() {
            File file = project.rootProject.file("config/detekt/${project.name}.yml")
            if (!file.exists()) {
                file = project.rootProject.file("config/detekt/detekt.yml")
            }
            file
        }
    }
}
