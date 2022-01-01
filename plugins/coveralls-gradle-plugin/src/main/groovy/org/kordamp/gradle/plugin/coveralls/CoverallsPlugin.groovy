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
package org.kordamp.gradle.plugin.coveralls

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kt3k.gradle.plugin.CoverallsPluginExtension

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * @author Andres Almiray
 * @since 0.31.0
 */
@CompileStatic
class CoverallsPlugin extends AbstractKordampPlugin {
    Project project

    void apply(Project project) {
        this.project = project

        if (isRootProject(project)) {
            configureRootProject(project)
        } else {
            configureRootProject(project.rootProject)
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(CoverallsPlugin)) {
            project.pluginManager.apply(CoverallsPlugin)
        }
    }

    private void configureRootProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        project.pluginManager.apply(org.kt3k.gradle.plugin.CoverallsPlugin)

        project.gradle.taskGraph.whenReady(new Action<TaskExecutionGraph>() {
            @Override
            void execute(TaskExecutionGraph graph) {
                configureCoveralls(project, graph)
            }
        })

        project.afterEvaluate {
            ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
            setEnabled(effectiveConfig.coverage.coveralls.enabled)

            project.tasks.named('coveralls').configure(new Action<Task>() {
                @Override
                void execute(Task t) {
                    t.group = 'Coverage'
                    t.description = 'Uploads the aggregated coverage report to Coveralls'
                    t.enabled = effectiveConfig.coverage.coveralls.enabled
                    t.dependsOn(project.tasks.named('aggregateJacocoReport'))
                    t.onlyIf { System.getenv().CI || System.getenv().GITHUB_ACTIONS }
                }
            })
        }
    }

    @CompileDynamic
    private void configureCoveralls(Project project, TaskExecutionGraph graph) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)

        Set<File> files = []
        project.tasks.withType(JacocoReport) { JacocoReport r ->
            files.addAll(r.allSourceDirs.files)
        }
        project.childProjects.values().each { p ->
            p.tasks.withType(JacocoReport) { JacocoReport r ->
                files.addAll(r.allSourceDirs.files)
            }
        }

        CoverallsPluginExtension coveralls = project.extensions.findByType(CoverallsPluginExtension)
        coveralls.jacocoReportPath = effectiveConfig.coverage.jacoco.aggregateReportXmlFile
        coveralls.sourceDirs.addAll(project.files(files))
    }
}
