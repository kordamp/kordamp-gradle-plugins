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
import org.kordamp.gradle.plugin.base.resolved.model.ResolvedPomOptions

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@CompileStatic
interface ResolvedBom extends ResolvedFeature, ResolvedPomOptions {
    String PLUGIN_ID = 'org.kordamp.gradle.bom'

    Provider<Set<String>> getCompile()

    Provider<Set<String>> getRuntime()

    Provider<Set<String>> getTest()

    Provider<Set<String>> getImport()

    Provider<Set<String>> getExcludes()

    Provider<Set<String>> getIncludes()

    Provider<Boolean> getAutoIncludes()
}
