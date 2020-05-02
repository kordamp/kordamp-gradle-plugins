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
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.SetProperty
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.plugin.base.model.ScaladocOptions
import org.kordamp.gradle.plugin.base.plugins.Scaladoc
import org.kordamp.gradle.plugin.base.resolved.model.ResolvedScaladocOptions
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedScaladoc
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedScaladoc.ResolvedAggregate

import static org.kordamp.gradle.PropertyUtils.booleanProvider
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
class ScaladocImpl extends AbstractFeature implements Scaladoc {
    final Property<String> title
    final Property<Boolean> replaceJavadoc
    final SetProperty<String> excludes
    final SetProperty<String> includes
    final ScaladocOptionsImpl options
    final AggregateImpl aggregate

    private ResolvedScaladoc resolved

    ScaladocImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
        super(project, ownerConfig, parentConfig)

        title = project.objects.property(String)
        replaceJavadoc = project.objects.property(Boolean)
        excludes = project.objects.setProperty(String).convention([])
        includes = project.objects.setProperty(String).convention([])
        options = new ScaladocOptionsImpl(project, ownerConfig, parentConfig)
        aggregate = new AggregateImpl(project, ownerConfig, parentConfig)
    }

    @Override
    void normalize() {
        if (!enabled.present) {
            if (isRoot()) {
                if (project.childProjects.isEmpty()) {
                    enabled.set(project.pluginManager.hasPlugin('scala') && isApplied())
                } else {
                    enabled.set(project.childProjects.values().any { p -> p.pluginManager.hasPlugin('scala') && isApplied(p) })
                }
            } else {
                enabled.set(project.pluginManager.hasPlugin('scala') && isApplied())
            }
        }

        boolean resolvedReplaceJavadoc = false
        Property<Boolean> parentReplaceJavadoc = parentConfig?.docs?.scaladoc?.replaceJavadoc
        if (replaceJavadoc.present) {
            resolvedReplaceJavadoc = replaceJavadoc.get()
        } else if (parentReplaceJavadoc?.present) {
            resolvedReplaceJavadoc = parentReplaceJavadoc.get()
        }
        if (resolvedReplaceJavadoc) {
            ownerConfig.docs.javadoc.enabled.set(false)
        }

        aggregate.normalize()
    }

    @Override
    void include(String str) {
        if (isNotBlank(str)) includes.add(str)
    }

    @Override
    void exclude(String str) {
        if (isNotBlank(str)) excludes.add(str)
    }

    @Override
    void options(Action<? super ScaladocOptions> action) {
        action.execute(options)
    }

    @Override
    void options(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ScaladocOptions) Closure<Void> action) {
        ConfigureUtil.configure(action, options)
    }

    @Override
    void aggregate(Action<? super Aggregate> action) {
        action.execute(aggregate)
    }

    @Override
    void aggregate(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Aggregate) Closure<Void> action) {
        ConfigureUtil.configure(action, aggregate)
    }

    ResolvedScaladoc asResolved() {
        if (!resolved) {
            resolved = new ResolvedScaladocImpl(project.providers,
                parentConfig?.asResolved()?.docs?.scaladoc,
                this)
        }
        resolved
    }

    @PackageScope
    @CompileStatic
    static class ResolvedScaladocImpl extends AbstractResolvedFeature implements ResolvedScaladoc {
        final Provider<Boolean> enabled
        final Provider<String> title
        final Provider<Boolean> replaceJavadoc
        final Provider<Set<String>> excludes
        final Provider<Set<String>> includes

        private final ResolvedScaladoc parent
        private final ScaladocImpl self
        private ResolvedAggregate aggregate

        ResolvedScaladocImpl(ProviderFactory providers, ResolvedScaladoc parent, ScaladocImpl self) {
            super(self.project)
            this.parent = parent
            this.self = self

            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                true)

            title = stringProvider(providers,
                parent?.title,
                self.title,
                "${project.name} ${project.version}")

            replaceJavadoc = booleanProvider(providers,
                parent?.replaceJavadoc,
                self.replaceJavadoc,
                false)

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
        ResolvedScaladocOptions getOptions() {
            self.options.asResolved()
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
                enabled       : enabled.get(),
                title         : title.get(),
                replaceJavadoc: replaceJavadoc.get(),
                excludes      : excludes.get(),
                includes      : includes.get(),
            ])
            map.putAll(getOptions().toMap())

            if (isRoot()) {
                map.aggregate = getAggregate().toMap()
            }

            new LinkedHashMap<>('scaladoc': map)
        }
    }

    @PackageScope
    @CompileStatic
    static class ScaladocOptionsImpl implements ScaladocOptions {
        final Property<String> bottom
        final Property<String> top
        final Property<String> windowTitle
        final Property<String> docTitle
        final Property<String> header
        final Property<String> footer
        final ListProperty<String> additionalParameters
        final Property<Boolean> deprecation
        final Property<Boolean> unchecked

        private final Project project
        private final ProjectConfigurationExtensionImpl ownerConfig
        private final ProjectConfigurationExtensionImpl parentConfig
        private ResolvedScaladocOptions resolved

        ScaladocOptionsImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
            this.project = project
            this.ownerConfig = ownerConfig
            this.parentConfig = parentConfig

            bottom = project.objects.property(String)
            top = project.objects.property(String)
            windowTitle = project.objects.property(String)
            docTitle = project.objects.property(String)
            header = project.objects.property(String)
            footer = project.objects.property(String)
            additionalParameters = project.objects.listProperty(String)
            deprecation = project.objects.property(Boolean)
            unchecked = project.objects.property(Boolean)
        }

        ResolvedScaladocOptions asResolved() {
            if (!resolved) {
                resolved = new ResolvedScaladocOptionsImpl(project.providers,
                    parentConfig?.asResolved()?.docs?.scaladoc?.options,
                    this)
            }
            resolved
        }
    }

    @PackageScope
    @CompileStatic
    static class ResolvedScaladocOptionsImpl implements ResolvedScaladocOptions {
        final Provider<String> bottom
        final Provider<String> top
        final Provider<String> windowTitle
        final Provider<String> docTitle
        final Provider<String> header
        final Provider<String> footer
        final Provider<List<String>> additionalParameters
        final Provider<Boolean> deprecation
        final Provider<Boolean> unchecked

        ResolvedScaladocOptionsImpl(ProviderFactory providers, ResolvedScaladocOptions parent, ScaladocOptionsImpl self) {
            bottom = stringProvider(providers, parent?.bottom, self.bottom, '')
            top = stringProvider(providers, parent?.top, self.top, '')
            windowTitle = stringProvider(providers, parent?.windowTitle, self.windowTitle, "${self.project.name} ${self.project.version}")
            docTitle = stringProvider(providers, parent?.docTitle, self.docTitle, "${self.project.name} ${self.project.version}")
            header = stringProvider(providers, parent?.header, self.header, "${self.project.name} ${self.project.version}")
            footer = stringProvider(providers, parent?.footer, self.footer, '')
            additionalParameters = listProvider(providers, parent?.additionalParameters, self.additionalParameters, [])
            deprecation = booleanProvider(providers, parent?.deprecation, self.deprecation, false)
            unchecked = booleanProvider(providers, parent?.unchecked, self.unchecked, false)
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<>([
                bottom              : bottom.get(),
                top                 : top.get(),
                windowTitle         : windowTitle.get(),
                docTitle            : docTitle.get(),
                header              : header.get(),
                footer              : footer.get(),
                additionalParameters: additionalParameters.get(),
                deprecation         : deprecation.get(),
                unchecked           : unchecked.get()
            ])

            new LinkedHashMap<>('options': map)
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
            Property<Boolean> parentReplaceJavadoc = parentConfig?.docs?.groovydoc?.aggregate?.replaceJavadoc
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
}
