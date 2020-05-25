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
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.kordamp.gradle.annotations.DependsOn
import org.kordamp.gradle.listener.AllProjectsEvaluatedListener
import org.kordamp.gradle.listener.ProjectEvaluatedListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.SourceXref

import javax.inject.Named

import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addAllProjectsEvaluatedListener
import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addProjectEvaluatedListener
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject
import static org.kordamp.gradle.util.PluginUtils.resolveClassesTask
import static org.kordamp.gradle.util.PluginUtils.resolveConfig
import static org.kordamp.gradle.util.StringUtils.isNotBlank

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
        super(SourceXref.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        configureProject(project)
        if (isRootProject(project)) {
            configureRootProject(project)
            project.childProjects.values().each {
                it.pluginManager.apply(SourceXrefPlugin)
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
            addProjectEvaluatedListener(project, new SourceXrefProjectEvaluatedListener())
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

        project.tasks.register(AGGREGATE_SOURCE_XREF_TASK_NAME + 'Jar', Jar,
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

        addAllProjectsEvaluatedListener(project, new SourceXrefAllProjectsEvaluatedListener())
    }

    @Named('sourceXref')
    @DependsOn(['base'])
    private class SourceXrefProjectEvaluatedListener implements ProjectEvaluatedListener {
        @Override
        void projectEvaluated(Project project) {
            ProjectConfigurationExtension config = resolveConfig(project)
            setEnabled(config.docs.sourceXref.enabled)

            configureSourceXrefTask(project)
        }
    }

    @Named('sourceXref')
    @DependsOn(['base'])
    private class SourceXrefAllProjectsEvaluatedListener implements AllProjectsEvaluatedListener {
        @Override
        void allProjectsEvaluated(Project rootProject) {
            configureAggregateSourceXrefTask(rootProject)
        }
    }

    private TaskProvider<JxrTask> configureSourceXrefTask(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)

        TaskProvider<JxrTask> jxrTask = project.tasks.register(SOURCE_XREF_TASK_NAME, JxrTask,
            new Action<JxrTask>() {
                @Override
                void execute(JxrTask t) {
                    t.enabled = config.docs.sourceXref.enabled
                    t.dependsOn resolveClassesTask(project)
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

    private void configureAggregateSourceXrefTask(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)

        FileCollection srcdirs = project.objects.fileCollection()
        project.tasks.withType(JxrTask) { JxrTask task ->
            if (project in config.docs.sourceHtml.aggregate.excludedProjects) return
            if (task.name != AGGREGATE_SOURCE_XREF_TASK_NAME &&
                task.enabled)
                srcdirs = srcdirs.plus(task.sourceDirs)
        }

        project.childProjects.values().each { p ->
            if (p in config.docs.sourceHtml.aggregate.excludedProjects) return
            p.tasks.withType(JxrTask) { JxrTask task ->
                if (task.enabled) srcdirs = srcdirs.plus(task.sourceDirs)
            }
        }

        TaskProvider<JxrTask> aggregateJxrTask = project.tasks.named(AGGREGATE_SOURCE_XREF_TASK_NAME, JxrTask,
            new Action<JxrTask>() {
                @Override
                void execute(JxrTask t) {
                    t.sourceDirs = srcdirs
                    t.enabled = config.docs.sourceXref.aggregate.enabled
                }
            })
        configureTask(config.docs.sourceXref, aggregateJxrTask)

        project.tasks.named(AGGREGATE_SOURCE_XREF_TASK_NAME + 'Jar', Jar, new Action<Jar>() {
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
