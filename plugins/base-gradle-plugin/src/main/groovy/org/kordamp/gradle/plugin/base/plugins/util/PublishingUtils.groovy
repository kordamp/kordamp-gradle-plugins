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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.ExcludeRule
import org.gradle.api.artifacts.ModuleDependency
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
import static org.kordamp.gradle.PluginUtils.supportsApiConfiguration
import static org.kordamp.gradle.StringUtils.isBlank
import static org.kordamp.gradle.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.13.0
 */
@CompileStatic
class PublishingUtils {
    static Publication configurePublication(Project project, String publicationName) {
        if (!publicationName) return

        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        PublishingExtension publishingExtension = project.extensions.findByType(PublishingExtension)
        Publication publication = publishingExtension.publications.findByName(publicationName)
        if (publication instanceof MavenPublication) {
            MavenPublication mavenPublication = (MavenPublication) publication
            configurePom(mavenPublication.pom, effectiveConfig, effectiveConfig.publishing.pom)
        }
        configureSigning(effectiveConfig, project, publicationName)
        publication
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
            Publication publication = publishingExtension.publications.findByName('main')
            if (publication) {
                signingExtension.sign(publication)
            }
        } else {
            publications.each { publicationName ->
                Publication publication = publishingExtension.publications.findByName(publicationName)
                if (publication) {
                    signingExtension.sign(publication)
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

    static void configureDependencies(MavenPom pom, ProjectConfigurationExtension config, Project project) {
        Closure<Boolean> filter = { org.gradle.api.artifacts.Dependency d ->
            d.name != 'unspecified'
        }

        Map<String, org.gradle.api.artifacts.Dependency> compileDependencies = project.configurations.findByName('compile')
            .allDependencies.findAll(filter)
            .collectEntries({ [("${it.group}:${it.name}:${it.version}"): it] })

        Map<String, org.gradle.api.artifacts.Dependency> runtimeDependencies = project.configurations.findByName('runtime')
            .allDependencies.findAll(filter)
            .collectEntries({ [("${it.group}:${it.name}:${it.version}"): it] })

        Map<String, org.gradle.api.artifacts.Dependency> testDependencies = project.configurations.findByName('testRuntime')
            .allDependencies.findAll(filter)
            .collectEntries({ [("${it.group}:${it.name}:${it.version}"): it] })

        Map<String, org.gradle.api.artifacts.Dependency> providedDependencies = project.configurations.findByName('compileOnly')
            .allDependencies.findAll(filter)
            .collectEntries({ [("${it.group}:${it.name}:${it.version}"): it] })

        if (supportsApiConfiguration(project)) {
            compileDependencies.putAll(project.configurations.findByName('api')
                ?.allDependencies.findAll(filter)
                ?.collectEntries({ [("${it.group}:${it.name}:${it.version}"): it] }))
            compileDependencies.putAll(project.configurations.findByName('implementation')
                ?.allDependencies.findAll(filter)
                ?.collectEntries({ [("${it.group}:${it.name}:${it.version}"): it] }))

            runtimeDependencies.putAll(project.configurations.findByName('runtimeOnly')
                ?.allDependencies.findAll(filter)
                ?.collectEntries({ [("${it.group}:${it.name}:${it.version}"): it] }))

            testDependencies.putAll(project.configurations.findByName('testImplementation')
                ?.allDependencies.findAll(filter)
                ?.collectEntries({ [("${it.group}:${it.name}:${it.version}"): it] }))
            testDependencies.putAll(project.configurations.findByName('testRuntimeOnly')
                ?.allDependencies.findAll(filter)
                ?.collectEntries({ [("${it.group}:${it.name}:${it.version}"): it] }))
        }

        compileDependencies.keySet().each { key ->
            runtimeDependencies.remove(key)
            testDependencies.remove(key)
        }
        runtimeDependencies.keySet().each { key ->
            testDependencies.remove(key)
        }

        if (compileDependencies || runtimeDependencies || testDependencies || providedDependencies) {
            injectDependencies(pom, config, compileDependencies, runtimeDependencies, testDependencies, providedDependencies)
        }
    }

    @CompileDynamic
    private static void injectDependencies(MavenPom pom,
                                           ProjectConfigurationExtension config,
                                           Map<String, org.gradle.api.artifacts.Dependency> compileDependencies,
                                           Map<String, org.gradle.api.artifacts.Dependency> runtimeDependencies,
                                           Map<String, org.gradle.api.artifacts.Dependency> testDependencies,
                                           Map<String, org.gradle.api.artifacts.Dependency> providedDependencies) {
        pom.withXml {
            Node dependenciesNode = asNode().appendNode('dependencies')
            if ('compile' in config.publishing.scopes || !config.publishing.scopes) {
                compileDependencies.values().each { org.gradle.api.artifacts.Dependency dep ->
                    configureDependency(dependenciesNode.appendNode('dependency'), dep, config.project, 'compile')
                }
            }
            if ('provided' in config.publishing.scopes || !config.publishing.scopes) {
                providedDependencies.values().each { org.gradle.api.artifacts.Dependency dep ->
                    configureDependency(dependenciesNode.appendNode('dependency'), dep, config.project, 'provided')
                }
            }
            if ('runtime' in config.publishing.scopes || !config.publishing.scopes) {
                runtimeDependencies.values().each { org.gradle.api.artifacts.Dependency dep ->
                    configureDependency(dependenciesNode.appendNode('dependency'), dep, config.project, 'runtime')
                }
            }
            if ('test' in config.publishing.scopes || !config.publishing.scopes) {
                testDependencies.values().each { org.gradle.api.artifacts.Dependency dep ->
                    configureDependency(dependenciesNode.appendNode('dependency'), dep, config.project, 'test')
                }
            }
        }
    }

    @CompileDynamic
    private static void configureDependency(Node node, org.gradle.api.artifacts.Dependency dep, Project project, String scope) {
        node.with {
            appendNode('groupId', dep.group)
            appendNode('artifactId', dep.name)
            appendNode('version', dep.version)
            appendNode('scope', scope)
            if (isOptional(project, dep)) {
                appendNode('optional', true)
            }
        }

        if (dep instanceof ModuleDependency) {
            ModuleDependency mdep = (ModuleDependency) dep
            if (mdep.excludeRules.size() > 0) {
                Node exclusions = node.appendNode('exclusions')
                exclusions.with {
                    mdep.excludeRules.each { ExcludeRule rule ->
                        exclusions.appendNode('exclusion').with {
                            appendNode('groupId', rule.group)
                            appendNode('artifactId', rule.module)
                        }
                    }
                }
            }
        }
    }

    @CompileDynamic
    private static boolean isOptional(Project project, org.gradle.api.artifacts.Dependency dependency) {
        project.findProperty('optionalDeps') && project.optionalDeps.contains(dependency)
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
            if (!effectiveConfig.info.organization.empty) {
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

        if (!effectiveConfig.info.issueManagement.empty && isOverwriteAllowed(pomOptions, pomOptions.overwriteIssueManagement)) {
            pom.issueManagement(new Action<MavenPomIssueManagement>() {
                @Override
                void execute(MavenPomIssueManagement issueManagement) {
                    if (isNotBlank(effectiveConfig.info.issueManagement.system)) issueManagement.system.set(effectiveConfig.info.issueManagement.system)
                    if (isNotBlank(effectiveConfig.info.issueManagement.url)) issueManagement.url.set(effectiveConfig.info.issueManagement.url)
                }
            })
        }

        if (!effectiveConfig.info.ciManagement.empty && isOverwriteAllowed(pomOptions, pomOptions.overwriteCiManagement)) {
            pom.ciManagement(new Action<MavenPomCiManagement>() {
                @Override
                void execute(MavenPomCiManagement ciManagement) {
                    if (isNotBlank(effectiveConfig.info.ciManagement.system)) ciManagement.system.set(effectiveConfig.info.ciManagement.system)
                    if (isNotBlank(effectiveConfig.info.ciManagement.url)) ciManagement.url.set(effectiveConfig.info.ciManagement.url)
                }
            })
        }

        if (!effectiveConfig.info.mailingLists.empty && isOverwriteAllowed(pomOptions, pomOptions.overwriteMailingLists)) {
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
