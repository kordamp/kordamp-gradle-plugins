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
package org.kordamp.gradle.plugin.jar

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
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

    JarPlugin() {
        super(org.kordamp.gradle.plugin.base.plugins.Jar.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        configureProject(project)
        if (isRootProject(project)) {
            configureRootProject(project)
            project.childProjects.values().each {
                it.pluginManager.apply(JarPlugin)
            }
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
    }

    private void configureRootProject(Project project) {
        project.gradle.addBuildListener(new BuildAdapter() {
            @Override
            void projectsEvaluated(Gradle gradle) {
                project.tasks.withType(Jar) { Jar t ->
                    if (t.name == 'jar' && resolveEffectiveConfig(project).artifacts.jar.enabled) {
                        configureJarMetainf(project, t)
                        configureClasspathManifest(project, t)
                    }
                    configureJarManifest(project, t)
                }
                project.childProjects.values().each { Project p ->
                    p.tasks.withType(Jar) { Jar t ->
                        if (t.name == 'jar' && resolveEffectiveConfig(p).artifacts.jar.enabled) {
                            configureJarMetainf(p, t)
                            configureClasspathManifest(p, t)
                        }
                        configureJarManifest(p, t)
                    }
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
                group = org.gradle.api.plugins.BasePlugin.BUILD_GROUP
                description = 'Assembles a jar archive'
                from project.sourceSets.main.output
                destinationDirectory.set(project.file("${project.buildDir}/libs"))
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
    private static void configureJarMetainf(Project project, Jar jarTask) {
        ProjectConfigurationExtension config = /*resolveEffectiveConfig(project.rootProject) ?:*/ resolveEffectiveConfig(project)

        if (config.artifacts.minpom.enabled) {
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
    private static void configureJarManifest(Project project, Jar jarTask) {
        ProjectConfigurationExtension config = resolveEffectiveConfig(project.rootProject) // ?: resolveEffectiveConfig(project)

        if (config.release) {
            Map<String, String> attributesMap = [:]

            if (config.buildInfo.enabled) {
                checkBuildInfoAttribute(config.buildInfo, 'buildCreatedBy', attributesMap, 'Created-By')
                checkBuildInfoAttribute(config.buildInfo, 'buildBy', attributesMap, 'Build-By')
                checkBuildInfoAttribute(config.buildInfo, 'buildJdk', attributesMap, 'Build-Jdk')
                checkBuildInfoAttribute(config.buildInfo, 'buildOs', attributesMap, 'Build-Os')
                checkBuildInfoAttribute(config.buildInfo, 'buildDate', attributesMap, 'Build-Date')
                checkBuildInfoAttribute(config.buildInfo, 'buildTime', attributesMap, 'Build-Time')
                checkBuildInfoAttribute(config.buildInfo, 'buildRevision', attributesMap, 'Build-Revision')
            }

            if (config.info.specification.enabled) {
                attributesMap.'Specification-Title' = config.info.specification.title
                attributesMap.'Specification-Version' = config.info.specification.version
                if (isNotBlank(config.info.specification.vendor)) attributesMap.'Specification-Vendor' = config.info.specification.vendor
            }

            if (config.info.implementation.enabled) {
                attributesMap.'Implementation-Title' = config.info.implementation.title
                attributesMap.'Implementation-Version' = config.info.implementation.version
                if (isNotBlank(config.info.implementation.vendor)) attributesMap.'Implementation-Vendor' = config.info.implementation.vendor
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

    @CompileDynamic
    private static void configureClasspathManifest(Project project, Jar jarTask) {
        if (!project.configurations.findByName('runtimeClasspath')) {
            return
        }

        ProjectConfigurationExtension config = resolveEffectiveConfig(project)
        jarTask.setEnabled(config.artifacts.jar.enabled)

        if (!config.artifacts.jar.manifest.enabled) {
            return
        }

        if (config.artifacts.jar.manifest.addClasspath) {
            if (config.artifacts.jar.manifest.classpathLayoutType == 'simple') {
                calculateSimpleClasspath(project, jarTask, config.artifacts.jar.manifest.classpathPrefix)
            } else if (config.artifacts.jar.manifest.classpathLayoutType == 'repository') {
                calculateRepositoryClasspath(project, jarTask, config.artifacts.jar.manifest.classpathPrefix)
            }
        }
    }

    private static void calculateSimpleClasspath(Project project, Jar jarTask, String prefix) {
        project.configurations.named('runtimeClasspath', new Action<Configuration>() {
            @Override
            @CompileDynamic
            void execute(Configuration c) {
                List<String> classpath = []
                String p = isNotBlank(prefix) && !prefix.endsWith('/') ? prefix + '/' : prefix;

                c.resolvedConfiguration.resolvedArtifacts.each {
                    classpath << "${p}${it.file.name}".toString()
                }
                if (classpath) {
                    jarTask.configure {
                        manifest {
                            attributes('Class-Path': classpath.join(' '))
                        }
                    }
                }
            }
        })
    }

    private static void calculateRepositoryClasspath(Project project, Jar jarTask, String prefix) {
        project.configurations.named('runtimeClasspath', new Action<Configuration>() {
            @Override
            @CompileDynamic
            void execute(Configuration c) {
                List<String> classpath = []
                String p = isNotBlank(prefix) && !prefix.endsWith('/') ? prefix + '/' : prefix;

                c.resolvedConfiguration.resolvedArtifacts.each {
                    String g = it.moduleVersion.id.group.replace('.', '/')
                    String m = it.moduleVersion.id.name
                    String v = it.moduleVersion.id.version
                    classpath << "${p}${g}/${m}/${v}/${it.file.name}".toString()
                }
                if (classpath) {
                    jarTask.configure {
                        manifest {
                            attributes('Class-Path': classpath.join(' '))
                        }
                    }
                }
            }
        })
    }
}