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
package org.kordamp.gradle.plugin.buildinfo

import groovy.transform.CompileStatic
import net.nemerosa.versioning.VersioningExtension
import net.nemerosa.versioning.VersioningPlugin
import org.gradle.api.Project
import org.kordamp.gradle.annotations.DependsOn
import org.kordamp.gradle.listener.AllProjectsEvaluatedListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.BuildInfo

import javax.inject.Named
import java.text.SimpleDateFormat

import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addAllProjectsEvaluatedListener
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject
import static org.kordamp.gradle.util.PluginUtils.resolveConfig

/**
 * Calculates build properties and attaches them to the root {@code Project}.
 * The following properties are exposed when this plugin is applied:
 * <ul>
 *     <li>buildDate: the value of {@code new Date()} formatted with "yyyy-MM-dd".</li>
 *     <li>buildTime: the value of {@code new Date()} formatted with "HH:mm:ss.ssXXX".</li>
 *     <li>buildBy: the value of the "user.name" System property.</li>
 *     <li>buildRevision: the value of the latest commit hash.</li>
 *     <li>buildJdk: concatenation of the following System properties [java.version, java.vendor, java.vm.version].</li>
 *     <li>buildCreatedBy: the Gradle version used in the build.</li>
 * </ul>
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class BuildInfoPlugin extends AbstractKordampPlugin {
    Project project

    BuildInfoPlugin() {
        super(BuildInfo.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        if (isRootProject(project)) {
            configureRootProject(project)
        } else {
            configureRootProject(project.rootProject)
        }
    }

    private void configureRootProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        addAllProjectsEvaluatedListener(project, new BuildInfoAllProjectsEvaluatedListener())
    }

    @Named('buildInfo')
    @DependsOn(['base'])
    private class BuildInfoAllProjectsEvaluatedListener implements AllProjectsEvaluatedListener {
        @Override
        void allProjectsEvaluated(Project rootProject) {
            ProjectConfigurationExtension config = resolveConfig(project)
            setEnabled(config.buildInfo.enabled)

            if (!enabled) {
                return
            }

            Date date = new Date()
            if (config.buildInfo.clearTime) {
                Calendar calendar = Calendar.instance
                calendar.time = date
                calendar.clear(Calendar.HOUR)
                calendar.clear(Calendar.MINUTE)
                calendar.clear(Calendar.SECOND)
                calendar.clear(Calendar.MILLISECOND)
                calendar.clear(Calendar.ZONE_OFFSET)
                date = calendar.time
            }

            if (!config.buildInfo.skipBuildBy) {
                config.buildInfo.buildBy = System.properties['user.name']
            }

            if (!config.buildInfo.skipBuildDate) {
                config.buildInfo.buildDate = new SimpleDateFormat('yyyy-MM-dd').format(date)
            }

            if (!config.buildInfo.skipBuildTime) {
                config.buildInfo.buildTime = new SimpleDateFormat('HH:mm:ssXXX').format(date)
            }

            if (!config.buildInfo.skipBuildRevision) {
                project.pluginManager.apply(VersioningPlugin)
                VersioningExtension versioning = project.extensions.findByType(VersioningExtension)
                config.buildInfo.buildRevision = versioning.info.commit
            }

            if (!config.buildInfo.skipBuildJdk) {
                config.buildInfo.buildJdk = "${System.properties['java.version']} (${System.properties['java.vendor']} ${System.properties['java.vm.version']})".toString()
            }

            if (!config.buildInfo.skipBuildOs) {
                config.buildInfo.buildOs = "${System.properties['os.name']} (${System.properties['os.version']}; ${System.properties['os.arch']})".toString()
            }

            if (!config.buildInfo.skipBuildCreatedBy) {
                config.buildInfo.buildCreatedBy = "Gradle ${project.gradle.gradleVersion}"
            }
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(BuildInfoPlugin)) {
            project.pluginManager.apply(BuildInfoPlugin)
        }
    }
}
