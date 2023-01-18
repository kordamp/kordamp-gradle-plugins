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
package org.kordamp.gradle.plugin.javadoc

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenArtifact
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
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
import static org.kordamp.gradle.util.PluginUtils.isAndroidProject
import static org.kordamp.gradle.util.PluginUtils.registerJarVariant
import static org.kordamp.gradle.util.PluginUtils.resolveAllSource
import static org.kordamp.gradle.util.PluginUtils.resolveClassesTask
import static org.kordamp.gradle.util.PluginUtils.resolveConfig
import static org.kordamp.gradle.util.PluginUtils.resolveMainSourceDirs

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

    JavadocPlugin() {
        super(org.kordamp.gradle.plugin.base.plugins.Javadoc.PLUGIN_ID)
    }

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
        addAllProjectsEvaluatedListener(project, new JavadocAllProjectsEvaluatedListener())
    }

    private void doConfigureRootProject(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)
        setEnabled(config.docs.javadoc.enabled || config.docs.javadoc.aggregate.enabled)

        List<Project> projects = []
        List<Javadoc> docTasks = []
        if (!(config.docs.javadoc.aggregate.getEmpty())) {
            project.tasks.withType(Javadoc) { Javadoc t ->
                if (project in config.docs.javadoc.aggregate.excludedProjects) return
                if (t.name != AGGREGATE_JAVADOC_TASK_NAME && t.enabled &&
                    !(config.docs.javadoc.empty)) {
                    docTasks << t
                    projects << project
                }
            }
            project.childProjects.values().each { Project p ->
                if (p in config.docs.javadoc.aggregate.excludedProjects) return
                p.tasks.withType(Javadoc) { Javadoc t ->
                    if (t.enabled && !(resolveConfig(p).docs.javadoc.empty)) {
                        docTasks << t
                        projects << p
                    }
                }
            }
            docTasks = docTasks.unique()
        }

        TaskProvider<Javadoc> aggregateJavadoc = project.tasks.named(AGGREGATE_JAVADOC_TASK_NAME, Javadoc,
            new Action<Javadoc>() {
                @Override
                @CompileDynamic
                void execute(Javadoc t) {
                    t.enabled = config.docs.javadoc.aggregate.enabled
                    if (!config.docs.javadoc.aggregate.fast) t.dependsOn docTasks
                    t.source docTasks.source
                    t.classpath = project.files(docTasks.classpath)
                    t.options.footer = "Copyright &copy; ${config.info.copyrightYear} ${config.info.getAuthors().join(', ')}. All rights reserved."
                    config.docs.javadoc.applyTo(t)
                }
            })
        createCopyDocFilesTask(project, aggregateJavadoc, 'aggregateCopyDocFiles', resolveMainSourceDirs(projects))

        project.tasks.named(AGGREGATE_JAVADOC_JAR_TASK_NAME, Jar,
            new Action<Jar>() {
                @Override
                void execute(Jar t) {
                    t.enabled = config.docs.javadoc.aggregate.enabled
                    t.from aggregateJavadoc.get().destinationDir
                    t.onlyIf { aggregateJavadoc.get().enabled }
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

                addProjectEvaluatedListener(project, new JavadocProjectEvaluatedListener())
            }
        })
    }

    @Named('javadoc')
    @DependsOn(['base'])
    private class JavadocProjectEvaluatedListener implements ProjectEvaluatedListener {
        @Override
        void projectEvaluated(Project project) {
            ProjectConfigurationExtension config = resolveConfig(project)
            setEnabled(config.docs.javadoc.enabled)

            if (!config.docs.javadoc.enabled) {
                return
            }

            TaskProvider<Javadoc> javadoc = createJavadocTask(project)
            createCopyDocFilesTask(project, javadoc, 'copyDocFiles', resolveMainSourceDirs(project))
            TaskProvider<Jar> javadocJar = createJavadocJarTask(project, javadoc)
            project.tasks.findByName(org.gradle.api.plugins.BasePlugin.ASSEMBLE_TASK_NAME).dependsOn(javadocJar)
        }
    }

    @Named('javadoc')
    @DependsOn(['publishing'])
    private class JavadocAllProjectsEvaluatedListener implements AllProjectsEvaluatedListener {
        @Override
        void allProjectsEvaluated(Project rootProject) {
            doConfigureRootProject(rootProject)
        }
    }

    private TaskProvider<Javadoc> createJavadocTask(Project project) {
        if (isAndroidProject(project)) {
            return project.tasks.register(JAVADOC_TASK_NAME, Javadoc,
                new Action<Javadoc>() {
                    @Override
                    @CompileDynamic
                    void execute(Javadoc t) {
                        ProjectConfigurationExtension config = resolveConfig(t.project)
                        t.enabled = config.docs.javadoc.enabled
                        t.dependsOn resolveClassesTask(project)
                        t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                        t.description = 'Generates Javadoc API documentation'
                        t.destinationDir = project.file("${project.buildDir}/docs/javadoc")
                        t.source = config.docs.javadoc.empty ? [] : resolveAllSource(project)
                        t.classpath += project.configurations.findByName('optional')
                        t.options.footer = "Copyright &copy; ${config.info.copyrightYear} ${config.info.getAuthors().join(', ')}. All rights reserved."
                        config.docs.javadoc.applyTo(t)
                        if (JavaVersion.current().isJava8Compatible()) {
                            t.options.addBooleanOption('Xdoclint:none', true)
                            t.options.quiet()
                        }
                    }
                })
        }

        project.tasks.named(JAVADOC_TASK_NAME, Javadoc,
            new Action<Javadoc>() {
                @Override
                @CompileDynamic
                void execute(Javadoc t) {
                    ProjectConfigurationExtension config = resolveConfig(t.project)
                    t.enabled = config.docs.javadoc.enabled
                    t.dependsOn resolveClassesTask(project)
                    t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                    t.description = 'Generates Javadoc API documentation'
                    t.destinationDir = project.file("${project.buildDir}/docs/javadoc")
                    t.source = config.docs.javadoc.empty ? [] : project.sourceSets.main.allJava
                    t.classpath += project.configurations.findByName('optional')
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
        ProjectConfigurationExtension config = resolveConfig(project)

        project.tasks.register(JAVADOC_JAR_TASK_NAME, Jar,
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
    }

    private void createCopyDocFilesTask(Project project,
                                        TaskProvider<Javadoc> javadoc,
                                        String taskName,
                                        Object sourceDirs) {
        ProjectConfigurationExtension config = resolveConfig(project)

        TaskProvider<Copy> copyDocFiles = project.tasks.register(taskName, Copy,
            new Action<Copy>() {
                @Override
                void execute(Copy t) {
                    t.enabled = config.docs.javadoc.enabled
                    t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                    t.description = 'Copy doc-files into Javadocs'
                    t.dependsOn javadoc
                    t.from sourceDirs
                    t.into(javadoc.get().destinationDir)
                    t.include('**/doc-files/*.*')
                    t.onlyIf { javadoc.get().enabled }
                    t.mustRunAfter(javadoc.get())
                }
            })

        javadoc.get().finalizedBy(copyDocFiles)
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
        if (!config.docs.javadoc.enabled) {
            return
        }

        if (project.tasks.findByName(JAVADOC_JAR_TASK_NAME)) {
            TaskProvider<Jar> javadocJar = project.tasks.named(JAVADOC_JAR_TASK_NAME, Jar)
            if (config.docs.javadoc.enabled && project.pluginManager.hasPlugin('maven-publish')) {
                PublishingExtension publishing = project.extensions.findByType(PublishingExtension)
                MavenPublication mainPublication = (MavenPublication) publishing.publications.findByName('main')
                if (mainPublication) {
                    MavenArtifact oldJavadocJar = mainPublication.artifacts?.find { it.classifier == 'javadoc' }
                    if (oldJavadocJar) mainPublication.artifacts.remove(oldJavadocJar)
                    mainPublication.artifact(javadocJar.get())
                    project.artifacts { archives(javadocJar.get()) }
                }

                registerJarVariant('Javadoc', 'javadoc', javadocJar, project)
            }
        }
    }

    private void createAggregateTasks(Project project) {
        TaskProvider<Javadoc> aggregateJavadoc = project.tasks.register(AGGREGATE_JAVADOC_TASK_NAME, Javadoc,
            new Action<Javadoc>() {
                @Override
                @CompileDynamic
                void execute(Javadoc t) {
                    t.enabled = false
                    t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                    t.description = 'Aggregates Javadoc API docs for all projects.'
                    t.destinationDir = project.file("${project.buildDir}/docs/aggregate-javadoc")
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
