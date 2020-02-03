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
package org.kordamp.gradle.plugin.project.tasks.reports

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.tasks.reports.ReportGeneratingTask

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.PluginUtils.supportsApiConfiguration

/**
 * @author Andres Almiray
 * @since 0.33.0
 */
@CompileStatic
class GenerateDependenciesReportTask extends DefaultTask implements ReportGeneratingTask {
    @OutputFile
    final RegularFileProperty outputFile = project.objects.fileProperty()
        .convention(project.layout.buildDirectory.file("project-reports/generated/dependencies-report.adoc"))

    @TaskAction
    void generateReport() {
        ProjectConfigurationExtension config = resolveEffectiveConfig(project.rootProject)

        StringBuilder document = new StringBuilder("""|
        |= Dependencies
        |""".stripMargin('|'))

        Map<String, List<Dependency>> dependencies = collectDependencies(project)

        if (dependencies.api) {
            processDependencies(dependencies.api, 'Api', document)
        }
        if (dependencies.implementation) {
            processDependencies(dependencies.implementation, 'Implementation', document)
        }
        if (dependencies.runtimeOnly) {
            processDependencies(dependencies.runtimeOnly, 'RuntimeOnly', document)
        }
        if (dependencies.compile) {
            processDependencies(dependencies.compile, 'Compile', document)
        }
        if (dependencies.runtime) {
            processDependencies(dependencies.runtime, 'Runtime', document)
        }

        document.append('''|
        ||===
        |'''.stripMargin('|'))

        outputFile.asFile.get().text = document.toString()
    }

    private Map<String, List<Dependency>> collectDependencies(Project project) {
        Closure<Boolean> filter = { Dependency d ->
            d.name != 'unspecified'
        }

        Map<String, List<Dependency>> map = [
            compile: project.configurations.findByName('compile')
                ?.allDependencies?.findAll(filter)
                ?.collect { it }?.sort(),
            runtime: project.configurations.findByName('runtime')
                ?.allDependencies?.findAll(filter)
                ?.collect { it }?.sort()
        ]

        if (supportsApiConfiguration(project)) {
            map.api = project.configurations.findByName('api')
                ?.allDependencies?.findAll(filter)
                ?.collect { it }?.sort()
            map.implementation = project.configurations.findByName('implementation')
                ?.allDependencies?.findAll(filter)
                ?.collect { it }?.sort()
            map.runtimeOnly = project.configurations.findByName('runtimeOnly')
                ?.allDependencies?.findAll(filter)
                ?.collect { it }?.sort()
        }

        map
    }

    private void processDependencies(List<Dependency> dependencies, String title, StringBuilder document) {
        document.append("""|
        |== ${title}
        |
        |[options="header", cols="3*"]
        ||===
        || Group
        || ArtifactId
        || Version
        |""".stripMargin('|'))

        dependencies.each { dep ->
            document.append("""|
            || ${dep.group}
            || ${dep.name}
            || ${dep.version}
            |""".stripMargin('|'))
        }

        document.append('''|
        ||===
        |'''.stripMargin('|'))
    }
}
