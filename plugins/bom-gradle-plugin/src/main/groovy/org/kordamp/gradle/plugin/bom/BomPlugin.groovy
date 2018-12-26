/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 Andres Almiray.
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
package org.kordamp.gradle.plugin.bom

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.plugins.signing.SigningPlugin
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.Credentials
import org.kordamp.gradle.plugin.base.model.Person
import org.kordamp.gradle.plugin.base.model.Repository

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * Generates a BOM file for the given inputs.
 *
 * @author Andres Almiray
 * @since 0.9.0
 */
class BomPlugin extends AbstractKordampPlugin {
    Project project

    void apply(Project project) {
        this.project = project

        configureProject(project)
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(BomPlugin)) {
            project.plugins.apply(BomPlugin)
        }
        if (!project.plugins.findPlugin(SigningPlugin)) {
            project.plugins.apply(SigningPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)

        if (!project.plugins.findPlugin(MavenPublishPlugin)) {
            project.plugins.apply(MavenPublishPlugin)
        }

        project.extensions.findByType(ProjectConfigurationExtension).publishing.enabled = false

        project.afterEvaluate {
            updatePublications(project)
        }
    }

    private void updatePublications(Project project) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        setEnabled(effectiveConfig.bom.enabled)

        if (!enabled) {
            return
        }

        def parseDependency = { String str ->
            String[] parts = str.trim().split(':')
            switch (parts.length) {
                case 0:
                    throw new IllegalStateException("Project '${str}' does not exist")
                case 1:
                    if (!isBlank(parts[0]) && project.rootProject.subprojects.find { it.name == parts[0] }) {
                        return new Dependency('${project.groupId}', parts[0], '${project.version}')
                    }
                    throw new IllegalStateException("Project '${str}' does not exist")
                case 2:
                    if (isBlank(parts[0]) &&
                        !isBlank(parts[1]) &&
                        project.rootProject.subprojects.find { it.name == parts[1] }) {
                        return new Dependency('${project.groupId}', parts[1], '${project.version}')
                    }
                    throw new IllegalStateException("Project '${str}' does not exist")
                case 3:
                    if (isBlank(parts[0]) || isBlank(parts[1]) || isBlank(parts[2])) {
                        throw new IllegalStateException("Invalid BOM dependency '${str}'")
                    }
                    return new Dependency(parts[0], parts[1], parts[2])
            }
        }

        Set<Dependency> compileDeps = effectiveConfig.bom.compile.collect(parseDependency)
        Set<Dependency> runtimeDeps = effectiveConfig.bom.runtime.collect(parseDependency)
        Set<Dependency> testDeps = effectiveConfig.bom.test.collect(parseDependency)

        project.rootProject.subprojects.each { Project prj ->
            if (prj == project) return

            Closure<Boolean> predicate = {
                it.artifactId == prj.name && (it.groupId == project.group || it.groupId == '${project.groupId}')
            }
            if ((!effectiveConfig.bom.excludes.contains(prj.name) && !effectiveConfig.bom.excludes.contains(':' + prj.name)) &&
                !compileDeps.find(predicate) && !runtimeDeps.find(predicate) && !testDeps.find(predicate)) {
                compileDeps << new Dependency('${project.groupId}', prj.name, '${project.version}')
            }
        }

        project.publishing {
            publications {
                mainPublication(MavenPublication) {
                    artifacts = []

                    pom {
                        name = effectiveConfig.info.name
                        description = effectiveConfig.info.description
                        url = effectiveConfig.info.url
                        inceptionYear = effectiveConfig.info.inceptionYear
                        packaging = 'pom'
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
                        if (!effectiveConfig.info.organization.isEmpty()) {
                            organization {
                                name = effectiveConfig.info.organization.name
                                url = effectiveConfig.info.organization.url
                            }
                        }
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

                    pom.withXml {
                        def dependencyManagementNode = asNode().appendNode('dependencyManagement').appendNode('dependencies')
                        compileDeps.each { Dependency dep ->
                            dependencyManagementNode.appendNode('dependency').with {
                                appendNode('groupId', dep.groupId)
                                appendNode('artifactId', dep.artifactId)
                                appendNode('version', dep.version)
                            }
                        }
                        runtimeDeps.each { Dependency dep ->
                            dependencyManagementNode.appendNode('dependency').with {
                                appendNode('groupId', dep.groupId)
                                appendNode('artifactId', dep.artifactId)
                                appendNode('version', dep.version)
                                appendNode('scope', 'runtime')
                            }
                        }
                        testDeps.each { Dependency dep ->
                            dependencyManagementNode.appendNode('dependency').with {
                                appendNode('groupId', dep.groupId)
                                appendNode('artifactId', dep.artifactId)
                                appendNode('version', dep.version)
                                appendNode('scope', 'test')
                            }
                        }
                    }
                }
            }

            String repositoryName = effectiveConfig.release ? effectiveConfig.publishing.releasesRepository : effectiveConfig.publishing.snapshotsRepository
            if (!isBlank(repositoryName)) {
                Repository repo = effectiveConfig.info.repositories.getRepository(repositoryName)
                if (repo == null) {
                    throw new IllegalStateException("Repository '${repositoryName}' was not found")
                }

                repositories {
                    maven {
                        url = repo.url
                        Credentials creds = effectiveConfig.info.credentials.getCredentials(repo.name)
                        if (repo.credentials) {
                            credentials {
                                username = repo.credentials.username
                                password = repo.credentials.password
                            }
                        } else if (creds) {
                            credentials {
                                username = creds.username
                                password = creds.password
                            }
                        }
                    }
                }
            }
        }

        if (effectiveConfig.publishing.signing) {
            project.signing {
                sign project.publishing.publications.mainPublication
            }
        }
    }

    @CompileStatic
    @Canonical
    private static class Dependency {
        String groupId
        String artifactId
        String version
    }
}
