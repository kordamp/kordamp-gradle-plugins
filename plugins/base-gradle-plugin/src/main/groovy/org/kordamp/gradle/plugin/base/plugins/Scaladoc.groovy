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
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.kordamp.gradle.plugin.base.model.ScaladocOptions

/**
 * @author Andres Almiray
 * @since 0.15.0
 */
@CompileStatic
interface Scaladoc extends Feature {
    String PLUGIN_ID = 'org.kordamp.gradle.scaladoc'

    Property<String> getTitle()

    Property<Boolean> getReplaceJavadoc()

    SetProperty<String> getExcludes()

    SetProperty<String> getIncludes()

    void include(String str)

    void exclude(String str)

    void options(Action<? super ScaladocOptions> action)

    void options(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ScaladocOptions) Closure<Void> action)

    void aggregate(Action<? super Aggregate> action)

    void aggregate(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Aggregate) Closure<Void> action)

    @CompileStatic
    interface Aggregate extends Feature {
        Property<Boolean> getFast()

        Property<Boolean> getReplaceJavadoc()

        SetProperty<Project> getExcludedProjects()

        void excludeProject(Project project)
    }
}
