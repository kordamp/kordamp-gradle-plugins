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
package org.kordamp.gradle.plugin.base

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project

import java.util.function.Function

/**
 * @author Andres Almiray
 * @since 0.35.0
 */
@CompileStatic
interface ProjectsSpec {
    void condition(Function<? extends Project, Boolean> condition, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Project) Closure<Void> action)

    void dir(String dir, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Project) Closure<Void> action)

    void dirs(List<String> dirs, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Project) Closure<Void> action)

    void path(String path, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Project) Closure<Void> action)

    void paths(List<String> paths, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Project) Closure<Void> action)

    void condition(Function<? extends Project, Boolean> condition, Action<? super Project> action)

    void dir(String dir, Action<? super Project> action)

    void dirs(List<String> dirs, Action<? super Project> action)

    void path(String path, Action<? super Project> action)

    void paths(List<String> paths, Action<? super Project> action)
}
