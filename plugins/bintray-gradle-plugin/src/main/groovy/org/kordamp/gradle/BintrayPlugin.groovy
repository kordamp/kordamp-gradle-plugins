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
import org.gradle.api.tasks.SourceSet
import org.kordamp.gradle.model.Information

import static org.kordamp.gradle.BasePlugin.isRootProject

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
class BintrayPlugin implements Plugin<Project> {
    static final String VISITED = BintrayPlugin.class.name.replace('.', '_') + '_VISITED'

    Project project

    void apply(Project project) {
        this.project = project

        if (isRootProject(project)) {
            configureBintrayIfNeeded(project)
            project.childProjects.values().each { prj ->
                configureBintrayIfNeeded(prj)
            }
        } else {
            configureBintrayIfNeeded(project)
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(BintrayPlugin)) {
            project.plugins.apply(BintrayPlugin)
        }
    }

    private void configureBintrayIfNeeded(Project project) {
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
            if (!project.plugins.findPlugin(com.jfrog.bintray.gradle.BintrayPlugin)) {
                project.plugins.apply(com.jfrog.bintray.gradle.BintrayPlugin)
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

        project.bintray {
            user = info.bintray.credentials.username
            key = info.bintray.credentials.password
            publications = [sourceSet.name]
            pkg {
                repo = info.bintray.repo
                userOrg = info.bintray.userOrg
                name = info.bintray.name
                desc = info.description
                licenses = info.licenses.resolveBintrayLicenseIds()
                labels = info.tags
                websiteUrl = info.url
                issueTrackerUrl = info.links.issueTracker
                vcsUrl = info.links.scm
                publicDownloadNumbers = true
                githubRepo = info.bintray.githubRepo
                version {
                    name = project.version
                    vcsTag = "${info.bintray.name}-${project.version}"
                    if (info.credentials.sonatype)
                        mavenCentralSync {
                            sync = true
                            user = info.credentials.sonatype.username
                            password = info.credentials.sonatype.password
                        }
                }
            }
        }
    }
}
