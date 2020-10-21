/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2020 Andres Almiray.
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
package org.kordamp.gradle.plugin.insight.internal

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.BuildResult
import org.gradle.api.Action
import org.gradle.api.ProjectEvaluationListener
import org.gradle.api.ProjectState
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.execution.TaskExecutionGraphListener
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.tasks.TaskStateInternal
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskState
import org.kordamp.gradle.plugin.insight.InsightExtension
import org.kordamp.gradle.plugin.insight.model.Build
import org.kordamp.gradle.plugin.insight.model.BuildReport
import org.kordamp.gradle.plugin.insight.model.Project
import org.kordamp.gradle.plugin.insight.model.Task
import org.kordamp.gradle.plugin.insight.reports.SummaryBuildReport

import java.time.ZonedDateTime

/**
 *
 * @author Andres Almiray
 * @since 0.40.0
 */
@CompileStatic
class BuildHelper extends BuildAdapter implements ProjectEvaluationListener,
    TaskExecutionGraphListener,
    TaskExecutionListener {

    private final Settings settings
    private final Build build = new Build()

    BuildHelper(Settings settings) {
        this.settings = settings
    }

    @Override
    void settingsEvaluated(Settings settings) {
        build.setSettingsEvaluated(System.currentTimeMillis())
        build.setRootProjectName(settings.rootProject.name)
    }

    @Override
    void projectsLoaded(Gradle gradle) {
        build.setProjectsLoaded(System.currentTimeMillis())
    }

    @Override
    void projectsEvaluated(Gradle gradle) {
        build.setProjectsEvaluated(System.currentTimeMillis())
    }

    @Override
    void buildFinished(BuildResult result) {
        build.setEnd(ZonedDateTime.now())
        build.setFailure(result.failure != null)
        writeReports()
    }

    @Override
    void beforeEvaluate(org.gradle.api.Project project) {
        Project p = build.projects.computeIfAbsent(project.path, { k -> new Project(k, project.name, project) })
        long millis = System.currentTimeMillis()
        p.setStartEvaluate(millis)
        p.setEndEvaluate(millis)
    }

    @Override
    void afterEvaluate(org.gradle.api.Project project, ProjectState projectState) {
        Project p = build.projects.get(project.path)
        p.setEndEvaluate(System.currentTimeMillis())
    }

    @Override
    @CompileDynamic
    void graphPopulated(TaskExecutionGraph graph) {
        build.setGraphPopulated(System.currentTimeMillis())

        graph.allTasks.groupBy { it.project.path }
            .each { projectPath, tasks ->
                build.projects[projectPath].tasksToBeExecuted.addAll(tasks*.path)
            }
    }

    @Override
    void beforeExecute(org.gradle.api.Task task) {
        Task t = build.projects.get(task.project.path)
            .tasks.computeIfAbsent(task.path, { k -> new Task(k, task.name) })
        long millis = System.currentTimeMillis()
        t.setBeforeExecute(millis)
        t.setAfterExecute(millis)

        Class<?> clazz = task.getClass()
        while (clazz != null) {
            if (clazz.getAnnotation(CacheableTask.class) != null) {
                t.setCacheable(true)
                break
            }
            clazz = clazz.getSuperclass()
        }
    }

    @Override
    void afterExecute(org.gradle.api.Task task, TaskState state) {
        Task t = build.projects.get(task.project.path)
            .tasks.get(task.path)
        t.setAfterExecute(System.currentTimeMillis())

        t.setExecuted(state.executed)
        t.setSkipped(state.skipped)
        t.setUpToDate(state.upToDate)
        t.setDidWork(state.didWork)
        t.setNoSource(state.noSource)
        t.setFailed(state.failure != null)
        if (state instanceof TaskStateInternal) {
            t.setFromCache(((TaskStateInternal) state).fromCache)
            t.setActionable(((TaskStateInternal) state).actionable)
        }
    }

    private void writeReports() {
        InsightExtensionImpl extension = (InsightExtensionImpl) settings.extensions.findByType(InsightExtension)
        if (!extension.resolvedEnabled.get()) return

        if (!extension.reports) {
            extension.report(SummaryBuildReport, new Action<SummaryBuildReport>() {
                @Override
                void execute(SummaryBuildReport buildReport) {
                    // empty
                }
            })
        }

        for (BuildReport report : extension.reports) {
            if (!report.enabled.get()) continue

            try {
                report.report(settings.gradle, extension, build)
            } catch (Exception ignored) {
                ignored.printStackTrace()
            }
        }
    }
}
