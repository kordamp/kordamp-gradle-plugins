/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2023 Andres Almiray.
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
package org.kordamp.gradle.plugin.cpd.internal

import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.logging.Logger
import org.gradle.api.specs.Spec
import org.gradle.internal.Factory
import org.gradle.internal.SystemProperties
import org.gradle.internal.logging.ConsoleRenderer
import org.kordamp.gradle.plugin.cpd.Cpd
import org.kordamp.gradle.plugin.cpd.CpdReports

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
final class CpdInvoker {
    private CpdInvoker() {
        // noop
    }

    static void invoke(Cpd cpdTask) {
        FileCollection cpdClasspath = cpdTask.cpdClasspath.filter(new FileExistFilter())
        def antBuilder = cpdTask.antBuilder
        FileTree source = cpdTask.source
        CpdReports reports = cpdTask.reports
        boolean ignoreFailures = cpdTask.getIgnoreFailures()
        Logger logger = cpdTask.logger

        SystemProperties.instance.withSystemProperty('java.class.path',
            cpdClasspath.files.join(File.pathSeparator),
            new Factory<Void>() {
                @Override
                Void create() {
                    antBuilder.withClasspath(cpdClasspath).execute { a ->
                        Map antCpdArgs = [
                            minimumtokencount: cpdTask.minimumTokenCount.get(),
                            encoding         : cpdTask.encoding.get(),
                            language         : cpdTask.language.get(),
                            ignoreLiterals   : cpdTask.ignoreLiterals.get(),
                            ignoreIdentifiers: cpdTask.ignoreIdentifiers.get(),
                            ignoreAnnotations: cpdTask.ignoreAnnotations.get(),
                            format           : 'xml'
                        ]

                        antCpdArgs.outputfile = reports.xml.outputLocation.asFile.get()

                        ant.taskdef(name: 'cpd', classname: 'net.sourceforge.pmd.cpd.CPDTask')
                        ant.cpd(antCpdArgs) {
                            source.addToAntBuilder(ant, 'fileset', FileCollection.AntType.FileSet)
                        }

                        File xmlReportFile = reports.xml.outputLocation.asFile.get()
                        def xml = new XmlSlurper().parse(xmlReportFile)
                        if (xml.duplication?.size() > 0) {
                            if (reports.html.enabled) {
                                ant.xslt(style: cpdTask.project.layout.buildDirectory.file(cpdTask.taskIdentity.name + '/cpdhtml.xslt').get().asFile,
                                    in: reports.xml.outputLocation.asFile.get(),
                                    out: reports.html.outputLocation.asFile.get())
                            }

                            int duplicates = xml.duplication.size()
                            def message = "[CPD] Found ${duplicates} duplicate block pair(s)."
                            def report = reports.firstEnabled
                            if (report) {
                                def reportUrl = new ConsoleRenderer().asClickableFileUrl(report.destination)
                                message += " See the report at: $reportUrl"
                            }
                            if (ignoreFailures) {
                                logger.warn(message)
                            } else {
                                throw new GradleException(message)
                            }
                        } else {
                            println("[CPD] No duplicates over " + cpdTask.minimumTokenCount.get() + " tokens found")
                        }
                    }

                    return null
                }
            })
    }

    private static class FileExistFilter implements Spec<File> {
        @Override
        boolean isSatisfiedBy(File element) {
            return element.exists()
        }
    }
}
