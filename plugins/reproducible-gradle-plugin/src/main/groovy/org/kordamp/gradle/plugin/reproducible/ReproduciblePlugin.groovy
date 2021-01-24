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
package org.kordamp.gradle.plugin.reproducible

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.kordamp.gradle.annotations.DependsOn
import org.kordamp.gradle.listener.AllProjectsEvaluatedListener
import org.kordamp.gradle.listener.ProjectEvaluatedListener
import org.kordamp.gradle.listener.TaskGraphReadyListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.Reproducible
import org.kordamp.gradle.plugin.reproducible.tasks.CreateBuildInfoTask

import javax.inject.Named

import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addAllProjectsEvaluatedListener
import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addProjectEvaluatedListener
import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addTaskGraphReadyListener
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject
import static org.kordamp.gradle.util.PluginUtils.resolveConfig

/**
 * @author Andres Almiray
 * @since 0.43.0
 */
@CompileStatic
class ReproduciblePlugin extends AbstractKordampPlugin {
    private static final String CREATE_BUILD_INFO_TASK_NAME = 'createBuildInfo'

    Project project

    ReproduciblePlugin() {
        super(Reproducible.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        configureProject(project)
        if (isRootProject(project)) {
            configureRootProject(project)
            project.childProjects.values().each {
                it.pluginManager.apply(ReproduciblePlugin)
            }
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(ReproduciblePlugin)) {
            project.pluginManager.apply(ReproduciblePlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        addProjectEvaluatedListener(project, new ReproducibleProjectEvaluatedListener())

        ProjectConfigurationExtension config = resolveConfig(project)
        config.docs.javadoc.options.noTimestamp = true
        config.docs.groovydoc.options.noTimestamp = true
    }

    private void configureRootProject(Project project) {
        addAllProjectsEvaluatedListener(project, new ReproducibleAllProjectsEvaluatedListener())
        addTaskGraphReadyListener(project, new ReproducibleTaskGraphReadyListener())
    }

    @Named('reproducible')
    @DependsOn(['buildInfo'])
    private class ReproducibleProjectEvaluatedListener implements ProjectEvaluatedListener {
        @Override
        void projectEvaluated(Project project) {
            ProjectConfigurationExtension config = resolveConfig(project)
            setEnabled(config.reproducible.enabled)

            adjustBuildInfo(config)
            if (config.reproducible.enabled) {
                registerTasks(project)
            }
        }
    }

    @Named('reproducible')
    @DependsOn(['publishing'])
    private class ReproducibleAllProjectsEvaluatedListener implements AllProjectsEvaluatedListener {
        @Override
        void allProjectsEvaluated(Project rootProject) {
            updatePublications(rootProject)
        }
    }

    @Named('reproducible')
    @DependsOn(['base'])
    private class ReproducibleTaskGraphReadyListener implements TaskGraphReadyListener {
        @Override
        void taskGraphReady(Project rootProject, TaskExecutionGraph graph) {
            adjustArchives(rootProject)
        }
    }

    private void registerTasks(Project project) {
        Task assembleTask = project.tasks.findByName(org.gradle.api.plugins.BasePlugin.ASSEMBLE_TASK_NAME)

        ProjectConfigurationExtension config = resolveConfig(project)

        TaskProvider<CreateBuildInfoTask> buildInfoTask = project.tasks.register(CREATE_BUILD_INFO_TASK_NAME, CreateBuildInfoTask,
            new Action<CreateBuildInfoTask>() {
                @Override
                void execute(CreateBuildInfoTask t) {
                    t.group = org.gradle.api.plugins.BasePlugin.BUILD_GROUP
                    t.description = 'Generates a .buildinfo file with reproducible build settings'
                    t.outputFile.set(project.layout.buildDirectory
                        .file("buildinfo/${project.name}-${project.version}.buildinfo"))
                    t.additionalArtifacts.from(config.reproducible.additionalArtifacts)
                    t.additionalProperties.putAll(config.reproducible.additionalProperties)
                    t.dependsOn(assembleTask)
                }
            })

        assembleTask.finalizedBy(buildInfoTask)
    }

    private void updatePublications(Project project) {
        updatePublication(project)
        for (Project p : project.childProjects.values()) {
            updatePublications(p)
        }
    }

    private void updatePublication(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)
        if (!config.reproducible.enabled) {
            return
        }

        if (project.tasks.findByName(CREATE_BUILD_INFO_TASK_NAME)) {
            TaskProvider<CreateBuildInfoTask> buildInfoTask = project.tasks.named(CREATE_BUILD_INFO_TASK_NAME, CreateBuildInfoTask)
            if (project.pluginManager.hasPlugin('maven-publish')) {
                PublishingExtension publishing = project.extensions.findByType(PublishingExtension)
                MavenPublication mainPublication = (MavenPublication) publishing.publications.findByName('main')
                if (mainPublication) mainPublication.artifact(buildInfoTask.get().outputFile)
            }
        }
    }

    private void adjustBuildInfo(ProjectConfigurationExtension config) {
        if (!config.reproducible.enabled) return
        config.buildInfo.skipBuildBy = true
        config.buildInfo.skipBuildDate = true
        config.buildInfo.skipBuildTime = true
        config.buildInfo.buildBy = null
        config.buildInfo.buildDate = null
        config.buildInfo.buildTime = null
    }

    private void adjustArchives(Project rootProject) {
        doAjdustArchives(rootProject)
        project.childProjects.values().each { p ->
            doAjdustArchives(p)
        }
    }

    private void doAjdustArchives(Project project) {
        if (!resolveConfig(project).reproducible.enabled) return

        project.tasks.withType(AbstractArchiveTask) { AbstractArchiveTask archive ->
            archive.preserveFileTimestamps = false
            archive.reproducibleFileOrder = true
        }
    }
}
