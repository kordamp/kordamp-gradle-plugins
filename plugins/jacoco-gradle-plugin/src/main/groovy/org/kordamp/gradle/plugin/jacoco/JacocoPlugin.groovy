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
package org.kordamp.gradle.plugin.jacoco

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoMerge
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.testing.jacoco.tasks.JacocoReportsContainer
import org.kordamp.gradle.PluginUtils
import org.kordamp.gradle.StringUtils
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.Jacoco

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 *
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
class JacocoPlugin extends AbstractKordampPlugin {
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
            project.pluginManager.apply(JacocoPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        project.pluginManager.apply(org.gradle.testing.jacoco.plugins.JacocoPlugin)

        TaskProvider<DefaultTask> allJacocoReports = null
        if (project.childProjects.isEmpty()) {
            allJacocoReports = project.tasks.register('allJacocoReports', DefaultTask,
                    new Action<DefaultTask>() {
                        @Override
                        void execute(DefaultTask t) {
                            t.enabled = false
                            t.group = 'Verification'
                            t.description = 'Executes all JaCoCo reports.'
                        }
                    })
        }

        project.afterEvaluate {
            ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
            setEnabled(effectiveConfig.jacoco.enabled)

            if (!enabled) {
                return
            }

            project.pluginManager.withPlugin('java-base') {
                // Do not aggregate root report if it does not have tests #198
                if (isRootProject(project) && !effectiveConfig.jacoco.hasTestSourceSets()) {
                    return
                }

                Set<JacocoReport> reportTasks = []
                project.tasks.withType(Test) { Test testTask ->
                    if (!testTask.enabled) {
                        return
                    }
                    JacocoReport reportTask = configureJacocoReportTask(project, testTask)
                    if (reportTask.enabled) {
                        effectiveConfig.jacoco.testTasks() << testTask
                        effectiveConfig.jacoco.reportTasks() << reportTask
                        effectiveConfig.jacoco.projects() << project
                        reportTasks << reportTask
                    }
                }
                if (allJacocoReports) {
                    configureAllJacocoReportsTask(project, allJacocoReports, reportTasks)
                }
            }
        }

        if (isRootProject(project) && !project.childProjects.isEmpty()) {
            TaskProvider<JacocoMerge> jacocoRootMerge = project.tasks.register('jacocoRootMerge', JacocoMerge,
                new Action<JacocoMerge>() {
                    @Override
                    void execute(JacocoMerge t) {
                        t.enabled = false
                        t.group = 'Reporting'
                        t.description = 'Aggregate Jacoco coverage reports.'
                    }
                })

            TaskProvider<JacocoReport> jacocoRootReport = project.tasks.register('jacocoRootReport', JacocoReport,
                new Action<JacocoReport>() {
                    @Override
                    void execute(JacocoReport t) {
                        t.enabled = false
                        t.dependsOn jacocoRootMerge
                        t.group = 'Reporting'
                        t.description = 'Generate aggregate Jacoco coverage report.'

                        t.reports(new Action<JacocoReportsContainer>() {
                            @Override
                            void execute(JacocoReportsContainer reports) {
                               reports.html.enabled = true
                               reports.xml.enabled = true
                            }
                        })
                    }
                })

            project.gradle.addBuildListener(new BuildAdapter() {
                @Override
                void projectsEvaluated(Gradle gradle) {
                    applyJacocoMerge(project, jacocoRootMerge, jacocoRootReport)
                }
            })
        }
    }

    static String resolveJacocoReportTaskName(String name) {
        return 'jacoco' + StringUtils.capitalize(name) + 'Report'
    }

