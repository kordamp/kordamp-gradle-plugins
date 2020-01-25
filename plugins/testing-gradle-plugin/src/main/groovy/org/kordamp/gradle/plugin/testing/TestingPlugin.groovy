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
package org.kordamp.gradle.plugin.testing

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestReport
import org.gradle.api.tasks.testing.TestResult
import org.kordamp.gradle.AnsiConsole
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.Testing
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
class TestingPlugin extends AbstractKordampPlugin {
    static final String AGGREGATE_TEST_REPORTS_TASK_NAME = 'aggregateTestReports'
    static final String AGGREGATE_INTEGRATION_TEST_REPORTS_TASK_NAME = 'aggregateIntegrationTestReports'
    static final String AGGREGATE_FUNCTIONAL_TEST_REPORTS_TASK_NAME = 'aggregateFunctionalTestReports'
    static final String AGGREGATE_ALL_TEST_REPORTS_TASK_NAME = 'aggregateAllTestReports'
    static final String ALL_TESTS_TASK_NAME = 'allTests'

    private static final boolean WINDOWS = System.getProperty('os.name').startsWith('Windows')

    Project project

    void apply(Project project) {
        this.project = project

        configureProject(project)
        if (isRootProject(project)) {
            configureRootProject(project)
            project.childProjects.values().each {
                it.pluginManager.apply(TestingPlugin)
            }
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(TestingPlugin)) {
            project.pluginManager.apply(TestingPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)

        project.pluginManager.withPlugin('java-base', new Action<AppliedPlugin>() {
            @Override
            void execute(AppliedPlugin appliedPlugin) {
                TaskProvider<DefaultTask> allTestsTask = null
                if (project.childProjects.isEmpty()) {
                    allTestsTask = project.tasks.register(ALL_TESTS_TASK_NAME, DefaultTask,
                        new Action<DefaultTask>() {
                            @Override
                            void execute(DefaultTask t) {
                                t.enabled = false
                                t.group = 'Verification'
                                t.description = 'Executes all tests.'
                            }
                        })
                }

                project.afterEvaluate {
                    ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
                    setEnabled(effectiveConfig.testing.enabled)

                    if (!enabled) {
                        return
                    }

                    project.tasks.withType(Test) { Test testTask ->
                        if (!testTask.enabled) {
                            return
                        }

                        if (testTask instanceof IntegrationTest) {
                            configureLogging(testTask, effectiveConfig.testing.integration.logging)
                            effectiveConfig.testing.integrationTasks() << (IntegrationTest) testTask
                        } else if (testTask instanceof FunctionalTest) {
                            configureLogging(testTask, effectiveConfig.testing.functional.logging)
                            effectiveConfig.testing.functionalTestTasks() << (FunctionalTest) testTask
                        } else {
                            configureLogging(testTask, effectiveConfig.testing.logging)
                            effectiveConfig.testing.testTasks() << testTask
                        }
                    }

                    if (allTestsTask) {
                        configureAllTestsTask(project, allTestsTask)
                    }
                }
            }
        })
    }

    private void configureRootProject(Project project) {
        TaskProvider<TestReport> aggregateTestReportTask = project.tasks.register(AGGREGATE_TEST_REPORTS_TASK_NAME, TestReport,
            new Action<TestReport>() {
                @Override
                void execute(TestReport t) {
                    t.enabled = false
                    t.group = 'Verification'
                    t.description = 'Aggregate test reports.'
                    t.destinationDir = project.file("${project.buildDir}/reports/aggregate-tests")
                }
            })

        TaskProvider<TestReport> aggregateIntegrationTestReportTask = project.tasks.register(AGGREGATE_INTEGRATION_TEST_REPORTS_TASK_NAME, TestReport,
            new Action<TestReport>() {
                @Override
                void execute(TestReport t) {
                    t.enabled = false
                    t.group = 'Verification'
                    t.description = 'Aggregate integration test reports.'
                    t.destinationDir = project.file("${project.buildDir}/reports/aggregate-integration-tests")
                }
            })

        TaskProvider<TestReport> aggregateFunctionalTestReportTask = project.tasks.register(AGGREGATE_FUNCTIONAL_TEST_REPORTS_TASK_NAME, TestReport,
            new Action<TestReport>() {
                @Override
                void execute(TestReport t) {
                    t.enabled = false
                    t.group = 'Verification'
                    t.description = 'Aggregate functional test reports.'
                    t.destinationDir = project.file("${project.buildDir}/reports/aggregate-functional-tests")
                }
            })

        TaskProvider<TestReport> aggregateAllTestReportTask = project.tasks.register(AGGREGATE_ALL_TEST_REPORTS_TASK_NAME, TestReport,
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

        project.gradle.taskGraph.whenReady(new Action<TaskExecutionGraph>() {
            @Override
            void execute(TaskExecutionGraph graph) {
                configureAggregates(project, graph)
            }
        })
    }

    @CompileDynamic
    private void configureAggregates(Project project, TaskExecutionGraph graph) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)

        Set<Test> tt = new LinkedHashSet<>(effectiveConfig.testing.testTasks())
        Set<Test> itt = new LinkedHashSet<>(effectiveConfig.testing.integrationTasks())
        Set<Test> ftt = new LinkedHashSet<>(effectiveConfig.testing.functionalTestTasks())

        project.childProjects.values().each {
            Testing e = resolveEffectiveConfig(it).testing
            if (e.enabled) {
                tt.addAll(e.testTasks())
                itt.addAll(e.integrationTasks())
                ftt.addAll(e.functionalTestTasks())
            }
        }

        if (graph.hasTask(':' + AGGREGATE_TEST_REPORTS_TASK_NAME)) {
            tt*.setIgnoreFailures(true)
        }
        if (graph.hasTask(':' + AGGREGATE_INTEGRATION_TEST_REPORTS_TASK_NAME)) {
            itt*.ignoreFailures = true
        }
        if (graph.hasTask(':' + AGGREGATE_FUNCTIONAL_TEST_REPORTS_TASK_NAME)) {
            ftt*.ignoreFailures = true
        }
        if (graph.hasTask(':' + AGGREGATE_ALL_TEST_REPORTS_TASK_NAME)) {
            tt*.ignoreFailures = true
            itt*.ignoreFailures = true
            ftt*.ignoreFailures = true
        }
    }

