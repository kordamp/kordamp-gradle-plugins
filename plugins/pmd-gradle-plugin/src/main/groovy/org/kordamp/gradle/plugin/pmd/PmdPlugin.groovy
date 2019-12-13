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
package org.kordamp.gradle.plugin.pmd

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.plugins.quality.Pmd
import org.gradle.api.plugins.quality.PmdExtension
import org.gradle.api.tasks.TaskProvider
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.pmd.tasks.InitPmdTask

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

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
        if (!project.plugins.findPlugin(PmdPlugin)) {
            project.pluginManager.apply(PmdPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

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

                project.afterEvaluate {
                    ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
                    setEnabled(effectiveConfig.quality.pmd.enabled)

                    PmdExtension pmdExt = project.extensions.findByType(PmdExtension)
                    pmdExt.toolVersion = effectiveConfig.quality.pmd.toolVersion

                    project.tasks.withType(Pmd) { Pmd task ->
                        task.setGroup('Quality')
                        effectiveConfig.quality.pmd.applyTo(task)
                    }

                    if (allPmdTask) {
                        configureAllPmdTask(project, allPmdTask)
                    }
                }
            }
        })

        if (isRootProject(project)) {
            TaskProvider<Pmd> aggregatePmdTask = project.tasks.register(AGGREGATE_PMD_TASK_NAME, Pmd,
                new Action<Pmd>() {
                    @Override
                    void execute(Pmd t) {
                        t.enabled = false
                        t.group = 'Quality'
                        t.description = 'Aggregate all pmd reports.'
                    }
                })

            project.gradle.addBuildListener(new BuildAdapter() {
                @Override
                void projectsEvaluated(Gradle gradle) {
                    configureAggregatePmdTask(project,
                        aggregatePmdTask)
                }
            })
        }
    }

    @CompileDynamic
    private void configureAggregatePmdTask(Project project,
                                           TaskProvider<Pmd> aggregatePmdTask) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        if (!effectiveConfig.quality.pmd.enabled) {
            return
        }

        Set<Pmd> tt = new LinkedHashSet<>()
        project.tasks.withType(Pmd) { Pmd task ->
            if (task.name != ALL_PMD_TASK_NAME &&
                task.name != AGGREGATE_PMD_TASK_NAME) tt << task
        }

        project.childProjects.values().each { p ->
            org.kordamp.gradle.plugin.base.plugins.Pmd e = resolveEffectiveConfig(p).quality.pmd
            if (e.enabled) {
                p.tasks.withType(Pmd) { Pmd task ->
                    if (task.name != ALL_PMD_TASK_NAME) tt << task
                }
            }
        }

        aggregatePmdTask.configure(new Action<Pmd>() {
            @Override
            void execute(Pmd t) {
                effectiveConfig.quality.pmd.applyTo(t)
                t.enabled &= tt.size() > 0
                t.ignoreFailures = false
                t.source(*((tt*.source).unique()))
                t.classpath = project.files(*((tt*.classpath).unique()))
                t.pmdClasspath = project.files(*((tt*.pmdClasspath).unique()))
            }
        })
    }

    private void configureAllPmdTask(Project project,
                                     TaskProvider<Pmd> allPmdTasks) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        if (!effectiveConfig.quality.pmd.enabled) {
            return
        }

        Set<Pmd> tt = new LinkedHashSet<>()
        project.tasks.withType(Pmd) { Pmd task ->
            if (task.name != ALL_PMD_TASK_NAME) tt << task
        }

        allPmdTasks.configure(new Action<Pmd>() {
            @Override
            @CompileDynamic
            void execute(Pmd t) {
                effectiveConfig.quality.pmd.applyTo(t)
                t.enabled &= tt.size() > 0
                t.source(*((tt*.source).unique()))
                t.classpath = project.files(*((tt*.classpath).unique()))
                t.pmdClasspath = project.files(*((tt*.pmdClasspath).unique()))
            }
        })
    }
}
