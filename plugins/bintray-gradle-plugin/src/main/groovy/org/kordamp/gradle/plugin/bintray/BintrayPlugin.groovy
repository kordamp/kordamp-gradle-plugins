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
package org.kordamp.gradle.plugin.bintray

import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.publishing.PublishingPlugin

import static org.kordamp.gradle.StringUtils.isBlank
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * Configures artifact publication via Bintray.
 *
 * @author Andres Almiray
 * @since 0.2.0
 */
class BintrayPlugin extends AbstractKordampPlugin {
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
        if (!project.plugins.findPlugin(BintrayPlugin)) {
            project.plugins.apply(BintrayPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        PublishingPlugin.applyIfMissing(project)

        project.plugins.withType(JavaBasePlugin) {
            if (!project.plugins.findPlugin(com.jfrog.bintray.gradle.BintrayPlugin)) {
                project.plugins.apply(com.jfrog.bintray.gradle.BintrayPlugin)
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

        if (!mergedConfiguration.bintray.enabled || !project.sourceSets.findByName('main')) {
            project.getTasks().findByName(BintrayUploadTask.TASK_NAME)?.enabled = false
            setEnabled(false)
            return
        }

        project.bintray {
            user = mergedConfiguration.bintray.credentials.username
            key = mergedConfiguration.bintray.credentials.password
            publications = ['mainPublication']
            pkg {
                repo = mergedConfiguration.bintray.repo
                userOrg = mergedConfiguration.bintray.userOrg
                name = mergedConfiguration.bintray.name
                desc = mergedConfiguration.info.description
                licenses = mergedConfiguration.license.resolveBintrayLicenseIds()
                labels = mergedConfiguration.info.tags
                websiteUrl = mergedConfiguration.info.url
                issueTrackerUrl = mergedConfiguration.info.links.issueTracker
                vcsUrl = BintrayPlugin.resolveScmLink(mergedConfiguration)
                publicDownloadNumbers = true
                githubRepo = mergedConfiguration.bintray.githubRepo
                version {
                    name = project.version
                    vcsTag = "${mergedConfiguration.bintray.name}-${project.version}"
                    if (mergedConfiguration.info.credentials.sonatype && !mergedConfiguration.bintray.skipMavenSync)
                        mavenCentralSync {
                            sync = true
                            user = mergedConfiguration.info.credentials.sonatype.username
                            password = mergedConfiguration.info.credentials.sonatype.password
                        }
                }
            }
        }
    }

    private static resolveScmLink(ProjectConfigurationExtension mergedConfiguration) {
        if (!isBlank(mergedConfiguration.info.scm.url)) {
            return mergedConfiguration.info.scm.url
        }
        return mergedConfiguration.info.links.scm
    }
}
