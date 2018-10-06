/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 the original author or authors.
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
package org.kordamp.gradle.model.impl

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.external.javadoc.MinimalJavadocOptions
import org.gradle.external.javadoc.StandardJavadocDocletOptions

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
@CompileStatic
@Canonical
class ExtStandardJavadocDocletOptions extends StandardJavadocDocletOptions {
    private boolean authorSet
    private boolean breakIteratorSet
    private boolean docFilesSubDirsSet
    private boolean keyWordsSet
    private boolean linkSourceSet
    private boolean noCommentSet
    private boolean noHelpSet
    private boolean noIndexSet
    private boolean noNavBarSet
    private boolean noSinceSet
    private boolean noTimestampSet
    private boolean noTreeSet
    private boolean serialWarnSet
    private boolean splitIndexSet
    private boolean useSet
    private boolean versionSet

    boolean isAuthorSet() {
        return authorSet
    }

    boolean isBreakIteratorSet() {
        return breakIteratorSet
    }

    boolean isDocFilesSubDirsSet() {
        docFilesSubDirsSet
    }

    boolean isKewWordsSet() {
        return keyWordsSet
    }

    boolean isLinkSourceSet() {
        return linkSourceSet
    }

    boolean isNoCommentSet() {
        return noCommentSet
    }

    boolean isNoHelpSet() {
        return noHelpSet
    }

    boolean isNoIndexSet() {
        return noIndexSet
    }

    boolean isNoNavBarSet() {
        return noNavBarSet
    }

    boolean isNoSinceSet() {
        return noSinceSet
    }

    boolean isNoTimestampSet() {
        return noTimestampSet
    }

    boolean isNoTreeSet() {
        return noTreeSet
    }

    boolean isSerialWarnSet() {
        return serialWarnSet
    }

    boolean isSplitIndexSet() {
        return splitIndexSet
    }

    boolean isUseSet() {
        return useSet
    }

    boolean isVersionSet() {
        return versionSet
    }

    @Override
    void setUse(boolean use) {
        super.setUse(use)
        useSet = true
    }

    @Override
    void setVersion(boolean version) {
        super.setVersion(version)
        versionSet = true
    }

    @Override
    void setAuthor(boolean author) {
        super.setAuthor(author)
        authorSet = true
    }

    @Override
    void setSplitIndex(boolean splitIndex) {
        super.setSplitIndex(splitIndex)
        splitIndexSet = true
    }

    @Override
    void setLinkSource(boolean linkSource) {
        super.setLinkSource(linkSource)
        linkSourceSet = true
    }

    @Override
    void setNoSince(boolean noSince) {
        super.setNoSince(noSince)
        noSinceSet = true
    }

    @Override
    StandardJavadocDocletOptions noSince(boolean noSince) {
        noSinceSet = true
        return super.noSince(noSince)
    }

    @Override
    void setNoTree(boolean noTree) {
        super.setNoTree(noTree)
        noTreeSet = true
    }

    @Override
    StandardJavadocDocletOptions noTree(boolean noTree) {
        noTreeSet = true
        return super.noTree(noTree)
    }

    @Override
    void setNoIndex(boolean noIndex) {
        super.setNoIndex(noIndex)
        noIndexSet = true
    }

    @Override
    StandardJavadocDocletOptions noIndex(boolean noIndex) {
        noIndexSet = true
        return super.noIndex(noIndex)
    }

    @Override
    void setNoHelp(boolean noHelp) {
        super.setNoHelp(noHelp)
        noHelpSet = true
    }

    @Override
    StandardJavadocDocletOptions noHelp(boolean noHelp) {
        noHelpSet = true
        return super.noHelp(noHelp)
    }

    @Override
    void setNoNavBar(boolean noNavBar) {
        super.setNoNavBar(noNavBar)
        noNavBarSet = true
    }

    @Override
    StandardJavadocDocletOptions noNavBar(boolean noNavBar) {
        noNavBarSet = true
        return super.noNavBar(noNavBar)
    }

    @Override
    void setSerialWarn(boolean serialWarn) {
        super.setSerialWarn(serialWarn)
        serialWarnSet = true
    }

    @Override
    StandardJavadocDocletOptions serialWarn(boolean serialWarn) {
        serialWarnSet = true
        return super.serialWarn(serialWarn)
    }

    @Override
    void setKeyWords(boolean keyWords) {
        super.setKeyWords(keyWords)
        keyWordsSet = true
    }

    @Override
    StandardJavadocDocletOptions keyWords(boolean keyWords) {
        keyWordsSet = true
        return super.keyWords(keyWords)
    }

