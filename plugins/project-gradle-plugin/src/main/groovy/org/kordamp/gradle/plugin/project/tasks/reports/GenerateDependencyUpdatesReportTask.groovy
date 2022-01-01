/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2022 Andres Almiray.
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

import groovy.text.SimpleTemplateEngine
import groovy.util.slurpersupport.GPathResult
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.kordamp.gradle.plugin.base.tasks.reports.ReportGeneratingTask

/**
 * @author Andres Almiray
 * @since 0.33.0
 */
class GenerateDependencyUpdatesReportTask extends DefaultTask implements ReportGeneratingTask {
    @InputFile
    final RegularFileProperty dependencyUpdatesXmlReport = project.objects.fileProperty()

    @OutputFile
    final RegularFileProperty outputFile = project.objects.fileProperty()
        .convention(project.layout.buildDirectory.file("project-reports/generated/dependency-updates-report.adoc"))

    @TaskAction
    void generateReport() {
        if (!dependencyUpdatesXmlReport.asFile.get().exists()) {
            project.logger.warn("Dependency update report not available (${dependencyUpdatesXmlReport.asFile.get()})")
            setDidWork(false)
            return
        }

        String report = GenerateDependencyUpdatesReportTask
            .classLoader
            .getResourceAsStream('org/kordamp/gradle/plugin/project/tasks/reports/dependency-updates-report.tpl')
            .text


        GPathResult xml = new XmlSlurper().parse(dependencyUpdatesXmlReport.asFile.get())
        Map<String, String> binding = [
            'dependencies_current'   : processCurrentDependencies(xml.current),
            'dependencies_outdated'  : processOutdatedDependencies(xml.outdated),
            'dependencies_exceeded'  : processExceededDependencies(xml.exceeded),
            'dependencies_unresolved': processUnresolvedDependencies(xml.unresolved)
        ]
        processGradleVersions(xml.gradle, binding)

        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        StringBuilder document = new StringBuilder(engine.createTemplate(report).make(binding).toString())
        outputFile.asFile.get().text = document.toString()
    }

    private String processCurrentDependencies(GPathResult current) {
        if (!current || !current.count.text().toInteger()) {
            return 'There are no dependencies declared in this project.'
        }

        StringBuilder table = new StringBuilder('''|
        |[options="header", cols="3*"]
        ||===
        || Group
        || Name
        || Version
        |'''.stripMargin('|'))

        current.dependencies.dependency.each { dep ->
            table.append("""|
            || ${dep.group.text()}
            || ${dep.name.text()}
            || ${dep.version.text()}
            |""".stripMargin('|'))
        }

        table.append('''|
        ||===
        |'''.stripMargin('|'))

        table.toString()
    }

    private String processOutdatedDependencies(GPathResult outdated) {
        if (!outdated || !outdated.count.text().toInteger()) {
            return 'There are no outdated dependencies in this project.'
        }

        StringBuilder table = new StringBuilder('''|
        |[options="header", cols="4*"]
        ||===
        || Group
        || Name
        || Version
        || Available
        |'''.stripMargin('|'))

        outdated.dependencies.outdatedDependency.each { dep ->
            StringBuilder available = new StringBuilder('[horizontal]\n')
            if (dep.available?.release?.text()) available.append("Release:: ${dep.available.release.text()}").append('\n')
            if (dep.available?.milestone?.text()) available.append("Milestone:: ${dep.available.milestone.text()}").append('\n')
            if (dep.available?.integration?.text()) available.append("Integration:: ${dep.available.integration.text()}").append('\n')

            table.append("""|
            || ${dep.group.text()}
            || ${dep.name.text()}
            || ${dep.version.text()}
            |a| ${available.toString()}
            |""".stripMargin('|'))
        }

        table.append('''|
        ||===
        |'''.stripMargin('|'))

        table.toString()
    }

    private String processExceededDependencies(GPathResult exceeded) {
        if (!exceeded || !exceeded.count.text().toInteger()) {
            return 'There are no exceeded dependencies in this project.'
        }

        StringBuilder table = new StringBuilder('''|
        |[options="header", cols="4*"]
        ||===
        || Group
        || Name
        || Version
        || Latest
        |'''.stripMargin('|'))

        exceeded.dependencies.exceededDependency.each { dep ->
            table.append("""|
            || ${dep.group.text()}
            || ${dep.name.text()}
            || ${dep.version.text()}
            || ${dep.latest.text()}
            |""".stripMargin('|'))
        }

        table.append('''|
        ||===
        |'''.stripMargin('|'))

        table.toString()
    }

    private String processUnresolvedDependencies(GPathResult unresolved) {
        if (!unresolved || !unresolved.count.text().toInteger()) {
            return 'There are no unresolved dependencies in this project.'
        }

        StringBuilder table = new StringBuilder('''|
        |[options="header", cols="4*"]
        ||===
        || Group
        || Name
        || Version
        || Reason
        |'''.stripMargin('|'))

        unresolved.dependencies.unresolvedDependency.each { dep ->
            table.append("""|
            || ${dep.group.text()}
            || ${dep.name.text()}
            || ${dep.version.text()}
            || ${dep.reason.text()}
            |""".stripMargin('|'))
        }

        table.append('''|
        ||===
        |'''.stripMargin('|'))

        table.toString()
    }

    private void processGradleVersions(GPathResult gradle, Map binding) {
        binding['gradle_running'] = gradle.running?.version?.text() ?: ''
        binding['gradle_current'] = gradle.current?.version?.text() ?: ''
    }
}
