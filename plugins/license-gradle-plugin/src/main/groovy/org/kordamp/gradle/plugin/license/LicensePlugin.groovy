/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kordamp.gradle.plugin.license

import com.hierynomus.gradle.license.LicenseReportingPlugin
import com.hierynomus.gradle.license.tasks.LicenseCheck
import com.hierynomus.gradle.license.tasks.LicenseFormat
import nl.javadude.gradle.plugins.license.DownloadLicenses
import nl.javadude.gradle.plugins.license.DownloadLicensesExtension
import nl.javadude.gradle.plugins.license.LicenseExtension
import nl.javadude.gradle.plugins.license.LicenseMetadata
import org.gradle.BuildAdapter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.License
import org.kordamp.gradle.plugin.base.model.LicenseId

import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
class LicensePlugin implements Plugin<Project> {
    private static final String VISITED = LicensePlugin.class.name.replace('.', '_') + '_VISITED'

    Project project

    void apply(Project project) {
        this.project = project

        if (isRootProject(project)) {
            if (project.childProjects.size()) {
                project.childProjects.values().each {
                    configureProject(it)
                }
            }
            configureProject(project)
        } else {
            configureProject(project)
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(LicensePlugin)) {
            project.plugins.apply(LicensePlugin)
        }
    }

    private void configureProject(Project project) {
        String visitedPropertyName = VISITED + '_' + project.name
        if (project.findProperty(visitedPropertyName)) {
            return
        }
        project.ext[visitedPropertyName] = true

        BasePlugin.applyIfMissing(project)

        if (!project.plugins.findPlugin(LicenseReportingPlugin)) {
            project.plugins.apply(LicenseReportingPlugin)
        }
        if (!project.plugins.findPlugin(nl.javadude.gradle.plugins.license.LicensePlugin)) {
            project.plugins.apply(nl.javadude.gradle.plugins.license.LicensePlugin)
        }

        preconfigureDownloadLicensesExtension(project)

        project.afterEvaluate {
            configureLicenseExtension(project)
        }

        if (isRootProject(project) && !project.childProjects.isEmpty()) {
            project.gradle.addBuildListener(new BuildAdapter() {
                @Override
                void projectsEvaluated(Gradle gradle) {
                    configureAggregateLicenseReportTask(project)
                }
            })
        }
    }

    private void configureLicenseExtension(Project project) {
        ProjectConfigurationExtension mergedConfiguration = project.ext.mergedConfiguration

        if (!mergedConfiguration.license.enabled) {
            project.tasks.withType(LicenseCheck).each { it.enabled = false }
            project.tasks.withType(LicenseFormat).each { it.enabled = false }
            return
        }

        License lic = mergedConfiguration.license.allLicenses()[0]
        if (mergedConfiguration.license.allLicenses().size() > 1) {
            lic = mergedConfiguration.license.allLicenses().find {
                it.primary
            } ?: mergedConfiguration.license.allLicenses()[0]
        }

        LicenseExtension licenseExtension = project.extensions.findByType(LicenseExtension)

        licenseExtension.header = project.rootProject.file('gradle/LICENSE_HEADER')
        licenseExtension.strictCheck = true
        licenseExtension.mapping {
            java   = 'SLASHSTAR_STYLE'
            groovy = 'SLASHSTAR_STYLE'
        }
        licenseExtension.ext.project = project.name
        licenseExtension.ext {
            projectName   = mergedConfiguration.info.name
            copyrightYear = mergedConfiguration.info.copyrightYear
            author        = mergedConfiguration.info.resolveAuthors().join(', ')
            license       = lic.id?.spdx()
        }
        licenseExtension.exclude '**/*.png'
        licenseExtension.exclude 'META-INF/services/*'
    }

    private void preconfigureDownloadLicensesExtension(Project project) {
        DownloadLicensesExtension extension = project.extensions.findByType(DownloadLicensesExtension)
        LicenseMetadata apacheTwo = extension.license('The Apache Software License, Version 2.0', LicenseId.APACHE_2_0.url())
        LicenseMetadata epl1 = extension.license('Eclipse Public License v1.0', LicenseId.EPL_1_0.url())
        LicenseMetadata epl2 = extension.license('Eclipse Public License v2.0', LicenseId.EPL_2_0.url())
        LicenseMetadata lgpl21 = extension.license('GNU Lesser General Public License v2.1 or later', LicenseId.LGPL_2_1_OR_LATER.url())
        LicenseMetadata mit = extension.license('MIT License', LicenseId.MIT.url())
        LicenseMetadata bsd2 = extension.license('BSD 2-Clause FreeBSD License', LicenseId.BSD_2_CLAUSE_FREEBSD.url())
        LicenseMetadata bsd3 = extension.license('BSD 3-Clause "New" or "Revised" License', LicenseId.BSD_3_CLAUSE.url())

        extension.aliases = [
            (apacheTwo): ['The Apache Software License, Version 2.0',
                          'The Apache Software License, version 2.0',
                          'Apache Software License - Version 2.0',
                          'Apache Software License - version 2.0',
                          'the Apache License, ASL Version 2.0',
                          'The Apache License, Version 2.0',
                          'The Apache License Version 2.0',
                          'Apache License, Version 2.0',
                          'Apache License, version 2.0',
                          'Apache License Version 2.0',
                          'Apache License version 2.0',
                          'The Apache License 2.0',
                          'Apache 2.0 License',
                          'Apache License 2.0',
                          'Apache 2.0',
                          'Apache-2.0',
                          'Apache 2',
                          apacheTwo],
            (epl1)     : ['Eclipse Public License - Version 1.0',
                          'Eclipse Public License v1.0',
                          'Eclipse Public License 1.0',
                          'Eclipse Public License',
                          'EPL v1.0',
                          'EPL 1.0',
                          'EPL-1.0',
                          epl1],
            (epl2)     : ['Eclipse Public License v2.0',
                          'Eclipse Public License 2.0',
                          'EPL v2.0',
                          'EPL 2.0',
                          'EPL-2.0',
                          epl1],
            (lgpl21)   : ['GNU Library General Public License v2.1 or later',
                          'GNU Lesser General Public License v2.1 or later',
                          'GNU Lesser General Public License, Version 2.1',
                          'LGPL 2.1',
                          'LGPL-2.1',
                          epl1],
            (mit)      : ['The MIT License',
                          'The MIT license',
                          'MIT License',
                          'MIT license',
                          'MIT',
                          mit],
            (bsd2)     : ['BSD 2-Clause FreeBSD License',
                          'The BSD License',
                          'The BSD license',
                          bsd2],
            (bsd3)     : ['BSD 3-Clause "New" or "Revised" License',
                          '3-Clause BSD License',
                          '3-Clause BSD license',
                          'Revised BSD License',
                          'Revised BSD license',
                          'BSD Revised License',
                          'BSD Revised license',
                          'New BSD License',
                          'New BSD license',
                          'BSD New License',
                          'BSD New license',
                          'BSD 3-Clause',
                          'BSD 3-clause',
                          bsd3]
        ]
    }

    private void configureAggregateLicenseReportTask(Project project) {
        ProjectConfigurationExtension mergedConfiguration = project.ext.mergedConfiguration
        if (!mergedConfiguration.license.enabled) {
            return
        }

        Set<DownloadLicenses> tasks = new LinkedHashSet<>()
        project.subprojects.each { prj ->
            tasks.addAll(prj.tasks.withType(DownloadLicenses))
        }

        project.tasks.create('aggregateLicenseReport', AggregateLicenseReportTask) {
            dependsOn tasks
            group 'Reporting'
            description 'Generates an aggregate license report'
        }
    }
}
