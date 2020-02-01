/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2020 Andres Almiray.
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
package org.kordamp.gradle.plugin.publishing

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.plugins.signing.SigningPlugin
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.Credentials
import org.kordamp.gradle.plugin.base.model.Repository
import org.kordamp.gradle.plugin.base.plugins.util.PublishingUtils
import org.kordamp.gradle.plugin.buildinfo.BuildInfoPlugin
import org.kordamp.gradle.plugin.jar.JarPlugin
import org.kordamp.gradle.plugin.source.SourceJarPlugin

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.PluginUtils.resolveSourceSets
import static org.kordamp.gradle.StringUtils.isNotBlank

/**
 * Configures artifact publication.
 *
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
class PublishingPlugin extends AbstractKordampPlugin {
    Project project

    void apply(Project project) {
        this.project = project

        configureProject(project)
        project.childProjects.values().each {
            it.pluginManager.apply(PublishingPlugin)
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(PublishingPlugin)) {
            project.pluginManager.apply(PublishingPlugin)
        }
        if (!project.plugins.findPlugin(SigningPlugin)) {
            project.pluginManager.apply(SigningPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        BuildInfoPlugin.applyIfMissing(project)

        project.pluginManager.withPlugin('java-base') {
            SourceJarPlugin.applyIfMissing(project)
            JarPlugin.applyIfMissing(project)

            if (!project.pluginManager.hasPlugin('maven-publish')) {
                project.pluginManager.apply(MavenPublishPlugin)
            }
        }

        project.pluginManager.withPlugin('java-platform') {
            if (!project.pluginManager.hasPlugin('maven-publish')) {
                project.pluginManager.apply(MavenPublishPlugin)
            }
        }

        project.pluginManager.withPlugin('maven-publish') {
            project.afterEvaluate {
                updatePublications(project)
            }
        }

        project.tasks.register('publicationSettings', PublicationSettingsTask,
            new Action<PublicationSettingsTask>() {
                @Override
                void execute(PublicationSettingsTask t) {
                    t.group = 'Insight'
                    t.description = 'Display publication settings.'
                }
            })

        project.tasks.addRule('Pattern: <PublicationName>PublicationSettings: Displays settings of a Publication.', new Action<String>() {
            @Override
            void execute(String publicationName) {
                if (publicationName.endsWith('PublicationSettings')) {
                    String resolvedPublicationName = publicationName - 'Settings'
                    project.tasks.register(publicationName, PublicationSettingsTask,
                        new Action<PublicationSettingsTask>() {
                            @Override
                            void execute(PublicationSettingsTask t) {
                                t.group = 'Insight'
                                t.publication = resolvedPublicationName
                                t.description = "Display settings of the '${resolvedPublicationName}' Publication."
                            }
                        })
                }
            }
        })
    }

    @CompileDynamic
    private void updatePublications(Project project) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)

        if (!effectiveConfig.publishing.enabled) {
            setEnabled(false)
            return
        }

        project.publishing {
            publications {
                effectiveConfig.publishing.publications.each { String pub ->
                    "${pub}"(MavenPublication) {
                        PublishingUtils.configurePom(pom, effectiveConfig, effectiveConfig.publishing.pom)
                    }
                }

                if (!effectiveConfig.publishing.publications.contains('main') && !effectiveConfig.publishing.publications) {
                    main(MavenPublication) {
                        if (project.pluginManager.hasPlugin('java-platform')) {
                            from project.components.javaPlatform
                        } else {
                            Task jar = project.tasks.findByName('jar')
                            Task sourcesJar = project.tasks.findByName('sourcesJar')

                            if (effectiveConfig.publishing.filterDependencies) {
                                groupId = project.group
                                artifactId = project.name
                                version = project.version
                                if (jar?.enabled) artifact jar
                                PublishingUtils.configureDependencies(pom, effectiveConfig, project)
                            } else {
                                try {
                                    if (project.components.java) {
                                        from project.components.java
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace()
                                    groupId = project.group
                                    artifactId = project.name
                                    version = project.version
                                    if (jar?.enabled) artifact jar
                                    PublishingUtils.configureDependencies(pom, effectiveConfig, project)
                                }
                            }

                            if (sourcesJar?.enabled && !artifacts.find { it.classifier == 'sources' }) artifact sourcesJar
                        }

                        PublishingUtils.configurePom(pom, effectiveConfig, effectiveConfig.publishing.pom)
                    }
                }
            }

            String repositoryName = effectiveConfig.release ? effectiveConfig.publishing.releasesRepository : effectiveConfig.publishing.snapshotsRepository
            if (isNotBlank(repositoryName)) {
                Repository repo = effectiveConfig.info.repositories.getRepository(repositoryName)
                if (repo == null) {
                    throw new IllegalStateException("Repository '${repositoryName}' was not found")
                }

                repositories {
                    maven {
                        name = repositoryName
                        url = repo.url
                        Credentials creds = effectiveConfig.info.credentials.getCredentials(repo.name)
                        if (repo.credentials && !repo.credentials.empty) {
                            credentials {
                                username = repo.credentials.username
                                password = repo.credentials.password
                            }
                        } else if (creds && !creds.empty) {
                            credentials {
                                username = creds.username
                                password = creds.password
                            }
                        }
                    }
                }
            }
        }

        List<String> publications = new ArrayList<>(effectiveConfig.publishing.publications)
        if (!publications) publications << 'main'
        PublishingUtils.configureSigning(effectiveConfig, project, *publications)
    }
}
