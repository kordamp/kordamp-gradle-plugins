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
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestReport
import org.gradle.api.tasks.testing.TestResult
import org.kordamp.gradle.annotations.DependsOn
import org.kordamp.gradle.listener.AllProjectsEvaluatedListener
import org.kordamp.gradle.listener.ProjectEvaluatedListener
import org.kordamp.gradle.listener.TaskGraphReadyListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.Testing
import org.kordamp.gradle.plugin.test.tasks.FunctionalTest
import org.kordamp.gradle.plugin.test.tasks.IntegrationTest
import org.kordamp.gradle.util.AnsiConsole
import org.kordamp.gradle.util.TimeUtils

import javax.inject.Named

import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addAllProjectsEvaluatedListener
import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addProjectEvaluatedListener
import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addTaskGraphReadyListener
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject
import static org.kordamp.gradle.util.PluginUtils.registerJarVariant
import static org.kordamp.gradle.util.PluginUtils.resolveConfig
import static org.kordamp.gradle.util.PluginUtils.resolveSourceSets

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
    static final String TEST_JAR_TASK_NAME = 'testJar'

    private static final boolean WINDOWS = System.getProperty('os.name').startsWith('Windows')

    Project project

    TestingPlugin() {
        super(Testing.PLUGIN_ID)
    }

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

                addProjectEvaluatedListener(project, new TestingProjectEvaluatedListener(allTestsTask))
            }
        })
    }

    private void configureRootProject(Project project) {
        project.tasks.register(AGGREGATE_TEST_REPORTS_TASK_NAME, TestReport,
            new Action<TestReport>() {
                @Override
                void execute(TestReport t) {
                    t.enabled = false
                    t.group = 'Verification'
                    t.description = 'Aggregate test reports.'
                    t.destinationDir = project.file("${project.buildDir}/reports/aggregate-tests")
                }
            })

        project.tasks.register(AGGREGATE_INTEGRATION_TEST_REPORTS_TASK_NAME, TestReport,
            new Action<TestReport>() {
                @Override
                void execute(TestReport t) {
                    t.enabled = false
                    t.group = 'Verification'
                    t.description = 'Aggregate integration test reports.'
                    t.destinationDir = project.file("${project.buildDir}/reports/aggregate-integration-tests")
                }
            })

        project.tasks.register(AGGREGATE_FUNCTIONAL_TEST_REPORTS_TASK_NAME, TestReport,
            new Action<TestReport>() {
                @Override
                void execute(TestReport t) {
                    t.enabled = false
                    t.group = 'Verification'
                    t.description = 'Aggregate functional test reports.'
                    t.destinationDir = project.file("${project.buildDir}/reports/aggregate-functional-tests")
                }
            })

        project.tasks.register(AGGREGATE_ALL_TEST_REPORTS_TASK_NAME, TestReport,
            new Action<TestReport>() {
                @Override
                void execute(TestReport t) {
                    t.enabled = false
                    t.group = 'Verification'
                    t.description = 'Aggregate all test reports.'
                    t.destinationDir = project.file("${project.buildDir}/reports/aggregate-all-tests")
                }
            })

        addAllProjectsEvaluatedListener(project, new TestingAllProjectsEvaluatedListener())
        addTaskGraphReadyListener(project, new TestingTaskGraphReadyListener())
    }

    @Named('testing')
    @DependsOn(['base'])
    private class TestingProjectEvaluatedListener implements ProjectEvaluatedListener {
        private final TaskProvider<DefaultTask> allTestsTask

        TestingProjectEvaluatedListener(TaskProvider<DefaultTask> allTestsTask) {
            this.allTestsTask = allTestsTask
        }

        @Override
        void projectEvaluated(Project project) {
            ProjectConfigurationExtension config = resolveConfig(project)
            setEnabled(config.testing.enabled)

            if (!enabled) {
                return
            }

            project.tasks.withType(Test) { Test testTask ->
                if (testTask instanceof IntegrationTest) {
                    configureLogging(testTask, config.testing.integration.logging)
                    config.testing.integrationTasks() << (IntegrationTest) testTask
                } else if (testTask instanceof FunctionalTest) {
                    configureLogging(testTask, config.testing.functional.logging)
                    config.testing.functionalTestTasks() << (FunctionalTest) testTask
                } else {
                    configureLogging(testTask, config.testing.logging)
                    if (config.testing.jar) {
                        project.tasks.findByName(org.gradle.api.plugins.BasePlugin.ASSEMBLE_TASK_NAME)
                            .dependsOn(createTestJar(project, testTask))
                    }
                    config.testing.testTasks() << testTask
                }
            }

            if (allTestsTask) {
                if (!config.testing.enabled) {
                    return
                }

                allTestsTask.configure(new Action<DefaultTask>() {
                    @Override
                    void execute(DefaultTask t) {
                        Set<Test> tt = new LinkedHashSet<>(config.testing.testTasks())
                        tt.addAll(config.testing.integrationTasks())
                        tt.addAll(config.testing.functionalTestTasks())
                        t.enabled = tt.size() > 0
                        t.dependsOn(tt)
                    }
                })
            }
        }
    }
    @Named('testing')
    @DependsOn(['base'])
    private class TestingTaskGraphReadyListener implements TaskGraphReadyListener {
        @Override
        void taskGraphReady(Project rootProject, TaskExecutionGraph graph) {
            configureAggregates(rootProject, graph)
        }
    }

    @Named('testing')
    @DependsOn(['publishing'])
    private class TestingAllProjectsEvaluatedListener implements AllProjectsEvaluatedListener {
        @Override
        void allProjectsEvaluated(Project rootProject) {
            configureAggregateTestReportTasks(rootProject)
            updatePublications(rootProject)
        }
    }

    @CompileDynamic
    private void configureAggregates(Project project, TaskExecutionGraph graph) {
        ProjectConfigurationExtension config = resolveConfig(project)

        Set<Test> tt = new LinkedHashSet<>(config.testing.testTasks())
        Set<Test> itt = new LinkedHashSet<>(config.testing.integrationTasks())
        Set<Test> ftt = new LinkedHashSet<>(config.testing.functionalTestTasks())

        project.childProjects.values().each {
            Testing e = resolveConfig(it).testing
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

            ProjectConfigurationExtension config = resolveConfig(testTask.project)
            AnsiConsole console = new AnsiConsole(testTask.project)
            String indicator = config.testing.colors.success(console, WINDOWS ? '√' : '✔')
            if (result.failedTestCount > 0) {
                indicator = config.testing.colors.failure(console, WINDOWS ? 'X' : '✘')
            }

            long time = result.endTime - result.startTime
            String duration = TimeUtils.formatDuration(time / 1000d)
            String str = console.erase("${indicator} Test ${descriptor.name} ")
            str += "[Tests: ${result.testCount}/${config.testing.colors.success(console, String.valueOf(result.successfulTestCount))}/"
            str += "${config.testing.colors.failure(console, String.valueOf(result.failedTestCount))}/"
            str += "${config.testing.colors.ignored(console, String.valueOf(result.skippedTestCount))}]"
            if (!descriptor.name.contains('Gradle Test Run')) {
                double timeThreshold = config.testing.timeThreshold
                if (timeThreshold <= 0d) timeThreshold = 2000d
                str += ' [Time: ' + (time >= timeThreshold ? config.testing.colors.failure(console, duration) : duration) + ']'
            }
            testTask.project.logger.lifecycle(str.toString())
        }
    }

    private static TaskProvider<Jar> createTestJar(Project project, Test testTask) {
        ProjectConfigurationExtension config = resolveConfig(project)

        project.tasks.register(TEST_JAR_TASK_NAME, Jar,
            new Action<Jar>() {
                @Override
                @CompileDynamic
                void execute(Jar t) {
                    t.enabled = config.testing.enabled
                    t.group = org.gradle.api.plugins.BasePlugin.BUILD_GROUP
                    t.description = 'An archive of the unit tests'
                    t.archiveClassifier.set('tests')
                    t.dependsOn testTask
                    t.from resolveSourceSets(project).test.output
                }
            })
    }

    @CompileDynamic
    private void configureAggregateTestReportTasks(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)
        if (!config.testing.enabled) {
            return
        }

        Set<Test> tt = new LinkedHashSet<>(config.testing.testTasks())
        Set<Test> itt = new LinkedHashSet<>(config.testing.integrationTasks())
        Set<Test> ftt = new LinkedHashSet<>(config.testing.functionalTestTasks())

        project.childProjects.values().each {
            Testing e = resolveConfig(it).testing
            if (e.enabled) {
                tt.addAll(e.testTasks())
                itt.addAll(e.integrationTasks())
                ftt.addAll(e.functionalTestTasks())
            }
        }

        project.tasks.named(AGGREGATE_TEST_REPORTS_TASK_NAME, TestReport, new Action<TestReport>() {
            @Override
            void execute(TestReport t) {
                t.enabled = tt.size() > 0
                t.reportOn(tt)
                configureAggregateTestReportTask(t, 'Unit', tt)
            }
        })

        project.tasks.named(AGGREGATE_INTEGRATION_TEST_REPORTS_TASK_NAME, TestReport, new Action<TestReport>() {
            @Override
            void execute(TestReport t) {
                t.enabled = itt.size() > 0
                t.reportOn(itt)
                configureAggregateTestReportTask(t, 'Integration', itt)
            }
        })

        project.tasks.named(AGGREGATE_FUNCTIONAL_TEST_REPORTS_TASK_NAME, TestReport, new Action<TestReport>() {
            @Override
            void execute(TestReport t) {
                t.enabled = ftt.size() > 0
                t.reportOn(ftt)
                configureAggregateTestReportTask(t, 'Functional', ftt)
            }
        })

        project.tasks.named(AGGREGATE_ALL_TEST_REPORTS_TASK_NAME, TestReport, new Action<TestReport>() {
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
                ProjectConfigurationExtension config = resolveConfig(project)
                AnsiConsole console = new AnsiConsole(project)
                String indicator = config.testing.colors.success(console, WINDOWS ? '√' : '✔')
                if (results.failure > 0) {
                    indicator = config.testing.colors.failure(console, WINDOWS ? 'X' : '✘')
                }

                String duration = TimeUtils.formatDuration(results.time / 1000d)
                String str = console.erase("${indicator} ${category} Tests ")
                str += "[Tests: ${results.total}/${config.testing.colors.success(console, String.valueOf(results.success))}/"
                str += "${config.testing.colors.failure(console, String.valueOf(results.failure))}/"
                str += "${config.testing.colors.ignored(console, String.valueOf(results.skipped))}] "
                str += "[Time: ${duration}]"
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
            results.put('time', results.get('time') + (result.endTime - result.startTime))
            results.put('success', results.get('success') + result.successfulTestCount)
            results.put('failure', results.get('failure') + result.failedTestCount)
            results.put('skipped', results.get('skipped') + result.skippedTestCount)
        }
        testTasks.each { t ->
            t.afterSuite(configurer)
        }

        results
    }

    private void updatePublications(Project project) {
        updatePublication(project)
        for (Project p : project.childProjects.values()) {
            updatePublications(p)
        }
    }

    private void updatePublication(Project project) {
        if (project.tasks.findByName(TEST_JAR_TASK_NAME)) {
            TaskProvider<Jar> testJar = project.tasks.named(TEST_JAR_TASK_NAME, Jar)
            ProjectConfigurationExtension config = resolveConfig(project)
            if (config.testing.enabled && project.pluginManager.hasPlugin('maven-publish')) {
                PublishingExtension publishing = project.extensions.findByType(PublishingExtension)
                MavenPublication mainPublication = (MavenPublication) publishing.publications.findByName('main')
                if (mainPublication) mainPublication.artifact(testJar.get())
                Configuration variant = registerJarVariant('Test', 'tests', testJar, project, false, 'runtime')
                variant.canBeResolved = true

                Configuration testArtifacts = project.configurations.maybeCreate('testArtifacts')
                testArtifacts.extendsFrom(variant)
                testArtifacts.extendsFrom(project.configurations.findByName('testImplementation'))
                testArtifacts.extendsFrom(project.configurations.findByName('testRuntimeOnly'))
                testArtifacts.visible = true
                testArtifacts.description = 'Test artifacts'
                testArtifacts.canBeResolved = true
                testArtifacts.canBeConsumed = true
            }
        }
    }
}
