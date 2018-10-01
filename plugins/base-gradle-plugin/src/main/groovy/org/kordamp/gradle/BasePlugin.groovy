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

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 *
 * @author Andres Almiray
 */
class BasePlugin implements Plugin<Project> {
    Project project

    void apply(Project project) {
        this.project = project

        if (!project.plugins.findPlugin(org.gradle.api.plugins.BasePlugin)) {
            project.plugins.apply(org.gradle.api.plugins.BasePlugin)
        }

        if (!project.extensions.findByType(ProjectConfigurationExtension)) {
            project.extensions.create('projectConfiguration', ProjectConfigurationExtension, project)
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(BasePlugin)) {
            project.plugins.apply(BasePlugin)
        }
    }
}
