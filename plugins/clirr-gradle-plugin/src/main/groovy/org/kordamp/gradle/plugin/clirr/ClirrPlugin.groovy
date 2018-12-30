/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 Andres Almiray.
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

import org.gradle.BuildAdapter
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.ReportingBasePlugin
import org.kordamp.gradle.PluginUtils
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.Clirr
import org.kordamp.gradle.plugin.clirr.tasks.AggregateClirrReportTask
import org.kordamp.gradle.plugin.clirr.tasks.ClirrTask

import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 *
 * @author Andres Almiray
 * @since 0.12.0
 */
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

            ClirrTask clirrTask = configureClirrTask(project)
            if (clirrTask.enabled) {
                effectiveConfig.clirr.projects() << project
                effectiveConfig.clirr.clirrTasks() << clirrTask
            }
        }

        if (isRootProject(project) && !project.childProjects.isEmpty()) {
            AggregateClirrReportTask aggregateClirrReportTask = project.tasks.create('aggregateClirr', AggregateClirrReportTask) {
                enabled = false
                group = 'Verification'
                description = 'Aggregates binary compatibility reports'
                reportFile = project.file("${project.reporting.baseDir.path}/clirr/aggregate-compatibility-report.html")
            }

            project.gradle.addBuildListener(new BuildAdapter() {
                @Override
                void projectsEvaluated(Gradle gradle) {
                    configureAggregateClirrTask(project, aggregateClirrReportTask)
                }
            })
        }
    }

    private ClirrTask configureClirrTask(Project project) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)

        ClirrTask clirrTask = project.tasks.findByName('clirr')
        if (!clirrTask) {
            FileCollection newfiles = null

            if (PluginUtils.isAndroidProject(project)) {
                newfiles = project.tasks.findByName('assemble').outputs.files
            } else {
                newfiles = project.tasks.findByName('jar').outputs.files
            }

            clirrTask = project.tasks.create('clirr', ClirrTask) {
                group 'Verification'
                description 'Determines the binary compatibility of the current codebase against a previous release'
                newFiles = newfiles
                newClasspath = project.configurations['compile'] // TODO: implementation
                xmlReport = project.file("${project.reporting.baseDir.path}/clirr/compatibility-report.xml")
                htmlReport = project.file("${project.reporting.baseDir.path}/clirr/compatibility-report.html")
            }
        }

        clirrTask.configure {
            enabled = effectiveConfig.clirr.enabled
        }

        if (!clirrTask.enabled) {
            return clirrTask
        }

        String baseline = effectiveConfig.clirr.baseline
        if (!baseline) {
            // attempt resolving baseline using current version
            Version current = Version.of(String.valueOf(project.version))
            if (current == Version.ZERO) {
                // can't run clirr
                project.logger.info("{}: version '{}' could not be parsed as semver", project.name, project.version)
                project.logger.info('{}: please set clirr.baseline explicitly or disable clirr', project.name)
                clirrTask.enabled = false
                return clirrTask
            }

            Versions versions = Versions.of(current, effectiveConfig.clirr.semver)
            if (versions.previous == Version.ZERO) {
                project.logger.info("{}: could not determine previous version for '{}' [semver compatibility={}]", project.name, current, effectiveConfig.clirr.semver)
                project.logger.info('{}: please set clirr.baseline explicitly or disable clirr', project.name)
                clirrTask.enabled = false
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
            clirrTask.baseFiles = detached
        } finally {
            project.group = projectGroup
        }

        clirrTask
    }

    private void configureAggregateClirrTask(Project project, AggregateClirrReportTask aggregateClirrReportTask) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        if (!effectiveConfig.clirr.enabled) {
            return
        }

        Set<Task> clirrTasks = new LinkedHashSet<>(effectiveConfig.clirr.clirrTasks())

        project.childProjects.values()*.effectiveConfig.clirr.each { Clirr e ->
            if (e.enabled) {
                clirrTasks.addAll(e.clirrTasks())
            }
        }

        aggregateClirrReportTask.configure {
            dependsOn clirrTasks
            enabled = effectiveConfig.clirr.enabled
            reports = project.files(clirrTasks.xmlReport)
        }
    }
}
