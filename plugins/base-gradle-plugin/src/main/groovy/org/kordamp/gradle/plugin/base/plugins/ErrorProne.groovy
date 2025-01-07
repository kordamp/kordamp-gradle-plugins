/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2025 Andres Almiray.
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
import org.kordamp.gradle.util.CollectionUtils

/**
 * @author Andres Almiray
 * @since 0.31.0
 */
@CompileStatic
class ErrorProne extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.errorprone'

    List<String> args = []
    Boolean disableAllChecks
    Boolean allErrorsAsWarnings
    Boolean allDisabledChecksAsWarnings
    Boolean disableWarningsInGeneratedCode = true
    Boolean ignoreUnknownCheckNames
    Boolean ignoreSuppressionAnnotations
    Boolean compilingTestOnlyCode
    String excludedPaths
    String errorProneVersion = DefaultVersions.INSTANCE.errorproneVersion
    String errorProneJavacVersion = '9+181-r4173-1'

    ErrorProne(ProjectConfigurationExtension config, Project project) {
        super(config, project, PLUGIN_ID)
    }

    @Override
    protected AbstractFeature getParentFeature() {
        return project.rootProject.extensions.getByType(ProjectConfigurationExtension).quality.errorprone
    }

    @Override
    protected boolean hasBasePlugin(Project project) {
        project.pluginManager.hasPlugin('java')
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        new LinkedHashMap<>('erorprone': new LinkedHashMap<String, Object>(
            enabled: enabled,
            disableAllChecks: disableAllChecks,
            allErrorsAsWarnings: allErrorsAsWarnings,
            allDisabledChecksAsWarnings: allDisabledChecksAsWarnings,
            disableWarningsInGeneratedCode: disableWarningsInGeneratedCode,
            ignoreUnknownCheckNames: ignoreUnknownCheckNames,
            ignoreSuppressionAnnotations: ignoreSuppressionAnnotations,
            compilingTestOnlyCode: compilingTestOnlyCode,
            excludedPaths: excludedPaths,
            erroProneVersion: errorProneVersion,
            erroProneJavacVersion: errorProneJavacVersion,
            args: args
        ))
    }

    boolean getDisableAllChecks() {
        null != disableAllChecks && disableAllChecks
    }

    boolean getAllErrorsAsWarnings() {
        null != allErrorsAsWarnings && allErrorsAsWarnings
    }

    boolean getAllDisabledChecksAsWarnings() {
        null != allDisabledChecksAsWarnings && allDisabledChecksAsWarnings
    }

    boolean getDisableWarningsInGeneratedCode() {
        null != disableWarningsInGeneratedCode && disableWarningsInGeneratedCode
    }

    boolean getIgnoreUnknownCheckNames() {
        null != ignoreUnknownCheckNames && ignoreUnknownCheckNames
    }

    boolean getIgnoreSuppressionAnnotations() {
        null != ignoreSuppressionAnnotations && ignoreSuppressionAnnotations
    }

    boolean getCompilingTestOnlyCode() {
        null != compilingTestOnlyCode && compilingTestOnlyCode
    }

    static void merge(ErrorProne o1, ErrorProne o2) {
        AbstractFeature.merge(o1, o2)
        o1.disableAllChecks = o1.@disableAllChecks != null ? o1.getDisableAllChecks() : o2.getDisableAllChecks()
        o1.allErrorsAsWarnings = o1.@allErrorsAsWarnings != null ? o1.getAllErrorsAsWarnings() : o2.getAllErrorsAsWarnings()
        o1.allDisabledChecksAsWarnings = o1.@allDisabledChecksAsWarnings != null ? o1.getAllDisabledChecksAsWarnings() : o2.getAllDisabledChecksAsWarnings()
        o1.disableWarningsInGeneratedCode = o1.@disableWarningsInGeneratedCode != null ? o1.getDisableWarningsInGeneratedCode() : o2.getDisableWarningsInGeneratedCode()
        o1.ignoreUnknownCheckNames = o1.@ignoreUnknownCheckNames != null ? o1.getIgnoreUnknownCheckNames() : o2.getIgnoreUnknownCheckNames()
        o1.ignoreSuppressionAnnotations = o1.@ignoreSuppressionAnnotations != null ? o1.getIgnoreSuppressionAnnotations() : o2.getIgnoreSuppressionAnnotations()
        o1.compilingTestOnlyCode = o1.@compilingTestOnlyCode != null ? o1.getCompilingTestOnlyCode() : o2.getCompilingTestOnlyCode()
        o1.excludedPaths = o1.excludedPaths ?: o2.excludedPaths
        o1.errorProneVersion = o1.errorProneVersion ?: o2.errorProneVersion
        o1.errorProneJavacVersion = o1.errorProneJavacVersion ?: o2.errorProneJavacVersion
        o1.args = CollectionUtils.merge(o1.args, o2?.args, false)
    }
}
