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
package org.kordamp.gradle.plugin.settings

import groovy.transform.CompileStatic
import org.gradle.api.Action

import java.util.function.Supplier

/**
 * @author Andres Almiray
 * @since 0.35.0
 */
@CompileStatic
interface PluginsSpec {
    void all(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PathMatchingPluginsSpec) Closure<Void> action)

    void dir(String dir, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DirMatchingPluginsSpec) Closure<Void> action)

    void dirs(List<String> dirs, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DirMatchingPluginsSpec) Closure<Void> action)

    void path(String path, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PathMatchingPluginsSpec) Closure<Void> action)

    void paths(List<String> paths, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PathMatchingPluginsSpec) Closure<Void> action)

    void all(Action<? super PathMatchingPluginsSpec> action)

    void dir(String dir, Action<? super DirMatchingPluginsSpec> action)

    void dirs(List<String> dirs, Action<? super DirMatchingPluginsSpec> action)

    void path(String path, Action<? super PathMatchingPluginsSpec> action)

    void paths(List<String> paths, Action<? super PathMatchingPluginsSpec> action)

    interface PathMatchingPluginsSpec {
        PluginIdSpec id(String pluginId)
    }

    interface DirMatchingPluginsSpec {
        void exclude(String path)

        PluginIdSpec id(String pluginId)
    }

    interface PluginIdSpec {
        void when(boolean value)

        void when(Supplier<Boolean> supplier)
    }
}
