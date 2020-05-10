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
package org.kordamp.gradle.plugin.project.java

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.artifacts.Dependency

/**
 *
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
interface PlatformHandler {
    void platform(Object platformGav)

    void platform(Object platformGav, Action<? super Dependency> action)

    void platform(Object platformGav, String... configurations)

    void platform(Object platformGav, List<String> configurations)

    void platform(Object platformGav, List<String> configurations, Action<? super Dependency> action)

    void enforcedPlatform(Object platformGav)

    void enforcedPlatform(Object platformGav, Action<? super Dependency> action)

    void enforcedPlatform(Object platformGav, String... configurations)

    void enforcedPlatform(Object platformGav, List<String> configurations)

    void enforcedPlatform(Object platformGav, List<String> configurations, Action<? super Dependency> action)
}
