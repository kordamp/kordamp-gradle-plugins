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
package org.kordamp.gradle.plugin.cpd

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.tasks.TaskProvider
import org.kordamp.gradle.annotations.DependsOn
import org.kordamp.gradle.listener.AllProjectsEvaluatedListener
import org.kordamp.gradle.listener.ProjectEvaluatedListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

import javax.inject.Named

import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addAllProjectsEvaluatedListener
import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addProjectEvaluatedListener
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject
import static org.kordamp.gradle.util.PluginUtils.resolveConfig

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
class CpdPlugin extends AbstractKordampPlugin {
    static final String ALL_CPD_TASK_NAME = 'allCpd'
    static final String AGGREGATE_CPD_TASK_NAME = 'aggregateCpd'

    Project project

    CpdPlugin() {
        super(org.kordamp.gradle.plugin.base.plugins.Cpd.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        configureProject(project)
        if (isRootProject(project)) {
            configureRootProject(project)
            project.childProjects.values().each {
                it.pluginManager.apply(CpdPlugin)
            }
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(CpdPlugin)) {
            project.pluginManager.apply(CpdPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        project.pluginManager.apply(BaseCpdPlugin)

        project.pluginManager.withPlugin('java-base', new Action<AppliedPlugin>() {
            @Override
            void execute(AppliedPlugin appliedPlugin) {
                TaskProvider<Cpd> allCpdTask = null
                if (project.childProjects.isEmpty()) {
                    allCpdTask = project.tasks.register(ALL_CPD_TASK_NAME, Cpd,
                        new Action<Cpd>() {
                            @Override
                            void execute(Cpd t) {
                                t.enabled = false
                                t.group = 'Quality'
                                t.description = 'Run Cpd analysis on all classes.'
                            }
                        })
                }

                addProjectEvaluatedListener(project, new CpdProjectEvaluatedListener(allCpdTask))
            }
        })
    }

    private void configureRootProject(Project project) {
        addAllProjectsEvaluatedListener(project, new CpdAllProjectsEvaluatedListener())

        project.tasks.register(AGGREGATE_CPD_TASK_NAME, Cpd,
            new Action<Cpd>() {
                @Override
                void execute(Cpd t) {
                    t.enabled = false
                    t.group = 'Quality'
                    t.description = 'Aggregate all cpd reports.'
                }
            })
    }

    @Named('cpd')
    @DependsOn(['base'])
    private class CpdProjectEvaluatedListener implements ProjectEvaluatedListener {
        private final TaskProvider<Cpd> allCpdTask

        CpdProjectEvaluatedListener(TaskProvider<Cpd> allCpdTask) {
            this.allCpdTask = allCpdTask
        }

        @Override
        void projectEvaluated(Project project) {
            ProjectConfigurationExtension config = resolveConfig(project)
            setEnabled(config.quality.cpd.enabled)

            CpdExtension cpdExt = project.extensions.findByType(CpdExtension)
            cpdExt.toolVersion = config.quality.cpd.toolVersion

            project.tasks.withType(Cpd).configureEach { Cpd task ->
                task.setGroup('Quality')
                applyTo(config, task)
                String sourceSetName = task.name['cpd'.size()..-1].uncapitalize()
                if (sourceSetName in config.quality.cpd.excludedSourceSets) {
                    task.enabled = false
                }
            }

            if (allCpdTask) {
                Set<Cpd> tt = new LinkedHashSet<>()
                project.tasks.withType(Cpd).configureEach { Cpd task ->
                    if (task.name != ALL_CPD_TASK_NAME &&
                        task.enabled) tt << task
                }

                allCpdTask.configure(new Action<Cpd>() {
                    @Override
                    @CompileDynamic
                    void execute(Cpd t) {
                        applyTo(config, t)
                        t.enabled &= tt.size() > 0
                        t.source(*((tt*.source).unique()))
                        t.cpdClasspath = project.files(*((tt*.cpdClasspath).unique()))
                    }
                })
            }
        }
    }

    @Named('cpd')
    @DependsOn(['base'])
    private class CpdAllProjectsEvaluatedListener implements AllProjectsEvaluatedListener {
        @Override
        void allProjectsEvaluated(Project rootProject) {
            configureAggregateCpdTask(rootProject)
        }
    }

    private void configureAggregateCpdTask(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)

        CpdExtension cpdExt = project.extensions.findByType(CpdExtension)
        cpdExt.toolVersion = config.quality.cpd.toolVersion

        Set<Cpd> tt = new LinkedHashSet<>()
        project.tasks.withType(Cpd).configureEach { Cpd task ->
            if (project in config.quality.cpd.aggregate.excludedProjects) return
            if (task.name != ALL_CPD_TASK_NAME &&
                task.name != AGGREGATE_CPD_TASK_NAME &&
                task.enabled) tt << task
        }

        project.childProjects.values().each { p ->
            if (p in config.quality.cpd.aggregate.excludedProjects) return
            p.tasks.withType(Cpd).configureEach { Cpd task ->
                if (task.name != ALL_CPD_TASK_NAME &&
                    task.enabled) tt << task
            }
        }

        project.tasks.named(AGGREGATE_CPD_TASK_NAME, Cpd, new Action<Cpd>() {
            @Override
            @CompileDynamic
            void execute(Cpd t) {
                applyTo(config, t)
                t.enabled = config.quality.cpd.aggregate.enabled && tt.size() > 0
                t.ignoreFailures = false
                t.source(*((tt*.source).unique()))
                t.cpdClasspath = project.files(*((tt*.cpdClasspath).unique()))
            }
        })
    }

    @CompileDynamic
    void applyTo(ProjectConfigurationExtension config, Cpd cpdTask) {
        String sourceSetName = (cpdTask.name - 'cpd').uncapitalize()
        sourceSetName = sourceSetName == 'allCpd' ? project.name : sourceSetName
        sourceSetName = sourceSetName == 'aggregateCpd' ? 'aggregate' : sourceSetName
        cpdTask.enabled = enabled
        cpdTask.minimumTokenCount.set config.quality.cpd.minimumTokenCount
        cpdTask.encoding.set config.quality.cpd.encoding
        cpdTask.language.set config.quality.cpd.language
        cpdTask.ignoreLiterals.set config.quality.cpd.getIgnoreLiterals()
        cpdTask.ignoreIdentifiers.set config.quality.cpd.getIgnoreIdentifiers()
        cpdTask.ignoreAnnotations.set config.quality.cpd.getIgnoreAnnotations()
        cpdTask.ignoreFailures = config.quality.cpd.ignoreFailures
        cpdTask.reports.html.enabled = true
        cpdTask.reports.xml.enabled = true
        cpdTask.reports.html.destination = project.layout.buildDirectory.file("reports/cpd/${sourceSetName}.html").get().asFile
        cpdTask.reports.xml.destination = project.layout.buildDirectory.file("reports/cpd/${sourceSetName}.xml").get().asFile
    }
}
