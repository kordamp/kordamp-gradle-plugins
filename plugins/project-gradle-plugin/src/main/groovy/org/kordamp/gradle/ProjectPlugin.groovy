/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 the original author or authors.
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
package org.kordamp.gradle

import com.github.benmanes.gradle.versions.VersionsPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

import static org.kordamp.gradle.BasePlugin.isRootProject

/**
 * Aggregator for all Kordamp plugins.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
class ProjectPlugin implements Plugin<Project> {
    Project project

    void apply(Project project) {
        this.project = project

        if (isRootProject(project)) {
            applyPlugins(project)
            project.childProjects.values().each { prj ->
                applyPlugins(prj)
            }
        } else {
            applyPlugins(project)
        }
    }

    static void applyPlugins(Project project) {
        BasePlugin.applyIfMissing(project)
        JacocoPlugin.applyIfMissing(project)
        LicensePlugin.applyIfMissing(project)
        BuildInfoPlugin.applyIfMissing(project)
        SourceJarPlugin.applyIfMissing(project)
        ApidocPlugin.applyIfMissing(project)
        MinPomPlugin.applyIfMissing(project)
        JarPlugin.applyIfMissing(project)
        PublishingPlugin.applyIfMissing(project)
        BintrayPlugin.applyIfMissing(project)

        project.plugins.apply(VersionsPlugin)
    }
}
