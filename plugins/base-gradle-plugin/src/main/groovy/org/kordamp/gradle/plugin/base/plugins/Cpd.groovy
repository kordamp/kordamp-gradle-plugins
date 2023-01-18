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
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.internal.DefaultVersions

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
class Cpd extends AbstractQualityFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.cpd'

    int minimumTokenCount
    String encoding
    String language
    Boolean ignoreLiterals
    Boolean ignoreIdentifiers
    Boolean ignoreAnnotations

    Cpd(ProjectConfigurationExtension config, Project project) {
        super(config, project, PLUGIN_ID, 'cpd')
        toolVersion = DefaultVersions.INSTANCE.pmdVersion
    }

    @Override
    protected AbstractFeature getParentFeature() {
        return project.rootProject.extensions.getByType(ProjectConfigurationExtension).quality.cpd
    }

    @Override
    void postMerge() {
        minimumTokenCount = minimumTokenCount <= 0 ? 50 : minimumTokenCount
        encoding = encoding ?: 'UTF-8'
        language = language ?: 'java'
        super.postMerge()
    }

    @Override
    protected void populateMapDescription(Map<String, Object> map) {
        super.populateMapDescription(map)
        map.language = this.language
        map.encoding = this.encoding
        map.minimumTokenCount = this.minimumTokenCount
        map.ignoreLiterals = this.ignoreLiterals
        map.ignoreIdentifiers = this.ignoreIdentifiers
        map.ignoreAnnotations = this.ignoreAnnotations
    }

    @Override
    protected boolean hasBasePlugin(Project project) {
        project.pluginManager.hasPlugin('java-base')
    }

    boolean getIgnoreLiterals() {
        null != ignoreLiterals && ignoreLiterals
    }

    boolean getIgnoreIdentifiers() {
        null != ignoreIdentifiers && ignoreIdentifiers
    }

    boolean getIgnoreAnnotations() {
        null != ignoreAnnotations && ignoreAnnotations
    }

    static void merge(Cpd o1, Cpd o2) {
        AbstractQualityFeature.merge(o1, o2)
        o1.language = o1.language ?: o2.language
        o1.encoding = o1.encoding ?: o2.encoding
        o1.minimumTokenCount = o1.minimumTokenCount ?: o2.minimumTokenCount
        o1.ignoreLiterals = o1.@ignoreLiterals != null ? o1.getIgnoreLiterals() : o2.getIgnoreLiterals()
        o1.ignoreIdentifiers = o1.@ignoreIdentifiers != null ? o1.getIgnoreIdentifiers() : o2.getIgnoreIdentifiers()
        o1.ignoreAnnotations = o1.@ignoreAnnotations != null ? o1.getIgnoreAnnotations() : o2.getIgnoreAnnotations()
    }
}
