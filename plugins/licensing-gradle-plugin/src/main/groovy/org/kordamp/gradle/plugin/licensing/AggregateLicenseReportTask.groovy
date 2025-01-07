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
package org.kordamp.gradle.plugin.licensing

import groovy.json.JsonOutput
import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.xml.MarkupBuilder
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import nl.javadude.gradle.plugins.license.DownloadLicensesExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
@CacheableTask
class AggregateLicenseReportTask extends DefaultTask {
    @Optional @OutputDirectory File reportDir

    AggregateLicenseReportTask() {
        reportDir = project.file("${project.reporting.baseDir.path}/license")
    }

    @TaskAction
    void computeAggregate() {
        computeAggregateByDependency()
        computeAggregateByLicense()
    }

    private void computeAggregateByDependency() {
        Map<String, Object> allDependencies = [:]

        project.subprojects.each { project ->
            DownloadLicensesExtension extension = project.extensions.findByType(DownloadLicensesExtension)
            File file = project.file("${extension.report.xml.destination}/${extension.reportByDependencyFileName}.xml")
            if (!file.exists()) {
                return
            }

            new XmlSlurper().parse(file).dependency.each { dep ->
                allDependencies[dep.@name.text()] = dep
            }
        }

        allDependencies = allDependencies.sort({ it.value.file.text() })

        File reportFile = project.file("${reportDir}/aggregate-dependency-license.xml")
        reportFile.parentFile.mkdirs()
        reportFile.text = XmlUtil.serialize(new StreamingMarkupBuilder().bind {
            dependencies {
                allDependencies.each { name, dep ->
                    dependency(name: dep.@name) {
                        file(dep.file.text())
                        dep.license.sort().each { lic ->
                            license(name: lic.@name, url: lic.@url)
                        }
                    }
                }
            }
        })

        reportFile = project.file("${reportDir}/aggregate-dependency-license.html")
        reportFile.withPrintWriter { writer ->
            MarkupBuilder html = new MarkupBuilder(writer)

            html.html {
                head {
                    title("HTML License report")
                }
                style(
                        '''table {
                  width: 85%;
                  border-collapse: collapse;
                  text-align: center;
                }
                .dependencies {
                  text-align: left;
                }
                tr {
                  border: 1px solid black;
                }
                td {
                  border: 1px solid black;
                  font-weight: bold;
                  color: #2E2E2E
                }
                th {
                  border: 1px solid black;
                }
                h3 {
                  text-align:center;
                  margin:3px
                }
                .license {
                    width:70%
                }

                .licenseName {
                    width:15%
                }
                ''')
                body {
                    table(align: 'center') {
                        tr {
                            th() { h3("Dependency") }
                            th() { h3("Jar") }
                            th() { h3("License name") }
                            th() { h3("License text URL") }
                        }

                        allDependencies.each { name, dep ->
                            dep.license.sort().each { lic ->
                                tr {
                                    td(dep.@name, class: 'dependencies')
                                    td(dep.file.text(), class: 'licenseName')
                                    td(lic.@name, class: 'licenseName')
                                    td(class: 'license') {
                                        if (lic.@url) {
                                            a(href: lic.@url, "Show license agreement")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        reportFile = project.file("${reportDir}/aggregate-dependency-license.json")
        reportFile.text = JsonOutput.toJson([dependencies: allDependencies.values().collect([]) { dep ->
            [
                    name    : String.valueOf(dep.@name),
                    file    : String.valueOf(dep.file.text()),
                    licenses: dep.license.sort().collect([]) { lic ->
                        [
                                name: String.valueOf(lic.@name),
                                url : String.valueOf(lic.@url)
                        ]
                    }
            ]
        }])
    }

    private void computeAggregateByLicense() {
        Map<String, License> allLicenses = [:]

        project.subprojects.each { project ->
            DownloadLicensesExtension extension = project.extensions.findByType(DownloadLicensesExtension)
            File file = project.file("${extension.report.xml.destination}/${extension.reportByLicenseFileName}.xml")
            if (!file.exists()) {
                return
            }

            new XmlSlurper().parse(file).license.each { lic ->
                String key = lic.@name.text()
                License license = allLicenses[key]
                if (!license) {
                    license = new License(lic.@name.text(), lic.@url.text())
                    allLicenses.put(key, license)
                }
                license.artifacts.addAll(lic.dependency*.text())
            }
        }

        allLicenses = allLicenses.sort({ it.key })

        File reportFile = project.file("${reportDir}/aggregate-license-dependency.xml")
        reportFile.parentFile.mkdirs()
        reportFile.text = XmlUtil.serialize(new StreamingMarkupBuilder().bind {
            licenses {
                allLicenses.each { name, lic ->
                    license(name: lic.name, url: lic.url) {
                        lic.artifacts.sort().each { artifact -> dependency(artifact) }
                    }
                }
            }
        })

        reportFile = project.file("${reportDir}/aggregate-license-dependency.html")
        reportFile.withPrintWriter { writer ->
            MarkupBuilder html = new MarkupBuilder(writer)

            html.html {
                head {
                    title("HTML License report")
                }
                style(
                        '''table {
                  width: 85%;
                  border-collapse: collapse;
                  text-align: center;
                }

                .dependencies {
                  text-align: left;
                  width:15%;
                }

                tr {
                  border: 1px solid black;
                }

                td {
                  border: 1px solid black;
                  font-weight: bold;
                  color: #2E2E2E
                }

                th {
                  border: 1px solid black;
                }

                h3 {
                  text-align:center;
                  margin:3px
                }

                .license {
                    width:70%
                }

                .licenseName {
                    width:15%
                }
                ''')
                body {
                    table(align: 'center') {
                        tr {
                            th() { h3("License") }
                            th() { h3("License text URL") }
                            th() { h3("Dependency") }
                        }

                        allLicenses.each { name, lic ->
                            tr {
                                td(lic.name, class: 'licenseName')
                                td(class: 'license') {
                                    if (lic.url) {
                                        a(href: lic.url, "License agreement")
                                    }
                                }
                                td(class: "dependencies") {
                                    ul() {
                                        lic.artifacts.sort().each {
                                            dependency ->
                                                li(dependency)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        reportFile = project.file("${reportDir}/aggregate-license-dependency.json")
        reportFile.text = JsonOutput.toJson([licences: allLicenses.values().collect([]) { lic ->
            [
                    name        : String.valueOf(lic.@name),
                    url         : String.valueOf(lic.@url),
                    dependencies: lic.artifacts.sort().collect([]) { artifact ->
                        String.valueOf(artifact)
                    }
            ]
        }])
    }

    @Canonical
    @CompileStatic
    private static class License {
        final String name
        final String url
        final Set<String> artifacts = new LinkedHashSet<>()
    }
}
