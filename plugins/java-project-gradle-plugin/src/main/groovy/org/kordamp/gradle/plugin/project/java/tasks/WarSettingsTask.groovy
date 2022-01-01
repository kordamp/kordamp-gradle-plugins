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
package org.kordamp.gradle.plugin.project.java.tasks

import groovy.transform.CompileStatic
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.War
import org.kordamp.gradle.plugin.base.tasks.AbstractSettingsTask

/**
 * @author Andres Almiray
 * @since 0.30.0
 */
@CompileStatic
class WarSettingsTask extends AbstractSettingsTask {
    @TaskAction
    void report() {
        if (tasks) {
            tasks.each { t ->
                printTask((War) project.tasks.findByName(t))
            }
        } else if (task) {
            try {
                printTask((War) project.tasks.findByName(task))
            } catch (NullPointerException e) {
                throw new IllegalStateException("No matching '${this.task}' task was found")
            }
        } else {
            Set<War> compileTasks = new LinkedHashSet<>(project.tasks.withType(War))
            compileTasks.each { t ->
                printTask(t)
            }
        }
    }

    private void printTask(War task) {
        print(task.name + ':', 0)
        doPrintMapEntry('archiveDestinationDirectory', task.destinationDirectory.orNull, 1)
        doPrintMapEntry('archiveFile', task.archiveFile.orNull, 1)
        doPrintMapEntry('archiveName', task.archiveFileName.orNull, 1)
        doPrintMapEntry('archiveBaseName', task.archiveBaseName.orNull, 1)
        doPrintMapEntry('archiveAppendix', task.archiveAppendix.orNull, 1)
        doPrintMapEntry('archiveVersion', task.archiveVersion.orNull, 1)
        doPrintMapEntry('archiveExtension', task.archiveExtension.orNull, 1)
        doPrintMapEntry('archiveClassifier', task.archiveClassifier.orNull, 1)
        doPrintMapEntry('preserveFileTimestamps', task.preserveFileTimestamps, 1)
        doPrintMapEntry('reproducibleFileOrder', task.reproducibleFileOrder, 1)
        doPrintMapEntry('dirMode', task.dirMode, 1)
        doPrintMapEntry('fileMode', task.fileMode, 1)
        doPrintMapEntry('caseSensitive', task.caseSensitive, 1)
        doPrintMapEntry('includeEmptyDirs', task.includeEmptyDirs, 1)
        doPrintMapEntry('duplicatesStrategy', task.duplicatesStrategy, 1)
        doPrintMapEntry('webXml', task.webXml, 1)
        doPrintCollection('includes', task.includes, 1)
        doPrintCollection('excludes', task.excludes, 1)

        print('manifest:', 1)
        doPrintMap('attributes', task.manifest.attributes, 2)
        if (isShowPaths()) {
            doPrintCollection('source', task.source, 1)
            doPrintCollection('classpath', task.classpath, 1)
        }
        println ' '
    }
}
