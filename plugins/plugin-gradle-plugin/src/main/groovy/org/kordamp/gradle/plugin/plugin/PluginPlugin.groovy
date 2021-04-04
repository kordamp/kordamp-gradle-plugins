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
package org.kordamp.gradle.plugin.plugin

import com.gradle.publish.PluginBundleExtension
import com.gradle.publish.PluginConfig
import com.gradle.publish.PublishPlugin
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.plugin.devel.PluginDeclaration
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin
import org.kordamp.gradle.listener.ProjectEvaluatedListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.Plugins
import org.kordamp.gradle.plugin.base.plugins.util.PublishingUtils

import javax.inject.Named

import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addProjectEvaluatedListener
import static org.kordamp.gradle.util.PluginUtils.resolveConfig
import static org.kordamp.gradle.util.StringUtils.isBlank
import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.16.0
 */
@CompileStatic
class PluginPlugin extends AbstractKordampPlugin {
    Project project

    PluginPlugin() {
        super(Plugins.PLUGIN_ID)
    }

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
                    t.description = 'Publishes plugin artifacts to the Gradle Plugin Portal'
                    t.dependsOn(project.tasks.named('publishPlugins'))
                }
            })

        addProjectEvaluatedListener(project, new PublishingProjectEvaluatedListener())

        project.pluginManager.apply(JavaGradlePluginPlugin)
        project.pluginManager.apply(PublishPlugin)

        // Must execute after JavaGradlePluginPlugin|PublishPlugin
        // because ${plugin.name}PluginMarkerMaven is created explicitly and not with `maybeCreate`.
        project.afterEvaluate {
            ProjectConfigurationExtension config = resolveConfig(project)
            config.plugins.plugins.values().each { plugin ->
                String pluginName = plugin.name
                updatePluginPublication(project, config, pluginName)
            }
            updatePublications(project, config)
        }
    }

    @Named('plugin')
    //@DependsOn(['publishing'])
    private class PublishingProjectEvaluatedListener implements ProjectEvaluatedListener {
        @Override
        void projectEvaluated(Project project) {
            ProjectConfigurationExtension config = resolveConfig(project)

            GradlePluginDevelopmentExtension gpdExt = project.extensions.findByType(GradlePluginDevelopmentExtension)
            PluginBundleExtension pbExt = project.extensions.findByType(PluginBundleExtension)

            if (isBlank(pbExt.website) && isNotBlank(config.info.url)) {
                pbExt.website = config.info.url
            }
            if (isBlank(pbExt.vcsUrl)) {
                pbExt.vcsUrl = config.info.resolveScmLink()
            }
            if (isBlank(pbExt.description)) {
                pbExt.description = config.info.description
            }
            if (!pbExt.tags) {
                pbExt.tags = config.info.tags
            }

            if (isBlank(pbExt.mavenCoordinates.groupId)) {
                pbExt.mavenCoordinates.groupId = project.group
            }
            if (isBlank(pbExt.mavenCoordinates.artifactId)) {
                pbExt.mavenCoordinates.artifactId = project.name
            }

            if (isBlank(pbExt.mavenCoordinates.version)) {
                pbExt.mavenCoordinates.version = project.version
            }

            config.plugins.plugins.values().each { plugin ->
                String pluginName = plugin.name

                PluginDeclaration pd = gpdExt.plugins.maybeCreate(pluginName)
                pd.id = plugin.id
                pd.displayName = plugin.displayName ?: config.info.description
                pd.description = plugin.description ?: config.info.description
                pd.implementationClass = plugin.implementationClass

                PluginConfig pc = pbExt.plugins.maybeCreate(pluginName)
                pc.id = plugin.id
                pc.displayName = plugin.displayName ?: config.info.description
                pc.description = plugin.description ?: config.info.description
                pc.tags = plugin.resolveTags(config)
                pc.version = project.version
            }
        }
    }

    @CompileDynamic
    private void updatePluginPublication(Project project, ProjectConfigurationExtension config, String pluginName) {
        PublishingUtils.configureSigning(config, project, pluginName + 'PluginMarkerMaven')
    }

    @CompileDynamic
    private void updatePublications(Project project, ProjectConfigurationExtension config) {
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
