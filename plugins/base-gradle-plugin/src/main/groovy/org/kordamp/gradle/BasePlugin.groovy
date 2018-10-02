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

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.kordamp.gradle.model.Information

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
class BasePlugin implements Plugin<Project> {
    static final String VISITED = BasePlugin.class.name.replace('.', '_') + '_VISITED'

    Project project

    void apply(Project project) {
        this.project = project

        if (!project.plugins.findPlugin(org.gradle.api.plugins.BasePlugin)) {
            project.plugins.apply(org.gradle.api.plugins.BasePlugin)
        }

        if (!project.extensions.findByType(ProjectConfigurationExtension)) {
            project.extensions.create('projectConfiguration', ProjectConfigurationExtension, project)
        }

        project.afterEvaluate {
            String visitedPropertyName = VISITED + '_' + project.name
            if (project.findProperty(visitedPropertyName)) {
                return
            }
            project.ext[visitedPropertyName] = true

            ProjectConfigurationExtension extension = project.extensions.findByType(ProjectConfigurationExtension)
            extension.information.normalize()

            List<String> errors = []
            if (isRootProject(project)) {
                errors = extension.information.validate()
                project.ext.mergedInformation = extension.information
            } else {
                ProjectConfigurationExtension rootExtension = project.rootProject.extensions.findByType(ProjectConfigurationExtension)
                if (rootExtension) {
                    Information merged = extension.information.merge(rootExtension.information)
                    errors = merged.validate()
                    project.ext.mergedInformation = merged
                }
            }

            if (errors) {
                errors.each { project.logger.error(it) }
                throw new GradleException("Project ${project.name} has not been properly configured")
            }
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(BasePlugin)) {
            project.plugins.apply(BasePlugin)
        }
    }

    static boolean isRootProject(Project project) {
        project == project.rootProject
    }
}
