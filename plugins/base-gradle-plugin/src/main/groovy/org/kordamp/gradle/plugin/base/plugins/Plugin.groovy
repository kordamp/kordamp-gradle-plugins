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
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.CompileStatic
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

/**
 * @author Andres Almiray
 * @since 0.16.0
 */
@CompileStatic
interface Plugin extends Feature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.plugin'

    Property<String> getId()

    Property<String> getImplementationClass()

    SetProperty<String> getTags()
}
