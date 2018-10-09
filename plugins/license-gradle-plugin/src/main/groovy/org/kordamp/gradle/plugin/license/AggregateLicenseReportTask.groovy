package org.kordamp.gradle.plugin.license

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import nl.javadude.gradle.plugins.license.DownloadLicensesExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
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

        File reportFile = project.file("${reportDir}/aggregate-dependency-license.xml")
        reportFile.parentFile.mkdirs()
        reportFile.text = XmlUtil.serialize(new StreamingMarkupBuilder().bind {
            dependencies {
                allDependencies.sort({ it.value.file.text() }).each { name, dep ->
                    dependency(name: dep.@name) {
                        file(dep.file.text())
                        dep.license.each { lic ->
                            license(name: lic.@name, url: lic.@url)
                        }
                    }
                }
            }
        })
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

        File reportFile = project.file("${reportDir}/aggregate-license-dependency.xml")
        reportFile.parentFile.mkdirs()
        reportFile.text = XmlUtil.serialize(new StreamingMarkupBuilder().bind {
            licenses {
                allLicenses.sort({ it.key }).each { name, lic ->
                    license(name: lic.name, url: lic.url) {
                        lic.artifacts.sort().each { artifact -> dependency(artifact) }
                    }
                }
            }
        })
    }

    @Canonical
    @CompileStatic
    private static class License {
        final String name
        final String url
        final Set<String> artifacts = new LinkedHashSet<>()
    }
}
