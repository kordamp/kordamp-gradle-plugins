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
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.SetProperty
import org.kordamp.gradle.plugin.base.plugins.SourceXref
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedSourceXref

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
class SourceXrefImpl extends AbstractAggregatingFeature implements SourceXref {
    final Property<String> templateDir
    final Property<String> inputEncoding
    final Property<String> outputEncoding
    final Property<String> windowTitle
    final Property<String> docTitle
    final Property<String> bottom
    final Property<String> stylesheet
    final Property<JavaVersion> javaVersion
    final SetProperty<String> excludes
    final SetProperty<String> includes

    private ResolvedSourceXref resolved

    SourceXrefImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
        super(project, ownerConfig, parentConfig)

        templateDir = project.objects.property(String)
        inputEncoding = project.objects.property(String)
        outputEncoding = project.objects.property(String)
        windowTitle = project.objects.property(String)
        docTitle = project.objects.property(String)
        bottom = project.objects.property(String)
        stylesheet = project.objects.property(String)
        javaVersion = project.objects.property(JavaVersion)
        excludes = project.objects.setProperty(String).convention([])
        includes = project.objects.setProperty(String).convention([])
    }

    ResolvedSourceXref asResolved() {
        if (!resolved) {
            resolved = new ResolvedSourceXrefImpl(project.providers,
                parentConfig?.asResolved()?.docs?.sourceXref,
                this)
        }
        resolved
    }

    @Override
    void setJavaVersion(String javaVersion) {
        if (isNotBlank(javaVersion)) {
            this.javaVersion.set(JavaVersion.toVersion(javaVersion))
        }
    }

    @Override
    void include(String str) {
        if (isNotBlank(str)) includes << str
    }

    @Override
    void exclude(String str) {
        if (isNotBlank(str)) excludes << str
    }

    @PackageScope
    @CompileStatic
    static class ResolvedSourceXrefImpl extends AbstractResolvedFeature implements ResolvedSourceXref {
        final Provider<Boolean> enabled
        final Provider<String> templateDir
        final Provider<String> inputEncoding
        final Provider<String> outputEncoding
        final Provider<String> windowTitle
        final Provider<String> docTitle
        final Provider<String> bottom
        final Provider<String> stylesheet
        final Provider<JavaVersion> javaVersion
        final Provider<Set<String>> excludes
        final Provider<Set<String>> includes

        private final ResolvedSourceXref parent
        private final SourceXrefImpl self
        private ResolvedAggregate aggregate

        ResolvedSourceXrefImpl(ProviderFactory providers, ResolvedSourceXref parent, SourceXrefImpl self) {
            super(self.project)
            this.parent = parent
            this.self = self

            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                true)

            templateDir = stringProvider(providers,
                parent?.templateDir,
                self.templateDir,
                '')

            inputEncoding = stringProvider(providers,
                parent?.inputEncoding,
                self.inputEncoding,
                System.getProperty('file.encoding'))

            outputEncoding = stringProvider(providers,
                parent?.outputEncoding,
                self.outputEncoding,
                'UTF-8')

            windowTitle = stringProvider(providers,
                parent?.windowTitle,
                self.windowTitle,
                '')

            docTitle = stringProvider(providers,
                parent?.docTitle,
                self.docTitle,
                '')

            bottom = stringProvider(providers,
                parent?.bottom,
                self.bottom,
                '')

            stylesheet = stringProvider(providers,
                parent?.stylesheet,
                self.stylesheet,
                '')

            javaVersion = objectProvider(providers,
                parent?.javaVersion,
                self.javaVersion,
                JavaVersion.current())

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
                enabled       : enabled.get(),
                templateDir   : templateDir.get(),
                inputEncoding : inputEncoding.get(),
                outputEncoding: outputEncoding.get(),
                windowTitle   : windowTitle.get(),
                docTitle      : docTitle.get(),
                bottom        : bottom.get(),
                stylesheet    : stylesheet.get(),
                javaVersion   : javaVersion.get().name(),
                excludes      : excludes.get(),
                includes      : includes.get()
            ])

            if (isRoot()) {
                map.aggregate = getAggregate().toMap()
            }

            new LinkedHashMap<>('sourceXref': map)
        }
    }
}
