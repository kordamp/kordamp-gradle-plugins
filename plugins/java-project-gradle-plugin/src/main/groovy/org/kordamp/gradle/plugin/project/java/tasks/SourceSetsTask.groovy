/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2021 Andres Almiray.
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
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import org.kordamp.gradle.plugin.base.tasks.AbstractReportingTask
import org.kordamp.gradle.util.PluginUtils

/**
 * @author Andres Almiray
 * @since 0.30.0
 */
@CompileStatic
class SourceSetsTask extends AbstractReportingTask {
    @TaskAction
    void report() {
        Map<String, Map<String, ?>> map = [:]

        def sourceSets = PluginUtils.resolveSourceSets(project)
        if (sourceSets instanceof SourceSetContainer) {
            sourceSets.eachWithIndex { SourceSet sourceSet, int index ->
                map.putAll(SourceSetsTask.doReport(sourceSet, index))
            }
        }

        println('Total sourceSets: ' + console.cyan((map.size()).toString()) + '\n')
        doPrint(map, 0)
    }

    private static Map<String, Map<String, ?>> doReport(SourceSet sourceSet, int index) {
        Map<String, ?> map = [:]

        map.name = sourceSet.name

        new LinkedHashMap<>([('sourceSet ' + index): map])
    }
}
