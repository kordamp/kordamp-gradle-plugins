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
package org.kordamp.gradle.plugin.project

import com.github.benmanes.gradle.versions.VersionsPlugin
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.tasks.reports.ReportGeneratingTask
import org.kordamp.gradle.plugin.bintray.BintrayPlugin
import org.kordamp.gradle.plugin.buildinfo.BuildInfoPlugin
import org.kordamp.gradle.plugin.coveralls.CoverallsPlugin
import org.kordamp.gradle.plugin.jacoco.JacocoPlugin
import org.kordamp.gradle.plugin.jar.JarPlugin
import org.kordamp.gradle.plugin.licensing.LicensingPlugin
import org.kordamp.gradle.plugin.minpom.MinPomPlugin
import org.kordamp.gradle.plugin.profiles.ProfilesPlugin
import org.kordamp.gradle.plugin.project.tasks.reports.GenerateDependenciesReportTask
import org.kordamp.gradle.plugin.project.tasks.reports.GenerateDependencyUpdatesReportTask
import org.kordamp.gradle.plugin.project.tasks.reports.GeneratePluginReportTask
import org.kordamp.gradle.plugin.project.tasks.reports.GenerateSummaryReportTask
import org.kordamp.gradle.plugin.project.tasks.reports.GenerateTeamReportTask
import org.kordamp.gradle.plugin.publishing.PublishingPlugin
import org.kordamp.gradle.plugin.source.SourceJarPlugin
import org.kordamp.gradle.plugin.sourcehtml.SourceHtmlPlugin
import org.kordamp.gradle.plugin.sourcexref.SourceXrefPlugin
import org.kordamp.gradle.plugin.stats.SourceStatsPlugin
import org.kordamp.gradle.plugin.testing.TestingPlugin

/**
 * Aggregator for all Kordamp plugins.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class ProjectPlugin extends AbstractKordampPlugin {
    Project project

    void apply(Project project) {
        this.project = project

        applyPlugins(project)
        project.childProjects.values().each {
            applyPlugins(it)
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(ProjectPlugin)) {
            project.pluginManager.apply(ProjectPlugin)
        }
    }

    private void applyPlugins(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        ProfilesPlugin.applyIfMissing(project)
        BuildInfoPlugin.applyIfMissing(project)
        LicensingPlugin.applyIfMissing(project)
        JacocoPlugin.applyIfMissing(project)
        CoverallsPlugin.applyIfMissing(project)
        PublishingPlugin.applyIfMissing(project)
        MinPomPlugin.applyIfMissing(project)
        JarPlugin.applyIfMissing(project)
        SourceJarPlugin.applyIfMissing(project)
        SourceStatsPlugin.applyIfMissing(project)
        SourceHtmlPlugin.applyIfMissing(project)
        SourceXrefPlugin.applyIfMissing(project)
        BintrayPlugin.applyIfMissing(project)
        TestingPlugin.applyIfMissing(project)

        project.pluginManager.apply(VersionsPlugin)

        registerTasks(project)
    }

    private void registerTasks(Project project) {
        TaskProvider<DependencyUpdatesTask> dependencyUpdates = project.tasks.named('dependencyUpdates', DependencyUpdatesTask,
            new Action<DependencyUpdatesTask>() {
                @Override
                void execute(DependencyUpdatesTask t) {
                    t.outputFormatter = 'plain,xml'
                }
            })

        project.tasks.register('generateDependencyUpdatesReport', GenerateDependencyUpdatesReportTask,
            new Action<GenerateDependencyUpdatesReportTask>() {
                @Override
                void execute(GenerateDependencyUpdatesReportTask t) {
                    t.dependsOn(dependencyUpdates)
                    t.group = 'Reports'
                    t.description = "Generates a dependency updates report for '$project.name'."
                    t.dependencyUpdatesXmlReport.set(new File(
                        dependencyUpdates.get().outputDir +
                            File.separator +
                            dependencyUpdates.get().reportfileName +
                            '.xml'))
                }
            })

        project.tasks.register('generateDependenciesReport', GenerateDependenciesReportTask,
            new Action<GenerateDependenciesReportTask>() {
                @Override
                void execute(GenerateDependenciesReportTask t) {
                    t.group = 'Reports'
                    t.description = "Generates a dependencies report for '$project.name'."
                }
            })

        project.tasks.register('generateTeamReport', GenerateTeamReportTask,
            new Action<GenerateTeamReportTask>() {
                @Override
                void execute(GenerateTeamReportTask t) {
                    t.group = 'Reports'
                    t.description = "Generates a team report for '$project.name'."
                }
            })

        project.tasks.register('generatePluginReport', GeneratePluginReportTask,
            new Action<GeneratePluginReportTask>() {
                @Override
                void execute(GeneratePluginReportTask t) {
                    t.group = 'Reports'
                    t.description = "Generates a plugin report for '$project.name'."
                }
            })

        project.tasks.register('generateSummaryReport', GenerateSummaryReportTask,
            new Action<GenerateSummaryReportTask>() {
                @Override
                void execute(GenerateSummaryReportTask t) {
                    t.group = 'Reports'
                    t.description = "Generates a summary report for '$project.name'."
                }
            })

        project.tasks.register('generateAllReports', DefaultTask,
            new Action<DefaultTask>() {
                @Override
                void execute(DefaultTask t) {
                    t.group = 'Reports'
                    t.description = "Generates all reports for '$project.name'."
                }
            })

        project.afterEvaluate(new Action<Project>() {
            @Override
            void execute(Project p) {
                configureAggregatingReportTasks(p)
            }
        })
    }


    private void configureAggregatingReportTasks(Project project) {
        Set<ReportGeneratingTask> tasks = []
        project.tasks.withType(ReportGeneratingTask, new Action<ReportGeneratingTask>() {
            @Override
            void execute(ReportGeneratingTask t) {
                tasks << t
            }
        })

        project.tasks.named('generateAllReports', DefaultTask,
            new Action<DefaultTask>() {
                @Override
                void execute(DefaultTask t) {
                    t.dependsOn(tasks)
                }
            })
    }
}
