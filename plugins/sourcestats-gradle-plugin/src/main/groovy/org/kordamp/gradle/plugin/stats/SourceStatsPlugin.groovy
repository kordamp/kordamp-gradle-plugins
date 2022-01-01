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
package org.kordamp.gradle.plugin.stats

import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskProvider
import org.kordamp.gradle.PluginUtils
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
@CompileStatic
class SourceStatsPlugin extends AbstractKordampPlugin {
    static final String AGGREGATE_STATS_TASK_NAME = 'aggregateSourceStats'

    Project project

    SourceStatsPlugin() {
        super(org.kordamp.gradle.plugin.base.plugins.Stats.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        configureProject(project)
        if (isRootProject(project)) {
            configureRootProject(project)
            project.childProjects.values().each {
                it.pluginManager.apply(SourceStatsPlugin)
            }
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(SourceStatsPlugin)) {
            project.pluginManager.apply(SourceStatsPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)

        project.afterEvaluate {
            ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
            setEnabled(effectiveConfig.stats.enabled)

            createSourceStatsTask(project)
        }
    }

    private void configureRootProject(Project project) {
        TaskProvider<AggregateSourceStatsReportTask> task = project.tasks.register(
            AGGREGATE_STATS_TASK_NAME,
            AggregateSourceStatsReportTask,
            new Action<AggregateSourceStatsReportTask>() {
                @Override
                void execute(AggregateSourceStatsReportTask t) {
                    t.enabled = false
                    t.group = 'Reporting'
                    t.description = 'Aggregate source stats reports.'
                }
            })

        project.gradle.addBuildListener(new BuildAdapter() {
            @Override
            void projectsEvaluated(Gradle gradle) {
                applyAggregateStats(project, task)
            }
        })
    }

    private void createSourceStatsTask(Project project) {
        ProjectConfigurationExtension config = resolveEffectiveConfig(project)

        project.tasks.register('sourceStats', SourceStatsTask,
            new Action<SourceStatsTask>() {
                @Override
                void execute(SourceStatsTask t) {
                    t.enabled = PluginUtils.resolveSourceSets(project) && config.stats.enabled
                    t.group = 'Reporting'
                    t.description = 'Generates a report on lines of code.'
                    t.paths = config.stats.paths
                    t.formats = config.stats.formats
                    t.counters = config.stats.counters
                }
            })
    }

    private void applyAggregateStats(Project project, TaskProvider<AggregateSourceStatsReportTask> aggregateSourceStatsTask) {
        ProjectConfigurationExtension config = resolveEffectiveConfig(project)

        Set<SourceStatsTask> tt = new LinkedHashSet<>()
        project.tasks.withType(SourceStatsTask) { SourceStatsTask task ->
            if (project in config.stats.aggregate.excludedProjects) return
            if (task.name != AGGREGATE_STATS_TASK_NAME &&
                task.enabled)
                tt << task
        }

        project.childProjects.values().each { p ->
            if (p in config.stats.aggregate.excludedProjects) return
            p.tasks.withType(SourceStatsTask) { SourceStatsTask task ->
                if (task.enabled) tt << task
            }
        }

        aggregateSourceStatsTask.configure(new Action<AggregateSourceStatsReportTask>() {
            @Override
            void execute(AggregateSourceStatsReportTask t) {
                t.dependsOn tt
                t.enabled = config.stats.aggregate.enabled
                t.formats = config.stats.formats
            }
        })
    }
}
