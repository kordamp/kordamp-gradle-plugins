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
package org.kordamp.gradle.plugin.cpd

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.plugins.quality.CodeQualityExtension
import org.gradle.api.provider.Property

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
class CpdExtension extends CodeQualityExtension {
    final Property<Integer> minimumTokenCount
    final Property<String> encoding
    final Property<String> language
    final Property<Boolean> ignoreLiterals
    final Property<Boolean> ignoreIdentifiers
    final Property<Boolean> ignoreAnnotations

    CpdExtension(Project project) {
        minimumTokenCount = project.objects.property(Integer).convention(50)
        encoding = project.objects.property(String).convention('UTF-8')
        language = project.objects.property(String).convention('java')
        ignoreLiterals = project.objects.property(Boolean).convention(false)
        ignoreIdentifiers = project.objects.property(Boolean).convention(false)
        ignoreAnnotations = project.objects.property(Boolean).convention(false)
    }
}
