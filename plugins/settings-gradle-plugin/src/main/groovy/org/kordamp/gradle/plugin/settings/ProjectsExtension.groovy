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
package org.kordamp.gradle.plugin.settings

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

import java.util.function.Supplier

/**
 * @author Andres Almiray
 * @since 0.15.0
 */
@CompileStatic
interface ProjectsExtension {
    Property<String> getLayout()

    Property<Boolean> getEnforceNamingConvention()

    ListProperty<String> getDirectories()

    ListProperty<String> getExcludes()

    Property<String> getPrefix()

    Property<String> getSuffix()

    Property<String> getFileNameTransformation()

    void plugins(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PluginsSpec) Closure<Void> action)

    void plugins(Action<? super PluginsSpec> action)

    DirectorySpec includeFromDir(String dir)

    PathSpec includeFromPath(String path)

    interface DirectorySpec {
        DirectorySpec exclude(String projectName)

        void when(boolean value)

        void when(Supplier<Boolean> supplier)
    }

    interface PathSpec {
        void when(boolean value)

        void when(Supplier<Boolean> supplier)
    }
}
