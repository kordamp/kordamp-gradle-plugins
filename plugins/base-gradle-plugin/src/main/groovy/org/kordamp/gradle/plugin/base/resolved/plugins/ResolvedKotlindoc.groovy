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
package org.kordamp.gradle.plugin.base.resolved.plugins

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Provider

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@CompileStatic
interface ResolvedKotlindoc extends ResolvedFeature {
    String PLUGIN_ID = 'org.kordamp.gradle.kotlindoc'

    Provider<String> getModuleName()

    Provider<Set<String>> getOutputFormats()

    Provider<File> getOutputDirectory()

    Provider<List<Object>> getIncludes()

    Provider<List<Object>> getSamples()

    Provider<Integer> getJdkVersion()

    Provider<String> getCacheRoot()

    Provider<String> getLanguageVersion()

    Provider<String> getApiVersion()

    Provider<Boolean> getIncludeNonPublic()

    Provider<Boolean> getSkipDeprecated()

    Provider<Boolean> getReportUndocumented()

    Provider<Boolean> getSkipEmptyPackages()

    Provider<Boolean> getNoStdlibLink()

    Provider<Set<String>> getImpliedPlatforms()

    ResolvedSourceLinkSet getSourceLinks()

    ResolvedExternalDocumentationLinkSet getResolvedExternalDocumentationLinks()

    ResolvedPackageOptionSet getResolvedPackageOptions()

    ResolvedAggregate getAggregate()

    @CompileStatic
    interface ResolvedSourceLinkSet {
        Map<String, Map<String, Object>> toMap()

        boolean isEmpty()

        void forEach(Action<? super ResolvedSourceLink> action)

        void forEach(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ResolvedSourceLink) Closure<Void> action)
    }

    @CompileStatic
    interface ResolvedSourceLink {
        Provider<String> getPath()

        Provider<String> getUrl()

        Provider<String> getSuffix()
    }

    @CompileStatic
    interface ResolvedExternalDocumentationLinkSet {
        Map<String, Map<String, Object>> toMap()

        boolean isEmpty()

        void forEach(Action<? super ResolvedExternalDocumentationLink> action)

        void forEach(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ResolvedExternalDocumentationLink) Closure<Void> action)
    }

    @CompileStatic
    interface ResolvedExternalDocumentationLink {
        Provider<String> getUrl()

        Provider<String> getPackageListUrl()
    }

    @CompileStatic
    interface ResolvedPackageOptionSet {
        Map<String, Map<String, Object>> toMap()

        boolean isEmpty()

        void forEach(Action<? super ResolvedPackageOption> action)

        void forEach(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ResolvedPackageOption) Closure<Void> action)
    }

    @CompileStatic
    interface ResolvedPackageOption {
        Provider<String> getPrefix()

        Provider<Boolean> getIncludeNonPublic()

        Provider<Boolean> getReportUndocumented()

        Provider<Boolean> getSkipDeprecated()

        Provider<Boolean> getSuppress()
    }

    @CompileStatic
    interface ResolvedAggregate extends ResolvedFeature {
        Provider<Boolean> getFast()

        Provider<Boolean> getReplaceJavadoc()

        Provider<Set<Project>> getExcludedProjects()
    }
}
