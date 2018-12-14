/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 Andres Almiray.
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
package org.kordamp.gradle.plugin.sourcehtml

import com.bmuschko.gradle.java2html.ConvertCodeTask
import com.bmuschko.gradle.java2html.GenerateOverviewTask
import org.gradle.BuildAdapter
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.FileCollection
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.GroovyBasePlugin
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.Copy
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.SourceHtml

import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
class SourceHtmlPlugin extends AbstractKordampPlugin {
    static final String AGGREGATE_SOURCE_HTML_TASK_NAME = 'aggregateSourceHtml'
    static final String AGGREGATE_CONVERT_CODE_TO_HTML_TASK_NAME = 'aggregateConvertCodeToHtml'
    static final String AGGREGATE_GENERATE_SOURCE_HTML_OVERVIEW_TASK_NAME = 'aggregateGenerateSourceHtmlOverview'
    static final String SOURCE_HTML_TASK_NAME = 'sourceHtml'
    static final String CONFIGURATION_NAME = 'java2html'

    Project project

    void apply(Project project) {
        this.project = project

        if (isRootProject(project)) {
            project.childProjects.values().each {
                configureProject(it)
            }
        }
        configureProject(project)
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(SourceHtmlPlugin)) {
            project.plugins.apply(SourceHtmlPlugin)
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
        project.afterEvaluate {
            ProjectConfigurationExtension mergedConfiguration = project.ext.mergedConfiguration

            if (mergedConfiguration.sourceHtml.enabled) {
                project.plugins.withType(JavaBasePlugin) {
                    if (configureSourceHtmlTask(project, configuration).enabled) {
                        mergedConfiguration.sourceHtml.projects() << project
                    } else {
                        mergedConfiguration.sourceHtml.enabled = false
                    }
                }
            }
            setEnabled(mergedConfiguration.sourceHtml.enabled)
        }

        if (isRootProject(project) && !project.childProjects.isEmpty()) {
            project.gradle.addBuildListener(new BuildAdapter() {
                @Override
                void projectsEvaluated(Gradle gradle) {
                    configureAggregateSourceHtmlTask(project, configuration)
                }
            })
        }
    }

    private Task configureSourceHtmlTask(Project project, Configuration configuration) {
        ProjectConfigurationExtension mergedConfiguration = project.ext.mergedConfiguration
        if (!mergedConfiguration.sourceHtml.enabled) {
            return
        }

        Task classesTask = project.tasks.findByName('classes')
        if (!classesTask) {
            return project.tasks.create(SOURCE_HTML_TASK_NAME, DefaultTask) {
                enabled = false
                group 'Documentation'
                description 'Generates a HTML report of the source code'
            }
        }

        mergedConfiguration.sourceHtml.srcDirs = resolveSrcDirs(project, mergedConfiguration.sourceHtml.conversion.srcDirs)

        Task convertCodeTask = project.tasks.create('convertCodeToHtml', ConvertCodeTask) {
            enabled = !mergedConfiguration.sourceHtml.srcDirs.isEmpty()
            dependsOn classesTask
            group 'Documentation'
            description 'Converts source code into HTML'
            classpath = configuration.asFileTree
            srcDirs = mergedConfiguration.sourceHtml.srcDirs
            destDir = mergedConfiguration.sourceHtml.conversion.destDir
            includes = mergedConfiguration.sourceHtml.conversion.includes
            outputFormat = mergedConfiguration.sourceHtml.conversion.outputFormat
            tabs = mergedConfiguration.sourceHtml.conversion.tabs
            style = mergedConfiguration.sourceHtml.conversion.style
            showLineNumbers = mergedConfiguration.sourceHtml.conversion.showLineNumbers
            showFileName = mergedConfiguration.sourceHtml.conversion.showFileName
            showDefaultTitle = mergedConfiguration.sourceHtml.conversion.showDefaultTitle
            showTableBorder = mergedConfiguration.sourceHtml.conversion.showTableBorder
            includeDocumentHeader = mergedConfiguration.sourceHtml.conversion.includeDocumentHeader
            includeDocumentFooter = mergedConfiguration.sourceHtml.conversion.includeDocumentFooter
            addLineAnchors = mergedConfiguration.sourceHtml.conversion.addLineAnchors
            lineAnchorPrefix = mergedConfiguration.sourceHtml.conversion.lineAnchorPrefix
            horizontalAlignment = mergedConfiguration.sourceHtml.conversion.horizontalAlignment
            useShortFileName = mergedConfiguration.sourceHtml.conversion.useShortFileName
            overwrite = mergedConfiguration.sourceHtml.conversion.overwrite
        }

        Task generateOverviewTask = project.tasks.create('generateSourceHtmlOverview', GenerateOverviewTask) {
            enabled = !mergedConfiguration.sourceHtml.srcDirs.isEmpty()
            dependsOn convertCodeTask
            group 'Documentation'
            description 'Generate an overview of converted source code'
            srcDirs = project.files(mergedConfiguration.sourceHtml.conversion.destDir)
            destDir = mergedConfiguration.sourceHtml.overview.destDir
            pattern = mergedConfiguration.sourceHtml.overview.pattern
            windowTitle = mergedConfiguration.sourceHtml.overview.windowTitle
            docTitle = mergedConfiguration.sourceHtml.overview.docTitle
            docDescription = mergedConfiguration.sourceHtml.overview.docDescription ?: ''
            icon = mergedConfiguration.sourceHtml.overview.icon
            stylesheet = mergedConfiguration.sourceHtml.overview.stylesheet
        }

        project.tasks.create(SOURCE_HTML_TASK_NAME, Copy) {
            enabled = !mergedConfiguration.sourceHtml.srcDirs.isEmpty()
            dependsOn generateOverviewTask
            group 'Documentation'
            description 'Generates a HTML report of the source code'
            destinationDir = project.file("${project.buildDir}/docs/source-html")
            from convertCodeTask.destDir
            from generateOverviewTask.destDir
        }
    }

