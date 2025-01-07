/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2025 Andres Almiray.
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
import groovy.xml.QName
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.plugins.signing.SigningPlugin
import org.kordamp.gradle.annotations.DependsOn
import org.kordamp.gradle.listener.AllProjectsEvaluatedListener
import org.kordamp.gradle.listener.ProjectEvaluatedListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.Credentials
import org.kordamp.gradle.plugin.base.model.Repository
import org.kordamp.gradle.plugin.base.model.artifact.Dependency
import org.kordamp.gradle.plugin.base.plugins.Publishing
import org.kordamp.gradle.plugin.base.plugins.util.PublishingUtils
import org.kordamp.gradle.plugin.buildinfo.BuildInfoPlugin
import org.kordamp.gradle.plugin.jar.JarPlugin
import org.kordamp.gradle.plugin.source.SourceJarPlugin

import javax.inject.Named

import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addAllProjectsEvaluatedListener
import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addProjectEvaluatedListener
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject
import static org.kordamp.gradle.util.PluginUtils.resolveConfig
import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * Configures artifact publication.
 *
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
class PublishingPlugin extends AbstractKordampPlugin {
    Project project

    PublishingPlugin() {
        super(Publishing.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        configureProject(project)
        project.childProjects.values().each {
            it.pluginManager.apply(PublishingPlugin)
        }
        if (isRootProject(project)) {
            addAllProjectsEvaluatedListener(project, new PublishingAllProjectsEvaluatedListener())
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
            addProjectEvaluatedListener(project, new PublishingProjectEvaluatedListener())
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

    @Named('publishing')
    @DependsOn(['jar', 'source'])
    private class PublishingProjectEvaluatedListener implements ProjectEvaluatedListener {
        @Override
        void projectEvaluated(Project project) {
            updatePublications(project)
        }
    }

    @Named('publishing')
    @DependsOn(['jar', 'source'])
    private class PublishingAllProjectsEvaluatedListener implements AllProjectsEvaluatedListener {
        @Override
        void allProjectsEvaluated(Project rootProject) {
            // noop
        }
    }

    @CompileDynamic
    private void updatePublications(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)

        if (!config.publishing.enabled) {
            setEnabled(false)
            return
        }

        if (project.pluginManager.hasPlugin('java-platform')) {
            config.publishing.pom.packaging = 'pom'
        }

        project.publishing {
            publications {
                for (String pub : config.publishing.publications) {
                    if (pub.contains('PluginMarker')) continue
                    "${pub}"(MavenPublication) {
                        PublishingUtils.configurePom(pom, config, config.publishing.pom)
                    }
                }

                if (!config.publishing.publications.contains('main') && !config.publishing.publications) {
                    main(MavenPublication) {
                        Map<String, String> expressions = [:]

                        if (project.pluginManager.hasPlugin('java-platform')) {
                            from project.components.javaPlatform

                            pom.withXml {
                                asNode().dependencyManagement.dependencies.dependency.each { dep ->
                                    String gid = dep.groupId.text()
                                    String aid = dep.artifactId.text()

                                    Dependency dependency = config.dependencyManagement.findDependencyByGA(gid, aid)
                                    if (dependency && dependency.version == dep.version.text()) {
                                        String versionKey = dependency.name + '.version'
                                        String versionExp = '${' + versionKey + '}'
                                        expressions.put(versionKey, dependency.version)
                                        dep.remove(dep.version)
                                        dep.appendNode('version', versionExp)
                                    }
                                }

                                pom.properties.putAll(expressions)

                                Node propertiesNode = asNode().children().find {
                                    it.name().toString().contains('properties')
                                }
                                if (!propertiesNode) {
                                    propertiesNode = new Node(null, 'properties')
                                    List nodes = asNode().children()
                                        .find { it.name().toString().contains('dependencyManagement') }
                                        .parent().children()
                                    nodes.add(nodes.size() - 1, propertiesNode)
                                }
                                expressions.each { versionKey, versionVal ->
                                    if (!(propertiesNode.children().find { it.name() == versionKey })) {
                                        propertiesNode.appendNode(versionKey, versionVal)
                                    }
                                }
                            }
                        } else {
                            Task jar = project.tasks.findByName('jar')
                            Task sourcesJar = project.tasks.findByName('sourcesJar')

                            groupId = project.group
                            artifactId = project.name
                            version = project.version
                            if (jar?.enabled) artifact jar

                            PublishingUtils.configureDependencies(pom, config, project, expressions)

                            if (sourcesJar?.enabled && !artifacts.find { it.classifier == 'sources' }) artifact sourcesJar
                        }

                        config.publishing.pom.properties.putAll(expressions)
                        PublishingUtils.configurePom(pom, config, config.publishing.pom)
                    }
                }
            }

            String repositoryName = config.release ? config.publishing.releasesRepository : config.publishing.snapshotsRepository
            if (isNotBlank(repositoryName)) {
                Repository repo = config.info.repositories.getRepository(repositoryName)
                if (repo == null) {
                    throw new IllegalStateException("Repository '${repositoryName}' was not found.")
                }

                repositories {
                    maven {
                        name = repositoryName
                        url = repo.url
                        Credentials creds = config.info.credentials.getCredentials(repo.name)
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

        List<String> publications = new ArrayList<>(config.publishing.publications)
        if (!publications) publications << 'main'
        PublishingUtils.configureSigning(config, project, *publications)
    }
}
