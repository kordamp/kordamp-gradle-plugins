/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.tasks.TaskProvider
import org.kordamp.gradle.PluginUtils
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.Clirr
import org.kordamp.gradle.plugin.clirr.tasks.AggregateClirrReportTask
import org.kordamp.gradle.plugin.clirr.tasks.ClirrTask

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 *
 * @author Andres Almiray
 * @since 0.12.0
 */
@CompileStatic
class ClirrPlugin extends AbstractKordampPlugin {
    Project project

    void apply(Project project) {
        this.project = project

        if (isRootProject(project)) {
            project.childProjects.values().each {
                configureProject(it)
            }
        }
        configureProject(project)
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(ClirrPlugin)) {
            project.plugins.apply(ClirrPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        project.plugins.apply(ReportingBasePlugin)

        project.afterEvaluate {
            ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
            setEnabled(effectiveConfig.clirr.enabled)

            if (!enabled) {
                return
            }

            TaskProvider<ClirrTask> clirrTask = configureClirrTask(project)
            if (clirrTask) {
                effectiveConfig.clirr.projects() << project
                effectiveConfig.clirr.clirrTasks() << clirrTask
            }
        }

        if (isRootProject(project) && !project.childProjects.isEmpty()) {
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
    }

    private TaskProvider<ClirrTask> configureClirrTask(Project project) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)

        FileCollection newfiles = null
        if (PluginUtils.isAndroidProject(project)) {
            newfiles = project.tasks.findByName('assemble')?.outputs?.files
        } else {
            newfiles = project.tasks.findByName('jar')?.outputs?.files
        }

        if (!newfiles || !effectiveConfig.clirr.enabled) {
            return null
        }

        TaskProvider<ClirrTask> clirrTask = project.tasks.register('clirr', ClirrTask,
            new Action<ClirrTask>() {
                @Override
                @CompileDynamic
                void execute(ClirrTask t) {
                    t.group = 'Verification'
                    t.description = 'Determines the binary compatibility of the current codebase against a previous release.'
                    t.newFiles = newfiles
                    t.newClasspath = project.configurations['compile'] // TODO: implementation
                    t.xmlReport = project.file("${project.reporting.baseDir.path}/clirr/compatibility-report.xml")
                    t.htmlReport = project.file("${project.reporting.baseDir.path}/clirr/compatibility-report.html")
                    t.enabled = effectiveConfig.clirr.enabled
                }
            })

        String baseline = effectiveConfig.clirr.baseline
        if (!baseline) {
            // attempt resolving baseline using current version
            Version current = Version.of(String.valueOf(project.version))
            if (current == Version.ZERO) {
                // can't run clirr
                project.logger.info("{}: version '{}' could not be parsed as semver", project.name, project.version)
                project.logger.info('{}: please set clirr.baseline explicitly or disable clirr', project.name)
                return null
            }

            Versions versions = Versions.of(current, effectiveConfig.clirr.semver)
            if (versions.previous == Version.ZERO) {
                project.logger.info("{}: could not determine previous version for '{}' [semver compatibility={}]", project.name, current, effectiveConfig.clirr.semver)
                project.logger.info('{}: please set clirr.baseline explicitly or disable clirr', project.name)
                return null
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

        clirrTask
    }

    @CompileDynamic
    private void configureAggregateClirrTask(Project project, TaskProvider<AggregateClirrReportTask> aggregateClirrReportTask) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        if (!effectiveConfig.clirr.enabled) {
            return
        }

        Set<TaskProvider<? extends Task>> clirrTasks = new LinkedHashSet<>(effectiveConfig.clirr.clirrTasks())

        project.childProjects.values().each {
            Clirr e = resolveEffectiveConfig(it).clirr
            if (e.enabled) {
                clirrTasks.addAll(e.clirrTasks())
            }
        }

        aggregateClirrReportTask.configure(new Action<AggregateClirrReportTask>() {
            @Override
            void execute(AggregateClirrReportTask t) {
                t.dependsOn clirrTasks
                t.enabled = effectiveConfig.clirr.enabled
                t.reports = project.files(clirrTasks*.get()*.xmlReport)
            }
        })
    }
}
