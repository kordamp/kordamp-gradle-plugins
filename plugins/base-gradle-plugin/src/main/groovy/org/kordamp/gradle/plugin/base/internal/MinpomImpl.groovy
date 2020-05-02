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
import org.kordamp.gradle.plugin.base.plugins.Minpom
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedMinpom

import static org.kordamp.gradle.PropertyUtils.booleanProvider

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@PackageScope
@CompileStatic
class MinpomImpl extends AbstractFeature implements Minpom {
    private ResolvedMinpom resolved

    MinpomImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
        super(project, ownerConfig, parentConfig)
    }

    @Override
    void normalize() {
        if (!enabled.present) {
            if (isRoot()) {
                if (project.childProjects.isEmpty()) {
                    enabled.set(project.pluginManager.hasPlugin('java') && isApplied())
                } else {
                    enabled.set(project.childProjects.values().any { p -> p.pluginManager.hasPlugin('java') && isApplied(p) })
                }
            } else {
                enabled.set(project.pluginManager.hasPlugin('java') && isApplied())
            }
        }
    }

    ResolvedMinpom asResolved() {
        if (!resolved) {
            resolved = new ResolvedMinpomImpl(project.providers,
                parentConfig?.asResolved()?.artifacts?.minpom,
                this)
        }
        resolved
    }

    @PackageScope
    @CompileStatic
    static class ResolvedMinpomImpl extends AbstractResolvedFeature implements ResolvedMinpom {
        final Provider<Boolean> enabled

        private final MinpomImpl self

        ResolvedMinpomImpl(ProviderFactory providers, ResolvedMinpom parent, MinpomImpl self) {
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

            new LinkedHashMap<>('minpom': map)
        }
    }
}
