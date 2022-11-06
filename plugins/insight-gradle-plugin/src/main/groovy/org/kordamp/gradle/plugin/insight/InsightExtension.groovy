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
package org.kordamp.gradle.plugin.insight

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.kordamp.gradle.plugin.insight.model.BuildReport

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@CompileStatic
interface InsightExtension {
    Property<Boolean> getEnabled()

    void report(Class<? extends BuildReport> reportClass, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = BuildReport) Closure<Void> action)

    public <T extends BuildReport> void report(Class<T> reportClass, Action<T> action)

    void colors(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Colors) Closure<Void> action)

    void colors(Action<? super Colors> action)

    interface Colors {
        Property<String> getSuccess()

        Property<String> getFailure()

        Property<String> getSkipped()

        Property<String> getPartial()
    }
}
