/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
package org.kordamp.gradle.plugin.test

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestReport
import org.gradle.api.tasks.testing.TestResult
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.test.tasks.FunctionalTest
import org.kordamp.gradle.plugin.test.tasks.IntegrationTest

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 *
 * @author Andres Almiray
 * @since 0.14.0
 */
@CompileStatic
class TestPlugin extends AbstractKordampPlugin {
    private static final boolean WINDOWS = System.getProperty('os.name').startsWith('Windows')

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
        if (!project.plugins.findPlugin(TestPlugin)) {
            project.plugins.apply(TestPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)

        project.afterEvaluate {
            ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
            setEnabled(effectiveConfig.test.enabled)

            if (!enabled) {
                return
            }

            project.tasks.withType(Test) { Test testTask ->
                if (!testTask.enabled) {
                    return
                }

                if (testTask instanceof IntegrationTest) {
                    configureLogging(testTask, effectiveConfig.test.integration.logging)
                    effectiveConfig.test.integrationTasks() << (IntegrationTest) testTask
                } else if (testTask instanceof FunctionalTest) {
                    configureLogging(testTask, effectiveConfig.test.functional.logging)
                    effectiveConfig.test.functionalTestTasks() << (FunctionalTest) testTask
                } else {
                    configureLogging(testTask, effectiveConfig.test.logging)
                    effectiveConfig.test.testTasks() << testTask
                }
            }
        }

        if (isRootProject(project) && !project.childProjects.isEmpty()) {
            TaskProvider<TestReport> aggregateTestReportTask = project.tasks.register('aggregateTestReports', TestReport,
                new Action<TestReport>() {
                    @Override
                    void execute(TestReport t) {
                        t.enabled = false
                        t.group = 'Verification'
                        t.description = 'Aggregate test reports.'
                        t.destinationDir = project.file("${project.buildDir}/reports/aggregate-tests")
                    }
                })

            TaskProvider<TestReport> aggregateIntegrationTestReportTask = project.tasks.register('aggregateIntegrationTestReports', TestReport,
                new Action<TestReport>() {
                    @Override
                    void execute(TestReport t) {
                        t.enabled = false
                        t.group = 'Verification'
                        t.description = 'Aggregate integration test reports.'
                        t.destinationDir = project.file("${project.buildDir}/reports/aggregate-integration-tests")
                    }
                })

            TaskProvider<TestReport> aggregateFunctionalTestReportTask = project.tasks.register('aggregateFunctionalTestReports', TestReport,
                new Action<TestReport>() {
                    @Override
                    void execute(TestReport t) {
                        t.enabled = false
                        t.group = 'Verification'
                        t.description = 'Aggregate functional test reports.'
                        t.destinationDir = project.file("${project.buildDir}/reports/aggregate-functional-tests")
                    }
                })

            TaskProvider<TestReport> aggregateAllTestReportTask = project.tasks.register('aggregateAllTestReports', TestReport,
                new Action<TestReport>() {
                    @Override
                    void execute(TestReport t) {
                        t.enabled = false
                        t.group = 'Verification'
                        t.description = 'Aggregate all test reports.'
                        t.destinationDir = project.file("${project.buildDir}/reports/aggregate-all-tests")
                    }
                })

            project.gradle.addBuildListener(new BuildAdapter() {
                @Override
                void projectsEvaluated(Gradle gradle) {
                    configureAggregateTestReportTasks(project,
                        aggregateTestReportTask,
                        aggregateIntegrationTestReportTask,
                        aggregateFunctionalTestReportTask,
                        aggregateAllTestReportTask)
                }
            })
        }
    }

    private void configureLogging(Test testTask, boolean logging) {
        if (!logging) return

        testTask.afterSuite { TestDescriptor descriptor, TestResult result ->
            String indicator = "\u001B[32m${WINDOWS ? '√' : '✔'}\u001b[0m".toString()
            if (result.failedTestCount > 0) {
                indicator = "\u001B[31m${WINDOWS ? 'X' : '✘'}\u001b[0m".toString()
            }

            String str = "${indicator} Test ${descriptor.name}; "
            str += "Executed: ${result.testCount}/\u001B[32m${result.successfulTestCount}\u001B[0m/"
            str += "\u001B[31m${result.failedTestCount}\u001B[0m/"
            str += "\u001B[33m${result.skippedTestCount}\u001B[0m  "
            project.logger.lifecycle(str.toString())
        }
    }

    private void configureAggregateTestReportTasks(Project project,
                                                   TaskProvider<TestReport> aggregateTestReportTask,
                                                   TaskProvider<TestReport> aggregateIntegrationTestReportTask,
                                                   TaskProvider<TestReport> aggregateFunctionalTestReportTask,
                                                   TaskProvider<TestReport> aggregateAllTestReportTask) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        if (!effectiveConfig.test.enabled) {
            return
        }

        Set<Test> tt = new LinkedHashSet<>(effectiveConfig.test.testTasks())
        Set<IntegrationTest> itt = new LinkedHashSet<>(effectiveConfig.test.integrationTasks())
        Set<FunctionalTest> ftt = new LinkedHashSet<>(effectiveConfig.test.functionalTestTasks())

        project.childProjects.values().each {
            org.kordamp.gradle.plugin.base.plugins.Test e = resolveEffectiveConfig(it).test
            if (e.enabled) {
                tt.addAll(e.testTasks())
                itt.addAll(e.integrationTasks())
                ftt.addAll(e.functionalTestTasks())
            }
        }

        aggregateTestReportTask.configure(new Action<TestReport>() {
            @Override
            @CompileDynamic
            void execute(TestReport t) {
                t.enabled = tt.size() > 0
                t.reportOn(tt)
            }
        })

        aggregateIntegrationTestReportTask.configure(new Action<TestReport>() {
            @Override
            @CompileDynamic
            void execute(TestReport t) {
                t.enabled = itt.size() > 0
                t.reportOn(itt)
            }
        })

        aggregateFunctionalTestReportTask.configure(new Action<TestReport>() {
            @Override
            @CompileDynamic
            void execute(TestReport t) {
                t.enabled = ftt.size() > 0
                t.reportOn(ftt)
            }
        })

        aggregateAllTestReportTask.configure(new Action<TestReport>() {
            @Override
            @CompileDynamic
            void execute(TestReport t) {
                t.enabled = tt.size() > 0 || itt.size() > 0 || ftt.size() > 0
                t.reportOn(tt + itt + ftt)
            }
        })
    }
}
