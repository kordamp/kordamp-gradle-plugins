/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2021 Andres Almiray.
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
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.execution.TaskExecutionGraph
import org.kordamp.gradle.listener.AllProjectsEvaluatedListener
import org.kordamp.gradle.listener.ProjectEvaluatedListener
import org.kordamp.gradle.listener.ProjectEvaluationListenerManager
import org.kordamp.gradle.listener.TaskGraphReadyListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.extensions.ConfigExtension
import org.kordamp.gradle.plugin.base.tasks.ArchivesTask
import org.kordamp.gradle.plugin.base.tasks.ClearKordampFileCacheTask
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

import javax.inject.Named

import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addAllProjectsEvaluatedListener
import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addProjectEvaluatedListener
import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addTaskGraphReadyListener
import static org.kordamp.gradle.util.PluginUtils.checkFlag
import static org.kordamp.gradle.util.PluginUtils.isGradle7Compatible
import static org.kordamp.gradle.util.PluginUtils.resolveConfig

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class BasePlugin extends AbstractKordampPlugin {
    static final String ORG_KORDAMP_GRADLE_BASE_VALIDATE = 'org.kordamp.gradle.base.validate'
    static final String ORG_KORDAMP_GRADLE_BASE_DEPENDENCY_MANAGEMENT = 'org.kordamp.gradle.base.dependency.management'

    Project project

    BasePlugin() {
        super('org.kordamp.gradle.base')
    }

    void apply(Project project) {
        project.allprojects { Project p ->
            ConfigExtension.createIfMissing(p)
        }

        if (true) {
            // TODO: remove after testing
            return
        }

        Banner.display(project)
        this.project = project
        ProjectEvaluationListenerManager.register(project.gradle)

        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        addProjectEvaluatedListener(project, new BaseProjectEvaluatedListener())
        if (isRootProject(project)) {
            addAllProjectsEvaluatedListener(project.rootProject, new BaseAllProjectsEvaluatedListener())
            addTaskGraphReadyListener(project.rootProject, new BaseTaskGraphReadyListener())
        }

        if (!project.plugins.findPlugin(org.gradle.api.plugins.BasePlugin)) {
            project.pluginManager.apply(org.gradle.api.plugins.BasePlugin)
        }

        if (!project.extensions.findByType(ProjectConfigurationExtension)) {
            project.extensions.create(ProjectConfigurationExtension.CONFIG_NAME, ProjectConfigurationExtension, project)
        }

        project.tasks.register('verify', DefaultTask,
            new Action<DefaultTask>() {
                @Override
                void execute(DefaultTask t) {
                    t.dependsOn(project.tasks.named('build'))
                    t.group = 'Build'
                    t.description = 'Assembles and tests this project.'
                }
            })

        project.tasks.register('package', DefaultTask,
            new Action<DefaultTask>() {
                @Override
                void execute(DefaultTask t) {
                    t.dependsOn(project.tasks.named('assemble'))
                    t.group = 'Build'
                    t.description = 'Assembles the outputs of this project.'
                }
            })

        project.tasks.register('clearKordampFileCache', ClearKordampFileCacheTask,
            new Action<ClearKordampFileCacheTask>() {
                @Override
                void execute(ClearKordampFileCacheTask t) {
                    t.group = 'Insight'
                    t.description = 'Clears the Kordamp file cache'
                }
            })

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

        project.tasks.register('archives', ArchivesTask,
            new Action<ArchivesTask>() {
                @Override
                void execute(ArchivesTask t) {
                    t.group = 'Insight'
                    t.description = "Displays all configured archives in project '$project.name'."
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
            if (!isGradle7Compatible()) {
                project.extensions.create('projects', ProjectsExtension, project)
            } else {
                project.extensions.create('gradleProjects', ProjectsExtension, project)
            }

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

    @Named('base')
    private class BaseProjectEvaluatedListener implements ProjectEvaluatedListener {
        @Override
        void projectEvaluated(Project project) {
            ProjectConfigurationExtension rootExtension = project.rootProject.extensions.findByType(ProjectConfigurationExtension)
            ProjectConfigurationExtension extension = project.extensions.findByType(ProjectConfigurationExtension)
            extension.normalize()

            boolean validate = checkFlag(ORG_KORDAMP_GRADLE_BASE_VALIDATE, true)

            List<String> errors = []
            if (isRootProject(project)) {
                // extension == rootExtension
                extension.postMerge()
            } else {
                // parent project may not have applied kordamp.base
                if (rootExtension) {
                    extension.merge(rootExtension)
                } else {
                    extension.postMerge()
                }
            }

            if (validate) errors.addAll(extension.validate())

            if (validate && errors) {
                errors.each { project.logger.error(it) }
                boolean ideaSync = (System.getProperty('idea.sync.active') ?: 'false').toBoolean()
                if (!ideaSync) throw new GradleException("Project ${project.name} has not been properly configured.")
            }
        }
    }

    @Named('base')
    private class BaseAllProjectsEvaluatedListener implements AllProjectsEvaluatedListener {
        @Override
        void allProjectsEvaluated(Project rootProject) {
            // noop
            if (checkFlag(ORG_KORDAMP_GRADLE_BASE_DEPENDENCY_MANAGEMENT, false)) {
                rootProject.allprojects(new Action<Project>() {
                    @Override
                    void execute(Project project) {
                        resolveConfig(project).dependencyManagement.resolve()
                    }
                })
            }
        }
    }

    @Named('base')
    private class BaseTaskGraphReadyListener implements TaskGraphReadyListener {
        @Override
        void taskGraphReady(Project rootProject, TaskExecutionGraph graph) {
            // noop
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
