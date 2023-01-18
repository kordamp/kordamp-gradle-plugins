/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2023 Andres Almiray.
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
package org.kordamp.gradle.plugin.project

import groovy.transform.CompileStatic

/**
 * @author Andres Almiray
 * @since 0.41.0
 */
@CompileStatic
interface ConfigurationsDependencyHandler {
    /**
     * Declares the target configuration on which platforms and dependencies will be applied.
     * @param configuration configuration the target configuration, e.g, <tt>api</tt>.
     * @return
     */
    DependencyHandlerSpec configuration(String configuration)

    /**
     * Declares target configurations on which platforms and dependencies will be applied.
     *
     * @param configuration the target configuration, e.g, <tt>api</tt>.
     * @param configurations additional configurations (if any).
     * @return
     */
    DependencyHandlerSpec configurations(String configuration, String... configurations)

    /**
     * Declares target configurations on which platforms and dependencies will be applied.
     *
     * @param configurations - the set of configurations to use. Can not be empty.
     * @return
     */
    DependencyHandlerSpec configurations(Set<String> configurations)

    /**
     * Declares the target configuration on which platforms and dependencies will be applied.
     * Alias for {@code configuration(configuration)}.
     *
     * @param configuration configuration the target configuration, e.g, <tt>api</tt>.
     * @return
     */
    DependencyHandlerSpec c(String configuration)

    /**
     * Declares target configurations on which platforms and dependencies will be applied.
     * Alias for {@code configurations(configuration,configurations)}.
     *
     * @param configuration the target configuration, e.g, <tt>api</tt>.
     * @param configurations additional configurations (if any).
     * @return
     */
    DependencyHandlerSpec c(String configuration, String... configurations)

    /**
     * Declares target configurations on which platforms and dependencies will be applied.
     * Alias for {@code configurations(configurations)}.
     *
     * @param configurations - the set of configurations to use. Can not be empty.
     * @return
     */
    DependencyHandlerSpec c(Set<String> configurations)
}
