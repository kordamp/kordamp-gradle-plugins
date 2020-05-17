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
package org.kordamp.gradle.plugin.javadoc

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenArtifact
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

import static org.kordamp.gradle.PluginUtils.registerJarVariant
import static org.kordamp.gradle.PluginUtils.resolveClassesTask
import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * Configures {@code javadoc} and {@code javadocJar} tasks.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class JavadocPlugin extends AbstractKordampPlugin {
    static final String JAVADOC_TASK_NAME = 'javadoc'
    static final String JAVADOC_JAR_TASK_NAME = 'javadocJar'
    static final String AGGREGATE_JAVADOC_TASK_NAME = 'aggregateJavadoc'
    static final String AGGREGATE_JAVADOC_JAR_TASK_NAME = 'aggregateJavadocJar'

    Project project

    void apply(Project project) {
        this.project = project

        configureProject(project)
        if (isRootProject(project)) {
            configureRootProject(project)
            project.childProjects.values().each {
                it.pluginManager.apply(JavadocPlugin)
            }
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(JavadocPlugin)) {
            project.pluginManager.apply(JavadocPlugin)
        }
    }

    private void configureRootProject(Project project) {
        createAggregateTasks(project)

        project.gradle.addBuildListener(new BuildAdapter() {
            @Override
            void projectsEvaluated(Gradle gradle) {
                doConfigureRootProject(project)
            }
        })
    }

    private void doConfigureRootProject(Project project) {
        ProjectConfigurationExtension config = resolveEffectiveConfig(project)
        setEnabled(config.docs.javadoc.aggregate.enabled)

        List<Javadoc> docTasks = []
        project.tasks.withType(Javadoc) { Javadoc t ->
            if (project in config.docs.javadoc.aggregate.excludedProjects) return
            if (t.name != AGGREGATE_JAVADOC_TASK_NAME && t.enabled) docTasks << t
        }
        project.childProjects.values().each { Project p ->
            if (p in config.docs.javadoc.aggregate.excludedProjects) return
            p.tasks.withType(Javadoc) { Javadoc t -> if (t.enabled) docTasks << t }
        }
        docTasks = docTasks.unique()

        if (docTasks) {
            TaskProvider<Javadoc> aggregateJavadoc = project.tasks.named(AGGREGATE_JAVADOC_TASK_NAME, Javadoc,
                new Action<Javadoc>() {
                    @Override
                    void execute(Javadoc t) {
                        t.enabled = config.docs.javadoc.aggregate.enabled
                        if (!config.docs.javadoc.aggregate.fast) t.dependsOn docTasks
                        t.source docTasks.source
                        t.classpath = project.files(docTasks.classpath)
                    }
                })

            project.tasks.named(AGGREGATE_JAVADOC_JAR_TASK_NAME, Jar,
                new Action<Jar>() {
                    @Override
                    void execute(Jar t) {
                        t.enabled = config.docs.javadoc.aggregate.enabled
                        t.from aggregateJavadoc.get().destinationDir
                        t.onlyIf { aggregateJavadoc.get().enabled }
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

        project.pluginManager.withPlugin('java-base', new Action<AppliedPlugin>() {
            @Override
            void execute(AppliedPlugin appliedPlugin) {
                project.tasks.register('checkAutoLinks', CheckAutoLinksTask.class,
                    new Action<CheckAutoLinksTask>() {
                        void execute(CheckAutoLinksTask t) {
                            t.group = 'Documentation'
                            t.description = 'Checks if generated Javadoc auto links are reachable.'
                        }
                    })

                project.afterEvaluate {
                    ProjectConfigurationExtension config = resolveEffectiveConfig(project)
                    setEnabled(config.docs.javadoc.enabled)

                    TaskProvider<Javadoc> javadoc = createJavadocTask(project)
                    TaskProvider<Jar> javadocJar = createJavadocJarTask(project, javadoc)
                    project.tasks.findByName(org.gradle.api.plugins.BasePlugin.ASSEMBLE_TASK_NAME).dependsOn(javadocJar)
                }
            }
        })
    }

    private TaskProvider<Javadoc> createJavadocTask(Project project) {
        project.tasks.named(JAVADOC_TASK_NAME, Javadoc,
            new Action<Javadoc>() {
                @Override
                @CompileDynamic
                void execute(Javadoc t) {
                    ProjectConfigurationExtension config = resolveEffectiveConfig(t.project)
                    t.enabled = config.docs.javadoc.enabled
                    t.dependsOn resolveClassesTask(project)
                    t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                    t.description = 'Generates Javadoc API documentation'
                    t.destinationDir = project.file("${project.buildDir}/docs/javadoc")
                    t.source = project.sourceSets.main.allJava
                    config.docs.javadoc.applyTo(t)
                    t.options.footer = "Copyright &copy; ${config.info.copyrightYear} ${config.info.getAuthors().join(', ')}. All rights reserved."
                    if (JavaVersion.current().isJava8Compatible()) {
                        t.options.addBooleanOption('Xdoclint:none', true)
                        t.options.quiet()
                    }
                }
            })
    }

    private TaskProvider<Jar> createJavadocJarTask(Project project, TaskProvider<Javadoc> javadoc) {
        ProjectConfigurationExtension config = resolveEffectiveConfig(project)

        TaskProvider<Jar> javadocJar = project.tasks.register(JAVADOC_JAR_TASK_NAME, Jar,
            new Action<Jar>() {
                @Override
                void execute(Jar t) {
                    t.enabled = config.docs.javadoc.enabled
                    t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                    t.description = 'An archive of the Javadoc API docs'
                    t.archiveClassifier.set('javadoc')
                    t.dependsOn javadoc
                    t.from javadoc.get().destinationDir
                    t.onlyIf { javadoc.get().enabled }
                }
            })

        if (config.docs.javadoc.enabled && project.pluginManager.hasPlugin('maven-publish')) {
            PublishingExtension publishing = project.extensions.findByType(PublishingExtension)
            MavenPublication mainPublication = (MavenPublication) publishing.publications.findByName('main')
            if (mainPublication) {
                MavenArtifact oldJavadocJar = mainPublication.artifacts?.find { it.classifier == 'javadoc' }
                if (oldJavadocJar) mainPublication.artifacts.remove(oldJavadocJar)
                mainPublication.artifact(javadocJar.get())
            }

            registerJarVariant('Javadoc', 'javadoc', javadocJar, project)
        }

        javadocJar
    }

    private void createAggregateTasks(Project project) {
        TaskProvider<Javadoc> aggregateJavadoc = project.tasks.register(AGGREGATE_JAVADOC_TASK_NAME, Javadoc,
            new Action<Javadoc>() {
                @Override
                @CompileDynamic
                void execute(Javadoc t) {
                    ProjectConfigurationExtension config = resolveEffectiveConfig(t.project)
                    t.enabled = false
                    t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                    t.description = 'Aggregates Javadoc API docs for all projects.'
                    t.destinationDir = project.file("${project.buildDir}/docs/aggregate-javadoc")
                    config.docs.javadoc.applyTo(t)
                    t.options.footer = "Copyright &copy; ${config.info.copyrightYear} ${config.info.getAuthors().join(', ')}. All rights reserved."
                    if (JavaVersion.current().isJava8Compatible()) {
                        t.options.addBooleanOption('Xdoclint:none', true)
                        t.options.quiet()
                    }
                }
            })

        project.tasks.register(AGGREGATE_JAVADOC_JAR_TASK_NAME, Jar,
            new Action<Jar>() {
                @Override
                void execute(Jar t) {
                    t.enabled = false
                    t.dependsOn aggregateJavadoc
                    t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                    t.description = 'An archive of the aggregate Javadoc API docs'
                    t.archiveClassifier.set('javadoc')
                }
            })
    }
}
