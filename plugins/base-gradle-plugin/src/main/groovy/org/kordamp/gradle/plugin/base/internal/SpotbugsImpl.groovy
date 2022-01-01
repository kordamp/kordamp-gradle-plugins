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
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.SetProperty
import org.kordamp.gradle.plugin.base.plugins.Spotbugs
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedSpotbugs

import static org.kordamp.gradle.PropertyUtils.booleanProvider
import static org.kordamp.gradle.PropertyUtils.fileProvider
import static org.kordamp.gradle.PropertyUtils.listProvider
import static org.kordamp.gradle.PropertyUtils.setProvider
import static org.kordamp.gradle.PropertyUtils.stringProvider
import static org.kordamp.gradle.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@PackageScope
@CompileStatic
class SpotbugsImpl extends AbstractQualityFeature implements Spotbugs {
    final RegularFileProperty includeFilterFile
    final RegularFileProperty excludeFilterFile
    final RegularFileProperty excludeBugsFilterFile
    final Property<String> effort
    final Property<String> reportLevel
    final Property<String> report
    final ListProperty<String> visitors
    final ListProperty<String> omitVisitors
    final ListProperty<String> extraArgs
    final ListProperty<String> jvmArgs
    final Property<Boolean> showProgress
    final SetProperty<String> excludes
    final SetProperty<String> includes

    private ResolvedSpotbugs resolved

    SpotbugsImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
        super(project, ownerConfig, parentConfig)

        includeFilterFile = project.objects.fileProperty()
        excludeFilterFile = project.objects.fileProperty()
        excludeBugsFilterFile = project.objects.fileProperty()
        effort = project.objects.property(String)
        reportLevel = project.objects.property(String)
        report = project.objects.property(String)
        visitors = project.objects.listProperty(String).convention([])
        omitVisitors = project.objects.listProperty(String).convention([])
        extraArgs = project.objects.listProperty(String).convention([])
        jvmArgs = project.objects.listProperty(String).convention([])
        showProgress = project.objects.property(Boolean)
        excludes = project.objects.setProperty(String).convention([])
        includes = project.objects.setProperty(String).convention([])
    }

    ResolvedSpotbugs asResolved() {
        if (!resolved) {
            resolved = new ResolvedSpotbugsImpl(project.providers,
                parentConfig?.asResolved()?.quality?.spotbugs,
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
    static class ResolvedSpotbugsImpl extends AbstractResolvedFeature implements ResolvedSpotbugs {
        final Provider<Boolean> enabled
        final Provider<Boolean> ignoreFailures
        final Provider<String> toolVersion
        final Provider<Set<String>> excludedSourceSets
        final Provider<File> includeFilterFile
        final Provider<File> excludeFilterFile
        final Provider<File> excludeBugsFilterFile
        final Provider<String> effort
        final Provider<String> reportLevel
        final Provider<String> report
        final Provider<List<String>> visitors
        final Provider<List<String>> omitVisitors
        final Provider<List<String>> extraArgs
        final Provider<List<String>> jvmArgs
        final Provider<Boolean> showProgress
        final Provider<Set<String>> excludes
        final Provider<Set<String>> includes

        private final ResolvedSpotbugs parent
        private final SpotbugsImpl self
        private ResolvedAggregate aggregate

        ResolvedSpotbugsImpl(ProviderFactory providers, ResolvedSpotbugs parent, SpotbugsImpl self) {
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
                '4.0.2')

            excludedSourceSets = setProvider(providers,
                parent?.excludedSourceSets,
                self.excludedSourceSets,
                [] as Set)

            includeFilterFile = fileProvider(providers,
                parent?.includeFilterFile,
                self.includeFilterFile,
                resolveFile('includeFilter'))

            excludeFilterFile = fileProvider(providers,
                parent?.includeFilterFile,
                self.includeFilterFile,
                resolveFile('excludeFilter'))

            excludeBugsFilterFile = fileProvider(providers,
                parent?.includeFilterFile,
                self.includeFilterFile,
                resolveFile('excludeBugsFilter'))

            effort = stringProvider(providers,
                parent?.toolVersion,
                self.toolVersion,
                'max')

            reportLevel = stringProvider(providers,
                parent?.toolVersion,
                self.toolVersion,
                'high')

            report = stringProvider(providers,
                parent?.toolVersion,
                self.toolVersion,
                'html')

            visitors = listProvider(providers,
                parent?.visitors,
                self.visitors,
                [])

            omitVisitors = listProvider(providers,
                parent?.omitVisitors,
                self.omitVisitors,
                [])

            extraArgs = listProvider(providers,
                parent?.extraArgs,
                self.extraArgs,
                [])

            jvmArgs = listProvider(providers,
                parent?.jvmArgs,
                self.jvmArgs,
                [])

            showProgress = booleanProvider(providers,
                parent?.showProgress,
                self.showProgress,
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
                includeFilterFile    : includeFilterFile.get(),
                excludeFilterFile    : excludeFilterFile.get(),
                excludeBugsFilterFile: excludeBugsFilterFile.get(),
                excludes             : excludes.get(),
                includes             : includes.get(),
                effort               : effort.get(),
                reportLevel          : reportLevel.get(),
                report               : report.get(),
                visitors             : visitors.get(),
                omitVisitors         : omitVisitors.get(),
                extraArgs            : extraArgs.get(),
                jvmArgs              : jvmArgs.get(),
                showProgress         : showProgress.get()
            ])

            if (isRoot()) {
                map.aggregate = getAggregate().toMap()
            }

            new LinkedHashMap<>('spotbugs': map)
        }

        private File resolveFile(String basename) {
            File file = project.rootProject.file("config/spotbugs/${project.name}-${basename}.xml")
            if (!file.exists()) {
                file = project.rootProject.file("config/spotbugs/${basename}.xml")
            }
            file
        }
    }
}
