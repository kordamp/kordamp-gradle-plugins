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
package org.kordamp.gradle.plugin.clirr.reporters

import groovy.xml.MarkupBuilder
import net.sf.clirr.core.ApiDifference
import net.sf.clirr.core.MessageTranslator
import net.sf.clirr.core.Severity
import org.gradle.api.Project
import org.kordamp.gradle.plugin.clirr.EventMessages

/**
 *
 * @author Andres Almiray
 * @since 0.12.0
 */
class XmlReporter implements Reporter {
    private final Project project
    private final Writer writer
    private final MessageTranslator translator

    XmlReporter(Project project, Writer writer) {
        this.project = project
        this.writer = writer
        translator = EventMessages.messageTranslator
    }

    @Override
    void report(Map<String, List<ApiDifference>> list) {
        int totalWarnings = 0
        int totalErrors = 0
        int totalInfos = 0

        list.each { key, value ->
            value.each {
                Severity severity = it.sourceCompatibilitySeverity
                if (Severity.ERROR == severity) {
                    totalErrors++
                } else if (Severity.WARNING == severity) {
                    totalWarnings++
                } else if (Severity.INFO == severity) {
                    totalInfos++
                }
            }
        }

        writer.write('<?xml version="1.0"?>')
        def builder = new MarkupBuilder(writer)
        builder.diffreport {
            header {
                project "${project.name}-${project.version}"
                summary {
                    info(totalInfos)
                    warnings(totalWarnings)
                    error(totalErrors)
                }
            }
            builder.differences {
                list.sort { it.key }.each { key, value ->
                    value.each { diff ->
                        builder.difference {
                            classname(key)
                            severity(diff.sourceCompatibilitySeverity.toString().toLowerCase())
                            identifier(diff.message.id)
                            message(diff.getReport(translator))
                        }
                    }
                }
            }
        }
        writer.flush()
    }
}
