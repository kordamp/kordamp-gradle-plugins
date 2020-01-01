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
package org.kordamp.gradle.plugin.apidoc

import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Groovydoc
import org.gradle.api.tasks.javadoc.Javadoc
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.groovydoc.GroovydocPlugin
import org.kordamp.gradle.plugin.javadoc.JavadocPlugin

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * Configures {@code aggregateApidocs} {@code aggregateApidocsJar} tasks on the root project.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@Deprecated
class ApidocPlugin extends AbstractKordampPlugin {
    static final String AGGREGATE_APIDOCS_TASK_NAME = 'aggregateApidocs'
    static final String AGGREGATE_JAVADOCS_TASK_NAME = 'aggregateJavadocs'
    static final String AGGREGATE_JAVADOCS_JAR_TASK_NAME = 'aggregateJavadocsJar'
    static final String AGGREGATE_GROOVYDOCS_TASK_NAME = 'aggregateGroovydocs'
    static final String AGGREGATE_GROOVYDOCS_JAR_TASK_NAME = 'aggregateGroovydocsJar'

    Project project

    @CompileStatic
    void apply(Project project) {
        println("The apidoc plugin has been deprecated and will be removed in a future release")
        this.project = project

        if (isRootProject(project)) {
            if (project.childProjects.size()) {
                project.childProjects.values().each {
                    configureProject(it)
                }
                configureRootProject(project, true)
            } else {
                configureProject(project)
                configureRootProject(project, false)
            }
        } else {
            configureProject(project)
        }
    }

