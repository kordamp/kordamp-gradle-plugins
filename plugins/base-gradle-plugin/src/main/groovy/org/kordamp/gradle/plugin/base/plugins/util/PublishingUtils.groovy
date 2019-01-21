/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
package org.kordamp.gradle.plugin.base.plugins.util

import org.gradle.api.publish.maven.MavenPom
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.Dependency
import org.kordamp.gradle.plugin.base.model.Person
import org.kordamp.gradle.plugin.base.model.PomOptions

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.13.0
 */
class PublishingUtils {
    static void configurePom(MavenPom pom, ProjectConfigurationExtension effectiveConfig, PomOptions pomOptions) {
        pom.with {
            name = effectiveConfig.info.name
            description = effectiveConfig.info.description
            if (isOverwriteAllowed(pomOptions, pomOptions.overwriteUrl)) url = effectiveConfig.info.url
            if (isOverwriteAllowed(pomOptions, pomOptions.overwriteInceptionYear)) inceptionYear = effectiveConfig.info.inceptionYear

            if (isOverwriteAllowed(pomOptions, pomOptions.overwriteLicenses)) {
                licenses {
                    effectiveConfig.license.licenses.forEach { lic ->
                        license {
                            name = lic.name
                            url = lic.url
                            distribution = lic.distribution
                            if (lic.comments) comments = lic.comments
                        }
                    }
                }
            }

            if (isOverwriteAllowed(pomOptions, pomOptions.overwriteScm)) {
                if (!isBlank(effectiveConfig.info.scm.url)) {
                    scm {
                        url = effectiveConfig.info.scm.url
                        if (effectiveConfig.info.scm.connection) {
                            connection = effectiveConfig.info.scm.connection
                        }
                        if (effectiveConfig.info.scm.connection) {
                            developerConnection = effectiveConfig.info.scm.developerConnection
                        }
                    }
                } else if (effectiveConfig.info.links.scm) {
                    scm {
                        url = effectiveConfig.info.links.scm
                    }
                }
            }

            if (isOverwriteAllowed(pomOptions, pomOptions.overwriteOrganization)) {
                if (!effectiveConfig.info.organization.isEmpty()) {
                    organization {
                        name = effectiveConfig.info.organization.name
                        url = effectiveConfig.info.organization.url
                    }
                }
            }

            if (isOverwriteAllowed(pomOptions, pomOptions.overwriteDevelopers)) {
                developers {
                    effectiveConfig.info.people.forEach { Person person ->
                        if ('developer' in person.roles*.toLowerCase()) {
                            developer {
                                if (person.id) id = person.id
                                if (person.name) name = person.name
                                if (person.url) url = person.url
                                if (person.email) email = person.email
                                if (person.organization?.name) organizationName = person.organization.name
                                if (person.organization?.url) organizationUrl = person.organization.url
                                if (person.roles) roles = person.roles as Set
                                if (person.properties) properties.set(person.properties)
                            }
                        }
                    }
                }
            }

            if (isOverwriteAllowed(pomOptions, pomOptions.overwriteContributors)) {
                contributors {
                    effectiveConfig.info.people.forEach { Person person ->
                        if ('contributor' in person.roles*.toLowerCase()) {
                            contributor {
                                if (person.name) name = person.name
                                if (person.url) url = person.url
                                if (person.email) email = person.email
                                if (person.organization?.name) organizationName = person.organization.name
                                if (person.organization?.url) organizationUrl = person.organization.url
                                if (person.roles) roles = person.roles as Set
                                if (person.properties) properties.set(person.properties)
                            }
                        }
                    }
                }
            }
        }

        if (!isBlank(pomOptions.parent)) {
            Dependency parentPom = Dependency.parseDependency(effectiveConfig.project, pomOptions.parent, true)
            pom.withXml {
                Node parentNode = new XmlParser().parseText("""
                    <parent>
                        <groupId>${parentPom.groupId}</groupId>
                        <artifactId>${parentPom.artifactId}</artifactId>
                        <version>${parentPom.version}</version>
                    </parent>
                """)
                asNode().children().add(1, parentNode)
            }
        }
    }

    private static boolean isOverwriteAllowed(PomOptions pom, boolean option) {
        !isBlank(pom.parent) && option
    }
}
