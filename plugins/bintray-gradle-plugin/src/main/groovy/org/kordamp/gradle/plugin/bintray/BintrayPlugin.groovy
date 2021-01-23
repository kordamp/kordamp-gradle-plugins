/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2021 Andres Almiray.
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
package org.kordamp.gradle.plugin.bintray

import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.kordamp.gradle.annotations.DependsOn
import org.kordamp.gradle.listener.ProjectEvaluatedListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.Bintray
import org.kordamp.gradle.plugin.publishing.PublishingPlugin

import javax.inject.Named

import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addProjectEvaluatedListener
import static org.kordamp.gradle.util.PluginUtils.resolveConfig
import static org.kordamp.gradle.util.StringUtils.isBlank
import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * Configures artifact publication via Bintray.
 *
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
class BintrayPlugin extends AbstractKordampPlugin {
    Project project

    BintrayPlugin() {
        super(Bintray.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        configureProject(project)
        project.childProjects.values().each {
            it.pluginManager.apply(BintrayPlugin)
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(BintrayPlugin)) {
            project.pluginManager.apply(BintrayPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        PublishingPlugin.applyIfMissing(project)

        BintrayProjectEvaluatedListener listener = new BintrayProjectEvaluatedListener()

        project.pluginManager.withPlugin('java-base') {
            if (!project.plugins.findPlugin(com.jfrog.bintray.gradle.BintrayPlugin)) {
                project.pluginManager.apply(com.jfrog.bintray.gradle.BintrayPlugin)
            }

            addProjectEvaluatedListener(project, listener)
        }

        project.pluginManager.withPlugin('java-platform') {
            if (!project.plugins.findPlugin(com.jfrog.bintray.gradle.BintrayPlugin)) {
                project.pluginManager.apply(com.jfrog.bintray.gradle.BintrayPlugin)
            }

            addProjectEvaluatedListener(project, listener)
        }
    }

    @Named('bintray')
    @DependsOn(['publishing'])
    private class BintrayProjectEvaluatedListener implements ProjectEvaluatedListener {
        @Override
        void projectEvaluated(Project project) {
            updatePublications(project)
        }
    }

    @CompileDynamic
    private void updatePublications(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)
        setEnabled(config.bintray.enabled)

        if (!config.bintray.enabled) {
            project.tasks.findByName(BintrayUploadTask.TASK_NAME)?.enabled = false
            setEnabled(false)
            return
        }

        Set<String> publications = []
        PublishingExtension pubExt = project.extensions.findByType(PublishingExtension)
        publications.addAll(config.bintray.resolvePublications())
        publications.addAll(config.publishing.publications)
        publications = publications.grep { String pub -> pubExt.publications.findByName(pub) }

        BintrayExtension bintray = project.extensions.findByType(BintrayExtension)
        if (isBlank(bintray.user)) bintray.user = config.bintray.credentials.username
        if (isBlank(bintray.key)) bintray.key = config.bintray.credentials.password
        bintray.publish = config.bintray.publish
        if (!bintray.publications) bintray.publications = publications.toArray(new String[0])
        if (isBlank(bintray.pkg.repo)) bintray.pkg.repo = config.bintray.repo
        if (isBlank(bintray.pkg.userOrg)) bintray.pkg.userOrg = config.bintray.userOrg
        if (isBlank(bintray.pkg.name)) bintray.pkg.name = config.bintray.name
        if (isBlank(bintray.pkg.desc)) bintray.pkg.desc = config.info.description
        if (isBlank(bintray.pkg.websiteUrl) && isNotBlank(config.info.url)) bintray.pkg.websiteUrl = config.info.url
        if (isBlank(bintray.pkg.issueTrackerUrl)) bintray.pkg.issueTrackerUrl = config.info.links.issueTracker
        if (isBlank(bintray.pkg.vcsUrl)) bintray.pkg.vcsUrl = config.info.resolveScmLink()
        if (!bintray.pkg.licenses) bintray.pkg.licenses = config.licensing.resolveBintrayLicenseIds().toArray(new String[0])
        if (!bintray.pkg.labels) bintray.pkg.labels = config.info.tags.toArray(new String[0])
        bintray.pkg.publicDownloadNumbers = config.bintray.publicDownloadNumbers
        if (isBlank(bintray.pkg.githubRepo)) bintray.pkg.githubRepo = config.bintray.githubRepo
        if (isBlank(bintray.pkg.version.name)) bintray.pkg.version.name = project.version
        if (isBlank(bintray.pkg.version.vcsTag)) bintray.pkg.version.vcsTag = "${config.bintray.name}-${project.version}".toString()
        if (config.info.credentials.sonatype && !config.bintray.skipMavenSync) {
            bintray.pkg.version.mavenCentralSync.with {
                sync = true
                user = config.info.credentials.sonatype.username
                password = config.info.credentials.sonatype.password
            }
        }
    }
}