    private static void configureLogging(Test testTask, boolean logging) {
        if (!logging) return

        testTask.afterSuite { TestDescriptor descriptor, TestResult result ->
            if (descriptor.name.contains('Gradle Test Executor')) return

            AnsiConsole console = new AnsiConsole(testTask.project)
            String indicator = console.green(WINDOWS ? '√' : '✔')
            if (result.failedTestCount > 0) {
                indicator = console.red(WINDOWS ? 'X' : '✘')
            }

            String str = console.erase("${indicator} Test ${descriptor.name} ")
            str += "Executed: ${result.testCount}/${console.green(String.valueOf(result.successfulTestCount))}/"
            str += "${console.red(String.valueOf(result.failedTestCount))}/"
            str += "${console.yellow(String.valueOf(result.skippedTestCount))} "
            testTask.project.logger.lifecycle(str.toString())
        }
    }

    private void configureAllTestsTask(Project project,
                                       TaskProvider<DefaultTask> allTestsTask) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        if (!effectiveConfig.testing.enabled) {
            return
        }

        Set<Test> tt = new LinkedHashSet<>(effectiveConfig.testing.testTasks())
        tt.addAll(effectiveConfig.testing.integrationTasks())
        tt.addAll(effectiveConfig.testing.functionalTestTasks())

