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
class HtmlReporter implements Reporter {
    private final Project project
    private final Writer writer
    private final MessageTranslator translator

    HtmlReporter(Project project, Writer writer) {
        this.project = project
        this.writer = writer

        translator = EventMessages.messageTranslator
    }

    @Override
    void report(final Map<String, List<ApiDifference>> differences) {
        int warnings = 0
        int errors = 0
        int infos = 0

        differences.each { key, value ->
            value.each {
                final Severity severity = it.sourceCompatibilitySeverity
                if (Severity.ERROR == severity) {
                    errors += 1
                } else if (Severity.WARNING == severity) {
                    warnings += 1
                } else if (Severity.INFO == severity) {
                    infos += 1
                }
            }
        }

        writer.write('<!DOCTYPE html>')
        def builder = new MarkupBuilder(writer)
        builder.html {
            head {
                title "Binary Compatibility Report - ${project.name}-${project.version}"
                style '''
                        body{margin:0;padding:0;font-family:sans-serif;font-size:12pt;}
                        body,a,a:visited{color:#303030;}
                        #content{padding-left:50px;padding-right:50px;padding-top:30px;padding-bottom:30px;}
                        #content h1{font-size:160%;margin-bottom:10px;}
                        #footer{margin-top:100px;font-size:80%;white-space:nowrap;}
                        #footer,#footer a{color:#a0a0a0;}
                        ul{margin-left:0;}
                        h1,h2,h3{white-space:nowrap;}
                        h2{font-size:120%;}
                        div.selected{display:block;}
                        div.deselected{display:none;}
                        #maintable{width:100%;border-collapse:collapse;}
                        #maintable th,#maintable td{border-bottom:solid #d0d0d0 1px;}
                        #maintable td{vertical-align:top}
                        th{text-align:left;white-space:nowrap;padding-left:2em;}
                        th:first-child{padding-left:0;}
                        td{padding-left:2em;padding-top:5px;padding-bottom:5px;}
                        td:first-child{padding-left:0;width:30%}
                        td.numeric,th.numeric{text-align:right;}
                        span.code{display:inline-block;margin-top:0em;margin-bottom:1em;}
                        span.code pre{font-size:11pt;padding-top:10px;padding-bottom:10px;padding-left:10px;padding-right:10px;margin:0;background-color:#f7f7f7;border:solid 1px #d0d0d0;min-width:700px;width:auto !important;width:700px;}
                        ul{margin:0px;padding:0px;}
                        .warning,.warning a{color:#fbcc45;}
                        .error,.error a{color:#b60808;}
                        .info, .info a{color:#3879d9}
                        #summary {margin-top: 30px;margin-bottom: 40px;border: solid 2px #d0d0d0;width:400px}
                        #summary table{border:none;}
                        #summary td{vertical-align:top;width:110px;padding-top:15px;padding-bottom:15px;text-align:center;}
                        #summary td p{margin:0;}
                       '''
            }
            body {
                div(id: "content") {
                    h1 "Binary Compatibility Report - ${project.name}-${project.version}"
                    div(id: "summary") {
                        table {
                            tr {
                                td {
                                    p(class: "error", 'ERROR')
                                    div errors
                                }
                                td {
                                    p(class: "warning", 'WARNING')
                                    div warnings
                                }
                                td {
                                    p(class: "info", 'INFO')
                                    div infos
                                }
                            }
                        }
                    }
                    table(id: "maintable") {
                        thead {
                            tr {
                                th 'Class'
                                th 'Results'
                            }
                        }
                        tbody {
                            differences.sort { it.key }.each { key, value ->
                                tr {
                                    td key
                                    td {
                                        ul {
                                            value.each { difference ->
                                                final Severity severity = difference.sourceCompatibilitySeverity
                                                li {
                                                    span(class: severity.toString().toLowerCase(), "$severity($difference.message.id): ")
                                                    mkp.yield difference.getReport(translator)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        writer.flush()
    }
}