    @Override
    void setNoTimestamp(boolean noTimestamp) {
        super.setNoTimestamp(noTimestamp)
        noTimestampSet = true
    }

    @Override
    StandardJavadocDocletOptions noTimestamp(boolean noTimestamp) {
        noTimestampSet = true
        return super.noTimestamp(noTimestamp)
    }

    @Override
    void setNoComment(boolean noComment) {
        super.setNoComment(noComment)
        noCommentSet = true
    }

    @Override
    StandardJavadocDocletOptions noComment(boolean noComment) {
        noCommentSet = true
        return super.noComment(noComment)
    }

    @Override
    void setBreakIterator(boolean breakIterator) {
        super.setBreakIterator(breakIterator)
        breakIteratorSet = true
    }

    @Override
    MinimalJavadocOptions breakIterator(boolean breakIterator) {
        breakIteratorSet = true
        return super.breakIterator(breakIterator)
    }

    @Override
    void setDocFilesSubDirs(boolean docFilesSubDirs) {
        super.setDocFilesSubDirs(docFilesSubDirs)
        docFilesSubDirsSet = true
    }

    ExtStandardJavadocDocletOptions copyOf() {
        copyInto(new ExtStandardJavadocDocletOptions())
    }

    @CompileDynamic
    ExtStandardJavadocDocletOptions copyInto(ExtStandardJavadocDocletOptions copy) {
        copy.@author.setValue(author)
        copy.@breakIterator.setValue(breakIterator)
        copy.@docFilesSubDir.setValue(docFilesSubDirs)
        copy.@keyWords.setValue(keyWords)
        copy.@linkSource.setValue(linkSource)
        copy.@noComment.setValue(noComment)
        copy.@noHelp.setValue(noHelp)
        copy.@noIndex.setValue(noIndex)
        copy.@noNavBar.setValue(noNavBar)
        copy.@noSince.setValue(noSince)
        copy.@noTimestamp.setValue(noTimestamp)
        copy.@noTree.setValue(noTree)
        copy.@serialWarn.setValue(serialWarn)
        copy.@splitIndex.setValue(splitIndex)
        copy.@use.setValue(use)
        copy.@version.setValue(version)

        copy.@authorSet = authorSet
        copy.@breakIteratorSet = breakIteratorSet
        copy.@keyWordsSet = keyWordsSet
        copy.@linkSourceSet = linkSourceSet
        copy.@noCommentSet = noCommentSet
        copy.@noHelpSet = noHelpSet
        copy.@noIndexSet = noIndexSet
        copy.@noNavBarSet = noNavBarSet
        copy.@noSinceSet = noSinceSet
        copy.@noTimestampSet = noTimestampSet
        copy.@noTreeSet = noTreeSet
        copy.@serialWarnSet = serialWarnSet
        copy.@splitIndexSet = splitIndexSet
        copy.@useSet = useSet
        copy.@versionSet = versionSet

        copy.setBootClasspath(list(bootClasspath))
        copy.setBottom(bottom)
        copy.setCharSet(charSet)
        copy.setClasspath(list(classpath))
        copy.setDestinationDirectory(destinationDirectory)
        copy.setDocEncoding(docEncoding)
        copy.setDoclet(doclet)
        copy.setDocletpath(list(docletpath))
        copy.setDocTitle(docTitle)
        copy.setEncoding(encoding)
        copy.setExcludeDocFilesSubDir(list(excludeDocFilesSubDir))
        copy.setExtDirs(list(extDirs))
        copy.setFooter(footer)
        copy.setGroups(map(groups))
        copy.setHeader(header)
        copy.setHelpFile(helpFile)
        copy.setJFlags(list(getJFlags()))
        copy.setLinks(list(links))
        copy.setLinksOffline(list(linksOffline))
        copy.setLocale(locale)
        copy.setMemberLevel(memberLevel)
        copy.setNoQualifiers(list(noQualifiers))
        copy.setOptionFiles(list(optionFiles))
        copy.setOutputLevel(outputLevel)
        copy.setOverview(overview)
        copy.setSource(source)
        copy.setSourceNames(list(sourceNames))
        copy.setStylesheetFile(stylesheetFile)
        copy.setTagletPath(list(tagletPath))
        copy.setTaglets(list(taglets))
        copy.setTags(list(tags))
        copy.setWindowTitle(windowTitle)

        copy
    }

