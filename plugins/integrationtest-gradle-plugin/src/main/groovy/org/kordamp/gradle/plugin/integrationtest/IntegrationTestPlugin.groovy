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
package org.kordamp.gradle.plugin.integrationtest

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestReport
import org.kordamp.gradle.plugin.base.BasePlugin

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
class IntegrationTestPlugin implements Plugin<Project> {
    private static final String VISITED = IntegrationTestPlugin.class.name.replace('.', '_') + '_VISITED'
    Project project

    void apply(Project project) {
        this.project = project

        String visitedPropertyName = VISITED + '_' + project.name
        if (project.findProperty(visitedPropertyName)) {
            return
        }
        project.ext[visitedPropertyName] = true

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
                    testSourceDirs += project.sourceSets.integrationTest.allSource.srcDirs
                }
            }
        }
    }

    private void createSourceSetsIfNeeded(Project project) {
        if (!project.sourceSets.findByName('integrationTest')) {
            if (project.plugins.findPlugin('groovy')) {
                project.file('src/integration-test/groovy').mkdirs()
            }
            project.file('src/integration-test/java').mkdirs()
            project.file('src/integration-test/resources').mkdirs()

            project.sourceSets {
                integrationTest {
                    java.srcDirs project.file('src/integration-test/java')
                    if (project.file('src/integration-test/groovy').exists()) {
                        groovy.srcDirs project.file('src/integration-test/groovy')
                    }
                    resources.srcDir project.file('src/integration-test/resources')
                    compileClasspath += project.sourceSets.main.output
                    compileClasspath += project.configurations.compileOnly
                    compileClasspath += project.configurations.testCompileOnly
                    runtimeClasspath += compileClasspath
                }
            }
        }
    }

    private void createTasksIfNeeded(Project project) {
        Task jarTask = project.tasks.findByName('jar')

        Task integrationTest = project.tasks.findByName('integrationTest')
        if (!integrationTest) {
            integrationTest = project.tasks.create('integrationTest', Test) {
                dependsOn jarTask
                group 'Verification'
                description 'Runs the integration tests.'
                testClassesDirs = project.sourceSets.integrationTest.output.classesDirs
                classpath = project.sourceSets.integrationTest.runtimeClasspath
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