    private void configureAggregateSourceHtmlTask(Project project, Configuration configuration) {
        ProjectConfigurationExtension mergedConfiguration = project.ext.mergedConfiguration

        Set<Project> projects = new LinkedHashSet<>()
        FileCollection srcdirs = project.files()

        project.childProjects.values()*.mergedConfiguration.sourceHtml.each { SourceHtml e ->
            if (e.enabled) {
                projects.addAll(e.projects())
                srcdirs = project.files(srcdirs, e.srcDirs)
            }
        }

        Task convertCodeTask = project.tasks.create(AGGREGATE_CONVERT_CODE_TO_HTML_TASK_NAME, ConvertCodeTask) {
            dependsOn projects.sourceHtml
            group 'Documentation'
            description 'Converts source code into HTML'
            classpath = configuration.asFileTree
            srcDirs = srcdirs
            destDir = mergedConfiguration.sourceHtml.conversion.destDir
            includes = mergedConfiguration.sourceHtml.conversion.includes
            outputFormat = mergedConfiguration.sourceHtml.conversion.outputFormat
            tabs = mergedConfiguration.sourceHtml.conversion.tabs
            style = mergedConfiguration.sourceHtml.conversion.style
            showLineNumbers = mergedConfiguration.sourceHtml.conversion.showLineNumbers
            showFileName = mergedConfiguration.sourceHtml.conversion.showFileName
            showDefaultTitle = mergedConfiguration.sourceHtml.conversion.showDefaultTitle
            showTableBorder = mergedConfiguration.sourceHtml.conversion.showTableBorder
            includeDocumentHeader = mergedConfiguration.sourceHtml.conversion.includeDocumentHeader
            includeDocumentFooter = mergedConfiguration.sourceHtml.conversion.includeDocumentFooter
            addLineAnchors = mergedConfiguration.sourceHtml.conversion.addLineAnchors
            lineAnchorPrefix = mergedConfiguration.sourceHtml.conversion.lineAnchorPrefix
            horizontalAlignment = mergedConfiguration.sourceHtml.conversion.horizontalAlignment
            useShortFileName = mergedConfiguration.sourceHtml.conversion.useShortFileName
            overwrite = mergedConfiguration.sourceHtml.conversion.overwrite
        }

        Task generateOverviewTask = project.tasks.create(AGGREGATE_GENERATE_SOURCE_HTML_OVERVIEW_TASK_NAME, GenerateOverviewTask) {
            dependsOn convertCodeTask
            group 'Documentation'
            description 'Generate an overview of converted source code'
            srcDirs = project.files(mergedConfiguration.sourceHtml.conversion.destDir)
            destDir = mergedConfiguration.sourceHtml.overview.destDir
            pattern = mergedConfiguration.sourceHtml.overview.pattern
            windowTitle = mergedConfiguration.sourceHtml.overview.windowTitle
            docTitle = mergedConfiguration.sourceHtml.overview.docTitle
            docDescription = mergedConfiguration.sourceHtml.overview.docDescription ?: ''
            icon = mergedConfiguration.sourceHtml.overview.icon
            stylesheet = mergedConfiguration.sourceHtml.overview.stylesheet
        }

        project.tasks.create(AGGREGATE_SOURCE_HTML_TASK_NAME, Copy) {
            dependsOn generateOverviewTask
            group 'Documentation'
            description 'Generates a HTML report of the source code'
            destinationDir = project.file("${project.buildDir}/docs/source-html")
            from convertCodeTask.destDir
            from generateOverviewTask.destDir
        }
    }

    private boolean hasJavaPlugin(Project project) {
        project.plugins.hasPlugin(JavaBasePlugin)
    }

    private boolean hasGroovyPlugin(Project project) {
        project.plugins.hasPlugin(GroovyBasePlugin)
    }

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
