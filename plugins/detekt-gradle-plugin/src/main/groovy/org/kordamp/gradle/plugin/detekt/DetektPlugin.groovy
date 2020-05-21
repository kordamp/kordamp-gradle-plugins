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
package org.kordamp.gradle.plugin.detekt

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.DetektGenerateConfigTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
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
 * @since 0.31.0
 */
@CompileStatic
class DetektPlugin extends AbstractKordampPlugin {
    static final String ALL_DETEKT_TASK_NAME = 'allDetekt'
    static final String AGGREGATE_DETEKT_TASK_NAME = 'aggregateDetekt'

    Project project

    DetektPlugin() {
        super(org.kordamp.gradle.plugin.base.plugins.Detekt.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        configureProject(project)
        if (isRootProject(project)) {
            configureRootProject(project)
            project.childProjects.values().each {
                it.pluginManager.apply(DetektPlugin)
            }
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(DetektPlugin)) {
            project.pluginManager.apply(DetektPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        project.pluginManager.apply(io.gitlab.arturbosch.detekt.DetektPlugin)

        project.pluginManager.withPlugin('kotlin-base', new Action<AppliedPlugin>() {
            @Override
            void execute(AppliedPlugin appliedPlugin) {
                TaskProvider<Detekt> allDetektTask = null
                if (project.childProjects.isEmpty()) {
                    allDetektTask = project.tasks.register(ALL_DETEKT_TASK_NAME, Detekt,
                        new Action<Detekt>() {
                            @Override
                            void execute(Detekt t) {
                                t.enabled = false
                                t.group = 'Quality'
                                t.description = 'Run Detekt analysis on all classes.'
                            }
                        })
                }

                addProjectEvaluatedListener(project, new DetektProjectEvaluatedListener(allDetektTask))
            }
        })
    }

    private void configureRootProject(Project project) {
        addAllProjectsEvaluatedListener(project, new DetektAllProjectsEvaluatedListener())

        project.tasks.register(AGGREGATE_DETEKT_TASK_NAME, Detekt,
            new Action<Detekt>() {
                @Override
                void execute(Detekt t) {
                    t.enabled = false
                    t.group = 'Quality'
                    t.description = 'Aggregate all detekt reports.'
                }
            })
    }

    @Named('checkstyle')
    @DependsOn(['base'])
    private class DetektProjectEvaluatedListener implements ProjectEvaluatedListener {
        private final TaskProvider<Detekt> allDetektTask

        DetektProjectEvaluatedListener(TaskProvider<Detekt> allDetektTask) {
            this.allDetektTask = allDetektTask
        }

        @Override
        void projectEvaluated(Project project) {
            ProjectConfigurationExtension config = resolveConfig(project)
            setEnabled(config.quality.detekt.enabled)

            DetektExtension detektExt = project.extensions.findByType(DetektExtension)
            detektExt.toolVersion = config.quality.detekt.toolVersion

            project.tasks.withType(Detekt) { Detekt task ->
                task.setGroup('Quality')
                DetektPlugin.applyTo(config, task)
                String sourceSetName = task.name['detekt'.size()..-1].uncapitalize()
                if (sourceSetName in config.quality.detekt.excludedSourceSets) {
                    task.enabled = false
                }
            }

            project.tasks.withType(DetektGenerateConfigTask) { DetektGenerateConfigTask t ->
                t.setGroup('Quality')
            }

            project.tasks.withType(DetektCreateBaselineTask) { DetektCreateBaselineTask t ->
                t.setGroup('Quality')
            }

            if (allDetektTask) {
                Set<Detekt> tt = new LinkedHashSet<>()
                project.tasks.withType(Detekt) { Detekt task ->
                    if (task.name != ALL_DETEKT_TASK_NAME &&
                        task.enabled) tt << task
                }

                allDetektTask.configure(new Action<Detekt>() {
                    @Override
                    @CompileDynamic
                    void execute(Detekt t) {
                        applyTo(config, t)
                        t.enabled &= tt.size() > 0
                        t.source(*((tt*.source).unique()))
                        t.classpath.from project.files(*((tt*.classpath).unique()))
                        t.detektClasspath.from project.files(*((tt*.detektClasspath).unique()))
                        t.pluginClasspath.from project.files(*((tt*.pluginClasspath).unique()))
                    }
                })
            }
        }
    }

    @Named('checkstyle')
    @DependsOn(['base'])
    private class DetektAllProjectsEvaluatedListener implements AllProjectsEvaluatedListener {
        @Override
        void allProjectsEvaluated(Project rootProject) {
            configureAggregateDetektTask(rootProject)
        }
    }

    private void configureAggregateDetektTask(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)

        DetektExtension detektExt = project.extensions.findByType(DetektExtension)
        detektExt.toolVersion = config.quality.detekt.toolVersion

        Set<Detekt> tt = new LinkedHashSet<>()
        project.tasks.withType(Detekt) { Detekt task ->
            if (project in config.quality.detekt.aggregate.excludedProjects) return
            if (task.name != ALL_DETEKT_TASK_NAME &&
                task.name != AGGREGATE_DETEKT_TASK_NAME &&
                task.enabled) tt << task
        }

        project.childProjects.values().each { p ->
            if (p in config.quality.detekt.aggregate.excludedProjects) return
            p.tasks.withType(Detekt) { Detekt task ->
                if (task.name != ALL_DETEKT_TASK_NAME &&
                    task.enabled) tt << task
            }
        }

        project.tasks.named(AGGREGATE_DETEKT_TASK_NAME, Detekt, new Action<Detekt>() {
            @Override
            @CompileDynamic
            void execute(Detekt t) {
                applyTo(config, t)
                t.enabled = config.quality.detekt.aggregate.enabled &&
                    config.quality.detekt.configFile.exists() &&
                    tt.size() > 0
                t.ignoreFailures = false
                t.source(*((tt*.source).unique()))
                t.classpath.from project.files(*((tt*.classpath).unique()))
                t.detektClasspath.from project.files(*((tt*.detektClasspath).unique()))
                t.pluginClasspath.from project.files(*((tt*.pluginClasspath).unique()))
            }
        })
    }

    @CompileDynamic
    private static void applyTo(ProjectConfigurationExtension config, Detekt detektTask) {
        String sourceSetName = (detektTask.name - 'detekt').uncapitalize()
        sourceSetName = sourceSetName == 'allDetekt' ? config.project.name : sourceSetName
        sourceSetName = sourceSetName == 'aggregateDetekt' ? 'aggregate' : sourceSetName
        File specificConfigFile = resolveConfigFile(config.quality.detekt.configFile, config.quality.detekt.configFileSet, sourceSetName)
        detektTask.setEnabled(config.quality.detekt.enabled && specificConfigFile.exists())
        detektTask.config.setFrom(specificConfigFile)
        detektTask.baseline.set(config.quality.detekt.baselineFile)
        detektTask.parallel = config.quality.detekt.parallel
        detektTask.failFast = config.quality.detekt.failFast
        detektTask.buildUponDefaultConfig = config.quality.detekt.buildUponDefaultConfig
        detektTask.disableDefaultRuleSets = config.quality.detekt.disableDefaultRuleSets
        detektTask.ignoreFailures = config.quality.detekt.ignoreFailures
        detektTask.reports.html.enabled = true
        detektTask.reports.xml.enabled = true
        detektTask.reports.html.destination = config.project.layout.buildDirectory.file("reports/detekt/${sourceSetName}.html").get().asFile
        detektTask.reports.xml.destination = config.project.layout.buildDirectory.file("reports/detekt/${sourceSetName}.xml").get().asFile
    }

    private File resolveConfigFile(File baseFile, boolean fileSet, String sourceSetName) {
        if (sourceSetName == project.name || sourceSetName == 'aggregate') {
            return baseFile
        }

        if (fileSet) {
            if (baseFile.name.endsWith('.yml')) {
                String filePath = baseFile.absolutePath[0..-5]
                File configFile = new File("${filePath}-${sourceSetName}.yml")
                if (configFile.exists()) {
                    return configFile
                }
            }
            return baseFile
        }

        for (String path : [
            "config/detekt/${project.name}-${sourceSetName}.yml",
            "config/detekt/${project.name}.yml",
            "config/detekt/detekt-${sourceSetName}.yml",
            "config/detekt/detekt.yml"]) {
            File file = project.rootProject.file(path)
            if (file.exists()) {
                return file
            }
        }

        baseFile
    }
}
