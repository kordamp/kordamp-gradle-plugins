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
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.plugin.base.plugins.SourceHtml
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedSourceHtml
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedSourceHtml.ResolvedConversion
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedSourceHtml.ResolvedOverview

import static org.kordamp.gradle.PropertyUtils.booleanProvider
import static org.kordamp.gradle.PropertyUtils.fileProvider
import static org.kordamp.gradle.PropertyUtils.intProvider
import static org.kordamp.gradle.PropertyUtils.stringProvider

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@PackageScope
@CompileStatic
class SourceHtmlImpl extends AbstractFeature implements SourceHtml {
    ConfigurableFileCollection srcDirs
    final ConversionImpl conversion
    final OverviewImpl overview

    private ResolvedSourceHtml resolved

    SourceHtmlImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
        super(project, ownerConfig, parentConfig)

        srcDirs = project.objects.fileCollection()
        conversion = new ConversionImpl(project, ownerConfig, parentConfig)
        overview = new OverviewImpl(project, ownerConfig, parentConfig)
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

    ResolvedSourceHtml asResolved() {
        if (!resolved) {
            resolved = new ResolvedSourceHtmlImpl(project.providers,
                parentConfig?.asResolved()?.docs?.sourceHtml,
                this)
        }
        resolved
    }

    @Override
    void conversion(Action<? super Conversion> action) {
        action.execute(conversion)
    }

