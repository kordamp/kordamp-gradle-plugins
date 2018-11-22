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
package org.kordamp.gradle.plugin.publishing

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.plugins.signing.SigningPlugin
import org.kordamp.gradle.plugin.apidoc.ApidocPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.Person
import org.kordamp.gradle.plugin.buildinfo.BuildInfoPlugin
import org.kordamp.gradle.plugin.groovydoc.GroovydocPlugin
import org.kordamp.gradle.plugin.jar.JarPlugin
import org.kordamp.gradle.plugin.javadoc.JavadocPlugin
import org.kordamp.gradle.plugin.source.SourceJarPlugin

import static org.kordamp.gradle.StringUtils.isBlank
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * Configures artifact publication.
 *
 * @author Andres Almiray
 * @since 0.2.0
 */
class PublishingPlugin implements Plugin<Project> {
    private static final String VISITED = PublishingPlugin.class.name.replace('.', '_') + '_VISITED'

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
        if (!project.plugins.findPlugin(PublishingPlugin)) {
            project.plugins.apply(PublishingPlugin)
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
        BuildInfoPlugin.applyIfMissing(project)
        SourceJarPlugin.applyIfMissing(project)
        ApidocPlugin.applyIfMissing(project)
        JarPlugin.applyIfMissing(project)

        project.plugins.withType(JavaBasePlugin) {
            if (!project.plugins.findPlugin(MavenPublishPlugin)) {
                project.plugins.apply(MavenPublishPlugin)
            }
        }

        project.afterEvaluate {
            project.plugins.withType(JavaBasePlugin) {
               updatePublications(project)
            }
        }
    }

    private void updatePublications(Project project) {
        ProjectConfigurationExtension mergedConfiguration = project.ext.mergedConfiguration

        if (!mergedConfiguration.publishing.enabled || !project.sourceSets.findByName('main')) {
            return
        }

        Task javadocJar = project.tasks.findByName(JavadocPlugin.JAVADOC_JAR_TASK_NAME)
        Task groovydocJar = project.tasks.findByName(GroovydocPlugin.GROOVYDOC_JAR_TASK_NAME)
        Task sourceJar = project.tasks.findByName(SourceJarPlugin.SOURCE_JAR_TASK_NAME)

        project.publishing {
            publications {
                mainPublication(MavenPublication) {
                    from project.components.java

                    if (javadocJar?.enabled) artifact javadocJar
                    if (groovydocJar?.enabled) artifact groovydocJar
                    if (sourceJar?.enabled) artifact sourceJar

                    pom {
                        name = mergedConfiguration.info.name
                        description = mergedConfiguration.info.description
                        url = mergedConfiguration.info.url
                        inceptionYear = mergedConfiguration.info.inceptionYear
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
                        if (!isBlank(mergedConfiguration.info.scm.url)) {
                            scm {
                                url = mergedConfiguration.info.scm.url
                                if (mergedConfiguration.info.scm.connection) {
                                    connection = mergedConfiguration.info.scm.connection
                                }
                                if (mergedConfiguration.info.scm.connection) {
                                    developerConnection = mergedConfiguration.info.scm.developerConnection
                                }
                            }
                        } else if (mergedConfiguration.info.links.scm) {
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
}
