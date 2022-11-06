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
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.internal.DefaultVersions

/**
 * @author Andres Almiray
 * @since 0.31.0
 */
@CompileStatic
class Detekt extends AbstractQualityFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.detekt'
    static final String KOTLIN_JVM_PLUGIN_ID = 'org.jetbrains.kotlin.jvm'

    File configFile
    File baselineFile
    boolean parallel = true
    boolean buildUponDefaultConfig = false
    boolean disableDefaultRuleSets = false
    boolean failFast = false

    private boolean parallelSet
    private boolean buildUponDefaultConfigSet
    private boolean disableDefaultRuleSetsSet
    private boolean failFastSet
    private boolean configFileSet

    Detekt(ProjectConfigurationExtension config, Project project) {
        super(config, project, PLUGIN_ID, 'detekt')
        toolVersion = DefaultVersions.INSTANCE.detektVersion
    }

    @Override
    protected AbstractFeature getParentFeature() {
        return project.rootProject.extensions.getByType(ProjectConfigurationExtension).quality.detekt
    }

    @Override
    protected void populateMapDescription(Map<String, Object> map) {
        super.populateMapDescription(map)
        map.configFile = this.configFile
        map.baselineFile = this.baselineFile
        map.parallel = this.parallel
        map.failFast = this.failFast
        map.buildUponDefaultConfig = this.buildUponDefaultConfig
        map.disableDefaultRuleSets = this.disableDefaultRuleSets
    }

    @Override
    void normalize() {
        if (null == configFile) {
            File file = project.rootProject.file("config/detekt/${project.name}.yml")
            if (!file.exists()) {
                file = project.rootProject.file('config/detekt/detekt.yml')
            }
            this.@configFile = file
        }

        super.normalize()
    }

    @Override
    protected boolean hasBasePlugin(Project project) {
        project.pluginManager.hasPlugin(KOTLIN_JVM_PLUGIN_ID)
    }

    void setConfigFile(File configFile) {
        this.configFile = configFile
        this.configFileSet = true
    }

    boolean isConfigFileSet() {
        return this.configFileSet
    }

    void setParallel(boolean parallel) {
        this.parallel = parallel
        this.parallelSet = true
    }

    boolean isParallelSet() {
        this.parallelSet
    }

    void setFailFast(boolean failFast) {
        this.failFast = failFast
        this.failFastSet = true
    }

    boolean isFailFastSet() {
        this.failFastSet
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

    static void merge(Detekt o1, Detekt o2) {
        AbstractQualityFeature.merge(o1, o2)
        o1.setParallel((boolean) (o1.parallelSet ? o1.parallel : o2.parallel))
        o1.setFailFast((boolean) (o1.failFastSet ? o1.failFast : o2.failFast))
        o1.setBuildUponDefaultConfig((boolean) (o1.buildUponDefaultConfigSet ? o1.buildUponDefaultConfig : o2.buildUponDefaultConfig))
        o1.setDisableDefaultRuleSets((boolean) (o1.disableDefaultRuleSetsSet ? o1.disableDefaultRuleSets : o2.disableDefaultRuleSets))
        if (!o1.configFileSet) {
            if (o2.configFileSet) o1.configFile = o2.configFile
        }
        o1.baselineFile = o1.baselineFile ?: o2.baselineFile
    }
}
