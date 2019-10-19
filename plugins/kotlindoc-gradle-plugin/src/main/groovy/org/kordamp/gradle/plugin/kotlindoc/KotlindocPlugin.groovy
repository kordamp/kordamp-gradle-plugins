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
package org.kordamp.gradle.plugin.kotlindoc

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.TaskProvider
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

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * Configures {@code kotlindoc} and {@code kotlindocJar} tasks.
 *
 * @author Andres Almiray
 * @since 0.7.0
 */
class KotlindocPlugin extends AbstractKordampPlugin {
    static final String KOTLINDOC_BASENAME = 'kotlindoc'
    static final String AGGREGATE_KOTLINDOC_BASENAME = 'aggregateKotlindoc'

    private static final DokkaVersion DOKKA_VERSION = new DokkaVersion()

    static {
        DOKKA_VERSION.loadFrom(DokkaPlugin.class.getResourceAsStream('/META-INF/gradle-plugins/org.jetbrains.dokka.properties'))
    }

    Project project

    @CompileStatic
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
            configureRootProject(project, project.childProjects.size() > 0)
        } else {
            configureProject(project)
        }
    }

    @CompileStatic
    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(KotlindocPlugin)) {
            project.pluginManager.apply(KotlindocPlugin)
        }
    }

    @CompileStatic
    private void configureRootProject(Project project, boolean checkIfApplied) {
        if (checkIfApplied && hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)

        if (isRootProject(project) && !project.childProjects.isEmpty()) {
            project.gradle.addBuildListener(new BuildAdapter() {
                @Override
                void projectsEvaluated(Gradle gradle) {
                    createAggregateKotlindocTasks(project)
                    doConfigureRootProject(project)
                }
            })
        }
    }

    private void doConfigureRootProject(Project project) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        setEnabled(effectiveConfig.kotlindoc.enabled)

        if (!enabled) {
            return
        }

        if (!project.childProjects.isEmpty()) {
            effectiveConfig.kotlindoc.outputFormats.each { String format ->
                String formatName = format == 'html-as-java' ? 'htmljava' : format
                String suffix = StringUtils.capitalize(formatName)
                String taskName = AGGREGATE_KOTLINDOC_BASENAME + suffix

                List<DokkaTask> kotlindocs = []
                project.tasks.withType(DokkaTask) { DokkaTask kotlindoc -> if (kotlindoc.name != taskName && kotlindoc.enabled && kotlindoc.name.endsWith(suffix)) kotlindocs << kotlindoc }

                project.childProjects.values().each { Project p ->
                    if (p in effectiveConfig.kotlindoc.excludedProjects()) return
                    p.tasks.withType(DokkaTask) { DokkaTask kotlindoc -> if (kotlindoc.enabled && kotlindoc.name.endsWith(suffix)) kotlindocs << kotlindoc }
                }
                kotlindocs = kotlindocs.unique()

                DokkaTask aggregateKotlindocs = project.tasks.findByName(taskName)
                Jar aggregateKotlindocsJar = project.tasks.findByName(taskName + 'Jar')
                applyConfiguration(effectiveConfig.kotlindoc, aggregateKotlindocs, format, formatName)

                if (kotlindocs) {
                    aggregateKotlindocs.configure { task ->
                        task.enabled true
                        task.dependsOn kotlindocs
                        task.classpath = project.files(kotlindocs.classpath)

                        applyConfiguration(effectiveConfig.kotlindoc, task, format, formatName)
                    }
                    aggregateKotlindocsJar.configure {
                        enabled true
                        // classifier = effectiveConfig.kotlindoc.replaceJavadoc ? 'javadoc' : 'kotlindoc'
                    }
                }
            }
        }
    }

    @CompileStatic
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
                Javadoc javadoc = (Javadoc) project.tasks.findByName(JavadocPlugin.JAVADOC_TASK_NAME)
                if (!javadoc) return

                effectiveConfig.kotlindoc.outputFormats.each { String format ->
                    Task kotlindoc = createKotlindocTaskIfNeeded(project, format)
                    if (!kotlindoc) return
                    effectiveConfig.kotlindoc.kotlindocTasks() << kotlindoc

                    TaskProvider<Jar> kotlindocJar = createKotlindocJarTask(project, kotlindoc, format)
                    project.tasks.findByName(org.gradle.api.plugins.BasePlugin.ASSEMBLE_TASK_NAME).dependsOn(kotlindocJar)
                    effectiveConfig.kotlindoc.kotlindocJarTasks() << kotlindocJar

                    effectiveConfig.kotlindoc.projects() << project
                }
            }
        }
    }

    @CompileDynamic
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

    @CompileDynamic
    private TaskProvider<Jar> createKotlindocJarTask(Project project, Task kotlindoc, String format) {
        String formatName = format == 'html-as-java' ? 'htmljava' : format
        String resolvedClassifier = 'kotlindoc'
        String taskName = KOTLINDOC_BASENAME + StringUtils.capitalize(formatName) + 'Jar'
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)

        if (effectiveConfig.kotlindoc.outputFormats.size() > 1) {
            resolvedClassifier += '-' + formatName
        }


        TaskProvider<Jar> kotlindocJarTask = project.tasks.register(taskName, Jar,
            new Action<Jar>() {
                @Override
                void execute(Jar t) {
                    t.dependsOn kotlindoc
                    t.group JavaBasePlugin.DOCUMENTATION_GROUP
                    t.description "An archive of the $format formatted Kotlindoc API docs"
                    t.archiveClassifier.set(resolvedClassifier)
                    t.from kotlindoc.outputDirectory
                }
            })

        if (effectiveConfig.kotlindoc.replaceJavadoc && effectiveConfig.kotlindoc.outputFormats.indexOf(format) == 0) {
            kotlindocJarTask.configure(new Action<Jar>() {
                @Override
                void execute(Jar t) {
                    t.archiveClassifier.set('javadoc')
                }
            })
            project.tasks.findByName(JavadocPlugin.JAVADOC_TASK_NAME)?.enabled = false
            project.tasks.findByName(JavadocPlugin.JAVADOC_JAR_TASK_NAME)?.enabled = false

        }

        if (project.plugins.findPlugin(MavenPublishPlugin)) {
            project.publishing {
                publications {
                    main(MavenPublication) {
                        artifact kotlindocJarTask
                    }
                }
            }
        }

        kotlindocJarTask
    }

    @CompileDynamic
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
            task.linkMapping {
                delegate.dir = linkMapping.dir
                delegate.url = linkMapping.url
                delegate.path = linkMapping.path
                delegate.suffix = linkMapping.suffix
            }
        }

        kotlindoc.externalDocumentationLinks.resolveExternalDocumentationLinks().each { link ->
            task.externalDocumentationLink {
                delegate.url = link.url?.toURL()
                delegate.packageListUrl = link.packageListUrl?.toURL()
            }
        }

        kotlindoc.packageOptions.resolvePackageOptions().each { packageOption ->
            task.packageOptions {
                delegate.prefix = packageOption.prefix
                delegate.includeNonPublic = packageOption.includeNonPublic
                delegate.reportUndocumented = packageOption.reportUndocumented
                delegate.skipDeprecated = packageOption.skipDeprecated
                delegate.suppress = packageOption.suppress
            }
        }
    }

    private void createAggregateKotlindocTasks(Project project) {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)

        effectiveConfig.kotlindoc.outputFormats.each { String format ->
            String formatName = format == 'html-as-java' ? 'htmljava' : format
            String suffix = StringUtils.capitalize(formatName)
            String taskName = AGGREGATE_KOTLINDOC_BASENAME + suffix

            DokkaTask aggregateKotlindocs = project.tasks.create(taskName, DokkaTask) {
                enabled false
                group JavaBasePlugin.DOCUMENTATION_GROUP
                description "Generates aggregate Kotlindoc API documentation in $format format"
                dokkaFatJar = "org.jetbrains.dokka:dokka-fatjar:${DOKKA_VERSION.version}"
            }

            project.tasks.create(taskName + 'Jar', Jar) {
                enabled false
                dependsOn aggregateKotlindocs
                group JavaBasePlugin.DOCUMENTATION_GROUP
                description "An archive of the aggregated $format formatted Kotlindoc API docs"
                classifier 'kotlindoc' + '-' + formatName
                from aggregateKotlindocs.outputDirectory
            }
        }
    }
}
