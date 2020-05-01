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
package org.kordamp.gradle.plugin.settings.internal

import groovy.transform.CompileDynamic
import org.kordamp.gradle.plugin.settings.ProjectsExtension

import java.util.function.Supplier

/**
 * @author Andres Almiray
 * @since 0.35.0
 */
@CompileDynamic
class DirectorySpecImpl implements ProjectsExtension.DirectorySpec {
    final String dir
    Supplier<Boolean> condition
    final List<String> excludes = []

    DirectorySpecImpl(String dir) {
        this.dir = dir
        condition = { -> true }
    }

    @Override
    ProjectsExtension.DirectorySpec exclude(String projectName) {
        if (!isBlank(projectName)) {
            excludes << projectName
        }
        this
    }

    @Override
    void when(boolean value) {
        condition = { -> value }
    }

    @Override
    void when(Supplier<Boolean> supplier) {
        if (supplier) condition = supplier
    }

    boolean applies() {
        return condition.get()
    }

    private boolean isBlank(String str) {
        if (str == null || str.length() == 0) {
            return true
        }
        for (char c : str.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                return false
            }
        }

        return true
    }
}