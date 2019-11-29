/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
import org.kordamp.gradle.plugin.apidoc.ApidocPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.bintray.BintrayPlugin
import org.kordamp.gradle.plugin.buildinfo.BuildInfoPlugin
import org.kordamp.gradle.plugin.groovydoc.GroovydocPlugin
import org.kordamp.gradle.plugin.jacoco.JacocoPlugin
import org.kordamp.gradle.plugin.jar.JarPlugin
import org.kordamp.gradle.plugin.javadoc.JavadocPlugin
import org.kordamp.gradle.plugin.licensing.LicensingPlugin
import org.kordamp.gradle.plugin.minpom.MinPomPlugin
import org.kordamp.gradle.plugin.publishing.PublishingPlugin
import org.kordamp.gradle.plugin.source.SourceJarPlugin
import org.kordamp.gradle.plugin.sourcehtml.SourceHtmlPlugin
import org.kordamp.gradle.plugin.stats.SourceStatsPlugin
import org.kordamp.gradle.plugin.testing.TestingPlugin

import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

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

        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        if (isRootProject(project)) {
            applyPlugins(project)
            project.childProjects.values().each {
                applyPlugins(it)
            }
        } else {
            applyPlugins(project)
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(ProjectPlugin)) {
            project.pluginManager.apply(ProjectPlugin)
        }
    }

    private void applyPlugins(Project project) {
        BasePlugin.applyIfMissing(project)
        JacocoPlugin.applyIfMissing(project)
        LicensingPlugin.applyIfMissing(project)
        BuildInfoPlugin.applyIfMissing(project)
        SourceJarPlugin.applyIfMissing(project)
        JavadocPlugin.applyIfMissing(project)
        GroovydocPlugin.applyIfMissing(project)
        ApidocPlugin.applyIfMissing(project)
        MinPomPlugin.applyIfMissing(project)
        JarPlugin.applyIfMissing(project)
        PublishingPlugin.applyIfMissing(project)
        SourceStatsPlugin.applyIfMissing(project)
        SourceHtmlPlugin.applyIfMissing(project)
        BintrayPlugin.applyIfMissing(project)
        TestingPlugin.applyIfMissing(project)

        project.pluginManager.apply(VersionsPlugin)
    }
}
