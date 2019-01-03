/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
package org.kordamp.gradle.plugin.source

import org.gradle.BuildAdapter
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.bundling.Jar
import org.kordamp.gradle.PluginUtils
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.Source

import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * Configures a {@code sourceJar} task.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
class SourceJarPlugin extends AbstractKordampPlugin {
    static final String SOURCE_JAR_TASK_NAME = 'sourceJar'
    static final String AGGREGATE_SOURCE_JAR_TASK_NAME = 'aggregateSourceJar'

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
        if (!project.plugins.findPlugin(SourceJarPlugin)) {
            project.plugins.apply(SourceJarPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)

        project.afterEvaluate {
            ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
            setEnabled(effectiveConfig.source.enabled)

            if (!enabled) {
                return
            }

            project.plugins.withType(JavaBasePlugin) {
                Jar sourceTask = createSourceJarTask(project)
                effectiveConfig.source.sourceTasks() << sourceTask
                effectiveConfig.source.projects() << project
            }
        }

        if (isRootProject(project) && !project.childProjects.isEmpty()) {
            Jar sourceJarTask = project.tasks.create(AGGREGATE_SOURCE_JAR_TASK_NAME, Jar) {
                group org.gradle.api.plugins.BasePlugin.BUILD_GROUP
                description 'An archive of all the source code'
                classifier 'sources'
                enabled = false
            }

            project.gradle.addBuildListener(new BuildAdapter() {
                @Override
                void projectsEvaluated(Gradle gradle) {
                    configureAggregateSourceJarTask(project, sourceJarTask)
                }
            })
        }
    }

    private Jar createSourceJarTask(Project project) {
        String taskName = SOURCE_JAR_TASK_NAME

        Task sourceJarTask = project.tasks.findByName(taskName)
        Task classesTask = project.tasks.findByName('classes')

        if (classesTask && !sourceJarTask) {
            sourceJarTask = project.tasks.create(SOURCE_JAR_TASK_NAME, Jar) {
                dependsOn classesTask
                group org.gradle.api.plugins.BasePlugin.BUILD_GROUP
                description 'An archive of the source code'
                classifier 'sources'
                from PluginUtils.resolveSourceSets(project).main.allSource
            }
        }

        project.tasks.findByName(org.gradle.api.plugins.BasePlugin.ASSEMBLE_TASK_NAME).dependsOn(sourceJarTask)

        sourceJarTask
    }

    private void configureAggregateSourceJarTask(Project project, Jar sourceJarTask) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)

        Set<Project> projects = new LinkedHashSet<>(effectiveConfig.source.projects())
        Set<Jar> sourceTasks = new LinkedHashSet<>(effectiveConfig.source.sourceTasks())

        project.childProjects.values()*.effectiveConfig.source.each { Source e ->
            if (!e.enabled || effectiveConfig.source.excludedProjects().intersect(e.projects())) return
            projects.addAll(e.projects())
            sourceTasks.addAll(e.sourceTasks())
        }

        sourceJarTask.configure {
            dependsOn sourceTasks
            from PluginUtils.resolveSourceSets(projects).main.allSource
            enabled = true
        }
    }
}