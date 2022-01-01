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
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.SetProperty
import org.gradle.api.resources.TextResource
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.plugin.base.model.GroovydocOptions
import org.kordamp.gradle.plugin.base.plugins.Groovydoc
import org.kordamp.gradle.plugin.base.resolved.model.ResolvedGroovydocOptions
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedGroovydoc
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedGroovydoc.ResolvedAggregate

import static org.kordamp.gradle.PropertyUtils.booleanProvider
import static org.kordamp.gradle.PropertyUtils.objectProvider
import static org.kordamp.gradle.PropertyUtils.setProvider
import static org.kordamp.gradle.PropertyUtils.stringProvider
import static org.kordamp.gradle.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@PackageScope
@CompileStatic
class GroovydocImpl extends AbstractFeature implements Groovydoc {
    final Property<Boolean> replaceJavadoc
    final SetProperty<String> excludes
    final SetProperty<String> includes
    final GroovydocOptionsImpl options
    final AggregateImpl aggregate

    private ResolvedGroovydoc resolved

    GroovydocImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
        super(project, ownerConfig, parentConfig)

        replaceJavadoc = project.objects.property(Boolean)
        excludes = project.objects.setProperty(String).convention([])
        includes = project.objects.setProperty(String).convention([])
        options = new GroovydocOptionsImpl(project, ownerConfig, parentConfig)
        aggregate = new AggregateImpl(project, ownerConfig, parentConfig)
    }

    @Override
    void normalize() {
        if (!enabled.present) {
            if (isRoot()) {
                if (project.childProjects.isEmpty()) {
                    enabled.set(project.pluginManager.hasPlugin('groovy') && isApplied())
                } else {
                    enabled.set(project.childProjects.values().any { p -> p.pluginManager.hasPlugin('groovy') && isApplied(p) })
                }
            } else {
                enabled.set(project.pluginManager.hasPlugin('groovy') && isApplied())
            }
        }

        boolean resolvedReplaceJavadoc = false
        Property<Boolean> parentReplaceJavadoc = parentConfig?.docs?.groovydoc?.replaceJavadoc
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
    void options(Action<? super GroovydocOptions> action) {
        action.execute(options)
    }

    @Override
    void options(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GroovydocOptions) Closure<Void> action) {
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

    ResolvedGroovydoc asResolved() {
        if (!resolved) {
            resolved = new ResolvedGroovydocImpl(project.providers,
                parentConfig?.asResolved()?.docs?.groovydoc,
                this)
        }
        resolved
    }

    @PackageScope
    @CompileStatic
    static class ResolvedGroovydocImpl extends AbstractResolvedFeature implements ResolvedGroovydoc {
        final Provider<Boolean> enabled
        final Provider<Boolean> replaceJavadoc
        final Provider<Set<String>> excludes
        final Provider<Set<String>> includes

        private final ResolvedGroovydoc parent
        private final GroovydocImpl self
        private ResolvedAggregate aggregate

        ResolvedGroovydocImpl(ProviderFactory providers, ResolvedGroovydoc parent, GroovydocImpl self) {
            super(self.project)
            this.parent = parent
            this.self = self

            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                true)

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
        ResolvedGroovydocOptions getOptions() {
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
                replaceJavadoc: replaceJavadoc.get(),
                excludes      : excludes.get(),
                includes      : includes.get(),
            ])
            map.putAll(getOptions().toMap())

            if (isRoot()) {
                map.aggregate = getAggregate().toMap()
            }

            new LinkedHashMap<>('groovydoc': map)
        }
    }

    @PackageScope
    @CompileStatic
    static class GroovydocOptionsImpl implements GroovydocOptions {
        final Property<String> windowTitle
        final Property<String> docTitle
        final Property<String> header
        final Property<String> footer
        final Property<TextResource> overviewText
        final Property<Boolean> noTimestamp
        final Property<Boolean> noVersionStamp
        final Property<Boolean> includePrivate
        final Property<Boolean> use
        final SetProperty<org.gradle.api.tasks.javadoc.Groovydoc.Link> links

        private final Project project
        private final ProjectConfigurationExtensionImpl ownerConfig
        private final ProjectConfigurationExtensionImpl parentConfig
        private ResolvedGroovydocOptions resolved

        GroovydocOptionsImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
            this.project = project
            this.ownerConfig = ownerConfig
            this.parentConfig = parentConfig

            windowTitle = project.objects.property(String)
            docTitle = project.objects.property(String)
            header = project.objects.property(String)
            footer = project.objects.property(String)
            overviewText = project.objects.property(TextResource)
            noTimestamp = project.objects.property(Boolean)
            noVersionStamp = project.objects.property(Boolean)
            includePrivate = project.objects.property(Boolean)
            use = project.objects.property(Boolean)
            links = project.objects.setProperty(org.gradle.api.tasks.javadoc.Groovydoc.Link)
        }

        ResolvedGroovydocOptions asResolved() {
            if (!resolved) {
                resolved = new ResolvedGroovydocOptionsImpl(project.providers,
                    parentConfig?.asResolved()?.docs?.groovydoc?.options,
                    this)
            }
            resolved
        }

        @Override
        void link(String url, String... packages) {
            if (isNotBlank(url) && packages?.length > 0) {
                links << new org.gradle.api.tasks.javadoc.Groovydoc.Link(url, packages)
            }
        }
    }

    @PackageScope
    @CompileStatic
    static class ResolvedGroovydocOptionsImpl implements ResolvedGroovydocOptions {
        final Provider<String> windowTitle
        final Provider<String> docTitle
        final Provider<String> header
        final Provider<String> footer
        final Provider<TextResource> overviewText
        final Provider<Boolean> noTimestamp
        final Provider<Boolean> noVersionStamp
        final Provider<Boolean> includePrivate
        final Provider<Boolean> use
        final Provider<Set<org.gradle.api.tasks.javadoc.Groovydoc.Link>> links

        ResolvedGroovydocOptionsImpl(ProviderFactory providers, ResolvedGroovydocOptions parent, GroovydocOptionsImpl self) {
            windowTitle = stringProvider(providers, parent?.windowTitle, self.windowTitle, "${self.project.name} ${self.project.version}")
            docTitle = stringProvider(providers, parent?.docTitle, self.docTitle, "${self.project.name} ${self.project.version}")
            header = stringProvider(providers, parent?.header, self.header, "${self.project.name} ${self.project.version}")
            footer = stringProvider(providers, parent?.footer, self.footer, '')
            overviewText = objectProvider(providers, parent?.overviewText, self.overviewText, null)
            noTimestamp = booleanProvider(providers, parent?.noTimestamp, self.noTimestamp, false)
            noVersionStamp = booleanProvider(providers, parent?.noVersionStamp, self.noVersionStamp, false)
            includePrivate = booleanProvider(providers, parent?.includePrivate, self.includePrivate, false)
            use = booleanProvider(providers, parent?.use, self.use, false)
            links = setProvider(providers, parent?.links, self.links, defaultLinks(self.project))
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            List<Map<String, String>> list = []
            links.get().each { link ->
                list << [(link.url): link.packages.join(', ')]
            }

            Map<String, Object> map = new LinkedHashMap<>([
                windowTitle   : windowTitle.get(),
                docTitle      : docTitle.get(),
                header        : header.get(),
                footer        : footer.get(),
                overviewText  : overviewText.get()?.asFile(),
                noTimestamp   : noTimestamp.get(),
                noVersionStamp: noVersionStamp.get(),
                includePrivate: includePrivate.get(),
                use           : use.get(),
                links         : list
            ])

            new LinkedHashMap<>('options': map)
        }

        private Set<org.gradle.api.tasks.javadoc.Groovydoc.Link> defaultLinks(Project project) {
            Set<org.gradle.api.tasks.javadoc.Groovydoc.Link> links = []

            links.add(link(resolveJavadocLinks(project.findProperty('targetCompatibility')), 'java.', 'javax.', 'org.xml.', 'org.w3c.'))
            links.add(link(resolveJavadocLinks(project.findProperty('sourceCompatibility')), 'java.', 'javax.', 'org.xml.', 'org.w3c.'))
            links.add(link(resolveJavadocLinks(project.findProperty('release')), 'java.', 'javax.', 'org.xml.', 'org.w3c.'))
            links.add(link('http://docs.groovy-lang.org/2.5.11/html/api/', 'groovy.', 'org.codehaus.groovy.', 'org.apache.groovy.'))

            links
        }

        private org.gradle.api.tasks.javadoc.Groovydoc.Link link(String url, String... packages) {
            new org.gradle.api.tasks.javadoc.Groovydoc.Link(url, packages)
        }

        private String resolveJavadocLinks(Object jv) {
            JavaVersion javaVersion = JavaVersion.current()

            if (jv instanceof JavaVersion) {
                javaVersion = (JavaVersion) jv
            } else if (jv != null) {
                javaVersion = JavaVersion.toVersion(jv)
            }

            if (javaVersion.isJava11Compatible()) {
                return "https://docs.oracle.com/en/java/javase/${javaVersion.majorVersion}/docs/api/".toString()
            }
            return "https://docs.oracle.com/javase/${javaVersion.majorVersion}/docs/api/"
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