    @Override
    void conversion(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Conversion) Closure<Void> action) {
        ConfigureUtil.configure(action, conversion)
    }

    @Override
    void overview(Action<? super Overview> action) {
        action.execute(overview)
    }

    @Override
    void overview(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Overview) Closure<Void> action) {
        ConfigureUtil.configure(action, overview)
    }

    @PackageScope
    @CompileStatic
    static class ResolvedSourceHtmlImpl extends AbstractResolvedFeature implements ResolvedSourceHtml {
        final Provider<Boolean> enabled
        final ConfigurableFileCollection srcDirs

        private final SourceHtmlImpl self

        ResolvedSourceHtmlImpl(ProviderFactory providers, ResolvedSourceHtml parent, SourceHtmlImpl self) {
            super(self.project)
            this.self = self

            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                true)

            srcDirs = self.project.objects.fileCollection()
            srcDirs.from(self.srcDirs)
            if (parent?.srcDirs) {
                srcDirs.from(parent.srcDirs)
            }
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<>([
                enabled: enabled.get(),
                srcDirs: srcDirs.files
            ])
            map.putAll(getConversion().toMap())
            map.putAll(getOverview().toMap())

            new LinkedHashMap<>('sourceHtml': map)
        }

        @Override
        ResolvedConversion getConversion() {
            self.conversion.asResolved()
        }

        @Override
        ResolvedOverview getOverview() {
            self.overview.asResolved()
        }
    }

    @PackageScope
    @CompileStatic
    static class ConversionImpl extends AbstractFeature implements Conversion {
        final ConfigurableFileCollection srcDirs
        final RegularFileProperty destDir
        final Property<String> includes
        final Property<String> outputFormat
        final Property<Integer> tabs
        final Property<String> style
        final Property<String> lineAnchorPrefix
        final Property<String> horizontalAlignment
        final Property<Boolean> showLineNumbers
        final Property<Boolean> showFileName
        final Property<Boolean> showDefaultTitle
        final Property<Boolean> showTableBorder
        final Property<Boolean> includeDocumentHeader
        final Property<Boolean> includeDocumentFooter
        final Property<Boolean> addLineAnchors
        final Property<Boolean> useShortFileName
        final Property<Boolean> overwrite

        private ResolvedConversion resolved

        ConversionImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
            super(project, ownerConfig, parentConfig)

            srcDirs = project.objects.fileCollection()
            destDir = project.objects.fileProperty()
            includes = project.objects.property(String)
            outputFormat = project.objects.property(String)
            tabs = project.objects.property(Integer)
            style = project.objects.property(String)
            lineAnchorPrefix = project.objects.property(String)
            horizontalAlignment = project.objects.property(String)
            showLineNumbers = project.objects.property(Boolean)
            showFileName = project.objects.property(Boolean)
            showDefaultTitle = project.objects.property(Boolean)
            showTableBorder = project.objects.property(Boolean)
            includeDocumentHeader = project.objects.property(Boolean)
            includeDocumentFooter = project.objects.property(Boolean)
            addLineAnchors = project.objects.property(Boolean)
            useShortFileName = project.objects.property(Boolean)
            overwrite = project.objects.property(Boolean)
        }

        ResolvedConversion asResolved() {
            if (!resolved) {
                resolved = new ResolvedConversionImpl(project.providers,
                    parentConfig?.asResolved()?.docs?.sourceHtml?.conversion,
                    this)
            }
            resolved
        }
    }

    @PackageScope
    @CompileStatic
    static class ResolvedConversionImpl implements ResolvedConversion {
        final ConfigurableFileCollection srcDirs
        final Provider<File> destDir
        final Provider<String> includes
        final Provider<String> outputFormat
        final Provider<Integer> tabs
        final Provider<String> style
        final Provider<String> lineAnchorPrefix
        final Provider<String> horizontalAlignment
        final Provider<Boolean> showLineNumbers
        final Provider<Boolean> showFileName
        final Provider<Boolean> showDefaultTitle
        final Provider<Boolean> showTableBorder
        final Provider<Boolean> includeDocumentHeader
        final Provider<Boolean> includeDocumentFooter
        final Provider<Boolean> addLineAnchors
        final Provider<Boolean> useShortFileName
        final Provider<Boolean> overwrite

        ResolvedConversionImpl(ProviderFactory providers, ResolvedConversion parent, ConversionImpl self) {
            destDir = fileProvider(providers,
                parent?.destDir,
                self.destDir,
                self.project.layout.buildDirectory.file('tmp/source-html/conversion').get().asFile)

            srcDirs = self.project.objects.fileCollection()
            srcDirs.from(self.srcDirs)
            if (parent?.srcDirs) {
                srcDirs.from(parent.srcDirs)
            }

            includes = stringProvider(providers, parent?.includes, self.includes, '**/*.java,**/*.groovy,**/*.kt,**/*.scala')
            outputFormat = stringProvider(providers, parent?.outputFormat, self.outputFormat, 'html')
            tabs = intProvider(providers, parent?.tabs, self.tabs, 4)
            style = stringProvider(providers, parent?.style, self.style, 'kawa')
            lineAnchorPrefix = stringProvider(providers, parent?.lineAnchorPrefix, self.lineAnchorPrefix, '')
            horizontalAlignment = stringProvider(providers, parent?.horizontalAlignment, self.horizontalAlignment, 'left')
            showLineNumbers = booleanProvider(providers, parent?.showLineNumbers, self.showLineNumbers, true)
            showFileName = booleanProvider(providers, parent?.showFileName, self.showFileName, true)
            showDefaultTitle = booleanProvider(providers, parent?.showDefaultTitle, self.showDefaultTitle, true)
            showTableBorder = booleanProvider(providers, parent?.showTableBorder, self.showTableBorder, false)
            includeDocumentHeader = booleanProvider(providers, parent?.includeDocumentHeader, self.includeDocumentHeader, true)
            includeDocumentFooter = booleanProvider(providers, parent?.includeDocumentFooter, self.includeDocumentFooter, true)
            addLineAnchors = booleanProvider(providers, parent?.addLineAnchors, self.addLineAnchors, true)
            useShortFileName = booleanProvider(providers, parent?.useShortFileName, self.useShortFileName, true)
            overwrite = booleanProvider(providers, parent?.overwrite, self.overwrite, true)
        }

        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<>([
                srcDirs              : srcDirs.files,
                destDir              : destDir.get(),
                includes             : includes.get(),
                outputFormat         : outputFormat.get(),
                tabs                 : tabs.get(),
                style                : style.get(),
                lineAnchorPrefix     : lineAnchorPrefix.get(),
                horizontalAlignment  : horizontalAlignment.get(),
                showLineNumbers      : showLineNumbers.get(),
                showFileName         : showFileName.get(),
                showDefaultTitle     : showDefaultTitle.get(),
                showTableBorder      : showTableBorder.get(),
                includeDocumentHeader: includeDocumentHeader.get(),
                includeDocumentFooter: includeDocumentFooter.get(),
                addLineAnchors       : addLineAnchors.get(),
                useShortFileName     : useShortFileName.get(),
                overwrite            : overwrite.get()
            ])

            new LinkedHashMap<>('conversion': map)
        }
    }

    @PackageScope
    @CompileStatic
    static class OverviewImpl extends AbstractFeature implements Overview {
        final RegularFileProperty destDir
        final Property<String> pattern
        final Property<String> windowTitle
        final Property<String> docTitle
        final Property<String> docDescription
        final RegularFileProperty icon
        final RegularFileProperty stylesheet

        private ResolvedOverview resolved

        OverviewImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
            super(project, ownerConfig, parentConfig)

            destDir = project.objects.fileProperty()
            pattern = project.objects.property(String)
            windowTitle = project.objects.property(String)
            docTitle = project.objects.property(String)
            docDescription = project.objects.property(String)
            icon = project.objects.fileProperty()
            stylesheet = project.objects.fileProperty()
        }

        ResolvedOverview asResolved() {
            if (!resolved) {
                resolved = new ResolvedOverviewImpl(project.providers,
                    parentConfig?.asResolved()?.docs?.sourceHtml?.overview,
                    this)
            }
            resolved
        }
    }

    @PackageScope
    @CompileStatic
    static class ResolvedOverviewImpl implements ResolvedOverview {
        final Provider<File> destDir
        final Provider<String> pattern
        final Provider<String> windowTitle
        final Provider<String> docTitle
        final Provider<String> docDescription
        final Provider<File> icon
        final Provider<File> stylesheet

        ResolvedOverviewImpl(ProviderFactory providers, ResolvedOverview parent, OverviewImpl self) {
            destDir = fileProvider(providers,
                parent?.destDir,
                self.destDir,
                self.project.layout.buildDirectory.file('tmp/source-html/overview').get().asFile)

            pattern = stringProvider(providers,
                parent?.pattern,
                self.pattern,
                '**/*.html')

            windowTitle = stringProvider(providers,
                parent?.windowTitle,
                self.windowTitle,
                "${self.project.name} ${self.project.version}")

            docTitle = stringProvider(providers,
                parent?.docTitle,
                self.docTitle,
                "${self.project.name} ${self.project.version}")

            docDescription = stringProvider(providers,
                parent?.docDescription,
                self.docDescription,
                '')

            icon = fileProvider(providers,
                parent?.icon,
                self.icon,
                null)

            stylesheet = fileProvider(providers,
                parent?.stylesheet,
                self.stylesheet,
                null)
        }

        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<>([
                destDir       : destDir.get(),
                pattern       : pattern.get(),
                windowTitle   : windowTitle.get(),
                docTitle      : docTitle.get(),
                docDescription: docDescription.get(),
                icon          : icon.get(),
                stylesheet    : stylesheet.get(),
            ])

            new LinkedHashMap<>('overview': map)
        }
    }
}
