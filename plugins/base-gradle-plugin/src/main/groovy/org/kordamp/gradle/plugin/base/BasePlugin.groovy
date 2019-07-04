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
package org.kordamp.gradle.plugin.base

import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.kordamp.gradle.PluginUtils
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.tasks.EffectiveSettingsTask
import org.kordamp.gradle.plugin.base.tasks.ExtensionsTask
import org.kordamp.gradle.plugin.base.tasks.GroovyCompilerSettingsTask
import org.kordamp.gradle.plugin.base.tasks.JavaCompilerSettingsTask
import org.kordamp.gradle.plugin.base.tasks.ListIncludedBuildsTask
import org.kordamp.gradle.plugin.base.tasks.ListProjectsTask
import org.kordamp.gradle.plugin.base.tasks.PluginsTask
import org.kordamp.gradle.plugin.base.tasks.ProjectPropertiesTask
import org.kordamp.gradle.plugin.base.tasks.RepositoriesTask
import org.kordamp.gradle.plugin.base.tasks.SourceSetSettingsTask
import org.kordamp.gradle.plugin.base.tasks.SourceSetsTask
import org.kordamp.gradle.plugin.base.tasks.TestSettingsTask

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class BasePlugin extends AbstractKordampPlugin {
    static final String ORG_KORDAMP_GRADLE_BASE_VALSourceSetNameATE = 'org.kordamp.gradle.base.validate'

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

        project.tasks.register('sourceSets', SourceSetsTask,
            new Action<SourceSetsTask>() {
                @Override
                void execute(SourceSetsTask t) {
                    t.group = 'Insight'
                    t.description = "Displays all sourceSets configured in project '$project.name'."
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

        project.tasks.register('sourceSetSettings', SourceSetSettingsTask,
            new Action<SourceSetSettingsTask>() {
                @Override
                void execute(SourceSetSettingsTask t) {
                    t.group = 'Insight'
                    t.description = 'Display Java compiler configuration.'
                }
            })

        project.tasks.addRule('Pattern: <SourceSetName>SourceSetSettings: Displays configuration of a SourceSet.', new Action<String>() {
            @Override
            void execute(String sourceSetName) {
                if (sourceSetName.endsWith('SourceSetSettings')) {
                    String resolvedSourceSetName = sourceSetName - 'SourceSetSettings'
                    project.tasks.register(sourceSetName, SourceSetSettingsTask,
                        new Action<SourceSetSettingsTask>() {
                            @Override
                            void execute(SourceSetSettingsTask t) {
                                t.group = 'Insight'
                                t.sourceSet = resolvedSourceSetName
                                t.description = "Display configuration for the ${resolvedSourceSetName} sourceSet."
                            }
                        })
                }
            }
        })

        project.tasks.register('javaCompilerSettings', JavaCompilerSettingsTask,
            new Action<JavaCompilerSettingsTask>() {
                @Override
                void execute(JavaCompilerSettingsTask t) {
                    t.group = 'Insight'
                    t.description = 'Display Java compiler configuration.'
                }
            })

        project.tasks.addRule('Pattern: compile<SourceSetName>JavaSettings: Displays compiler configuration of a JavaCompile task.', new Action<String>() {
            @Override
            void execute(String taskName) {
                if (taskName.startsWith('compile') && taskName.endsWith('JavaSettings')) {
                    String resolvedTaskName = taskName - 'Settings'
                    project.tasks.register(taskName, JavaCompilerSettingsTask,
                        new Action<JavaCompilerSettingsTask>() {
                            @Override
                            void execute(JavaCompilerSettingsTask t) {
                                t.group = 'Insight'
                                t.task = resolvedTaskName
                                t.description = "Display Java compiler configuration for the ${resolvedTaskName} task."
                            }
                        })
                }
            }
        })

        project.tasks.register('testSettings', TestSettingsTask,
            new Action<TestSettingsTask>() {
                @Override
                void execute(TestSettingsTask t) {
                    t.group = 'Insight'
                    t.description = 'Display test task configuration.'
                }
            })

        project.tasks.addRule('Pattern: <SourceSetName>TestSettings: Displays configuration of a Test task.', new Action<String>() {
            @Override
            void execute(String taskName) {
                if (taskName.endsWith('TestSettings')) {
                    String resolvedTaskName = taskName - 'Settings'
                    project.tasks.register(taskName, TestSettingsTask,
                        new Action<TestSettingsTask>() {
                            @Override
                            void execute(TestSettingsTask t) {
                                t.group = 'Insight'
                                t.task = resolvedTaskName
                                t.description = "Display configuration for the ${resolvedTaskName} task."
                            }
                        })
                }
            }
        })

        if (isRootProject(project)) {
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

            project.gradle.addBuildListener(new BuildAdapter() {
                @Override
                void projectsEvaluated(Gradle gradle) {
                    project.subprojects.each { Project subproject ->
                        PluginUtils.resolveEffectiveConfig(subproject).rootReady()
                    }
                    PluginUtils.resolveEffectiveConfig(project).rootReady()
                }
            })
        }

        project.afterEvaluate {
            if (project.plugins.findPlugin('groovy')) {
                project.tasks.register('groovyCompilerSettings', GroovyCompilerSettingsTask,
                    new Action<GroovyCompilerSettingsTask>() {
                        @Override
                        void execute(GroovyCompilerSettingsTask t) {
                            t.group = 'Insight'
                            t.description = 'Display Groovy compiler configuration.'
                        }
                    })

                project.tasks.addRule('Pattern: compile<SourceSetName>GroovySettings: Displays compiler configuration of a GroovyCompile task.', new Action<String>() {
                    @Override
                    void execute(String taskName) {
                        if (taskName.startsWith('compile') && taskName.endsWith('GroovySettings')) {
                            String resolvedTaskName = taskName - 'Settings'
                            project.tasks.register(taskName, GroovyCompilerSettingsTask,
                                new Action<GroovyCompilerSettingsTask>() {
                                    @Override
                                    void execute(GroovyCompilerSettingsTask t) {
                                        t.group = 'Insight'
                                        t.task = resolvedTaskName
                                        t.description = "Display Groovy compiler configuration for the ${resolvedTaskName} task."
                                    }
                                })
                        }
                    }
                })
            }

            ProjectConfigurationExtension rootExtension = project.rootProject.extensions.findByType(ProjectConfigurationExtension)
            ProjectConfigurationExtension extension = project.extensions.findByType(ProjectConfigurationExtension)
            extension.normalize()

            boolean validate = PluginUtils.checkFlag(ORG_KORDAMP_GRADLE_BASE_VALSourceSetNameATE, true)

            List<String> errors = []
            if (isRootProject(project)) {
                // extension == rootExtension
                ProjectConfigurationExtension merged = extension.postMerge()
                if (validate) errors.addAll(merged.validate())
                project.extensions.create(ProjectConfigurationExtension.EFFECTIVE_CONFIG_NAME, ProjectConfigurationExtension, merged).ready()
            } else {
                // parent project may not have applied kordamp.base
                if (rootExtension) {
                    ProjectConfigurationExtension merged = extension.merge(rootExtension)
                    if (validate) errors.addAll(merged.validate())
                    project.extensions.create(ProjectConfigurationExtension.EFFECTIVE_CONFIG_NAME, ProjectConfigurationExtension, merged).ready()
                } else {
                    extension = extension.postMerge()
                    if (validate) errors.addAll(extension.validate())
                    project.extensions.create(ProjectConfigurationExtension.EFFECTIVE_CONFIG_NAME, ProjectConfigurationExtension, extension).ready()
                }
            }

            if (validate && errors) {
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
