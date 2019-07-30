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
package org.kordamp.gradle.plugin.javadoc

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

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
        if (!project.plugins.findPlugin(JavadocPlugin)) {
            project.pluginManager.apply(JavadocPlugin)
        }
    }

    @CompileDynamic
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
                    ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
                    setEnabled(effectiveConfig.javadoc.enabled)

                    if (!enabled) {
                        return
                    }

                    project.pluginManager.withPlugin('java-base') {
                        Task javadoc = createJavadocTaskIfNeeded(project)
                        if (!javadoc) return
                        effectiveConfig.javadoc.javadocTasks() << javadoc

                        Task javadocJar = createJavadocJarTask(project, javadoc)
                        project.tasks.findByName(org.gradle.api.plugins.BasePlugin.ASSEMBLE_TASK_NAME).dependsOn(javadocJar)
                        effectiveConfig.javadoc.javadocJarTasks() << javadocJar

                        effectiveConfig.javadoc.projects() << project
                    }

                    project.tasks.withType(Javadoc) { Javadoc task ->
                        effectiveConfig.javadoc.applyTo(task)
                        task.options.footer = "Copyright &copy; ${effectiveConfig.info.copyrightYear} ${effectiveConfig.info.getAuthors().join(', ')}. All rights reserved."

                        if (JavaVersion.current().isJava8Compatible()) {
                            task.options.addStringOption('Xdoclint:none', '-quiet')
                        }
                    }
                }
            }
        })
    }

    @CompileDynamic
    private Task createJavadocTaskIfNeeded(Project project) {
        String taskName = JAVADOC_TASK_NAME

        Javadoc javadocTask = project.tasks.findByName(taskName)
        Task classesTask = project.tasks.findByName('classes')

        if (classesTask && !javadocTask) {
            javadocTask = project.tasks.create(taskName, Javadoc) {
                dependsOn classesTask
                group JavaBasePlugin.DOCUMENTATION_GROUP
                description 'Generates Javadoc API documentation'
                source project.sourceSets.main.allSource
                destinationDir project.file("${project.buildDir}/docs/javadoc")
            }
        }

        if (javadocTask) {
            ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
            javadocTask.configure {
                include(effectiveConfig.javadoc.includes)
                exclude(effectiveConfig.javadoc.excludes)
            }
        }

        javadocTask
    }

    @CompileDynamic
    private Task createJavadocJarTask(Project project, Task javadoc) {
        String taskName = JAVADOC_JAR_TASK_NAME

        Task javadocJarTask = project.tasks.findByName(taskName)

        if (!javadocJarTask) {
            javadocJarTask = project.tasks.create(taskName, Jar) {
                dependsOn javadoc
                group JavaBasePlugin.DOCUMENTATION_GROUP
                description 'An archive of the Javadoc API docs'
                classifier 'javadoc'
                from javadoc.destinationDir
            }
        }

        javadocJarTask
    }
}
