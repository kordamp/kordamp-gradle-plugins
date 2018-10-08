/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 the original author or authors.
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
package org.kordamp.gradle.plugin.jacoco

import org.gradle.BuildAdapter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoMerge
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.kordamp.gradle.StringUtils
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.Jacoco

import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 *
 * @author Andres Almiray
 * @since 0.2.0
 */
class JacocoPlugin implements Plugin<Project> {
    private static final String VISITED = JacocoPlugin.class.name.replace('.', '_') + '_VISITED'

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
        if (!project.plugins.findPlugin(JacocoPlugin)) {
            project.plugins.apply(JacocoPlugin)
        }
    }

    private void configureProject(Project project) {
        String visitedPropertyName = VISITED + '_' + project.name
        if (project.findProperty(visitedPropertyName)) {
            return
        }
        project.ext[visitedPropertyName] = true

        BasePlugin.applyIfMissing(project)
        project.plugins.apply(org.gradle.testing.jacoco.plugins.JacocoPlugin)

        project.afterEvaluate {
            ProjectConfigurationExtension mergedConfiguration = project.ext.mergedConfiguration
            if (!mergedConfiguration.jacoco.enabled) {
                return
            }

            project.plugins.withType(JavaBasePlugin) {
                project.tasks.withType(Test) { Test testTask ->
                    Task reportTask = configureJacocoReportTask(project, testTask)
                    if (reportTask.enabled) {
                        mergedConfiguration.jacoco.testTasks() << testTask
                        mergedConfiguration.jacoco.reportTasks() << reportTask
                        mergedConfiguration.jacoco.projects() << project
                    }
                }
            }
        }

        if (isRootProject(project) && !project.childProjects.isEmpty()) {
            project.gradle.addBuildListener(new BuildAdapter() {
                @Override
                void projectsEvaluated(Gradle gradle) {
                    applyJacocoMerge(project)
                }
            })
        }
    }

    static String resolveJacocoReportTaskName(String name) {
        return 'jacoco' + StringUtils.capitalize(name) + 'Report'
    }

    private Task configureJacocoReportTask(Project project, Test testTask) {
        ProjectConfigurationExtension mergedConfiguration = project.ext.mergedConfiguration

        String taskName = resolveJacocoReportTaskName(testTask.name)

        Task jacocoReportTask = project.tasks.findByName(taskName)

        if (!jacocoReportTask) {
            jacocoReportTask = project.tasks.create(taskName, JacocoReport) {
                dependsOn testTask
                group 'Verification'
                description "Generates code coverage report for the ${testTask.name} task."

                jacocoClasspath = project.configurations.jacocoAnt

                additionalSourceDirs = project.files(project.sourceSets.main.allSource.srcDirs)
                sourceDirectories = project.files(project.sourceSets.main.allSource.srcDirs)
                classDirectories = project.files(project.sourceSets.main.output.classesDirs*.path)
                executionData testTask

                reports {
                    html.destination = project.file("${project.reporting.baseDir.path}/jacoco/${testTask.name}/html")
                    xml.destination = project.file("${project.reporting.baseDir.path}/jacoco/${testTask.name}/jacocoTestReport.xml")
                }
            }
        }

        jacocoReportTask.configure {
            enabled = mergedConfiguration.jacoco.enabled
            reports {
                xml.enabled = true
                csv.enabled = false
                html.enabled = true
            }
        }

        jacocoReportTask
    }

    private void applyJacocoMerge(Project project) {
        ProjectConfigurationExtension mergedConfiguration = project.ext.mergedConfiguration
        if (!mergedConfiguration.jacoco.enabled) {
            return
        }

        Set<Project> projects = new LinkedHashSet<>(mergedConfiguration.jacoco.projects())
        Set<Test> testTasks = new LinkedHashSet<>(mergedConfiguration.jacoco.testTasks())
        Set<JacocoReport> reportTasks = new LinkedHashSet<>(mergedConfiguration.jacoco.reportTasks())

        project.childProjects.values()*.mergedConfiguration.jacoco.each { Jacoco e ->
            if (e.enabled) {
                projects.addAll(e.projects())
                testTasks.addAll(e.testTasks())
                reportTasks.addAll(e.reportTasks())
            }
        }

        Task jacocoRootMerge = project.tasks.create('jacocoRootMerge', JacocoMerge) {
            enabled = mergedConfiguration.jacoco.enabled
            group = 'Reporting'
            description = 'Aggregate Jacoco coverage reports.'
            dependsOn testTasks + reportTasks
            executionData reportTasks.executionData.files.flatten()
            destinationFile mergedConfiguration.jacoco.mergeExecFile
        }

        project.tasks.create('jacocoRootReport', JacocoReport) {
            dependsOn jacocoRootMerge
            enabled = mergedConfiguration.jacoco.enabled
            group = 'Reporting'
            description = 'Generate aggregate Jacoco coverage report.'

            additionalSourceDirs = project.files(projects.sourceSets.main.allSource.srcDirs)
            sourceDirectories = project.files(projects.sourceSets.main.allSource.srcDirs)
            classDirectories = project.files(projects.sourceSets.main.output)
            executionData project.files(jacocoRootMerge.destinationFile)

            reports {
                html.enabled = true
                xml.enabled = true
                html.destination = mergedConfiguration.jacoco.mergeReportHtmlFile
                xml.destination = mergedConfiguration.jacoco.mergeReportXmlFile
            }
        }
    }
}
