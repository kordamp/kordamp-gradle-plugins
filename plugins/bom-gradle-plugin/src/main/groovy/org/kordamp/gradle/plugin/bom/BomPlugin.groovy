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
package org.kordamp.gradle.plugin.bom

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.plugins.signing.SigningPlugin
import org.kordamp.gradle.annotations.DependsOn
import org.kordamp.gradle.annotations.Evicts
import org.kordamp.gradle.listener.ProjectEvaluatedListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.Credentials
import org.kordamp.gradle.plugin.base.model.Repository
import org.kordamp.gradle.plugin.base.model.artifact.Dependency
import org.kordamp.gradle.plugin.base.model.artifact.internal.DependencyImpl
import org.kordamp.gradle.plugin.base.plugins.Bom
import org.kordamp.gradle.plugin.base.plugins.util.PublishingUtils

import javax.inject.Named

import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addProjectEvaluatedListener
import static org.kordamp.gradle.util.PluginUtils.resolveConfig
import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * Generates a BOM file for the given inputs.
 *
 * @author Andres Almiray
 * @since 0.9.0
 */
@CompileStatic
class BomPlugin extends AbstractKordampPlugin {
    Project project

    BomPlugin() {
        super(Bom.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        configureProject(project)
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(BomPlugin)) {
            project.pluginManager.apply(BomPlugin)
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

        if (!project.plugins.findPlugin(MavenPublishPlugin)) {
            project.pluginManager.apply(MavenPublishPlugin)
        }

        project.extensions.findByType(ProjectConfigurationExtension).publishing.enabled = false

        addProjectEvaluatedListener(project, new BomProjectEvaluatedListener())
    }

    @Named('bom')
    @DependsOn(['base'])
    @Evicts('publishing')
    private class BomProjectEvaluatedListener implements ProjectEvaluatedListener {
        @Override
        void projectEvaluated(Project project) {
            updatePublications(project)
        }
    }

    private Dependency findOwnDependency(ProjectConfigurationExtension config) {
        Dependency artifact = findOwnDependency(config.bom.dependencies.values())
        if (!artifact) artifact = findOwnDependency(config.dependencies.dependencies.values())
        if (artifact) {
            artifact
        } else {
            String name = config.project.rootProject.name.toLowerCase().replace('-', '.').replace('_', '.')
            DependencyImpl.of(name, config.project.group.toString(), name, config.project.version.toString())
        }
    }

    private Dependency findOwnDependency(Collection<? extends Dependency> dependencies) {
        for (Dependency dependency : dependencies) {
            if (dependency.groupId == project.rootProject.group) {
                return dependency
            }
        }
        null
    }

    @CompileDynamic
    private void updatePublications(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)
        setEnabled(config.bom.enabled)

        if (!enabled) {
            return
        }

        Dependency ownDependency = findOwnDependency(config)
        Set<Dependency> projectDependencies = [] as Set

        Map<String, String> versions = [:]
        if (ownDependency) {
            versions.put(ownDependency.name + '.version', ownDependency.version)
        }

        Set<String> excludedProjects = config.bom.excludes.collect { it.startsWith(':') ? it[1..-1] : it }
        Set<String> includedProjects = config.bom.includes.collect { it.startsWith(':') ? it[1..-1] : it }
        excludedProjects.removeAll(includedProjects)

        if (config.bom.autoIncludes) {
            for (Project p : project.rootProject.subprojects) {
                if (p == project || excludedProjects.contains(p.name)) continue
                if (includedProjects) {
                    if (includedProjects.contains(p.name)) {
                        projectDependencies << DependencyImpl.of(ownDependency.name, String.valueOf(p.group), p.name, '${' + ownDependency.name + '.version}')
                    }
                } else {
                    projectDependencies << DependencyImpl.of(ownDependency.name, String.valueOf(p.group), p.name, '${' + ownDependency.name + '.version}')
                }
            }
        }

        project.publishing {
            publications {
                main(MavenPublication) {
                    artifacts = []

                    pom.withXml {
                        def dependencyManagementNode = asNode().appendNode('dependencyManagement').appendNode('dependencies')
                        for (Dependency d : projectDependencies) {
                            dependencyManagementNode.appendNode('dependency').with {
                                appendNode('groupId', d.groupId)
                                appendNode('artifactId', d.artifactId)
                                appendNode('version', d.version)
                            }
                        }
                        for (Dependency d : config.bom.dependencies.values()) {
                            String versionKey = d.name + '.version'
                            String versionExp = '${' + versionKey + '}'
                            versions.put(versionKey, d.version)
                            dependencyManagementNode.appendNode('dependency').with {
                                appendNode('groupId', d.groupId)
                                appendNode('artifactId', d.artifactId)
                                appendNode('version', versionExp)
                                if (d.platform) {
                                    appendNode('scope', 'import')
                                    appendNode('type', 'pom')
                                }
                            }
                        }

                        pom.properties.putAll(versions)
                    }

                    config.bom.properties.putAll(versions)
                    PublishingUtils.configurePom(pom, config, config.bom)
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

        PublishingUtils.configureSigning(config, project)
    }
}
