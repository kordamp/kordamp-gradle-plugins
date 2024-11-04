/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2024 Andres Almiray.
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

import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.41.0
 */
@CompileStatic
enum MergeStrategy {
    APPEND,
    PREPEND,
    UNIQUE,
    OVERRIDE

    /**
     * Parses the given input into a {@code MergeStrategy}. Never returns {@code null}.
     * @param str the text to match
     * @return the matching {@code MergeStrategy} or {@code MergeStrategy.UNIQUE} if not match
     */
    static MergeStrategy of(String str) {
        isNotBlank(str) ? valueOf(str.trim().toUpperCase()) : UNIQUE
    }
}