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
package org.kordamp.gradle.plugin.kotlindoc

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.jetbrains.dokka.gradle.DokkaPlugin
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.DokkaVersion
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper
import org.kordamp.gradle.StringUtils
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.Kotlindoc
import org.kordamp.gradle.plugin.javadoc.JavadocPlugin

import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * Configures {@code kotlindoc} and {@code kotlindocJar} tasks.
 *
 * @author Andres Almiray
 * @since 0.7.0
 */
class KotlindocPlugin extends AbstractKordampPlugin {
    static final String KOTLINDOC_BASENAME = 'kotlindoc'

    private static final DokkaVersion DOKKA_VERSION = new DokkaVersion()

    static {
        DOKKA_VERSION.loadFrom(DokkaPlugin.class.getResourceAsStream('/META-INF/gradle-plugins/org.jetbrains.dokka.properties'))
    }

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
        if (!project.plugins.findPlugin(KotlindocPlugin)) {
            project.plugins.apply(KotlindocPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        // apply first then we can be certain javadoc tasks can be located on time
        JavadocPlugin.applyIfMissing(project)

        project.afterEvaluate {
            ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
            setEnabled(effectiveConfig.kotlindoc.enabled)

            if (!enabled) {
                return
            }

            project.plugins.withType(KotlinBasePluginWrapper) {
                Javadoc javadoc = project.tasks.findByName(JavadocPlugin.JAVADOC_TASK_NAME)
                if (!javadoc) return

                effectiveConfig.kotlindoc.outputFormats.each { String format ->
                    Task kotlindoc = createKotlindocTaskIfNeeded(project, format)
                    if (!kotlindoc) return
                    effectiveConfig.kotlindoc.kotlindocTasks() << kotlindoc

                    Task kotlindocJar = createKotlindocJarTask(project, kotlindoc, format)
                    project.tasks.findByName(org.gradle.api.plugins.BasePlugin.ASSEMBLE_TASK_NAME).dependsOn(kotlindocJar)
                    effectiveConfig.kotlindoc.kotlindocJarTasks() << kotlindocJar

                    effectiveConfig.kotlindoc.projects() << project
                }
            }
        }
    }

    private Task createKotlindocTaskIfNeeded(Project project, String format) {
        String formatName = format == 'html-as-java' ? 'htmljava' : format
        String taskName = KOTLINDOC_BASENAME + StringUtils.capitalize(formatName)

        DokkaTask kotlindocTask = project.tasks.findByName(taskName)
        Task classesTask = project.tasks.findByName('classes')

        if (classesTask && !kotlindocTask) {
            kotlindocTask = project.tasks.create(taskName, DokkaTask) {
                dependsOn classesTask
                group JavaBasePlugin.DOCUMENTATION_GROUP
                description "Generates Kotlindoc API documentation in $format format"
                dokkaFatJar = "org.jetbrains.dokka:dokka-fatjar:${DOKKA_VERSION.version}"
            }
        }

        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        applyConfiguration(effectiveConfig.kotlindoc, kotlindocTask, format, formatName)

        kotlindocTask
    }

    private Task createKotlindocJarTask(Project project, Task kotlindoc, String format) {
        String formatName = format == 'html-as-java' ? 'htmljava' : format
        String resolvedClassifier = 'kotlindoc'
        String taskName = KOTLINDOC_BASENAME + StringUtils.capitalize(formatName) + 'Jar'
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)

        if (effectiveConfig.kotlindoc.outputFormats.size() > 1) {
            resolvedClassifier += '-' + formatName
        }

        Task kotlindocJarTask = project.tasks.findByName(taskName)

        if (!kotlindocJarTask) {
            kotlindocJarTask = project.tasks.create(taskName, Jar) {
                dependsOn kotlindoc
                group JavaBasePlugin.DOCUMENTATION_GROUP
                description "An archive of the $format formatted Kotlindoc API docs"
                classifier resolvedClassifier
                from kotlindoc.outputDirectory
            }
        }

        if (effectiveConfig.kotlindoc.replaceJavadoc && effectiveConfig.kotlindoc.outputFormats.indexOf(format) == 0) {
            kotlindocJarTask.classifier = 'javadoc'
            project.tasks.findByName(JavadocPlugin.JAVADOC_TASK_NAME)?.enabled = false
            project.tasks.findByName(JavadocPlugin.JAVADOC_JAR_TASK_NAME)?.enabled = false

            if (project.plugins.findPlugin(MavenPublishPlugin)) {
                project.publishing {
                    publications {
                        mainPublication(MavenPublication) {
                            artifact kotlindocJarTask
                        }
                    }
                }
            }
        }

        kotlindocJarTask
    }

    static void applyConfiguration(Kotlindoc kotlindoc, DokkaTask task, String format, String formatName) {
        task.moduleName = kotlindoc.moduleName
        task.outputFormat = format
        task.outputDirectory = kotlindoc.outputDirectory.absolutePath + File.separator + formatName
        task.jdkVersion = kotlindoc.jdkVersion
        task.cacheRoot = kotlindoc.cacheRoot
        task.languageVersion = kotlindoc.languageVersion
        task.languageVersion = kotlindoc.languageVersion
        task.apiVersion = kotlindoc.apiVersion
        task.includeNonPublic = kotlindoc.includeNonPublic
        task.skipDeprecated = kotlindoc.skipDeprecated
        task.reportUndocumented = kotlindoc.reportUndocumented
        task.skipEmptyPackages = kotlindoc.skipEmptyPackages
        task.noStdlibLink = kotlindoc.noStdlibLink
        task.impliedPlatforms = new ArrayList<>(kotlindoc.impliedPlatforms)
        task.includes = new ArrayList<>(kotlindoc.includes)
        task.samples = new ArrayList<>(kotlindoc.samples)

        kotlindoc.linkMappings.resolveLinkMappings().each { linkMapping ->
            task.linkMapping { lm ->
                lm.dir = linkMapping.dir
                lm.url = linkMapping.url
                lm.path = linkMapping.path
                lm.suffix = linkMapping.suffix
            }
        }

        kotlindoc.externalDocumentationLinks.resolveExternalDocumentationLinks().each { link ->
            task.externalDocumentationLink { edl ->
                edl.url = link.url
                edl.packageListUrl = link.packageListUrl
            }
        }

        kotlindoc.packageOptions.resolvePackageOptions().each { packageOption ->
            task.packageOptions { po ->
                po.prefix = packageOption.prefix
                po.includeNonPublic = packageOption.includeNonPublic
                po.reportUndocumented = packageOption.reportUndocumented
                po.skipDeprecated = packageOption.skipDeprecated
                po.suppress = packageOption.suppress
            }
        }
    }
}
