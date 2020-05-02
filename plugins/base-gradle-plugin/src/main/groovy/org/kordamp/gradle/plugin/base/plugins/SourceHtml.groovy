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
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
interface SourceHtml extends Feature {
    String PLUGIN_ID = 'org.kordamp.gradle.source-html'

    ConfigurableFileCollection getSrcDirs()

    void conversion(Action<? super Conversion> action)

    void conversion(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Conversion) Closure<Void> action)

    void overview(Action<? super Overview> action)

    void overview(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Overview) Closure<Void> action)

    @CompileStatic
    interface Conversion {
        ConfigurableFileCollection getSrcDirs()

        RegularFileProperty getDestDir()

        Property<String> getIncludes()

        Property<String> getOutputFormat()

        Property<Integer> getTabs()

        Property<String> getStyle()

        Property<String> getLineAnchorPrefix()

        Property<String> getHorizontalAlignment()

        Property<Boolean> getShowLineNumbers()

        Property<Boolean> getShowFileName()

        Property<Boolean> getShowDefaultTitle()

        Property<Boolean> getShowTableBorder()

        Property<Boolean> getIncludeDocumentHeader()

        Property<Boolean> getIncludeDocumentFooter()

        Property<Boolean> getAddLineAnchors()

        Property<Boolean> getUseShortFileName()

        Property<Boolean> getOverwrite()
    }

    @CompileStatic
    interface Overview {
        RegularFileProperty getDestDir()

        Property<String> getPattern()

        Property<String> getWindowTitle()

        Property<String> getDocTitle()

        Property<String> getDocDescription()

        RegularFileProperty getIcon()

        RegularFileProperty getStylesheet()
    }
}
