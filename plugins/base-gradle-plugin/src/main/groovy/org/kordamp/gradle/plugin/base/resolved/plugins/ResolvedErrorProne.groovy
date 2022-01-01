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
package org.kordamp.gradle.plugin.base.resolved.plugins

import groovy.transform.CompileStatic
import org.gradle.api.provider.Provider

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@CompileStatic
interface ResolvedErrorProne extends ResolvedFeature {
    String PLUGIN_ID = 'org.kordamp.gradle.errorprone'

    Provider<Boolean> getDisableAllChecks()

    Provider<Boolean> getAllErrorsAsWarnings()

    Provider<Boolean> getAllDisabledChecksAsWarnings()

    Provider<Boolean> getDisableWarningsInGeneratedCode()

    Provider<Boolean> getIgnoreUnknownCheckNames()

    Provider<Boolean> getIgnoreSuppressionAnnotations()

    Provider<Boolean> getCompilingTestOnlyCode()

    Provider<String> getExcludedPaths()

    Provider<String> getErrorProneVersion()

    Provider<String> getErrorProneJavacVersion()
}
