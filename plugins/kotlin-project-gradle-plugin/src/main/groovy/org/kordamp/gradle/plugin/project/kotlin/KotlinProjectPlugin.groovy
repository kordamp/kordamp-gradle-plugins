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
package org.kordamp.gradle.plugin.project.kotlin

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.AppliedPlugin
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.kotlindoc.KotlindocPlugin
import org.kordamp.gradle.plugin.project.java.JavaProjectPlugin
import org.kordamp.gradle.plugin.project.kotlin.tasks.KotlinCompilerSettingsTask

/**
 * @author Andres Almiray
 * @since 0.30.0
 */
@CompileStatic
class KotlinProjectPlugin extends AbstractKordampPlugin {
    Project project

    void apply(Project project) {
        this.project = project

        applyPlugins(project)
        project.childProjects.values().each {
            applyPlugins(it)
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(KotlinProjectPlugin)) {
            project.pluginManager.apply(KotlinProjectPlugin)
        }
    }

    private void applyPlugins(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        JavaProjectPlugin.applyIfMissing(project)
        KotlindocPlugin.applyIfMissing(project)

        project.pluginManager.withPlugin('org.jetbrains.kotlin.jvm', new Action<AppliedPlugin>() {
            @Override
            void execute(AppliedPlugin appliedPlugin) {
                project.tasks.register('kotlinCompilerSettings', KotlinCompilerSettingsTask,
                    new Action<KotlinCompilerSettingsTask>() {
                        @Override
                        void execute(KotlinCompilerSettingsTask t) {
                            t.group = 'Insight'
                            t.description = 'Display Kotlin compiler settings.'
                        }
                    })

                project.tasks.addRule('Pattern: compile<SourceSetName>KotlinSettings: Displays compiler settings of a KotlinCompile task.', new Action<String>() {
                    @Override
                    void execute(String taskName) {
                        if (taskName.startsWith('compile') && taskName.endsWith('KotlinSettings')) {
                            String resolvedTaskName = taskName - 'Settings'
                            project.tasks.register(taskName, KotlinCompilerSettingsTask,
                                new Action<KotlinCompilerSettingsTask>() {
                                    @Override
                                    void execute(KotlinCompilerSettingsTask t) {
                                        t.group = 'Insight'
                                        t.task = resolvedTaskName
                                        t.description = "Display Kotlin compiler settings of the '${resolvedTaskName}' task."
                                    }
                                })
                        }
                    }
                })
            }
        })
    }
}
