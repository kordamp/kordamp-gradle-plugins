/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 Andres Almiray.
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
package org.kordamp.gradle.plugin.buildinfo

import net.nemerosa.versioning.VersioningExtension
import net.nemerosa.versioning.VersioningPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.text.SimpleDateFormat

/**
 * Calculates build properties and attaches them to the root {@code Project}.
 * The following properties are exposed when this plugin is applied:
 * <ul>
 *     <li>buildTimeAndDate: a {@code java.util.Date} instance.</li>
 *     <li>buildDate: the value of {@code buildTimeAndDate} formatted with "yyyy-MM-dd".</li>
 *     <li>buildtime: the value of {@code buildTimeAndDate} formatted with "HH:mm:ss.SSSZ".</li>
 *     <li>buildBy: the value of the "user.name" System property.</li>
 *     <li>buildRevision: the value of the latest commit hash.</li>
 *     <li>buildJdk: concatenation of the following System properties [java.version, java.vendor, java.vm.version].</li>
 *     <li>buildCreatedBy: the Gradle version used in the build.</li>
 * </ul>
 * @author Andres Almiray
 * @since 0.1.0
 */
class BuildInfoPlugin implements Plugin<Project> {
    Project project

    void apply(Project project) {
        this.project = project

        Project root = project.rootProject

        if (!root.findProperty('buildTimeAndDate')) {
            root.plugins.apply(VersioningPlugin)

            VersioningExtension versioning = root.extensions.findByName('versioning')

            Date date = new Date()
            root.ext.buildinfo = [
                buildTimeAndDate: date,
                buildBy         : System.properties['user.name'],
                buildDate       : new SimpleDateFormat('yyyy-MM-dd').format(date),
                buildTime       : new SimpleDateFormat('HH:mm:ss.SSSZ').format(date),
                buildRevision   : versioning.info.commit,
                buildJdk        : "${System.properties['java.version']} (${System.properties['java.vendor']} ${System.properties['java.vm.version']})".toString(),
                buildCreatedBy  : "Gradle ${root.gradle.gradleVersion}"
            ]
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(BuildInfoPlugin)) {
            project.plugins.apply(BuildInfoPlugin)
        }
    }
}
