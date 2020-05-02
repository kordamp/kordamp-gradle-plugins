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
package org.kordamp.gradle.plugin.base.internal

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.kordamp.gradle.plugin.base.plugins.Source
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedSource

import static org.kordamp.gradle.PropertyUtils.booleanProvider

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@PackageScope
@CompileStatic
class SourceImpl extends AbstractFeature implements Source {
    private ResolvedSource resolved

    SourceImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
        super(project, ownerConfig, parentConfig)
    }

    ResolvedSource asResolved() {
        if (!resolved) {
            resolved = new ResolvedSourceImpl(project.providers,
                parentConfig?.asResolved()?.artifacts?.source,
                this)
        }
        resolved
    }

    @PackageScope
    @CompileStatic
    static class ResolvedSourceImpl extends AbstractResolvedFeature implements ResolvedSource {
        final Provider<Boolean> enabled

        private final SourceImpl self

        ResolvedSourceImpl(ProviderFactory providers, ResolvedSource parent, SourceImpl self) {
            super(self.project)
            this.self = self

            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                true)
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<>([
                enabled: enabled.get(),
            ])

            new LinkedHashMap<>('source': map)
        }
    }
}