        allTestsTask.configure(new Action<DefaultTask>() {
            @Override
            void execute(DefaultTask t) {
                t.enabled = tt.size() > 0
                t.dependsOn(tt)
            }
        })
    }

    @CompileDynamic
    private void configureAggregateTestReportTasks(Project project,
                                                   TaskProvider<TestReport> aggregateTestReportTask,
                                                   TaskProvider<TestReport> aggregateIntegrationTestReportTask,
                                                   TaskProvider<TestReport> aggregateFunctionalTestReportTask,
                                                   TaskProvider<TestReport> aggregateAllTestReportTask) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        if (!effectiveConfig.testing.enabled) {
            return
        }

        Set<Test> tt = new LinkedHashSet<>(effectiveConfig.testing.testTasks())
        Set<Test> itt = new LinkedHashSet<>(effectiveConfig.testing.integrationTasks())
        Set<Test> ftt = new LinkedHashSet<>(effectiveConfig.testing.functionalTestTasks())

        project.childProjects.values().each {
            Testing e = resolveEffectiveConfig(it).testing
            if (e.enabled) {
                tt.addAll(e.testTasks())
                itt.addAll(e.integrationTasks())
                ftt.addAll(e.functionalTestTasks())
            }
        }

        aggregateTestReportTask.configure(new Action<TestReport>() {
            @Override
            void execute(TestReport t) {
                t.enabled = tt.size() > 0
                t.reportOn(tt)
                configureAggregateTestReportTask(t, 'Unit', tt)
            }
        })

        aggregateIntegrationTestReportTask.configure(new Action<TestReport>() {
            @Override
            void execute(TestReport t) {
                t.enabled = itt.size() > 0
                t.reportOn(itt)
                configureAggregateTestReportTask(t, 'Integration', itt)
            }
        })

        aggregateFunctionalTestReportTask.configure(new Action<TestReport>() {
            @Override
            void execute(TestReport t) {
                t.enabled = ftt.size() > 0
                t.reportOn(ftt)
                configureAggregateTestReportTask(t, 'Functional', ftt)
            }
        })

        aggregateAllTestReportTask.configure(new Action<TestReport>() {
            @Override
            @CompileDynamic
            void execute(TestReport t) {
                t.enabled = tt.size() > 0 || itt.size() > 0 || ftt.size() > 0
                t.reportOn(tt + itt + ftt)
                configureAggregateTestReportTask(t, 'All', tt + itt + ftt)
            }
        })
    }

    private void configureAggregateTestReportTask(TestReport t, String category, Set<Test> testTasks) {
        Map<String, Long> results = configureTestsForAggregation(testTasks)

        t.doLast(new Action<Task>() {
            @Override
            void execute(Task task) {
                AnsiConsole console = new AnsiConsole(project)
                String indicator = console.green(WINDOWS ? '√' : '✔')
                if (results.failure > 0) {
                    indicator = console.red(WINDOWS ? 'X' : '✘')
                }

                String str = console.erase("${indicator} ${category} Tests ")
                str += "Executed: ${results.total}/${console.green(String.valueOf(results.success))}/"
                str += "${console.red(String.valueOf(results.failure))}/"
                str += "${console.yellow(String.valueOf(results.skipped))} "
                t.project.logger.lifecycle(str.toString())

                if (results.failure > 0) {
                    println("There were failing tests. See the report at: ${t.destinationDir}/index.html")
                    throw new IllegalMonitorStateException('There were failing tests')
                }
            }
        })
    }

    private Map<String, Long> configureTestsForAggregation(Set<Test> testTasks) {
        Map<String, Long> results = [:].withDefault { k -> 0L }

        Closure configurer = { TestDescriptor descriptor, TestResult result ->
            if (descriptor.name.contains('Gradle Test Executor') ||
                descriptor.name.contains('Gradle Test Run')) return

            results.put('total', results.get('total') + result.testCount)
            results.put('success', results.get('success') + result.successfulTestCount)
            results.put('failure', results.get('failure') + result.failedTestCount)
            results.put('skipped', results.get('skipped') + result.skippedTestCount)
        }
        testTasks.each { t ->
            t.afterSuite(configurer)
        }

        results
    }
}
