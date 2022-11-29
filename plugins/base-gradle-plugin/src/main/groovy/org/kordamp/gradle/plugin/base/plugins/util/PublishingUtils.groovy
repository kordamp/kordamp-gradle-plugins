/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2022 Andres Almiray.
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
import groovy.xml.QName
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.ExcludeRule
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.attributes.Category
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
import org.kordamp.gradle.plugin.base.model.License
import org.kordamp.gradle.plugin.base.model.MailingList
import org.kordamp.gradle.plugin.base.model.Person
import org.kordamp.gradle.plugin.base.model.PomOptions
import org.kordamp.gradle.plugin.base.model.artifact.Dependency
import org.kordamp.gradle.plugin.base.model.artifact.Platform
import org.kordamp.gradle.plugin.base.model.artifact.internal.DependencyUtils
import org.kordamp.gradle.plugin.base.plugins.Publishing

import static org.kordamp.gradle.util.PluginUtils.resolveConfig
import static org.kordamp.gradle.util.StringUtils.isBlank
import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.13.0
 */
@CompileStatic
class PublishingUtils {
    static Publication configurePublication(Project project, String publicationName) {
        if (!publicationName) return

        ProjectConfigurationExtension config = resolveConfig(project)
        PublishingExtension publishingExtension = project.extensions.findByType(PublishingExtension)
        Publication publication = publishingExtension.publications.findByName(publicationName)
        if (publication instanceof MavenPublication) {
            MavenPublication mavenPublication = (MavenPublication) publication
            configurePom(mavenPublication.pom, config, config.publishing.pom)
        }
        configureSigning(config, project, publicationName)
        publication
    }

    static void configurePublications(Project project, String... publications) {
        if (!publications) return

        ProjectConfigurationExtension config = resolveConfig(project)
        PublishingExtension publishingExtension = project.extensions.findByType(PublishingExtension)
        publications.each { String publicationName ->
            Publication publication = publishingExtension.publications.findByName(publicationName)
            if (publication instanceof MavenPublication) {
                MavenPublication mavenPublication = (MavenPublication) publication
                configurePom(mavenPublication.pom, config, config.publishing.pom)
            }
        }
        configureSigning(config, project, publications)
    }

    static void configureAllPublications(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)
        PublishingExtension publishingExtension = project.extensions.findByType(PublishingExtension)
        SigningExtension signingExtension = project.extensions.findByType(SigningExtension)
        List<String> publications = publishingExtension.publications*.name

        publications.each { String publicationName ->
            Publication publication = publishingExtension.publications.findByName(publicationName)
            if (publication instanceof MavenPublication) {
                MavenPublication mavenPublication = (MavenPublication) publication
                configurePom(mavenPublication.pom, config, config.publishing.pom)
            }

            if (config.publishing.signing.getEnabled()) {
                configureSigningExtension(signingExtension, config.publishing.signing)
                signingExtension.sign(publication)
            }
        }

