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
            if (project.childProjects.size()) {
                project.childProjects.values().each {
                    configureProject(it)
                }
            } else {
                configureProject(project)
            }
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

        if (!project.plugins.findPlugin(nl.javadude.gradle.plugins.license.LicensePlugin)) {
            project.plugins.apply(nl.javadude.gradle.plugins.license.LicensePlugin)
        }

        project.afterEvaluate {
            ProjectConfigurationExtension mergedConfiguration = project.ext.mergedConfiguration

            License lic = mergedConfiguration.license.allLicenses()[0]
            if (mergedConfiguration.license.allLicenses().size() > 1) {
                lic = mergedConfiguration.license.allLicenses().find { it.primary } ?: mergedConfiguration.license.allLicenses()[0]
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
    }
}
