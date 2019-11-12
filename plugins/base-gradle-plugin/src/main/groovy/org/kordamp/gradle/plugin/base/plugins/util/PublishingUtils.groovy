/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
package org.kordamp.gradle.plugin.base.plugins.util

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPomCiManagement
import org.gradle.api.publish.maven.MavenPomContributor
import org.gradle.api.publish.maven.MavenPomContributorSpec
import org.gradle.api.publish.maven.MavenPomDeveloper
import org.gradle.api.publish.maven.MavenPomDeveloperSpec
import org.gradle.api.publish.maven.MavenPomIssueManagement
import org.gradle.api.publish.maven.MavenPomLicense
import org.gradle.api.publish.maven.MavenPomLicenseSpec
import org.gradle.api.publish.maven.MavenPomMailingList
import org.gradle.api.publish.maven.MavenPomMailingListSpec
import org.gradle.api.publish.maven.MavenPomOrganization
import org.gradle.api.publish.maven.MavenPomScm
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.Dependency
import org.kordamp.gradle.plugin.base.model.License
import org.kordamp.gradle.plugin.base.model.MailingList
import org.kordamp.gradle.plugin.base.model.Person
import org.kordamp.gradle.plugin.base.model.PomOptions

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.StringUtils.isBlank
import static org.kordamp.gradle.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.13.0
 */
@CompileStatic
class PublishingUtils {
    static void configurePublication(Project project, String publicationName) {
        if (!publicationName) return

        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        PublishingExtension publishingExtension = project.extensions.findByType(PublishingExtension)
        Publication publication = publishingExtension.publications.findByName(publicationName)
        if (publication instanceof MavenPublication) {
            MavenPublication mavenPublication = (MavenPublication) publication
            configurePom(mavenPublication.pom, effectiveConfig, effectiveConfig.publishing.pom)
        }
        configureSigning(effectiveConfig, project, publicationName)
    }

    static void configurePublications(Project project, String... publications) {
        if (!publications) return

        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        PublishingExtension publishingExtension = project.extensions.findByType(PublishingExtension)
        publications.each { String publicationName ->
            Publication publication = publishingExtension.publications.findByName(publicationName)
            if (publication instanceof MavenPublication) {
                MavenPublication mavenPublication = (MavenPublication) publication
                configurePom(mavenPublication.pom, effectiveConfig, effectiveConfig.publishing.pom)
            }
        }
        configureSigning(effectiveConfig, project, publications)
    }

    static void configureAllPublications(Project project) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        PublishingExtension publishingExtension = project.extensions.findByType(PublishingExtension)
        SigningExtension signingExtension = project.extensions.findByType(SigningExtension)
        List<String> publications = publishingExtension.publications*.name

        publications.each { String publicationName ->
            Publication publication = publishingExtension.publications.findByName(publicationName)
            if (publication instanceof MavenPublication) {
                MavenPublication mavenPublication = (MavenPublication) publication
                configurePom(mavenPublication.pom, effectiveConfig, effectiveConfig.publishing.pom)
            }
            signingExtension.sign(publication)
        }

