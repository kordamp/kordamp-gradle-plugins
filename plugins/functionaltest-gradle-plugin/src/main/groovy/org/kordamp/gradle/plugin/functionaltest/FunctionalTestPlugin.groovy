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
package org.kordamp.gradle.plugin.functionaltest

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
class FunctionalTestPlugin extends AbstractKordampPlugin {
    Project project

    void apply(Project project) {
        this.project = project

        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)

        project.plugins.withType(JavaBasePlugin) {
            createSourceSetsIfNeeded(project)
            createConfigurationsIfNeeded(project)
            createTasksIfNeeded(project)
        }
    }

    private void createConfigurationsIfNeeded(Project project) {
        Configuration functionalTestCompile = project.configurations.findByName('functionalTestCompile')
        if (!functionalTestCompile) {
            functionalTestCompile = project.configurations.create('functionalTestCompile')
        }
        functionalTestCompile.extendsFrom project.configurations.compile

        Configuration functionalTestRuntime = project.configurations.findByName('functionalTestRuntime')
        if (!functionalTestRuntime) {
            functionalTestRuntime = project.configurations.create('functionalTestRuntime')
        }
        functionalTestRuntime.extendsFrom project.configurations.runtime

        if (project.plugins.findPlugin('idea')) {
            project.idea {
                module {
                    scopes.TEST.plus += [project.configurations.functionalTestCompile]
                    scopes.TEST.plus += [project.configurations.functionalTestRuntime]
                    testSourceDirs += resolveSourceSets(project).functionalTest.allSource.srcDirs
                }
            }
        }
    }

    private void createSourceSetsIfNeeded(Project project) {
        if (!project.sourceSets.findByName('functionalTest')) {
            project.sourceSets {
                functionalTest {
                    if (project.file('src/functional-test/java').exists()) {
                        java.srcDirs project.file('src/functional-test/java')
                    }
                    if (project.file('src/functional-test/groovy').exists()) {
                        groovy.srcDirs project.file('src/functional-test/groovy')
                    }
                    if (project.file('src/functional-test/resources').exists()) {
                        resources.srcDir project.file('src/functional-test/resources')
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

        Test functionalTest = project.tasks.findByName('functionalTest')
        if (!functionalTest) {
            functionalTest = project.tasks.create('functionalTest', Test) {
                dependsOn jarTask
                group 'Verification'
                description 'Runs the functional tests.'
                testClassesDirs = resolveSourceSets(project).functionalTest.output.classesDirs
                classpath = resolveSourceSets(project).functionalTest.runtimeClasspath
                reports.html.enabled = false
                forkEvery = Runtime.runtime.availableProcessors()

                testLogging {
                    events 'passed', 'skipped', 'failed'
                }
            }
        }

        TestReport functionalTestReport = project.tasks.findByName('functionalTestReport')
        if (!functionalTestReport) {
            functionalTestReport = project.tasks.create('functionalTestReport', TestReport) {
                group 'Reporting'
                description 'Generates a report on functional tests.'
                destinationDir = project.file("${project.reporting.baseDir.path}/functional-tests")
                reportOn functionalTest.binResultsDir
            }
        }

        functionalTest.mustRunAfter project.tasks.test
        functionalTest.finalizedBy functionalTestReport
        project.tasks.check.dependsOn functionalTestReport
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(FunctionalTestPlugin)) {
            project.plugins.apply(FunctionalTestPlugin)
        }
    }
}
