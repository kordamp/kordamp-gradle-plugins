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
package org.kordamp.gradle.plugin.sourcexref

import org.gradle.BuildAdapter
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.GroovyBasePlugin
import org.gradle.api.plugins.JavaBasePlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.SourceXref

import static org.kordamp.gradle.StringUtils.isBlank
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * @author Andres Almiray
 * @since 0.7.0
 */
class SourceXrefPlugin implements Plugin<Project> {
    private static final String VISITED = SourceXrefPlugin.class.name.replace('.', '_') + '_VISITED'

    static final String SOURCE_XREF_TASK_NAME = 'sourceXref'
    static final String AGGREGATE_SOURCE_XREF_TASK_NAME = 'aggregateSourceXref'

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
        if (!project.plugins.findPlugin(SourceXrefPlugin)) {
            project.plugins.apply(SourceXrefPlugin)
        }
    }

    private void configureProject(Project project) {
        String visitedPropertyName = VISITED + '_' + project.name
        if (project.findProperty(visitedPropertyName)) {
            return
        }
        project.ext[visitedPropertyName] = true

        BasePlugin.applyIfMissing(project)

        project.afterEvaluate {
            ProjectConfigurationExtension mergedConfiguration = project.ext.mergedConfiguration

            if (mergedConfiguration.sourceXref.enabled) {
                project.plugins.withType(JavaBasePlugin) {
                    JxrTask xrefTask = configureSourceXrefTask(project)
                    if (xrefTask.enabled) {
                        mergedConfiguration.sourceXref.projects() << project
                        mergedConfiguration.sourceXref.xrefTasks() << xrefTask
                    } else {
                        mergedConfiguration.sourceXref.enabled = false
                    }
                }
            }
        }

        if (isRootProject(project) && !project.childProjects.isEmpty()) {
            project.gradle.addBuildListener(new BuildAdapter() {
                @Override
                void projectsEvaluated(Gradle gradle) {
                    configureAggregateSourceXrefTask(project)
                }
            })
        }
    }

    private Task configureSourceXrefTask(Project project) {
        ProjectConfigurationExtension mergedConfiguration = project.ext.mergedConfiguration
        if (!mergedConfiguration.sourceXref.enabled) {
            return
        }

        Task classesTask = project.tasks.findByName('classes')
        if (!classesTask) {
            return project.tasks.create(SOURCE_XREF_TASK_NAME, DefaultTask) {
                enabled = false
                group 'Documentation'
                description 'Generates a JXR report of the source code'
            }
        }

        JxrTask jxrTask = project.tasks.create(SOURCE_XREF_TASK_NAME, JxrTask) {
            group 'Documentation'
            description 'Generates a JXR report of the source code'
            outputDirectory = project.file("${project.buildDir}/docs/source-xref")
            sourceDirs = resolveSrcDirs(project)
        }

        configureTask(mergedConfiguration.sourceXref, jxrTask)
    }

    private void configureAggregateSourceXrefTask(Project project) {
        ProjectConfigurationExtension mergedConfiguration = project.ext.mergedConfiguration

        Set<Project> projects = new LinkedHashSet<>()
        Set<Task> xrefTasks = new LinkedHashSet<>()
        FileCollection srcdirs = project.files()

        project.childProjects.values()*.mergedConfiguration.sourceXref.each { SourceXref e ->
            if (e.enabled) {
                projects.addAll(e.projects())
                xrefTasks.addAll(e.xrefTasks())
                srcdirs = project.files(srcdirs, e.xrefTasks()*.sourceDirs)
            }
        }

        JxrTask jxrTask = project.tasks.create(AGGREGATE_SOURCE_XREF_TASK_NAME, JxrTask) {
            dependsOn xrefTasks
            group 'Documentation'
            description 'Generates an aggregate JXR report of the source code'
            outputDirectory = project.file("${project.buildDir}/docs/source-xref")
            sourceDirs = srcdirs
        }

        configureTask(mergedConfiguration.sourceXref, jxrTask)
    }

    private JxrTask configureTask(SourceXref sourceXref, JxrTask jxrTask) {
        sourceXref.with {
            if (!isBlank(templateDir)) jxrTask.templateDir = templateDir
            if (!isBlank(inputEncoding)) jxrTask.inputEncoding = inputEncoding
            if (!isBlank(outputEncoding)) jxrTask.outputEncoding = outputEncoding
            if (!isBlank(windowTitle)) jxrTask.windowTitle = windowTitle
            if (!isBlank(docTitle)) jxrTask.docTitle = docTitle
            if (!isBlank(bottom)) jxrTask.bottom = bottom
            if (!isBlank(stylesheet)) jxrTask.stylesheet = stylesheet
            if (javaVersion) jxrTask.javaVersion = javaVersion
            if (excludes) jxrTask.excludes.addAll(excludes)
            if (includes) jxrTask.includes.addAll(includes)
        }

        jxrTask
    }

    private boolean hasJavaPlugin(Project project) {
        project.plugins.hasPlugin(JavaBasePlugin)
    }

    private boolean hasGroovyPlugin(Project project) {
        project.plugins.hasPlugin(GroovyBasePlugin)
    }

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
            return project.files()
        }
    }
}
