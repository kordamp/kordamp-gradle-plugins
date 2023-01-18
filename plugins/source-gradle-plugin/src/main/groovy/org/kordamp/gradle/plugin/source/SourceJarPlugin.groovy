/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2023 Andres Almiray.
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
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenArtifact
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.kordamp.gradle.annotations.DependsOn
import org.kordamp.gradle.listener.AllProjectsEvaluatedListener
import org.kordamp.gradle.listener.ProjectEvaluatedListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.Source

import javax.inject.Named

import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addAllProjectsEvaluatedListener
import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addProjectEvaluatedListener
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject
import static org.kordamp.gradle.util.PluginUtils.hasSourceSets
import static org.kordamp.gradle.util.PluginUtils.registerJarVariant
import static org.kordamp.gradle.util.PluginUtils.resolveAllSource
import static org.kordamp.gradle.util.PluginUtils.resolveConfig

/**
 * Configures a {@code sourcesJar} task.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class SourceJarPlugin extends AbstractKordampPlugin {
    static final String SOURCES_JAR_TASK_NAME = 'sourcesJar'
    static final String AGGREGATE_SOURCES_JAR_TASK_NAME = 'aggregateSourcesJar'

    Project project

    SourceJarPlugin() {
        super(Source.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        configureProject(project)
        if (isRootProject(project)) {
            configureRootProject(project)
            project.childProjects.values().each {
                it.pluginManager.apply(SourceJarPlugin)
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
                addProjectEvaluatedListener(project, new SourceProjectEvaluatedListener())
            }
        })
    }

    private void configureRootProject(Project project) {
        addAllProjectsEvaluatedListener(project, new SourceAllProjectsEvaluatedListener())

        project.tasks.register(AGGREGATE_SOURCES_JAR_TASK_NAME, Jar,
            new Action<Jar>() {
                @Override
                void execute(Jar t) {
                    t.group = org.gradle.api.plugins.BasePlugin.BUILD_GROUP
                    t.description = 'An archive of all the source code.'
                    t.archiveClassifier.set('sources')
                    t.enabled = false
                }
            })
    }

    @Named('source')
    @DependsOn(['base'])
    private class SourceProjectEvaluatedListener implements ProjectEvaluatedListener {
        @Override
        void projectEvaluated(Project project) {
            ProjectConfigurationExtension config = resolveConfig(project)
            setEnabled(config.artifacts.source.enabled)

            if (!config.artifacts.source.enabled) {
                return
            }

            TaskProvider<Jar> sourcesJar = createSourceJarTask(project)
            project.tasks.findByName(org.gradle.api.plugins.BasePlugin.ASSEMBLE_TASK_NAME).dependsOn(sourcesJar)
        }
    }

    @Named('source')
    @DependsOn(['base'])
    private class SourceAllProjectsEvaluatedListener implements AllProjectsEvaluatedListener {
        @Override
        void allProjectsEvaluated(Project rootProject) {
            configureAggregateSourceJarTask(rootProject)
        }
    }

    @CompileDynamic
    private TaskProvider<Jar> createSourceJarTask(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)

        TaskProvider<Jar> sourcesJar = project.tasks.register(SOURCES_JAR_TASK_NAME, Jar,
            new Action<Jar>() {
                @Override
                @CompileDynamic
                void execute(Jar t) {
                    t.group = org.gradle.api.plugins.BasePlugin.BUILD_GROUP
                    t.description = 'An archive of the source code.'
                    t.archiveClassifier.set('sources')
                    t.setEnabled(config.artifacts.source.enabled)
                    t.from(config.artifacts.source.empty ? [] : resolveAllSource(project))
                }
            })

        if (config.artifacts.source.enabled && project.pluginManager.hasPlugin('maven-publish')) {
            PublishingExtension publishing = project.extensions.findByType(PublishingExtension)
            MavenPublication mainPublication = (MavenPublication) publishing.publications.findByName('main')
            if (mainPublication) {
                MavenArtifact oldSourceJar = mainPublication.artifacts?.find { it.classifier == 'sources' }
                if (oldSourceJar) mainPublication.artifacts.remove(oldSourceJar)
                mainPublication.artifact(sourcesJar.get())
                project.artifacts { archives(sourcesJar.get()) }
            }

            registerJarVariant('Source', 'sources', sourcesJar, project)
        }

        sourcesJar
    }

    private void configureAggregateSourceJarTask(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)
        setEnabled(config.artifacts.source.enabled || config.artifacts.source.aggregate.enabled)

        project.tasks.named(AGGREGATE_SOURCES_JAR_TASK_NAME, Jar, new Action<Jar>() {
            @Override
            @CompileDynamic
            void execute(Jar t) {
                t.enabled = config.artifacts.source.aggregate.enabled

                if (config.artifacts.source.aggregate.getEmpty()) {
                    t.from([])
                } else {
                    Set<Project> projects = new LinkedHashSet<>()
                    if (!(project in config.artifacts.source.aggregate.excludedProjects) &&
                        config.artifacts.source.enabled &&
                        !(config.artifacts.source.empty) &&
                        hasSourceSets(project)) projects << project

                    project.childProjects.values().each { p ->
                        ProjectConfigurationExtension c = resolveConfig(p)
                        if (!(p in config.artifacts.source.aggregate.excludedProjects) &&
                            c.artifacts.source.enabled &&
                            !(c.artifacts.source.empty) &&
                            hasSourceSets(p)) projects << p
                    }

                    t.from projects.collect { resolveAllSource(it) }.flatten()
                }
            }
        })
    }
}