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
package org.kordamp.gradle.plugin.plugin

import com.gradle.publish.PluginBundleExtension
import com.gradle.publish.PluginConfig
import com.gradle.publish.PublishPlugin
import com.jfrog.bintray.gradle.BintrayExtension
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin
import org.kordamp.gradle.annotations.DependsOn
import org.kordamp.gradle.listener.ProjectEvaluatedListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.util.PublishingUtils

import javax.inject.Named

import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addProjectEvaluatedListener
import static org.kordamp.gradle.util.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.util.StringUtils.isBlank
import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.16.0
 */
@CompileStatic
class PluginPlugin extends AbstractKordampPlugin {
    Project project

    void apply(Project project) {
        this.project = project

        configureProject(project)
        project.childProjects.values().each {
            it.pluginManager.apply(PluginPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        project.pluginManager.apply(JavaGradlePluginPlugin)
        project.pluginManager.apply(PublishPlugin)

        project.tasks.register('listPluginDescriptors', ListPluginDescriptorsTask.class,
            new Action<ListPluginDescriptorsTask>() {
                void execute(ListPluginDescriptorsTask t) {
                    t.group = 'Plugin development'
                    t.description = 'Lists plugin descriptors from plugin declarations.'
                    t.declarations.set(project.extensions.findByType(GradlePluginDevelopmentExtension).plugins)
                }
            })

        project.tasks.register('publishRelease', DefaultTask,
            new Action<DefaultTask>() {
                @Override
                void execute(DefaultTask t) {
                    t.group = 'Publishing'
                    t.description = 'Publishes plugin artifacts to Bintray and the Gradle Plugin Portal'
                    Task bintrayUpload = project.tasks.findByName('bintrayUpload')
                    if (bintrayUpload) {
                        t.dependsOn(bintrayUpload)
                    }
                    t.dependsOn(project.tasks.named('publishPlugins'))
                }
            })

        addProjectEvaluatedListener(project, new PublishingProjectEvaluatedListener())
    }

    @Named('plugin')
    @DependsOn(['publishing'])
    private class PublishingProjectEvaluatedListener implements ProjectEvaluatedListener {
        @Override
        void projectEvaluated(Project project) {
            ProjectConfigurationExtension config = resolveEffectiveConfig(project)
            String pluginName = config.plugin.pluginName

            PluginBundleExtension pbe = project.extensions.findByType(PluginBundleExtension)
            PluginConfig pc = pbe.plugins.maybeCreate(pluginName)

            if (isBlank(pbe.website) && isNotBlank(config.info.url)) {
                pbe.website = config.info.url
            }
            if (isBlank(pbe.vcsUrl)) {
                pbe.vcsUrl = config.info.resolveScmLink()
            }
            if (isBlank(pbe.description)) {
                pbe.description = config.info.description
            }
            if (!pbe.tags) {
                pbe.tags = config.plugin.resolveTags(config)
            }

            if (isBlank(pbe.mavenCoordinates.groupId)) {
                pbe.mavenCoordinates.groupId = project.group
            }
            if (isBlank(pbe.mavenCoordinates.artifactId)) {
                pbe.mavenCoordinates.artifactId = project.name
            }

            if (isBlank(pbe.mavenCoordinates.version)) {
                pbe.mavenCoordinates.version = project.version
            }

            if (isBlank(pc.id)) {
                pc.id = config.plugin.id
            }
            if (isBlank(pc.displayName)) {
                pc.displayName = config.info.name
            }

            BintrayExtension bintray = project.extensions.findByType(BintrayExtension)
            if (bintray) {
                bintray.pkg.version.attributes = ['gradle-plugin': "${config.plugin.id}:${project.group}:${project.name}".toString()]
            }

            updatePublication(project)
        }
    }

    @CompileDynamic
    private void updatePublication(Project project) {
        ProjectConfigurationExtension config = resolveEffectiveConfig(project)

        project.publishing {
            publications {
                pluginMaven(MavenPublication) {
                    PublishingUtils.configurePom(pom, config, config.publishing.pom)
                }
            }
        }

        PublishingUtils.configureSigning(config, project, 'pluginMaven')
    }
}
