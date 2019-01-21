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
package org.kordamp.gradle.plugin.bintray

import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.publishing.PublishingPlugin

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
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
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)

        if (!effectiveConfig.bintray.enabled || !project.sourceSets.findByName('main')) {
            project.getTasks().findByName(BintrayUploadTask.TASK_NAME)?.enabled = false
            setEnabled(false)
            return
        }

        project.bintray {
            user = effectiveConfig.bintray.credentials.username
            key = effectiveConfig.bintray.credentials.password
            publications = ['main']
            pkg {
                repo = effectiveConfig.bintray.repo
                userOrg = effectiveConfig.bintray.userOrg
                name = effectiveConfig.bintray.name
                desc = effectiveConfig.info.description
                licenses = effectiveConfig.license.resolveBintrayLicenseIds()
                labels = effectiveConfig.info.tags
                websiteUrl = effectiveConfig.info.url
                issueTrackerUrl = effectiveConfig.info.links.issueTracker
                vcsUrl = BintrayPlugin.resolveScmLink(effectiveConfig)
                publicDownloadNumbers = true
                githubRepo = effectiveConfig.bintray.githubRepo
                version {
                    name = project.version
                    vcsTag = "${effectiveConfig.bintray.name}-${project.version}"
                    if (effectiveConfig.info.credentials.sonatype && !effectiveConfig.bintray.skipMavenSync)
                        mavenCentralSync {
                            sync = true
                            user = effectiveConfig.info.credentials.sonatype.username
                            password = effectiveConfig.info.credentials.sonatype.password
                        }
                }
            }
        }
    }

    private static resolveScmLink(ProjectConfigurationExtension effectiveConfig) {
        if (!isBlank(effectiveConfig.info.scm.url)) {
            return effectiveConfig.info.scm.url
        }
        return effectiveConfig.info.links.scm
    }
}
