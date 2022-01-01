/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2022 Andres Almiray.
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
package org.kordamp.gradle.plugin.base.internal

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.provider.Property
import org.kordamp.gradle.plugin.base.plugins.Feature

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@PackageScope
@CompileStatic
abstract class AbstractFeature implements Feature {
    final Project project
    final ProjectConfigurationExtensionImpl ownerConfig
    final ProjectConfigurationExtensionImpl parentConfig

    final Property<Boolean> enabled

    AbstractFeature(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
        this.project = project
        this.ownerConfig = ownerConfig
        this.parentConfig = parentConfig

        enabled = project.objects.property(Boolean)
    }

    void normalize() {
        // if (!enabled.present) {
        //     if (isRoot()) {
        //         if (project.childProjects.isEmpty()) {
        //             enabled.set(isApplied())
        //         } else {
        //             enabled.set(project.childProjects.values().any { p -> isApplied(p) })
        //         }
        //     } else {
        //         enabled.set(isApplied())
        //     }
        // }
    }

    void validate(List<String> errors) {

    }

    final boolean isRoot() {
        project == project.rootProject
    }

    @CompileDynamic
    protected boolean isApplied() {
        isApplied(project)
    }

    @CompileDynamic
    protected boolean isApplied(Project project) {
        ExtraPropertiesExtension ext = project.extensions.findByType(ExtraPropertiesExtension)
        ext.has('VISITED_' + getClass().PLUGIN_ID.replace('.', '_') + '_' + project.path.replace(':', '#'))
    }
}
