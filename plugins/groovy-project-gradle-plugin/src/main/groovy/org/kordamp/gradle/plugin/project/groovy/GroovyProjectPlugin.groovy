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
package org.kordamp.gradle.plugin.project.groovy

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.AppliedPlugin
import org.kordamp.gradle.plugin.project.groovy.tasks.GroovyCompilerSettingsTask
import org.kordamp.gradle.plugin.project.java.JavaProjectPlugin

import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * @author Andres Almiray
 * @since 0.30.0
 */
@CompileStatic
class GroovyProjectPlugin implements Plugin<Project> {
    Project project

    void apply(Project project) {
        this.project = project

        if (isRootProject(project)) {
            applyPlugins(project)
            project.childProjects.values().each {
                applyPlugins(it)
            }
        } else {
            applyPlugins(project)
        }
    }

    static void applyPlugins(Project project) {
        JavaProjectPlugin.applyPlugins(project)

        project.pluginManager.withPlugin('groovy-base', new Action<AppliedPlugin>() {
            @Override
            void execute(AppliedPlugin appliedPlugin) {
                project.tasks.register('groovyCompilerSettings', GroovyCompilerSettingsTask,
                        new Action<GroovyCompilerSettingsTask>() {
                            @Override
                            void execute(GroovyCompilerSettingsTask t) {
                                t.group = 'Insight'
                                t.description = 'Display Groovy compiler settings.'
                            }
                        })

                project.tasks.addRule('Pattern: compile<SourceSetName>GroovySettings: Displays compiler settings of a GroovyCompile task.', new Action<String>() {
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
                                            t.description = "Display Groovy compiler settings of the '${resolvedTaskName}' task."
                                        }
                                    })
                        }
                    }
                })
            }
        })
    }
}
