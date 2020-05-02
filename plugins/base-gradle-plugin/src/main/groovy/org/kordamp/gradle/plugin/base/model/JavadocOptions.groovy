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
package org.kordamp.gradle.plugin.base.model

import groovy.transform.CompileStatic
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.external.javadoc.JavadocMemberLevel
import org.gradle.external.javadoc.JavadocOutputLevel

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
@CompileStatic
interface JavadocOptions {
    Property<String> getOverview()

    Property<JavadocMemberLevel> getMemberLevel()

    Property<String> getDoclet()

    ConfigurableFileCollection getDocletpath()

    Property<String> getSource()

    ConfigurableFileCollection getClasspath()

    ConfigurableFileCollection getBootClasspath()

    ConfigurableFileCollection getExtDirs()

    Property<JavadocOutputLevel> getOutputLevel()

    Property<Boolean> getBreakIterator()

    Property<String> getLocale()

    Property<String> getEncoding()

    ListProperty<String> getSourceNames()

    RegularFileProperty getDestinationDirectory()

    Property<Boolean> getUse()

    Property<Boolean> getVersion()

    Property<Boolean> getAuthor()

    Property<Boolean> getSplitIndex()

    Property<String> getWindowTitle()

    Property<String> getHeader()

    Property<String> getDocTitle()

    Property<String> getFooter()

    Property<String> getBottom()

    Property<String> getTop()

    ListProperty<String> getLinks()

    Property<Boolean> getLinkSource()

    MapProperty<String, List<String>> getGroups()

    Property<Boolean> getNoDeprecated()

    Property<Boolean> getNoDeprecatedList()

    Property<Boolean> getNoSince()

    Property<Boolean> getNoTree()

    Property<Boolean> getNoIndex()

    Property<Boolean> getNoHelp()

    Property<Boolean> getNoNavBar()

    RegularFileProperty getHelpFile()

    RegularFileProperty getStylesheetFile()

    Property<Boolean> getSerialWarn()

    Property<String> getCharSet()

    Property<String> getDocEncoding()

    Property<Boolean> getKeyWords()

    ListProperty<String> getTags()

    ListProperty<String> getTaglets()

    ConfigurableFileCollection getTagletPath()

    Property<Boolean> getDocFilesSubDirs()

    ListProperty<String> getExcludeDocFilesSubDir()

    ListProperty<String> getNoQualifiers()

    Property<Boolean> getNoTimestamp()

    Property<Boolean> getNoComment()

    void link(String url)

    void linksOffline(String extDocUrl, String packageListLoc)

    void addBooleanOption(String key, boolean value)

    void addStringOption(String key, String value)
}
