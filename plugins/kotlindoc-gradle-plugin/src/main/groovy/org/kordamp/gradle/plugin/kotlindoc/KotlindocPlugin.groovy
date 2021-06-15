/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2021 Andres Almiray.
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
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenArtifact
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.dokka.gradle.DokkaCollectorTask
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.dokka.gradle.DokkaPlugin
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.GradleDokkaSourceSetBuilder
import org.jetbrains.dokka.gradle.GradleExternalDocumentationLinkBuilder
import org.jetbrains.dokka.gradle.GradlePackageOptionsBuilder
import org.jetbrains.dokka.gradle.GradleSourceLinkBuilder
import org.kordamp.gradle.annotations.DependsOn
import org.kordamp.gradle.listener.AllProjectsEvaluatedListener
import org.kordamp.gradle.listener.ProjectEvaluatedListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.Kotlindoc
import org.kordamp.gradle.plugin.javadoc.JavadocPlugin

import javax.inject.Named

import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addAllProjectsEvaluatedListener
import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addProjectEvaluatedListener
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject
import static org.kordamp.gradle.util.PluginUtils.registerJarVariant
import static org.kordamp.gradle.util.PluginUtils.resolveConfig

/**
 * Configures {@code kotlindoc} and {@code kotlindocJar} tasks.
 *
 * @author Andres Almiray
 * @since 0.7.0
 */
@CompileStatic
class KotlindocPlugin extends AbstractKordampPlugin {
    Project project

    KotlindocPlugin() {
        super(Kotlindoc.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

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
        addAllProjectsEvaluatedListener(project, new KotlindocAllProjectsEvaluatedListener())
    }

    private void doConfigureRootProject(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)
        setEnabled(config.docs.kotlindoc.aggregate.enabled)

        project.tasks.withType(DokkaCollectorTask) { DokkaCollectorTask task ->
            task.setEnabled(config.docs.kotlindoc.aggregate.enabled)
        }

        updatePublications(project)
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        project.pluginManager.apply(DokkaPlugin)

        project.pluginManager.withPlugin('org.jetbrains.kotlin.jvm') {
            addProjectEvaluatedListener(project, new KotlindocProjectEvaluatedListener())
        }
    }

    @Named('kotlindoc')
    @DependsOn(['javadoc'])
    private class KotlindocProjectEvaluatedListener implements ProjectEvaluatedListener {
        @Override
        void projectEvaluated(Project project) {
            ProjectConfigurationExtension config = resolveConfig(project)
            setEnabled(config.docs.kotlindoc.enabled)

            if (!config.docs.kotlindoc.enabled) {
                return
            }

            List<DokkaTask> tasks = []
            project.tasks.withType(DokkaTask).each { DokkaTask dokkaTask ->
                tasks << dokkaTask
            }

            tasks.each { DokkaTask dokkaTask ->
                dokkaTask.setEnabled(config.docs.kotlindoc.enabled)
                configureDokkaTask(config, dokkaTask)
                TaskProvider<Jar> kotlindocJar = createKotlindocJarTask(project, dokkaTask)
                project.tasks.findByName(org.gradle.api.plugins.BasePlugin.ASSEMBLE_TASK_NAME).dependsOn(kotlindocJar)
            }

            project.tasks.withType(DokkaMultiModuleTask) { DokkaMultiModuleTask task ->
                task.setEnabled(config.docs.kotlindoc.enabled)
            }
        }
    }

