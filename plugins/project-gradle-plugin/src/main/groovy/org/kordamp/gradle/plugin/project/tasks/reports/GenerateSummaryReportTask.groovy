/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2021 Andres Almiray.
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
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.tasks.reports.ReportGeneratingTask

import static org.kordamp.gradle.util.PluginUtils.resolveConfig

/**
 * @author Andres Almiray
 * @since 0.33.0
 */
@CompileStatic
class GenerateSummaryReportTask extends DefaultTask implements ReportGeneratingTask {
    @OutputFile
    final RegularFileProperty outputFile = project.objects.fileProperty()
        .convention(project.layout.buildDirectory.file("project-reports/generated/summary-report.adoc"))

    @TaskAction
    void generateReport() {
        ProjectConfigurationExtension config = resolveConfig(project.rootProject)

        StringBuilder document = new StringBuilder("""|
        |= Summary
        |
        |[cols="<40,<60"]
        ||===
        |a| *Name*
        || ${project.name}
        |
        |a| *Path*
        || ${project.path}
        |""".stripMargin('|'))

        Jar jarTask = (Jar) project.tasks.findByName('jar')
        if (jarTask) {
            document.append("""|
            |a| *Group*
            || ${project.group}
            |
            |a| *ArtifactId*
            || ${jarTask.archiveBaseName.get()}
            |
            |a| *Version*
            || ${jarTask.archiveVersion.get()}
            |""".stripMargin('|'))
        }

        document.append("""|
        |a| *Created-By*
        || ${config.buildInfo.buildCreatedBy}
        |
        |a| *Build-By*
        || ${config.buildInfo.buildBy}
        |
        |a| *Build-OS*
        || ${config.buildInfo.buildOs}
        |
        |a| *Build-Jdk*
        || ${config.buildInfo.buildJdk}
        |
        |a| *Build-Date*
        || ${config.buildInfo.buildDate}
        |
        |a| *Build-Time*
        || ${config.buildInfo.buildTime}
        |
        |a| *Build-Revision*
        || ${config.buildInfo.buildRevision}
        |""".stripMargin('|'))

        if (jarTask) {
            config = resolveConfig(project)
            if (config.info.specification.enabled) {
                document.append("""|
                |a| *Specification-Title*
                || ${config.info.specification.title}
                |
                |a| *Specification-Version*
                || ${config.info.specification.version}
                |
                |a| *Specification-Vendor*
                || ${config.info.specification.vendor ?: '-'}
                |""".stripMargin('|'))
            }
            if (config.info.implementation.enabled) {
                document.append("""|
                |a| *Implementation-Title*
                || ${config.info.implementation.title}
                |
                |a| *Implementation-Version*
                || ${config.info.implementation.version}
                |
                |a| *Implementation-Vendor*
                || ${config.info.implementation.vendor ?: '-'}
                |""".stripMargin('|'))
            }
        }

        document.append('''|
        ||===
        |'''.stripMargin('|'))

        outputFile.asFile.get().text = document.toString()
    }
}
