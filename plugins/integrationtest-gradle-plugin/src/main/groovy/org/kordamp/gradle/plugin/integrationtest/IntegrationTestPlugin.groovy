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
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.testing.TestReport
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.test.tasks.IntegrationTest

import static org.kordamp.gradle.PluginUtils.resolveSourceSets

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
    }

    private void createConfigurationsIfNeeded(Project project) {
        Configuration integrationTestImplementation = project.configurations.findByName('integrationTestImplementation')
        if (!integrationTestImplementation) {
            integrationTestImplementation = project.configurations.create('integrationTestImplementation')
        }
        integrationTestImplementation.extendsFrom project.configurations.testImplementation

        Configuration integrationTestRuntimeOnly = project.configurations.findByName('integrationTestRuntimeOnly')
        if (!integrationTestRuntimeOnly) {
            integrationTestRuntimeOnly = project.configurations.create('integrationTestRuntimeOnly')
        }
        integrationTestRuntimeOnly.extendsFrom integrationTestImplementation, project.configurations.testRuntimeOnly

        if (project.plugins.findPlugin('idea')) {
            project.idea {
                module {
                    scopes.TEST.plus += [integrationTestImplementation]
                    scopes.TEST.plus += [integrationTestRuntimeOnly]
                    testSourceDirs += resolveSourceSets(project).integrationTest.allSource.srcDirs
                }
            }
        }
    }

    private void createSourceSetsIfNeeded(Project project, String sourceSetName) {
        SourceSet sourceSet = project.sourceSets.maybeCreate('integrationTest')
        if (project.file('src/integration-test/'+ sourceSetName).exists()) {
            sourceSet[sourceSetName].srcDirs project.file('src/integration-test/' + sourceSetName)
        }
        //if (project.file('src/integration-test/groovy').exists()) {
        //    sourceSet.groovy.srcDirs project.file('src/integration-test/groovy')
        //}
        //if (project.file('src/integration-test/kotlin').exists()) {
        //    sourceSet.kotlin.srcDirs project.file('src/integration-test/kotlin')
        //}
        //if (project.file('src/integration-test/scala').exists()) {
        //    sourceSet.scala.srcDirs project.file('src/integration-test/scala')
        //}
        if (project.file('src/integration-test/resources').exists()) {
            sourceSet.resources.srcDir project.file('src/integration-test/resources')
        }
        sourceSet.compileClasspath += resolveSourceSets(project).main.output
        sourceSet.compileClasspath += project.configurations.compileOnly
        sourceSet.compileClasspath += project.configurations.testCompileOnly
        sourceSet.runtimeClasspath += sourceSet.compileClasspath
    }

    private void createTasksIfNeeded(Project project) {
        Task jarTask = project.tasks.findByName('jar')

        IntegrationTest integrationTest = project.tasks.findByName('integrationTest')
        if (!integrationTest) {
            integrationTest = project.tasks.create('integrationTest', IntegrationTest) {
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

        TestReport integrationTestReport = project.tasks.findByName('integrationTestReport')
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
            project.pluginManager.apply(IntegrationTestPlugin)
        }
    }
}