    private void configureDokkaTask(ProjectConfigurationExtension config, DokkaTask dokkaTask) {
        Kotlindoc kotlindoc = config.docs.kotlindoc
        if (kotlindoc.outputDirectory) dokkaTask.outputDirectory.set(kotlindoc.outputDirectory)
        dokkaTask.dokkaSourceSets.configureEach(new Action<GradleDokkaSourceSetBuilder>() {
            @Override
            void execute(GradleDokkaSourceSetBuilder ss) {
                ss.displayName.set(kotlindoc.displayName ?: config.project.displayName)
                if (kotlindoc.includes) ss.includes.from(kotlindoc.includes)
                if (kotlindoc.samples) ss.samples.from(kotlindoc.samples)
                if (kotlindoc.jdkVersion) ss.jdkVersion.set(kotlindoc.jdkVersion)
                if (kotlindoc.includeNonPublic) ss.includeNonPublic.set(kotlindoc.includeNonPublic)
                if (kotlindoc.skipDeprecated) ss.skipDeprecated.set(kotlindoc.skipDeprecated)
                if (kotlindoc.reportUndocumented) ss.reportUndocumented.set(kotlindoc.reportUndocumented)
                if (kotlindoc.skipEmptyPackages) ss.skipEmptyPackages.set(kotlindoc.skipEmptyPackages)
                if (kotlindoc.noStdlibLink) ss.noStdlibLink.set(kotlindoc.noStdlibLink)
                if (kotlindoc.noJdkLink) ss.noJdkLink.set(kotlindoc.noJdkLink)
                ss.classpath.from(ss.classpath, config.project.configurations.findByName('optional'))

                kotlindoc.sourceLinks.resolveSourceLinks().each { sourceLink ->
                    ss.sourceLink(new Action<GradleSourceLinkBuilder>() {
                        @Override
                        void execute(GradleSourceLinkBuilder l) {
                            if (sourceLink.remoteUrl) l.remoteUrl.set(sourceLink.remoteUrl.toURL())
                            if (sourceLink.localDirectory) l.localDirectory.set(dokkaTask.project.file(sourceLink.localDirectory))
                            if (sourceLink.remoteLineSuffix) l.remoteLineSuffix.set(sourceLink.remoteLineSuffix)
                        }
                    })
                }

                kotlindoc.externalDocumentationLinks.resolveExternalDocumentationLinks().each { link ->
                    ss.externalDocumentationLink(new Action<GradleExternalDocumentationLinkBuilder>() {
                        @Override
                        void execute(GradleExternalDocumentationLinkBuilder l) {
                            if (link.url) l.url.set(link.url.toURL())
                            if (link.packageListUrl) l.packageListUrl.set(link.packageListUrl.toURL())
                        }
                    })
                }

                kotlindoc.packageOptions.resolvePackageOptions().each { packageOption ->
                    ss.perPackageOption(new Action<GradlePackageOptionsBuilder>() {
                        @Override
                        void execute(GradlePackageOptionsBuilder p) {
                            if (packageOption.prefix) p.matchingRegex.set(packageOption.prefix)
                            p.includeNonPublic.set(packageOption.includeNonPublic)
                            p.reportUndocumented.set(packageOption.reportUndocumented)
                            p.skipDeprecated.set(packageOption.skipDeprecated)
                            p.suppress.set(packageOption.suppress)
                        }
                    })
                }
            }
        })
    }

    @Named('kotlindoc')
    @DependsOn(['javadoc'])
    private class KotlindocAllProjectsEvaluatedListener implements AllProjectsEvaluatedListener {
        @Override
        void allProjectsEvaluated(Project rootProject) {
            createAggregateTasks(rootProject)
            doConfigureRootProject(rootProject)
        }
    }

    private TaskProvider<Jar> createKotlindocJarTask(Project project, DokkaTask dokkaTask) {
        String classifier = (dokkaTask.name - 'dokka').toLowerCase()
        classifier = classifier != 'javadoc' ? classifier : 'kotlindoc'

        ProjectConfigurationExtension config = resolveConfig(project)

        project.tasks.register(dokkaTask.name + 'Jar', Jar,
            new Action<Jar>() {
                @Override
                void execute(Jar t) {
                    t.enabled = config.docs.kotlindoc.enabled
                    t.dependsOn dokkaTask
                    t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                    t.description = "An archive of the ${dokkaTask.path} docs"
                    t.archiveClassifier.set(classifier)
                    t.from dokkaTask.outputDirectory
                    t.onlyIf { dokkaTask.enabled }
                }
            })
    }

