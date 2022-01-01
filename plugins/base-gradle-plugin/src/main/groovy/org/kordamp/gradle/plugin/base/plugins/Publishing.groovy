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
import org.kordamp.gradle.plugin.base.model.PomOptions

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
interface Publishing extends Feature {
    String PLUGIN_ID = 'org.kordamp.gradle.publishing'

    Property<String> getReleasesRepository()

    Property<String> getSnapshotsRepository()

    Property<Boolean> getSigning()

    SetProperty<String> getPublications()

    SetProperty<String> getScopes()

    Property<Boolean> getFilterDependencies()

    void pom(Action<? super PomOptions> action)

    void pom(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PomOptions) Closure<Void> action)
}