    @CompileStatic
    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(ApidocPlugin)) {
            project.pluginManager.apply(ApidocPlugin)
        }
    }

    @CompileStatic
    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        JavadocPlugin.applyIfMissing(project)
        GroovydocPlugin.applyIfMissing(project)
    }

    @CompileStatic
    private void configureRootProject(Project project, boolean checkIfApplied) {
        if (checkIfApplied && hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)

        if (isRootProject(project)) {
            createAggregateGroovydocsTask(project, createAggregateJavadocsTask(project))
            createAggregateApidocTask(project)

            project.gradle.addBuildListener(new BuildAdapter() {
                @Override
                void projectsEvaluated(Gradle gradle) {
                    doConfigureRootProject(project)
                }
            })
        }
    }

    private void doConfigureRootProject(Project project) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        setEnabled(effectiveConfig.docs.apidoc.enabled)

        if (!project.childProjects.isEmpty()) {
            List<Javadoc> javadocs = []
            project.tasks.withType(Javadoc) { Javadoc javadoc -> if (javadoc.name != AGGREGATE_JAVADOCS_TASK_NAME && javadoc.enabled) javadocs << javadoc }
            project.childProjects.values().each { Project p ->
                if (p in effectiveConfig.docs.apidoc.excludedProjects()) return
                p.tasks.withType(Javadoc) { Javadoc javadoc -> if (javadoc.enabled) javadocs << javadoc }
            }
            javadocs = javadocs.unique()

            List<Groovydoc> groovydocs = []
            project.tasks.withType(Groovydoc) { Groovydoc groovydoc -> if (groovydoc.name != AGGREGATE_GROOVYDOCS_TASK_NAME && groovydoc.enabled) groovydocs << groovydoc }
            project.childProjects.values().each { Project p ->
                if (p in effectiveConfig.docs.apidoc.excludedProjects()) return
                p.tasks.withType(Groovydoc) { Groovydoc groovydoc -> if (groovydoc.enabled) groovydocs << groovydoc }
            }
            groovydocs = groovydocs.unique()

            Javadoc aggregateJavadocs = (Javadoc) project.tasks.findByName(AGGREGATE_JAVADOCS_TASK_NAME)
            Jar aggregateJavadocsJar = (Jar) project.tasks.findByName(AGGREGATE_JAVADOCS_JAR_TASK_NAME)
            Groovydoc aggregateGroovydocs = (Groovydoc) project.tasks.findByName(AGGREGATE_GROOVYDOCS_TASK_NAME)
            Jar aggregateGroovydocsJar = (Jar) project.tasks.findByName(AGGREGATE_GROOVYDOCS_JAR_TASK_NAME)
            Task aggregateApidocTask = project.tasks.findByName(AGGREGATE_APIDOCS_TASK_NAME)

            if (javadocs && !effectiveConfig.docs.groovydoc.replaceJavadoc) {
                aggregateJavadocs.configure { task ->
                    task.enabled = effectiveConfig.docs.apidoc.enabled
                    task.dependsOn javadocs
                    task.source javadocs.source
                    task.classpath = project.files(javadocs.classpath)

                    effectiveConfig.docs.javadoc.applyTo(task)
                    task.options.footer = "Copyright &copy; ${effectiveConfig.info.copyrightYear} ${effectiveConfig.info.authors.join(', ')}. All rights reserved."
                }
                aggregateJavadocsJar.configure {
                    enabled = effectiveConfig.docs.apidoc.enabled
                    from aggregateJavadocs.destinationDir
                }

                aggregateApidocTask.enabled = effectiveConfig.docs.apidoc.enabled
                aggregateApidocTask.dependsOn aggregateJavadocs
            }

            if (groovydocs && effectiveConfig.docs.groovydoc.enabled) {
                aggregateGroovydocs.configure { task ->
                    task.enabled = effectiveConfig.docs.apidoc.enabled
                    task.dependsOn groovydocs + javadocs
                    task.source groovydocs.source + javadocs.source
                    task.classpath = project.files(groovydocs.classpath + javadocs.classpath)
                    task.groovyClasspath = project.files(groovydocs.groovyClasspath.flatten().unique())

                    effectiveConfig.docs.groovydoc.applyTo(task)
                    task.footer = "Copyright &copy; ${effectiveConfig.info.copyrightYear} ${effectiveConfig.info.authors.join(', ')}. All rights reserved."
                }
                aggregateGroovydocsJar.configure {
                    enabled = effectiveConfig.docs.apidoc.enabled
                    from aggregateGroovydocs.destinationDir
                    archiveClassifier.set(effectiveConfig.docs.groovydoc.replaceJavadoc ? 'javadoc' : 'groovydoc')
                }

                aggregateApidocTask.enabled = effectiveConfig.docs.apidoc.enabled
                aggregateApidocTask.dependsOn aggregateGroovydocs
            }
        }
    }

    private TaskProvider<Javadoc> createAggregateJavadocsTask(Project project) {
        TaskProvider<Javadoc> aggregateJavadocs = project.tasks.register(AGGREGATE_JAVADOCS_TASK_NAME, Javadoc,
                new Action<Javadoc>() {
                    @Override
                    void execute(Javadoc t) {
                        t.enabled = false
                        t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                        t.description = 'Aggregates Javadoc API docs for all projects.'
                        t.destinationDir = project.file("${project.buildDir}/docs/javadoc")
                        if (JavaVersion.current().isJava8Compatible()) {
                            t.options.addBooleanOption('Xdoclint:none', true)
                            t.options.quiet()
                        }
                    }
                })

        project.tasks.register(AGGREGATE_JAVADOCS_JAR_TASK_NAME, Jar,
                new Action<Jar>() {
                    @Override
                    void execute(Jar t) {
                        t.dependsOn aggregateJavadocs
                        t.enabled = false
                        t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                        t.description = 'An archive of the aggregate Javadoc API docs'
                        t.archiveClassifier.set('javadoc')
                    }
                })

        aggregateJavadocs
    }

    @CompileStatic
    private void createAggregateGroovydocsTask(Project project, TaskProvider<Javadoc> aggregateJavadoc) {
        TaskProvider<Groovydoc> aggregateGroovydocs = project.tasks.register(AGGREGATE_GROOVYDOCS_TASK_NAME, Groovydoc,
                new Action<Groovydoc>() {
                    @Override
                    void execute(Groovydoc t) {
                        t.enabled = false
                        t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                        t.description = 'Aggregates Groovy API docs for all projects.'
                        t.destinationDir = project.file("${project.buildDir}/docs/groovydoc")
                        t.classpath = aggregateJavadoc.get().classpath
                    }
                })

        project.tasks.register(AGGREGATE_GROOVYDOCS_JAR_TASK_NAME, Jar,
                new Action<Jar>() {
                    @Override
                    void execute(Jar t) {
                        t.dependsOn aggregateGroovydocs
                        t.enabled = false
                        t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                        t.description = 'An archive of the aggregate Groovy API docs'
                        t.archiveClassifier.set('groovydoc')
                    }
                })
    }

    @CompileStatic
    private void createAggregateApidocTask(Project project) {
        project.tasks.register(AGGREGATE_APIDOCS_TASK_NAME, DefaultTask, new Action<DefaultTask>() {
            @Override
            void execute(DefaultTask t) {
                t.enabled = false
                t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                t.description = 'Aggregates all API docs for all projects.'
            }
        })
    }
}
