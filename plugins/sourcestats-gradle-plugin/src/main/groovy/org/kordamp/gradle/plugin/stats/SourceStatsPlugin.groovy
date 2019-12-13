/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
import org.kordamp.gradle.plugin.base.plugins.Stats

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

            if (enabled) {
                if (isRootProject(project) && project.childProjects.isEmpty()) {
                    maybeCreateStatsTask(project)
                } else if (!isRootProject(project)) {
                    maybeCreateStatsTask(project)
                }
            }
        }

        if (isRootProject(project)) {
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
    }

    private void maybeCreateStatsTask(Project project) {
        try {
            // see if the project supports sourceSets
            PluginUtils.resolveSourceSets(project)
            createStatsTask(project)
        } catch (Exception ignored) {
            // incompatible project, skip it
        }
    }

    private void createStatsTask(Project project) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)

        TaskProvider<SourceStatsTask> statsTask = project.tasks.register('sourceStats', SourceStatsTask,
                new Action<SourceStatsTask>() {
                    @Override
                    void execute(SourceStatsTask t) {
                        t.enabled = effectiveConfig.stats.enabled
                        t.group = 'Reporting'
                        t.description = 'Generates a report on lines of code.'
                        t.paths = effectiveConfig.stats.paths
                        t.formats = effectiveConfig.stats.formats
                        t.counters = effectiveConfig.stats.counters
                    }
                })

        if (effectiveConfig.stats.enabled) {
            effectiveConfig.stats.projects() << project
            effectiveConfig.stats.statsTasks() << statsTask
        }
    }

    private void applyAggregateStats(Project project, TaskProvider<AggregateSourceStatsReportTask> task) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)

        Set<TaskProvider<?>> allStatsTasks = new LinkedHashSet<>(effectiveConfig.stats.statsTasks())

        project.childProjects.values().each {
            Stats stats = resolveEffectiveConfig(it).stats
            if (stats.enabled) {
                allStatsTasks.addAll(stats.statsTasks())
            }
        }

        task.configure(new Action<AggregateSourceStatsReportTask>() {
            @Override
            void execute(AggregateSourceStatsReportTask t) {
                t.enabled = effectiveConfig.stats.enabled
                t.dependsOn allStatsTasks
                t.formats = effectiveConfig.stats.formats
            }
        })
    }
}
