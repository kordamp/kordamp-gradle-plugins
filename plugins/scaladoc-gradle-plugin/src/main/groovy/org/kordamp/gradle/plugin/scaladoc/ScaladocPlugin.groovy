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
package org.kordamp.gradle.plugin.scaladoc

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenArtifact
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.scala.ScalaDoc
import org.kordamp.gradle.annotations.DependsOn
import org.kordamp.gradle.listener.AllProjectsEvaluatedListener
import org.kordamp.gradle.listener.ProjectEvaluatedListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.javadoc.JavadocPlugin

import javax.inject.Named

import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addAllProjectsEvaluatedListener
import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addProjectEvaluatedListener
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject
import static org.kordamp.gradle.util.PluginUtils.registerJarVariant
import static org.kordamp.gradle.util.PluginUtils.resolveAllSource
import static org.kordamp.gradle.util.PluginUtils.resolveClassesTask
import static org.kordamp.gradle.util.PluginUtils.resolveEffectiveConfig

/**
 * Configures {@code scaladoc} and {@code scaladocJar} tasks.
 *
 * @author Andres Almiray
 * @since 0.15.0
 */
@CompileStatic
class ScaladocPlugin extends AbstractKordampPlugin {
    static final String SCALADOC_TASK_NAME = 'scaladoc'
    static final String SCALADOC_JAR_TASK_NAME = 'scaladocJar'
    static final String AGGREGATE_SCALADOC_TASK_NAME = 'aggregateScaladoc'
    static final String AGGREGATE_SCALADOC_JAR_TASK_NAME = 'aggregateScaladocJar'

    Project project

