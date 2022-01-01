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
package org.kordamp.gradle.plugin.base.resolved.plugins

import groovy.transform.CompileStatic
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Provider

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@CompileStatic
interface ResolvedSourceHtml extends ResolvedFeature {
    String PLUGIN_ID = 'org.kordamp.gradle.source-html'

    ConfigurableFileCollection getSrcDirs()

    ResolvedConversion getConversion()

    ResolvedOverview getOverview()

    @CompileStatic
    interface ResolvedConversion {
        Map<String, Map<String, Object>> toMap()

        ConfigurableFileCollection getSrcDirs()

        Provider<File> getDestDir()

        Provider<String> getIncludes()

        Provider<String> getOutputFormat()

        Provider<Integer> getTabs()

        Provider<String> getStyle()

        Provider<String> getLineAnchorPrefix()

        Provider<String> getHorizontalAlignment()

        Provider<Boolean> getShowLineNumbers()

        Provider<Boolean> getShowFileName()

        Provider<Boolean> getShowDefaultTitle()

        Provider<Boolean> getShowTableBorder()

        Provider<Boolean> getIncludeDocumentHeader()

        Provider<Boolean> getIncludeDocumentFooter()

        Provider<Boolean> getAddLineAnchors()

        Provider<Boolean> getUseShortFileName()

        Provider<Boolean> getOverwrite()
    }

    @CompileStatic
    interface ResolvedOverview {
        Map<String, Map<String, Object>> toMap()

        Provider<File> getDestDir()

        Provider<String> getPattern()

        Provider<String> getWindowTitle()

        Provider<String> getDocTitle()

        Provider<String> getDocDescription()

        Provider<File> getIcon()

        Provider<File> getStylesheet()
    }
}
