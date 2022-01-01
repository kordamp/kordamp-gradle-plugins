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
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.SetProperty
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.plugin.base.plugins.Kotlindoc
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedKotlindoc
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedKotlindoc.ResolvedAggregate

import static org.kordamp.gradle.PropertyUtils.booleanProvider
import static org.kordamp.gradle.PropertyUtils.fileProvider
import static org.kordamp.gradle.PropertyUtils.intProvider
import static org.kordamp.gradle.PropertyUtils.listProvider
import static org.kordamp.gradle.PropertyUtils.setProvider
import static org.kordamp.gradle.PropertyUtils.stringProvider

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@PackageScope
@CompileStatic
class KotlindocImpl extends AbstractFeature implements Kotlindoc {
    final Property<String> moduleName
    final SetProperty<String> outputFormats
    final RegularFileProperty outputDirectory
    final ListProperty<Object> includes
    final ListProperty<Object> samples
    final Property<Integer> jdkVersion
    final Property<String> cacheRoot
    final Property<String> languageVersion
    final Property<String> apiVersion
    final Property<Boolean> includeNonPublic
    final Property<Boolean> skipDeprecated
    final Property<Boolean> reportUndocumented
    final Property<Boolean> skipEmptyPackages
    final Property<Boolean> noStdlibLink
    final SetProperty<String> impliedPlatforms
    final AggregateImpl aggregate

    private ResolvedKotlindoc resolved

    KotlindocImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
        super(project, ownerConfig, parentConfig)

        moduleName = project.objects.property(String)
        outputFormats = project.objects.setProperty(String).convention([])
        outputDirectory = project.objects.fileProperty()
        includes = project.objects.listProperty(Object).convention([])
        samples = project.objects.listProperty(Object).convention([])
        jdkVersion = project.objects.property(Integer)
        cacheRoot = project.objects.property(String)
        languageVersion = project.objects.property(String)
        apiVersion = project.objects.property(String)
        includeNonPublic = project.objects.property(Boolean)
        skipDeprecated = project.objects.property(Boolean)
        reportUndocumented = project.objects.property(Boolean)
        skipEmptyPackages = project.objects.property(Boolean)
        noStdlibLink = project.objects.property(Boolean)
        impliedPlatforms = project.objects.setProperty(String).convention([])

