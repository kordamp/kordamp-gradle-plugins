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
package org.kordamp.gradle.plugin.jar

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.bundling.Jar
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.BuildInfo
import org.kordamp.gradle.plugin.buildinfo.BuildInfoPlugin
import org.kordamp.gradle.plugin.minpom.MinPomPlugin

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.StringUtils.isNotBlank
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * Configures a {@code jar} task.
 * Configures Manifest and MetaInf entries if the {@code config.release} is enabled.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class JarPlugin extends AbstractKordampPlugin {
    Project project

    void apply(Project project) {
        this.project = project

        if (isRootProject(project)) {
            if (project.childProjects.size()) {
                project.childProjects.values().each {
                    configureProject(it)
                }
            } else {
                configureProject(project)
            }
        } else {
            configureProject(project)
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(JarPlugin)) {
            project.pluginManager.apply(JarPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        BuildInfoPlugin.applyIfMissing(project)
        MinPomPlugin.applyIfMissing(project)

        project.pluginManager.withPlugin('java-base') {
            project.afterEvaluate {
                createJarTaskIfNeeded(project)
            }
        }

        project.rootProject.gradle.addBuildListener(new BuildAdapter() {
            @Override
            void projectsEvaluated(Gradle gradle) {
                project.tasks.withType(Jar) { Jar jarTask ->
                    if (jarTask.name == 'jar') configureJarMetainf(project, jarTask)
                    configureJarManifest(project, jarTask)
                }
            }
        })
    }

    @CompileDynamic
    private void createJarTaskIfNeeded(Project project) {
        if (!project.sourceSets.findByName('main')) return

        String taskName = 'jar'

        Task jarTask = project.tasks.findByName(taskName)

        if (!jarTask) {
            jarTask = project.tasks.create(taskName, Jar) {
                dependsOn project.sourceSets.main.output
                group org.gradle.api.plugins.BasePlugin.BUILD_GROUP
                description 'Assembles a jar archive'
                from project.sourceSets.main.output
                destinationDir project.file("${project.buildDir}/libs")
            }
        }

        jarTask.configure {
            metaInf {
                from(project.rootProject.file('.')) {
                    include 'LICENSE*'
                }
            }
        }
    }

    @CompileDynamic
    private void configureJarMetainf(Project project, Jar jarTask) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project.rootProject) ?: resolveEffectiveConfig(project)
        setEnabled(effectiveConfig.minpom.enabled)

        if (enabled) {
            jarTask.configure {
                dependsOn MinPomPlugin.MINPOM_TASK_NAME
                metaInf {
                    from(MinPomPlugin.resolveMinPomDestinationDir(project)) {
                        into "maven/${project.group}/${project.name}"
                    }
                }
            }
        }
    }

    @CompileDynamic
    private void configureJarManifest(Project project, Jar jarTask) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project.rootProject) ?: resolveEffectiveConfig(project)

        if (effectiveConfig.release) {
            Map<String, String> attributesMap = [:]

            if (effectiveConfig.buildInfo.enabled) {
                checkBuildInfoAttribute(effectiveConfig.buildInfo, 'buildCreatedBy', attributesMap, 'Created-By')
                checkBuildInfoAttribute(effectiveConfig.buildInfo, 'buildBy', attributesMap, 'Build-By')
                checkBuildInfoAttribute(effectiveConfig.buildInfo, 'buildJdk', attributesMap, 'Build-Jdk')
                checkBuildInfoAttribute(effectiveConfig.buildInfo, 'buildDate', attributesMap, 'Build-Date')
                checkBuildInfoAttribute(effectiveConfig.buildInfo, 'buildTime', attributesMap, 'Build-Time')
                checkBuildInfoAttribute(effectiveConfig.buildInfo, 'buildRevision', attributesMap, 'Build-Revision')
            }

            if (effectiveConfig.info.specification.enabled) {
                attributesMap.'Specification-Title' = effectiveConfig.info.specification.title
                attributesMap.'Specification-Version' = effectiveConfig.info.specification.version
                if (isNotBlank(effectiveConfig.info.specification.vendor)) attributesMap.'Specification-Vendor' = effectiveConfig.info.specification.vendor
            }

            if (effectiveConfig.info.implementation.enabled) {
                attributesMap.'Implementation-Title' = effectiveConfig.info.implementation.title
                attributesMap.'Implementation-Version' = effectiveConfig.info.implementation.version
                if (isNotBlank(effectiveConfig.info.implementation.vendor)) attributesMap.'Implementation-Vendor' = effectiveConfig.info.implementation.vendor
            }

            jarTask.configure {
                manifest {
                    attributes(attributesMap)
                }
            }
        }
    }

    @CompileDynamic
    private static void checkBuildInfoAttribute(BuildInfo buildInfo, String key, Map map, String manifestKey) {
        if (!buildInfo."skip${key.capitalize()}") {
            map[manifestKey] = buildInfo[key]
        }
    }
}
