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
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

import java.util.function.Supplier

/**
 * Conventions for included projects in a build.
 *
 * @author Andres Almiray
 * @since 0.15.0
 */
@CompileStatic
interface ProjectsExtension {
    enum FileNameTransformation {
        /** Add text to the file name (see suffix, prefix) */
        ADD,
        /** Remove text from the file name (see suffix, prefix) */
        REMOVE
    }

    enum Layout {
        /** Projects organized by a common parent */
        TWO_LEVEL,
        /** Projects organized at mixed levels */
        MULTI_LEVEL,
        /** Projects adjacent to the root */
        STANDARD,
        /** Custom combination */
        EXPLICIT
    }

    Property<String> getLayout()

    void setLayout(String layout)

    Property<Boolean> getEnforceNamingConvention()

    Property<Boolean> getUseLongPaths()

    Property<Boolean> getCache()

    ListProperty<String> getDirectories()

    ListProperty<String> getExcludes()

    /**
     * The prefix to add/remove to/from the build file name.
     */
    Property<String> getPrefix()

    /**
     * The suffix to add/remove to/from the build file name.
     */
    Property<String> getSuffix()

    /**
     * The transformation to be applied to the build file name.
     */
    Property<FileNameTransformation> getFileNameTransformation()

    /**
     * Sets the transformation to be applied to the build file name.
     * Valid values are {@code ADD}, {@code REMOVE}.
     */
    void setFileNameTransformation(String transformation)

    /**
     * Configures plugins for all included projects.
     */
    void plugins(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PluginsSpec) Closure<Void> action)

    /**
     * Configures plugins for all included projects.
     */
    void plugins(Action<? super PluginsSpec> action)

    /**
     * Includes matching projects in the given directory.
     * Projects must be direct children of the given argument.
     *
     * @param dir the parent directory of the projects to be included.
     */
    DirectorySpec includeProjects(String dir)

    /**
     * Includes a single project.
     * @param dir the project directory.
     */
    PathSpec includeProject(String dir)

    /**
     * Includes matching projects in the given directory.
     * Projects must be direct children of the given argument.
     *
     * @param dir the parent directory of the projects to be included.
     * @deprecated As of release 0.41.0, replaced with {@link #includeProjects()} instead.
     */
    @Deprecated
    DirectorySpec includeFromDir(String dir)

    /**
     * Includes a single project.
     * @param dir the project directory.
     * @deprecated As of release 0.41.0, replaced with {@link #includeProject()} instead.
     */
    @Deprecated
    PathSpec includeFromPath(String path)

    /**
     * Configures multiple projects inside a parent directory
     */
    interface DirectorySpec {
        /**
         * Excludes the given directory from the build.
         *
         * @param projectName the exact match to the project's directory name.
         */
        DirectorySpec exclude(String projectName)

        /**
         * Includes the project if the input is {@code true}.
         */
        void includeIf(boolean value)

        /**
         * Applies the project if the condition evaluates to {@code true}.
         */
        void includeIf(Supplier<Boolean> supplier)

        /**
         * Includes the project if the input is {@code true}.
         * @deprecated As of release 0.41.0, replaced with {@link #includeIf()} instead.
         */
        @Deprecated
        void when(boolean value)

        /**
         * Applies the project if the condition evaluates to {@code true}.
         * @deprecated As of release 0.41.0, replaced with {@link #includeIf()} instead.
         */
        @Deprecated
        void when(Supplier<Boolean> supplier)
    }

    /**
     * Configures a single project by directory path.
     */
    interface PathSpec {
        /**
         * Includes the project if the input is {@code true}.
         */
        void includeIf(boolean value)

        /**
         * Applies the project if the condition evaluates to {@code true}.
         */
        void includeIf(Supplier<Boolean> supplier)

        /**
         * Includes the project if the input is {@code true}.
         * @deprecated As of release 0.41.0, replaced with {@link #includeIf()} instead.
         */
        @Deprecated
        void when(boolean value)

        /**
         * Applies the project if the condition evaluates to {@code true}.
         * @deprecated As of release 0.41.0, replaced with {@link #includeIf()} instead.
         */
        @Deprecated
        void when(Supplier<Boolean> supplier)
    }
}
