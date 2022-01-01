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
package org.kordamp.gradle.plugin.codenarc

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.plugins.quality.CodeNarc
import org.gradle.api.plugins.quality.CodeNarcExtension
import org.gradle.api.plugins.quality.CodeNarcPlugin
import org.gradle.api.tasks.TaskProvider
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.codenarc.tasks.InitCodenarcTask

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * @author Andres Almiray
 * @since 0.31.0
 */
@CompileStatic
class CodenarcPlugin extends AbstractKordampPlugin {
    static final String INIT_CODENARC_TASK_NAME = 'initCodenarc'
    static final String ALL_CODENARC_TASK_NAME = 'allCodenarc'
    static final String AGGREGATE_CODENARC_TASK_NAME = 'aggregateCodenarc'

    Project project

    CodenarcPlugin() {
        super(org.kordamp.gradle.plugin.base.plugins.Codenarc.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        configureProject(project)
        if (isRootProject(project)) {
            configureRootProject(project)
            project.childProjects.values().each {
                it.pluginManager.apply(CodenarcPlugin)
            }
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(CodenarcPlugin)) {
            project.pluginManager.apply(CodenarcPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        project.pluginManager.apply(CodeNarcPlugin)

        project.tasks.register(INIT_CODENARC_TASK_NAME, InitCodenarcTask,
            new Action<InitCodenarcTask>() {
                @Override
                void execute(InitCodenarcTask t) {
                    t.group = 'Project Setup'
                    t.description = 'Initializes codenarc configuration'
                }
            })

        project.pluginManager.withPlugin('java-base', new Action<AppliedPlugin>() {
            @Override
            void execute(AppliedPlugin appliedPlugin) {

                TaskProvider<CodeNarc> allCodenarcTask = null
                if (project.childProjects.isEmpty()) {
                    allCodenarcTask = project.tasks.register(ALL_CODENARC_TASK_NAME, CodeNarc,
                        new Action<CodeNarc>() {
                            @Override
                            void execute(CodeNarc t) {
                                t.enabled = false
                                t.group = 'Quality'
                                t.description = 'Run Codenarc analysis on all classes.'
                            }
                        })
                }

                project.afterEvaluate {
                    ProjectConfigurationExtension config = resolveEffectiveConfig(project)
                    setEnabled(config.quality.codenarc.enabled)

                    CodeNarcExtension codenarcExt = project.extensions.findByType(CodeNarcExtension)
                    codenarcExt.toolVersion = config.quality.codenarc.toolVersion

                    project.tasks.withType(CodeNarc) { CodeNarc task ->
                        task.setGroup('Quality')
                        config.quality.codenarc.applyTo(task)
                        String sourceSetName = task.name['codenarc'.size()..-1].uncapitalize()
                        if (sourceSetName in config.quality.codenarc.excludedSourceSets) {
                            task.enabled = false
                        }
                    }

                    if (allCodenarcTask) {
                        configureAllCodenarcTask(project, allCodenarcTask)
                    }
                }
            }
        })
    }

    private void configureRootProject(Project project) {
        TaskProvider<CodeNarc> aggregateCodenarcTask = project.tasks.register(AGGREGATE_CODENARC_TASK_NAME, CodeNarc,
            new Action<CodeNarc>() {
                @Override
                void execute(CodeNarc t) {
                    t.enabled = false
                    t.group = 'Quality'
                    t.description = 'Aggregate all codenarc reports.'
                }
            })

        project.gradle.addBuildListener(new BuildAdapter() {
            @Override
            void projectsEvaluated(Gradle gradle) {
                configureAggregateCodenarcTask(project,
                    aggregateCodenarcTask)
            }
        })
    }

    @CompileDynamic
    private void configureAggregateCodenarcTask(Project project,
                                                TaskProvider<CodeNarc> aggregateCodenarcTask) {
        ProjectConfigurationExtension config = resolveEffectiveConfig(project)

        CodeNarcExtension codenarcExt = project.extensions.findByType(CodeNarcExtension)
        codenarcExt.toolVersion = config.quality.codenarc.toolVersion

        Set<CodeNarc> tt = new LinkedHashSet<>()
        project.tasks.withType(CodeNarc) { CodeNarc task ->
            if (project in config.quality.codenarc.aggregate.excludedProjects) return
            if (task.name != ALL_CODENARC_TASK_NAME &&
                task.name != AGGREGATE_CODENARC_TASK_NAME &&
                task.enabled) tt << task
        }

        project.childProjects.values().each { p ->
            if (p in config.quality.codenarc.aggregate.excludedProjects) return
            p.tasks.withType(CodeNarc) { CodeNarc task ->
                if (task.name != ALL_CODENARC_TASK_NAME &&
                    task.enabled) tt << task
            }
        }

        aggregateCodenarcTask.configure(new Action<CodeNarc>() {
            @Override
            void execute(CodeNarc t) {
                config.quality.codenarc.applyTo(t)
                t.enabled = config.quality.codenarc.aggregate.enabled && config.quality.codenarc.configFile.exists() && tt.size() > 0
                t.ignoreFailures = false
                t.source(*((tt*.source).unique()))
                t.compilationClasspath = project.files(*((tt*.compilationClasspath).unique()))
                t.codenarcClasspath = project.files(*((tt*.codenarcClasspath).unique()))
            }
        })
    }

    private void configureAllCodenarcTask(Project project,
                                          TaskProvider<CodeNarc> allCodenarcTasks) {
        ProjectConfigurationExtension config = resolveEffectiveConfig(project)

        Set<CodeNarc> tt = new LinkedHashSet<>()
        project.tasks.withType(CodeNarc) { CodeNarc task ->
            if (task.name != ALL_CODENARC_TASK_NAME &&
                task.enabled) tt << task
        }

        allCodenarcTasks.configure(new Action<CodeNarc>() {
            @Override
            @CompileDynamic
            void execute(CodeNarc t) {
                config.quality.codenarc.applyTo(t)
                t.enabled &= tt.size() > 0
                t.source(*((tt*.source).unique()))
                t.compilationClasspath = project.files(*((tt*.compilationClasspath).unique()))
                t.codenarcClasspath = project.files(*((tt*.codenarcClasspath).unique()))
            }
        })
    }
}
