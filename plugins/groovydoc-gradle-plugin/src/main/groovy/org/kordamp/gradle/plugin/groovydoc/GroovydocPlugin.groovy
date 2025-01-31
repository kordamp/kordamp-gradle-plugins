/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2025 Andres Almiray.
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
package org.kordamp.gradle.plugin.groovydoc

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenArtifact
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Groovydoc
import org.gradle.api.tasks.javadoc.Javadoc
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
import static org.kordamp.gradle.util.PluginUtils.resolveConfig

/**
 * Configures {@code groovydoc} and {@code groovydocJar} tasks.
 *
 * @author Andres Almiray
 * @since 0.4.0
 */
@CompileStatic
class GroovydocPlugin extends AbstractKordampPlugin {
    static final String GROOVYDOC_TASK_NAME = 'groovydoc'
    static final String GROOVYDOC_JAR_TASK_NAME = 'groovydocJar'
    static final String AGGREGATE_GROOVYDOC_TASK_NAME = 'aggregateGroovydoc'
    static final String AGGREGATE_GROOVYDOC_JAR_TASK_NAME = 'aggregateGroovydocJar'

    Project project

    GroovydocPlugin() {
        super(org.kordamp.gradle.plugin.base.plugins.Groovydoc.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        configureProject(project)
        if (isRootProject(project)) {
            configureRootProject(project)
            project.childProjects.values().each {
                it.pluginManager.apply(GroovydocPlugin)
            }
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(GroovydocPlugin)) {
            project.pluginManager.apply(GroovydocPlugin)
        }
    }

    private void configureRootProject(Project project) {
        createAggregateTasks(project)
        addAllProjectsEvaluatedListener(project, new GroovydocAllProjectsEvaluatedListener())
    }

    private void doConfigureRootProject(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)
        setEnabled(config.docs.groovydoc.enabled || config.docs.groovydoc.aggregate.enabled)

        List<Groovydoc> docTasks = []
        if (!(config.docs.groovydoc.aggregate.getEmpty())) {
            project.tasks.withType(Groovydoc) { Groovydoc t ->
                if (project in config.docs.groovydoc.aggregate.excludedProjects) return
                if (t.name != AGGREGATE_GROOVYDOC_TASK_NAME && t.enabled &&
                    !(config.docs.groovydoc.empty)) docTasks << t
            }
            project.childProjects.values().each { Project p ->
                if (p in config.docs.groovydoc.aggregate.excludedProjects) return
                p.tasks.withType(Groovydoc) { Groovydoc t ->
                    if (t.enabled && !(resolveConfig(p).docs.groovydoc.empty)) {
                        docTasks << t
                    }
                }
            }
            docTasks = docTasks.unique()
        }

        TaskProvider<Groovydoc> aggregateGroovydoc = project.tasks.named(AGGREGATE_GROOVYDOC_TASK_NAME, Groovydoc,
            new Action<Groovydoc>() {
                @Override
                void execute(Groovydoc t) {
                    t.enabled = config.docs.groovydoc.aggregate.enabled
                    if (!config.docs.groovydoc.aggregate.fast) t.dependsOn docTasks
                    t.source docTasks.source
                    t.classpath = project.files(docTasks.classpath)
                    t.groovyClasspath = project.files(docTasks.groovyClasspath)
                    t.footer = "Copyright &copy; ${config.info.copyrightYear} ${config.info.getAuthors().join(', ')}. All rights reserved."
                    config.docs.groovydoc.applyTo(t)
                }
            })

        project.tasks.named(AGGREGATE_GROOVYDOC_JAR_TASK_NAME, Jar,
            new Action<Jar>() {
                @Override
                void execute(Jar t) {
                    t.enabled = config.docs.groovydoc.aggregate.enabled
                    t.from aggregateGroovydoc.get().destinationDir
                    t.archiveClassifier.set config.docs.groovydoc.aggregate.replaceJavadoc ? 'javadoc' : 'groovydoc'
                    t.onlyIf { aggregateGroovydoc.get().enabled }
                }
            })

        updatePublications(project)
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)