        aggregate = new AggregateImpl(project, ownerConfig, parentConfig)
    }

    @Override
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

        aggregate.normalize()
    }

    @Override
    void aggregate(Action<? super Aggregate> action) {
        action.execute(aggregate)
    }

    @Override
    void aggregate(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Aggregate) Closure<Void> action) {
        ConfigureUtil.configure(action, aggregate)
    }

    ResolvedKotlindoc asResolved() {
        if (!resolved) {
            resolved = new ResolvedKotlindocImpl(project.providers,
                parentConfig?.asResolved()?.docs?.kotlindoc,
                this)
        }
        resolved
    }

    @PackageScope
    @CompileStatic
    static class ResolvedKotlindocImpl extends AbstractResolvedFeature implements ResolvedKotlindoc {
        final Provider<Boolean> enabled
        final Provider<String> moduleName
        final Provider<Set<String>> outputFormats
        final Provider<File> outputDirectory
        final Provider<List<Object>> includes
        final Provider<List<Object>> samples
        final Provider<Integer> jdkVersion
        final Provider<String> cacheRoot
        final Provider<String> languageVersion
        final Provider<String> apiVersion
        final Provider<Boolean> includeNonPublic
        final Provider<Boolean> skipDeprecated
        final Provider<Boolean> reportUndocumented
        final Provider<Boolean> skipEmptyPackages
        final Provider<Boolean> noStdlibLink
        final Provider<Set<String>> impliedPlatforms

        private final ResolvedKotlindoc parent
        private final KotlindocImpl self
        private ResolvedAggregate aggregate

        ResolvedKotlindocImpl(ProviderFactory providers, ResolvedKotlindoc parent, KotlindocImpl self) {
            super(self.project)
            this.parent = parent
            this.self = self

            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                true)

            moduleName = stringProvider(providers, parent?.moduleName, self.moduleName, '')
            outputFormats = setProvider(providers, parent?.outputFormats, self.outputFormats, ['html'] as Set)
            outputDirectory = fileProvider(providers, parent?.outputDirectory, self.outputDirectory, null)
            includes = listProvider(providers, parent?.includes, self.includes, [])
            samples = listProvider(providers, parent?.samples, self.samples, [])
            jdkVersion = intProvider(providers, parent?.jdkVersion, self.jdkVersion, 0)
            cacheRoot = stringProvider(providers, parent?.cacheRoot, self.cacheRoot, '')
            languageVersion = stringProvider(providers, parent?.languageVersion, self.languageVersion, '')
            apiVersion = stringProvider(providers, parent?.apiVersion, self.apiVersion, '')
            includeNonPublic = booleanProvider(providers, parent?.includeNonPublic, self.includeNonPublic, false)
            skipDeprecated = booleanProvider(providers, parent?.skipDeprecated, self.skipDeprecated, true)
            reportUndocumented = booleanProvider(providers, parent?.reportUndocumented, self.reportUndocumented, true)
            skipEmptyPackages = booleanProvider(providers, parent?.skipEmptyPackages, self.skipEmptyPackages, false)
            noStdlibLink = booleanProvider(providers, parent?.noStdlibLink, self.noStdlibLink, false)
            impliedPlatforms = setProvider(providers, parent?.impliedPlatforms, self.impliedPlatforms, ['JVM'] as Set)

        }

        @Override
        ResolvedAggregate getAggregate() {
            if (!aggregate) {
                aggregate = new ResolvedAggregateImpl(project.providers,
                    parent?.aggregate,
                    self.aggregate)
            }
            aggregate
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<>([
                enabled           : enabled.get(),
                moduleName        : moduleName.get(),
                outputDirectory   : outputDirectory.get(),
                outputFormats     : outputFormats.get(),
                includes          : includes.get(),
                samples           : samples.get(),
                jdkVersion        : jdkVersion.get(),
                cacheRoot         : cacheRoot.get(),
                languageVersion   : languageVersion.get(),
                apiVersion        : apiVersion.get(),
                includeNonPublic  : includeNonPublic.get(),
                skipDeprecated    : skipDeprecated.get(),
                reportUndocumented: reportUndocumented.get(),
                skipEmptyPackages : skipEmptyPackages.get(),
                noStdlibLink      : noStdlibLink.get(),
                impliedPlatforms  : impliedPlatforms.get()
            ])

            if (isRoot()) {
                map.aggregate = getAggregate().toMap()
            }

            new LinkedHashMap<>('kotlindoc': map)
        }
    }

    @PackageScope
    @CompileStatic
    static class AggregateImpl extends AbstractFeature implements Aggregate {
        final Property<Boolean> fast
        final Property<Boolean> replaceJavadoc
        final SetProperty<Project> excludedProjects

        AggregateImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
            super(project, ownerConfig, parentConfig)
            fast = project.objects.property(Boolean)
            replaceJavadoc = project.objects.property(Boolean)
            excludedProjects = project.objects.setProperty(Project)
        }

        @Override
        void excludeProject(Project project) {
            if (project) excludedProjects.add(project)
        }

        @Override
        void normalize() {
            boolean resolvedReplaceJavadoc = false
            Property<Boolean> parentReplaceJavadoc = parentConfig?.docs?.kotlindoc?.aggregate?.replaceJavadoc
            if (replaceJavadoc.present) {
                resolvedReplaceJavadoc = replaceJavadoc.get()
            } else if (parentReplaceJavadoc?.present) {
                resolvedReplaceJavadoc = parentReplaceJavadoc.get()
            }
            if (resolvedReplaceJavadoc) {
                ownerConfig.docs.javadoc.aggregate.enabled.set(false)
            }
        }
    }

    @PackageScope
    @CompileStatic
    static class ResolvedAggregateImpl extends AbstractResolvedFeature implements ResolvedAggregate {
        final Provider<Boolean> enabled
        final Provider<Boolean> fast
        final Provider<Boolean> replaceJavadoc
        final Provider<Set<Project>> excludedProjects

        private final AggregateImpl self

        ResolvedAggregateImpl(ProviderFactory providers, ResolvedAggregate parent, AggregateImpl self) {
            super(self.project)
            this.self = self

            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                true)

            fast = booleanProvider(providers,
                parent?.fast,
                self.fast,
                true)

            replaceJavadoc = booleanProvider(providers,
                parent?.replaceJavadoc,
                self.replaceJavadoc,
                false)

            excludedProjects = setProvider(providers,
                parent?.excludedProjects,
                self.excludedProjects,
                [] as Set)
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<>([
                enabled         : enabled.get(),
                fast            : fast.get(),
                replaceJavadoc  : replaceJavadoc.get(),
                excludedProjects: excludedProjects.get()
            ])

            new LinkedHashMap<>('aggregate': map)
        }
    }

    /*
    @PackageScope
    @CompileStatic
    static class PackageOptionImpl extends AbstractFeature implements PackageOption {

        private ResolvedPackageOption resolved

        PackageOptionImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
            super(project, ownerConfig, parentConfig)
        }

        ResolvedPackageOption asResolved() {
            if (!resolved) {
                resolved = new ResolvedPackageOptionImpl(project.providers,
                    parentConfig?.asResolved()?.docs?.kotlindoc?.packageOption,
                    this)
            }
            resolved
        }
    }

    @PackageScope
    @CompileStatic
    static class ResolvedPackageOptionImpl extends AbstractResolvedFeature implements ResolvedPackageOption {
        final Provider<Boolean> enabled
        final Provider<Boolean> logging
        final Provider<Boolean> aggregate
        final Provider<String> baseDir

        ResolvedPackageOptionImpl(ProviderFactory providers, ResolvedPackageOption parent, PackageOptionImpl self) {
            super(self.project)

            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                true)
        }
    }
     */
}
