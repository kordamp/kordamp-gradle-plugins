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
package org.kordamp.gradle.plugin.project.java

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.tasks.JavaExec
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.javadoc.JavadocPlugin
import org.kordamp.gradle.plugin.project.ProjectPlugin
import org.kordamp.gradle.plugin.project.java.tasks.JarSettingsTask
import org.kordamp.gradle.plugin.project.java.tasks.JavaCompilerSettingsTask
import org.kordamp.gradle.plugin.project.java.tasks.JavaExecSettingsTask
import org.kordamp.gradle.plugin.project.java.tasks.SourceSetSettingsTask
import org.kordamp.gradle.plugin.project.java.tasks.SourceSetsTask
import org.kordamp.gradle.plugin.project.java.tasks.TestSettingsTask
import org.kordamp.gradle.plugin.project.java.tasks.WarSettingsTask

/**
 *
 * @author Andres Almiray
 * @since 0.30.0
 */
@CompileStatic
class JavaProjectPlugin extends AbstractKordampPlugin {
    Project project

    void apply(Project project) {
        this.project = project

        applyPlugins(project)
        project.childProjects.values().each {
            applyPlugins(it)
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(JavaProjectPlugin)) {
            project.pluginManager.apply(JavaProjectPlugin)
        }
    }

    private void applyPlugins(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        ProjectPlugin.applyIfMissing(project)
        JavadocPlugin.applyIfMissing(project)

        project.pluginManager.withPlugin('java-base', new Action<AppliedPlugin>() {
            @Override
            void execute(AppliedPlugin appliedPlugin) {
                project.tasks.register('sourceSets', SourceSetsTask,
                    new Action<SourceSetsTask>() {
                        @Override
                        void execute(SourceSetsTask t) {
                            t.group = 'Insight'
                            t.description = "Displays all sourceSets available in project '$project.name'."
                        }
                    })

                project.tasks.register('sourceSetSettings', SourceSetSettingsTask,
                    new Action<SourceSetSettingsTask>() {
                        @Override
                        void execute(SourceSetSettingsTask t) {
                            t.group = 'Insight'
                            t.description = 'Display the settings of a SourceSet.'
                        }
                    })

                project.tasks.addRule('Pattern: <SourceSetName>SourceSetSettings: Displays the settings of a SourceSet.', new Action<String>() {
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
                                        t.description = "Display the settings of the '${resolvedSourceSetName}' sourceSet."
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
                            t.description = 'Display Java compiler settings.'
                        }
                    })

                project.tasks.addRule('Pattern: compile<SourceSetName>JavaSettings: Displays compiler settings of a JavaCompile task.', new Action<String>() {
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
                                        t.description = "Display Java compiler settings of the '${resolvedTaskName}' task."
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
                            t.description = 'Display test task settings.'
                        }
                    })

                project.tasks.addRule('Pattern: <SourceSetName>TestSettings: Displays settings of a Test task.', new Action<String>() {
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
                                        t.description = "Display settings of the '${resolvedTaskName}' task."
                                    }
                                })
                        }
                    }
                })

                project.tasks.register('jarSettings', JarSettingsTask,
                    new Action<JarSettingsTask>() {
                        @Override
                        void execute(JarSettingsTask t) {
                            t.group = 'Insight'
                            t.description = 'Display JAR settings.'
                        }
                    })

                project.tasks.addRule('Pattern: <JarName>JarSettings: Displays settings of a JAR task.', new Action<String>() {
                    @Override
                    void execute(String taskName) {
                        if (taskName.endsWith('JarSettings')) {
                            String resolvedTaskName = taskName - 'JarSettings'
                            resolvedTaskName = resolvedTaskName ?: 'jar'
                            project.tasks.register(taskName, JarSettingsTask,
                                new Action<JarSettingsTask>() {
                                    @Override
                                    void execute(JarSettingsTask t) {
                                        t.group = 'Insight'
                                        t.task = resolvedTaskName
                                        t.description = "Display settings of the '${resolvedTaskName}' JAR task."
                                    }
                                })
                        }
                    }
                })
            }
        })

        project.pluginManager.withPlugin('war', new Action<AppliedPlugin>() {
            @Override
            void execute(AppliedPlugin appliedPlugin) {
                project.tasks.register('warSettings', WarSettingsTask,
                    new Action<WarSettingsTask>() {
                        @Override
                        void execute(WarSettingsTask t) {
                            t.group = 'Insight'
                            t.description = 'Display WAR settings.'
                        }
                    })

                project.tasks.addRule('Pattern: <WarName>WarSettings: Displays settings of a WAR task.', new Action<String>() {
                    @Override
                    void execute(String taskName) {
                        if (taskName.endsWith('WarSettings')) {
                            String resolvedTaskName = taskName - 'WarSettings'
                            resolvedTaskName = resolvedTaskName ?: 'war'
                            project.tasks.register(taskName, WarSettingsTask,
                                new Action<WarSettingsTask>() {
                                    @Override
                                    void execute(WarSettingsTask t) {
                                        t.group = 'Insight'
                                        t.task = resolvedTaskName
                                        t.description = "Display settings of the '${resolvedTaskName}' WAR task."
                                    }
                                })
                        }
                    }
                })
            }
        })

        project.afterEvaluate {
            project.tasks.withType(JavaExec, new Action<JavaExec>() {
                @Override
                void execute(JavaExec t) {
                    String resolvedTaskName = t.name
                    project.tasks.register(t.name + 'Settings', JavaExecSettingsTask,
                        new Action<JavaExecSettingsTask>() {
                            @Override
                            void execute(JavaExecSettingsTask s) {
                                s.group = 'Insight'
                                s.task = resolvedTaskName
                                s.description = "Display settings of the '${resolvedTaskName}' task."
                            }
                        })
                }
            })
        }
    }
}
