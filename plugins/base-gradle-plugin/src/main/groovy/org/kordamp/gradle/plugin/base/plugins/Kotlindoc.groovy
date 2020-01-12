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

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.CollectionUtils
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
class Kotlindoc extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.kotlindoc'
    static final String KOTLIN_JVM_PLUGIN_ID = 'org.jetbrains.kotlin.jvm'

    private final Set<String> PLATFORMS = ['Common', 'JVM', 'JS', 'Native'] as Set
    private final Set<String> FORMATS = ['html', 'javadoc', 'html-as-java', 'markdown', 'gfm', 'jekyll'] as Set

    String moduleName = ""
    List<String> outputFormats = ['html']
    File outputDirectory
    List<Object> includes = []
    List<Object> samples = []
    int jdkVersion
    String cacheRoot
    String languageVersion
    String apiVersion
    boolean includeNonPublic = false
    boolean skipDeprecated = false
    boolean reportUndocumented = true
    boolean skipEmptyPackages = true
    boolean noStdlibLink = false
    Set<String> impliedPlatforms = [] as Set
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

    Kotlindoc(ProjectConfigurationExtension config, Project project) {
        super(config, project)
        aggregate = new Aggregate(config, project)
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        List<Map<String, Map<String, String>>> lms = []
        sourceLinks.sourceLinks.each { lm ->
            if (!lm.empty) lms << new LinkedHashMap<String, Map<String, String>>([(lm.url): new LinkedHashMap<String, String>([
                url   : lm.url,
                path  : lm.path,
                suffix: lm.suffix
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
        map.moduleName = moduleName
        map.outputDirectory = outputDirectory
        map.outputFormats = outputFormats
        map.includes = includes
        map.samples = samples
        map.jdkVersion = jdkVersion
        map.cacheRoot = cacheRoot
        map.languageVersion = languageVersion
        map.samapiVersionples = apiVersion
        map.includeNonPublic = includeNonPublic
        map.skipDeprecated = skipDeprecated
        map.reportUndocumented = reportUndocumented
        map.skipEmptyPackages = skipEmptyPackages
        map.noStdlibLink = noStdlibLink
        map.impliedPlatforms = impliedPlatforms
        map.sourceLinks = lms
        map.externalDocumentationLinks = edls
        map.packageOptions = pos

        if (isRoot()) {
            map.putAll(aggregate.toMap())
        }

        new LinkedHashMap<>('kotlindoc': map)
    }

    void normalize() {
        if (!impliedPlatforms) {
            impliedPlatforms << 'JVM'
        }

        if (!enabledSet) {
            if (isRoot()) {
                if (project.childProjects.isEmpty()) {
                    setEnabled(project.pluginManager.hasPlugin(KOTLIN_JVM_PLUGIN_ID) && isApplied())
                } else {
                    setEnabled(project.childProjects.values().any { p -> p.pluginManager.hasPlugin(KOTLIN_JVM_PLUGIN_ID) && isApplied()})
                }
            } else {
                setEnabled(project.pluginManager.hasPlugin(KOTLIN_JVM_PLUGIN_ID) && isApplied())
            }
        }
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

    void sourceLinks(Action<? super SourceLinkSet> action) {
        action.execute(sourceLinks)
    }

    void sourceLinks(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SourceLinkSet) Closure action) {
        ConfigureUtil.configure(action, sourceLinks)
    }

    void externalDocumentationLinks(Action<? super ExternalDocumentationLinkSet> action) {
        action.execute(externalDocumentationLinks)
    }

    void externalDocumentationLinks(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ExternalDocumentationLinkSet) Closure action) {
        ConfigureUtil.configure(action, externalDocumentationLinks)
    }

    void packageOptions(Action<? super PackageOptionSet> action) {
        action.execute(packageOptions)
    }

    void packageOptions(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PackageOptionSet) Closure action) {
        ConfigureUtil.configure(action, packageOptions)
    }

    void aggregate(Action<? super Aggregate> action) {
        action.execute(aggregate)
    }

    void aggregate(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Aggregate) Closure action) {
        ConfigureUtil.configure(action, aggregate)
    }

    void copyInto(Kotlindoc copy) {
        super.copyInto(copy)
        copy.@replaceJavadoc = replaceJavadoc
        copy.@replaceJavadocSet = replaceJavadocSet
        copy.@includeNonPublic = includeNonPublic
        copy.@includeNonPublicSet = includeNonPublicSet
        copy.@skipDeprecated = skipDeprecated
        copy.@skipDeprecatedSet = skipDeprecatedSet
        copy.@reportUndocumented = reportUndocumented
        copy.@reportUndocumentedSet = reportUndocumentedSet
        copy.@skipEmptyPackages = skipEmptyPackages
        copy.@skipEmptyPackagesSet = skipEmptyPackagesSet
        copy.@noStdlibLink = noStdlibLink
        copy.@noStdlibLinkSet = noStdlibLinkSet
        copy.moduleName = moduleName
        copy.outputFormats = new ArrayList<>(outputFormats)
        copy.outputDirectory = outputDirectory
        copy.jdkVersion = jdkVersion
        copy.cacheRoot = cacheRoot
        copy.languageVersion = languageVersion
        copy.apiVersion = apiVersion
        copy.impliedPlatforms.addAll(impliedPlatforms)
        copy.includes = new ArrayList<>(includes)
        copy.samples = new ArrayList<>(samples)
        sourceLinks.copyInto(copy.sourceLinks)
        externalDocumentationLinks.copyInto(copy.externalDocumentationLinks)
        packageOptions.copyInto(copy.packageOptions)
        aggregate.copyInto(copy.aggregate)
    }

    static void merge(Kotlindoc o1, Kotlindoc o2) {
        o2.normalize()
        AbstractFeature.merge(o1, o2)
        o1.setReplaceJavadoc((boolean) (o1.replaceJavadocSet ? o1.replaceJavadoc : o2.replaceJavadoc))
        o1.moduleName = o1.moduleName ?: o2.moduleName
        CollectionUtils.merge(o1.outputFormats, o2?.outputFormats)
        o1.outputDirectory = o1.outputDirectory ?: o1.project.file("${o1.project.buildDir}/docs/kotlindoc")
        o1.jdkVersion = o1.jdkVersion ?: o2.jdkVersion
        o1.cacheRoot = o1.cacheRoot ?: o2.cacheRoot
        o1.languageVersion = o1.languageVersion ?: o2.languageVersion
        o1.apiVersion = o1.apiVersion ?: o2.apiVersion
        o1.setIncludeNonPublic((boolean) (o1.includeNonPublicSet ? o1.includeNonPublic : o2.includeNonPublic))
        o1.setSkipDeprecated((boolean) (o1.skipDeprecatedSet ? o1.skipDeprecated : o2.skipDeprecated))
        o1.setReportUndocumented((boolean) (o1.reportUndocumentedSet ? o1.reportUndocumented : o2.reportUndocumented))
        o1.setSkipEmptyPackages((boolean) (o1.skipEmptyPackagesSet ? o1.skipEmptyPackages : o2.skipEmptyPackages))
        o1.setNoStdlibLink((boolean) (o1.noStdlibLinkSet ? o1.noStdlibLink : o2.noStdlibLink))
        CollectionUtils.merge(o1.impliedPlatforms, o2?.impliedPlatforms)
        CollectionUtils.merge(o1.includes, o2?.includes)
        CollectionUtils.merge(o1.samples, o2?.samples)
        SourceLinkSet.merge(o1.sourceLinks, o2.sourceLinks)
        ExternalDocumentationLinkSet.merge(o1.externalDocumentationLinks, o2.externalDocumentationLinks)
        PackageOptionSet.merge(o1.packageOptions, o2.packageOptions)
        o1.aggregate.merge(o2.aggregate)
        o1.normalize()
    }

    void postMerge() {
        outputDirectory = outputDirectory ?: project.file("${project.buildDir}/docs/kotlindoc")
        jdkVersion = jdkVersion ?: 6
    }

    List<String> validate(ProjectConfigurationExtension extension) {
        List<String> errors = []

        if (!enabled) return errors

        impliedPlatforms.each { platform ->
            if (!PLATFORMS.contains(platform)) {
                errors << "Platform '$platform' is not supported".toString()
            }
        }

        outputFormats.each { format ->
            if (!FORMATS.contains(format)) {
                errors << "Output format '$format' is not supported".toString()
            }
        }

        errors
    }

    @CompileStatic
    @Canonical
    @ToString(includeNames = true)
    static class SourceLinkSet {
        final List<SourceLink> sourceLinks = []

        void sourceLink(Action<? super SourceLink> action) {
            SourceLink sourceLink = new SourceLink()
            action.execute(sourceLink)
            sourceLinks << sourceLink
        }

        void sourceLink(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SourceLink) Closure action) {
            SourceLink sourceLink = new SourceLink()
            ConfigureUtil.configure(action, sourceLink)
            sourceLinks << sourceLink
        }

        void copyInto(SourceLinkSet sourceLinkSet) {
            sourceLinkSet.sourceLinks.addAll(sourceLinks.collect { it.copyOf() })
        }

        static void merge(SourceLinkSet o1, SourceLinkSet o2) {
            Map<String, SourceLink> a = o1.sourceLinks.collectEntries { [(it.url): it] }
            Map<String, SourceLink> b = o2.sourceLinks.collectEntries { [(it.url): it] }

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
    @Canonical
    @ToString(includeNames = true)
    static class SourceLink {
        String path
        String url
        String suffix

        SourceLink copyOf() {
            SourceLink copy = new SourceLink()
            copyInto(copy)
            copy
        }

        void copyInto(SourceLink copy) {
            copy.path = path
            copy.url = url
            copy.suffix = suffix
        }

        static void merge(SourceLink o1, SourceLink o2) {
            o1.path = o1.path ?: o2?.path
            o1.url = o1.url ?: o2?.url
            o1.suffix = o1.suffix ?: o2?.suffix
        }

        boolean isEmpty() {
            isBlank(path) || isBlank(url)
        }
    }

    @CompileStatic
    @Canonical
    @ToString(includeNames = true)
    static class ExternalDocumentationLinkSet {
        final List<ExternalDocumentationLink> externalDocumentationLinks = []

        void externalDocumentationLink(Action<? super ExternalDocumentationLink> action) {
            ExternalDocumentationLink externalDocumentationLink = new ExternalDocumentationLink()
            action.execute(externalDocumentationLink)
            externalDocumentationLinks << externalDocumentationLink
        }

        void externalDocumentationLink(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ExternalDocumentationLink) Closure action) {
            ExternalDocumentationLink externalDocumentationLink = new ExternalDocumentationLink()
            ConfigureUtil.configure(action, externalDocumentationLink)
            externalDocumentationLinks << externalDocumentationLink
        }

        void copyInto(ExternalDocumentationLinkSet externalDocumentationLinkSet) {
            externalDocumentationLinkSet.externalDocumentationLinks.addAll(externalDocumentationLinks.collect {
                it.copyOf()
            })
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
    @Canonical
    @ToString(includeNames = true)
    static class ExternalDocumentationLink {
        String url
        String packageListUrl

        ExternalDocumentationLink copyOf() {
            ExternalDocumentationLink copy = new ExternalDocumentationLink()
            copyInto(copy)
            copy
        }

        void copyInto(ExternalDocumentationLink copy) {
            copy.url = url
            copy.packageListUrl = packageListUrl
        }

        static void merge(ExternalDocumentationLink o1, ExternalDocumentationLink o2) {
            o1.url = o1.url ?: o2?.url
            o1.packageListUrl = o1.packageListUrl ?: o2?.packageListUrl
        }

        boolean isEmpty() {
            isBlank(url)
        }
    }

    @CompileStatic
    @Canonical
    @ToString(includeNames = true)
    static class PackageOptionSet {
        final List<PackageOption> packageOptions = []

        void packageOption(Action<? super PackageOption> action) {
            PackageOption packageOption = new PackageOption()
            action.execute(packageOption)
            packageOptions << packageOption
        }

        void packageOption(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PackageOption) Closure action) {
            PackageOption packageOption = new PackageOption()
            ConfigureUtil.configure(action, packageOption)
            packageOptions << packageOption
        }

        void copyInto(PackageOptionSet packageOptionSet) {
            packageOptionSet.packageOptions.addAll(packageOptions.collect { it.copyOf() })
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
    @Canonical
    @ToString(includeNames = true)
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

        PackageOption copyOf() {
            PackageOption copy = new PackageOption()
            copyInto(copy)
            copy
        }

        void copyInto(PackageOption copy) {
            copy.prefix = prefix
            copy.@includeNonPublic = includeNonPublic
            copy.@includeNonPublicSet = includeNonPublicSet
            copy.@reportUndocumented = reportUndocumented
            copy.@reportUndocumentedSet = reportUndocumentedSet
            copy.@skipDeprecated = skipDeprecated
            copy.@skipDeprecatedSet = skipDeprecatedSet
            copy.@suppress = suppress
            copy.@suppressSet = suppressSet
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

        void copyInto(Aggregate copy) {
            copy.@enabled = this.@enabled
            copy.@fast = this.@fast
            copy.@replaceJavadoc = this.@replaceJavadoc
            copy.excludedProjects.addAll(excludedProjects)
        }

        Aggregate copyOf() {
            Aggregate copy = new Aggregate(config, project)
            copyInto(copy)
            copy
        }

        Aggregate merge(Aggregate other) {
            Aggregate copy = copyOf()
            copy.enabled = copy.@enabled != null ? copy.getEnabled() : other.getEnabled()
            copy.fast = copy.@fast != null ? copy.getFast() : other.getFast()
            copy.replaceJavadoc = copy.@replaceJavadoc != null ? copy.getReplaceJavadoc() : other.getReplaceJavadoc()
            copy
        }
    }
}
