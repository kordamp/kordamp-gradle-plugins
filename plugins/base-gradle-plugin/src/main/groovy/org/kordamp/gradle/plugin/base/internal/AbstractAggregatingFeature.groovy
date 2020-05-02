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
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.SetProperty
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.plugin.base.plugins.AggregatingFeature
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedAggregatingFeature

import static org.kordamp.gradle.PropertyUtils.booleanProvider
import static org.kordamp.gradle.PropertyUtils.setProvider

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@PackageScope
@CompileStatic
abstract class AbstractAggregatingFeature extends AbstractFeature implements AggregatingFeature {
    protected final AggregateImpl aggregate

    AbstractAggregatingFeature(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
        super(project, ownerConfig, parentConfig)
        this.aggregate = new AggregateImpl(project, ownerConfig, parentConfig)
    }

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

    void aggregate(Action<? super Aggregate> action) {
        action.execute(aggregate)
    }

    void aggregate(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Aggregate) Closure<Void> action) {
        ConfigureUtil.configure(action, aggregate)
    }

    @PackageScope
    @CompileStatic
    static class AggregateImpl extends AbstractFeature implements Aggregate {
        final Property<Boolean> enabled
        final SetProperty<Project> excludedProjects

        AggregateImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
            super(project, ownerConfig, parentConfig)
            enabled = project.objects.property(Boolean)
            excludedProjects = project.objects.setProperty(Project).convention([])
        }

        @Override
        void excludeProject(Project project) {
            if (project) excludedProjects.add(project)
        }
    }

    @PackageScope
    @CompileStatic
    static class ResolvedAggregateImpl extends AbstractResolvedFeature implements ResolvedAggregatingFeature.ResolvedAggregate {
        final Provider<Boolean> enabled
        final Provider<Set<Project>> excludedProjects

        private final AggregateImpl self

        ResolvedAggregateImpl(ProviderFactory providers, ResolvedAggregatingFeature.ResolvedAggregate parent, AggregateImpl self) {
            super(self.project)
            this.self = self

            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                true)

            excludedProjects = setProvider(providers,
                parent?.excludedProjects,
                self.excludedProjects,
                [] as Set)
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<>([
                enabled         : enabled.get(),
                excludedProjects: excludedProjects.get()
            ])

            new LinkedHashMap<>('aggregate': map)
        }
    }
}
