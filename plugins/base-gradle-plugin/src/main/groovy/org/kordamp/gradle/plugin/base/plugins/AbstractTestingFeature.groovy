/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2024 Andres Almiray.
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
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

import static org.kordamp.gradle.util.PluginUtils.isAndroidProject
import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.38.0
 */
@CompileStatic
abstract class AbstractTestingFeature extends AbstractFeature {
    AbstractTestingFeature(ProjectConfigurationExtension config, Project project, String pluginId) {
        super(config, project, pluginId)
    }

    @Override
    protected void normalizeEnabled() {
        if (!enabledSet) {
            enabled = hasTestSourceSets()
            if (enabled && !isRoot()) {
                getParentFeature().enabled = enabled
            }
        }
    }

    @Override
    protected boolean hasBasePlugin(Project project) {
        project.pluginManager.hasPlugin('java-base')
    }

    boolean hasTestSourceSets() {
        hasTestSourceSets(project)
    }

    boolean hasTestSourceSets(Project project) {
        ProjectConfigurationExtension config = project.extensions.getByType(ProjectConfigurationExtension)

        hasTestsAt(project.file('src/test')) ||
            (isNotBlank(config.testing.integration.baseDir) && hasTestsAt(project.file(config.testing.integration.baseDir))) ||
            (isNotBlank(config.testing.functional.baseDir) && hasTestsAt(project.file(config.testing.functional.baseDir))) ||
            (isAndroidProject(project) && hasTestsAt(project.file('src/androidTest')))
    }

    private static boolean hasTestsAt(File testDir) {
        testDir.exists() && testDir.listFiles()?.size()
    }
}