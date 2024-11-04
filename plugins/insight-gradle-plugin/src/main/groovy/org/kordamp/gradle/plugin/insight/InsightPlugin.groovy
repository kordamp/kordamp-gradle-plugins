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
package org.kordamp.gradle.plugin.insight

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory
import org.kordamp.gradle.plugin.insight.internal.BuildHelper
import org.kordamp.gradle.plugin.insight.internal.InsightExtensionImpl

import javax.inject.Inject

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@CompileStatic
class InsightPlugin implements Plugin<Settings> {
    private final ObjectFactory objects
    private final ProviderFactory providers

    @Inject
    InsightPlugin(ObjectFactory objects, ProviderFactory providers) {
        this.objects = objects
        this.providers = providers
    }

    @Override
    void apply(Settings settings) {
        settings.extensions.create(InsightExtension, 'insight', InsightExtensionImpl, settings, objects, providers)

        settings.gradle.addListener(new BuildHelper(settings))
    }
}
