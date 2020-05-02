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
package org.kordamp.gradle.plugin.base.resolved.model

import groovy.transform.CompileStatic
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Provider
import org.gradle.external.javadoc.JavadocMemberLevel
import org.gradle.external.javadoc.JavadocOutputLevel

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@CompileStatic
interface ResolvedJavadocOptions {
    Provider<String> getOverview()

    Provider<JavadocMemberLevel> getMemberLevel()

    Provider<String> getDoclet()

    ConfigurableFileCollection getDocletpath()

    Provider<String> getSource()

    ConfigurableFileCollection getClasspath()

    ConfigurableFileCollection getBootClasspath()

    ConfigurableFileCollection getExtDirs()

    Provider<JavadocOutputLevel> getOutputLevel()

    Provider<Boolean> getBreakIterator()

    Provider<String> getLocale()

    Provider<String> getEncoding()

    Provider<List<String>> getSourceNames()

    Provider<File> getDestinationDirectory()

    Provider<Boolean> getUse()

    Provider<Boolean> getVersion()

    Provider<Boolean> getAuthor()

    Provider<Boolean> getSplitIndex()

    Provider<String> getWindowTitle()

    Provider<String> getHeader()

    Provider<String> getDocTitle()

    Provider<String> getFooter()

    Provider<String> getBottom()

    Provider<String> getTop()

    Provider<List<String>> getLinks()

    Provider<Boolean> getLinkSource()

    Provider<Map<String, List>> getGroups()

    Provider<Boolean> getNoDeprecated()

    Provider<Boolean> getNoDeprecatedList()

    Provider<Boolean> getNoSince()

    Provider<Boolean> getNoTree()

    Provider<Boolean> getNoIndex()

    Provider<Boolean> getNoHelp()

    Provider<Boolean> getNoNavBar()

    Provider<File> getHelpFile()

    Provider<File> getStylesheetFile()

    Provider<Boolean> getSerialWarn()

    Provider<String> getCharSet()

    Provider<String> getDocEncoding()

    Provider<Boolean> getKeyWords()

    Provider<List<String>> getTags()

    Provider<List<String>> getTaglets()

    ConfigurableFileCollection getTagletPath()

    Provider<Boolean> getDocFilesSubDirs()

    Provider<List<String>> getExcludeDocFilesSubDir()

    Provider<List<String>> getNoQualifiers()

    Provider<Boolean> getNoTimestamp()

    Provider<Boolean> getNoComment()

    Provider<Map<String, String>> getOfflineLinks()

    Provider<Map<String, Boolean>> getBooleanOptions()

    Provider<Map<String, String>> getStringOptions()

    Map<String, Map<String, Object>> toMap()
}