    ExtStandardJavadocDocletOptions merge(ExtStandardJavadocDocletOptions o1, ExtStandardJavadocDocletOptions o2) {
        setAuthor((boolean) (authorSet ? o1?.isAuthor() : o2?.isAuthor()))
        setBreakIterator((boolean) (breakIteratorSet ? o1?.isBreakIterator() : o2?.isBreakIterator()))
        setDocFilesSubDirs((boolean) (docFilesSubDirsSet ? o1?.isDocFilesSubDirs() : o2?.isDocFilesSubDirs()))
        setKeyWords((boolean) (keyWordsSet ? o1?.isKeyWords() : o2?.isKeyWords()))
        setLinkSource((boolean) (linkSourceSet ? o1?.isLinkSource() : o2?.isLinkSource()))
        setNoComment((boolean) (noCommentSet ? o1?.isNoComment() : o2?.isNoComment()))
        setNoHelp((boolean) (noHelpSet ? o1?.isNoHelp() : o2?.isNoHelp()))
        setNoIndex((boolean) (noIndexSet ? o1?.isNoIndex() : o2?.isNoIndex()))
        setNoNavBar((boolean) (noNavBarSet ? o1?.isNoNavBar() : o2?.isNoNavBar()))
        setNoSince((boolean) (noSinceSet ? o1?.isNoSince() : o2?.isNoSince()))
        setNoTimestamp((boolean) (noTimestampSet ? o1?.isNoTimestamp() : o2?.isNoTimestamp()))
        setNoTree((boolean) (noTreeSet ? o1?.isNoTree() : o2?.isNoTree()))
        setSerialWarn((boolean) (serialWarnSet ? o1?.isSerialWarn() : o2?.isSerialWarn()))
        setSplitIndex((boolean) (splitIndexSet ? o1?.isSplitIndex() : o2?.isSplitIndex()))
        setUse((boolean) (useSet ? o1?.isUse() : o2?.isUse()))
        setVersion((boolean) (versionSet ? o1?.isVersion() : o2?.isVersion()))

        setBootClasspath(list(o1?.bootClasspath ?: o2?.bootClasspath))
        setBottom(o1?.getBottom() ?: o2?.getBottom())
        setCharSet(o1?.getCharSet() ?: o2?.getCharSet())
        setClasspath(list(o1?.classpath ?: o2?.classpath))
        setDestinationDirectory(o1?.destinationDirectory ?: o2?.destinationDirectory)
        setDocEncoding(o1?.getDocEncoding() ?: o2?.getDocEncoding())
        setDoclet(o1?.doclet ?: o2?.doclet)
        setDocletpath(list(o1?.docletpath ?: o2?.docletpath))
        setDocTitle(o1?.getDocTitle() ?: o2?.getDocTitle())
        setEncoding(o1?.encoding ?: o2?.encoding)
        setExcludeDocFilesSubDir(list(o1?.getExcludeDocFilesSubDir() ?: o2?.getExcludeDocFilesSubDir()))
        setExtDirs(list(o1?.extDirs ?: o2?.extDirs))
        setFooter(o1?.getFooter() ?: o2?.getFooter())
        setGroups(map(o1?.getGroups() ?: o2?.getGroups()))
        setHeader(o1?.header ?: o2?.header)
        setHelpFile(o1?.getHelpFile() ?: o2?.getHelpFile())
        setJFlags(list(o1?.getJFlags() ?: o2?.getJFlags()))
        setLinks(list(o1?.getLinks() ?: o2?.getLinks()))
        setLinksOffline(list(o1?.getLinksOffline() ?: o2?.getLinksOffline()))
        setLocale(o1?.locale ?: o2?.locale)
        setMemberLevel(o1?.getMemberLevel() ?: o2?.getMemberLevel())
        setNoQualifiers(list(o1?.getNoQualifiers() ?: o2?.getNoQualifiers()))
        setOptionFiles(list(o1?.getOptionFiles() ?: o2?.getOptionFiles()))
        setOutputLevel(o1?.getOutputLevel() ?: o2?.getOutputLevel())
        setOverview(o1?.getOverview() ?: o2?.getOverview())
        setSource(o1?.getSource() ?: o2?.getSource())
        setSourceNames(list(o1?.getSourceNames() ?: o2?.getSourceNames()))
        setStylesheetFile(o1?.getStylesheetFile() ?: o2?.getStylesheetFile())
        setTagletPath(list(o1?.getTagletPath() ?: o2?.getTagletPath()))
        setTaglets(list(o1?.getTaglets() ?: o2?.getTaglets()))
        setTags(list(o1?.getTags() ?: o2?.getTags()))
        setWindowTitle(o1?.getWindowTitle() ?: o2?.getWindowTitle())

        this
    }


