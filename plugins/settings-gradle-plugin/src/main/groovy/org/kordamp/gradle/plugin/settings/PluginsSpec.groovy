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
 * Configures plugins for included projects.
 *
 * @author Andres Almiray
 * @since 0.35.0
 */
@CompileStatic
interface PluginsSpec {
    /**
     * Configures plugins for all projects.
     */
    void all(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PathsMatchingPluginsSpec) Closure<Void> action)

    /**
     * Configures plugins for a project matching by its directory.
     */
    void dir(String dir, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DirMatchingPluginsSpec) Closure<Void> action)

    /**
     * Configures plugins for all matching projects by their directories.
     */
    void dirs(List<String> dirs, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DirMatchingPluginsSpec) Closure<Void> action)

    /**
     * Configures plugins for a project matching by path.
     */
    void path(String path, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PathMatchingPluginsSpec) Closure<Void> action)

    /**
     * Configures plugins for all matching projects by their paths.
     */
    void paths(List<String> paths, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PathsMatchingPluginsSpec) Closure<Void> action)

    /**
     * Configures plugins for all projects.
     */
    void all(Action<? super PathsMatchingPluginsSpec> action)

    /**
     * Configures plugins for a project matching by its directory.
     */
    void dir(String dir, Action<? super DirMatchingPluginsSpec> action)

    /**
     * Configures plugins for all matching projects by their directories.
     */
    void dirs(List<String> dirs, Action<? super DirMatchingPluginsSpec> action)

    /**
     * Configures plugins for a project matching by path.
     */
    void path(String path, Action<? super PathMatchingPluginsSpec> action)

    /**
     * Configures plugins for all matching projects by their paths.
     */
    void paths(List<String> paths, Action<? super PathsMatchingPluginsSpec> action)

    /**
     * Configures a project path matching instruction.
     */
    interface PathMatchingPluginsSpec {
        /**
         * Defines a plugin id to apply to the matching project.
         * @param id a plugin id such as "java-library".
         */
        PluginIdSpec id(String pluginId)
    }

    /**
     * Configures a project path matching instruction for multiple paths.
     */
    interface PathsMatchingPluginsSpec extends PathMatchingPluginsSpec {
        /**
         * Excludes the given project matching its path. May be an exact project path match or a regex.
         */
        void excludePath(String path)
    }

    /**
     * Configures a directory matching instruction.
     */
    interface DirMatchingPluginsSpec {
        /**
         * Defines a plugin id to apply to the matching project.
         * @param id a plugin id such as "java-library".
         */
        PluginIdSpec id(String pluginId)

        /**
         * Excludes the given project matching its directory.
         * @deprecated As of release 0.41.0, replaced with {@link #excludeDir()} instead.
         */
        @Deprecated
        void exclude(String dir)

        /**
         * Excludes the given project matching its directory.
         */
        void excludeDir(String dir)

        /**
         * Excludes the given project matching its path. May be an exact project path match or a regex.
         */
        void excludePath(String path)
    }

    /**
     * Configures a plugin id definition
     */
    interface PluginIdSpec {
        /**
         * Applies the plugin if the condition evaluates to {@code true}.
         */
        void includeIf(boolean value)

        /**
         * Applies the plugin if the condition evaluates to {@code true}..
         */
        void includeIf(Supplier<Boolean> supplier)

        /**
         * Applies the plugin if the condition evaluates to {@code true}.
         * @deprecated As of release 0.41.0, replaced with {@link #includeIf()} instead.
         */
        @Deprecated
        void when(boolean value)

        /**
         * Applies the plugin if the condition evaluates to {@code true}.
         * @deprecated As of release 0.41.0, replaced with {@link #includeIf()} instead.
         */
        @Deprecated
        void when(Supplier<Boolean> supplier)
    }
}
