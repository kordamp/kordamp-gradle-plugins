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
package org.kordamp.gradle.plugin.sourcehtml

import com.bmuschko.gradle.java2html.ConvertCodeTask
import com.bmuschko.gradle.java2html.GenerateOverviewTask
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.FileCollection
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.SourceHtml

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

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
            project.afterEvaluate {
                ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)

                if (effectiveConfig.docs.sourceHtml.enabled) {
                    if (configureSourceHtmlTask(project, configuration)) {
                        effectiveConfig.docs.sourceHtml.projects() << project
                    } else {
                        effectiveConfig.docs.sourceHtml.enabled = false
                    }
                }
                setEnabled(effectiveConfig.docs.sourceHtml.enabled)
            }
        }
    }

    private void configureRootProject(Project project) {
        TaskProvider<Copy> sourceHtmlTask = project.tasks.register(AGGREGATE_SOURCE_HTML_TASK_NAME, Copy,
            new Action<Copy>() {
                @Override
                void execute(Copy t) {
                    t.group = 'Documentation'
                    t.description = 'Generates a HTML report of the source code.'
                    t.destinationDir = project.file("${project.buildDir}/docs/source-html")
                    t.enabled = false
                }
            })

        TaskProvider<Jar> sourceHtmlJarTask = project.tasks.register(AGGREGATE_SOURCE_HTML_TASK_NAME + 'Jar', Jar,
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

        project.gradle.addBuildListener(new BuildAdapter() {
            @Override
            void projectsEvaluated(Gradle gradle) {
                Configuration configuration = project.configurations.findByName(CONFIGURATION_NAME)
                configureAggregateSourceHtmlTask(project, configuration, sourceHtmlTask, sourceHtmlJarTask)
            }
        })
    }

    private boolean configureSourceHtmlTask(Project project, Configuration configuration) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        if (!effectiveConfig.docs.sourceHtml.enabled) {
            return false
        }

        effectiveConfig.docs.sourceHtml.srcDirs = resolveSrcDirs(project, effectiveConfig.docs.sourceHtml.conversion.srcDirs)

        TaskProvider<ConvertCodeTask> convertCodeTask = project.tasks.register('convertCodeToHtml', ConvertCodeTask,
            new Action<ConvertCodeTask>() {
                @Override
                void execute(ConvertCodeTask t) {
                    t.enabled = !effectiveConfig.docs.sourceHtml.srcDirs.isEmpty()
                    t.dependsOn project.tasks.named('classes')
                    t.group = 'Documentation'
                    t.description = 'Converts source code into HTML.'
                    t.classpath = configuration.asFileTree
                    t.srcDirs = effectiveConfig.docs.sourceHtml.srcDirs
                    t.destDir = effectiveConfig.docs.sourceHtml.conversion.destDir
                    t.includes = effectiveConfig.docs.sourceHtml.conversion.includes
                    t.outputFormat = effectiveConfig.docs.sourceHtml.conversion.outputFormat
                    t.tabs = effectiveConfig.docs.sourceHtml.conversion.tabs
                    t.style = effectiveConfig.docs.sourceHtml.conversion.style
                    t.showLineNumbers = effectiveConfig.docs.sourceHtml.conversion.showLineNumbers
                    t.showFileName = effectiveConfig.docs.sourceHtml.conversion.showFileName
                    t.showDefaultTitle = effectiveConfig.docs.sourceHtml.conversion.showDefaultTitle
                    t.showTableBorder = effectiveConfig.docs.sourceHtml.conversion.showTableBorder
                    t.includeDocumentHeader = effectiveConfig.docs.sourceHtml.conversion.includeDocumentHeader
                    t.includeDocumentFooter = effectiveConfig.docs.sourceHtml.conversion.includeDocumentFooter
                    t.addLineAnchors = effectiveConfig.docs.sourceHtml.conversion.addLineAnchors
                    t.lineAnchorPrefix = effectiveConfig.docs.sourceHtml.conversion.lineAnchorPrefix
                    t.horizontalAlignment = effectiveConfig.docs.sourceHtml.conversion.horizontalAlignment
                    t.useShortFileName = effectiveConfig.docs.sourceHtml.conversion.useShortFileName
                    t.overwrite = effectiveConfig.docs.sourceHtml.conversion.overwrite
                }
            })

        TaskProvider<GenerateOverviewTask> generateOverviewTask = project.tasks.register('generateSourceHtmlOverview', GenerateOverviewTask,
            new Action<GenerateOverviewTask>() {
                @Override
                void execute(GenerateOverviewTask t) {
                    t.enabled = !effectiveConfig.docs.sourceHtml.srcDirs.isEmpty()
                    t.dependsOn convertCodeTask
                    t.group = 'Documentation'
                    t.description = 'Generate an overview of converted source code.'
                    t.srcDirs = project.files(effectiveConfig.docs.sourceHtml.conversion.destDir)
                    t.destDir = effectiveConfig.docs.sourceHtml.overview.destDir
                    t.pattern = effectiveConfig.docs.sourceHtml.overview.pattern
                    t.windowTitle = effectiveConfig.docs.sourceHtml.overview.windowTitle
                    t.docTitle = effectiveConfig.docs.sourceHtml.overview.docTitle
                    t.docDescription = effectiveConfig.docs.sourceHtml.overview.docDescription ?: ''
                    t.icon = effectiveConfig.docs.sourceHtml.overview.icon
                    t.stylesheet = effectiveConfig.docs.sourceHtml.overview.stylesheet
                }
            })

        TaskProvider<Copy> sourceHtmlTask = project.tasks.register(SOURCE_HTML_TASK_NAME, Copy,
            new Action<Copy>() {
                @Override
                void execute(Copy t) {
                    t.enabled = !effectiveConfig.docs.sourceHtml.srcDirs.isEmpty()
                    t.dependsOn generateOverviewTask
                    t.group = 'Documentation'
                    t.description = 'Generates a HTML report of the source code.'
                    t.destinationDir = project.file("${project.buildDir}/docs/source-html")
                    t.from convertCodeTask.get().destDir
                    t.from generateOverviewTask.get().destDir
                }
            })

        project.tasks.register(SOURCE_HTML_TASK_NAME + 'Jar', Jar,
            new Action<Jar>() {
                @Override
                void execute(Jar t) {
                    t.enabled = !effectiveConfig.docs.sourceHtml.srcDirs.isEmpty()
                    t.dependsOn sourceHtmlTask
                    t.group = 'Documentation'
                    t.description = 'An archive of the HTML report the source code.'
                    t.archiveClassifier.set 'sources-html'
                    t.from sourceHtmlTask.get().destinationDir
                }
            })

        return !effectiveConfig.docs.sourceHtml.srcDirs.isEmpty()
    }

    private void configureAggregateSourceHtmlTask(Project project, Configuration configuration, TaskProvider<Copy> sourceHtmlTask, TaskProvider<Jar> sourceHtmlJarTask) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)

        Set<Project> projects = new LinkedHashSet<>()
        FileCollection srcdirs = project.files()
        List<Task> sourceHtmlTasks = []

        project.childProjects.values().each {
            SourceHtml e = resolveEffectiveConfig(it).docs.sourceHtml
            if (!e.enabled || effectiveConfig.docs.sourceHtml.excludedProjects().intersect(e.projects())) return
            projects.addAll(e.projects())
            srcdirs = project.files(srcdirs, e.srcDirs)
        }

        projects.each { p ->
            sourceHtmlTasks << p.tasks.findByName('sourceHtml')
        }

        TaskProvider<ConvertCodeTask> convertCodeTask = project.tasks.register(AGGREGATE_CONVERT_CODE_TO_HTML_TASK_NAME, ConvertCodeTask,
            new Action<ConvertCodeTask>() {
                @Override
                void execute(ConvertCodeTask t) {
                    t.dependsOn sourceHtmlTasks
                    t.group = 'Documentation'
                    t.description = 'Converts source code into HTML.'
                    t.classpath = configuration.asFileTree
                    t.srcDirs = srcdirs
                    t.destDir = effectiveConfig.docs.sourceHtml.conversion.destDir
                    t.includes = effectiveConfig.docs.sourceHtml.conversion.includes
                    t.outputFormat = effectiveConfig.docs.sourceHtml.conversion.outputFormat
                    t.tabs = effectiveConfig.docs.sourceHtml.conversion.tabs
                    t.style = effectiveConfig.docs.sourceHtml.conversion.style
                    t.showLineNumbers = effectiveConfig.docs.sourceHtml.conversion.showLineNumbers
                    t.showFileName = effectiveConfig.docs.sourceHtml.conversion.showFileName
                    t.showDefaultTitle = effectiveConfig.docs.sourceHtml.conversion.showDefaultTitle
                    t.showTableBorder = effectiveConfig.docs.sourceHtml.conversion.showTableBorder
                    t.includeDocumentHeader = effectiveConfig.docs.sourceHtml.conversion.includeDocumentHeader
                    t.includeDocumentFooter = effectiveConfig.docs.sourceHtml.conversion.includeDocumentFooter
                    t.addLineAnchors = effectiveConfig.docs.sourceHtml.conversion.addLineAnchors
                    t.lineAnchorPrefix = effectiveConfig.docs.sourceHtml.conversion.lineAnchorPrefix
                    t.horizontalAlignment = effectiveConfig.docs.sourceHtml.conversion.horizontalAlignment
                    t.useShortFileName = effectiveConfig.docs.sourceHtml.conversion.useShortFileName
                    t.overwrite = effectiveConfig.docs.sourceHtml.conversion.overwrite
                }
            })

        TaskProvider<GenerateOverviewTask> generateOverviewTask = project.tasks.register(AGGREGATE_GENERATE_SOURCE_HTML_OVERVIEW_TASK_NAME, GenerateOverviewTask,
            new Action<GenerateOverviewTask>() {
                @Override
                void execute(GenerateOverviewTask t) {
                    t.dependsOn convertCodeTask
                    t.group = 'Documentation'
                    t.description = 'Generate an overview of converted source code.'
                    t.srcDirs = project.files(effectiveConfig.docs.sourceHtml.conversion.destDir)
                    t.destDir = effectiveConfig.docs.sourceHtml.overview.destDir
                    t.pattern = effectiveConfig.docs.sourceHtml.overview.pattern
                    t.windowTitle = effectiveConfig.docs.sourceHtml.overview.windowTitle
                    t.docTitle = effectiveConfig.docs.sourceHtml.overview.docTitle
                    t.docDescription = effectiveConfig.docs.sourceHtml.overview.docDescription ?: ''
                    t.icon = effectiveConfig.docs.sourceHtml.overview.icon
                    t.stylesheet = effectiveConfig.docs.sourceHtml.overview.stylesheet
                }
            })

        sourceHtmlTask.configure(new Action<Copy>() {
            @Override
            void execute(Copy t) {
                t.dependsOn generateOverviewTask
                t.from convertCodeTask.get().destDir
                t.from generateOverviewTask.get().destDir
                t.enabled = true
            }
        })

        sourceHtmlJarTask.configure(new Action<Jar>() {
            @Override
            void execute(Jar t) {
                t.enabled = true
            }
        })
    }

    private boolean hasJavaPlugin(Project project) {
        project.pluginManager.hasPlugin('java-base')
    }

    private boolean hasGroovyPlugin(Project project) {
        project.pluginManager.hasPlugin('groovy-base')
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
            return project.files()
        }

        files
    }
}
