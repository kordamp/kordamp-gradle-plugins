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
package org.kordamp.gradle.plugin.base.model.impl

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import org.gradle.external.javadoc.CoreJavadocOptions
import org.gradle.external.javadoc.JavadocOptionFileOption
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

    private final Map<String, JavadocOptionFileOption<?>> myOptions = new LinkedHashMap<>()

    public <T> JavadocOptionFileOption<T> addOption(JavadocOptionFileOption<T> option) {
        JavadocOptionFileOption<T> o = super.addOption(option)
        myOptions?.put(o.option, o)
        o
    }

    JavadocOptionFileOption<String> addStringOption(String option) {
        JavadocOptionFileOption<String> o = super.addStringOption(option)
        myOptions?.put(o.option, o)
        o
    }

    JavadocOptionFileOption<String> addStringOption(String option, String value) {
        JavadocOptionFileOption<String> o = super.addStringOption(option, value)
        myOptions?.put(o.option, o)
        o
    }

    public <T extends Enum<T>> JavadocOptionFileOption<T> addEnumOption(String option) {
        JavadocOptionFileOption<T> o = super.addEnumOption(option)
        myOptions?.put(o.option, o)
        o
    }

    public <T extends Enum<T>> JavadocOptionFileOption<T> addEnumOption(String option, T value) {
        JavadocOptionFileOption<T> o = super.addEnumOption(option, value)
        myOptions?.put(o.option, o)
        o
    }

    JavadocOptionFileOption<List<File>> addPathOption(String option) {
        JavadocOptionFileOption<List<File>> o = super.addPathOption(option)
        myOptions?.put(o.option, o)
        o
    }

    JavadocOptionFileOption<List<File>> addPathOption(String option, String joinBy) {
        JavadocOptionFileOption<List<File>> o = super.addPathOption(option, joinBy)
        myOptions?.put(o.option, o)
        o
    }

    JavadocOptionFileOption<List<String>> addStringsOption(String option) {
        JavadocOptionFileOption<List<String>> o = super.addStringsOption(option)
        myOptions?.put(o.option, o)
        o
    }

    JavadocOptionFileOption<List<String>> addStringsOption(String option, String joinBy) {
        JavadocOptionFileOption<List<String>> o = super.addStringsOption(option, joinBy)
        myOptions?.put(o.option, o)
        o
    }

    JavadocOptionFileOption<List<String>> addMultilineStringsOption(String option) {
        JavadocOptionFileOption<List<String>> o = super.addMultilineStringsOption(option)
        myOptions?.put(o.option, o)
        o
    }

    JavadocOptionFileOption<List<List<String>>> addMultilineMultiValueOption(String option) {
        JavadocOptionFileOption<List<List<String>>> o = super.addMultilineMultiValueOption(option)
        myOptions?.put(o.option, o)
        o
    }

    JavadocOptionFileOption<Boolean> addBooleanOption(String option) {
        JavadocOptionFileOption<Boolean> o = super.addBooleanOption(option)
        myOptions?.put(o.option, o)
        o
    }

    JavadocOptionFileOption<Boolean> addBooleanOption(String option, boolean value) {
        JavadocOptionFileOption<Boolean> o = super.addBooleanOption(option, value)
        myOptions?.put(o.option, o)
        o
    }

    JavadocOptionFileOption<File> addFileOption(String option) {
        JavadocOptionFileOption<File> o = super.addFileOption(option)
        myOptions?.put(o.option, o)
        o
    }

    JavadocOptionFileOption<File> addFileOption(String option, File value) {
        JavadocOptionFileOption<File> o = super.addFileOption(option, value)
        myOptions?.put(o.option, o)
        o
    }

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

    private void doSetUse(boolean use) {
        super.setUse(use)
    }

    private void doSetVersion(boolean version) {
        super.setVersion(version)
    }

    private void doSetAuthor(boolean author) {
        super.setAuthor(author)
    }

    private void doSetSplitIndex(boolean splitIndex) {
        super.setSplitIndex(splitIndex)
    }

    private void doSetLinkSource(boolean linkSource) {
        super.setLinkSource(linkSource)
    }

    private void doSetNoSince(boolean noSince) {
        super.setNoSince(noSince)
    }

    private void doSetNoTree(boolean noTree) {
        super.setNoTree(noTree)
    }

    private void doSetNoIndex(boolean noIndex) {
        super.setNoIndex(noIndex)
    }

    private void doSetNoHelp(boolean noHelp) {
        super.setNoHelp(noHelp)
    }

    private void doSetNoNavBar(boolean noNavBar) {
        super.setNoNavBar(noNavBar)
    }

    private void doSetSerialWarn(boolean serialWarn) {
        super.setSerialWarn(serialWarn)
    }

    private void doSetKeyWords(boolean keyWords) {
        super.setKeyWords(keyWords)
    }

    private void doSetNoTimestamp(boolean noTimestamp) {
        super.setNoTimestamp(noTimestamp)
    }

    private void doSetNoComment(boolean noComment) {
        super.setNoComment(noComment)
    }

    private void doSetBreakIterator(boolean breakIterator) {
        super.setBreakIterator(breakIterator)
    }

    private void doSetDocFilesSubDirs(boolean docFilesSubDirs) {
        super.setDocFilesSubDirs(docFilesSubDirs)
    }

    ExtStandardJavadocDocletOptions copyOf() {
        copyInto(new ExtStandardJavadocDocletOptions())
    }

    ExtStandardJavadocDocletOptions copyInto(ExtStandardJavadocDocletOptions copy) {
        copy.doSetAuthor(isAuthor())
        copy.doSetBreakIterator(isBreakIterator())
        copy.doSetDocFilesSubDirs(isDocFilesSubDirs())
        copy.doSetKeyWords(isKeyWords())
        copy.doSetLinkSource(isLinkSource())
        copy.doSetNoComment(isNoComment())
        copy.doSetNoHelp(isNoHelp())
        copy.doSetNoIndex(isNoIndex())
        copy.doSetNoNavBar(isNoNavBar())
        copy.doSetNoSince(isNoSince())
        copy.doSetNoTimestamp(isNoTimestamp())
        copy.doSetNoTree(isNoTree())
        copy.doSetSerialWarn(isSerialWarn())
        copy.doSetSplitIndex(isSplitIndex())
        copy.doSetUse(isUse())
        copy.doSetVersion(isVersion())

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

        copy.setBootClasspath(list(getBootClasspath()))
        copy.setBottom(getBottom())
        copy.setCharSet(getCharSet())
        copy.setClasspath(list(getClasspath()))
        copy.setDestinationDirectory(getDestinationDirectory())
        copy.setDocEncoding(getDocEncoding())
        copy.setDoclet(getDoclet())
        copy.setDocletpath(list(getDocletpath()))
        copy.setDocTitle(getDocTitle())
        copy.setEncoding(getEncoding())
        copy.setExcludeDocFilesSubDir(list(getExcludeDocFilesSubDir()))
        copy.setExtDirs(list(getExtDirs()))
        copy.setFooter(getFooter())
        copy.setGroups(map(getGroups()))
        copy.setHeader(getHeader())
        copy.setHelpFile(getHelpFile())
        copy.setJFlags(list(getJFlags()))
        copy.setLinks(list(getLinks()))
        copy.setLinksOffline(list(getLinksOffline()))
        copy.setLocale(getLocale())
        copy.setMemberLevel(getMemberLevel())
        copy.setNoQualifiers(list(getNoQualifiers()))
        copy.setOptionFiles(list(getOptionFiles()))
        copy.setOutputLevel(getOutputLevel())
        copy.setOverview(getOverview())
        copy.setSource(getSource())
        copy.setSourceNames(list(getSourceNames()))
        copy.setStylesheetFile(getStylesheetFile())
        copy.setTagletPath(list(getTagletPath()))
        copy.setTaglets(list(getTaglets()))
        copy.setTags(list(getTags()))
        copy.setWindowTitle(getWindowTitle())

        copy
    }

    static void merge(ExtStandardJavadocDocletOptions o1, ExtStandardJavadocDocletOptions o2) {
        o1.setAuthor((boolean) (o1.authorSet ? o1.isAuthor() : o2.isAuthor()))
        o1.setBreakIterator((boolean) (o1.breakIteratorSet ? o1.isBreakIterator() : o2.isBreakIterator()))
        o1.setDocFilesSubDirs((boolean) (o1.docFilesSubDirsSet ? o1.isDocFilesSubDirs() : o2.isDocFilesSubDirs()))
        o1.setKeyWords((boolean) (o1.keyWordsSet ? o1.isKeyWords() : o2.isKeyWords()))
        o1.setLinkSource((boolean) (o1.linkSourceSet ? o1.isLinkSource() : o2.isLinkSource()))
        o1.setNoComment((boolean) (o1.noCommentSet ? o1.isNoComment() : o2.isNoComment()))
        o1.setNoHelp((boolean) (o1.noHelpSet ? o1.isNoHelp() : o2.isNoHelp()))
        o1.setNoIndex((boolean) (o1.noIndexSet ? o1.isNoIndex() : o2.isNoIndex()))
        o1.setNoNavBar((boolean) (o1.noNavBarSet ? o1.isNoNavBar() : o2.isNoNavBar()))
        o1.setNoSince((boolean) (o1.noSinceSet ? o1.isNoSince() : o2.isNoSince()))
        o1.setNoTimestamp((boolean) (o1.noTimestampSet ? o1.isNoTimestamp() : o2.isNoTimestamp()))
        o1.setNoTree((boolean) (o1.noTreeSet ? o1.isNoTree() : o2.isNoTree()))
        o1.setSerialWarn((boolean) (o1.serialWarnSet ? o1.isSerialWarn() : o2.isSerialWarn()))
        o1.setSplitIndex((boolean) (o1.splitIndexSet ? o1.isSplitIndex() : o2.isSplitIndex()))
        o1.setUse((boolean) (o1.useSet ? o1.isUse() : o2.isUse()))
        o1.setVersion((boolean) (o1.versionSet ? o1.isVersion() : o2.isVersion()))

        o1.setBootClasspath(list(o1.getBootClasspath(), o2.getBootClasspath()))
        o1.setBottom(o1.getBottom() ?: o2.getBottom())
        o1.setCharSet(o1.getCharSet() ?: o2.getCharSet())
        o1.setClasspath(list(o1.getClasspath(), o2.getClasspath()))
        o1.setDestinationDirectory(o1.getDestinationDirectory() ?: o2.getDestinationDirectory())
        o1.setDocEncoding(o1.getDocEncoding() ?: o2.getDocEncoding())
        o1.setDoclet(o1.getDoclet() ?: o2.getDoclet())
        o1.setDocletpath(list(o1.getDocletpath(), o2.getDocletpath()))
        o1.setDocTitle(o1.getDocTitle() ?: o2.getDocTitle())
        o1.setEncoding(o1.getEncoding() ?: o2.getEncoding())
        o1.setExcludeDocFilesSubDir(list(o1.getExcludeDocFilesSubDir(), o2.getExcludeDocFilesSubDir()))
        o1.setExtDirs(list(o1.getExtDirs(), o2.getExtDirs()))
        o1.setFooter(o1.getFooter() ?: o2.getFooter())
        o1.setGroups(map(o1.getGroups(), o2.getGroups()))
        o1.setHeader(o1.getHeader() ?: o2.getHeader())
        o1.setHelpFile(o1.getHelpFile() ?: o2.getHelpFile())
        o1.setJFlags(list(o1.getJFlags(), o2.getJFlags()))
        o1.setLinks(list(o1.getLinks(), o2.getLinks()))
        o1.setLinksOffline(list(o1.getLinksOffline(), o2.getLinksOffline()))
        o1.setLocale(o1.getLocale() ?: o2.getLocale())
        o1.setMemberLevel(o1.getMemberLevel() ?: o2.getMemberLevel())
        o1.setNoQualifiers(list(o1.getNoQualifiers(), o2.getNoQualifiers()))
        o1.setOptionFiles(list(o1.getOptionFiles(), o2.getOptionFiles()))
        o1.setOutputLevel(o1.getOutputLevel() ?: o2.getOutputLevel())
        o1.setOverview(o1.getOverview() ?: o2.getOverview())
        o1.setSource(o1.getSource() ?: o2.getSource())
        o1.setSourceNames(list(o1.getSourceNames(), o2.getSourceNames()))
        o1.setStylesheetFile(o1.getStylesheetFile() ?: o2.getStylesheetFile())
        o1.setTagletPath(list(o1.getTagletPath(), o2.getTagletPath()))
        o1.setTaglets(list(o1.getTaglets(), o2.getTaglets()))
        o1.setTags(list(o1.getTags(), o2.getTags()))
        o1.setWindowTitle(o1.getWindowTitle() ?: o2.getWindowTitle())

        Map<String, JavadocOptionFileOption<?>> map = new LinkedHashMap<>(o2.myOptions)
        map.putAll(o1.myOptions)
        o1.myOptions.clear()
        o1.myOptions?.putAll(map)
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

        if (options instanceof CoreJavadocOptions) {
            CoreJavadocOptions coptions = (CoreJavadocOptions) options
            myOptions.values().each { JavadocOptionFileOption o -> coptions.addOption(o) }
        }

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
            soptions.setExcludeDocFilesSubDir(list(getExcludeDocFilesSubDir(), soptions.getExcludeDocFilesSubDir()))
            soptions.setFooter(getFooter() ?: soptions.getFooter())
            soptions.setGroups(map(getGroups(), soptions.getGroups()))
            soptions.setHelpFile(getHelpFile() ?: soptions.getHelpFile())
            soptions.setLinks(list(getLinks(), soptions.getLinks()))
            soptions.setLinksOffline(list(getLinksOffline(), soptions.getLinksOffline()))
            soptions.setNoQualifiers(list(getNoQualifiers(), soptions.getNoQualifiers()))
            soptions.setStylesheetFile(getStylesheetFile() ?: soptions.getStylesheetFile())
            soptions.setTagletPath(list(getTagletPath(), soptions.getTagletPath()))
            soptions.setTaglets(list(getTaglets(), soptions.getTaglets()))
            soptions.setTags(list(getTags(), soptions.getTags()))
        }
    }

    static <T> List<T> list(List<T> src) {
        new ArrayList<>(src.unique())
    }

    static <T> List<T> list(List<T> l1, List<T> l2) {
        new ArrayList<>((l1 + l2).unique())
    }

    static <K, V> Map<K, V> map(Map<K, V> src) {
        new LinkedHashMap<>(src)
    }

    static <K, V> Map<K, V> map(Map<K, V> m1, Map<K, V> m2) {
        new LinkedHashMap<>(m2 + m1)
    }
}