    ScaladocPlugin() {
        super(org.kordamp.gradle.plugin.base.plugins.Scaladoc.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        configureProject(project)
        if (isRootProject(project)) {
            configureRootProject(project)
            project.childProjects.values().each {
                it.pluginManager.apply(ScaladocPlugin)
            }
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(ScaladocPlugin)) {
            project.pluginManager.apply(ScaladocPlugin)
        }
    }

    private void configureRootProject(Project project) {
        createAggregateTasks(project)
        addAllProjectsEvaluatedListener(project, new ScaladocAllProjectsEvaluatedListener())
    }

    private void doConfigureRootProject(Project project) {
        ProjectConfigurationExtension config = resolveEffectiveConfig(project)
        setEnabled(config.docs.scaladoc.aggregate.enabled)

        List<ScalaDoc> docTasks = []
        project.tasks.withType(ScalaDoc) { ScalaDoc t ->
            if (project in config.docs.scaladoc.aggregate.excludedProjects) return
            if (t.name != AGGREGATE_SCALADOC_TASK_NAME && t.enabled) docTasks << t
        }
        project.childProjects.values().each { Project p ->
            if (p in config.docs.scaladoc.aggregate.excludedProjects) return
            p.tasks.withType(ScalaDoc) { ScalaDoc t -> if (t.enabled) docTasks << t }
        }
        docTasks = docTasks.unique()

        if (docTasks) {
            TaskProvider<ScalaDoc> aggregateScaladoc = project.tasks.named(AGGREGATE_SCALADOC_TASK_NAME, ScalaDoc,
                new Action<ScalaDoc>() {
                    @Override
                    void execute(ScalaDoc t) {
                        t.enabled = config.docs.scaladoc.aggregate.enabled
                        t.dependsOn docTasks
                        if (!config.docs.scaladoc.aggregate.fast) t.dependsOn docTasks
                        t.source docTasks.source
                        t.classpath = project.files(docTasks.classpath)
                        config.docs.scaladoc.applyTo(t)
                    }
                })

            project.tasks.named(AGGREGATE_SCALADOC_JAR_TASK_NAME, Jar,
                new Action<Jar>() {
                    @Override
                    void execute(Jar t) {
                        t.enabled = config.docs.scaladoc.aggregate.enabled
                        t.from aggregateScaladoc.get().destinationDir
                        t.archiveClassifier.set config.docs.scaladoc.aggregate.replaceJavadoc ? 'javadoc' : 'scaladoc'
                        t.onlyIf { aggregateScaladoc.get().enabled }
                    }
                })
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)

        project.pluginManager.withPlugin('scala-base') {
            addProjectEvaluatedListener(project, new ScaladocProjectEvaluatedListener())
        }
    }

    @Named('scaladoc')
    @DependsOn(['javadoc'])
    private class ScaladocProjectEvaluatedListener implements ProjectEvaluatedListener {
        @Override
        void projectEvaluated(Project project) {
            ProjectConfigurationExtension config = resolveEffectiveConfig(project)
            setEnabled(config.docs.scaladoc.enabled)

            TaskProvider<ScalaDoc> scaladoc = createScaladocTask(project)
            TaskProvider<Jar> scaladocJar = createScaladocJarTask(project, scaladoc)
            project.tasks.findByName(org.gradle.api.plugins.BasePlugin.ASSEMBLE_TASK_NAME).dependsOn(scaladocJar)
        }
    }

    @Named('scaladoc')
    @DependsOn(['javadoc'])
    private class ScaladocAllProjectsEvaluatedListener implements AllProjectsEvaluatedListener {
        @Override
        void allProjectsEvaluated(Project rootProject) {
            doConfigureRootProject(rootProject)
        }
    }

    private TaskProvider<ScalaDoc> createScaladocTask(Project project) {
        project.tasks.named(SCALADOC_TASK_NAME, ScalaDoc,
            new Action<ScalaDoc>() {
                @Override
                @CompileDynamic
                void execute(ScalaDoc t) {
                    ProjectConfigurationExtension config = resolveEffectiveConfig(project)
                    t.enabled = config.docs.scaladoc.enabled
                    t.dependsOn resolveClassesTask(project)
                    t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                    t.description = 'Generates Scaladoc API documentation'
                    t.source = resolveAllSource(project)
                    t.destinationDir = project.file("${project.buildDir}/docs/scaladoc")
                    config.docs.scaladoc.applyTo(t)
                }
            })
    }

    private TaskProvider<Jar> createScaladocJarTask(Project project, TaskProvider<ScalaDoc> scaladoc) {
        ProjectConfigurationExtension config = resolveEffectiveConfig(project)

        TaskProvider<Jar> scaladocJarTask = project.tasks.register(SCALADOC_JAR_TASK_NAME, Jar,
            new Action<Jar>() {
                @Override
                void execute(Jar t) {
                    t.enabled = config.docs.scaladoc.enabled
                    t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                    t.description = 'An archive of the Scaladoc API docs'
                    t.archiveClassifier.set(config.docs.groovydoc.replaceJavadoc ? 'javadoc' : 'scaladoc')
                    t.dependsOn scaladoc
                    t.from scaladoc.get().destinationDir
                    t.onlyIf { scaladoc.get().enabled }
                }
            })

        if (config.docs.scaladoc.enabled && project.pluginManager.hasPlugin('maven-publish')) {
            PublishingExtension publishing = project.extensions.findByType(PublishingExtension)
            MavenPublication mainPublication = (MavenPublication) publishing.publications.findByName('main')
            if (mainPublication) {
                if (config.docs.scaladoc.replaceJavadoc) {
                    MavenArtifact javadocJar = mainPublication.artifacts?.find { it.classifier == 'javadoc' }
                    if (javadocJar) mainPublication.artifacts.remove(javadocJar)

                    project.tasks.findByName(JavadocPlugin.JAVADOC_TASK_NAME)?.enabled = false
                    project.tasks.findByName(JavadocPlugin.JAVADOC_JAR_TASK_NAME)?.enabled = false
                }
                mainPublication.artifact(scaladocJarTask.get())
            }

            registerJarVariant('Scaladoc', config.docs.scaladoc.replaceJavadoc ? 'javadoc' : 'scaladoc', scaladocJarTask, project)
        }

        scaladocJarTask
    }

    private void createAggregateTasks(Project project) {
        TaskProvider<ScalaDoc> aggregateScaladoc = project.tasks.register(AGGREGATE_SCALADOC_TASK_NAME, ScalaDoc,
            new Action<ScalaDoc>() {
                @Override
                void execute(ScalaDoc t) {
                    t.enabled = false
                    t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                    t.description = 'Aggregates Scaladoc API docs for all projects.'
                    t.destinationDir = project.file("${project.buildDir}/docs/aggregate-scaladoc")
                }
            })

        project.tasks.register(AGGREGATE_SCALADOC_JAR_TASK_NAME, Jar,
            new Action<Jar>() {
                @Override
                void execute(Jar t) {
                    t.dependsOn aggregateScaladoc
                    t.enabled = false
                    t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                    t.description = 'An archive of the aggregate Scaladoc API docs'
                    t.archiveClassifier.set('scaladoc')
                }
            })
    }
}
