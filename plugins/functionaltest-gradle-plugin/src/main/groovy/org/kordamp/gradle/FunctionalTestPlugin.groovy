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
package org.kordamp.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestReport

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
class FunctionalTestPlugin implements Plugin<Project> {
    static final String VISITED = FunctionalTestPlugin.class.name.replace('.', '_') + '_VISITED'
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
                    testSourceDirs += project.sourceSets.functionalTest.allSource.srcDirs
                }
            }
        }
    }

    private void createSourceSetsIfNeeded(Project project) {
        if (!project.sourceSets.findByName('functionalTest')) {
            if (project.plugins.findPlugin('groovy')) {
                project.file('src/functional-test/groovy').mkdirs()
            }
            project.file('src/functional-test/java').mkdirs()
            project.file('src/functional-test/resources').mkdirs()

            project.sourceSets {
                functionalTest {
                    java.srcDirs project.file('src/functional-test/java')
                    if (project.file('src/functional-test/groovy').exists()) {
                        groovy.srcDirs project.file('src/functional-test/groovy')
                    }
                    resources.srcDir project.file('src/functional-test/resources')
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

        Task functionalTest = project.tasks.findByName('functionalTest')
        if (!functionalTest) {
            functionalTest = project.tasks.create('functionalTest', Test) {
                dependsOn jarTask
                group 'Verification'
                description 'Runs the functional tests.'
                testClassesDirs = project.sourceSets.functionalTest.output.classesDirs
                classpath = project.sourceSets.functionalTest.runtimeClasspath
                reports.html.enabled = false
                forkEvery = Runtime.runtime.availableProcessors()

                testLogging {
                    events 'passed', 'skipped', 'failed'
                }
            }
        }

        Task functionalTestReport = project.tasks.findByName('functionalTestReport')
        if (!functionalTestReport) {
            functionalTestReport = project.tasks.create('functionalTestReport', TestReport) {
                group 'Reporting'
                description 'Generates a report on functional tests.'
                destinationDir = project.file("${project.buildDir}/reports/functional-tests")
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