        project.pluginManager.withPlugin('groovy-base', new Action<AppliedPlugin>() {
            @Override
            void execute(AppliedPlugin appliedPlugin) {
                // apply first then we can be certain javadoc tasks can be located on time
                JavadocPlugin.applyIfMissing(project)
                addProjectEvaluatedListener(project, new GroovydocProjectEvaluatedListener())
            }
        })
    }

    @Named('groovydoc')
    @DependsOn(['javadoc'])
    private class GroovydocProjectEvaluatedListener implements ProjectEvaluatedListener {
        @Override
        void projectEvaluated(Project project) {
            ProjectConfigurationExtension config = resolveConfig(project)
            setEnabled(config.docs.groovydoc.enabled)

            if (!config.docs.groovydoc.enabled) {
                return
            }

            TaskProvider<Groovydoc> groovydoc = createGroovydocTask(project)
            TaskProvider<Jar> groovydocJar = createGroovydocJarTask(project, groovydoc)
            project.tasks.findByName(org.gradle.api.plugins.BasePlugin.ASSEMBLE_TASK_NAME).dependsOn(groovydocJar)
        }
    }

    @Named('groovydoc')
    @DependsOn(['javadoc'])
    private class GroovydocAllProjectsEvaluatedListener implements AllProjectsEvaluatedListener {
        @Override
        void allProjectsEvaluated(Project rootProject) {
            doConfigureRootProject(rootProject)
        }
    }

    private TaskProvider<Groovydoc> createGroovydocTask(Project project) {
        project.tasks.named(GROOVYDOC_TASK_NAME, Groovydoc,
            new Action<Groovydoc>() {
                @Override
                @CompileDynamic
                void execute(Groovydoc t) {
                    ProjectConfigurationExtension config = resolveConfig(project)
                    t.dependsOn resolveClassesTask(project)
                    t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                    t.description = 'Generates Groovydoc API documentation'
                    t.source = config.docs.groovydoc.empty ? [] : resolveAllSource(project)
                    t.destinationDir = project.file("${project.buildDir}/docs/groovydoc")
                    t.classpath = project.tasks.named('javadoc', Javadoc).get().classpath
                    t.enabled = config.docs.groovydoc.enabled
                    t.footer = "Copyright &copy; ${config.info.copyrightYear} ${config.info.getAuthors().join(', ')}. All rights reserved."
                    config.docs.groovydoc.applyTo(t)
                }
            })
    }

    private TaskProvider<Jar> createGroovydocJarTask(Project project, TaskProvider<Groovydoc> groovydoc) {
        ProjectConfigurationExtension config = resolveConfig(project)

        project.tasks.register(GROOVYDOC_JAR_TASK_NAME, Jar,
            new Action<Jar>() {
                @Override
                void execute(Jar t) {
                    t.enabled = config.docs.groovydoc.enabled
                    t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                    t.description = 'An archive of the Groovydoc API docs'
                    t.archiveClassifier.set(config.docs.groovydoc.replaceJavadoc ? 'javadoc' : 'groovydoc')
                    t.dependsOn groovydoc
                    t.from groovydoc.get().destinationDir
                    t.onlyIf { groovydoc.get().enabled }
                }
            })
    }

    private void updatePublications(Project project) {
        updatePublication(project)
        for (Project p : project.childProjects.values()) {
            updatePublications(p)
        }
    }

    @CompileDynamic
    private void updatePublication(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)
        if (!config.docs.groovydoc.enabled) {
            return
        }

        if (project.tasks.findByName(GROOVYDOC_JAR_TASK_NAME)) {
            TaskProvider<Jar> groovydocJar = project.tasks.named(GROOVYDOC_JAR_TASK_NAME, Jar)
            if (config.docs.groovydoc.enabled && project.pluginManager.hasPlugin('maven-publish')) {
                PublishingExtension publishing = project.extensions.findByType(PublishingExtension)
                MavenPublication mainPublication = (MavenPublication) publishing.publications.findByName('main')
                if (mainPublication) {
                    if (config.docs.groovydoc.replaceJavadoc) {
                        MavenArtifact javadocJar = mainPublication.artifacts?.find { it.classifier == 'javadoc' }
                        if (javadocJar) mainPublication.artifacts.remove(javadocJar)

                        project.tasks.findByName(JavadocPlugin.JAVADOC_TASK_NAME)?.enabled = false
                        project.tasks.findByName(JavadocPlugin.JAVADOC_JAR_TASK_NAME)?.enabled = false
                    }
                    mainPublication.artifact(groovydocJar.get())
                    project.artifacts { archives(groovydocJar.get()) }
                }

                registerJarVariant('Groovydoc', config.docs.groovydoc.replaceJavadoc ? 'javadoc' : 'groovydoc', groovydocJar, project)
            }
        }
    }

    private void createAggregateTasks(Project project) {
        TaskProvider<Groovydoc> aggregateGroovydoc = project.tasks.register(AGGREGATE_GROOVYDOC_TASK_NAME, Groovydoc,
            new Action<Groovydoc>() {
                @Override
                @CompileDynamic
                void execute(Groovydoc t) {
                    t.enabled = false
                    t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                    t.description = 'Aggregates Groovydoc API docs for all projects.'
                    t.destinationDir = project.file("${project.buildDir}/docs/aggregate-groovydoc")
                }
            })

        project.tasks.register(AGGREGATE_GROOVYDOC_JAR_TASK_NAME, Jar,
            new Action<Jar>() {
                @Override
                void execute(Jar t) {
                    t.enabled = false
                    t.dependsOn aggregateGroovydoc
                    t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                    t.description = 'An archive of the aggregate Groovydoc API docs'
                    t.archiveClassifier.set('groovydoc')
                }
            })
    }
}
