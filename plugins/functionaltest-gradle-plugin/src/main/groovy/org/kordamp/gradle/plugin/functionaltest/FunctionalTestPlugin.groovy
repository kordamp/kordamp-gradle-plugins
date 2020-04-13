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
package org.kordamp.gradle.plugin.functionaltest

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.TestReport
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.test.tasks.FunctionalTest

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.PluginUtils.resolveSourceSets
import static org.kordamp.gradle.PluginUtils.supportsApiConfiguration

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
            createSourceSetsIfNeeded(project)
            createConfigurationsIfNeeded(project)
            createTasksIfNeeded(project)
        }

        project.pluginManager.withPlugin('groovy-base') {
            createSourceSetsIfNeeded(project)
            createConfigurationsIfNeeded(project)
            createTasksIfNeeded(project)
        }

        project.pluginManager.withPlugin('org.jetbrains.kotlin.jvm') {
            createSourceSetsIfNeeded(project)
            createConfigurationsIfNeeded(project)
            createTasksIfNeeded(project)
        }

        project.pluginManager.withPlugin('scala-base') {
            createSourceSetsIfNeeded(project)
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
        SourceSet sourceSet = resolveSourceSets(project).functionalTest

        ProjectConfigurationExtension config = resolveEffectiveConfig(project)
        String sourceSetDir = config.testing.functional.baseDir
        project.pluginManager.withPlugin('java-base') {
            adjustSourceSet(project, sourceSetDir, 'java')
        }

        project.pluginManager.withPlugin('groovy-base') {
            adjustSourceSet(project, sourceSetDir, 'groovy')
        }

        project.pluginManager.withPlugin('org.jetbrains.kotlin.jvm') {
            adjustSourceSet(project, sourceSetDir, 'kotlin')
        }

        project.pluginManager.withPlugin('scala-base') {
            adjustSourceSet(project, sourceSetDir, 'scala')
        }

        sourceSet.compileClasspath += resolveSourceSets(project).main.output
        sourceSet.compileClasspath += project.configurations.compileClasspath
        sourceSet.runtimeClasspath += sourceSet.compileClasspath

        project.configurations.findByName('functionalTestCompileClasspath')
            .extendsFrom(project.configurations.compileClasspath)
        project.configurations.findByName('functionalTestRuntimeClasspath')
            .extendsFrom(project.configurations.compileClasspath)
    }

    @CompileStatic
    private void adjustConfigurations(Project project) {
        String compileSuffix = 'Compile'
        String runtimeSuffix = 'Runtime'
        if (supportsApiConfiguration(project)) {
            compileSuffix = 'Implementation'
            runtimeSuffix = 'RuntimeOnly'
        }

        project.configurations.findByName('functionalTest' + compileSuffix)
            .extendsFrom project.configurations.findByName(compileSuffix.uncapitalize())
        project.configurations.findByName('functionalTest' + runtimeSuffix)
            .extendsFrom project.configurations.findByName(runtimeSuffix.uncapitalize())
    }

    @CompileStatic
    private void adjustTaskDependencies(Project project) {
        SourceSet sourceSet = ((SourceSetContainer) resolveSourceSets(project)).findByName('functionalTest')
        FunctionalTest functionalTest = (FunctionalTest) project.tasks.findByName('functionalTest')
        functionalTest.classpath = sourceSet.runtimeClasspath
        TestReport functionalTestReport = (TestReport) project.tasks.findByName('functionalTestReport')
        functionalTest.dependsOn project.tasks.findByName('jar')
        functionalTest.mustRunAfter project.tasks.findByName('test')
        functionalTest.finalizedBy functionalTestReport
        functionalTestReport.reportOn functionalTest.binResultsDir
        functionalTestReport.dependsOn functionalTest
        project.tasks.findByName('check').dependsOn functionalTestReport
    }

    private void createConfigurationsIfNeeded(Project project) {
        String compileSuffix = 'Compile'
        String runtimeSuffix = 'Runtime'
        if (supportsApiConfiguration(project)) {
            compileSuffix = 'Implementation'
            runtimeSuffix = 'RuntimeOnly'
        }

        project.configurations.maybeCreate('functionalTest' + compileSuffix)
        project.configurations.maybeCreate('functionalTest' + runtimeSuffix)
    }

    private void createSourceSetsIfNeeded(Project project) {
        project.sourceSets.maybeCreate('functionalTest')
    }

    private void adjustSourceSet(Project project, String sourceSetDir, String sourceSetName) {
        SourceSet sourceSet = project.sourceSets.maybeCreate('functionalTest')

        if (project.file(sourceSetDir + File.separator + sourceSetName).exists()) {
            sourceSet[sourceSetName].srcDirs project.file(sourceSetDir + File.separator + sourceSetName)
        }
        if (project.file(sourceSetDir + File.separator + 'resources').exists()) {
            sourceSet.resources.srcDir project.file(sourceSetDir + File.separator + 'resources')
        }
    }

    private void createTasksIfNeeded(Project project) {
        if (!project.tasks.findByName('functionalTest')) {
            project.tasks.register('functionalTest', FunctionalTest,
                new Action<FunctionalTest>() {
                    @Override
                    void execute(FunctionalTest t) {
                        t.group = 'Verification'
                        t.description = 'Runs the functional tests.'
                        t.testClassesDirs = resolveSourceSets(project).functionalTest.output.classesDirs
                        t.classpath = resolveSourceSets(project).functionalTest.runtimeClasspath
                        t.reports.html.enabled = false
                        t.forkEvery = Runtime.runtime.availableProcessors()

                        t.testLogging {
                            events 'passed', 'skipped', 'failed'
                        }
                    }
                })
        }

        if (!project.tasks.findByName('functionalTestReport')) {
            project.tasks.register('functionalTestReport', TestReport,
                new Action<TestReport>() {
                    @Override
                    void execute(TestReport t) {
                        t.group = 'Reporting'
                        t.description = 'Generates a report on functional tests.'
                        t.destinationDir = project.file("${project.reporting.baseDir.path}/functional-tests")
                    }
                })
        }
    }

    @CompileStatic
    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(FunctionalTestPlugin)) {
            project.pluginManager.apply(FunctionalTestPlugin)
        }
    }
}