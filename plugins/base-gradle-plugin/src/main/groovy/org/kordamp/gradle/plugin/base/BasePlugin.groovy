/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
package org.kordamp.gradle.plugin.base

import org.gradle.BuildAdapter
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.tasks.EffectiveSettingsTask
import org.kordamp.gradle.plugin.base.tasks.ExtensionsTask
import org.kordamp.gradle.plugin.base.tasks.PluginsTask
import org.kordamp.gradle.plugin.base.tasks.ProjectPropertiesTask
import org.kordamp.gradle.plugin.base.tasks.RepositoriesTask

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
class BasePlugin extends AbstractKordampPlugin {
    Project project

    void apply(Project project) {
        this.project = project

        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        if (!project.plugins.findPlugin(org.gradle.api.plugins.BasePlugin)) {
            project.plugins.apply(org.gradle.api.plugins.BasePlugin)
        }

        if (!project.extensions.findByType(ProjectConfigurationExtension)) {
            project.extensions.create(ProjectConfigurationExtension.CONFIG_NAME, ProjectConfigurationExtension, project)
        }

        project.tasks.register('effectiveSettings', EffectiveSettingsTask) {
            group 'Insight'
            description "Displays resolved settings for project '$project.name'"
        }

        project.tasks.register('repositories', RepositoriesTask) {
            group 'Insight'
            description "Displays all repositories for project '$project.name'"
        }

        project.tasks.register('plugins', PluginsTask) {
            group 'Insight'
            description "Displays all plugins applied to project '$project.name'"
        }

        project.tasks.register('extensions', ExtensionsTask) {
            group 'Insight'
            description "Displays all extensions applied to project '$project.name'"
        }

        project.tasks.register('projectProperties', ProjectPropertiesTask) {
            group 'Insight'
            description "Displays all properties found in project '$project.name'"
        }

        if (isRootProject(project)) {
            project.gradle.addBuildListener(new BuildAdapter() {
                @Override
                void projectsEvaluated(Gradle gradle) {
                    project.subprojects.each { Project subproject ->
                        subproject.extensions.findByName(ProjectConfigurationExtension.EFFECTIVE_CONFIG_NAME).rootReady()
                    }
                    project.extensions.findByName(ProjectConfigurationExtension.EFFECTIVE_CONFIG_NAME).rootReady()
                }
            })
        }

        project.afterEvaluate {
            ProjectConfigurationExtension rootExtension = project.rootProject.extensions.findByType(ProjectConfigurationExtension)
            ProjectConfigurationExtension extension = project.extensions.findByType(ProjectConfigurationExtension)
            extension.normalize()

            List<String> errors = []
            if (isRootProject(project)) {
                ProjectConfigurationExtension merged = extension.merge(rootExtension)
                errors = merged.validate()
                project.extensions.create(ProjectConfigurationExtension.EFFECTIVE_CONFIG_NAME, ProjectConfigurationExtension, merged).ready()
            } else {
                if (rootExtension) {
                    ProjectConfigurationExtension merged = extension.merge(rootExtension)
                    errors = merged.validate()
                    project.extensions.create(ProjectConfigurationExtension.EFFECTIVE_CONFIG_NAME, ProjectConfigurationExtension, merged).ready()
                } else {
                    errors = extension.validate()
                    project.extensions.create(ProjectConfigurationExtension.EFFECTIVE_CONFIG_NAME, ProjectConfigurationExtension, extension).ready()
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
