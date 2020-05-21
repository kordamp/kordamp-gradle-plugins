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
package org.kordamp.gradle.plugin.base.tasks

import groovy.transform.CompileStatic
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.31.0
 */
@CompileStatic
class ExtensionSettingsTask extends AbstractReportingTask {
    @Input
    @Optional
    String ext
    @Input
    @Optional
    Set<String> exts

    @Option(option = 'extension', description = 'The extension to generate the report for.')
    void setExtension(String extension) {
        this.ext = extension
    }

    @Option(option = 'extensions', description = 'The extensions to generate the report for.')
    void setExtensions(String extensions) {
        if (isNotBlank(extensions)) {
            this.exts = (extensions.split(',').collect { it.trim() }) as Set
        }
    }

    @TaskAction
    void report() {
        if (exts) {
            exts.each { e ->
                printExtension(e, project.extensions.findByName(e))
            }
        } else if (ext) {
            try {
                printExtension(ext, project.extensions.findByName(ext))
            } catch (NullPointerException e) {
                throw new IllegalStateException("No matching '${this.ext}' extension was found")
            }
        } else {
            project.extensions.extensionsSchema.elements.each { e ->
                printExtension(e.name, project.extensions.findByName(e.name))
            }
        }
    }

    private void printExtension(String extensionName, Object extension) {
        print(extensionName + ':', 0)
        extension.properties.sort().each { name, value ->
            if (name in ['class', 'metaClass', 'asDynamicObject']) {
                return
            }
            if (value instanceof Collection) {
                doPrintCollection(name.toString(), (Collection) value, 1)
            } else {
                doPrintMapEntry(name.toString(), value, 1)
            }
        }

        println ' '
    }
}
