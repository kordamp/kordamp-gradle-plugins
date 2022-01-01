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

/**
 * @author Andres Almiray
 * @since 0.14.0
 */
@CompileStatic
interface Testing extends Feature {
    String PLUGIN_ID = 'org.kordamp.gradle.testing'

    Property<Boolean> getLogging()

    Property<Boolean> getAggregate()

    void integration(Action<? super Integration> action)

    void integration(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Integration) Closure<Void> action)

    void functional(Action<? super Functional> action)

    void functional(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Functional) Closure<Void> action)

    @CompileStatic
    interface Integration extends Feature {
        String PLUGIN_ID = 'org.kordamp.gradle.integration-test'

        Property<Boolean> getLogging()

        Property<Boolean> getAggregate()

        Property<String> getBaseDir()
    }

    @CompileStatic
    interface Functional extends Feature {
        String PLUGIN_ID = 'org.kordamp.gradle.functional-test'

        Property<Boolean> getLogging()

        Property<Boolean> getAggregate()

        Property<String> getBaseDir()
    }
}
