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
package org.kordamp.gradle.plugin.base

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.internal.ProjectConfigurationExtensionImpl
import org.kordamp.gradle.plugin.base.tasks.ConfigurationSettingsTask
import org.kordamp.gradle.plugin.base.tasks.ConfigurationsTask
import org.kordamp.gradle.plugin.base.tasks.EffectiveSettingsTask
import org.kordamp.gradle.plugin.base.tasks.ExtensionSettingsTask
import org.kordamp.gradle.plugin.base.tasks.ExtensionsTask
import org.kordamp.gradle.plugin.base.tasks.ListIncludedBuildsTask
import org.kordamp.gradle.plugin.base.tasks.ListProjectsTask
import org.kordamp.gradle.plugin.base.tasks.PluginsTask
import org.kordamp.gradle.plugin.base.tasks.ProjectPropertiesTask
import org.kordamp.gradle.plugin.base.tasks.RepositoriesTask
import org.kordamp.gradle.plugin.base.tasks.TarSettingsTask
import org.kordamp.gradle.plugin.base.tasks.TaskSettingsTask
import org.kordamp.gradle.plugin.base.tasks.ZipSettingsTask

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class BasePlugin extends AbstractKordampPlugin {
    Project project

    void apply(Project project) {
        Banner.display(project)
        this.project = project

        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        if (!project.plugins.findPlugin(org.gradle.api.plugins.BasePlugin)) {
            project.pluginManager.apply(org.gradle.api.plugins.BasePlugin)
        }

        ProjectConfigurationExtensionImpl parentConfig = null
        if (!isRootProject(project)) {
            parentConfig = (ProjectConfigurationExtensionImpl) project.rootProject.extensions.findByType(ProjectConfigurationExtension)
        }
        ProjectConfigurationExtensionImpl config = (ProjectConfigurationExtensionImpl) project.extensions
            .create(ProjectConfigurationExtension, 'config',
                ProjectConfigurationExtensionImpl, project, parentConfig)

        project.extensions.add(ResolvedProjectConfigurationExtension,
            'resolvedConfig',
            config.asResolved())

        project.tasks.register('effectiveSettings', EffectiveSettingsTask,
            new Action<EffectiveSettingsTask>() {
                @Override
                void execute(EffectiveSettingsTask t) {
                    t.group = 'Insight'
                    t.description = "Displays resolved settings for project '$project.name'."
                }
            })

        project.tasks.register('repositories', RepositoriesTask,
            new Action<RepositoriesTask>() {
                @Override
                void execute(RepositoriesTask t) {
                    t.group = 'Insight'
                    t.description = "Displays all repositories for project '$project.name'."
                }
            })

        project.tasks.register('plugins', PluginsTask,
            new Action<PluginsTask>() {
                @Override
                void execute(PluginsTask t) {
                    t.group = 'Insight'
                    t.description = "Displays all plugins applied to project '$project.name'."
                }
            })

        project.tasks.register('extensions', ExtensionsTask,
            new Action<ExtensionsTask>() {
                @Override
                void execute(ExtensionsTask t) {
                    t.group = 'Insight'
                    t.description = "Displays all extensions applied to project '$project.name'."
                }
            })

        project.tasks.register('configurations', ConfigurationsTask,
            new Action<ConfigurationsTask>() {
                @Override
                void execute(ConfigurationsTask t) {
                    t.group = 'Insight'
                    t.description = "Displays all configurations available in project '$project.name'."
                }
            })

        project.tasks.register('projectProperties', ProjectPropertiesTask,
            new Action<ProjectPropertiesTask>() {
                @Override
                void execute(ProjectPropertiesTask t) {
                    t.group = 'Insight'
                    t.description = "Displays all properties found in project '$project.name'."
                }
            })

        project.tasks.register('extensionSettings', ExtensionSettingsTask,
            new Action<ExtensionSettingsTask>() {
                @Override
                void execute(ExtensionSettingsTask t) {
                    t.group = 'Insight'
                    t.description = 'Display the settings of an Extension.'
                }
            })

        project.tasks.addRule('Pattern: <ExtensionName>ExtensionSettings: Displays the settings of an Extension.', new Action<String>() {
            @Override
            void execute(String extensionName) {
                if (extensionName.endsWith('ExtensionSettings')) {
                    String resolvedExtensionName = extensionName - 'ExtensionSettings'
                    project.tasks.register(extensionName, ExtensionSettingsTask,
                        new Action<ExtensionSettingsTask>() {
                            @Override
                            void execute(ExtensionSettingsTask t) {
                                t.group = 'Insight'
                                t.extension = resolvedExtensionName
                                t.description = "Display the settings of the '${resolvedExtensionName}' Extension."
                            }
                        })
                }
            }
        })

        project.tasks.register('configurationSettings', ConfigurationSettingsTask,
            new Action<ConfigurationSettingsTask>() {
                @Override
                void execute(ConfigurationSettingsTask t) {
                    t.group = 'Insight'
                    t.description = 'Display the settings of a Configuration.'
                }
            })

        project.tasks.addRule('Pattern: <ConfigurationName>ConfigurationSettings: Displays the settings of a Configuration.', new Action<String>() {
            @Override
            void execute(String configurationName) {
                if (configurationName.endsWith('ConfigurationSettings')) {
                    String resolvedConfigurationName = configurationName - 'ConfigurationSettings'
                    project.tasks.register(configurationName, ConfigurationSettingsTask,
                        new Action<ConfigurationSettingsTask>() {
                            @Override
                            void execute(ConfigurationSettingsTask t) {
                                t.group = 'Insight'
                                t.configuration = resolvedConfigurationName
                                t.description = "Display the settings of the '${resolvedConfigurationName}' Configuration."
                            }
                        })
                }
            }
        })

        project.tasks.register('zipSettings', ZipSettingsTask,
            new Action<ZipSettingsTask>() {
                @Override
                void execute(ZipSettingsTask t) {
                    t.group = 'Insight'
                    t.description = 'Display ZIP settings.'
                }
            })

        project.tasks.addRule('Pattern: <ZipName>ZipSettings: Displays settings of a ZIP task.', new Action<String>() {
            @Override
            void execute(String taskName) {
                if (taskName.endsWith('ZipSettings')) {
                    String resolvedTaskName = taskName - 'ZipSettings'
                    resolvedTaskName = resolvedTaskName ?: 'zip'
                    project.tasks.register(taskName, ZipSettingsTask,
                        new Action<ZipSettingsTask>() {
                            @Override
                            void execute(ZipSettingsTask t) {
                                t.group = 'Insight'
                                t.task = resolvedTaskName
                                t.description = "Display settings of the '${resolvedTaskName}' ZIP task."
                            }
                        })
                }
            }
        })

        project.tasks.register('tarSettings', TarSettingsTask,
            new Action<TarSettingsTask>() {
                @Override
                void execute(TarSettingsTask t) {
                    t.group = 'Insight'
                    t.description = 'Display TAR settings.'
                }
            })

        project.tasks.addRule('Pattern: <TarName>TarSettings: Displays settings of a TAR task.', new Action<String>() {
            @Override
            void execute(String taskName) {
                if (taskName.endsWith('TarSettings')) {
                    String resolvedTaskName = taskName - 'TarSettings'
                    resolvedTaskName = resolvedTaskName ?: 'tar'
                    project.tasks.register(taskName, TarSettingsTask,
                        new Action<TarSettingsTask>() {
                            @Override
                            void execute(TarSettingsTask t) {
                                t.group = 'Insight'
                                t.task = resolvedTaskName
                                t.description = "Display settings of the '${resolvedTaskName}' TAR task."
                            }
                        })
                }
            }
        })

        project.tasks.register('taskSettings', TaskSettingsTask,
            new Action<TaskSettingsTask>() {
                @Override
                void execute(TaskSettingsTask t) {
                    t.group = 'Insight'
                    t.description = 'Display the settings of a Task.'
                }
            })

        project.tasks.addRule('Pattern: <TaskName>TaskSettings: Displays the settings of a Task.', new Action<String>() {
            @Override
            void execute(String taskName) {
                if (taskName.endsWith('TaskSettings')) {
                    String resolvedTaskName = taskName - 'TaskSettings'
                    project.tasks.register(taskName, TaskSettingsTask,
                        new Action<TaskSettingsTask>() {
                            @Override
                            void execute(TaskSettingsTask t) {
                                t.group = 'Insight'
                                t.task = resolvedTaskName
                                t.description = "Display the settings of the '${resolvedTaskName}' Task."
                            }
                        })
                }
            }
        })

        if (isRootProject(project)) {
            project.extensions.create('projects', ProjectsExtension, project)

            project.tasks.register('listProjects', ListProjectsTask,
                new Action<ListProjectsTask>() {
                    @Override
                    void execute(ListProjectsTask t) {
                        t.group = 'Insight'
                        t.description = 'List all projects.'
                    }
                })

            project.tasks.register('listIncludedBuilds', ListIncludedBuildsTask,
                new Action<ListIncludedBuildsTask>() {
                    @Override
                    void execute(ListIncludedBuildsTask t) {
                        t.group = 'Insight'
                        t.description = 'List all included builds.'
                    }
                })
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(BasePlugin)) {
            project.pluginManager.apply(BasePlugin)
        }
    }

    static boolean isRootProject(Project project) {
        project == project.rootProject
    }
}
