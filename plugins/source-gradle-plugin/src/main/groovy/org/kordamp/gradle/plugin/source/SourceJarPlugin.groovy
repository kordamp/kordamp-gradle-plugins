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
package org.kordamp.gradle.plugin.source

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenArtifact
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.kordamp.gradle.PluginUtils
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * Configures a {@code sourceJar} task.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class SourceJarPlugin extends AbstractKordampPlugin {
    static final String SOURCE_JAR_TASK_NAME = 'sourceJar'
    static final String AGGREGATE_SOURCE_JAR_TASK_NAME = 'aggregateSourceJar'

    Project project

    SourceJarPlugin() {
        super(org.kordamp.gradle.plugin.base.plugins.Source.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        configureProject(project)
        if (isRootProject(project)) {
            configureRootProject(project)
            project.childProjects.values().each {
                configureProject(it)
            }
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(SourceJarPlugin)) {
            project.pluginManager.apply(SourceJarPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)

        project.pluginManager.withPlugin('java-base', new Action<AppliedPlugin>() {
            @Override
            void execute(AppliedPlugin appliedPlugin) {
                project.afterEvaluate {
                    ProjectConfigurationExtension config = resolveEffectiveConfig(project)
                    setEnabled(config.source.enabled)

                    TaskProvider<Jar> sourceJar = createSourceJarTask(project)
                    project.tasks.findByName(org.gradle.api.plugins.BasePlugin.ASSEMBLE_TASK_NAME).dependsOn(sourceJar)
                }
            }
        })
    }

    private void configureRootProject(Project project) {
        TaskProvider<Jar> sourceJarTask = project.tasks.register(AGGREGATE_SOURCE_JAR_TASK_NAME, Jar,
            new Action<Jar>() {
                @Override
                void execute(Jar t) {
                    t.group = org.gradle.api.plugins.BasePlugin.BUILD_GROUP
                    t.description = 'An archive of all the source code.'
                    t.archiveClassifier.set('sources')
                    t.enabled = false
                }
            })

        project.gradle.addBuildListener(new BuildAdapter() {
            @Override
            void projectsEvaluated(Gradle gradle) {
                configureAggregateSourceJarTask(project, sourceJarTask)
            }
        })
    }

    private TaskProvider<Jar> createSourceJarTask(Project project) {
        ProjectConfigurationExtension config = resolveEffectiveConfig(project)
        TaskProvider<Jar> sourceJar = project.tasks.register(SOURCE_JAR_TASK_NAME, Jar,
            new Action<Jar>() {
                @Override
                @CompileDynamic
                void execute(Jar t) {
                    t.group = org.gradle.api.plugins.BasePlugin.BUILD_GROUP
                    t.description = 'An archive of the source code.'
                    t.archiveClassifier.set('sources')
                    t.dependsOn project.tasks.named('classes')
                    t.setEnabled(resolveEffectiveConfig(t.project).source.enabled)
                    t.from PluginUtils.resolveSourceSets(project).main.allSource
                }
            })

        if (config.source.enabled && project.pluginManager.hasPlugin('maven-publish')) {
            PublishingExtension publishing = project.extensions.findByType(PublishingExtension)
            MavenPublication mainPublication = (MavenPublication) publishing.publications.findByName('main')
            if (mainPublication) {
                MavenArtifact oldSourceJar = mainPublication.artifacts?.find { it.classifier == 'sources' }
                if (oldSourceJar) mainPublication.artifacts.remove(oldSourceJar)
                mainPublication.artifact(sourceJar.get())
            }
        }

        sourceJar
    }

    private void configureAggregateSourceJarTask(Project project,
                                                 TaskProvider<Jar> aggregateSourceJarTask) {
        ProjectConfigurationExtension config = resolveEffectiveConfig(project)

        Set<Project> projects = new LinkedHashSet<>()
        if (!(project in config.source.aggregate.excludedProjects()) && config.source.enabled) {
            projects << project
        }

        project.childProjects.values().each { p ->
            if (p in config.source.aggregate.excludedProjects() || !config.source.enabled) return
            projects << p
        }

        aggregateSourceJarTask.configure(new Action<Jar>() {
            @Override
            @CompileDynamic
            void execute(Jar t) {
                t.from PluginUtils.resolveSourceSets(projects).main.allSource
                t.enabled = config.source.aggregate.enabled
            }
        })
    }
}