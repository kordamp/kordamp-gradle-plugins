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
package org.kordamp.gradle.plugin.clirr

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.tasks.TaskProvider
import org.kordamp.gradle.PluginUtils
import org.kordamp.gradle.Version
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.clirr.tasks.AggregateClirrReportTask
import org.kordamp.gradle.plugin.clirr.tasks.ClirrTask

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.PluginUtils.supportsApiConfiguration
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 *
 * @author Andres Almiray
 * @since 0.12.0
 */
@CompileStatic
class ClirrPlugin extends AbstractKordampPlugin {
    Project project

    ClirrPlugin() {
        super(org.kordamp.gradle.plugin.base.plugins.Clirr.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        configureProject(project)
        if (isRootProject(project)) {
            configureRootProject(project)
            project.childProjects.values().each {
                it.pluginManager.apply(ClirrPlugin)
            }
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(ClirrPlugin)) {
            project.pluginManager.apply(ClirrPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        project.pluginManager.apply(ReportingBasePlugin)

        project.afterEvaluate {
            ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
            setEnabled(effectiveConfig.clirr.enabled)

            project.pluginManager.withPlugin('java', new Action<AppliedPlugin>() {
                @Override
                void execute(AppliedPlugin appliedPlugin) {
                    configureClirrTask(project)
                }
            })
        }
    }

    private void configureRootProject(Project project) {
        TaskProvider<AggregateClirrReportTask> aggregateClirrReportTask = project.tasks.register('aggregateClirr', AggregateClirrReportTask,
            new Action<AggregateClirrReportTask>() {
                @Override
                @CompileDynamic
                void execute(AggregateClirrReportTask t) {
                    t.enabled = false
                    t.group = 'Verification'
                    t.description = 'Aggregates binary compatibility reports.'
                    t.reportFile = project.file("${project.reporting.baseDir.path}/clirr/aggregate-compatibility-report.html")
                }
            })

        project.gradle.addBuildListener(new BuildAdapter() {
            @Override
            void projectsEvaluated(Gradle gradle) {
                configureAggregateClirrTask(project, aggregateClirrReportTask)
            }
        })
    }

    private TaskProvider<ClirrTask> configureClirrTask(Project project) {
        ProjectConfigurationExtension config = resolveEffectiveConfig(project)

        FileCollection newfiles = null
        if (PluginUtils.isAndroidProject(project)) {
            newfiles = project.tasks.findByName('assemble')?.outputs?.files
        } else {
            newfiles = project.tasks.findByName('jar')?.outputs?.files
        }

        TaskProvider<ClirrTask> clirrTask = project.tasks.register('clirr', ClirrTask,
            new Action<ClirrTask>() {
                @Override
                @CompileDynamic
                void execute(ClirrTask t) {
                    t.group = 'Verification'
                    t.description = 'Determines the binary compatibility of the current codebase against a previous release.'
                    t.newFiles = newfiles ?: t.project.objects.fileCollection()
                    t.newClasspath = t.project.configurations[supportsApiConfiguration(t.project) ? 'api' : 'compile']
                    t.xmlReport = t.project.file("${t.project.reporting.baseDir.path}/clirr/compatibility-report.xml")
                    t.htmlReport = t.project.file("${t.project.reporting.baseDir.path}/clirr/compatibility-report.html")
                    t.enabled = newfiles && config.clirr.enabled
                }
            })

        // calculate baseline only if enabled
        if (config.clirr.enabled) {
            String baseline = config.clirr.baseline
            if (!baseline) {
                // attempt resolving baseline using current version
                Version current = Version.of(String.valueOf(project.version))
                if (current == Version.ZERO) {
                    // can't run clirr
                    project.logger.info("{}: version '{}' could not be parsed as semver", project.name, project.version)
                    project.logger.info('{}: please set clirr.baseline explicitly or disable clirr', project.name)
                    return clirrTask
                }

                Versions versions = Versions.of(current, config.clirr.semver)
                if (versions.previous == Version.ZERO) {
                    project.logger.info("{}: could not determine previous version for '{}' [semver compatibility={}]", project.name, current, config.clirr.semver)
                    project.logger.info('{}: please set clirr.baseline explicitly or disable clirr', project.name)
                    return clirrTask
                }

                baseline = [project.group ?: '', project.name, versions.previous].join(':')
            }

            project.logger.info('{}: baseline has been set to {}', project.name, baseline)

            // temporary change the group of the current project  otherwise
            // the latest version will always override the baseline
            String projectGroup = project.group
            try {
                project.group = projectGroup + '.clirr'
                Configuration detached = project.configurations.detachedConfiguration(
                    project.dependencies.create(baseline)
                )
                detached.transitive = true
                detached.resolve()
                clirrTask.configure(new Action<ClirrTask>() {
                    @Override
                    void execute(ClirrTask t) {
                        t.baseFiles = detached
                    }
                })
            } finally {
                project.group = projectGroup
            }
        }

        clirrTask
    }

    @CompileDynamic
    private void configureAggregateClirrTask(Project project, TaskProvider<AggregateClirrReportTask> aggregateClirrReportTask) {
        ProjectConfigurationExtension config = resolveEffectiveConfig(project)

        List<ClirrTask> clirrTasks = []
        project.tasks.withType(ClirrTask) { ClirrTask t ->
            if (project in config.clirr.aggregate.excludedProjects) return
            if (t.enabled) clirrTasks << t
        }
        project.childProjects.values().each { Project p ->
            if (p in config.clirr.aggregate.excludedProjects) return
            p.tasks.withType(ClirrTask) { ClirrTask t -> if (t.enabled) clirrTasks << t }
        }
        clirrTasks = clirrTasks.unique()

        aggregateClirrReportTask.configure(new Action<AggregateClirrReportTask>() {
            @Override
            void execute(AggregateClirrReportTask t) {
                t.dependsOn clirrTasks
                t.enabled = config.clirr.aggregate.enabled
                t.reports = project.files(clirrTasks*.xmlReport)
            }
        })
    }
}
