/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2021 Andres Almiray.
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
package org.kordamp.gradle.plugin.checkstyle

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.TaskProvider
import org.kordamp.gradle.annotations.DependsOn
import org.kordamp.gradle.listener.AllProjectsEvaluatedListener
import org.kordamp.gradle.listener.ProjectEvaluatedListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.checkstyle.tasks.InitCheckstyleTask

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
class CheckstylePlugin extends AbstractKordampPlugin {
    static final String INIT_CHECKSTYLE_TASK_NAME = 'initCheckstyle'
    static final String ALL_CHECKSTYLE_TASK_NAME = 'allCheckstyle'
    static final String AGGREGATE_CHECKSTYLE_TASK_NAME = 'aggregateCheckstyle'

    Project project

    CheckstylePlugin() {
        super(org.kordamp.gradle.plugin.base.plugins.Checkstyle.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        configureProject(project)
        if (isRootProject(project)) {
            configureRootProject(project)
            project.childProjects.values().each {
                it.pluginManager.apply(CheckstylePlugin)
            }
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(CheckstylePlugin)) {
            project.pluginManager.apply(CheckstylePlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        project.pluginManager.apply(org.gradle.api.plugins.quality.CheckstylePlugin)

        project.tasks.register(INIT_CHECKSTYLE_TASK_NAME, InitCheckstyleTask,
            new Action<InitCheckstyleTask>() {
                @Override
                void execute(InitCheckstyleTask t) {
                    t.group = 'Project Setup'
                    t.description = 'Initializes checkstyle configuration'
                }
            })

        project.pluginManager.withPlugin('java-base', new Action<AppliedPlugin>() {
            @Override
            void execute(AppliedPlugin appliedPlugin) {
                TaskProvider<Checkstyle> allCheckstyleTask = null
                if (project.childProjects.isEmpty()) {
                    allCheckstyleTask = project.tasks.register(ALL_CHECKSTYLE_TASK_NAME, Checkstyle,
                        new Action<Checkstyle>() {
                            @Override
                            void execute(Checkstyle t) {
                                t.enabled = false
                                t.group = 'Quality'
                                t.description = 'Run Checkstyle analysis on all classes.'
                            }
                        })
                }

                addProjectEvaluatedListener(project, new CheckstyleProjectEvaluatedListener(allCheckstyleTask))
            }
        })
    }

    private void configureRootProject(Project project) {
        addAllProjectsEvaluatedListener(project, new CheckstyleAllProjectsEvaluatedListener())

        TaskProvider<Checkstyle> aggregateTask = project.tasks.register(AGGREGATE_CHECKSTYLE_TASK_NAME, Checkstyle,
            new Action<Checkstyle>() {
                @Override
                void execute(Checkstyle t) {
                    t.enabled = false
                    t.group = 'Quality'
                    t.description = 'Aggregate all checkstyle reports.'
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

    @Named('checkstyle')
    @DependsOn(['base'])
    private class CheckstyleProjectEvaluatedListener implements ProjectEvaluatedListener {
        private final TaskProvider<Checkstyle> allCheckstyleTask

        CheckstyleProjectEvaluatedListener(TaskProvider<Checkstyle> allCheckstyleTask) {
            this.allCheckstyleTask = allCheckstyleTask
        }

        @Override
        void projectEvaluated(Project project) {
            ProjectConfigurationExtension config = resolveConfig(project)
            setEnabled(config.quality.checkstyle.enabled)

            CheckstyleExtension checkstyleExt = project.extensions.findByType(CheckstyleExtension)
            checkstyleExt.toolVersion = config.quality.checkstyle.toolVersion

            project.tasks.withType(Checkstyle) { Checkstyle task ->
                task.enabled = config.quality.checkstyle.enabled
                task.setGroup('Quality')
                config.quality.checkstyle.applyTo(task)
                String sourceSetName = task.name['checkstyle'.size()..-1].uncapitalize()
                if (sourceSetName in config.quality.checkstyle.excludedSourceSets) {
                    task.enabled = false
                }
            }

            if (allCheckstyleTask) {
                Set<Checkstyle> tt = new LinkedHashSet<>()
                project.tasks.withType(Checkstyle) { Checkstyle task ->
                    if (task.name != ALL_CHECKSTYLE_TASK_NAME &&
                        task.enabled) tt << task
                }

                allCheckstyleTask.configure(new Action<Checkstyle>() {
                    @Override
                    @CompileDynamic
                    void execute(Checkstyle t) {
                        config.quality.checkstyle.applyTo(t)
                        t.enabled &= tt.size() > 0
                        t.source(*((tt*.source).unique()))
                        t.classpath = project.files(*((tt*.classpath).unique()))
                        t.checkstyleClasspath = project.files(*((tt*.checkstyleClasspath).unique()))
                    }
                })
            }
        }
    }

    @Named('checkstyle')
    @DependsOn(['base'])
    private class CheckstyleAllProjectsEvaluatedListener implements AllProjectsEvaluatedListener {
        @Override
        void allProjectsEvaluated(Project rootProject) {
            configureAggregateCheckstyleTask(rootProject)
        }
    }

    private void configureAggregateCheckstyleTask(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)

        CheckstyleExtension checkstyleExt = project.extensions.findByType(CheckstyleExtension)
        checkstyleExt.toolVersion = config.quality.checkstyle.toolVersion

        Set<Checkstyle> tt = new LinkedHashSet<>()
        project.tasks.withType(Checkstyle) { Checkstyle task ->
            if (project in config.quality.checkstyle.aggregate.excludedProjects) return
            if (task.name != ALL_CHECKSTYLE_TASK_NAME &&
                task.name != AGGREGATE_CHECKSTYLE_TASK_NAME &&
                task.enabled)
                tt << task
        }

        project.childProjects.values().each { p ->
            if (p in config.quality.checkstyle.aggregate.excludedProjects) return
            p.tasks.withType(Checkstyle) { Checkstyle task ->
                if (task.name != ALL_CHECKSTYLE_TASK_NAME &&
                    task.enabled) tt << task
            }
        }

        project.tasks.named(AGGREGATE_CHECKSTYLE_TASK_NAME, Checkstyle, new Action<Checkstyle>() {
            @Override
            @CompileDynamic
            void execute(Checkstyle t) {
                config.quality.checkstyle.applyTo(t)
                t.enabled = config.quality.checkstyle.aggregate.enabled && config.quality.checkstyle.configFile.exists() && tt.size() > 0
                t.ignoreFailures = false
                t.source(*((tt*.source).unique()))
                t.classpath = project.files(*((tt*.classpath).unique()))
                t.checkstyleClasspath = project.files(*((tt*.checkstyleClasspath).unique()))
            }
        })
    }
}
