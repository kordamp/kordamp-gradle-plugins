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
package org.kordamp.gradle.plugin.sourcehtml

import com.bmuschko.gradle.java2html.ConvertCodeTask
import com.bmuschko.gradle.java2html.GenerateOverviewTask
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
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
import static org.kordamp.gradle.util.PluginUtils.resolveClassesTask
import static org.kordamp.gradle.util.PluginUtils.resolveEffectiveConfig

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
@CompileStatic
class SourceHtmlPlugin extends AbstractKordampPlugin {
    static final String AGGREGATE_SOURCE_HTML_TASK_NAME = 'aggregateSourceHtml'
    static final String AGGREGATE_CONVERT_CODE_TO_HTML_TASK_NAME = 'aggregateConvertCodeToHtml'
    static final String AGGREGATE_GENERATE_SOURCE_HTML_OVERVIEW_TASK_NAME = 'aggregateGenerateSourceHtmlOverview'
    static final String SOURCE_HTML_TASK_NAME = 'sourceHtml'
    static final String CONFIGURATION_NAME = 'java2html'

    Project project

    SourceHtmlPlugin() {
        super(org.kordamp.gradle.plugin.base.plugins.SourceHtml.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        configureProject(project)
        if (isRootProject(project)) {
            configureRootProject(project)
            project.childProjects.values().each {
                it.pluginManager.apply(SourceHtmlPlugin)
            }
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(SourceHtmlPlugin)) {
            project.pluginManager.apply(SourceHtmlPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)

        Configuration configuration = project.configurations.create(CONFIGURATION_NAME)
            .setVisible(false)
            .setTransitive(true)
            .setDescription('The Java2HTML library to be used for this project.')

        configuration.incoming.beforeResolve(new Action<ResolvableDependencies>() {
            @SuppressWarnings('UnusedMethodParameter')
            void execute(ResolvableDependencies resolvableDependencies) {
                DependencyHandler dependencyHandler = project.dependencies
                DependencySet dependencies = configuration.dependencies
                dependencies.add(dependencyHandler.create('de.java2html:java2html:5.0'))
            }
        })
        project.pluginManager.withPlugin('java-base') {
            addProjectEvaluatedListener(project, new SourceHtmlProjectEvaluatedListener())
        }
    }

    private void configureRootProject(Project project) {
        TaskProvider<SourceHtmlTask> sourceHtmlTask = project.tasks.register(AGGREGATE_SOURCE_HTML_TASK_NAME, SourceHtmlTask,
            new Action<SourceHtmlTask>() {
                @Override
                void execute(SourceHtmlTask t) {
                    t.group = 'Documentation'
                    t.description = 'Generates a HTML report of the source code.'
                    t.destinationDir = project.file("${project.buildDir}/docs/aggregate-source-html")
                    t.enabled = false
                }
            })

        project.tasks.register(AGGREGATE_SOURCE_HTML_TASK_NAME + 'Jar', Jar,
            new Action<Jar>() {
                @Override
                void execute(Jar t) {
                    t.dependsOn sourceHtmlTask
                    t.group = 'Documentation'
                    t.description = 'An archive of the HTML report the source code.'
                    t.archiveClassifier.set 'sources-html'
                    t.from sourceHtmlTask.get().destinationDir
                    t.enabled = false
                }
            })

        addAllProjectsEvaluatedListener(project, new SourceHtmlAllProjectsEvaluatedListener())
    }

    @Named('sourceHtml')
    @DependsOn(['base'])
    private class SourceHtmlProjectEvaluatedListener implements ProjectEvaluatedListener {
        @Override
        void projectEvaluated(Project project) {
            ProjectConfigurationExtension config = resolveEffectiveConfig(project)
            setEnabled(config.docs.sourceHtml.enabled)

            configureSourceHtmlTask(project)
        }
    }

    @Named('sourceHtml')
    @DependsOn(['base'])
    private class SourceHtmlAllProjectsEvaluatedListener implements AllProjectsEvaluatedListener {
        @Override
        void allProjectsEvaluated(Project rootProject) {
            configureAggregateSourceHtmlTask(rootProject)
        }
    }

    private boolean configureSourceHtmlTask(Project project) {
        Configuration configuration = project.configurations.findByName(CONFIGURATION_NAME)
        ProjectConfigurationExtension config = resolveEffectiveConfig(project)

        config.docs.sourceHtml.srcDirs = resolveSrcDirs(project, config.docs.sourceHtml.conversion.srcDirs)

        TaskProvider<ConvertCodeTask> convertCodeTask = project.tasks.register('convertCodeToHtml', ConvertCodeTask,
            new Action<ConvertCodeTask>() {
                @Override
                void execute(ConvertCodeTask t) {
                    t.enabled = config.docs.sourceHtml.enabled && !config.docs.sourceHtml.srcDirs.isEmpty()
                    t.dependsOn resolveClassesTask(project)
                    t.group = 'Documentation'
                    t.description = 'Converts source code into HTML.'
                    t.classpath = configuration.asFileTree
                    t.srcDirs = config.docs.sourceHtml.srcDirs
                    t.destDir = config.docs.sourceHtml.conversion.destDir
                    t.includes = config.docs.sourceHtml.conversion.includes
                    t.outputFormat = config.docs.sourceHtml.conversion.outputFormat
                    t.tabs = config.docs.sourceHtml.conversion.tabs
                    t.style = config.docs.sourceHtml.conversion.style
                    t.showLineNumbers = config.docs.sourceHtml.conversion.showLineNumbers
                    t.showFileName = config.docs.sourceHtml.conversion.showFileName
                    t.showDefaultTitle = config.docs.sourceHtml.conversion.showDefaultTitle
                    t.showTableBorder = config.docs.sourceHtml.conversion.showTableBorder
                    t.includeDocumentHeader = config.docs.sourceHtml.conversion.includeDocumentHeader
                    t.includeDocumentFooter = config.docs.sourceHtml.conversion.includeDocumentFooter
                    t.addLineAnchors = config.docs.sourceHtml.conversion.addLineAnchors
                    t.lineAnchorPrefix = config.docs.sourceHtml.conversion.lineAnchorPrefix
                    t.horizontalAlignment = config.docs.sourceHtml.conversion.horizontalAlignment
                    t.useShortFileName = config.docs.sourceHtml.conversion.useShortFileName
                    t.overwrite = config.docs.sourceHtml.conversion.overwrite
                }
            })

        TaskProvider<GenerateOverviewTask> generateOverviewTask = project.tasks.register('generateSourceHtmlOverview', GenerateOverviewTask,
            new Action<GenerateOverviewTask>() {
                @Override
                void execute(GenerateOverviewTask t) {
                    t.enabled = config.docs.sourceHtml.enabled && !config.docs.sourceHtml.srcDirs.isEmpty()
                    t.dependsOn convertCodeTask
                    t.group = 'Documentation'
                    t.description = 'Generate an overview of converted source code.'
                    t.srcDirs = project.files(config.docs.sourceHtml.conversion.destDir)
                    t.destDir = config.docs.sourceHtml.overview.destDir
                    t.pattern = config.docs.sourceHtml.overview.pattern
                    t.windowTitle = config.docs.sourceHtml.overview.windowTitle
                    t.docTitle = config.docs.sourceHtml.overview.docTitle
                    t.docDescription = config.docs.sourceHtml.overview.docDescription ?: ''
                    t.icon = config.docs.sourceHtml.overview.icon
                    t.stylesheet = config.docs.sourceHtml.overview.stylesheet
                    t.onlyIf { convertCodeTask.get().enabled }
                }
            })

        TaskProvider<SourceHtmlTask> sourceHtmlTask = project.tasks.register(SOURCE_HTML_TASK_NAME, SourceHtmlTask,
            new Action<SourceHtmlTask>() {
                @Override
                void execute(SourceHtmlTask t) {
                    t.enabled = config.docs.sourceHtml.enabled && !config.docs.sourceHtml.srcDirs.isEmpty()
                    t.dependsOn generateOverviewTask
                    t.group = 'Documentation'
                    t.description = 'Generates a HTML report of the source code.'
                    t.destinationDir = project.file("${project.buildDir}/docs/source-html")
                    t.from convertCodeTask.get().destDir
                    t.from generateOverviewTask.get().destDir
                    t.onlyIf { generateOverviewTask.get().enabled }
                }
            })

        project.tasks.register(SOURCE_HTML_TASK_NAME + 'Jar', Jar,
            new Action<Jar>() {
                @Override
                void execute(Jar t) {
                    t.enabled = config.docs.sourceHtml.enabled && !config.docs.sourceHtml.srcDirs.isEmpty()
                    t.dependsOn sourceHtmlTask
                    t.group = 'Documentation'
                    t.description = 'An archive of the HTML report the source code.'
                    t.archiveClassifier.set 'sources-html'
                    t.from sourceHtmlTask.get().destinationDir
                    t.onlyIf { sourceHtmlTask.get().enabled }
                }
            })

        return !config.docs.sourceHtml.srcDirs.isEmpty()
    }

    private void configureAggregateSourceHtmlTask(Project project) {
        Configuration configuration = project.configurations.findByName(CONFIGURATION_NAME)
        ProjectConfigurationExtension config = resolveEffectiveConfig(project)

        FileCollection srcdirs = project.objects.fileCollection()
        project.tasks.withType(SourceHtmlTask) { SourceHtmlTask task ->
            if (project in config.docs.sourceHtml.aggregate.excludedProjects) return
            if (task.name != AGGREGATE_SOURCE_HTML_TASK_NAME &&
                task.enabled)
                srcdirs = srcdirs.plus(config.docs.sourceHtml.srcDirs)
        }

        project.childProjects.values().each { p ->
            if (p in config.docs.sourceHtml.aggregate.excludedProjects) return
            p.tasks.withType(SourceHtmlTask) { SourceHtmlTask task ->
                if (task.enabled) srcdirs = srcdirs.plus(resolveEffectiveConfig(p).docs.sourceHtml.srcDirs)
            }
        }

        TaskProvider<ConvertCodeTask> convertCodeTask = project.tasks.register(AGGREGATE_CONVERT_CODE_TO_HTML_TASK_NAME, ConvertCodeTask,
            new Action<ConvertCodeTask>() {
                @Override
                void execute(ConvertCodeTask t) {

                    t.enabled = config.docs.sourceHtml.aggregate.enabled
                    t.group = 'Documentation'
                    t.description = 'Converts source code into HTML.'
                    t.classpath = configuration.asFileTree
                    t.srcDirs = srcdirs
                    t.destDir = config.docs.sourceHtml.conversion.destDir
                    t.includes = config.docs.sourceHtml.conversion.includes
                    t.outputFormat = config.docs.sourceHtml.conversion.outputFormat
                    t.tabs = config.docs.sourceHtml.conversion.tabs
                    t.style = config.docs.sourceHtml.conversion.style
                    t.showLineNumbers = config.docs.sourceHtml.conversion.showLineNumbers
                    t.showFileName = config.docs.sourceHtml.conversion.showFileName
                    t.showDefaultTitle = config.docs.sourceHtml.conversion.showDefaultTitle
                    t.showTableBorder = config.docs.sourceHtml.conversion.showTableBorder
                    t.includeDocumentHeader = config.docs.sourceHtml.conversion.includeDocumentHeader
                    t.includeDocumentFooter = config.docs.sourceHtml.conversion.includeDocumentFooter
                    t.addLineAnchors = config.docs.sourceHtml.conversion.addLineAnchors
                    t.lineAnchorPrefix = config.docs.sourceHtml.conversion.lineAnchorPrefix
                    t.horizontalAlignment = config.docs.sourceHtml.conversion.horizontalAlignment
                    t.useShortFileName = config.docs.sourceHtml.conversion.useShortFileName
                    t.overwrite = config.docs.sourceHtml.conversion.overwrite
                }
            })

        TaskProvider<GenerateOverviewTask> generateOverviewTask = project.tasks.register(AGGREGATE_GENERATE_SOURCE_HTML_OVERVIEW_TASK_NAME, GenerateOverviewTask,
            new Action<GenerateOverviewTask>() {
                @Override
                void execute(GenerateOverviewTask t) {
                    t.enabled = config.docs.sourceHtml.aggregate.enabled
                    t.dependsOn convertCodeTask
                    t.group = 'Documentation'
                    t.description = 'Generate an overview of converted source code.'
                    t.srcDirs = project.files(config.docs.sourceHtml.conversion.destDir)
                    t.destDir = config.docs.sourceHtml.overview.destDir
                    t.pattern = config.docs.sourceHtml.overview.pattern
                    t.windowTitle = config.docs.sourceHtml.overview.windowTitle
                    t.docTitle = config.docs.sourceHtml.overview.docTitle
                    t.docDescription = config.docs.sourceHtml.overview.docDescription ?: ''
                    t.icon = config.docs.sourceHtml.overview.icon
                    t.stylesheet = config.docs.sourceHtml.overview.stylesheet
                }
            })

        TaskProvider<SourceHtmlTask> aggregateSourceHtmlTask = project.tasks.named(AGGREGATE_SOURCE_HTML_TASK_NAME, SourceHtmlTask,
            new Action<SourceHtmlTask>() {
                @Override
                void execute(SourceHtmlTask t) {
                    t.dependsOn generateOverviewTask
                    t.from convertCodeTask.get().destDir
                    t.from generateOverviewTask.get().destDir
                    t.enabled = config.docs.sourceHtml.aggregate.enabled
                }
            })

        project.tasks.named(AGGREGATE_SOURCE_HTML_TASK_NAME + 'Jar', Jar, new Action<Jar>() {
            @Override
            void execute(Jar t) {
                t.enabled = config.docs.sourceHtml.aggregate.enabled
                t.onlyIf { aggregateSourceHtmlTask.get().enabled }
            }
        })
    }

    private boolean hasJavaPlugin(Project project) {
        project.pluginManager.hasPlugin('java-base')
    }

    private boolean hasGroovyPlugin(Project project) {
        project.pluginManager.hasPlugin('groovy-base')
    }

    private boolean hasKotlinPlugin(Project project) {
        project.pluginManager.hasPlugin('org.jetbrains.kotlin.jvm')
    }

    private boolean hasScalaPlugin(Project project) {
        project.pluginManager.hasPlugin('scala-base')
    }

    @CompileDynamic
    private FileCollection resolveSrcDirs(Project project, FileCollection files) {
        try {
            if (project.sourceSets.main) {
                if (hasGroovyPlugin(project)) {
                    return project.files(project.files(files,
                        project.sourceSets.main.groovy.srcDirs,
                        project.sourceSets.main.java.srcDirs).files.findAll { file ->
                        file.exists()
                    })
                } else if (hasScalaPlugin(project)) {
                    return project.files(project.files(files,
                        project.sourceSets.main.scala.srcDirs,
                        project.sourceSets.main.java.srcDirs).files.findAll { file ->
                        file.exists()
                    })
                } else if (hasKotlinPlugin(project)) {
                    return project.files(project.files(files,
                        project.sourceSets.main.kotlin.srcDirs,
                        project.sourceSets.main.java.srcDirs).files.findAll { file ->
                        file.exists()
                    })
                } else if (hasJavaPlugin(project)) {
                    return project.files(
                        project.files(files,
                            project.sourceSets.main.java.srcDirs).files.findAll { file ->
                            file.exists()
                        })
                }
            }
        } catch (Exception ignored) {
            // ignore this project
            return project.objects.fileCollection()
        }

        files
    }
}
