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
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

import static java.util.Collections.unmodifiableSet

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
interface Kotlindoc extends Feature {
    String PLUGIN_ID = 'org.kordamp.gradle.kotlindoc'
    String KOTLIN_JVM_PLUGIN_ID = 'org.jetbrains.kotlin.jvm'

    Set<String> PLATFORMS = unmodifiableSet(['Common', 'JVM', 'JS', 'Native'] as Set)
    Set<String> FORMATS = unmodifiableSet(['html', 'javadoc', 'html-as-java', 'markdown', 'gfm', 'jekyll'] as Set)

    Property<String> getModuleName()

    SetProperty<String> getOutputFormats()

    RegularFileProperty getOutputDirectory()

    ListProperty<Object> getIncludes()

    ListProperty<Object> getSamples()

    Property<Integer> getJdkVersion()

    Property<String> getCacheRoot()

    Property<String> getLanguageVersion()

    Property<String> getApiVersion()

    Property<Boolean> getIncludeNonPublic()

    Property<Boolean> getSkipDeprecated()

    Property<Boolean> getReportUndocumented()

    Property<Boolean> getSkipEmptyPackages()

    Property<Boolean> getNoStdlibLink()

    SetProperty<String> getImpliedPlatforms()

    void sourceLinks(Action<? super SourceLinkSet> action)

    void sourceLinks(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SourceLinkSet) Closure<Void> action)

    void externalDocumentationLinks(Action<? super ExternalDocumentationLinkSet> action)

    void externalDocumentationLinks(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ExternalDocumentationLinkSet) Closure<Void> action)

    void packageOptions(Action<? super PackageOptionSet> action)

    void packageOptions(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PackageOptionSet) Closure<Void> action)

    void aggregate(Action<? super Aggregate> action)

    void aggregate(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Aggregate) Closure<Void> action)

    @CompileStatic
    interface SourceLinkSet {
        void sourceLink(Action<? super SourceLink> action)

        void sourceLink(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SourceLink) Closure<Void> action)
    }

    @CompileStatic
    interface SourceLink {
        Property<String> getPath()

        Property<String> getUrl()

        Property<String> getSuffix()
    }

    @CompileStatic
    interface ExternalDocumentationLinkSet {
        void externalDocumentationLink(Action<? super ExternalDocumentationLink> action)

        void externalDocumentationLink(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ExternalDocumentationLink) Closure<Void> action)
    }

    @CompileStatic
    interface ExternalDocumentationLink {
        Property<String> getUrl()

        Property<String> getPackageListUrl()
    }

    @CompileStatic
    interface PackageOptionSet {
        void packageOption(Action<? super PackageOption> action)

        void packageOption(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PackageOption) Closure<Void> action)
    }

    @CompileStatic
    interface PackageOption {
        Property<String> getPrefix()

        Property<Boolean> getIncludeNonPublic()

        Property<Boolean> getReportUndocumented()

        Property<Boolean> getSkipDeprecated()

        Property<Boolean> getSuppress()
    }

    @CompileStatic
    interface Aggregate extends Feature {
        Property<Boolean> getFast()

        Property<Boolean> getReplaceJavadoc()

        SetProperty<Project> getExcludedProjects()

        void excludeProject(Project project)
    }
}
