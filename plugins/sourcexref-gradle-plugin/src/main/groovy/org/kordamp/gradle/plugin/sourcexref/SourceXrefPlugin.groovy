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
package org.kordamp.gradle.plugin.sourcexref

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.SourceXref

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.StringUtils.isNotBlank
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * @author Andres Almiray
 * @since 0.7.0
 */
@CompileStatic
class SourceXrefPlugin extends AbstractKordampPlugin {
    static final String SOURCE_XREF_TASK_NAME = 'sourceXref'
    static final String AGGREGATE_SOURCE_XREF_TASK_NAME = 'aggregateSourceXref'

    Project project

    SourceXrefPlugin() {
        super(org.kordamp.gradle.plugin.base.plugins.SourceXref.PLUGIN_ID)
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
        if (!project.plugins.findPlugin(SourceXrefPlugin)) {
            project.pluginManager.apply(SourceXrefPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)

        project.pluginManager.withPlugin('java-base') {
            project.afterEvaluate {
                ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
                setEnabled(effectiveConfig.docs.sourceXref.enabled)

                configureSourceXrefTask(project)
            }
        }
    }

    private void configureRootProject(Project project) {
        TaskProvider<JxrTask> aggregateJxrTask = project.tasks.register(AGGREGATE_SOURCE_XREF_TASK_NAME, JxrTask,
            new Action<JxrTask>() {
                @Override
                void execute(JxrTask t) {
                    t.group = 'Documentation'
                    t.description = 'Generates an aggregate JXR report of the source code.'
                    t.outputDirectory = project.file("${project.buildDir}/docs/aggregate-source-xref")
                    t.enabled = false
                }
            })

        TaskProvider<Jar> aggregateJxrJarTask = project.tasks.register(AGGREGATE_SOURCE_XREF_TASK_NAME + 'Jar', Jar,
            new Action<Jar>() {
                @Override
                void execute(Jar t) {
                    t.dependsOn aggregateJxrTask
                    t.group = 'Documentation'
                    t.description = 'An archive of the JXR report the source code.'
                    t.archiveClassifier.set 'sources-jxr'
                    t.from aggregateJxrTask.get().outputDirectory
                    t.onlyIf { aggregateJxrTask.get().enabled }
                    t.enabled = false
                }
            })

        project.gradle.addBuildListener(new BuildAdapter() {
            @Override
            void projectsEvaluated(Gradle gradle) {
                configureAggregateSourceXrefTask(project, aggregateJxrTask, aggregateJxrJarTask)
            }
        })
    }

    private TaskProvider<JxrTask> configureSourceXrefTask(Project project) {
        ProjectConfigurationExtension config = resolveEffectiveConfig(project)

        TaskProvider<JxrTask> jxrTask = project.tasks.register(SOURCE_XREF_TASK_NAME, JxrTask,
            new Action<JxrTask>() {
                @Override
                void execute(JxrTask t) {
                    t.enabled = config.docs.sourceXref.enabled
                    t.dependsOn project.tasks.named('classes')
                    t.group = 'Documentation'
                    t.description = 'Generates a JXR report of the source code.'
                    t.outputDirectory = project.file("${project.buildDir}/docs/source-xref")
                    t.sourceDirs = resolveSrcDirs(project)
                }
            })

        configureTask(config.docs.sourceXref, jxrTask)

        project.tasks.register(SOURCE_XREF_TASK_NAME + 'Jar', Jar,
            new Action<Jar>() {
                @Override
                void execute(Jar t) {
                    t.enabled = config.docs.sourceXref.enabled
                    t.dependsOn jxrTask
                    t.group = 'Documentation'
                    t.description = 'An archive of the JXR report the source code.'
                    t.archiveClassifier.set 'sources-jxr'
                    t.from jxrTask.get().outputDirectory
                    t.onlyIf { jxrTask.get().enabled }
                }
            })

        jxrTask
    }

    private void configureAggregateSourceXrefTask(Project project,
                                                  TaskProvider<JxrTask> aggregateJxrTask,
                                                  TaskProvider<Jar> aggregateJxrJarTask) {
        ProjectConfigurationExtension config = resolveEffectiveConfig(project)

        FileCollection srcdirs = project.objects.fileCollection()
        project.tasks.withType(JxrTask) { JxrTask task ->
            if (project in config.docs.sourceHtml.aggregate.excludedProjects()) return
            if (task.name != AGGREGATE_SOURCE_XREF_TASK_NAME &&
                task.enabled)
                srcdirs = srcdirs.plus(task.sourceDirs)
        }

        project.childProjects.values().each { p ->
            if (p in config.docs.sourceHtml.aggregate.excludedProjects()) return
            p.tasks.withType(JxrTask) { JxrTask task ->
                if (task.enabled) srcdirs = srcdirs.plus(task.sourceDirs)
            }
        }

        aggregateJxrTask.configure(new Action<JxrTask>() {
            @Override
            void execute(JxrTask t) {
                t.sourceDirs = srcdirs
                t.enabled = config.docs.sourceXref.aggregate.enabled
            }
        })
        configureTask(config.docs.sourceXref, aggregateJxrTask)

        aggregateJxrJarTask.configure(new Action<Jar>() {
            @Override
            void execute(Jar t) {
                t.enabled = config.docs.sourceXref.aggregate.enabled
            }
        })
    }

    private TaskProvider<JxrTask> configureTask(SourceXref sourceXref, TaskProvider<JxrTask> jxrTask) {
        jxrTask.configure(new Action<JxrTask>() {
            @Override
            void execute(JxrTask t) {
                if (isNotBlank(sourceXref.templateDir)) t.templateDir = sourceXref.templateDir
                if (isNotBlank(sourceXref.inputEncoding)) t.inputEncoding = sourceXref.inputEncoding
                if (isNotBlank(sourceXref.outputEncoding)) t.outputEncoding = sourceXref.outputEncoding
                if (isNotBlank(sourceXref.windowTitle)) t.windowTitle = sourceXref.windowTitle
                if (isNotBlank(sourceXref.docTitle)) t.docTitle = sourceXref.docTitle
                if (isNotBlank(sourceXref.bottom)) t.bottom = sourceXref.bottom
                if (isNotBlank(sourceXref.stylesheet)) t.stylesheet = sourceXref.stylesheet
                if (sourceXref.javaVersion) t.javaVersion = sourceXref.javaVersion
                if (sourceXref.excludes) t.excludes.addAll(sourceXref.excludes)
                if (sourceXref.includes) t.includes.addAll(sourceXref.includes)
            }
        })

        jxrTask
    }

    private boolean hasJavaPlugin(Project project) {
        project.pluginManager.hasPlugin('java-base')
    }

    private boolean hasGroovyPlugin(Project project) {
        project.pluginManager.hasPlugin('groovy-base')
    }

    @CompileDynamic
    private FileCollection resolveSrcDirs(Project project) {
        try {
            if (project.sourceSets.main) {
                if (hasGroovyPlugin(project)) {
                    return project.files(project.files(
                        project.sourceSets.main.groovy.srcDirs,
                        project.sourceSets.main.java.srcDirs).files.findAll { file ->
                        file.exists()
                    })
                } else if (hasJavaPlugin(project)) {
                    return project.files(
                        project.files(project.sourceSets.main.java.srcDirs).files.findAll { file ->
                            file.exists()
                        })
                }
            }
        } catch (Exception ignored) {
            // ignore this project
            return project.objects.fileCollection()
        }
    }
}
