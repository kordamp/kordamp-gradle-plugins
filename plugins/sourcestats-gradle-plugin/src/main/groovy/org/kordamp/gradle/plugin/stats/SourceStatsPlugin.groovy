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
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.mutable.MutableStats

import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
class SourceStatsPlugin implements Plugin<Project> {
    private static final String VISITED = SourceStatsPlugin.class.name.replace('.', '_') + '_VISITED'

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
        String visitedPropertyName = VISITED + '_' + project.name
        if (project.findProperty(visitedPropertyName)) {
            return
        }
        project.ext[visitedPropertyName] = true

        BasePlugin.applyIfMissing(project)

        project.afterEvaluate {
            ProjectConfigurationExtension mergedConfiguration = project.ext.mergedConfiguration

            if (mergedConfiguration.stats.enabled) {
                if(isRootProject(project) && project.childProjects.isEmpty()) {
                    maybeCreateStatsTask(project)
                } else if (!isRootProject(project)) {
                    maybeCreateStatsTask(project)
                }
            }
        }

        if (isRootProject(project) && !project.childProjects.isEmpty()) {
            project.gradle.addBuildListener(new BuildAdapter() {
                @Override
                void projectsEvaluated(Gradle gradle) {
                    applyAggregateStats(project)
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
        ProjectConfigurationExtension mergedConfiguration = project.ext.mergedConfiguration

        SourceStatsTask statsTask = project.tasks.create('sourceStats', SourceStatsTask) {
            enabled = mergedConfiguration.stats.enabled
            group = 'Reporting'
            description = 'Generates a report on lines of code'
            paths = mergedConfiguration.stats.paths
            formats = mergedConfiguration.stats.formats
            counters = mergedConfiguration.stats.counters
        }

        if (mergedConfiguration.stats.enabled) {
            mergedConfiguration.stats.projects() << project
            mergedConfiguration.stats.statsTasks() << statsTask
        }
    }

    private void applyAggregateStats(Project project) {
        ProjectConfigurationExtension mergedConfiguration = project.ext.mergedConfiguration

        Set<Project> allProjects = new LinkedHashSet<>(mergedConfiguration.stats.projects())
        Set<Task> allStatsTasks = new LinkedHashSet<>(mergedConfiguration.stats.statsTasks())

        project.childProjects.values()*.mergedConfiguration.stats.each { MutableStats e ->
            if (e.enabled) {
                allStatsTasks.addAll(e.statsTasks())
            }
        }

        project.tasks.create(AGGREGATE_STATS_TASK_NAME, AggregateSourceStatsReportTask) {
            enabled = mergedConfiguration.stats.enabled
            group = 'Reporting'
            description = 'Aggregate source stats reports.'
            dependsOn allStatsTasks
            formats = mergedConfiguration.stats.formats
        }
    }
}
