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
package org.kordamp.gradle.plugin.scaladoc

import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.scala.ScalaBasePlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.scala.ScalaDoc
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * Configures {@code scaladoc} and {@code scaladocJar} tasks.
 *
 * @author Andres Almiray
 * @since 0.15.0
 */
class ScaladocPlugin extends AbstractKordampPlugin {
    static final String SCALADOC_TASK_NAME = 'scaladoc'
    static final String SCALADOC_JAR_TASK_NAME = 'scaladocJar'
    static final String AGGREGATE_SCALADOCS_TASK_NAME = 'aggregateScaladocs'
    static final String AGGREGATE_SCALADOCS_JAR_TASK_NAME = 'aggregateScaladocsJar'

    Project project

    @CompileStatic
    void apply(Project project) {
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
        if (!project.plugins.findPlugin(ScaladocPlugin)) {
            project.plugins.apply(ScaladocPlugin)
        }
    }

    @CompileStatic
    private void configureRootProject(Project project, boolean checkIfApplied) {
        if (checkIfApplied && hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)

        if (isRootProject(project) && !project.childProjects.isEmpty()) {
            createAggregateScaladocsTask(project)

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
        setEnabled(effectiveConfig.scaladoc.enabled)

        if (!enabled) {
            return
        }

        if (!project.childProjects.isEmpty()) {
            List<ScalaDoc> scaladocs = []
            project.tasks.withType(ScalaDoc) { ScalaDoc scaladoc -> if (scaladoc.name != AGGREGATE_SCALADOCS_TASK_NAME && scaladoc.enabled) scaladocs << scaladoc }
            project.childProjects.values().each { Project p ->
                if (p in effectiveConfig.scaladoc.excludedProjects()) return
                p.tasks.withType(ScalaDoc) { ScalaDoc scaladoc -> if (scaladoc.enabled) scaladocs << scaladoc }
            }
            scaladocs = scaladocs.unique()

            ScalaDoc aggregateScaladocs = project.tasks.findByName(AGGREGATE_SCALADOCS_TASK_NAME)
            Jar aggregateScaladocsJar = project.tasks.findByName(AGGREGATE_SCALADOCS_JAR_TASK_NAME)

            if (scaladocs) {
                aggregateScaladocs.configure { task ->
                    task.enabled true
                    task.dependsOn scaladocs
                    task.source scaladocs.source
                    task.classpath = project.files(scaladocs.classpath)

                    effectiveConfig.scaladoc.applyTo(task)
                    // task.options.footer = "Copyright &copy; ${effectiveConfig.info.copyrightYear} ${effectiveConfig.info.authors.join(', ')}. All rights reserved."
                }
                aggregateScaladocsJar.configure {
                    enabled true
                    from aggregateScaladocs.destinationDir
                }
            }
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
            setEnabled(effectiveConfig.scaladoc.enabled)

            if (!enabled) {
                return
            }

            project.plugins.withType(ScalaBasePlugin) {
                Task scaladoc = configureScaladocTask(project)
                effectiveConfig.scaladoc.scaladocTasks() << scaladoc

                Task scaladocJar = createScaladocJarTask(project, scaladoc)
                project.tasks.findByName(org.gradle.api.plugins.BasePlugin.ASSEMBLE_TASK_NAME).dependsOn(scaladocJar)
                effectiveConfig.scaladoc.scaladocJarTasks() << scaladocJar

                effectiveConfig.scaladoc.projects() << project

                project.tasks.withType(ScalaDoc) { task ->
                    effectiveConfig.scaladoc.applyTo(task)
                    // task.scalaDocOptions.footer = "Copyright &copy; ${effectiveConfig.info.copyrightYear} ${effectiveConfig.info.getAuthors().join(', ')}. All rights reserved."
                }
            }
        }
    }

    private Task configureScaladocTask(Project project) {
        String taskName = SCALADOC_TASK_NAME

        ScalaDoc scaladocTask = project.tasks.findByName(taskName)
        Task classesTask = project.tasks.findByName('classes')

        if (classesTask && !scaladocTask) {
            scaladocTask = project.tasks.create(taskName, ScalaDoc) {
                dependsOn classesTask
                group JavaBasePlugin.DOCUMENTATION_GROUP
                description 'Generates Scaladoc API documentation'
                source project.sourceSets.main.allSource
                destinationDir project.file("${project.buildDir}/docs/scaladoc")
            }
        }

        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        scaladocTask.configure {
            include(effectiveConfig.scaladoc.includes)
            exclude(effectiveConfig.scaladoc.excludes)
        }

        scaladocTask
    }

    private Task createScaladocJarTask(Project project, Task scaladoc) {
        String taskName = SCALADOC_JAR_TASK_NAME

        Task scaladocJarTask = project.tasks.findByName(taskName)

        if (!scaladocJarTask) {
            scaladocJarTask = project.tasks.create(taskName, Jar) {
                dependsOn scaladoc
                group JavaBasePlugin.DOCUMENTATION_GROUP
                description 'An archive of the Scaladoc API docs'
                classifier 'scaladoc'
                from scaladoc.destinationDir
            }
        }

        scaladocJarTask
    }

    private List<Task> createAggregateScaladocsTask(Project project) {
        ScalaDoc aggregateScaladocs = project.tasks.create(AGGREGATE_SCALADOCS_TASK_NAME, ScalaDoc) {
            enabled false
            group JavaBasePlugin.DOCUMENTATION_GROUP
            description 'Aggregates ScalaDoc API docs for all projects.'
            destinationDir project.file("${project.buildDir}/docs/scaladoc")
        }

        Jar aggregateScaladocsJar = project.tasks.create(AGGREGATE_SCALADOCS_JAR_TASK_NAME, Jar) {
            enabled false
            dependsOn aggregateScaladocs
            group JavaBasePlugin.DOCUMENTATION_GROUP
            description 'An archive of the aggregate ScalaDoc API docs'
            classifier 'scaladoc'
        }

        [aggregateScaladocs, aggregateScaladocsJar]
    }
}
