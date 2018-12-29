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
package org.kordamp.gradle.plugin.stats

import org.gradle.BuildAdapter
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.Stats

import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
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
            project.plugins.apply(SourceStatsPlugin)
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
                if(isRootProject(project) && project.childProjects.isEmpty()) {
                    maybeCreateStatsTask(project)
                } else if (!isRootProject(project)) {
                    maybeCreateStatsTask(project)
                }
            }
        }

        if (isRootProject(project) && !project.childProjects.isEmpty()) {
            AggregateSourceStatsReportTask task = project.tasks.create(AGGREGATE_STATS_TASK_NAME, AggregateSourceStatsReportTask) {
                enabled = false
                group = 'Reporting'
                description = 'Aggregate source stats reports.'
            }

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
            project.sourceSets
            createStatsTask(project)
        } catch (MissingPropertyException ignored) {
            // incompatible project, skip it
        }
    }

    private void createStatsTask(Project project) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)

        SourceStatsTask statsTask = project.tasks.create('sourceStats', SourceStatsTask) {
            enabled = effectiveConfig.stats.enabled
            group = 'Reporting'
            description = 'Generates a report on lines of code'
            paths = effectiveConfig.stats.paths
            formats = effectiveConfig.stats.formats
            counters = effectiveConfig.stats.counters
        }

        if (effectiveConfig.stats.enabled) {
            effectiveConfig.stats.projects() << project
            effectiveConfig.stats.statsTasks() << statsTask
        }
    }

    private void applyAggregateStats(Project project, AggregateSourceStatsReportTask task) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)

        Set<Project> allProjects = new LinkedHashSet<>(effectiveConfig.stats.projects())
        Set<Task> allStatsTasks = new LinkedHashSet<>(effectiveConfig.stats.statsTasks())

        project.childProjects.values()*.effectiveConfig.stats.each { Stats e ->
            if (e.enabled) {
                allStatsTasks.addAll(e.statsTasks())
            }
        }

        task.configure {
            enabled = effectiveConfig.stats.enabled
            dependsOn allStatsTasks
            formats = effectiveConfig.stats.formats
        }
    }
}
