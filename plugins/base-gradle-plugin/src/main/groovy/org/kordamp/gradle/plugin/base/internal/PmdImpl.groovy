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
package org.kordamp.gradle.plugin.base.internal

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.kordamp.gradle.plugin.base.plugins.Pmd
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedPmd

import static org.kordamp.gradle.PropertyUtils.booleanProvider
import static org.kordamp.gradle.PropertyUtils.intProvider
import static org.kordamp.gradle.PropertyUtils.setProvider
import static org.kordamp.gradle.PropertyUtils.stringProvider

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@PackageScope
@CompileStatic
class PmdImpl extends AbstractQualityFeature implements Pmd {
    final ConfigurableFileCollection ruleSetFiles
    final Property<Boolean> incrementalAnalysis
    final Property<Integer> rulePriority

    private ResolvedPmd resolved

    PmdImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
        super(project, ownerConfig, parentConfig)

        ruleSetFiles = project.objects.fileCollection()
        incrementalAnalysis = project.objects.property(Boolean)
        rulePriority = project.objects.property(Integer)
    }

    ResolvedPmd asResolved() {
        if (!resolved) {
            resolved = new ResolvedPmdImpl(project.providers,
                parentConfig?.asResolved()?.quality?.pmd,
                this)
        }
        resolved
    }

    @PackageScope
    @CompileStatic
    static class ResolvedPmdImpl extends AbstractResolvedFeature implements ResolvedPmd {
        final Provider<Boolean> enabled
        final Provider<Boolean> ignoreFailures
        final Provider<String> toolVersion
        final Provider<Set<String>> excludedSourceSets
        final ConfigurableFileCollection ruleSetFiles
        final Provider<Boolean> incrementalAnalysis
        final Provider<Integer> rulePriority

        private final ResolvedPmd parent
        private final PmdImpl self
        private ResolvedAggregate aggregate

        ResolvedPmdImpl(ProviderFactory providers, ResolvedPmd parent, PmdImpl self) {
            super(self.project)
            this.parent = parent
            this.self = self

            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                true)

            ignoreFailures = booleanProvider(providers,
                parent?.ignoreFailures,
                self.ignoreFailures,
                true)

            toolVersion = stringProvider(providers,
                parent?.toolVersion,
                self.toolVersion,
                '6.23.0')

            excludedSourceSets = setProvider(providers,
                parent?.excludedSourceSets,
                self.excludedSourceSets,
                [] as Set)

            ruleSetFiles = project.objects.fileCollection()
            ruleSetFiles.from(self.ruleSetFiles)
            if (parent?.ruleSetFiles) {
                ruleSetFiles.from(parent.ruleSetFiles)
            }
            File file = resolveFile()
            if (!ruleSetFiles.contains(file)) {
                ruleSetFiles.from([file] as Set)
            }

            incrementalAnalysis = booleanProvider(providers,
                parent?.incrementalAnalysis,
                self.incrementalAnalysis,
                false)

            rulePriority = intProvider(providers,
                parent?.rulePriority,
                self.rulePriority,
                5)
        }

        @Override
        ResolvedAggregate getAggregate() {
            if (!aggregate) {
                aggregate = new AbstractAggregatingFeature.ResolvedAggregateImpl(project.providers,
                    parent?.aggregate,
                    self.aggregate)
            }
            aggregate
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<>([
                enabled            : enabled.get(),
                excludedSourceSets : excludedSourceSets.get(),
                ruleSetFiles       : ruleSetFiles.files*.absolutePath,
                incrementalAnalysis: incrementalAnalysis.get(),
                rulePriority       : rulePriority.get()
            ])

            if (isRoot()) {
                map.aggregate = getAggregate().toMap()
            }

            new LinkedHashMap<>('pmd': map)
        }

        private File resolveFile() {
            File file = project.rootProject.file("config/pmd/${project.name}.xml")
            if (!file.exists()) {
                file = project.rootProject.file("config/pmd/pmd.xml")
            }
            file
        }
    }
}
