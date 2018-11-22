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
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.plugins.signing.SigningPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.Person

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * Generates a BOM file for the given inputs.
 *
 * @author Andres Almiray
 * @since 0.9.0
 */
class BomPlugin implements Plugin<Project> {
    private static final String VISITED = BomPlugin.class.name.replace('.', '_') + '_VISITED'

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
        String visitedPropertyName = VISITED + '_' + project.name
        if (project.findProperty(visitedPropertyName)) {
            return
        }
        project.ext[visitedPropertyName] = true

        BasePlugin.applyIfMissing(project)

        if (!project.plugins.findPlugin(MavenPublishPlugin)) {
            project.plugins.apply(MavenPublishPlugin)
        }

        project.afterEvaluate {
            updatePublications(project)
        }
    }

    private void updatePublications(Project project) {
        ProjectConfigurationExtension mergedConfiguration = project.ext.mergedConfiguration

        if (!mergedConfiguration.bom.enabled) {
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

        Set<Dependency> compileDeps = mergedConfiguration.bom.compile.collect(parseDependency)
        Set<Dependency> runtimeDeps = mergedConfiguration.bom.runtime.collect(parseDependency)
        Set<Dependency> testDeps = mergedConfiguration.bom.test.collect(parseDependency)

        project.rootProject.subprojects.each { Project prj ->
            if (prj == project) return

            Closure<Boolean> predicate = {
                it.artifactId == prj.name && (it.groupId == project.group || it.groupId == '${project.groupId}')
            }
            if ((!mergedConfiguration.bom.excludes.contains(prj.name) && !mergedConfiguration.bom.excludes.contains(':' + prj.name)) &&
                !compileDeps.find(predicate) && !runtimeDeps.find(predicate) && !testDeps.find(predicate)) {
                compileDeps << new Dependency('${project.groupId}', prj.name, '${project.version}')
            }
        }

        project.publishing {
            publications {
                mainPublication(MavenPublication) {
                    artifacts = []

                    pom {
                        name = mergedConfiguration.info.name
                        description = mergedConfiguration.info.description
                        url = mergedConfiguration.info.url
                        inceptionYear = mergedConfiguration.info.inceptionYear
                        packaging = 'pom'
                        licenses {
                            mergedConfiguration.license.licenses.forEach { lic ->
                                license {
                                    name = lic.name
                                    url = lic.url
                                    distribution = lic.distribution
                                    if (lic.comments) comments = lic.comments
                                }
                            }
                        }
                        if (mergedConfiguration.info.links.scm) {
                            scm {
                                url = mergedConfiguration.info.links.scm
                            }
                        }
                        if (!mergedConfiguration.info.organization.isEmpty()) {
                            organization {
                                name = mergedConfiguration.info.organization.name
                                url = mergedConfiguration.info.organization.url
                            }
                        }
                        developers {
                            mergedConfiguration.info.people.forEach { Person person ->
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
                            mergedConfiguration.info.people.forEach { Person person ->
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

            if (mergedConfiguration.release) {
                if (!isBlank(mergedConfiguration.publishing.releasesRepoUrl)) {
                    repositories {
                        maven {
                            url = mergedConfiguration.publishing.releasesRepoUrl
                        }
                    }
                }
            } else {
                if (!isBlank(mergedConfiguration.publishing.snapshotsRepoUrl)) {
                    repositories {
                        maven {
                            url = mergedConfiguration.publishing.snapshotsRepoUrl
                        }
                    }
                }
            }
        }

        if (mergedConfiguration.publishing.signing) {
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
