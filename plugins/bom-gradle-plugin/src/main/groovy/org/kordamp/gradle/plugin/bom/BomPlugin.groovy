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
package org.kordamp.gradle.plugin.bom

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.plugins.signing.SigningPlugin
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.Credentials
import org.kordamp.gradle.plugin.base.model.Dependency
import org.kordamp.gradle.plugin.base.model.Repository
import org.kordamp.gradle.plugin.base.plugins.util.PublishingUtils

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.StringUtils.isNotBlank

/**
 * Generates a BOM file for the given inputs.
 *
 * @author Andres Almiray
 * @since 0.9.0
 */
@CompileStatic
class BomPlugin extends AbstractKordampPlugin {
    Project project

    void apply(Project project) {
        this.project = project

        configureProject(project)
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(BomPlugin)) {
            project.plugins.apply(BomPlugin)
        }
        if (!project.plugins.findPlugin(SigningPlugin)) {
            project.plugins.apply(SigningPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)

        if (!project.plugins.findPlugin(MavenPublishPlugin)) {
            project.plugins.apply(MavenPublishPlugin)
        }

        project.extensions.findByType(ProjectConfigurationExtension).publishing.enabled = false

        project.afterEvaluate {
            updatePublications(project)
        }
    }

    @CompileDynamic
    private void updatePublications(Project project) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        setEnabled(effectiveConfig.bom.enabled)

        if (!enabled) {
            return
        }

        List<Dependency> compileDeps = effectiveConfig.bom.compile.collect { Dependency.parseDependency(project, it) }
        List<Dependency> runtimeDeps = effectiveConfig.bom.runtime.collect { Dependency.parseDependency(project, it) }
        List<Dependency> testDeps = effectiveConfig.bom.test.collect { Dependency.parseDependency(project, it) }

        if (effectiveConfig.bom.autoIncludes) {
            project.rootProject.subprojects.each { Project prj ->
                if (prj == project) return

                Closure<Boolean> predicate = { Dependency d ->
                    d.artifactId == prj.name && (d.groupId == project.group || d.groupId == '${project.groupId}')
                }
                if ((!effectiveConfig.bom.excludes.contains(prj.name) && !effectiveConfig.bom.excludes.contains(':' + prj.name)) &&
                    !compileDeps.find(predicate) && !runtimeDeps.find(predicate) && !testDeps.find(predicate)) {
                    compileDeps << new Dependency('${project.groupId}', prj.name, '${project.version}')
                }
            }
        }

        project.publishing {
            publications {
                main(MavenPublication) {
                    artifacts = []

                    PublishingUtils.configurePom(pom, effectiveConfig, effectiveConfig.bom)

                    pom {
                        packaging = 'pom'
                    }

                    pom.withXml {
                        def dependencyManagementNode = asNode().appendNode('dependencyManagement').appendNode('dependencies')
                        compileDeps.each { Dependency dep ->
                            dependencyManagementNode.appendNode('dependency').with {
                                appendNode('groupId', dep.groupId)
                                appendNode('artifactId', dep.artifactId)
                                appendNode('version', dep.version)
                            }
                        }
                        runtimeDeps.each { Dependency dep ->
                            dependencyManagementNode.appendNode('dependency').with {
                                appendNode('groupId', dep.groupId)
                                appendNode('artifactId', dep.artifactId)
                                appendNode('version', dep.version)
                                appendNode('scope', 'runtime')
                            }
                        }
                        testDeps.each { Dependency dep ->
                            dependencyManagementNode.appendNode('dependency').with {
                                appendNode('groupId', dep.groupId)
                                appendNode('artifactId', dep.artifactId)
                                appendNode('version', dep.version)
                                appendNode('scope', 'test')
                            }
                        }
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

        PublishingUtils.configureSigning(effectiveConfig, project)
    }
}
