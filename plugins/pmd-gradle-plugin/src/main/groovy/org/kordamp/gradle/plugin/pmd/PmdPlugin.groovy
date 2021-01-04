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
package org.kordamp.gradle.plugin.pmd

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.plugins.quality.Pmd
import org.gradle.api.plugins.quality.PmdExtension
import org.gradle.api.tasks.TaskProvider
import org.kordamp.gradle.annotations.DependsOn
import org.kordamp.gradle.listener.AllProjectsEvaluatedListener
import org.kordamp.gradle.listener.ProjectEvaluatedListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.pmd.tasks.InitPmdTask

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
class PmdPlugin extends AbstractKordampPlugin {
    static final String INIT_PMD_TASK_NAME = 'initPmd'
    static final String ALL_PMD_TASK_NAME = 'allPmd'
    static final String AGGREGATE_PMD_TASK_NAME = 'aggregatePmd'

    Project project

    PmdPlugin() {
        super(org.kordamp.gradle.plugin.base.plugins.Pmd.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        configureProject(project)
        if (isRootProject(project)) {
            configureRootProject(project)
            project.childProjects.values().each {
                it.pluginManager.apply(PmdPlugin)
            }
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(PmdPlugin)) {
            project.pluginManager.apply(PmdPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        project.pluginManager.apply(org.gradle.api.plugins.quality.PmdPlugin)

        project.tasks.register(INIT_PMD_TASK_NAME, InitPmdTask,
            new Action<InitPmdTask>() {
                @Override
                void execute(InitPmdTask t) {
                    t.group = 'Project Setup'
                    t.description = 'Initializes pmd configuration'
                }
            })

        project.pluginManager.withPlugin('java-base', new Action<AppliedPlugin>() {
            @Override
            void execute(AppliedPlugin appliedPlugin) {
                TaskProvider<Pmd> allPmdTask = null
                if (project.childProjects.isEmpty()) {
                    allPmdTask = project.tasks.register(ALL_PMD_TASK_NAME, Pmd,
                        new Action<Pmd>() {
                            @Override
                            void execute(Pmd t) {
                                t.enabled = false
                                t.group = 'Quality'
                                t.description = 'Run Pmd analysis on all classes.'
                            }
                        })
                }

                addProjectEvaluatedListener(project, new PmdProjectEvaluatedListener(allPmdTask))
            }
        })
    }

    private void configureRootProject(Project project) {
        addAllProjectsEvaluatedListener(project, new PmdAllProjectsEvaluatedListener())

        project.tasks.register(AGGREGATE_PMD_TASK_NAME, Pmd,
            new Action<Pmd>() {
                @Override
                void execute(Pmd t) {
                    t.enabled = false
                    t.group = 'Quality'
                    t.description = 'Aggregate all pmd reports.'
                }
            })
    }

    @Named('pmd')
    @DependsOn(['base'])
    private class PmdProjectEvaluatedListener implements ProjectEvaluatedListener {
        private final TaskProvider<Pmd> allPmdTask

        PmdProjectEvaluatedListener(TaskProvider<Pmd> allPmdTask) {
            this.allPmdTask = allPmdTask
        }

        @Override
        void projectEvaluated(Project project) {
            ProjectConfigurationExtension config = resolveConfig(project)
            setEnabled(config.quality.pmd.enabled)

            PmdExtension pmdExt = project.extensions.findByType(PmdExtension)
            pmdExt.toolVersion = config.quality.pmd.toolVersion

            project.tasks.withType(Pmd) { Pmd task ->
                task.setGroup('Quality')
                config.quality.pmd.applyTo(task)
                String sourceSetName = task.name['pmd'.size()..-1].uncapitalize()
                if (sourceSetName in config.quality.pmd.excludedSourceSets) {
                    task.enabled = false
                }
            }

            if (allPmdTask) {
                Set<Pmd> tt = new LinkedHashSet<>()
                project.tasks.withType(Pmd) { Pmd task ->
                    if (task.name != ALL_PMD_TASK_NAME &&
                        task.enabled) tt << task
                }

                allPmdTask.configure(new Action<Pmd>() {
                    @Override
                    @CompileDynamic
                    void execute(Pmd t) {
                        config.quality.pmd.applyTo(t)
                        t.enabled &= tt.size() > 0
                        t.source(*((tt*.source).unique()))
                        t.classpath = project.files(*((tt*.classpath).unique()))
                        t.pmdClasspath = project.files(*((tt*.pmdClasspath).unique()))
                    }
                })
            }
        }
    }

    @Named('pmd')
    @DependsOn(['base'])
    private class PmdAllProjectsEvaluatedListener implements AllProjectsEvaluatedListener {
        @Override
        void allProjectsEvaluated(Project rootProject) {
            configureAggregatePmdTask(rootProject)
        }
    }

    private void configureAggregatePmdTask(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)

        PmdExtension pmdExt = project.extensions.findByType(PmdExtension)
        pmdExt.toolVersion = config.quality.pmd.toolVersion

        Set<Pmd> tt = new LinkedHashSet<>()
        project.tasks.withType(Pmd) { Pmd task ->
            if (project in config.quality.pmd.aggregate.excludedProjects) return
            if (task.name != ALL_PMD_TASK_NAME &&
                task.name != AGGREGATE_PMD_TASK_NAME &&
                task.enabled) tt << task
        }

        project.childProjects.values().each { p ->
            if (p in config.quality.pmd.aggregate.excludedProjects) return
            p.tasks.withType(Pmd) { Pmd task ->
                if (task.name != ALL_PMD_TASK_NAME &&
                    task.enabled) tt << task
            }
        }

        project.tasks.named(AGGREGATE_PMD_TASK_NAME, Pmd, new Action<Pmd>() {
            @Override
            @CompileDynamic
            void execute(Pmd t) {
                config.quality.pmd.applyTo(t)
                t.enabled = config.quality.pmd.aggregate.enabled &&
                    !config.quality.pmd.ruleSetFiles.empty &&
                    config.quality.pmd.ruleSetFiles.files.every { it.exists() } &&
                    tt.size() > 0
                t.ignoreFailures = false
                t.source(*((tt*.source).unique()))
                t.classpath = project.files(*((tt*.classpath).unique()))
                t.pmdClasspath = project.files(*((tt*.pmdClasspath).unique()))
            }
        })
    }
}
