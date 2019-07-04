/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

import static org.kordamp.gradle.PluginUtils.resolveSourceSets
import static org.kordamp.gradle.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.24.0
 */
@CompileStatic
class SourceSetSettingsTask extends AbstractReportingTask {
    protected String sourceSet
    protected Set<String> sourceSets

    private final Property<Boolean> showPaths = project.objects.property(Boolean)

    @Option(option = 'show-paths', description = 'Display path information (OPTIONAL).')
    void setShowPaths(boolean showPaths) {
        this.showPaths.set(showPaths)
    }

    boolean isShowPaths() {
        showPaths.getOrElse(false)
    }

    @Option(option = 'sourceSet', description = 'The sourceSet to generate the report for.')
    void setSourceSet(String sourceSet) {
        this.sourceSet = sourceSet
    }

    @Option(option = 'sourceSets', description = 'The sourceSets to generate the report for.')
    void setSourceSets(String sourceSets) {
        if (isNotBlank(sourceSets)) {
            this.sourceSets = (sourceSets.split(',').collect { it.trim() }) as Set
        }
    }

    @TaskAction
    void report() {
        def ss = resolveSourceSets(project)
        if (ss instanceof SourceSetContainer) {
            if (sourceSets) {
                sourceSets.each { s ->
                    printSourceSet((SourceSet) ((SourceSetContainer) ss).findByName(s))
                }
            } else if (sourceSet) {
                try {
                    printSourceSet((SourceSet) ((SourceSetContainer) ss).findByName(sourceSet))
                } catch (NullPointerException e) {
                    throw new IllegalStateException("No matching ${this.sourceSet} sourceSet was found")
                }
            } else {
                ((SourceSetContainer) ss).each { s ->
                    printSourceSet(s)
                }
            }
        }
    }

    private void printSourceSet(SourceSet sourceSet) {
        print(sourceSet.name + ':', 0)
        sourceSet.properties.sort().each { name, value ->
            if (value instanceof FileCollection ||
                (name in ['asDynamicObject', 'class', 'convention', 'conventionMapping', 'extensions', 'name'])) {
                return
            }
            doPrintMapEntry(name.toString(), value, 1)
        }
        if (isShowPaths()) {
            sourceSet.properties.sort().each { name, value ->
                if (value instanceof FileCollection) {
                    doPrintCollection(name.toString(), value, 1)
                }
            }
        }

        println ' '
    }
}