        project.tasks.withType(Sign, new Action<Sign>() {
            @Override
            void execute(Sign t) {
                t.onlyIf {
                    if (effectiveConfig.publishing.signingSet) {
                        return effectiveConfig.publishing.signing
                    } else {
                        return project.gradle.taskGraph.hasTask(":${project.name}:uploadArchives".toString())
                    }
                }
            }
        })
    }

    static void configureSigning(ProjectConfigurationExtension effectiveConfig, Project project, String... publications) {
        SigningExtension signingExtension = project.extensions.findByType(SigningExtension)
        PublishingExtension publishingExtension = project.extensions.findByType(PublishingExtension)

        if (!publications) {
            if (publishingExtension.publications.findByName('main')) {
                signingExtension.sign(publishingExtension.publications.main)
            }
        } else {
            publications.each { publicationName ->
                if (publishingExtension.publications.findByName(publicationName)) {
                    signingExtension.sign(publishingExtension.publications.findByName(publicationName))
                }
            }
        }

        project.tasks.withType(Sign, new Action<Sign>() {
            @Override
            void execute(Sign t) {
                t.onlyIf {
                    if (effectiveConfig.publishing.signingSet) {
                        return effectiveConfig.publishing.signing
                    } else {
                        return project.gradle.taskGraph.hasTask(":${project.name}:uploadArchives".toString())
                    }
                }
            }
        })
    }

    static void configurePom(MavenPom pom, ProjectConfigurationExtension effectiveConfig, PomOptions pomOptions) {
        pom.name.set(effectiveConfig.info.name)
        pom.description.set(effectiveConfig.info.description)
        if (isOverwriteAllowed(pomOptions, pomOptions.overwriteUrl)) pom.url.set(effectiveConfig.info.url)
        if (isOverwriteAllowed(pomOptions, pomOptions.overwriteInceptionYear)) pom.inceptionYear.set(effectiveConfig.info.inceptionYear)

        if (isOverwriteAllowed(pomOptions, pomOptions.overwriteLicenses)) {
            pom.licenses(new Action<MavenPomLicenseSpec>() {
                @Override
                void execute(MavenPomLicenseSpec licenses) {
                    effectiveConfig.licensing.licenses.forEach { License lic ->
                        licenses.license(new Action<MavenPomLicense>() {
                            @Override
                            void execute(MavenPomLicense license) {
                                license.name.set(lic.name)
                                license.url.set(lic.url)
                                license.distribution.set(lic.distribution)
                                if (lic.comments) license.comments.set(lic.comments)
                            }
                        })
                    }
                }
            })
        }

        if (isOverwriteAllowed(pomOptions, pomOptions.overwriteScm)) {
            if (isNotBlank(effectiveConfig.info.scm.url)) {
                pom.scm(new Action<MavenPomScm>() {
                    @Override
                    void execute(MavenPomScm scm) {
                        scm.url.set(effectiveConfig.info.scm.url)
                        if (effectiveConfig.info.scm.tag) {
                            scm.tag.set(effectiveConfig.info.scm.tag)
                        }
                        if (effectiveConfig.info.scm.connection) {
                            scm.connection.set(effectiveConfig.info.scm.connection)
                        }
                        if (effectiveConfig.info.scm.connection) {
                            scm.developerConnection.set(effectiveConfig.info.scm.developerConnection)
                        }
                    }
                })
            } else if (effectiveConfig.info.links.scm) {
                pom.scm(new Action<MavenPomScm>() {
                    @Override
                    void execute(MavenPomScm scm) {
                        scm.url.set(effectiveConfig.info.links.scm)
                    }
                })
            }
        }

        if (isOverwriteAllowed(pomOptions, pomOptions.overwriteOrganization)) {
            if (!effectiveConfig.info.organization.isEmpty()) {
                pom.organization(new Action<MavenPomOrganization>() {
                    @Override
                    void execute(MavenPomOrganization organization) {
                        organization.name.set(effectiveConfig.info.organization.name)
                        organization.url.set(effectiveConfig.info.organization.url)
                    }
                })
            }
        }

        if (isOverwriteAllowed(pomOptions, pomOptions.overwriteDevelopers)) {
            pom.developers(new Action<MavenPomDeveloperSpec>() {
                @Override
                void execute(MavenPomDeveloperSpec developers) {
                    effectiveConfig.info.people.forEach { Person person ->
                        if ('developer' in person.roles*.toLowerCase()) {
                            developers.developer(new Action<MavenPomDeveloper>() {
                                @Override
                                void execute(MavenPomDeveloper developer) {
                                    if (person.id) developer.id.set(person.id)
                                    if (person.name) developer.name.set(person.name)
                                    if (person.url) developer.url.set(person.url)
                                    if (person.email) developer.email.set(person.email)
                                    if (person.organization?.name) developer.organization.set(person.organization.name)
                                    if (person.organization?.url) developer.organizationUrl.set(person.organization.url)
                                    if (person.roles) developer.roles.set(person.roles as Set)
                                    if (person.properties) developer.properties.set(person.properties)
                                }
                            })
                        }
                    }
                }
            })
        }

        if (isOverwriteAllowed(pomOptions, pomOptions.overwriteContributors)) {
            pom.contributors(new Action<MavenPomContributorSpec>() {
                @Override
                void execute(MavenPomContributorSpec contributors) {
                    effectiveConfig.info.people.forEach { Person person ->
                        if ('contributor' in person.roles*.toLowerCase()) {
                            contributors.contributor(new Action<MavenPomContributor>() {
                                @Override
                                void execute(MavenPomContributor contributor) {
                                    if (person.name) contributor.name.set(person.name)
                                    if (person.url) contributor.url.set(person.url)
                                    if (person.email) contributor.email.set(person.email)
                                    if (person.organization?.name) contributor.organization.set(person.organization.name)
                                    if (person.organization?.url) contributor.organizationUrl.set(person.organization.url)
                                    if (person.roles) contributor.roles.set(person.roles as Set)
                                    if (person.properties) contributor.properties.set(person.properties)
                                }
                            })
                        }
                    }
                }
            })
        }

        if (isOverwriteAllowed(pomOptions, pomOptions.overwriteIssueManagement)) {
            pom.issueManagement(new Action<MavenPomIssueManagement>() {
                @Override
                void execute(MavenPomIssueManagement issueManagement) {
                    if (isNotBlank(effectiveConfig.info.issueManagement.system)) issueManagement.system.set(effectiveConfig.info.issueManagement.system)
                    if (isNotBlank(effectiveConfig.info.issueManagement.url)) issueManagement.url.set(effectiveConfig.info.issueManagement.url)
                }
            })
        }

        if (isOverwriteAllowed(pomOptions, pomOptions.overwriteCiManagement)) {
            pom.ciManagement(new Action<MavenPomCiManagement>() {
                @Override
                void execute(MavenPomCiManagement ciManagement) {
                    if (isNotBlank(effectiveConfig.info.ciManagement.system)) ciManagement.system.set(effectiveConfig.info.ciManagement.system)
                    if (isNotBlank(effectiveConfig.info.ciManagement.url)) ciManagement.url.set(effectiveConfig.info.ciManagement.url)
                }
            })
        }

        if (isOverwriteAllowed(pomOptions, pomOptions.overwriteMailingLists)) {
            pom.mailingLists(new Action<MavenPomMailingListSpec>() {
                @Override
                void execute(MavenPomMailingListSpec mailingLists) {
                    effectiveConfig.info.mailingLists.forEach { MailingList ml ->
                        mailingLists.mailingList(new Action<MavenPomMailingList>() {
                            @Override
                            void execute(MavenPomMailingList mailingList) {
                                if (isNotBlank(ml.name)) mailingList.name.set(ml.name)
                                if (isNotBlank(ml.subscribe)) mailingList.subscribe.set(ml.subscribe)
                                if (isNotBlank(ml.unsubscribe)) mailingList.unsubscribe.set(ml.unsubscribe)
                                if (isNotBlank(ml.post)) mailingList.post.set(ml.post)
                                if (isNotBlank(ml.archive)) mailingList.archive.set(ml.archive)
                                if (ml.otherArchives) mailingList.otherArchives.set(ml.otherArchives as Set)
                            }
                        })
                    }
                }
            })
        }

        if (isNotBlank(pomOptions.parent)) {
            Dependency parentPom = Dependency.parseDependency(effectiveConfig.project, pomOptions.parent, true)
            pom.withXml {
                Node parentNode = new XmlParser().parseText("""
                    <parent>
                        <groupId>${parentPom.groupId}</groupId>
                        <artifactId>${parentPom.artifactId}</artifactId>
                        <version>${parentPom.version}</version>
                    </parent>
                """)
                it.asNode().children().add(1, parentNode)
            }
        }
    }

    private static boolean isOverwriteAllowed(PomOptions pom, boolean option) {
        isBlank(pom.parent) || (isNotBlank(pom.parent) && option)
    }
}
