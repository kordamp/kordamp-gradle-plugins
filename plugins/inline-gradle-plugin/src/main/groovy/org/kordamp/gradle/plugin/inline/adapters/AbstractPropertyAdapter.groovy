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
package org.kordamp.gradle.plugin.inline.adapters

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.kordamp.gradle.plugin.inline.PropertyAdapter

import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.39.0
 */
@CompileStatic
abstract class AbstractPropertyAdapter implements PropertyAdapter {
    protected String resolveProperty(Project project, String key) {
        String value = System.getProperty(key)
        isNotBlank(value) ? value : String.valueOf(project.findProperty(key) ?: '')
    }
}
