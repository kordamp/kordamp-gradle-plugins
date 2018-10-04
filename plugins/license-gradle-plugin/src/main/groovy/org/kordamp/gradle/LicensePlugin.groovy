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
package org.kordamp.gradle

import nl.javadude.gradle.plugins.license.LicenseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.kordamp.gradle.model.Information
import org.kordamp.gradle.model.License

import static org.kordamp.gradle.BasePlugin.isRootProject

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
            applyLicensePlugin(project)
            project.childProjects.values().each { prj ->
                applyLicensePlugin(prj)
            }
        } else {
            applyLicensePlugin(project)
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(LicensePlugin)) {
            project.plugins.apply(LicensePlugin)
        }
    }

    private void applyLicensePlugin(Project project) {
        String visitedPropertyName = VISITED + '_' + project.name
        if (project.findProperty(visitedPropertyName)) {
            return
        }
        project.ext[visitedPropertyName] = true

        BasePlugin.applyIfMissing(project)

        if (!project.plugins.findPlugin(nl.javadude.gradle.plugins.license.LicensePlugin)) {
            project.plugins.apply(nl.javadude.gradle.plugins.license.LicensePlugin)
        }

        project.afterEvaluate { Project prj ->
            Information info = prj.ext.mergedInfo

            String initialYear = info.inceptionYear
            String currentYear = currentYear()
            String year = initialYear
            if (initialYear != currentYear) {
                year += '-' + currentYear
            }

            License lic = info.licenses.licenses[0]
            if (info.licenses.licenses.size() > 1) {
                lic = info.licenses.licenses.find { it.primary } ?: info.licenses.licenses[0]
            }

            LicenseExtension licenseExtension = prj.extensions.findByType(LicenseExtension)

            licenseExtension.header = prj.rootProject.file('gradle/LICENSE_HEADER')
            licenseExtension.strictCheck = true
            licenseExtension.mapping {
                java   = 'SLASHSTAR_STYLE'
                groovy = 'SLASHSTAR_STYLE'
            }
            licenseExtension.ext.project = prj.name
            licenseExtension.ext {
                projectName   = info.name
                copyrightYear = year
                author        = info.resolveAuthors().join(', ')
                license       = lic.id?.spdx()
            }
            licenseExtension.exclude '**/*.png'
            licenseExtension.exclude 'META-INF/services/*'
        }
    }

    static String currentYear() {
        Date now = new Date()
        Calendar c = Calendar.getInstance()
        c.setTime(now)
        return c.get(Calendar.YEAR).toString()
    }
}
