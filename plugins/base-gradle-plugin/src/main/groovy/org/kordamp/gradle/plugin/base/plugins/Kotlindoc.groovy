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
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.util.CollectionUtils
import org.kordamp.gradle.util.ConfigureUtil

import static org.kordamp.gradle.util.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
class Kotlindoc extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.kotlindoc'
    static final String KOTLIN_JVM_PLUGIN_ID = 'org.jetbrains.kotlin.jvm'

    File outputDirectory
    boolean includeNonPublic = false
    boolean skipDeprecated = false
    boolean reportUndocumented = true
    boolean skipEmptyPackages = true
    String displayName
    List<Object> includes = []
    List<Object> samples = []
    int jdkVersion
    boolean noStdlibLink = false
    boolean noJdkLink = false

    final SourceLinkSet sourceLinks = new SourceLinkSet()
    final ExternalDocumentationLinkSet externalDocumentationLinks = new ExternalDocumentationLinkSet()
    final PackageOptionSet packageOptions = new PackageOptionSet()
    final Aggregate aggregate

    boolean replaceJavadoc = false

    private boolean replaceJavadocSet
    private boolean includeNonPublicSet
    private boolean skipDeprecatedSet
    private boolean reportUndocumentedSet
    private boolean skipEmptyPackagesSet
    private boolean noStdlibLinkSet
    private boolean noJdkLinkSet

    Kotlindoc(ProjectConfigurationExtension config, Project project) {
        super(config, project, PLUGIN_ID)
        aggregate = new Aggregate(config, project)
    }

    @Override
    protected AbstractFeature getParentFeature() {
        return project.rootProject.extensions.getByType(ProjectConfigurationExtension).docs.kotlindoc
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        List<Map<String, Map<String, String>>> lms = []
        sourceLinks.sourceLinks.each { lm ->
            if (!lm.empty) lms << new LinkedHashMap<String, Map<String, String>>([(lm.remoteUrl): new LinkedHashMap<String, String>([
                url   : lm.remoteUrl,
                path  : lm.localDirectory,
                suffix: lm.remoteLineSuffix
            ])])
        }

        List<Map<String, Map<String, String>>> edls = []
        externalDocumentationLinks.externalDocumentationLinks.each { el ->
            if (!el.empty) edls << new LinkedHashMap<String, Map<String, String>>([(el.url): new LinkedHashMap<String, String>([
                packageListUrl: el.packageListUrl
            ])])
        }

        List<Map<String, Map<String, String>>> pos = []
        packageOptions.packageOptions.each { po ->
            if (!po.empty) pos << new LinkedHashMap<String, Map<String, String>>([(po.prefix): new LinkedHashMap<String, String>([
                includeNonPublic  : po.includeNonPublic.toString(),
                reportUndocumented: po.reportUndocumented.toString(),
                skipDeprecated    : po.skipDeprecated.toString(),
                suppress          : po.suppress.toString()
            ])])
        }

        map.replaceJavadoc = replaceJavadoc
        map.displayName = displayName
        map.outputDirectory = outputDirectory
        map.includes = includes
        map.samples = samples
        map.jdkVersion = jdkVersion
        map.includeNonPublic = includeNonPublic
        map.skipDeprecated = skipDeprecated
        map.reportUndocumented = reportUndocumented
        map.skipEmptyPackages = skipEmptyPackages
        map.noStdlibLink = noStdlibLink
        map.noJdkLink = noJdkLink
        map.sourceLinks = lms
        map.externalDocumentationLinks = edls
        map.packageOptions = pos

        if (isRoot()) {
            map.putAll(aggregate.toMap())
        }

        new LinkedHashMap<>('kotlindoc': map)
    }

    @Override
    protected boolean hasBasePlugin(Project project) {
        project.pluginManager.hasPlugin(KOTLIN_JVM_PLUGIN_ID)
    }

    void setReplaceJavadoc(boolean replaceJavadoc) {
        this.replaceJavadoc = replaceJavadoc
        this.replaceJavadocSet = true
    }

    boolean isReplaceJavadocSet() {
        this.replaceJavadocSet
    }

    void setIncludeNonPublic(boolean includeNonPublic) {
        this.includeNonPublic = includeNonPublic
        this.includeNonPublicSet = true
    }

    boolean isIncludeNonPublicSet() {
        this.includeNonPublicSet
    }

    void setSkipDeprecated(boolean skipDeprecated) {
        this.skipDeprecated = skipDeprecated
        this.skipDeprecatedSet = true
    }

    boolean isSkipDeprecatedSet() {
        this.skipDeprecatedSet
    }

    void setReportUndocumented(boolean reportUndocumented) {
        this.reportUndocumented = reportUndocumented
        this.reportUndocumentedSet = true
    }

    boolean isReportUndocumentedSet() {
        this.reportUndocumentedSet
    }

    void setSkipEmptyPackages(boolean skipEmptyPackages) {
        this.skipEmptyPackages = skipEmptyPackages
        this.skipEmptyPackagesSet = true
    }

    boolean isSkipEmptyPackagesSet() {
        this.skipEmptyPackagesSet
    }

    void setNoStdlibLink(boolean noStdlibLink) {
        this.noStdlibLink = noStdlibLink
        this.noStdlibLinkSet = true
    }

    boolean isNoStdlibLinkSet() {
        this.noStdlibLinkSet
    }

    void setNoJdkLink(boolean noJdkLink) {
        this.noJdkLink = noJdkLink
        this.noJdkLinkSet = true
    }

    boolean isNoJdkLinkSet() {
        this.noJdkLink
    }

    void sourceLinks(Action<? super SourceLinkSet> action) {
        action.execute(sourceLinks)
    }

    void sourceLinks(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SourceLinkSet) Closure<Void> action) {
        ConfigureUtil.configure(action, sourceLinks)
    }

    void externalDocumentationLinks(Action<? super ExternalDocumentationLinkSet> action) {
        action.execute(externalDocumentationLinks)
    }

    void externalDocumentationLinks(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ExternalDocumentationLinkSet) Closure<Void> action) {
        ConfigureUtil.configure(action, externalDocumentationLinks)
    }

    void packageOptions(Action<? super PackageOptionSet> action) {
        action.execute(packageOptions)
    }

    void packageOptions(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PackageOptionSet) Closure<Void> action) {
        ConfigureUtil.configure(action, packageOptions)
    }

    void aggregate(Action<? super Aggregate> action) {
        action.execute(aggregate)
    }

    void aggregate(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Aggregate) Closure<Void> action) {
        ConfigureUtil.configure(action, aggregate)
    }

    static void merge(Kotlindoc o1, Kotlindoc o2) {
        o2.normalize()
        AbstractFeature.merge(o1, o2)
        o1.setReplaceJavadoc((boolean) (o1.replaceJavadocSet ? o1.replaceJavadoc : o2.replaceJavadoc))
        o1.displayName = o1.displayName ?: o2.displayName
        o1.outputDirectory = o1.outputDirectory ?: o1.project.file("${o1.project.buildDir}/docs/kotlindoc")
        o1.jdkVersion = o1.jdkVersion ?: o2.jdkVersion
        o1.setIncludeNonPublic((boolean) (o1.includeNonPublicSet ? o1.includeNonPublic : o2.includeNonPublic))
        o1.setSkipDeprecated((boolean) (o1.skipDeprecatedSet ? o1.skipDeprecated : o2.skipDeprecated))
        o1.setReportUndocumented((boolean) (o1.reportUndocumentedSet ? o1.reportUndocumented : o2.reportUndocumented))
        o1.setSkipEmptyPackages((boolean) (o1.skipEmptyPackagesSet ? o1.skipEmptyPackages : o2.skipEmptyPackages))
        o1.setNoStdlibLink((boolean) (o1.noStdlibLinkSet ? o1.noStdlibLink : o2.noStdlibLink))
        o1.setNoJdkLink((boolean) (o1.noJdkLinkSet ? o1.noJdkLink : o2.noJdkLink))
        o1.includes = CollectionUtils.merge(o1.includes, o2?.includes, false)
        o1.samples = CollectionUtils.merge(o1.samples, o2?.samples, false)
        SourceLinkSet.merge(o1.sourceLinks, o2.sourceLinks)
        ExternalDocumentationLinkSet.merge(o1.externalDocumentationLinks, o2.externalDocumentationLinks)
        PackageOptionSet.merge(o1.packageOptions, o2.packageOptions)
        Aggregate.merge(o1.aggregate, o2.aggregate)
        o1.normalize()
    }

    void postMerge() {
        super.postMerge()
        outputDirectory = outputDirectory ?: project.file("${project.buildDir}/docs/kotlindoc")
        jdkVersion = jdkVersion ?: 6
        if (replaceJavadoc) config.docs.javadoc.enabled = false
    }

    @CompileStatic
    static class SourceLinkSet {
        final List<SourceLink> sourceLinks = []

        void sourceLink(Action<? super SourceLink> action) {
            SourceLink sourceLink = new SourceLink()
            action.execute(sourceLink)
            sourceLinks << sourceLink
        }

        void sourceLink(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SourceLink) Closure<Void> action) {
            SourceLink sourceLink = new SourceLink()
            ConfigureUtil.configure(action, sourceLink)
            sourceLinks << sourceLink
        }

        static void merge(SourceLinkSet o1, SourceLinkSet o2) {
            Map<String, SourceLink> a = o1.sourceLinks.collectEntries { [(it.remoteUrl): it] }
            Map<String, SourceLink> b = o2.sourceLinks.collectEntries { [(it.remoteUrl): it] }

            a.each { k, sourceLink ->
                SourceLink.merge(sourceLink, b.remove(k))
            }
            a.putAll(b)
            o1.sourceLinks.clear()
            o1.sourceLinks.addAll(a.values())
        }

        List<SourceLink> resolveSourceLinks() {
            sourceLinks.findAll { !it.isEmpty() }
        }
    }

    @CompileStatic
    static class SourceLink {
        String localDirectory
        String remoteUrl
        String remoteLineSuffix

        static void merge(SourceLink o1, SourceLink o2) {
            o1.localDirectory = o1.localDirectory ?: o2?.localDirectory
            o1.remoteUrl = o1.remoteUrl ?: o2?.remoteUrl
            o1.remoteLineSuffix = o1.remoteLineSuffix ?: o2?.remoteLineSuffix
        }

        boolean isEmpty() {
            isBlank(localDirectory) || isBlank(remoteUrl)
        }
    }

    @CompileStatic
    static class ExternalDocumentationLinkSet {
        final List<ExternalDocumentationLink> externalDocumentationLinks = []

        void externalDocumentationLink(Action<? super ExternalDocumentationLink> action) {
            ExternalDocumentationLink externalDocumentationLink = new ExternalDocumentationLink()
            action.execute(externalDocumentationLink)
            externalDocumentationLinks << externalDocumentationLink
        }

        void externalDocumentationLink(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ExternalDocumentationLink) Closure<Void> action) {
            ExternalDocumentationLink externalDocumentationLink = new ExternalDocumentationLink()
            ConfigureUtil.configure(action, externalDocumentationLink)
            externalDocumentationLinks << externalDocumentationLink
        }

        static void merge(ExternalDocumentationLinkSet o1, ExternalDocumentationLinkSet o2) {
            Map<String, ExternalDocumentationLink> a = o1.externalDocumentationLinks.collectEntries {
                [(it.url): it]
            }
            Map<String, ExternalDocumentationLink> b = o2.externalDocumentationLinks.collectEntries {
                [(it.url): it]
            }

            a.each { k, externalDocumentationLink ->
                ExternalDocumentationLink.merge(externalDocumentationLink, b.remove(k))
            }
            a.putAll(b)
            o1.externalDocumentationLinks.clear()
            o1.externalDocumentationLinks.addAll(a.values())
        }

        List<ExternalDocumentationLink> resolveExternalDocumentationLinks() {
            externalDocumentationLinks.findAll { !it.isEmpty() }
        }
    }

    @CompileStatic
    static class ExternalDocumentationLink {
        String url
        String packageListUrl

        static void merge(ExternalDocumentationLink o1, ExternalDocumentationLink o2) {
            o1.url = o1.url ?: o2?.url
            o1.packageListUrl = o1.packageListUrl ?: o2?.packageListUrl
        }

        boolean isEmpty() {
            isBlank(url)
        }
    }

    @CompileStatic
    static class PackageOptionSet {
        final List<PackageOption> packageOptions = []

        void packageOption(Action<? super PackageOption> action) {
            PackageOption packageOption = new PackageOption()
            action.execute(packageOption)
            packageOptions << packageOption
        }

        void packageOption(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PackageOption) Closure<Void> action) {
            PackageOption packageOption = new PackageOption()
            ConfigureUtil.configure(action, packageOption)
            packageOptions << packageOption
        }

        static void merge(PackageOptionSet o1, PackageOptionSet o2) {
            Map<String, PackageOption> a = o1.packageOptions.collectEntries { [(it.prefix): it] }
            Map<String, PackageOption> b = o2.packageOptions.collectEntries { [(it.prefix): it] }

            a.each { k, packageOption ->
                PackageOption.merge(packageOption, b.remove(k))
            }
            a.putAll(b)
            o1.packageOptions.clear()
            o1.packageOptions.addAll(a.values())
        }

        List<PackageOption> resolvePackageOptions() {
            packageOptions.findAll { !it.isEmpty() }
        }
    }

    @CompileStatic
    static class PackageOption {
        String prefix
        boolean includeNonPublic = false
        boolean reportUndocumented = true
        boolean skipDeprecated = false
        boolean suppress = false

        private boolean includeNonPublicSet
        private boolean reportUndocumentedSet
        private boolean skipDeprecatedSet
        private boolean suppressSet

        void setIncludeNonPublic(boolean includeNonPublic) {
            this.includeNonPublic = includeNonPublic
            this.includeNonPublicSet = true
        }

        boolean isIncludeNonPublicSet() {
            this.includeNonPublicSet
        }

        void setReportUndocumented(boolean reportUndocumented) {
            this.reportUndocumented = reportUndocumented
            this.reportUndocumentedSet = true
        }

        boolean isReportUndocumentedSet() {
            this.reportUndocumentedSet
        }

        void setSkipDeprecated(boolean skipDeprecated) {
            this.skipDeprecated = skipDeprecated
            this.skipDeprecatedSet = true
        }

        boolean isSkipDeprecatedSet() {
            this.skipDeprecatedSet
        }

        void setSuppress(boolean suppress) {
            this.suppress = suppress
            this.suppressSet = true
        }

        boolean isSuppressSet() {
            this.suppressSet
        }

        static void merge(PackageOption o1, PackageOption o2) {
            o1.prefix = o1.prefix ?: o2?.prefix
            o1.setIncludeNonPublic((boolean) (o1.includeNonPublicSet ? o1.includeNonPublic : o2.includeNonPublic))
            o1.setReportUndocumented((boolean) (o1.reportUndocumentedSet ? o1.reportUndocumented : o2.reportUndocumented))
            o1.setSkipDeprecated((boolean) (o1.skipDeprecatedSet ? o1.skipDeprecated : o2.skipDeprecated))
            o1.setSuppress((boolean) (o1.suppressSet ? o1.suppress : o2.suppress))
        }

        boolean isEmpty() {
            isBlank(prefix)
        }
    }

    @CompileStatic
    static class Aggregate {
        Boolean enabled
        Boolean fast
        Boolean replaceJavadoc
        final Set<Project> excludedProjects = new LinkedHashSet<>()

        private final ProjectConfigurationExtension config
        private final Project project

        Aggregate(ProjectConfigurationExtension config, Project project) {
            this.config = config
            this.project = project
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<String, Object>()

            map.enabled = getEnabled()
            map.fast = getFast()
            map.replaceJavadoc = getReplaceJavadoc()
            map.excludedProjects = excludedProjects

            new LinkedHashMap<>('aggregate': map)
        }

        boolean getEnabled() {
            this.@enabled == null || this.@enabled
        }

        boolean getFast() {
            this.@fast == null || this.@fast
        }

        boolean getReplaceJavadoc() {
            this.@replaceJavadoc != null && this.@replaceJavadoc
        }

        static Aggregate merge(Aggregate o1, Aggregate o2) {
            o1.enabled = o1.@enabled != null ? o1.getEnabled() : o2.getEnabled()
            o1.fast = o1.@fast != null ? o1.getFast() : o2.getFast()
            o1.replaceJavadoc = o1.@replaceJavadoc != null ? o1.getReplaceJavadoc() : o2.getReplaceJavadoc()
            o1
        }
    }
}
