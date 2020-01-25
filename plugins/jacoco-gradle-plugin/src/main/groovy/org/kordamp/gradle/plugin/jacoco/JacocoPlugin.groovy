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
package org.kordamp.gradle.plugin.jacoco

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.file.FileCollection
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.testing.jacoco.tasks.JacocoReportsContainer
import org.kordamp.gradle.PluginUtils
import org.kordamp.gradle.StringUtils
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.Jacoco
import org.kordamp.gradle.plugin.base.plugins.Testing

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 *
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
class JacocoPlugin extends AbstractKordampPlugin {
    static final String AGGREGATE_JACOCO_MERGE_TASK_NAME = 'aggregateJacocoMerge'
    static final String AGGREGATE_JACOCO_REPORT_TASK_NAME = 'aggregateJacocoReport'

    Project project

    void apply(Project project) {
        this.project = project

        configureProject(project)
        if (isRootProject(project)) {
            configureRootProject(project)
            project.childProjects.values().each {
                it.pluginManager.apply(JacocoPlugin)
            }
        }
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

        project.pluginManager.withPlugin('java-base', new Action<AppliedPlugin>() {
            @Override
            void execute(AppliedPlugin appliedPlugin) {
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
                    ProjectConfigurationExtension config = resolveEffectiveConfig(project)
                    setEnabled(config.coverage.jacoco.enabled)

                    JacocoPluginExtension jacocoExt = project.extensions.findByType(JacocoPluginExtension)
                    jacocoExt.toolVersion = config.coverage.jacoco.toolVersion

                    project.pluginManager.withPlugin('java-base') {
                        // Do not aggregate root report if it does not have tests #198
                        if (isRootProject(project) && !config.coverage.jacoco.hasTestSourceSets()) {
                            return
                        }

                        Set<JacocoReport> reportTasks = []
                        project.tasks.withType(Test) { Test testTask ->
                            JacocoReport reportTask = configureJacocoReportTask(project, testTask)
                            reportTask.enabled &= testTask.enabled & config.coverage.jacoco.enabled
                            if (reportTask.enabled) {
                                config.coverage.jacoco.testTasks() << testTask
                                config.coverage.jacoco.reportTasks() << reportTask
                                config.coverage.jacoco.projects() << project
                                reportTasks << reportTask
                            }
                        }
                        if (allJacocoReports) {
                            configureAllJacocoReportsTask(project, allJacocoReports, reportTasks)
                        }
                    }
                }
            }
        })
    }

    private void configureRootProject(Project project) {
        TaskProvider<JacocoMerge> aggregateJacocoMerge = project.tasks.register(AGGREGATE_JACOCO_MERGE_TASK_NAME, JacocoMerge,
            new Action<JacocoMerge>() {
                @Override
                void execute(JacocoMerge t) {
                    t.enabled = false
                    t.group = 'Reporting'
                    t.description = 'Aggregate Jacoco coverage reports.'
                }
            })

        TaskProvider<JacocoReport> aggregateJacocoReport = project.tasks.register(AGGREGATE_JACOCO_REPORT_TASK_NAME, JacocoReport,
            new Action<JacocoReport>() {
                @Override
                void execute(JacocoReport t) {
                    t.enabled = false
                    t.dependsOn aggregateJacocoMerge
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
                applyJacocoMerge(project, aggregateJacocoMerge, aggregateJacocoReport)
            }
        })

        project.gradle.taskGraph.whenReady(new Action<TaskExecutionGraph>() {
            @Override
            void execute(TaskExecutionGraph graph) {
                configureAggregates(project, graph)
            }
        })
    }

    static String resolveJacocoReportTaskName(String name) {
        return 'jacoco' + StringUtils.capitalize(name) + 'Report'
    }

    @CompileDynamic
    private void configureAggregates(Project project, TaskExecutionGraph graph) {
        ProjectConfigurationExtension config = resolveEffectiveConfig(project)

        Set<Test> tt = new LinkedHashSet<>(config.testing.testTasks())
        Set<Test> itt = new LinkedHashSet<>(config.testing.integrationTasks())
        Set<Test> ftt = new LinkedHashSet<>(config.testing.functionalTestTasks())

        project.childProjects.values().each {
            Testing e = resolveEffectiveConfig(it).testing
            if (e.enabled) {
                tt.addAll(e.testTasks())
                itt.addAll(e.integrationTasks())
                ftt.addAll(e.functionalTestTasks())
            }
        }

        if (graph.hasTask(':' + AGGREGATE_JACOCO_MERGE_TASK_NAME)) {
            tt*.ignoreFailures = true
            itt*.ignoreFailures = true
            ftt*.ignoreFailures = true
        }
    }

    @CompileDynamic
    private static JacocoReport configureJacocoReportTask(Project project, Test testTask) {
        ProjectConfigurationExtension config = resolveEffectiveConfig(project)

        String taskName = resolveJacocoReportTaskName(testTask.name)

        Task jacocoReportTask = project.tasks.findByName(taskName)

        if (!jacocoReportTask) {
            jacocoReportTask = project.tasks.create(taskName, JacocoReport) {
                dependsOn testTask
                group = 'Verification'
                description = "Generates code coverage report for the ${testTask.name} task."

                jacocoClasspath = project.configurations.jacocoAnt

                additionalSourceDirs.from project.files(resolveSourceDirs(config, project))
                sourceDirectories.from project.files(resolveSourceDirs(config, project))
                classDirectories.from project.files(resolveClassDirs(config, project))
                executionData testTask

                reports {
                    html.destination = project.file("${project.reporting.baseDir.path}/jacoco/${testTask.name}/html")
                    xml.destination = project.file("${project.reporting.baseDir.path}/jacoco/${testTask.name}/jacocoTestReport.xml")
                }
            }
        }

        jacocoReportTask.configure {
            enabled = config.coverage.jacoco.enabled
            reports {
                xml.enabled = true
                csv.enabled = false
                html.enabled = true
            }
        }

        jacocoReportTask
    }

    private static FileCollection resolveSourceDirs(ProjectConfigurationExtension effectiveConfig, Project project) {
        project.files(PluginUtils.resolveMainSourceDirs(project))
            .from(effectiveConfig.coverage.jacoco.additionalSourceDirs.files.flatten().unique())
    }

    @CompileDynamic
    private static FileCollection resolveClassDirs(ProjectConfigurationExtension effectiveConfig, Project project) {
        project.files(PluginUtils.resolveSourceSets(project).main.output.classesDirs*.path.flatten().unique())
            .from(effectiveConfig.coverage.jacoco.additionalClassDirs.files.flatten().unique())
    }

    private FileCollection resolveSourceDirs(ProjectConfigurationExtension effectiveConfig, Collection<Project> projects) {
        project.files(projects.collect { resolveSourceDirs(effectiveConfig, it) }.flatten().unique())
    }

    private FileCollection resolveClassDirs(ProjectConfigurationExtension effectiveConfig, Collection<Project> projects) {
        project.files(projects.collect { resolveClassDirs(effectiveConfig, it) }.flatten().unique())
    }

    private void applyJacocoMerge(Project project,
                                  TaskProvider<JacocoMerge> aggregateJacocoMerge,
                                  TaskProvider<JacocoReport> aggregateJacocoReport) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        if (!effectiveConfig.coverage.jacoco.enabled) {
            return
        }

        JacocoPluginExtension jacocoExt = project.extensions.findByType(JacocoPluginExtension)
        jacocoExt.toolVersion = effectiveConfig.coverage.jacoco.toolVersion

        Set<Project> projects = new LinkedHashSet<>(effectiveConfig.coverage.jacoco.projects())
        Set<Test> testTasks = new LinkedHashSet<>(effectiveConfig.coverage.jacoco.testTasks())
        Set<JacocoReport> reportTasks = new LinkedHashSet<>(effectiveConfig.coverage.jacoco.reportTasks())

        project.childProjects.values().each {
            Jacoco e = resolveEffectiveConfig(it).coverage.jacoco
            if (e.enabled) {
                projects.addAll(e.projects())
                testTasks.addAll(e.testTasks())
                reportTasks.addAll(e.reportTasks())
            }
        }

        aggregateJacocoMerge.configure(new Action<JacocoMerge>() {
            @Override
            @CompileDynamic
            void execute(JacocoMerge t) {
                t.dependsOn testTasks + reportTasks
                t.enabled = effectiveConfig.coverage.jacoco.enabled
                t.executionData = project.files(reportTasks.executionData.files.flatten())
                t.destinationFile = effectiveConfig.coverage.jacoco.aggregateExecFile
            }
        })

        aggregateJacocoReport.configure(new Action<JacocoReport>() {
            @Override
            void execute(JacocoReport t) {
                t.enabled = effectiveConfig.coverage.jacoco.enabled

                t.additionalSourceDirs.from project.files(resolveSourceDirs(effectiveConfig, projects))
                t.sourceDirectories.from project.files(resolveSourceDirs(effectiveConfig, projects))
                t.classDirectories.from project.files(resolveClassDirs(effectiveConfig, projects))
                t.executionData project.files(aggregateJacocoMerge.get().destinationFile)

                t.reports(new Action<JacocoReportsContainer>() {
                    @Override
                    void execute(JacocoReportsContainer reports) {
                        reports.html.setDestination(effectiveConfig.coverage.jacoco.aggregateReportHtmlFile)
                        reports.xml.setDestination(effectiveConfig.coverage.jacoco.aggregateReportXmlFile)
                    }
                })
            }
        })
    }

    private static void configureAllJacocoReportsTask(Project project,
                                                      TaskProvider<DefaultTask> allJacocoReports,
                                                      Set<JacocoReport> reportTasks) {
        allJacocoReports.configure(new Action<DefaultTask>() {
            @Override
            void execute(DefaultTask t) {
                ProjectConfigurationExtension config = resolveEffectiveConfig(project)
                t.enabled = config.coverage.jacoco.enabled && reportTasks.size() > 0
                t.dependsOn(reportTasks)
            }
        })
    }
}
