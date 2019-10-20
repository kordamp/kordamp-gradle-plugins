/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
import org.gradle.api.Task
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenArtifact
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Groovydoc
import org.gradle.api.tasks.javadoc.Javadoc
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.javadoc.JavadocPlugin

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

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

    Project project

    void apply(Project project) {
        this.project = project

        if (isRootProject(project)) {
            if (project.childProjects.size()) {
                project.childProjects.values().each {
                    configureProject(it)
                }
            } else {
                configureProject(project)
            }
        } else {
            configureProject(project)
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(GroovydocPlugin)) {
            project.pluginManager.apply(GroovydocPlugin)
        }
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

                project.afterEvaluate {
                    ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
                    setEnabled(effectiveConfig.groovydoc.enabled)

                    if (!enabled) {
                        return
                    }

                    Javadoc javadoc = (Javadoc) project.tasks.findByName(JavadocPlugin.JAVADOC_TASK_NAME)
                    if (!javadoc) return

                    Groovydoc groovydoc = createGroovydocTaskIfNeeded(project, javadoc)
                    if (!groovydoc) return
                    effectiveConfig.groovydoc.groovydocTasks() << groovydoc

                    TaskProvider<Jar> groovydocJar = createGroovydocJarTask(project, groovydoc)
                    project.tasks.findByName(org.gradle.api.plugins.BasePlugin.ASSEMBLE_TASK_NAME).dependsOn(groovydocJar)
                    effectiveConfig.groovydoc.groovydocJarTasks() << groovydocJar

                    effectiveConfig.groovydoc.projects() << project

                    project.tasks.withType(Groovydoc) { Groovydoc task ->
                        effectiveConfig.groovydoc.applyTo(task)
                        task.footer = "Copyright &copy; ${effectiveConfig.info.copyrightYear} ${effectiveConfig.info.getAuthors().join(', ')}. All rights reserved."
                    }
                }
            }
        })
    }

    @CompileDynamic
    private Groovydoc createGroovydocTaskIfNeeded(Project project, Javadoc javadoc) {
        String taskName = GROOVYDOC_TASK_NAME

        Groovydoc groovydocTask = project.tasks.findByName(taskName)
        Task classesTask = project.tasks.findByName('classes')

        if (classesTask && !groovydocTask) {
            groovydocTask = project.tasks.create(taskName, Groovydoc) {
                dependsOn classesTask
                group JavaBasePlugin.DOCUMENTATION_GROUP
                description 'Generates Groovydoc API documentation'
                source project.sourceSets.main.allSource
                destinationDir project.file("${project.buildDir}/docs/groovydoc")
            }
        }

        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        groovydocTask.configure {
            classpath = javadoc.classpath
            include(effectiveConfig.groovydoc.includes)
            exclude(effectiveConfig.groovydoc.excludes)
        }

        try {
            // project may not have 'sourceSets' property
            if (project.sourceSets.main.java) {
                groovydocTask.source(project.files(project.sourceSets.main.java.srcDirs))
            }
        } catch (Exception ignored) {
            // ignored
        }

        groovydocTask
    }

    private TaskProvider<Jar> createGroovydocJarTask(Project project, Groovydoc groovydoc) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        TaskProvider<Jar> groovydocJar = project.tasks.register(GROOVYDOC_JAR_TASK_NAME, Jar,
            new Action<Jar>() {
                @Override
                void execute(Jar t) {
                    t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                    t.description = 'An archive of the Groovydoc API docs'
                    t.archiveClassifier.set(effectiveConfig.groovydoc.replaceJavadoc ? 'javadoc' : 'groovydoc')
                    t.dependsOn groovydoc
                    t.from groovydoc.destinationDir
                }
            })

        if (project.pluginManager.hasPlugin('maven-publish')) {
            PublishingExtension publishing = project.extensions.findByType(PublishingExtension)
            MavenPublication mainPublication = (MavenPublication) publishing.publications.findByName('main')
            if (effectiveConfig.groovydoc.replaceJavadoc) {
                MavenArtifact javadocJar = mainPublication.artifacts.find { it.classifier == 'javadoc' }
                mainPublication.artifacts.remove(javadocJar)
            }
            mainPublication.artifact(groovydocJar.get())
        }

        if (effectiveConfig.groovydoc.replaceJavadoc) {
            project.tasks.findByName(JavadocPlugin.JAVADOC_TASK_NAME)?.enabled = false
            project.tasks.findByName(JavadocPlugin.JAVADOC_JAR_TASK_NAME)?.enabled = false
        }

        groovydocJar
    }
}
