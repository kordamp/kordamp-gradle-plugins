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
package org.kordamp.gradle.plugin.functionaltest

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.testing.TestReport
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.test.tasks.FunctionalTest

import static org.kordamp.gradle.PluginUtils.resolveSourceSets

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
class FunctionalTestPlugin extends AbstractKordampPlugin {
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
    }

    private void createConfigurationsIfNeeded(Project project) {
        Configuration functionalTestImplementation = project.configurations.findByName('functionalTestImplementation')
        if (!functionalTestImplementation) {
            functionalTestImplementation = project.configurations.create('functionalTestImplementation')
        }
        functionalTestImplementation.extendsFrom project.configurations.implementation

        Configuration functionalTestRuntimeOnly = project.configurations.findByName('functionalTestRuntimeOnly')
        if (!functionalTestRuntimeOnly) {
            functionalTestRuntimeOnly = project.configurations.create('functionalTestRuntimeOnly')
        }
        functionalTestRuntimeOnly.extendsFrom project.configurations.runtimeOnly

        if (project.plugins.findPlugin('idea')) {
            project.idea {
                module {
                    scopes.TEST.plus += [functionalTestImplementation]
                    scopes.TEST.plus += [functionalTestRuntimeOnly]
                    testSourceDirs += resolveSourceSets(project).functionalTest.allSource.srcDirs
                }
            }
        }
    }

    private void createSourceSetsIfNeeded(Project project, String sourceSetName) {
        SourceSet sourceSet = project.sourceSets.maybeCreate('functionalTest')
        if (project.file('src/functional-test/'+ sourceSetName).exists()) {
            sourceSet[sourceSetName].srcDirs project.file('src/functional-test/' + sourceSetName)
        }
        //if (project.file('src/functional-test/groovy').exists()) {
        //    sourceSet.groovy.srcDirs project.file('src/functional-test/groovy')
        //}
        //if (project.file('src/functional-test/kotlin').exists()) {
        //    sourceSet.kotlin.srcDirs project.file('src/functional-test/kotlin')
        //}
        //if (project.file('src/functional-test/scala').exists()) {
        //    sourceSet.scala.srcDirs project.file('src/functional-test/scala')
        //}
        if (project.file('src/functional-test/resources').exists()) {
            sourceSet.resources.srcDir project.file('src/functional-test/resources')
        }
        sourceSet.compileClasspath += resolveSourceSets(project).main.output
        sourceSet.compileClasspath += project.configurations.compileOnly
        sourceSet.compileClasspath += project.configurations.testCompileOnly
        sourceSet.runtimeClasspath += sourceSet.compileClasspath
    }

    private void createTasksIfNeeded(Project project) {
        Task jarTask = project.tasks.findByName('jar')

        FunctionalTest functionalTest = project.tasks.findByName('functionalTest')
        if (!functionalTest) {
            functionalTest = project.tasks.create('functionalTest', FunctionalTest) {
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
            project.pluginManager.apply(FunctionalTestPlugin)
        }
    }
}
