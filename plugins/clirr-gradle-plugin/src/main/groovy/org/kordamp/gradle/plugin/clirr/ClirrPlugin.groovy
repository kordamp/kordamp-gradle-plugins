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
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.tasks.TaskProvider
import org.kordamp.gradle.annotations.DependsOn
import org.kordamp.gradle.listener.AllProjectsEvaluatedListener
import org.kordamp.gradle.listener.ProjectEvaluatedListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.Clirr
import org.kordamp.gradle.plugin.clirr.tasks.AggregateClirrReportTask
import org.kordamp.gradle.plugin.clirr.tasks.ClirrTask
import org.kordamp.gradle.util.PluginUtils
import org.kordamp.gradle.util.Version

import javax.inject.Named

import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addAllProjectsEvaluatedListener
import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addProjectEvaluatedListener
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject
import static org.kordamp.gradle.util.PluginUtils.resolveConfig
import static org.kordamp.gradle.util.PluginUtils.supportsApiConfiguration

/**
 *
 * @author Andres Almiray
 * @since 0.12.0
 */
@CompileStatic
class ClirrPlugin extends AbstractKordampPlugin {
    private static final String AGGREGATE_CLIRR_TASK_NAME = 'aggregateClirr'

    Project project

    ClirrPlugin() {
        super(Clirr.PLUGIN_ID)
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

        project.pluginManager.withPlugin('java', new Action<AppliedPlugin>() {
            @Override
            void execute(AppliedPlugin appliedPlugin) {
                addProjectEvaluatedListener(project, new ClirrProjectEvaluatedListener())
            }
        })
    }

    private void configureRootProject(Project project) {
        addAllProjectsEvaluatedListener(project, new ClirrAllProjectsEvaluatedListener())

        project.tasks.register(AGGREGATE_CLIRR_TASK_NAME, AggregateClirrReportTask,
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
    }

    @Named('clirr')
    @DependsOn(['base'])
    private class ClirrProjectEvaluatedListener implements ProjectEvaluatedListener {
        @Override
        void projectEvaluated(Project project) {
            ProjectConfigurationExtension config = resolveConfig(project)
            setEnabled(config.clirr.enabled)
            configureClirrTask(project)
        }
    }

    @Named('clirr')
    @DependsOn(['base'])
    private class ClirrAllProjectsEvaluatedListener implements AllProjectsEvaluatedListener {
        @Override
        void allProjectsEvaluated(Project rootProject) {
            configureAggregateClirrTask(rootProject)
        }
    }

    private void configureClirrTask(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)

        if (!config.clirr.enabled) {
            return
        }

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
                    t.enabled = !newfiles.isEmpty()
                }
            })

        String baseline = config.clirr.baseline
        if (!baseline) {
            // attempt resolving baseline using current version
            Version current = Version.of(String.valueOf(project.version))
            if (current == Version.ZERO) {
                // can't run clirr
                project.logger.info("{}: version '{}' could not be parsed as semver", project.name, project.version)
                project.logger.info('{}: please set clirr.baseline explicitly or disable clirr', project.name)
                return
            }

            Versions versions = Versions.of(current, config.clirr.semver)
            if (versions.previous == Version.ZERO) {
                project.logger.info("{}: could not determine previous version for '{}' [semver compatibility={}]", project.name, current, config.clirr.semver)
                project.logger.info('{}: please set clirr.baseline explicitly or disable clirr', project.name)
                return
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

    @CompileDynamic
    private void configureAggregateClirrTask(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)

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

        if (clirrTasks) {
            project.tasks.named(AGGREGATE_CLIRR_TASK_NAME, AggregateClirrReportTask, new Action<AggregateClirrReportTask>() {
                @Override
                void execute(AggregateClirrReportTask t) {
                    t.dependsOn clirrTasks
                    t.enabled = config.clirr.aggregate.enabled
                    t.reports = project.files(clirrTasks*.xmlReport)
                }
            })
        }
    }
}
