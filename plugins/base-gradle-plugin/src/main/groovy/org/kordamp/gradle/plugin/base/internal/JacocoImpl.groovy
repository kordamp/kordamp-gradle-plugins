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
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.SetProperty
import org.kordamp.gradle.plugin.base.ResolvedProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.Jacoco
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedJacoco

import static org.kordamp.gradle.PropertyUtils.booleanProvider
import static org.kordamp.gradle.PropertyUtils.fileProvider
import static org.kordamp.gradle.PropertyUtils.setProvider
import static org.kordamp.gradle.PropertyUtils.stringProvider
import static org.kordamp.gradle.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@PackageScope
@CompileStatic
class JacocoImpl extends AbstractFeature implements Jacoco {
    private ResolvedJacoco resolved
    final RegularFileProperty aggregateExecFile
    final RegularFileProperty aggregateReportHtmlFile
    final RegularFileProperty aggregateReportXmlFile
    final Property<String> toolVersion
    final SetProperty<String> excludes
    final ConfigurableFileCollection additionalSourceDirs
    final ConfigurableFileCollection additionalClassDirs

    JacocoImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
        super(project, ownerConfig, parentConfig)

        aggregateExecFile = project.objects.fileProperty()
        aggregateReportHtmlFile = project.objects.fileProperty()
        aggregateReportXmlFile = project.objects.fileProperty()
        toolVersion = project.objects.property(String)
        excludes = project.objects.setProperty(String).convention([])
        additionalSourceDirs = project.objects.fileCollection()
        additionalClassDirs = project.objects.fileCollection()
    }

    void exclude(String str) {
        if (isNotBlank(str)) excludes.add(str)
    }

    ResolvedJacoco asResolved() {
        if (!resolved) {
            resolved = new ResolvedJacocoImpl(project.providers,
                parentConfig?.asResolved()?.coverage?.jacoco,
                this)
        }
        resolved
    }

    @PackageScope
    @CompileStatic
    static class ResolvedJacocoImpl extends AbstractResolvedFeature implements ResolvedJacoco {
        final Provider<Boolean> enabled
        final Provider<File> aggregateExecFile
        final Provider<File> aggregateReportHtmlFile
        final Provider<File> aggregateReportXmlFile
        final Provider<String> toolVersion
        final Provider<Set<String>> excludes
        final ConfigurableFileCollection additionalSourceDirs
        final ConfigurableFileCollection additionalClassDirs

        private final JacocoImpl self

        ResolvedJacocoImpl(ProviderFactory providers, ResolvedJacoco parent, JacocoImpl self) {
            super(self.project)
            this.self = self

            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                enabledDefault())

            toolVersion = stringProvider(providers,
                parent?.toolVersion,
                self.toolVersion,
                '0.8.5')

            excludes = setProvider(providers,
                parent?.excludes,
                self.excludes,
                [] as Set)

            File destinationDir = project.layout.buildDirectory.file('reports/jacoco/aggregate').get().asFile

            aggregateExecFile = fileProvider(providers,
                parent?.aggregateExecFile,
                self.aggregateExecFile,
                project.layout.buildDirectory.file('jacoco/aggregate.exec').get().asFile)

            aggregateReportHtmlFile = fileProvider(providers,
                parent?.aggregateReportHtmlFile,
                self.aggregateReportHtmlFile,
                project.file("${destinationDir}/html"))

            aggregateReportXmlFile = fileProvider(providers,
                parent?.aggregateReportXmlFile,
                self.aggregateReportXmlFile,
                project.file("${destinationDir}/jacocoTestReport.xml"))

            additionalSourceDirs = project.objects.fileCollection()
            additionalSourceDirs.from(self.additionalSourceDirs)
            if (parent?.additionalSourceDirs) {
                additionalSourceDirs.from(parent.additionalSourceDirs)
            }

            additionalClassDirs = project.objects.fileCollection()
            additionalClassDirs.from(self.additionalClassDirs)
            if (parent?.additionalClassDirs) {
                additionalClassDirs.from(parent.additionalClassDirs)
            }
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<>([
                enabled: enabled.get(),
            ])

            if (isRoot()) {
                map.aggregateExecFile = aggregateExecFile.get()
                map.aggregateReportHtmlFile = aggregateReportHtmlFile.get()
                map.aggregateReportXmlFile = aggregateReportXmlFile.get()
            } else {
                if (!additionalSourceDirs.empty) map.additionalSourceDirs = additionalSourceDirs.files*.absolutePath
                if (!additionalClassDirs.empty) map.additionalClassDirs = additionalClassDirs.files*.absolutePath
            }
            map.toolVersion = toolVersion.get()
            map.excludes = excludes.get()

            new LinkedHashMap<>('jacoco': map)
        }

        private boolean enabledDefault() {
            if (isRoot()) {
                if (project.childProjects.isEmpty()) {
                    return hasTestSourceSets()
                }
            } else {
                return hasTestSourceSets()
            }
            false
        }

        private boolean hasTestSourceSets() {
            ResolvedProjectConfigurationExtension resolvedConfig = resolvedConfig()

            hasTestsAt(project.file('src/test')) ||
                hasTestsAt(project.file(resolvedConfig.testing.integration.baseDir)) ||
                hasTestsAt(project.file(resolvedConfig.testing.functional.baseDir))
        }

        private static boolean hasTestsAt(File testDir) {
            testDir.exists() && testDir.listFiles().length
        }
    }
}
