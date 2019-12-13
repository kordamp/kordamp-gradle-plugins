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
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

/**
 * @author Andres Almiray
 * @since 0.31.0
 */
@CompileStatic
@Canonical
class Detekt extends AbstractFeature {
    File configFile
    File baselineFile
    boolean parallel = true
    boolean buildUponDefaultConfig = false
    boolean disableDefaultRuleSets = false
    boolean ignoreFailures = true
    String toolVersion = '1.2.2'

    private boolean parallelSet
    private boolean buildUponDefaultConfigSet
    private boolean disableDefaultRuleSetsSet
    private boolean ignoreFailuresSet

    Detekt(ProjectConfigurationExtension config, Project project) {
        super(config, project)
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        new LinkedHashMap<>('detekt': new LinkedHashMap<String, Object>(
            enabled: enabled,
            configFile: configFile,
            baselineFile: baselineFile,
            parallel: parallel,
            buildUponDefaultConfig: buildUponDefaultConfig,
            disableDefaultRuleSets: disableDefaultRuleSets,
            ignoreFailures: ignoreFailures,
            toolVersion: toolVersion
        ))
    }

    void normalize() {
        if (null == configFile) {
            File file = project.rootProject.file("config/detekt/${project.name}.yml")
            if (!file.exists()) {
                file = project.rootProject.file('config/detekt/detekt.yml')
            }
            configFile = file
        }

        if (!enabledSet) {
            if (isRoot()) {
                if (project.childProjects.isEmpty()) {
                    enabled = project.pluginManager.hasPlugin('org.jetbrains.kotlin.jvm')
                } else {
                    enabled = project.childProjects.values().any { p -> p.pluginManager.hasPlugin('org.jetbrains.kotlin.jvm') }
                }
            } else {
                enabled = project.pluginManager.hasPlugin('org.jetbrains.kotlin.jvm')
            }
        }
    }

    void setParallel(boolean parallel) {
        this.parallel = parallel
        this.parallelSet = true
    }

    boolean isParallelSet() {
        this.parallelSet
    }

    void setBuildUponDefaultConfig(boolean buildUponDefaultConfig) {
        this.buildUponDefaultConfig = buildUponDefaultConfig
        this.buildUponDefaultConfigSet = true
    }

    boolean isBuildUponDefaultConfigSet() {
        this.buildUponDefaultConfigSet
    }

    void setDisableDefaultRuleSets(boolean disableDefaultRuleSets) {
        this.disableDefaultRuleSets = disableDefaultRuleSets
        this.disableDefaultRuleSetsSet = true
    }

    boolean isDisableDefaultRuleSetsSet() {
        this.disableDefaultRuleSetsSet
    }

    void setIgnoreFailures(boolean ignoreFailures) {
        this.ignoreFailures = ignoreFailures
        this.ignoreFailuresSet = true
    }

    boolean isIgnoreFailuresSet() {
        this.ignoreFailuresSet
    }

    void copyInto(Detekt copy) {
        super.copyInto(copy)
        copy.@parallel = parallel
        copy.@parallelSet = parallelSet
        copy.@buildUponDefaultConfig = buildUponDefaultConfig
        copy.@buildUponDefaultConfigSet = buildUponDefaultConfigSet
        copy.@disableDefaultRuleSets = disableDefaultRuleSets
        copy.@disableDefaultRuleSetsSet = disableDefaultRuleSetsSet
        copy.@ignoreFailures = ignoreFailures
        copy.@ignoreFailuresSet = ignoreFailuresSet
        copy.configFile = configFile
        copy.baselineFile = baselineFile
        copy.toolVersion = toolVersion
    }

    static void merge(Detekt o1, Detekt o2) {
        AbstractFeature.merge(o1, o2)
        o1.setParallel((boolean) (o1.parallelSet ? o1.parallel : o2.parallel))
        o1.setBuildUponDefaultConfig((boolean) (o1.buildUponDefaultConfigSet ? o1.buildUponDefaultConfig : o2.buildUponDefaultConfig))
        o1.setDisableDefaultRuleSets((boolean) (o1.disableDefaultRuleSetsSet ? o1.disableDefaultRuleSets : o2.disableDefaultRuleSets))
        o1.setIgnoreFailures((boolean) (o1.ignoreFailuresSet ? o1.ignoreFailures : o2.ignoreFailures))
        o1.configFile = o1.configFile ?: o2.configFile
        o1.baselineFile = o1.baselineFile ?: o2.baselineFile
        o1.toolVersion = o1.toolVersion ?: o2.toolVersion
    }
}
