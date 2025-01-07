/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2025 Andres Almiray.
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
package org.kordamp.gradle.plugin.base.model

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

/**
 * @author Andres Almiray
 * @since 0.41.0
 */
@CompileStatic
interface LicenseSet extends DomainSet<License> {

    List<License> getLicenses()

    void license(Action<? super License> action)

    void license(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = License) Closure<Void> action)

    List<String> validate(ProjectConfigurationExtension extension)
}