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
package org.kordamp.gradle.plugin.buildinfo

import groovy.transform.CompileStatic
import net.nemerosa.versioning.VersioningExtension
import net.nemerosa.versioning.VersioningPlugin
import org.gradle.api.Project
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

import java.text.SimpleDateFormat

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * Calculates build properties and attaches them to the root {@code Project}.
 * The following properties are exposed when this plugin is applied:
 * <ul>
 *     <li>buildTimeAndDate: a {@code java.util.Date} instance.</li>
 *     <li>buildDate: the value of {@code buildTimeAndDate} formatted with "yyyy-MM-dd".</li>
 *     <li>buildTime: the value of {@code buildTimeAndDate} formatted with "HH:mm:ss.SSSZ".</li>
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

        project.afterEvaluate {
            configureBuildProperties(project)
        }
    }

    private void configureBuildProperties(Project project) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        setEnabled(effectiveConfig.buildInfo.enabled)

        if (!enabled) {
            return
        }

        Date date = new Date()
        if (effectiveConfig.buildInfo.clearTime) {
            Calendar calendar = Calendar.instance
            calendar.time = date
            calendar.clear(Calendar.HOUR)
            calendar.clear(Calendar.MINUTE)
            calendar.clear(Calendar.SECOND)
            calendar.clear(Calendar.MILLISECOND)
            calendar.clear(Calendar.ZONE_OFFSET)
            date = calendar.time
        }

        if (!effectiveConfig.buildInfo.skipBuildBy) {
            effectiveConfig.buildInfo.buildBy = System.properties['user.name']
        }

        if (!effectiveConfig.buildInfo.skipBuildDate) {
            effectiveConfig.buildInfo.buildDate = new SimpleDateFormat('yyyy-MM-dd').format(date)
        }

        if (!effectiveConfig.buildInfo.skipBuildTime) {
            effectiveConfig.buildInfo.buildTime = new SimpleDateFormat('HH:mm:ss.SSSZ').format(date)
        }

        if (!effectiveConfig.buildInfo.skipBuildRevision) {
            project.pluginManager.apply(VersioningPlugin)
            VersioningExtension versioning = project.extensions.findByType(VersioningExtension)
            effectiveConfig.buildInfo.buildRevision = versioning.info.commit
        }

        if (!effectiveConfig.buildInfo.skipBuildJdk) {
            effectiveConfig.buildInfo.buildJdk = "${System.properties['java.version']} (${System.properties['java.vendor']} ${System.properties['java.vm.version']})".toString()
        }

        if (!effectiveConfig.buildInfo.skipBuildOs) {
            effectiveConfig.buildInfo.buildOs = "${System.properties['os.name']} (${System.properties['os.version']}; ${System.properties['os.arch']})".toString()
        }

        if (!effectiveConfig.buildInfo.skipBuildCreatedBy) {
            effectiveConfig.buildInfo.buildCreatedBy = "Gradle ${project.gradle.gradleVersion}"
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(BuildInfoPlugin)) {
            project.pluginManager.apply(BuildInfoPlugin)
        }
    }
}
