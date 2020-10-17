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

import groovy.text.SimpleTemplateEngine
import groovy.text.Template
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.Person
import org.kordamp.gradle.plugin.base.tasks.reports.ReportGeneratingTask

import static org.kordamp.gradle.util.PluginUtils.resolveConfig

/**
 * @author Andres Almiray
 * @since 0.33.0
 */
@CompileStatic
class GenerateTeamReportTask extends DefaultTask implements ReportGeneratingTask {
    @Optional
    @InputFile
    final RegularFileProperty teamHeaderTemplate = project.objects.fileProperty()
    @Optional
    @InputFile
    final RegularFileProperty teamFooterTemplate = project.objects.fileProperty()
    @Optional
    @InputFile
    final RegularFileProperty teamTableHeaderTemplate = project.objects.fileProperty()

    @Optional
    @InputFile
    final RegularFileProperty teamTableRowTemplate = project.objects.fileProperty()

    @OutputFile
    final RegularFileProperty outputFile = project.objects.fileProperty()
        .convention(project.layout.buildDirectory.file("project-reports/generated/team-report.adoc"))

    @TaskAction
    void generateReport() {
        ProjectConfigurationExtension config = resolveConfig(project)
        if (config.info.people.empty) {
            project.logger.warn("There are no team members defined in project ${project.name}")
            setDidWork(false)
            return
        }

        String teamHeader = resolveTemplate(teamHeaderTemplate.getAsFile().orNull,
            GenerateTeamReportTask.classLoader.getResourceAsStream('org/kordamp/gradle/plugin/project/tasks/reports/team-header.tpl'))
        String teamFooter = resolveTemplate(teamFooterTemplate.getAsFile().orNull,
            GenerateTeamReportTask.classLoader.getResourceAsStream('org/kordamp/gradle/plugin/project/tasks/reports/team-footer.tpl'))
        String teamTableHeader = resolveTemplate(teamTableHeaderTemplate.getAsFile().orNull,
            GenerateTeamReportTask.classLoader.getResourceAsStream('org/kordamp/gradle/plugin/project/tasks/reports/team-table-header.tpl'))
        String teamTableRow = resolveTemplate(teamTableRowTemplate.getAsFile().orNull,
            GenerateTeamReportTask.classLoader.getResourceAsStream('org/kordamp/gradle/plugin/project/tasks/reports/team-table-row.tpl'))

        SimpleTemplateEngine engine = new SimpleTemplateEngine()

        StringBuilder document = new StringBuilder(engine.createTemplate(teamHeader).make().toString())
        document.append(engine.createTemplate(teamTableHeader).make())

        config.info.people.forEach { Person person ->
            document.append(processPerson(person, engine.createTemplate(teamTableRow)))
        }

        document.append('''|
        ||===
        |'''.stripMargin('|'))
        document.append(engine.createTemplate(teamFooter).make())

        outputFile.asFile.get().text = document.toString()
    }

    private String resolveTemplate(File file, InputStream is) {
        file?.exists() ? file.text : is.text
    }

    private String processPerson(Person person, Template template) {
        Map binding = [
            person_image_url        : resolveImageUrl(person),
            person_id               : person.id ?: '-',
            person_name             : person.name,
            person_email            : person.email ?: '-',
            person_url              : person.url ?: '-',
            person_timezone         : person.timezone ?: '-',
            person_organization_name: person.organization?.name ?: '-',
            person_organization_url : person.organization?.url ?: '-',
            person_roles            : person.roles.collect { it.capitalize() }.join(', ')
        ]
        person.properties.each { k, v ->
            binding[('person_' + k)] = v
        }
        template.make(binding).toString()
    }

    private String resolveImageUrl(Person person) {
        if (person.properties.picUrl) {
            return person.properties.picUrl
        } else if (person.properties.twitter) {
            return "image:https://avatars.io/twitter/${person.properties.twitter}/medium[]".toString()
        }
        '-'
    }
}
