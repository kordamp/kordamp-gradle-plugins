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
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.SetProperty
import org.gradle.external.javadoc.JavadocMemberLevel
import org.gradle.external.javadoc.JavadocOutputLevel
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.plugin.base.model.JavadocOptions
import org.kordamp.gradle.plugin.base.plugins.Javadoc
import org.kordamp.gradle.plugin.base.resolved.model.ResolvedJavadocOptions
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedJavadoc
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedJavadoc.ResolvedAggregate
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedJavadoc.ResolvedAutoLinks

import static org.kordamp.gradle.PropertyUtils.booleanProvider
import static org.kordamp.gradle.PropertyUtils.fileProvider
import static org.kordamp.gradle.PropertyUtils.listProvider
import static org.kordamp.gradle.PropertyUtils.mapProvider
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
class JavadocImpl extends AbstractFeature implements Javadoc {
    final Property<String> title
    final SetProperty<String> excludes
    final SetProperty<String> includes
    final JavadocOptionsImpl options
    final AutoLinksImpl autoLinks
    final AggregateImpl aggregate

    private ResolvedJavadoc resolved

    JavadocImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
        super(project, ownerConfig, parentConfig)

        title = project.objects.property(String)
        excludes = project.objects.setProperty(String).convention([])
        includes = project.objects.setProperty(String).convention([])
        options = new JavadocOptionsImpl(project, ownerConfig, parentConfig)
        autoLinks = new AutoLinksImpl(project, ownerConfig, parentConfig)
        aggregate = new AggregateImpl(project, ownerConfig, parentConfig)
    }

    @Override
    void normalize() {
        if (!enabled.present) {
            if (isRoot()) {
                if (project.childProjects.isEmpty()) {
                    enabled.set(project.pluginManager.hasPlugin('java') && isApplied())
                } else {
                    enabled.set(project.childProjects.values().any { p -> p.pluginManager.hasPlugin('java') && isApplied(p) })
                }
            } else {
                enabled.set(project.pluginManager.hasPlugin('java') && isApplied())
            }
        }
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
    void options(Action<? super JavadocOptions> action) {
        action.execute(options)
    }

    @Override
    void options(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = JavadocOptions) Closure<Void> action) {
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

    @Override
    void autoLinks(Action<? super AutoLinks> action) {
        action.execute(autoLinks)
    }

    @Override
    void autoLinks(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = AutoLinks) Closure<Void> action) {
        ConfigureUtil.configure(action, autoLinks)
    }

    ResolvedJavadoc asResolved() {
        if (!resolved) {
            resolved = new ResolvedJavadocImpl(project.providers,
                parentConfig?.asResolved()?.docs?.javadoc,
                this)
        }
        resolved
    }

    @PackageScope
    @CompileStatic
    static class ResolvedJavadocImpl extends AbstractResolvedFeature implements ResolvedJavadoc {
        final Provider<Boolean> enabled
        final Provider<String> title
        final Provider<Set<String>> excludes
        final Provider<Set<String>> includes

        private final ResolvedJavadoc parent
        private final JavadocImpl self
        private ResolvedAggregate aggregate

        ResolvedJavadocImpl(ProviderFactory providers, ResolvedJavadoc parent, JavadocImpl self) {
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
        ResolvedJavadocOptions getOptions() {
            self.options.asResolved()
        }

        @Override
        ResolvedAutoLinks getAutoLinks() {
            self.autoLinks.asResolved()
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
                enabled : enabled.get(),
                title : title.get(),
                excludes: excludes.get(),
                includes: includes.get(),
            ])
            map.putAll(getOptions().toMap())
            map.putAll(getAutoLinks().toMap())

            if (isRoot()) {
                map.aggregate = getAggregate().toMap()
            }

            new LinkedHashMap<>('javadoc': map)
        }

        /*
        private List<String> resolveLinks(Project project) {
            List<String> links = []

            if (!enabled.get()) return links

            Set<String> cs = new LinkedHashSet<>(configurations.get())
            if (!cs) {
                cs = DEFAULT_CONFIGURATIONS
            }

            for (String cn : cs) {
                Configuration c = project.configurations.findByName(cn)
                c?.dependencies?.each { Dependency dep ->
                    if (dep instanceof ProjectDependency) {
                        ProjectDependency pdep = (ProjectDependency) dep
                        String packageListLoc = calculateLocalJavadocLink(project, pdep.dependencyProject)
                        String extDocUrl = config.release ? calculateRemoteJavadocLink(pdep.group, pdep.name, pdep.version) : packageListLoc
                        links.put(extDocUrl, packageListLoc)
                    } else {
                        String artifactName = "${dep.name}-${dep.version}".toString()
                        if (!isExcluded(artifactName) && dep.name != 'unspecified' &&
                            isNotBlank(dep.group) && isNotBlank(dep.version)) {
                            links << calculateRemoteJavadocLink(dep.group, dep.name, dep.version)
                        }
                    }
                }
            }

            links
        }

        private boolean isExcluded(String artifactName) {
            for (String s : excludes.get()) {
                if (artifactName.matches(s)) {
                    return true
                }
            }
            false
        }

        private String calculateRemoteJavadocLink(String group, String name, String version) {
            if (group == 'javax' && name == 'javaee-api' && version.matches('[567]\\..*')) {
                'https://docs.oracle.com/javaee/' + version[0, 1] + '/api/'
            } else if (group == 'javax' && name == 'javaee-api' && version.startsWith('8')) {
                'https://javaee.github.io/javaee-spec/javadocs/'
            } else if (group == 'org.springframework' && name.startsWith('spring-')) {
                'https://docs.spring.io/spring/docs/' + version + '/javadoc-api/'
            } else if (group == 'org.springframework.boot' && name.startsWith('spring-boot')) {
                'https://docs.spring.io/spring-boot/docs/' + version + '/api/'
            } else if (group == 'org.springframework.security' && name.startsWith('spring-security')) {
                'https://docs.spring.io/spring-security/site/docs/' + version + '/api/'
            } else if (group == 'org.springframework.data' && name == 'spring-data-jpa') {
                'https://docs.spring.io/spring-data/jpa/docs/' + version + '/api/'
            } else if (group == 'org.springframework.webflow' && name == 'spring-webflow') {
                'https://docs.spring.io/spring-webflow/docs/' + version + '/api/'
            } else if (group == 'com.squareup.okio' && version.startsWith('1.')) {
                'https://square.github.io/okio/1.x/' + name + '/'
            } else if (group == 'com.squareup.okhttp3') {
                'https://square.github.io/okhttp/3.x/' + name + '/'
            } else if (group == 'org.hibernate' && name == 'hibernate-core') {
                'https://docs.jboss.org/hibernate/orm/' + version[0, 3] + '/javadocs/'
            } else if ((group == 'org.hibernate' || group == 'org.hibernate.validator') && name == 'hibernate-validator') {
                'https://docs.jboss.org/hibernate/validator/' + version[0, 3] + '/api/'
            } else if (group == 'org.eclipse.jetty') {
                'https://www.eclipse.org/jetty/javadoc/' + version + '/'
            } else if (group == 'org.ow2.asm') {
                'https://asm.ow2.io/javadoc/'
            } else if (group.startsWith('org.apache.tomcat')) {
                'https://tomcat.apache.org/tomcat-' + version[0, 3] + '-doc/api/'
            } else if (getUseJavadocIo()) {
                "https://static.javadoc.io/${group}/${name}/${version}/".toString()
            } else {
                String normalizedGroup = group.replace('.', '/')
                "https://oss.sonatype.org/service/local/repositories/releases/archive/$normalizedGroup/$name/$version/$name-$version-javadoc.jar/!/".toString()
            }
        }

        @CompileDynamic
        private String calculateLocalJavadocLink(Project dependentProject, Project project) {
            ResolvedProjectConfigurationExtension config = project.extensions.getByType(ResolvedProjectConfigurationExtension)

            Task taskDependency = null
            if (config.docs.javadoc.enabled) {
                taskDependency = project.tasks.findByName('javadoc')
            }

            if (config.docs.groovydoc.enabled && config.docs.groovydoc.replaceJavadoc) {
                taskDependency = project.tasks.findByName('groovydoc')
            }

            // Android projects don't have a 'javadoc' task
            if (taskDependency) {
                dependentProject.tasks.findByName('javadoc').dependsOn(taskDependency)
                taskDependency.destinationDir.absolutePath.replace('\\', '/')
            }
        }
         */
    }

    @PackageScope
    @CompileStatic
    static class AutoLinksImpl extends AbstractFeature implements AutoLinks {
        final Property<Boolean> useJavadocIo
        final SetProperty<String> excludes
        final SetProperty<String> configurations
        final MapProperty<String, String> offlineLinks

        private ResolvedAutoLinks resolved

        AutoLinksImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
            super(project, ownerConfig, parentConfig)

            useJavadocIo = project.objects.property(Boolean)
            excludes = project.objects.setProperty(String).convention([])
            configurations = project.objects.setProperty(String).convention([])
            offlineLinks = project.objects.mapProperty(String, String).convention([:])
        }

        ResolvedAutoLinks asResolved() {
            if (!resolved) {
                resolved = new ResolvedAutoLinksImpl(project.providers,
                    parentConfig?.asResolved()?.docs?.javadoc?.autoLinks,
                    this)
            }
            resolved
        }

        @Override
        void exclude(String str) {
            if (isNotBlank(str)) excludes << str.trim()
        }

        @Override
        void offlineLink(String url1, String url2) {
            if (isNotBlank(url1) && isNotBlank(url2)) {
                offlineLinks.put(url1, url2)
            }
        }
    }

    @PackageScope
    @CompileStatic
    static class ResolvedAutoLinksImpl extends AbstractResolvedFeature implements ResolvedAutoLinks {
        private static final Set<String> DEFAULT_CONFIGURATIONS = [
            'api', 'implementation', 'compileOnly', 'annotationProcessor', 'runtimeOnly'
        ] as Set

        final Provider<Boolean> enabled
        final Provider<Boolean> useJavadocIo
        final Provider<Set<String>> excludes
        final Provider<Set<String>> configurations
        final Provider<Map<String, String>> offlineLinks

        ResolvedAutoLinksImpl(ProviderFactory providers, ResolvedAutoLinks parent, AutoLinksImpl self) {
            super(self.project)

            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                true)

            useJavadocIo = booleanProvider(providers,
                parent?.useJavadocIo,
                self.useJavadocIo,
                true)

            excludes = setProvider(providers,
                parent?.excludes,
                self.excludes,
                [] as Set)

            configurations = setProvider(providers,
                parent?.configurations,
                self.configurations,
                DEFAULT_CONFIGURATIONS)

            offlineLinks = mapProvider(providers,
                parent?.offlineLinks,
                self.offlineLinks,
                [:])
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<>([
                enabled       : enabled.get(),
                useJavadocIo  : useJavadocIo.get(),
                excludes      : excludes.get(),
                configurations: configurations.get(),
                offlineLinks  : offlineLinks.get(),
            ])

            new LinkedHashMap<>('autoLinks': map)
        }
    }

    @PackageScope
    @CompileStatic
    static class JavadocOptionsImpl implements JavadocOptions {
        final Property<String> overview
        final Property<JavadocMemberLevel> memberLevel
        final Property<String> doclet
        final ConfigurableFileCollection docletpath
        final Property<String> source
        final ConfigurableFileCollection classpath
        final ConfigurableFileCollection bootClasspath
        final ConfigurableFileCollection extDirs
        final Property<JavadocOutputLevel> outputLevel
        final Property<Boolean> breakIterator
        final Property<String> locale
        final Property<String> encoding
        final ListProperty<String> sourceNames
        final RegularFileProperty destinationDirectory
        final Property<Boolean> use
        final Property<Boolean> version
        final Property<Boolean> author
        final Property<Boolean> splitIndex
        final Property<String> windowTitle
        final Property<String> header
        final Property<String> docTitle
        final Property<String> footer
        final Property<String> bottom
        final Property<String> top
        final ListProperty<String> links
        final Property<Boolean> linkSource
        final MapProperty<String, List> groups
        final Property<Boolean> noDeprecated
        final Property<Boolean> noDeprecatedList
        final Property<Boolean> noSince
        final Property<Boolean> noTree
        final Property<Boolean> noIndex
        final Property<Boolean> noHelp
        final Property<Boolean> noNavBar
        final RegularFileProperty helpFile
        final RegularFileProperty stylesheetFile
        final Property<Boolean> serialWarn
        final Property<String> charSet
        final Property<String> docEncoding
        final Property<Boolean> keyWords
        final ListProperty<String> tags
        final ListProperty<String> taglets
        final ConfigurableFileCollection tagletPath
        final Property<Boolean> docFilesSubDirs
        final ListProperty<String> excludeDocFilesSubDir
        final ListProperty<String> noQualifiers
        final Property<Boolean> noTimestamp
        final Property<Boolean> noComment
        final MapProperty<String, Boolean> booleanOptions
        final MapProperty<String, String> stringOptions
        final MapProperty<String, String> offlineLinks

        private final Project project
        private final ProjectConfigurationExtensionImpl ownerConfig
        private final ProjectConfigurationExtensionImpl parentConfig
        private ResolvedJavadocOptions resolved

        JavadocOptionsImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
            this.project = project
            this.ownerConfig = ownerConfig
            this.parentConfig = parentConfig

            overview = project.objects.property(String)
            memberLevel = project.objects.property(JavadocMemberLevel)
            doclet = project.objects.property(String)
            docletpath = project.objects.fileCollection()
            source = project.objects.property(String)
            classpath = project.objects.fileCollection()
            bootClasspath = project.objects.fileCollection()
            extDirs = project.objects.fileCollection()
            outputLevel = project.objects.property(JavadocOutputLevel)
            breakIterator = project.objects.property(Boolean)
            locale = project.objects.property(String)
            encoding = project.objects.property(String)
            sourceNames = project.objects.listProperty(String).convention([])
            destinationDirectory = project.objects.fileProperty()
            use = project.objects.property(Boolean)
            version = project.objects.property(Boolean)
            author = project.objects.property(Boolean)
            splitIndex = project.objects.property(Boolean)
            windowTitle = project.objects.property(String)
            header = project.objects.property(String)
            docTitle = project.objects.property(String)
            footer = project.objects.property(String)
            bottom = project.objects.property(String)
            top = project.objects.property(String)
            links = project.objects.listProperty(String).convention([])
            linkSource = project.objects.property(Boolean)
            groups = project.objects.mapProperty(String, List).convention([:])
            noDeprecated = project.objects.property(Boolean)
            noDeprecatedList = project.objects.property(Boolean)
            noSince = project.objects.property(Boolean)
            noTree = project.objects.property(Boolean)
            noIndex = project.objects.property(Boolean)
            noHelp = project.objects.property(Boolean)
            noNavBar = project.objects.property(Boolean)
            helpFile = project.objects.fileProperty()
            stylesheetFile = project.objects.fileProperty()
            serialWarn = project.objects.property(Boolean)
            charSet = project.objects.property(String)
            docEncoding = project.objects.property(String)
            keyWords = project.objects.property(Boolean)
            tags = project.objects.listProperty(String).convention([])
            taglets = project.objects.listProperty(String).convention([])
            tagletPath = project.objects.fileCollection()
            docFilesSubDirs = project.objects.property(Boolean)
            excludeDocFilesSubDir = project.objects.listProperty(String).convention([])
            noQualifiers = project.objects.listProperty(String).convention([])
            noTimestamp = project.objects.property(Boolean)
            noComment = project.objects.property(Boolean)
            booleanOptions = project.objects.mapProperty(String, Boolean).convention([:])
            stringOptions = project.objects.mapProperty(String, String).convention([:])
            offlineLinks = project.objects.mapProperty(String, String).convention([:])
        }

        ResolvedJavadocOptions asResolved() {
            if (!resolved) {
                resolved = new ResolvedJavadocOptionsImpl(project.providers,
                    parentConfig?.asResolved()?.docs?.javadoc?.options,
                    this)
            }
            resolved
        }

        @Override
        void link(String url) {
            if (isNotBlank(url)) links.add(url.trim())
        }

        @Override
        void linksOffline(String extDocUrl, String packageListLoc) {
            if (isNotBlank(extDocUrl) && isNotBlank(packageListLoc)) {
                offlineLinks.put(extDocUrl.trim(), packageListLoc.trim())
            }
        }

        @Override
        void addBooleanOption(String key, boolean value) {
            if (isNotBlank(key)) {
                booleanOptions.put(key.trim(), value)
            }
        }

        @Override
        void addStringOption(String key, String value) {
            if (isNotBlank(key)) {
                stringOptions.put(key.trim(), value)
            }
        }
    }

    @PackageScope
    @CompileStatic
    static class ResolvedJavadocOptionsImpl implements ResolvedJavadocOptions {
        final Provider<String> overview
        final Provider<JavadocMemberLevel> memberLevel
        final Provider<String> doclet
        final ConfigurableFileCollection docletpath
        final Provider<String> source
        final ConfigurableFileCollection classpath
        final ConfigurableFileCollection bootClasspath
        final ConfigurableFileCollection extDirs
        final Provider<JavadocOutputLevel> outputLevel
        final Provider<Boolean> breakIterator
        final Provider<String> locale
        final Provider<String> encoding
        final Provider<List<String>> sourceNames
        final Provider<File> destinationDirectory
        final Provider<Boolean> use
        final Provider<Boolean> version
        final Provider<Boolean> author
        final Provider<Boolean> splitIndex
        final Provider<String> windowTitle
        final Provider<String> header
        final Provider<String> docTitle
        final Provider<String> footer
        final Provider<String> bottom
        final Provider<String> top
        final Provider<List<String>> links
        final Provider<Boolean> linkSource
        final Provider<Map<String, List>> groups
        final Provider<Boolean> noDeprecated
        final Provider<Boolean> noDeprecatedList
        final Provider<Boolean> noSince
        final Provider<Boolean> noTree
        final Provider<Boolean> noIndex
        final Provider<Boolean> noHelp
        final Provider<Boolean> noNavBar
        final Provider<File> helpFile
        final Provider<File> stylesheetFile
        final Provider<Boolean> serialWarn
        final Provider<String> charSet
        final Provider<String> docEncoding
        final Provider<Boolean> keyWords
        final Provider<List<String>> tags
        final Provider<List<String>> taglets
        final ConfigurableFileCollection tagletPath
        final Provider<Boolean> docFilesSubDirs
        final Provider<List<String>> excludeDocFilesSubDir
        final Provider<List<String>> noQualifiers
        final Provider<Boolean> noTimestamp
        final Provider<Boolean> noComment
        final Provider<Map<String, String>> offlineLinks
        final Provider<Map<String, Boolean>> booleanOptions
        final Provider<Map<String, String>> stringOptions

        ResolvedJavadocOptionsImpl(ProviderFactory providers, ResolvedJavadocOptions parent, JavadocOptionsImpl self) {
            docletpath = self.project.objects.fileCollection()
            docletpath.from(self.docletpath)
            if (parent?.docletpath) {
                docletpath.from(parent.docletpath)
            }

            classpath = self.project.objects.fileCollection()
            classpath.from(self.classpath)
            if (parent?.classpath) {
                classpath.from(parent.classpath)
            }

            bootClasspath = self.project.objects.fileCollection()
            bootClasspath.from(self.bootClasspath)
            if (parent?.bootClasspath) {
                bootClasspath.from(parent.bootClasspath)
            }

            extDirs = self.project.objects.fileCollection()
            extDirs.from(self.extDirs)
            if (parent?.extDirs) {
                extDirs.from(parent.extDirs)
            }

            tagletPath = self.project.objects.fileCollection()
            tagletPath.from(self.tagletPath)
            if (parent?.tagletPath) {
                tagletPath.from(parent.tagletPath)
            }

            Set<String> javaLinks = []
            javaLinks.add(resolveJavadocLinks(self.project.findProperty('targetCompatibility')))
            javaLinks.add(resolveJavadocLinks(self.project.findProperty('sourceCompatibility')))
            javaLinks.add(resolveJavadocLinks(self.project.findProperty('release')))

            overview = stringProvider(providers, parent?.overview, self.overview, null)
            memberLevel = objectProvider(providers, parent?.memberLevel, self.memberLevel, null)
            doclet = stringProvider(providers, parent?.doclet, self.doclet, null)
            source = stringProvider(providers, parent?.source, self.source, null)
            outputLevel = objectProvider(providers, parent?.outputLevel, self.outputLevel, null)
            breakIterator = booleanProvider(providers, parent?.breakIterator, self.breakIterator, false)
            locale = stringProvider(providers, parent?.locale, self.locale, null)
            encoding = stringProvider(providers, parent?.encoding, self.encoding, 'UTF-8')
            sourceNames = listProvider(providers, parent?.sourceNames, self.sourceNames, [])
            destinationDirectory = fileProvider(providers, parent?.destinationDirectory, self.destinationDirectory, null)
            use = booleanProvider(providers, parent?.use, self.use, true)
            version = booleanProvider(providers, parent?.version, self.version, true)
            author = booleanProvider(providers, parent?.author, self.author, false)
            splitIndex = booleanProvider(providers, parent?.splitIndex, self.splitIndex, true)
            windowTitle = stringProvider(providers, parent?.windowTitle, self.windowTitle, "${self.project.name} ${self.project.version}")
            docTitle = stringProvider(providers, parent?.docTitle, self.docTitle, "${self.project.name} ${self.project.version}")
            header = stringProvider(providers, parent?.header, self.header, "${self.project.name} ${self.project.version}")
            footer = stringProvider(providers, parent?.footer, self.footer, '')
            bottom = stringProvider(providers, parent?.bottom, self.bottom, '')
            top = stringProvider(providers, parent?.top, self.top, '')
            links = listProvider(providers, parent?.links, self.links, javaLinks as List)
            linkSource = booleanProvider(providers, parent?.linkSource, self.linkSource, false)
            groups = mapProvider(providers, parent?.groups, self.groups, [:])
            noDeprecated = booleanProvider(providers, parent?.noDeprecated, self.noDeprecated, false)
            noDeprecatedList = booleanProvider(providers, parent?.noDeprecatedList, self.noDeprecatedList, false)
            noSince = booleanProvider(providers, parent?.noSince, self.noSince, false)
            noTree = booleanProvider(providers, parent?.noTree, self.noTree, false)
            noIndex = booleanProvider(providers, parent?.noIndex, self.noIndex, false)
            noHelp = booleanProvider(providers, parent?.noHelp, self.noHelp, false)
            noNavBar = booleanProvider(providers, parent?.noNavBar, self.noNavBar, false)
            helpFile = fileProvider(providers, parent?.helpFile, self.helpFile, null)
            stylesheetFile = fileProvider(providers, parent?.stylesheetFile, self.stylesheetFile, null)
            serialWarn = booleanProvider(providers, parent?.serialWarn, self.serialWarn, false)
            charSet = stringProvider(providers, parent?.charSet, self.charSet, null)
            docEncoding = stringProvider(providers, parent?.docEncoding, self.docEncoding, null)
            keyWords = booleanProvider(providers, parent?.keyWords, self.keyWords, false)
            tags = listProvider(providers, parent?.tags, self.tags, [])
            taglets = listProvider(providers, parent?.taglets, self.taglets, [])
            docFilesSubDirs = booleanProvider(providers, parent?.docFilesSubDirs, self.docFilesSubDirs, false)
            excludeDocFilesSubDir = listProvider(providers, parent?.excludeDocFilesSubDir, self.excludeDocFilesSubDir, [])
            noQualifiers = listProvider(providers, parent?.noQualifiers, self.noQualifiers, [])
            noTimestamp = booleanProvider(providers, parent?.noTimestamp, self.noTimestamp, false)
            noComment = booleanProvider(providers, parent?.noComment, self.noComment, false)
            offlineLinks = mapProvider(providers, parent?.offlineLinks, self.offlineLinks, [:])
            booleanOptions = mapProvider(providers, parent?.booleanOptions, self.booleanOptions, [:])
            stringOptions = mapProvider(providers, parent?.stringOptions, self.stringOptions, [:])
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<>([
                windowTitle: windowTitle.get(),
                docTitle   : docTitle.get(),
                header     : header.get(),
                encoding   : encoding.get(),
                author     : author.get(),
                version    : version.get(),
                splitIndex : splitIndex.get(),
                use        : use.get(),
                links      : links.get()
            ])

            new LinkedHashMap<>('options': map)
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
        final SetProperty<Project> excludedProjects

        AggregateImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
            super(project, ownerConfig, parentConfig)
            fast = project.objects.property(Boolean)
            excludedProjects = project.objects.setProperty(Project)
        }

        @Override
        void excludeProject(Project project) {
            if (project) excludedProjects.add(project)
        }
    }

    @PackageScope
    @CompileStatic
    static class ResolvedAggregateImpl extends AbstractResolvedFeature implements ResolvedAggregate {
        final Provider<Boolean> enabled
        final Provider<Boolean> fast
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
                excludedProjects: excludedProjects.get()
            ])

            new LinkedHashMap<>('aggregate': map)
        }
    }
}
