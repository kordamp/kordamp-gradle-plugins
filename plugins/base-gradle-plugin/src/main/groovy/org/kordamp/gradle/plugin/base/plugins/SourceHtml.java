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
package org.kordamp.gradle.plugin.base.plugins;

import org.gradle.api.file.FileCollection;

import java.io.File;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public interface SourceHtml extends Feature {
    Conversion getConversion();

    Overview getOverview();

    interface Conversion {
        FileCollection getSrcDirs();

        File getDestDir();

        String getIncludes();

        String getOutputFormat();

        int getTabs();

        String getStyle();

        String getLineAnchorPrefix();

        String getHorizontalAlignment();

        boolean isShowLineNumbers();

        boolean isShowFileName();

        boolean isShowDefaultTitle();

        boolean isShowTableBorder();

        boolean isIncludeDocumentHeader();

        boolean isIncludeDocumentFooter();

        boolean isAddLineAnchors();

        boolean isUseShortFileName();

        boolean isOverwrite();
    }

    interface Overview {
        File getDestDir();

        String getPattern();

        String getWindowTitle();

        String getDocTitle();

        String getDocDescription();

        File getIcon();

        File getStylesheet();
    }
}