        project.tasks.withType(Sign, new Action<Sign>() {
            @Override
            void execute(Sign t) {
                t.onlyIf {
                    return config.publishing.signing.getEnabled()
                }
            }
        })
    }

    static void configureSigning(ProjectConfigurationExtension config, Project project, String... publications) {
        SigningExtension signingExtension = project.extensions.findByType(SigningExtension)
        PublishingExtension publishingExtension = project.extensions.findByType(PublishingExtension)

        if (!publications) {
            Publication publication = publishingExtension.publications.findByName('main')

            if (publication && config.publishing.signing.getEnabled()) {
                configureSigningExtension(signingExtension, config.publishing.signing)
                signingExtension.sign(publication)
            }
        } else {
            for (String publicationName : publications) {
                if (publicationName.contains('PluginMarker')) continue
                Publication publication = publishingExtension.publications.findByName(publicationName)
                if (publication && config.publishing.signing.getEnabled()) {
                    configureSigningExtension(signingExtension, config.publishing.signing)
                    signingExtension.sign(publication)
                }
            }
        }

        project.tasks.withType(Sign, new Action<Sign>() {
            @Override
            void execute(Sign t) {
                t.onlyIf {
                    return config.publishing.signing.getEnabled()
                }
            }
        })
    }

    static void configureSigningExtension(SigningExtension signingExtension, Publishing.Signing signing) {
        if (isNotBlank(signing.keyId) && isNotBlank(signing.secretKey) && isNotBlank(signing.password)) {
            signingExtension.useInMemoryPgpKeys(
                signing.keyId,
                signing.secretKey,
                signing.password)
        } else if (isNotBlank(signing.secretKey) && isNotBlank(signing.password)) {
            signingExtension.useInMemoryPgpKeys(
                signing.secretKey,
                signing.password)
        }
    }

    static boolean isPlatformDependency(org.gradle.api.artifacts.Dependency dep) {
        if (dep instanceof ModuleDependency) {
            ModuleDependency md = (ModuleDependency) dep
            String value = String.valueOf(md.attributes.getAttribute(Category.CATEGORY_ATTRIBUTE))
            return Category.REGULAR_PLATFORM.equals(value) || Category.ENFORCED_PLATFORM.equals(value)
        }
        return false
    }

    static void configureDependencies(MavenPom pom, ProjectConfigurationExtension config, Project project, Map<String, String> expressions) {
        Closure<Boolean> filter = { org.gradle.api.artifacts.Dependency d ->
            d.name != 'unspecified'
        }

        Map<String, Platform> platformDependencies = [:]
        Map<String, org.gradle.api.artifacts.Dependency> compileDependencies = [:]
        Map<String, org.gradle.api.artifacts.Dependency> runtimeDependencies = [:]
        Map<String, org.gradle.api.artifacts.Dependency> testDependencies = [:]
        Map<String, org.gradle.api.artifacts.Dependency> providedDependencies = [:]

        Closure<Void> processDependency = { Map<String, org.gradle.api.artifacts.Dependency> bucket, org.gradle.api.artifacts.Dependency dep ->
            String coordinates = "${dep.group}:${dep.name}:${dep.version}".toString()
            if (PublishingUtils.isPlatformDependency(dep)) {
                platformDependencies.put(coordinates, asPlatform(dep))
            } else {
                bucket.put(coordinates, dep)
            }
            return null
        }

        if (project.configurations.findByName('compile')) {
            project.configurations.findByName('compile')
                .allDependencies.findAll(filter)
                .each(processDependency.curry(compileDependencies))
        }

        if (project.configurations.findByName('runtime')) {
            project.configurations.findByName('runtime')
                .allDependencies.findAll(filter)
                .each(processDependency.curry(runtimeDependencies))
        }

        if (project.configurations.findByName('testRuntime')) {
            project.configurations.findByName('testRuntime')
                .allDependencies.findAll(filter)
                .each(processDependency.curry(testDependencies))
        }

        if (project.configurations.findByName('compileOnly')) {
            project.configurations.findByName('compileOnly')
                .allDependencies.findAll(filter)
                .each(processDependency.curry(providedDependencies))
        }

        if (project.configurations.findByName('api')) {
            project.configurations.findByName('api')
                .allDependencies.findAll(filter)
                .each(processDependency.curry(compileDependencies))
        }

        if (project.configurations.findByName('implementation')) {
            project.configurations.findByName('implementation')
                .allDependencies.findAll(filter)
                .each(processDependency.curry(runtimeDependencies))
        }

        if (project.configurations.findByName('runtimeOnly')) {
            project.configurations.findByName('runtimeOnly')
                .allDependencies.findAll(filter)
                .each(processDependency.curry(runtimeDependencies))
        }

        if (project.configurations.findByName('testImplementation')) {
            project.configurations.findByName('testImplementation')
                .allDependencies.findAll(filter)
                .each(processDependency.curry(testDependencies))
        }

        if (project.configurations.findByName('testRuntimeOnly')) {
            project.configurations.findByName('testRuntimeOnly')
                .allDependencies.findAll(filter)
                .each(processDependency.curry(testDependencies))
        }

        compileDependencies.keySet().each { key ->
            runtimeDependencies.remove(key)
            testDependencies.remove(key)
        }
        runtimeDependencies.keySet().each { key ->
            testDependencies.remove(key)
        }

        if (platformDependencies || compileDependencies || runtimeDependencies || testDependencies || providedDependencies) {
            injectDependencies(pom, config, platformDependencies, compileDependencies, runtimeDependencies, testDependencies, providedDependencies, expressions)
        }
    }

    private static Platform asPlatform(org.gradle.api.artifacts.Dependency dependency) {
        return DependencyUtils.platform(
            dependency.name.replace('-','.').replace('_','.'),
            dependency.group,
            dependency.name,
            dependency.version
        )
    }

    @CompileDynamic
    private static void injectDependencies(MavenPom pom,
                                           ProjectConfigurationExtension config,
                                           Map<String, Platform> platformDependencies,
                                           Map<String, org.gradle.api.artifacts.Dependency> compileDependencies,
                                           Map<String, org.gradle.api.artifacts.Dependency> runtimeDependencies,
                                           Map<String, org.gradle.api.artifacts.Dependency> testDependencies,
                                           Map<String, org.gradle.api.artifacts.Dependency> providedDependencies,
                                           Map<String, String> expressions) {
        Set<Platform> platforms = [] as Set

        pom.withXml {
            Node dependenciesNode = asNode().appendNode('dependencies')
            if ('compile' in config.publishing.scopes || !config.publishing.scopes) {
                compileDependencies.values().each { org.gradle.api.artifacts.Dependency dep ->
                    configureDependency(dependenciesNode, dep, config, expressions, platforms, 'compile')
                }
            }
            if ('provided' in config.publishing.scopes) {
                providedDependencies.values().each { org.gradle.api.artifacts.Dependency dep ->
                    configureDependency(dependenciesNode, dep, config, expressions, platforms, 'provided')
                }
            }
            if ('runtime' in config.publishing.scopes || !config.publishing.scopes) {
                runtimeDependencies.values().each { org.gradle.api.artifacts.Dependency dep ->
                    configureDependency(dependenciesNode, dep, config, expressions, platforms, 'runtime')
                }
            }
            if ('test' in config.publishing.scopes) {
                testDependencies.values().each { org.gradle.api.artifacts.Dependency dep ->
                    configureDependency(dependenciesNode, dep, config, expressions, platforms, 'test')
                }
            }

            platformDependencies.values().each { platform ->
                if (!platforms.find { it.groupId == platform.groupId && it.artifactId == platform.artifactId }) {
                    platforms.add(platform)
                }
            }

            if (platforms && !config.publishing.flattenPlatforms) {
                Node dependencyManagementNode = asNode().children().find {
                    (it.name() instanceof QName ? it.name().localPart : it.name()) == 'dependencyManagement'
                }
                if (!dependencyManagementNode) {
                    dependencyManagementNode = new Node(null, 'dependencyManagement')
                    List nodes = dependenciesNode.parent().children()
                    nodes.add(nodes.size() - 1, dependencyManagementNode)
                }

                Node managedDependencies = dependencyManagementNode.children().find {
                    (it.name() instanceof QName ? it.name().localPart : it.name()) == 'dependencies'
                }
                if (!managedDependencies) {
                    managedDependencies = new Node(dependencyManagementNode, 'dependencies')
                }

                for (Platform platform : platforms) {
                    String versionKey = platform.name + '.version'
                    String versionExp = platform.version
                    if (config.publishing.useVersionExpressions) {
                        versionExp = '${' + versionKey + '}'
                        expressions.put(versionKey, platform.version)
                    }

                    managedDependencies.appendNode('dependency').with {
                        appendNode('groupId', platform.groupId)
                        appendNode('artifactId', platform.artifactId)
                        appendNode('version', versionExp)
                        appendNode('scope', 'import')
                        appendNode('type', 'pom')
                    }
                }
            }

            if (config.publishing.useVersionExpressions) {
                Node propertiesNode = asNode().children().find {
                    (it.name() instanceof QName ? it.name().localPart : it.name()) == 'properties'
                }
                if (!propertiesNode) {
                    Node dependencyManagementNode = asNode().children().find {
                        (it.name() instanceof QName ? it.name().localPart : it.name()) == 'dependencyManagement'
                    }
                    int offset = dependencyManagementNode ? 2 : 1
                    propertiesNode = new Node(null, 'properties')
                    List nodes = dependenciesNode.parent().children()
                    nodes.add(nodes.size() - offset, propertiesNode)
                }
                expressions.each { versionKey, versionVal ->
                    if (!(propertiesNode.children().find { it.name() == versionKey })) {
                        propertiesNode.appendNode(versionKey, versionVal)
                    }
                }
            }
        }
    }

    @CompileDynamic
    private static void configureDependency(Node node,
                                            org.gradle.api.artifacts.Dependency dep,
                                            ProjectConfigurationExtension config,
                                            Map<String, String> expressions,
                                            Set<Platform> platforms,
                                            String scope) {
        String versionExp = dep.version

        if (config.publishing.useVersionExpressions) {
            Dependency dependency = config.dependencyManagement.findDependencyByGA(dep.group, dep.name)
            if (dependency) {
                if (config.publishing.flattenPlatforms) {
                    if (dependency instanceof Platform) {
                        if (dependency.artifactId == dep.name) {
                            platforms << (Platform) dependency
                            return
                        } else {
                            String versionKey = dependency.name + '.version'
                            versionExp = '${' + versionKey + '}'
                            expressions.put(versionKey, dependency.version)
                        }
                    } else if (versionExp == dependency.version || !versionExp) {
                        String versionKey = dependency.name + '.version'
                        versionExp = '${' + versionKey + '}'
                        expressions.put(versionKey, dependency.version)
                    }
                } else {
                    if (dependency instanceof Platform) {
                        if (dependency.artifactId == dep.name) {
                            platforms << (Platform) dependency
                            return
                        } else {
                            versionExp = ''
                        }
                    } else if (versionExp == dependency.version || !versionExp) {
                        String versionKey = dependency.name + '.version'
                        versionExp = '${' + versionKey + '}'
                        expressions.put(versionKey, dependency.version)
                    }
                }
            }
        } else {
            Dependency dependency = config.dependencyManagement.findDependencyByGA(dep.group, dep.name)
            if (dependency) {
                if (config.publishing.flattenPlatforms) {
                    versionExp = dependency.version
                    if (dependency instanceof Platform && dependency.artifactId == dep.name) {
                        platforms << (Platform) dependency
                        return
                    }
                } else {
                    if (dependency instanceof Platform) {
                        if (dependency.artifactId == dep.name) {
                            platforms << (Platform) dependency
                            return
                        } else {
                            versionExp = ''
                        }
                    } else {
                        versionExp = dependency.version
                    }
                }
            }
        }

        node = node.appendNode('dependency')
        node.with {
            appendNode('groupId', dep.group)
            appendNode('artifactId', dep.name)
            if (versionExp) appendNode('version', versionExp)
            if (scope != 'compile') appendNode('scope', scope)
            if (isOptional(config.project, dep)) {
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

    static void configurePom(MavenPom pom, ProjectConfigurationExtension config, PomOptions pomOptions) {
        pom.name.set(config.info.name)
        pom.description.set(config.info.description)
        pom.packaging = pomOptions.packaging
        if (isOverwriteAllowed(pomOptions, pomOptions.overwriteUrl) && isNotBlank(config.info.url)) pom.url.set(config.info.url)
        if (isOverwriteAllowed(pomOptions, pomOptions.overwriteInceptionYear)) pom.inceptionYear.set(config.info.inceptionYear)
        pom.properties.set(pomOptions.properties)

        if (isOverwriteAllowed(pomOptions, pomOptions.overwriteLicenses)) {
            pom.licenses(new Action<MavenPomLicenseSpec>() {
                @Override
                void execute(MavenPomLicenseSpec licenses) {
                    config.licensing.licenses.forEach { License lic ->
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
            if (config.info.scm.enabled) {
                if (isNotBlank(config.info.scm.url)) {
                    pom.scm(new Action<MavenPomScm>() {
                        @Override
                        void execute(MavenPomScm scm) {
                            scm.url.set(config.info.scm.url)
                            if (config.info.scm.tag) {
                                scm.tag.set(config.info.scm.tag)
                            }
                            if (config.info.scm.connection) {
                                scm.connection.set(config.info.scm.connection)
                            }
                            if (config.info.scm.connection) {
                                scm.developerConnection.set(config.info.scm.developerConnection)
                            }
                        }
                    })
                } else if (config.info.links.scm) {
                    pom.scm(new Action<MavenPomScm>() {
                        @Override
                        void execute(MavenPomScm scm) {
                            scm.url.set(config.info.links.scm)
                        }
                    })
                }
            }
        }

        if (isOverwriteAllowed(pomOptions, pomOptions.overwriteOrganization)) {
            if (!config.info.organization.empty) {
                pom.organization(new Action<MavenPomOrganization>() {
                    @Override
                    void execute(MavenPomOrganization organization) {
                        organization.name.set(config.info.organization.name)
                        organization.url.set(config.info.organization.url)
                    }
                })
            }
        }

        if (isOverwriteAllowed(pomOptions, pomOptions.overwriteDevelopers)) {
            pom.developers(new Action<MavenPomDeveloperSpec>() {
                @Override
                void execute(MavenPomDeveloperSpec developers) {
                    config.info.people.forEach { Person person ->
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
                    config.info.people.forEach { Person person ->
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

        if (!config.info.issueManagement.empty && isOverwriteAllowed(pomOptions, pomOptions.overwriteIssueManagement)) {
            pom.issueManagement(new Action<MavenPomIssueManagement>() {
                @Override
                void execute(MavenPomIssueManagement issueManagement) {
                    if (isNotBlank(config.info.issueManagement.system)) issueManagement.system.set(config.info.issueManagement.system)
                    if (isNotBlank(config.info.issueManagement.url)) issueManagement.url.set(config.info.issueManagement.url)
                }
            })
        }

        if (!config.info.ciManagement.empty && isOverwriteAllowed(pomOptions, pomOptions.overwriteCiManagement)) {
            pom.ciManagement(new Action<MavenPomCiManagement>() {
                @Override
                void execute(MavenPomCiManagement ciManagement) {
                    if (isNotBlank(config.info.ciManagement.system)) ciManagement.system.set(config.info.ciManagement.system)
                    if (isNotBlank(config.info.ciManagement.url)) ciManagement.url.set(config.info.ciManagement.url)
                }
            })
        }

        if (!config.info.mailingLists.empty && isOverwriteAllowed(pomOptions, pomOptions.overwriteMailingLists)) {
            pom.mailingLists(new Action<MavenPomMailingListSpec>() {
                @Override
                void execute(MavenPomMailingListSpec mailingLists) {
                    config.info.mailingLists.forEach { MailingList ml ->
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
            Dependency parentPom = DependencyUtils.parseDependency(config.project.rootProject, 'parent', pomOptions.parent)
                .asDependency()

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
