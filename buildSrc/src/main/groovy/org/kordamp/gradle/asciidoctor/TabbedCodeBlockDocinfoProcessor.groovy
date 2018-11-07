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
package org.kordamp.gradle.asciidoctor

import org.asciidoctor.ast.Document
import org.asciidoctor.extension.DocinfoProcessor

/**
 * Based on https://github.com/bmuschko/gradle-docker-plugin/blob/master/buildSrc/src/main/kotlin/com/bmuschko/asciidoctor/TabbedCodeBlockDocinfoProcessor.kt
 *
 * @author Andres Almiray
 */
class TabbedCodeBlockDocinfoProcessor extends DocinfoProcessor {
    TabbedCodeBlockDocinfoProcessor() {
    }

    TabbedCodeBlockDocinfoProcessor(Map<String, Object> config) {
        super(config)
    }

    @Override
    String process(Document document) {
        String css = TabbedCodeBlockDocinfoProcessor.class.getResource('/codeBlockSwitch.css').text
        String javascript = TabbedCodeBlockDocinfoProcessor.class.getResource('/codeBlockSwitch.js').text

        """<style>
$css
</style>
<script src="https://cdnjs.cloudflare.com/ajax/libs/zepto/1.2.0/zepto.min.js"></script>
<script type="text/javascript">
$javascript
</script>
"""
    }
}
