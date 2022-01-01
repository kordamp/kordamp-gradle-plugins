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

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.ResolvedProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedFeature

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@PackageScope
@CompileStatic
abstract class AbstractResolvedFeature implements ResolvedFeature {
    protected final Project project

    AbstractResolvedFeature(Project project) {
        this.project = project
    }

    protected boolean isRoot() {
        project == project.rootProject
    }

    ResolvedProjectConfigurationExtension resolvedConfig() {
        project.extensions.findByType(ResolvedProjectConfigurationExtension)
    }
}
