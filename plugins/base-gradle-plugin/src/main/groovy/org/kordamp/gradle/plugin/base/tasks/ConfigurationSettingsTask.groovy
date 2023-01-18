/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2023 Andres Almiray.
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
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.24.0
 */
@CompileStatic
class ConfigurationSettingsTask extends AbstractReportingTask {
    @Input @Optional String configuration
    @Input @Optional Set<String> configurations

    private final Property<Boolean> showPaths = project.objects.property(Boolean)

    @Option(option = 'show-paths', description = 'Display path information (OPTIONAL).')
    void setShowPaths(boolean showPaths) {
        this.showPaths.set(showPaths)
    }

    @Input
    boolean isShowPaths() {
        showPaths.getOrElse(false)
    }

    @Option(option = 'configuration', description = 'The configuration to generate the report for.')
    void setConfiguration(String configuration) {
        this.configuration = configuration
    }

    @Option(option = 'configurations', description = 'The configurations to generate the report for.')
    void setConfigurations(String configurations) {
        if (isNotBlank(configurations)) {
            this.configurations = (configurations.split(',').collect { it.trim() }) as Set
        }
    }

    @TaskAction
    void report() {
        if (configurations) {
            configurations.each { c ->
                printConfiguration((Configuration) project.configurations.findByName(c))
            }
        } else if (configuration) {
            try {
                printConfiguration((Configuration) project.configurations.findByName(configuration))
            } catch (NullPointerException e) {
                throw new IllegalStateException("No matching '${this.configuration}' configuration was found")
            }
        } else {
            project.configurations.each { c ->
                printConfiguration(c)
            }
        }
    }

    private void printConfiguration(Configuration configuration) {
        print(configuration.name + ':', 0)
        configuration.properties.sort().each { name, value ->
            if (value instanceof FileCollection ||
                (name in ['class', 'name', 'all', 'asDynamicObject', 'asFileTree', 'asFileTrees', 'asPath', 'convention', 'conventionMapping', 'buildDependencies',
                          'dependenciesResolver', 'dependencyResolutionListeners', 'extensions', 'outgoing', 'module', 'resolutionStrategy', 'resolvedConfiguration',
                          'allArtifacts', 'allDependencies', 'artifacts'])) {
                return
            }
            if (value instanceof Collection) {
                doPrintCollection(name.toString(), (Collection) value, 1)
            } else {
                try {
                    doPrintMapEntry(name.toString(), value, 1)
                } catch (IllegalStateException ise) {
                    if (configuration.canBeResolved) {
                        throw ise
                    }
                }
            }
        }
        if (isShowPaths() && configuration.canBeResolved) {
            configuration.properties.sort().each { name, value ->
                if (value instanceof FileCollection) {
                    doPrintCollection(name.toString(), value, 1)
                }
            }
        }

        println ' '
    }
}