    @CompileDynamic
    private JacocoReport configureJacocoReportTask(Project project, Test testTask) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)

        String taskName = resolveJacocoReportTaskName(testTask.name)

        Task jacocoReportTask = project.tasks.findByName(taskName)

        if (!jacocoReportTask) {
            jacocoReportTask = project.tasks.create(taskName, JacocoReport) {
                dependsOn testTask
                group = 'Verification'
                description = "Generates code coverage report for the ${testTask.name} task."

                jacocoClasspath = project.configurations.jacocoAnt

                additionalSourceDirs.from project.files(resolveSourceDirs(effectiveConfig, project))
                sourceDirectories.from project.files(resolveSourceDirs(effectiveConfig, project))
                classDirectories.from project.files(resolveClassDirs(effectiveConfig, project))
                executionData testTask

                reports {
                    html.destination = project.file("${project.reporting.baseDir.path}/jacoco/${testTask.name}/html")
                    xml.destination = project.file("${project.reporting.baseDir.path}/jacoco/${testTask.name}/jacocoTestReport.xml")
                }
            }
        }

        jacocoReportTask.configure {
            enabled = effectiveConfig.jacoco.enabled
            reports {
                xml.enabled = true
                csv.enabled = false
                html.enabled = true
            }
        }

        jacocoReportTask
    }

    private FileCollection resolveSourceDirs(ProjectConfigurationExtension effectiveConfig, Project project) {
        project.files(PluginUtils.resolveMainSourceDirs(project))
            .from(effectiveConfig.jacoco.additionalSourceDirs.files.flatten().unique())
    }

    @CompileDynamic
    private FileCollection resolveClassDirs(ProjectConfigurationExtension effectiveConfig, Project project) {
        project.files(PluginUtils.resolveSourceSets(project).main.output.classesDirs*.path.flatten().unique())
            .from(effectiveConfig.jacoco.additionalClassDirs.files.flatten().unique())
    }

    private FileCollection resolveSourceDirs(ProjectConfigurationExtension effectiveConfig, Collection<Project> projects) {
        project.files(projects.collect { resolveSourceDirs(effectiveConfig, it) }.flatten().unique())
    }

    private FileCollection resolveClassDirs(ProjectConfigurationExtension effectiveConfig, Collection<Project> projects) {
        project.files(projects.collect { resolveClassDirs(effectiveConfig, it) }.flatten().unique())
    }

    private void applyJacocoMerge(Project project, TaskProvider<JacocoMerge> jacocoRootMerge, TaskProvider<JacocoReport> jacocoRootReport) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        if (!effectiveConfig.jacoco.enabled) {
            return
        }

        Set<Project> projects = new LinkedHashSet<>(effectiveConfig.jacoco.projects())
        Set<Test> testTasks = new LinkedHashSet<>(effectiveConfig.jacoco.testTasks())
        Set<JacocoReport> reportTasks = new LinkedHashSet<>(effectiveConfig.jacoco.reportTasks())

        project.childProjects.values().each {
            Jacoco e = resolveEffectiveConfig(it).jacoco
            if (e.enabled) {
                projects.addAll(e.projects())
                testTasks.addAll(e.testTasks())
                reportTasks.addAll(e.reportTasks())
            }
        }

        jacocoRootMerge.configure(new Action<JacocoMerge>() {
            @Override
            @CompileDynamic
            void execute(JacocoMerge t) {
                t.dependsOn testTasks + reportTasks
                t.enabled = effectiveConfig.jacoco.enabled
                t.executionData = project.files(reportTasks.executionData.files.flatten())
                t.destinationFile = effectiveConfig.jacoco.mergeExecFile
            }
        })

        jacocoRootReport.configure(new Action<JacocoReport>() {
            @Override
            void execute(JacocoReport t) {
                t.enabled = effectiveConfig.jacoco.enabled

                t.additionalSourceDirs.from project.files(resolveSourceDirs(effectiveConfig, projects))
                t.sourceDirectories.from project.files(resolveSourceDirs(effectiveConfig, projects))
                t.classDirectories.from project.files(resolveClassDirs(effectiveConfig, projects))
                t.executionData project.files(jacocoRootMerge.get().destinationFile)

                t.reports(new Action<JacocoReportsContainer>() {
                    @Override
                    void execute(JacocoReportsContainer reports) {
                        reports.html.setDestination(effectiveConfig.jacoco.mergeReportHtmlFile)
                        reports.xml.setDestination(effectiveConfig.jacoco.mergeReportXmlFile)
                    }
                })
            }
        })
    }

    private void configureAllJacocoReportsTask(Project project,
                                        TaskProvider<DefaultTask> allJacocoReports,
                                               Set<JacocoReport> reportTasks) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        if (!effectiveConfig.jacoco.enabled) {
            return
        }

        allJacocoReports.configure(new Action<DefaultTask>() {
            @Override
            void execute(DefaultTask t) {
                t.enabled = reportTasks.size() > 0
                t.dependsOn(reportTasks)
            }
        })
    }
}
