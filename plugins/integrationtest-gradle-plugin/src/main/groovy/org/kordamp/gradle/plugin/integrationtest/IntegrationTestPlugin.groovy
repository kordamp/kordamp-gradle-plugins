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
package org.kordamp.gradle.plugin.integrationtest

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.testing.TestReport
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.test.tasks.IntegrationTest

import static org.kordamp.gradle.PluginUtils.resolveSourceSets
import static org.kordamp.gradle.PluginUtils.supportsApiConfiguration

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
class IntegrationTestPlugin extends AbstractKordampPlugin {
    Project project

    @CompileStatic
    void apply(Project project) {
        this.project = project

        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)

        project.pluginManager.withPlugin('java-base') {
            createSourceSetsIfNeeded(project, 'java')
            createConfigurationsIfNeeded(project)
            createTasksIfNeeded(project)
        }

        project.pluginManager.withPlugin('groovy-base') {
            createSourceSetsIfNeeded(project, 'groovy')
            createConfigurationsIfNeeded(project)
            createTasksIfNeeded(project)
        }

        project.pluginManager.withPlugin('org.jetbrains.kotlin.jvm') {
            createSourceSetsIfNeeded(project, 'kotlin')
            createConfigurationsIfNeeded(project)
            createTasksIfNeeded(project)
        }

        project.pluginManager.withPlugin('scala-base') {
            createSourceSetsIfNeeded(project, 'scala')
            createConfigurationsIfNeeded(project)
            createTasksIfNeeded(project)
        }

        project.afterEvaluate {
            adjustSourceSets(project)
            adjustConfigurations(project)
            adjustTaskDependencies(project)
        }
    }

    private void adjustSourceSets(Project project) {
        SourceSet sourceSet = resolveSourceSets(project).integrationTest
        sourceSet.compileClasspath += resolveSourceSets(project).main.output
        sourceSet.compileClasspath += project.configurations.compileOnly
        sourceSet.compileClasspath += project.configurations.testCompileOnly
        sourceSet.runtimeClasspath += sourceSet.compileClasspath
    }

    @CompileStatic
    private void adjustConfigurations(Project project) {
        String compileSuffix = 'Compile'
        String runtimeSuffix = 'Runtime'
        if (supportsApiConfiguration(project)) {
            compileSuffix = 'Implementation'
            runtimeSuffix = 'RuntimeOnly'
        }

        project.configurations.findByName('integrationTest' + compileSuffix)
                .extendsFrom project.configurations.findByName('test' + compileSuffix)
        project.configurations.findByName('integrationTest' + runtimeSuffix)
                .extendsFrom project.configurations.findByName('test' + runtimeSuffix)
    }

    @CompileStatic
    private void adjustTaskDependencies(Project project) {
        IntegrationTest integrationTest = (IntegrationTest) project.tasks.findByName('integrationTest')
        TestReport integrationTestReport = (TestReport) project.tasks.findByName('integrationTestReport')
        integrationTest.dependsOn project.tasks.findByName('jar')
        integrationTest.mustRunAfter project.tasks.findByName('test')
        integrationTest.finalizedBy integrationTestReport
        integrationTestReport.reportOn integrationTest.binResultsDir
        integrationTestReport.dependsOn integrationTest
        project.tasks.findByName('check').dependsOn integrationTestReport
    }

    private void createConfigurationsIfNeeded(Project project) {
        String compileSuffix = 'Compile'
        String runtimeSuffix = 'Runtime'
        if (supportsApiConfiguration(project)) {
            compileSuffix = 'Implementation'
            runtimeSuffix = 'RuntimeOnly'
        }

        project.configurations.maybeCreate('integrationTest' + compileSuffix)
        project.configurations.maybeCreate('integrationTest' + runtimeSuffix)
    }

    private void createSourceSetsIfNeeded(Project project, String sourceSetName) {
        SourceSet sourceSet = project.sourceSets.maybeCreate('integrationTest')
        if (project.file('src/integration-test/' + sourceSetName).exists()) {
            sourceSet[sourceSetName].srcDirs project.file('src/integration-test/' + sourceSetName)
        }
        if (project.file('src/integration-test/resources').exists()) {
            sourceSet.resources.srcDir project.file('src/integration-test/resources')
        }
    }

    private void createTasksIfNeeded(Project project) {
        if (!project.tasks.findByName('integrationTest')) {
            project.tasks.register('integrationTest', IntegrationTest,
                    new Action<IntegrationTest>() {
                        @Override
                        void execute(IntegrationTest t) {
                            t.group = 'Verification'
                            t.description = 'Runs the integration tests.'
                            t.testClassesDirs = resolveSourceSets(project).integrationTest.output.classesDirs
                            t.classpath = resolveSourceSets(project).integrationTest.runtimeClasspath
                            t.reports.html.enabled = false
                            t.forkEvery = Runtime.runtime.availableProcessors()

                            t.testLogging {
                                events 'passed', 'skipped', 'failed'
                            }
                        }
                    })
        }

        if (!project.tasks.findByName('integrationTestReport')) {
            project.tasks.register('integrationTestReport', TestReport,
                    new Action<TestReport>() {
                        @Override
                        void execute(TestReport t) {
                            t.group = 'Reporting'
                            t.description = 'Generates a report on integration tests.'
                            t.destinationDir = project.file("${project.reporting.baseDir.path}/integration-tests")
                        }
                    })
        }
    }

    @CompileStatic
    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(IntegrationTestPlugin)) {
            project.pluginManager.apply(IntegrationTestPlugin)
        }
    }
}
