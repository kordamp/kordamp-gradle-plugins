/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2020 Andres Almiray.
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
import org.gradle.api.Action
import org.kordamp.gradle.plugin.base.model.LicenseId

import java.security.Provider

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@CompileStatic
interface ResolvedLicensing extends ResolvedFeature {
    String PLUGIN_ID = 'org.kordamp.gradle.licensing'

    Provider<String> getMergeStrategy()

    ResolvedLicenseSet getLicenses()

    Provider<Set<String>> getExcludedSourceSets()

    @CompileStatic
    interface ResolvedLicenseSet {
        Map<String, Map<String, Object>> toMap()

        boolean isEmpty()

        void forEach(Action<? super ResolvedLicense> action)

        void forEach(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ResolvedLicense) Closure<Void> action)
    }

    @CompileStatic
    interface ResolvedLicense {
        Provider<LicenseId> getId()

        Provider<String> getName()

        Provider<String> getUrl()

        Provider<String> getDistribution()

        Provider<String> getComments()

        Provider<Boolean> getPrimary()

        Provider<Set<String>> getAliases()

        Map<String, Object> toMap()
    }
}
