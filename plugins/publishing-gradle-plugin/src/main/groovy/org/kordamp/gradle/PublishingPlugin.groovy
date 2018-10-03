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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.SourceSet
import org.kordamp.gradle.model.Information

import static org.kordamp.gradle.BasePlugin.isRootProject

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
class PublishingPlugin implements Plugin<Project> {
    static final String VISITED = PublishingPlugin.class.name.replace('.', '_') + '_VISITED'

    Project project

    void apply(Project project) {
        this.project = project

        if (isRootProject(project)) {
            createPublicationsIfNeeded(project)
            project.childProjects.values().each { prj ->
                createPublicationsIfNeeded(prj)
            }
        } else {
            createPublicationsIfNeeded(project)
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(PublishingPlugin)) {
            project.plugins.apply(PublishingPlugin)
        }
    }

    private void createPublicationsIfNeeded(Project project) {
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

        project.afterEvaluate { Project prj ->
            prj.plugins.withType(JavaBasePlugin) {
                prj.sourceSets.each { SourceSet ss ->
                    // skip generating a publication for SourceSets that may contain tests
                    if (!ss.name.toLowerCase().contains('test')) {
                        updatePublications(prj, ss)
                    }
                }
            }
        }
    }

    private void updatePublications(Project project, SourceSet sourceSet) {
        Information info = project.ext.mergedInfo

        project.publishing {
            publications {
                "${sourceSet.name}"(MavenPublication) {
                    groupId project.group
                    artifactId project.tasks.findByName(JarPlugin.resolveJarTaskName(sourceSet)).baseName
                    version project.version

                    pom {
                        name = info.name
                        description = info.description
                        url = info.links.website
                        inceptionYear = info.inceptionYear
                        licenses {
                            info.licenses.forEach { lic ->
                                license {
                                    name = lic.name
                                    if (lic.url) url = lic.url
                                    distribution = lic.distribution
                                    if (lic.comments) comments = lic.comments
                                }
                            }
                        }
                        if (!info.organization.isEmpty()) {
                            organization {
                                name = info.organization.name
                                url = info.organization.url
                            }
                        }
                        developers {
                            info.people.forEach { person ->
                                if('developer' in person.roles*.toLowerCase()) {
                                    developer {
                                        if (person.id) id = person.id
                                        if (person.name) name = person.name
                                        if (person.url) url = person.url
                                        if (person.email) email = person.email
                                        if (person.organization?.name) organizationName = person.organization.name
                                        if (person.organization?.url) organizationUrl = person.organization.url
                                        if (person.roles) roles = person.roles as Set
                                    }
                                }
                            }
                        }
                        contributors {
                            info.people.forEach { person ->
                                if('contributor' in person.roles*.toLowerCase()) {
                                    contributor {
                                        if (person.name) name = person.name
                                        if (person.url) url = person.url
                                        if (person.email) email = person.email
                                        if (person.organization?.name) organizationName = person.organization.name
                                        if (person.organization?.url) organizationUrl = person.organization.url
                                        if (person.roles) roles = person.roles as Set
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
