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
package org.kordamp.gradle.plugin.kotlindoc

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenArtifact
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.dokka.gradle.DokkaPlugin
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.DokkaVersion
import org.jetbrains.dokka.gradle.GradlePassConfigurationImpl
import org.kordamp.gradle.StringUtils
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.Kotlindoc
import org.kordamp.gradle.plugin.javadoc.JavadocPlugin

import static org.kordamp.gradle.PluginUtils.registerJarVariant
import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * Configures {@code kotlindoc} and {@code kotlindocJar} tasks.
 *
 * @author Andres Almiray
 * @since 0.7.0
 */
@CompileStatic
class KotlindocPlugin extends AbstractKordampPlugin {
    static final String KOTLINDOC_BASENAME = 'kotlindoc'
    static final String AGGREGATE_KOTLINDOC_BASENAME = 'aggregateKotlindoc'
    static final String DOKKA_RUNTIME_CONFIGURATION_NAME = 'dokkaRuntime'

    private static final DokkaVersion DOKKA_VERSION = DokkaVersion.INSTANCE

    static {
        DOKKA_VERSION.loadFrom(DokkaPlugin.class.getResourceAsStream('/META-INF/gradle-plugins/org.jetbrains.dokka.properties'))
    }

    Project project

    KotlindocPlugin() {
        super(org.kordamp.gradle.plugin.base.plugins.Kotlindoc.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        configureDokkaRuntimeConfiguration(project)

        configureProject(project)
        if (isRootProject(project)) {
            configureRootProject(project)
            project.childProjects.values().each {
                it.pluginManager.apply(KotlindocPlugin)
            }
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(KotlindocPlugin)) {
            project.pluginManager.apply(KotlindocPlugin)
        }
    }

    private void configureRootProject(Project project) {
        project.gradle.addBuildListener(new BuildAdapter() {
            @Override
            void projectsEvaluated(Gradle gradle) {
                createAggregateTasks(project)
                doConfigureRootProject(project)
            }
        })
    }

    private void doConfigureRootProject(Project project) {
        ProjectConfigurationExtension config = resolveEffectiveConfig(project)
        setEnabled(config.docs.kotlindoc.aggregate.enabled)

        config.docs.kotlindoc.outputFormats.each { String format ->
            String formatName = format == 'html-as-java' ? 'htmljava' : format
            String suffix = StringUtils.capitalize(formatName)
            String taskName = AGGREGATE_KOTLINDOC_BASENAME + suffix

            List<DokkaTask> docTasks = []
            project.tasks.withType(DokkaTask) { DokkaTask t ->
                if (project in config.docs.kotlindoc.aggregate.excludedProjects) return
                if (t.name != taskName && t.enabled && t.name.endsWith(suffix)) docTasks << t
            }
            project.childProjects.values().each { Project p ->
                if (p in config.docs.kotlindoc.aggregate.excludedProjects) return
                p.tasks.withType(DokkaTask) { DokkaTask t -> if (t.enabled && t.name.endsWith(suffix)) docTasks << t }
            }
            docTasks = docTasks.unique()

            if (docTasks) {
                TaskProvider<DokkaTask> aggregateKotlindoc = project.tasks.named(taskName, DokkaTask,
                    new Action<DokkaTask>() {
                        @Override
                        @CompileDynamic
                        void execute(DokkaTask t) {
                            t.enabled = config.docs.kotlindoc.aggregate.enabled
                            if (!config.docs.kotlindoc.aggregate.fast) t.dependsOn docTasks
                            t.configuration.classpath = docTasks.configuration.classpath.flatten().unique()
                        }
                    })

                project.tasks.named(taskName + 'Jar', Jar,
                    new Action<Jar>() {
                        @Override
                        void execute(Jar t) {
                            t.enabled = config.docs.kotlindoc.aggregate.enabled
                            t.from aggregateKotlindoc.get().outputDirectory
                            t.onlyIf { aggregateKotlindoc.get().enabled }
                            // classifier = config.docs.kotlindoc.aggregate.replaceJavadoc ? 'javadoc' : 'kotlindoc'
                        }
                    })
            }
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)

        project.pluginManager.withPlugin('org.jetbrains.kotlin.jvm') {
            project.afterEvaluate {
                ProjectConfigurationExtension config = resolveEffectiveConfig(project)
                setEnabled(config.docs.kotlindoc.enabled)

                config.docs.kotlindoc.outputFormats.each { String format ->
                    TaskProvider<DokkaTask> kotlindoc = createKotlindocTask(project, format)
                    TaskProvider<Jar> kotlindocJar = createKotlindocJarTask(project, kotlindoc, format)
                    project.tasks.findByName(org.gradle.api.plugins.BasePlugin.ASSEMBLE_TASK_NAME).dependsOn(kotlindocJar)
                }
            }
        }
    }

    @CompileDynamic
    private TaskProvider<DokkaTask> createKotlindocTask(Project project, String format) {
        String formatName = format == 'html-as-java' ? 'htmljava' : format
        String taskName = KOTLINDOC_BASENAME + StringUtils.capitalize(formatName)

        ProjectConfigurationExtension config = resolveEffectiveConfig(project)

        project.tasks.register(taskName, DokkaTask,
            new Action<DokkaTask>() {
                @Override
                void execute(DokkaTask t) {
                    t.enabled = config.docs.kotlindoc.enabled
                    t.dependsOn project.tasks.named('classes')
                    t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                    t.description = "Generates Kotlindoc API documentation in $format format"
                    t.dokkaRuntime = project.configurations.findByName(DOKKA_RUNTIME_CONFIGURATION_NAME)
                    t.extensions.add('multiplatform', project.container(GradlePassConfigurationImpl))
                    t.extensions.add('configuration', new GradlePassConfigurationImpl())
                    applyConfiguration(config.docs.kotlindoc, t, format, formatName)
                }
            })
    }

    private TaskProvider<Jar> createKotlindocJarTask(Project project, TaskProvider<DokkaTask> kotlindoc, String format) {
        String formatName = format == 'html-as-java' ? 'htmljava' : format
        String resolvedClassifier = 'kotlindoc'
        String taskName = KOTLINDOC_BASENAME + StringUtils.capitalize(formatName) + 'Jar'
        ProjectConfigurationExtension config = resolveEffectiveConfig(project)

        if (config.docs.kotlindoc.outputFormats.size() > 1) {
            resolvedClassifier += '-' + formatName
        }

        TaskProvider<Jar> kotlindocJar = project.tasks.register(taskName, Jar,
            new Action<Jar>() {
                @Override
                void execute(Jar t) {
                    t.enabled = config.docs.kotlindoc.enabled
                    t.dependsOn kotlindoc
                    t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                    t.description = "An archive of the $format formatted Kotlindoc API docs"
                    t.archiveClassifier.set(resolvedClassifier)
                    t.from kotlindoc.get().outputDirectory
                    t.onlyIf { kotlindoc.get().enabled }
                }
            })

        if (config.docs.kotlindoc.enabled && project.pluginManager.hasPlugin('maven-publish')) {
            PublishingExtension publishing = project.extensions.findByType(PublishingExtension)
            MavenPublication mainPublication = (MavenPublication) publishing.publications.findByName('main')
            if (mainPublication) {
                if (config.docs.kotlindoc.replaceJavadoc) {
                    MavenArtifact javadocJar = mainPublication.artifacts?.find { it.classifier == 'javadoc' }
                    if (javadocJar) mainPublication.artifacts.remove(javadocJar)
                    if (config.docs.kotlindoc.outputFormats.indexOf(format) == 0) {
                        kotlindocJar.configure(new Action<Jar>() {
                            @Override
                            void execute(Jar t) {
                                t.archiveClassifier.set('javadoc')
                            }
                        })
                        project.tasks.findByName(JavadocPlugin.JAVADOC_TASK_NAME)?.enabled = false
                        project.tasks.findByName(JavadocPlugin.JAVADOC_JAR_TASK_NAME)?.enabled = false
                    }
                    mainPublication.artifact(kotlindocJar.get())
                }
            }

            registerJarVariant('Kotlindoc (' + format + ')', ' kotlindoc ', kotlindocJar, project)
        }

        kotlindocJar
    }

    private void createAggregateTasks(Project project) {
        ProjectConfigurationExtension config = resolveEffectiveConfig(project)

        config.docs.kotlindoc.outputFormats.each { String format ->
            String formatName = format == 'html-as-java' ? 'htmljava' : format
            String suffix = StringUtils.capitalize(formatName)
            String taskName = AGGREGATE_KOTLINDOC_BASENAME + suffix

            TaskProvider<DokkaTask> aggregateKotlindoc = project.tasks.register(taskName, DokkaTask,
                new Action<DokkaTask>() {
                    @Override
                    void execute(DokkaTask t) {
                        t.enabled = false
                        t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                        t.description = "Generates aggregate Kotlindoc API documentation in $format format"
                        t.dokkaRuntime = project.configurations.findByName(DOKKA_RUNTIME_CONFIGURATION_NAME)
                        t.extensions.add('multiplatform', project.container(GradlePassConfigurationImpl))
                        t.extensions.add('configuration', new GradlePassConfigurationImpl())
                        applyConfiguration(config.docs.kotlindoc, t, format, formatName)
                        t.outputDirectory = config.docs.kotlindoc.outputDirectory.absolutePath + File.separator + 'aggregate-' + formatName
                    }
                })

            project.tasks.register(taskName + 'Jar', Jar,
                new Action<Jar>() {
                    @Override
                    void execute(Jar t) {
                        t.enabled = false
                        t.dependsOn aggregateKotlindoc
                        t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                        t.description = "An archive of the aggregated $format formatted Kotlindoc API docs"
                        t.archiveClassifier.set 'kotlindoc' + '-' + formatName
                        t.from aggregateKotlindoc.get().outputDirectory
                    }
                })
        }
    }

    private Configuration configureDokkaRuntimeConfiguration(Project project) {
        Configuration dokkaRuntime = project.configurations.maybeCreate(DOKKA_RUNTIME_CONFIGURATION_NAME)
        dokkaRuntime.incoming.beforeResolve(new Action<ResolvableDependencies>() {
            @SuppressWarnings('UnusedMethodParameter')
            void execute(ResolvableDependencies resolvableDependencies) {
                DependencyHandler dependencyHandler = project.dependencies
                DependencySet dependencies = dokkaRuntime.dependencies
                dependencies.add(project.dependencies.create("org.jetbrains.dokka:dokka-fatjar:${DOKKA_VERSION.version}"))
            }
        })

        dokkaRuntime
    }

    @CompileDynamic
    static void applyConfiguration(Kotlindoc kotlindoc, DokkaTask task, String format, String formatName) {
        task.outputFormat = format
        task.outputDirectory = kotlindoc.outputDirectory.absolutePath + File.separator + formatName
        task.cacheRoot = kotlindoc.cacheRoot
        task.impliedPlatforms = new ArrayList<>(kotlindoc.impliedPlatforms)
        task.configuration.moduleName = kotlindoc.moduleName
        task.configuration.jdkVersion = kotlindoc.jdkVersion
        task.configuration.languageVersion = kotlindoc.languageVersion
        task.configuration.languageVersion = kotlindoc.languageVersion
        task.configuration.apiVersion = kotlindoc.apiVersion
        task.configuration.includeNonPublic = kotlindoc.includeNonPublic
        task.configuration.skipDeprecated = kotlindoc.skipDeprecated
        task.configuration.reportUndocumented = kotlindoc.reportUndocumented
        task.configuration.skipEmptyPackages = kotlindoc.skipEmptyPackages
        task.configuration.noStdlibLink = kotlindoc.noStdlibLink
        task.configuration.includes = new ArrayList<>(kotlindoc.includes)
        task.configuration.samples = new ArrayList<>(kotlindoc.samples)

        kotlindoc.sourceLinks.resolveSourceLinks().each { sourceLink ->
            task.configuration.sourceLinks {
                delegate.url = sourceLink.url
                delegate.path = sourceLink.path
                delegate.suffix = sourceLink.suffix
            }
        }

        kotlindoc.externalDocumentationLinks.resolveExternalDocumentationLinks().each { link ->
            task.configuration.externalDocumentationLink {
                delegate.url = link.url?.toURL()
                delegate.packageListUrl = link.packageListUrl?.toURL()
            }
        }

        kotlindoc.packageOptions.resolvePackageOptions().each { packageOption ->
            task.configuration.perPackageOption {
                delegate.prefix = packageOption.prefix
                delegate.includeNonPublic = packageOption.includeNonPublic
                delegate.reportUndocumented = packageOption.reportUndocumented
                delegate.skipDeprecated = packageOption.skipDeprecated
                delegate.suppress = packageOption.suppress
            }
        }
    }
}
