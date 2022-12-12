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
package org.kordamp.gradle.plugin.spotbugs

import com.github.spotbugs.snom.SpotBugsExtension
import com.github.spotbugs.snom.SpotBugsPlugin
import com.github.spotbugs.snom.SpotBugsTask
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.TaskProvider
import org.kordamp.gradle.annotations.DependsOn
import org.kordamp.gradle.listener.AllProjectsEvaluatedListener
import org.kordamp.gradle.listener.ProjectEvaluatedListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.Spotbugs

import javax.inject.Named

import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addAllProjectsEvaluatedListener
import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addProjectEvaluatedListener
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject
import static org.kordamp.gradle.util.PluginUtils.resolveConfig

/**
 * @author Andres Almiray
 * @since 0.31.0
 */
@CompileStatic
class SpotbugsPlugin extends AbstractKordampPlugin {
    static final String ALL_SPOTBUGS_TASK_NAME = 'allSpotbugs'
    static final String AGGREGATE_SPOTBUGS_TASK_NAME = 'aggregateSpotbugs'

    Project project

    SpotbugsPlugin() {
        super(Spotbugs.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        configureProject(project)
        if (isRootProject(project)) {
            configureRootProject(project)
            project.childProjects.values().each {
                it.pluginManager.apply(SpotbugsPlugin)
            }
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(SpotbugsPlugin)) {
            project.pluginManager.apply(SpotbugsPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        project.pluginManager.apply(SpotBugsPlugin)

        project.pluginManager.withPlugin('java-base', new Action<AppliedPlugin>() {
            @Override
            void execute(AppliedPlugin appliedPlugin) {
                TaskProvider<SpotBugsTask> allSpotBugsTask = null
                if (project.childProjects.isEmpty()) {
                    allSpotBugsTask = project.tasks.register(ALL_SPOTBUGS_TASK_NAME, SpotBugsTask,
                        new Action<SpotBugsTask>() {
                            @Override
                            void execute(SpotBugsTask t) {
                                t.enabled = false
                                t.group = 'Quality'
                                t.description = 'Run SpotBugs analysis on all classes.'
                            }
                        })
                }

                addProjectEvaluatedListener(project, new SpotbugsProjectEvaluatedListener(allSpotBugsTask))
            }
        })
    }

    private void configureRootProject(Project project) {
        addAllProjectsEvaluatedListener(project, new SpotbugsAllProjectsEvaluatedListener())

        TaskProvider<SpotBugsTask> aggregateTask = project.tasks.register(AGGREGATE_SPOTBUGS_TASK_NAME, SpotBugsTask,
            new Action<SpotBugsTask>() {
                @Override
                void execute(SpotBugsTask t) {
                    t.enabled = false
                    t.group = 'Quality'
                    t.description = 'Aggregate all spotbugs reports.'
                }
            })

        project.tasks.matching(new Spec<Task>() {
            @Override
            boolean isSatisfiedBy(Task t) {
                return t.name == 'check'
            }
        }).all(new Action<Task>() {
            @Override
            void execute(Task t) {
                t.dependsOn(aggregateTask)
            }
        })
    }

    @Named('spotbugs')
    @DependsOn(['base'])
    private class SpotbugsProjectEvaluatedListener implements ProjectEvaluatedListener {
        private final TaskProvider<SpotBugsTask> allSpotBugsTask

        SpotbugsProjectEvaluatedListener(TaskProvider<SpotBugsTask> allSpotBugsTask) {
            this.allSpotBugsTask = allSpotBugsTask
        }

        @Override
        void projectEvaluated(Project project) {
            ProjectConfigurationExtension config = resolveConfig(project)
            setEnabled(config.quality.spotbugs.enabled)

            SpotBugsExtension spotbugsExt = project.extensions.findByType(SpotBugsExtension)
            spotbugsExt.toolVersion.set(config.quality.spotbugs.toolVersion)

            project.tasks.withType(SpotBugsTask) { SpotBugsTask task ->
                task.enabled = config.quality.spotbugs.enabled
                task.setGroup('Quality')
                applyTo(config, task)
                String sourceSetName = task.name['spotbugs'.size()..-1].uncapitalize()
                if (sourceSetName in config.quality.spotbugs.excludedSourceSets) {
                    task.enabled = false
                }
            }

            if (allSpotBugsTask) {
                Set<SpotBugsTask> tt = new LinkedHashSet<>()
                project.tasks.withType(SpotBugsTask) { SpotBugsTask task ->
                    if (task.name != ALL_SPOTBUGS_TASK_NAME) tt << task
                }

                allSpotBugsTask.configure(new Action<SpotBugsTask>() {
                    @Override
                    @CompileDynamic
                    void execute(SpotBugsTask t) {
                        applyTo(config, t)
                        t.enabled &= tt.size() > 0
                        t.sourceDirs = project.files(*((tt*.sourceDirs).unique()))
                        t.classDirs = project.files(*((tt*.classDirs).flatten()))
                        t.auxClassPaths = project.files(*((tt*.auxClassPaths).unique()))
                    }
                })
            }
        }
    }

    @Named('spotbugs')
    @DependsOn(['base'])
    private class SpotbugsAllProjectsEvaluatedListener implements AllProjectsEvaluatedListener {
        @Override
        void allProjectsEvaluated(Project rootProject) {
            configureAggregateSpotBugsTask(rootProject)
        }
    }

    private void configureAggregateSpotBugsTask(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)

        SpotBugsExtension spotbugsExt = project.extensions.findByType(SpotBugsExtension)
        spotbugsExt.toolVersion.set(config.quality.spotbugs.toolVersion)

        Set<SpotBugsTask> tt = new LinkedHashSet<>()
        project.tasks.withType(SpotBugsTask) { SpotBugsTask task ->
            if (project in config.quality.spotbugs.aggregate.excludedProjects) return
            if (task.name != ALL_SPOTBUGS_TASK_NAME &&
                task.name != AGGREGATE_SPOTBUGS_TASK_NAME &&
                task.name) tt << task
        }

        project.childProjects.values().each { p ->
            if (p in config.quality.spotbugs.aggregate.excludedProjects) return
            p.tasks.withType(SpotBugsTask) { SpotBugsTask task ->
                if (task.name != ALL_SPOTBUGS_TASK_NAME &&
                    task.enabled) tt << task
            }
        }

        project.tasks.named(AGGREGATE_SPOTBUGS_TASK_NAME, SpotBugsTask, new Action<SpotBugsTask>() {
            @Override
            @CompileDynamic
            void execute(SpotBugsTask t) {
                applyTo(config, t)
                t.enabled = config.quality.spotbugs.aggregate.enabled && tt.size() > 0
                t.ignoreFailures = false
                t.sourceDirs = project.files(*((tt*.sourceDirs).unique()))
                t.classDirs = project.files(*((tt*.classDirs).flatten()))
                t.auxClassPaths = project.files(*((tt*.auxClassPaths).unique()))
            }
        })
    }

    @CompileDynamic
    private static void applyTo(ProjectConfigurationExtension config, SpotBugsTask t) {
        String sourceSetName = (t.name - 'spotbugs').uncapitalize()
        sourceSetName = sourceSetName == 'allSpotbugs' ? config.project.name : sourceSetName
        sourceSetName = sourceSetName == 'aggregateSpotbugs' ? 'aggregate' : sourceSetName
        t.setEnabled(config.quality.spotbugs.enabled)
        if (config.quality.spotbugs.includeFilterFile.exists()) t.includeFilterConfig.setFrom(config.quality.spotbugs.includeFilterFile)
        if (config.quality.spotbugs.excludeFilterFile.exists()) t.excludeFilterConfig.setFrom(config.quality.spotbugs.excludeFilterFile)
        if (config.quality.spotbugs.excludeBugsFilterFile.exists()) t.excludeBugsFilterConfig.setFrom(config.quality.spotbugs.excludeBugsFilterFile)
        if (config.quality.spotbugs.includes) t.includes.addAll(config.quality.spotbugs.includes)
        if (config.quality.spotbugs.excludes) t.excludes.addAll(config.quality.spotbugs.excludes)
        if (config.quality.spotbugs.visitors) t.visitors.addAll(config.quality.spotbugs.visitors)
        if (config.quality.spotbugs.omitVisitors) t.omitVisitors.addAll(config.quality.spotbugs.omitVisitors)
        t.showProgress.set(config.quality.spotbugs.showProgress)
        t.ignoreFailures = config.quality.spotbugs.getIgnoreFailures()
        t.effort = config.quality.spotbugs.effort
        t.reportLevel = config.quality.spotbugs.reportLevel
        if (config.quality.spotbugs.extraArgs) t.extraArgs.addAll(config.quality.spotbugs.extraArgs)
        if (config.quality.spotbugs.jvmArgs) t.jvmArgs.addAll(config.quality.spotbugs.jvmArgs)
        switch (config.quality.spotbugs.report?.toLowerCase()) {
            case 'xml':
                t.reports.maybeCreate('xml').enabled = true
                t.reports.maybeCreate('htm').enabled = false
                break
            case 'html':
            default:
                t.reports.maybeCreate('xml').enabled = false
                t.reports.maybeCreate('html').enabled = true
                break
        }
        t.reports.maybeCreate('html').destination = config.project.layout.buildDirectory.file("reports/spotbugs/${sourceSetName}.html").get().asFile
        t.reports.maybeCreate('xml').destination = config.project.layout.buildDirectory.file("reports/spotbugs/${sourceSetName}.xml").get().asFile
    }
}
