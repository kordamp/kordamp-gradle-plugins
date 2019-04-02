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

import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.SourceSetContainer
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.publishing.PublishingPlugin

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.PluginUtils.resolveSourceSets
import static org.kordamp.gradle.StringUtils.isBlank
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * Configures artifact publication via Bintray.
 *
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
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

        if (!effectiveConfig.bintray.enabled || !((SourceSetContainer) resolveSourceSets(project)).findByName('main')) {
            project.getTasks().findByName(BintrayUploadTask.TASK_NAME)?.enabled = false
            setEnabled(false)
            return
        }

        BintrayExtension bintray = project.extensions.findByType(BintrayExtension)
        if (isBlank(bintray.user)) bintray.user = effectiveConfig.bintray.credentials.username
        if (isBlank(bintray.key)) bintray.key = effectiveConfig.bintray.credentials.password
        if (!bintray.publications) bintray.publications = effectiveConfig.bintray.resolvePublications().toArray(new String[0])
        if (isBlank(bintray.pkg.repo)) bintray.pkg.repo = effectiveConfig.bintray.repo
        if (isBlank(bintray.pkg.userOrg)) bintray.pkg.userOrg = effectiveConfig.bintray.userOrg
        if (isBlank(bintray.pkg.name)) bintray.pkg.name = effectiveConfig.bintray.name
        if (isBlank(bintray.pkg.desc)) bintray.pkg.desc = effectiveConfig.info.description
        if (isBlank(bintray.pkg.websiteUrl)) bintray.pkg.websiteUrl = effectiveConfig.info.url
        if (isBlank(bintray.pkg.issueTrackerUrl)) bintray.pkg.issueTrackerUrl = effectiveConfig.info.links.issueTracker
        if (isBlank(bintray.pkg.vcsUrl)) bintray.pkg.vcsUrl = effectiveConfig.info.resolveScmLink()
        if (!bintray.pkg.licenses) bintray.pkg.licenses = effectiveConfig.licensing.resolveBintrayLicenseIds().toArray(new String[0])
        if (!bintray.pkg.labels) bintray.pkg.labels = effectiveConfig.info.tags.toArray(new String[0])
        bintray.pkg.publicDownloadNumbers = true // TODO: fin a way to conditionally set this value
        if (isBlank(bintray.pkg.githubRepo)) bintray.pkg.githubRepo = effectiveConfig.bintray.githubRepo
        if (isBlank(bintray.pkg.version.name)) bintray.pkg.version.name = project.version
        if (isBlank(bintray.pkg.version.vcsTag)) bintray.pkg.version.vcsTag = "${effectiveConfig.bintray.name}-${project.version}".toString()
        if (effectiveConfig.info.credentials.sonatype && !effectiveConfig.bintray.skipMavenSync) {
            bintray.pkg.version.mavenCentralSync.with {
                sync = true
                user = effectiveConfig.info.credentials.sonatype.username
                password = effectiveConfig.info.credentials.sonatype.password
            }
        }
    }
}
