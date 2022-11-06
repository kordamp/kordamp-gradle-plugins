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
package org.kordamp.gradle.plugin.project.scala

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.AppliedPlugin
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.project.java.JavaProjectPlugin
import org.kordamp.gradle.plugin.project.scala.tasks.ScalaCompilerSettingsTask
import org.kordamp.gradle.plugin.scaladoc.ScaladocPlugin

/**
 * @author Andres Almiray
 * @since 0.30.0
 */
@CompileStatic
class ScalaProjectPlugin extends AbstractKordampPlugin {
    Project project

    ScalaProjectPlugin() {
        super('org.kordamp.gradle.scala-project')
    }

    void apply(Project project) {
        this.project = project

        applyPlugins(project)
        project.childProjects.values().each {
            applyPlugins(it)
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(ScalaProjectPlugin)) {
            project.pluginManager.apply(ScalaProjectPlugin)
        }
    }

    private void applyPlugins(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        JavaProjectPlugin.applyIfMissing(project)
        ScaladocPlugin.applyIfMissing(project)

        project.pluginManager.withPlugin('scala-base', new Action<AppliedPlugin>() {
            @Override
            void execute(AppliedPlugin appliedPlugin) {
                project.tasks.register('scalaCompilerSettings', ScalaCompilerSettingsTask,
                    new Action<ScalaCompilerSettingsTask>() {
                        @Override
                        void execute(ScalaCompilerSettingsTask t) {
                            t.group = 'Insight'
                            t.description = 'Display Scala compiler settings.'
                        }
                    })

                project.tasks.addRule('Pattern: compile<SourceSetName>ScalaSettings: Displays compiler settings of a ScalaCompile task.', new Action<String>() {
                    @Override
                    void execute(String taskName) {
                        if (taskName.startsWith('compile') && taskName.endsWith('ScalaSettings')) {
                            String resolvedTaskName = taskName - 'Settings'
                            project.tasks.register(taskName, ScalaCompilerSettingsTask,
                                new Action<ScalaCompilerSettingsTask>() {
                                    @Override
                                    void execute(ScalaCompilerSettingsTask t) {
                                        t.group = 'Insight'
                                        t.task = resolvedTaskName
                                        t.description = "Display Scala compiler settings of the '${resolvedTaskName}' task."
                                    }
                                })
                        }
                    }
                })
            }
        })
    }
}
