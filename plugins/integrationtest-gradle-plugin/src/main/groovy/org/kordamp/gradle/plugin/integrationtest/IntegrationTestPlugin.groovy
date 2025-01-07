/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2025 Andres Almiray.
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
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.TestReport
import org.kordamp.gradle.annotations.DependsOn
import org.kordamp.gradle.listener.ProjectEvaluatedListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.Integration
import org.kordamp.gradle.plugin.test.tasks.IntegrationTest

import javax.inject.Named

import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addProjectEvaluatedListener
import static org.kordamp.gradle.util.PluginUtils.resolveConfig
import static org.kordamp.gradle.util.PluginUtils.resolveSourceSets

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
class IntegrationTestPlugin extends AbstractKordampPlugin {
    Project project

    IntegrationTestPlugin() {
        super(Integration.PLUGIN_ID)
    }

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

        addProjectEvaluatedListener(project, new IntegrationProjectEvaluatedListener())
    }

    @Named('integration')
    @DependsOn(['base'])
    private class IntegrationProjectEvaluatedListener implements ProjectEvaluatedListener {
        @Override
        void projectEvaluated(Project project) {
            adjustSourceSets(project)
            adjustConfigurations(project)
            adjustTaskDependencies(project)
        }
    }

    private void adjustSourceSets(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)
        String sourceSetDir = config.testing.integration.baseDir
        project.pluginManager.withPlugin('java-base') {
            adjustSourceSet(project, sourceSetDir, 'java')
        }

        project.pluginManager.withPlugin('groovy-base') {
            adjustSourceSet(project, sourceSetDir, 'groovy')
        }

        SourceSet sourceSet = resolveSourceSets(project).integrationTest
        sourceSet.compileClasspath += resolveSourceSets(project).main.output
        sourceSet.compileClasspath += project.configurations.compileClasspath
        sourceSet.compileClasspath += project.configurations.testCompileClasspath
        if (config.testing.integration.includeTestOutput) {
            sourceSet.compileClasspath += resolveSourceSets(project).test.output
        }
        sourceSet.runtimeClasspath += sourceSet.compileClasspath
        sourceSet.runtimeClasspath += project.configurations.testRuntimeClasspath

        project.configurations.findByName('integrationTestCompileClasspath')
            .extendsFrom(project.configurations.compileClasspath, project.configurations.testCompileClasspath)
        project.configurations.findByName('integrationTestRuntimeClasspath')
            .extendsFrom(project.configurations.compileClasspath, project.configurations.testRuntimeClasspath)
    }

    @CompileStatic
    private void adjustConfigurations(Project project) {
        String compileSuffix = 'Implementation'
        String runtimeSuffix = 'RuntimeOnly'

        project.configurations.findByName('integrationTest' + compileSuffix)
            .extendsFrom(project.configurations.findByName('test' + compileSuffix))
        project.configurations.findByName('integrationTest' + runtimeSuffix)
            .extendsFrom(project.configurations.findByName('test' + runtimeSuffix))
    }

    @CompileStatic
    private void adjustTaskDependencies(Project project) {
        SourceSet sourceSet = ((SourceSetContainer) resolveSourceSets(project)).findByName('integrationTest')
        IntegrationTest integrationTest = (IntegrationTest) project.tasks.findByName('integrationTest')
        integrationTest.include('**/*Test.class', '**/*IT.class')
        integrationTest.classpath = sourceSet.runtimeClasspath
        TestReport integrationTestReport = (TestReport) project.tasks.findByName('integrationTestReport')
        integrationTest.dependsOn project.tasks.findByName('jar')
        integrationTest.mustRunAfter project.tasks.findByName('test')
        integrationTest.finalizedBy integrationTestReport
        integrationTestReport.testResults.from(integrationTest.binaryResultsDirectory)
        project.tasks.findByName('check').dependsOn integrationTest
        project.tasks.findByName('check').dependsOn integrationTestReport
    }

    private void createConfigurationsIfNeeded(Project project) {
        project.configurations.maybeCreate('integrationTestImplementation')
        project.configurations.maybeCreate('integrationTestRuntimeOnly')
    }

    private void createSourceSetsIfNeeded(Project project) {
        project.sourceSets.maybeCreate('integrationTest')
    }

    private void adjustSourceSet(Project project, String sourceSetDir, String sourceSetName) {
        SourceSet sourceSet = project.sourceSets.maybeCreate('integrationTest')

        if (project.file(sourceSetDir + File.separator + sourceSetName).exists()) {
            sourceSet[sourceSetName].srcDirs project.file(sourceSetDir + File.separator + sourceSetName)
        }
        if (project.file(sourceSetDir + File.separator + 'resources').exists()) {
            sourceSet.resources.srcDir project.file(sourceSetDir + File.separator + 'resources')
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
                        t.reports.html.required.set(true)
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
                        t.destinationDirectory.set(project.file("${project.reporting.baseDir.path}/integration-tests"))
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