    void applyTo(MinimalJavadocOptions options) {
        options.setBreakIterator((boolean) (breakIteratorSet ? isBreakIterator() : options.isBreakIterator()))
        options.setBootClasspath(getBootClasspath() ?: options.getBootClasspath())
        options.setClasspath(getClasspath() ?: options.getClasspath())
        options.setDestinationDirectory(getDestinationDirectory() ?: options.getDestinationDirectory())
        options.setDoclet(getDoclet() ?: options.getDoclet())
        options.setDocletpath(getDocletpath() ?: options.getDocletpath())
        options.setEncoding(getEncoding() ?: options.getEncoding())
        options.setExtDirs(getExtDirs() ?: options.getExtDirs())
        options.setHeader(getHeader() ?: options.getHeader())
        options.setJFlags(getJFlags() ?: options.getJFlags())
        options.setLocale(getLocale() ?: options.getLocale())
        options.setMemberLevel(getMemberLevel() ?: options.getMemberLevel())
        options.setOptionFiles(getOptionFiles() ?: options.getOptionFiles())
        options.setOutputLevel(getOutputLevel() ?: options.getOutputLevel())
        options.setOverview(getOverview() ?: options.getOverview())
        options.setSource(getSource() ?: options.getSource())
        options.setSourceNames(getSourceNames() ?: options.getSourceNames())
        options.setWindowTitle(getWindowTitle() ?: options.getWindowTitle())

        if (options instanceof StandardJavadocDocletOptions) {
            StandardJavadocDocletOptions soptions = (StandardJavadocDocletOptions) options

            soptions.setAuthor((boolean) (authorSet ? isAuthor() : soptions.isAuthor()))
            soptions.setDocFilesSubDirs((boolean) (docFilesSubDirsSet ? isDocFilesSubDirs() : soptions.isDocFilesSubDirs()))
            soptions.setKeyWords((boolean) (keyWordsSet ? isKeyWords() : soptions.isKeyWords()))
            soptions.setLinkSource((boolean) (linkSourceSet ? isLinkSource() : soptions.isLinkSource()))
            soptions.setNoComment((boolean) (noCommentSet ? isNoComment() : soptions.isNoComment()))
            soptions.setNoHelp((boolean) (noHelpSet ? isNoHelp() : soptions.isNoHelp()))
            soptions.setNoIndex((boolean) (noIndexSet ? isNoIndex() : soptions.isNoIndex()))
            soptions.setNoNavBar((boolean) (noNavBarSet ? isNoNavBar() : soptions.isNoNavBar()))
            soptions.setNoSince((boolean) (noSinceSet ? isNoSince() : soptions.isNoSince()))
            soptions.setNoTimestamp((boolean) (noTimestampSet ? isNoTimestamp() : soptions.isNoTimestamp()))
            soptions.setNoTree((boolean) (noTreeSet ? isNoTree() : soptions.isNoTree()))
            soptions.setSerialWarn((boolean) (serialWarnSet ? isSerialWarn() : soptions.isSerialWarn()))
            soptions.setSplitIndex((boolean) (splitIndexSet ? isSplitIndex() : soptions.isSplitIndex()))
            soptions.setUse((boolean) (useSet ? isUse() : soptions.isUse()))
            soptions.setVersion((boolean) (versionSet ? isVersion() : soptions.isVersion()))

            soptions.setBottom(getBottom() ?: soptions.getBottom())
            soptions.setCharSet(getCharSet() ?: soptions.getCharSet())
            soptions.setDocEncoding(getDocEncoding() ?: soptions.getDocEncoding())
            soptions.setDocTitle(getDocTitle() ?: soptions.getDocTitle())
            soptions.setExcludeDocFilesSubDir(list(getExcludeDocFilesSubDir() ?: soptions.getExcludeDocFilesSubDir()))
            soptions.setFooter(getFooter() ?: soptions.getFooter())
            soptions.setGroups(map(getGroups() ?: soptions.getGroups()))
            soptions.setHelpFile(getHelpFile() ?: soptions.getHelpFile())
            soptions.setLinks(list(getLinks() ?: soptions.getLinks()))
            soptions.setLinksOffline(list(getLinksOffline() ?: soptions.getLinksOffline()))
            soptions.setNoQualifiers(list(getNoQualifiers() ?: soptions.getNoQualifiers()))
            soptions.setStylesheetFile(getStylesheetFile() ?: soptions.getStylesheetFile())
            soptions.setTagletPath(list(getTagletPath() ?: soptions.getTagletPath()))
            soptions.setTaglets(list(getTaglets() ?: soptions.getTaglets()))
            soptions.setTags(list(getTags() ?: soptions.getTags()))
        }
    }

    static <T> List<T> list(List<T> src) {
        new ArrayList<>(src)
    }

    static <K, V> Map<K, V> map(Map<K, V> src) {
        new LinkedHashMap<>(src)
    }
}
