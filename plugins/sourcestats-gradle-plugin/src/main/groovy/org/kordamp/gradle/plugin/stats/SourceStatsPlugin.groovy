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
import org.gradle.api.Action
import org.gradle.api.Project
import org.kordamp.gradle.annotations.DependsOn
import org.kordamp.gradle.listener.AllProjectsEvaluatedListener
import org.kordamp.gradle.listener.ProjectEvaluatedListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.Stats
import org.kordamp.gradle.util.PluginUtils

import javax.inject.Named

import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addAllProjectsEvaluatedListener
import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addProjectEvaluatedListener
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject
import static org.kordamp.gradle.util.PluginUtils.resolveConfig

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
@CompileStatic
class SourceStatsPlugin extends AbstractKordampPlugin {
    static final String AGGREGATE_STATS_TASK_NAME = 'aggregateSourceStats'

    Project project

    SourceStatsPlugin() {
        super(Stats.PLUGIN_ID)
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

        addProjectEvaluatedListener(project, new SourceStatsProjectEvaluatedListener())
    }

    private void configureRootProject(Project project) {
        project.tasks.register(
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

        addAllProjectsEvaluatedListener(project, new SourceStatsAllProjectsEvaluatedListener())
    }

    @Named('stats')
    @DependsOn(['base'])
    private class SourceStatsProjectEvaluatedListener implements ProjectEvaluatedListener {
        @Override
        void projectEvaluated(Project project) {
            ProjectConfigurationExtension config = resolveConfig(project)
            setEnabled(config.stats.enabled)

            if (config.stats.enabled) {
                createSourceStatsTask(project)
            }
        }
    }

    @Named('stats')
    @DependsOn(['base'])
    private class SourceStatsAllProjectsEvaluatedListener implements AllProjectsEvaluatedListener {
        @Override
        void allProjectsEvaluated(Project rootProject) {
            applyAggregateStats(rootProject)
        }
    }

    private void createSourceStatsTask(Project project) {
        project.tasks.register('sourceStats', SourceStatsTask,
            new Action<SourceStatsTask>() {
                @Override
                void execute(SourceStatsTask t) {
                    ProjectConfigurationExtension config = resolveConfig(project)
                    t.enabled = PluginUtils.resolveSourceSets(project) && config.stats.enabled
                    t.group = 'Reporting'
                    t.description = 'Generates a report on lines of code.'
                    t.paths = config.stats.paths
                    t.formats = config.stats.formats
                    t.counters = config.stats.counters
                }
            })
    }

    private void applyAggregateStats(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)

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

        project.tasks.named(AGGREGATE_STATS_TASK_NAME, AggregateSourceStatsReportTask,
            new Action<AggregateSourceStatsReportTask>() {
                @Override
                void execute(AggregateSourceStatsReportTask t) {
                    t.dependsOn tt
                    t.enabled = config.stats.aggregate.enabled
                    t.formats = config.stats.formats
                }
            })
    }
}
