/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.util.ConfigureUtil

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
class SourceHtml extends AbstractFeature {
    final Conversion conversion
    final Overview overview

    FileCollection srcDirs
    private final Set<Project> projects = new LinkedHashSet<>()
    private final Set<Project> excludedProjects = new LinkedHashSet<>()

    SourceHtml(Project project) {
        super(project)
        this.conversion = new Conversion(project)
        this.overview = new Overview(project)
        srcDirs = project.files()
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    @CompileDynamic
    Map<String, Map<String, Object>> toMap() {
        Map map = [enabled: enabled]

        if (enabled) {
            map.conversion = conversion.toMap()
            map.overview = overview.toMap()
        }

        if (isRoot()) {
            if (enabled) {
                map.excludedProjects = excludedProjects
            }
        }

        ['sourceHtml': map]
    }

    void copyInto(SourceHtml copy) {
        super.copyInto(copy)
        conversion.copyInto(copy.conversion)
        overview.copyInto(overview)
        copy.srcDirs = srcDirs
    }

    static void merge(SourceHtml o1, SourceHtml o2) {
        AbstractFeature.merge(o1, o2)
        Conversion.merge(o1.conversion, o2.conversion)
        Overview.merge(o1.overview, o2.overview)
        o1.projects().addAll(o2.projects())
        o1.excludedProjects().addAll(o2.excludedProjects())
    }

    Set<Project> excludedProjects() {
        excludedProjects
    }

    Set<Project> projects() {
        projects
    }

    void conversion(Action<? super Conversion> action) {
        action.execute(conversion)
    }

    void conversion(@DelegatesTo(Conversion) Closure action) {
        ConfigureUtil.configure(action, conversion)
    }

    void overview(Action<? super Overview> action) {
        action.execute(overview)
    }

    void overview(@DelegatesTo(Overview) Closure action) {
        ConfigureUtil.configure(action, overview)
    }

    @CompileStatic
    @Canonical
    @ToString(includeNames = true)
    static class Conversion {
        FileCollection srcDirs
        File destDir
        String includes = '**/*.java,**/*.groovy'
        String outputFormat = 'html'
        int tabs = 4
        String style = 'kawa'
        String lineAnchorPrefix = ''
        String horizontalAlignment = 'left'
        boolean showLineNumbers = true
        boolean showFileName = true
        boolean showDefaultTitle = true
        boolean showTableBorder
        boolean includeDocumentHeader = true
        boolean includeDocumentFooter = true
        boolean addLineAnchors = true
        boolean useShortFileName = true
        boolean overwrite = true

        boolean showLineNumbersSet
        boolean showFileNameSet
        boolean showDefaultTitleSet
        boolean showTableBorderSet
        boolean includeDocumentHeaderSet
        boolean includeDocumentFooterSet
        boolean addLineAnchorsSet
        boolean useShortFileNameSet
        boolean overwriteSet

        private final Project project

        Conversion(Project project) {
            this.project = project
            destDir = project.file("${project.buildDir}/tmp/source-html/conversion")
        }

        @CompileDynamic
        Map<String, Object> toMap() {
            [
                srcDirs              : srcDirs?.files,
                destDir              : destDir,
                includes             : includes,
                outputFormat         : outputFormat,
                tabs                 : tabs,
                style                : style,
                lineAnchorPrefix     : lineAnchorPrefix,
                horizontalAlignment  : horizontalAlignment,
                showLineNumbers      : showLineNumbers,
                showFileName         : showFileName,
                showDefaultTitle     : showDefaultTitle,
                showTableBorder      : showTableBorder,
                includeDocumentHeader: includeDocumentHeader,
                includeDocumentFooter: includeDocumentFooter,
                addLineAnchors       : addLineAnchors,
                useShortFileName     : useShortFileName,
                overwrite            : overwrite
            ]
        }

        boolean isShowLineNumbersSet() {
            return showLineNumbersSet
        }

        boolean isShowFileNameSet() {
            return showFileNameSet
        }

        boolean isShowDefaultTitleSet() {
            return showDefaultTitleSet
        }

        boolean isShowTableBorderSet() {
            return showTableBorderSet
        }

        boolean isIncludeDocumentHeaderSet() {
            return includeDocumentHeaderSet
        }

        boolean isIncludeDocumentFooterSet() {
            return includeDocumentFooterSet
        }

        boolean isAddLineAnchorsSet() {
            return addLineAnchorsSet
        }

        boolean isUseShortFileNameSet() {
            return useShortFileNameSet
        }

        boolean isOverwriteSet() {
            return overwriteSet
        }

        void setShowLineNumbers(boolean showLineNumbers) {
            this.showLineNumbers = showLineNumbers
            showLineNumbersSet = true
        }

        void setShowFileName(boolean showFileName) {
            this.showFileName = showFileName
            showFileNameSet = true
        }

        void setShowDefaultTitle(boolean showDefaultTitle) {
            this.showDefaultTitle = showDefaultTitle
            showDefaultTitleSet = true
        }

        void setShowTableBorder(boolean showTableBorder) {
            this.showTableBorder = showTableBorder
            showTableBorderSet = true
        }

        void setIncludeDocumentHeader(boolean includeDocumentHeader) {
            this.includeDocumentHeader = includeDocumentHeader
            includeDocumentHeaderSet = true
        }

        void setIncludeDocumentFooter(boolean includeDocumentFooter) {
            this.includeDocumentFooter = includeDocumentFooter
            includeDocumentFooterSet = true
        }

        void setAddLineAnchors(boolean addLineAnchors) {
            this.addLineAnchors = addLineAnchors
            addLineAnchorsSet = true
        }

        void setUseShortFileName(boolean useShortFileName) {
            this.useShortFileName = useShortFileName
            useShortFileNameSet = true
        }

        void setOverwrite(boolean overwrite) {
            this.overwrite = overwrite
            overwriteSet = true
        }

        void copyInto(Conversion copy) {
            copy.srcDirs = srcDirs ? project.files(srcDirs) : null
            copy.destDir = destDir
            copy.includes = includes
            copy.outputFormat = outputFormat
            copy.tabs = tabs
            copy.style = style
            copy.lineAnchorPrefix = lineAnchorPrefix
            copy.horizontalAlignment = horizontalAlignment

            copy.@showLineNumbers = showLineNumbers
            copy.@showFileName = showFileName
            copy.@showDefaultTitle = showDefaultTitle
            copy.@showTableBorder = showTableBorder
            copy.@includeDocumentHeader = includeDocumentHeader
            copy.@includeDocumentFooter = includeDocumentFooter
            copy.@addLineAnchors = addLineAnchors
            copy.@useShortFileName = useShortFileName
            copy.@overwrite = overwrite

            copy.showLineNumbersSet = showLineNumbersSet
            copy.showFileNameSet = showFileNameSet
            copy.showDefaultTitleSet = showDefaultTitleSet
            copy.showTableBorderSet = showTableBorderSet
            copy.includeDocumentHeaderSet = includeDocumentHeaderSet
            copy.includeDocumentFooterSet = includeDocumentFooterSet
            copy.addLineAnchorsSet = addLineAnchorsSet
            copy.useShortFileNameSet = useShortFileNameSet
            copy.overwriteSet = overwriteSet
        }

        @CompileDynamic
        static void merge(Conversion o1, Conversion o2) {
            o1.srcDirs = o1.project.files((o1.srcDirs ?: []), (o2.srcDirs ?: []))
            o1.destDir = o1.destDir ?: o2.destDir
            o1.includes = (o1.includes + ',' + o2.includes).split(',').collect().unique().join(',')
            o1.outputFormat = o1.outputFormat ?: o2.outputFormat
            o1.tabs = o1.tabs ?: o2.tabs
            o1.style = o1.style ?: o2.style
            o1.lineAnchorPrefix = o1.lineAnchorPrefix ?: o2.lineAnchorPrefix
            o1.horizontalAlignment = o1.horizontalAlignment ?: o2.horizontalAlignment

            o1.setShowLineNumbers((boolean) (o1.showLineNumbersSet ? o1.showLineNumbers : o2.showLineNumbers))
            o1.setShowFileName((boolean) (o1.showFileNameSet ? o1.showFileName : o2.showFileName))
            o1.setShowDefaultTitle((boolean) (o1.showDefaultTitleSet ? o1.showDefaultTitle : o2.showDefaultTitle))
            o1.setShowTableBorder((boolean) (o1.showTableBorderSet ? o1.showTableBorder : o2.showTableBorder))
            o1.setIncludeDocumentHeader((boolean) (o1.includeDocumentHeaderSet ? o1.includeDocumentHeader : o2.includeDocumentHeader))
            o1.setIncludeDocumentFooter((boolean) (o1.includeDocumentFooterSet ? o1.includeDocumentFooter : o2.includeDocumentFooter))
            o1.setAddLineAnchors((boolean) (o1.addLineAnchorsSet ? o1.addLineAnchors : o2.addLineAnchors))
            o1.setUseShortFileName((boolean) (o1.useShortFileNameSet ? o1.useShortFileName : o2.useShortFileName))
            o1.setOverwrite((boolean) (o1.overwriteSet ? o1.overwrite : o2.overwrite))
        }
    }

    @CompileStatic
    @Canonical
    @ToString(includeNames = true)
    static class Overview {
        File destDir
        String pattern = '**/*.html'
        String windowTitle
        String docTitle
        String docDescription
        File icon
        File stylesheet

        private final Project project

        Overview(Project project) {
            this.project = project
            destDir = project.file("${project.buildDir}/tmp/source-html/overview")
            windowTitle = "$project.name $project.version"
            docTitle = "$project.name $project.version"
        }

        @CompileDynamic
        Map<String, Object> toMap() {
            [
                destDir       : destDir,
                pattern       : pattern,
                windowTitle   : windowTitle,
                docTitle      : docTitle,
                docDescription: docDescription,
                icon          : icon,
                stylesheet    : stylesheet
            ]
        }

        void copyInto(Overview copy) {
            copy.destDir = destDir
            copy.pattern = pattern
            copy.windowTitle = windowTitle
            copy.docTitle = docTitle
            copy.docDescription = docDescription
            copy.icon = icon
            copy.stylesheet = stylesheet
        }

        static void merge(Overview o1, Overview o2) {
            o1.destDir = o1.destDir ?: o2.destDir
            o1.pattern = o1.pattern ?: o2.pattern
            o1.windowTitle = o1.windowTitle ?: o2.windowTitle
            o1.docTitle = o1.docTitle ?: o2.docTitle
            o1.docDescription = o1.docDescription ?: o2.docDescription
            o1.icon = o1.icon ?: o2.icon
            o1.stylesheet = o1.stylesheet ?: o2.stylesheet
        }
    }
}
