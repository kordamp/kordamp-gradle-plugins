/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
package org.kordamp.gradle.plugin.base.tasks

import org.gradle.api.plugins.ExtensionsSchema
import org.gradle.api.tasks.TaskAction

/**
 * @author Andres Almiray
 * @since 0.11.0
 */
class ExtensionsTask extends AbstractReportingTask {
    @TaskAction
    void report() {
        Map<String, Map<String, Object>> extensions = [:]

        project.extensions.extensionsSchema.elements.eachWithIndex { extension, index -> extensions.putAll(doReport(extension, index)) }

        doPrint(extensions, 0)
    }

    private Map<String, Map<String, ?>> doReport(ExtensionsSchema.ExtensionSchema extension, int index) {
        Map<String, ?> map = [:]

        map.name = extension.name
        map.type = extension.publicType.toString()

        [('extension ' + index): map]
    }
}