    private void updatePublications(Project project) {
        updatePublication(project)
        for (Project p : project.childProjects.values()) {
            updatePublications(p)
        }
    }

    @CompileDynamic
    private void updatePublication(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)
        if (!config.docs.kotlindoc.enabled) {
            return
        }

        project.tasks.withType(DokkaTask).each { DokkaTask dokkaTask ->
            String taskName = dokkaTask.name + 'Jar'
            if (project.tasks.findByName(taskName)) {
                TaskProvider<Jar> kotlindocJar = project.tasks.named(taskName, Jar)
                if (config.docs.kotlindoc.enabled && project.pluginManager.hasPlugin('maven-publish')) {
                    PublishingExtension publishing = project.extensions.findByType(PublishingExtension)
                    MavenPublication mainPublication = (MavenPublication) publishing.publications.findByName('main')
                    if (mainPublication) {
                        if (config.docs.kotlindoc.replaceJavadoc) {
                            MavenArtifact javadocJar = mainPublication.artifacts?.find { it.classifier == 'javadoc' }
                            if (javadocJar) mainPublication.artifacts.remove(javadocJar)
                            kotlindocJar.configure(new Action<Jar>() {
                                @Override
                                void execute(Jar t) {
                                    t.archiveClassifier.set('javadoc')
                                }
                            })
                            project.tasks.findByName(JavadocPlugin.JAVADOC_TASK_NAME)?.enabled = false
                            project.tasks.findByName(JavadocPlugin.JAVADOC_JAR_TASK_NAME)?.enabled = false

                            mainPublication.artifact(kotlindocJar.get())
                            project.artifacts { archives(kotlindocJar.get()) }
                        }
                    }

                    String classifier = kotlindocJar.get().archiveClassifier.get()
                    registerJarVariant('Kotlindoc (' + classifier + ')', " $classifier ".toString(), kotlindocJar, project)
                }
            }
        }
    }

    private void createAggregateTasks(Project project) {
        List<DokkaCollectorTask> tasks = []

        project.tasks.withType(DokkaCollectorTask).each { DokkaCollectorTask collectorTask ->
            tasks << collectorTask
        }

        tasks.each { DokkaCollectorTask collectorTask ->
            String classifier = (collectorTask.name - 'dokka' - 'Collector').toLowerCase()
            classifier = classifier != 'javadoc' ? classifier : 'kotlindoc'


            project.tasks.register(collectorTask.name + 'Jar', Jar,
                new Action<Jar>() {
                    @Override
                    void execute(Jar t) {
                        t.enabled = false
                        t.dependsOn collectorTask
                        t.group = JavaBasePlugin.DOCUMENTATION_GROUP
                        t.description = "An archive of the aggregated ${collectorTask.name} docs"
                        t.archiveClassifier.set classifier
                        t.from collectorTask.outputDirectory
                    }
                })
        }
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
        task.configuration.apiVersion = kotlindoc.apiVersion
        task.configuration.includeNonPublic = kotlindoc.includeNonPublic
        task.configuration.skipDeprecated = kotlindoc.skipDeprecated
        task.configuration.reportUndocumented = kotlindoc.reportUndocumented
        task.configuration.skipEmptyPackages = kotlindoc.skipEmptyPackages
        task.configuration.noStdlibLink = kotlindoc.noStdlibLink
        task.configuration.includes = new ArrayList<>(kotlindoc.includes)
        task.configuration.samples = new ArrayList<>(kotlindoc.samples)

        kotlindoc.sourceLinks.resolveSourceLinks().each { sourceLink ->
            task.configuration.sourceLink {
                delegate.url = sourceLink.remoteUrl
                delegate.path = sourceLink.localDirectory
                delegate.lineSuffix = sourceLink.remoteLineSuffix
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
