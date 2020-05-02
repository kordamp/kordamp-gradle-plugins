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
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.kordamp.gradle.plugin.base.plugins.ErrorProne
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedErrorProne

import static org.kordamp.gradle.PropertyUtils.booleanProvider
import static org.kordamp.gradle.PropertyUtils.stringProvider

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@PackageScope
@CompileStatic
class ErrorProneImpl extends AbstractFeature implements ErrorProne {
    final Property<Boolean> disableAllChecks
    final Property<Boolean> allErrorsAsWarnings
    final Property<Boolean> allDisabledChecksAsWarnings
    final Property<Boolean> disableWarningsInGeneratedCode
    final Property<Boolean> ignoreUnknownCheckNames
    final Property<Boolean> ignoreSuppressionAnnotations
    final Property<Boolean> compilingTestOnlyCode
    final Property<String> excludedPaths
    final Property<String> errorProneVersion
    final Property<String> errorProneJavacVersion
    private ResolvedErrorProne resolved

    ErrorProneImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
        super(project, ownerConfig, parentConfig)

        disableAllChecks = project.objects.property(Boolean)
        allErrorsAsWarnings = project.objects.property(Boolean)
        allDisabledChecksAsWarnings = project.objects.property(Boolean)
        disableWarningsInGeneratedCode = project.objects.property(Boolean)
        ignoreUnknownCheckNames = project.objects.property(Boolean)
        ignoreSuppressionAnnotations = project.objects.property(Boolean)
        compilingTestOnlyCode = project.objects.property(Boolean)
        excludedPaths = project.objects.property(String)
        errorProneVersion = project.objects.property(String)
        errorProneJavacVersion = project.objects.property(String)
    }

    ResolvedErrorProne asResolved() {
        if (!resolved) {
            resolved = new ResolvedErrorProneImpl(project.providers,
                parentConfig?.asResolved()?.quality?.errorprone,
                this)
        }
        resolved
    }

    @PackageScope
    @CompileStatic
    static class ResolvedErrorProneImpl extends AbstractResolvedFeature implements ResolvedErrorProne {
        final Provider<Boolean> enabled
        final Provider<Boolean> disableAllChecks
        final Provider<Boolean> allErrorsAsWarnings
        final Provider<Boolean> allDisabledChecksAsWarnings
        final Provider<Boolean> disableWarningsInGeneratedCode
        final Provider<Boolean> ignoreUnknownCheckNames
        final Provider<Boolean> ignoreSuppressionAnnotations
        final Provider<Boolean> compilingTestOnlyCode
        final Provider<String> excludedPaths
        final Provider<String> errorProneVersion
        final Provider<String> errorProneJavacVersion

        private final ResolvedErrorProne parent
        private final ErrorProneImpl self

        ResolvedErrorProneImpl(ProviderFactory providers, ResolvedErrorProne parent, ErrorProneImpl self) {
            super(self.project)
            this.parent = parent
            this.self = self

            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                true)

            disableAllChecks = booleanProvider(providers,
                parent?.disableAllChecks,
                self.disableAllChecks,
                false)

            allErrorsAsWarnings = booleanProvider(providers,
                parent?.allErrorsAsWarnings,
                self.allErrorsAsWarnings,
                false)

            allDisabledChecksAsWarnings = booleanProvider(providers,
                parent?.allDisabledChecksAsWarnings,
                self.allDisabledChecksAsWarnings,
                false)

            disableWarningsInGeneratedCode = booleanProvider(providers,
                parent?.disableWarningsInGeneratedCode,
                self.disableWarningsInGeneratedCode,
                true)

            ignoreUnknownCheckNames = booleanProvider(providers,
                parent?.ignoreUnknownCheckNames,
                self.ignoreUnknownCheckNames,
                false)

            ignoreSuppressionAnnotations = booleanProvider(providers,
                parent?.ignoreSuppressionAnnotations,
                self.ignoreSuppressionAnnotations,
                false)

            compilingTestOnlyCode = booleanProvider(providers,
                parent?.compilingTestOnlyCode,
                self.compilingTestOnlyCode,
                false)

            excludedPaths = stringProvider(providers,
                parent?.excludedPaths,
                self.excludedPaths,
                null)

            errorProneVersion = stringProvider(providers,
                parent?.errorProneVersion,
                self.errorProneVersion,
                '2.3.4')

            errorProneJavacVersion = stringProvider(providers,
                parent?.errorProneJavacVersion,
                self.errorProneJavacVersion,
                '9+181-r4173-1')
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<>([
                enabled                       : enabled.get(),
                disableAllChecks              : disableAllChecks.get(),
                allErrorsAsWarnings           : allErrorsAsWarnings.get(),
                allDisabledChecksAsWarnings   : allDisabledChecksAsWarnings.get(),
                disableWarningsInGeneratedCode: disableWarningsInGeneratedCode.get(),
                ignoreUnknownCheckNames       : ignoreUnknownCheckNames.get(),
                ignoreSuppressionAnnotations  : ignoreSuppressionAnnotations.get(),
                compilingTestOnlyCode         : compilingTestOnlyCode.get(),
                excludedPaths                 : excludedPaths.get(),
                errorProneVersion             : errorProneVersion.get(),
                errorProneJavacVersion        : errorProneJavacVersion.get()
            ])

            new LinkedHashMap<>('errorprone': map)
        }
    }
}
