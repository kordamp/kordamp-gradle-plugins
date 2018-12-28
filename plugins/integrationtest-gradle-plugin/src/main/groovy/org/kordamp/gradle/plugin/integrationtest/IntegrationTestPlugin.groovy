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
package org.kordamp.gradle.plugin.integrationtest

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestReport
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin

import static org.kordamp.gradle.PluginUtils.resolveSourceSets

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
class IntegrationTestPlugin extends AbstractKordampPlugin {
    Project project

    void apply(Project project) {
        this.project = project

        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)

        project.plugins.withType(JavaBasePlugin) {
            createConfigurationsIfNeeded(project)
            createSourceSetsIfNeeded(project)
            createTasksIfNeeded(project)
        }
    }

    private void createConfigurationsIfNeeded(Project project) {
        Configuration integrationTestCompile = project.configurations.findByName('integrationTestCompile')
        if (!integrationTestCompile) {
            integrationTestCompile = project.configurations.create('integrationTestCompile')
        }
        integrationTestCompile.extendsFrom project.configurations.testCompile

        Configuration integrationTestRuntime = project.configurations.findByName('integrationTestRuntime')
        if (!integrationTestRuntime) {
            integrationTestRuntime = project.configurations.create('integrationTestRuntime')
        }
        integrationTestRuntime.extendsFrom integrationTestCompile, project.configurations.testRuntime

        if (project.plugins.findPlugin('idea')) {
            project.idea {
                module {
                    scopes.TEST.plus += [project.configurations.integrationTestCompile]
                    scopes.TEST.plus += [project.configurations.integrationTestRuntime]
                    testSourceDirs += resolveSourceSets(project).integrationTest.allSource.srcDirs
                }
            }
        }
    }

    private void createSourceSetsIfNeeded(Project project) {
        if (!project.sourceSets.findByName('integrationTest')) {
            project.sourceSets {
                integrationTest {
                    if (project.file('src/integration-test/java').exists()) {
                        java.srcDirs project.file('src/integration-test/java')
                    }
                    if (project.file('src/integration-test/groovy').exists()) {
                        groovy.srcDirs project.file('src/integration-test/groovy')
                    }
                    if (project.file('src/integration-test/resources').exists()) {
                        resources.srcDir project.file('src/integration-test/resources')
                    }
                    compileClasspath += resolveSourceSets(project).main.output
                    compileClasspath += project.configurations.compileOnly
                    compileClasspath += project.configurations.testCompileOnly
                    runtimeClasspath += compileClasspath
                }
            }
        }
    }

    private void createTasksIfNeeded(Project project) {
        Task jarTask = project.tasks.findByName('jar')

        Test integrationTest = project.tasks.findByName('integrationTest')
        if (!integrationTest) {
            integrationTest = project.tasks.create('integrationTest', Test) {
                dependsOn jarTask
                group 'Verification'
                description 'Runs the integration tests.'
                testClassesDirs = resolveSourceSets(project).integrationTest.output.classesDirs
                classpath = resolveSourceSets(project).integrationTest.runtimeClasspath
                reports.html.enabled = false
                forkEvery = Runtime.runtime.availableProcessors()

                testLogging {
                    events 'passed', 'skipped', 'failed'
                }
            }
        }

        Task integrationTestReport = project.tasks.findByName('integrationTestReport')
        if (!integrationTestReport) {
            integrationTestReport = project.tasks.create('integrationTestReport', TestReport) {
                group 'Reporting'
                description 'Generates a report on integration tests.'
                destinationDir = project.file("${project.reporting.baseDir.path}/integration-tests")
                reportOn integrationTest.binResultsDir
            }
        }

        integrationTest.mustRunAfter project.tasks.test
        integrationTest.finalizedBy integrationTestReport
        project.tasks.check.dependsOn integrationTestReport
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(IntegrationTestPlugin)) {
            project.plugins.apply(IntegrationTestPlugin)
        }
    }
}
