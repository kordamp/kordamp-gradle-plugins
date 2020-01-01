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
package org.kordamp.gradle.plugin.project

import com.github.benmanes.gradle.versions.VersionsPlugin
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.bintray.BintrayPlugin
import org.kordamp.gradle.plugin.buildinfo.BuildInfoPlugin
import org.kordamp.gradle.plugin.coveralls.CoverallsPlugin
import org.kordamp.gradle.plugin.jacoco.JacocoPlugin
import org.kordamp.gradle.plugin.jar.JarPlugin
import org.kordamp.gradle.plugin.licensing.LicensingPlugin
import org.kordamp.gradle.plugin.minpom.MinPomPlugin
import org.kordamp.gradle.plugin.publishing.PublishingPlugin
import org.kordamp.gradle.plugin.source.SourceJarPlugin
import org.kordamp.gradle.plugin.sourcehtml.SourceHtmlPlugin
import org.kordamp.gradle.plugin.sourcexref.SourceXrefPlugin
import org.kordamp.gradle.plugin.stats.SourceStatsPlugin
import org.kordamp.gradle.plugin.testing.TestingPlugin

/**
 * Aggregator for all Kordamp plugins.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class ProjectPlugin extends AbstractKordampPlugin {
    Project project

    void apply(Project project) {
        this.project = project

        applyPlugins(project)
        project.childProjects.values().each {
            applyPlugins(it)
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(ProjectPlugin)) {
            project.pluginManager.apply(ProjectPlugin)
        }
    }

    private void applyPlugins(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        BuildInfoPlugin.applyIfMissing(project)
        JacocoPlugin.applyIfMissing(project)
        CoverallsPlugin.applyIfMissing(project)
        LicensingPlugin.applyIfMissing(project)
        SourceJarPlugin.applyIfMissing(project)
        MinPomPlugin.applyIfMissing(project)
        JarPlugin.applyIfMissing(project)
        PublishingPlugin.applyIfMissing(project)
        SourceStatsPlugin.applyIfMissing(project)
        SourceHtmlPlugin.applyIfMissing(project)
        SourceXrefPlugin.applyIfMissing(project)
        BintrayPlugin.applyIfMissing(project)
        TestingPlugin.applyIfMissing(project)

        project.pluginManager.apply(VersionsPlugin)
    }
}
