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
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.kordamp.gradle.plugin.base.model.LicenseId

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
interface Licensing extends Feature {
    String PLUGIN_ID = 'org.kordamp.gradle.licensing'

    Property<String> getMergeStrategy()

    void licenses(Action<? super LicenseSet> action)

    void licenses(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = LicenseSet) Closure<Void> action)

    void excludeSourceSet(String sourceSetName)

    @CompileStatic
    interface LicenseSet {
        void license(Action<? super License> action)

        void license(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = License) Closure<Void> action)
    }

    @CompileStatic
    interface License {
        Property<LicenseId> getId()

        Property<String> getName()

        Property<String> getUrl()

        Property<String> getDistribution()

        Property<String> getComments()

        Property<Boolean> getPrimary()

        SetProperty<String> getAliases()
    }
}
