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
package org.kordamp.gradle.plugin.guide

import groovy.transform.CompileDynamic
import org.asciidoctor.gradle.jvm.AsciidoctorJPlugin
import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip
import org.kordamp.gradle.annotations.DependsOn
import org.kordamp.gradle.listener.AllProjectsEvaluatedListener
import org.kordamp.gradle.listener.ProjectEvaluatedListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.Guide
import org.kordamp.gradle.plugin.sourcehtml.SourceHtmlPlugin

import javax.inject.Named

import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addAllProjectsEvaluatedListener
import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addProjectEvaluatedListener
import static org.kordamp.gradle.util.PluginUtils.resolveConfig

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
class GuidePlugin extends AbstractKordampPlugin {
    static final String CREATE_GUIDE_TASK_NAME = 'createGuide'
    static final String INIT_GUIDE_TASK_NAME = 'initGuide'
    static final String ZIP_GUIDE_TASK_NAME = 'zipGuide'
    static final String ASCIIDOCTOR = 'asciidoctor'
    static final String ASCIIDOCTOR_SRC_DIR = 'src/docs/asciidoc'
    static final String ASCIIDOCTOR_RESOURCE_DIR = 'src/docs/resources'
    static final String IMAGES_DIR = 'images'

    Project project

    GuidePlugin() {
        super(Guide.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        SourceHtmlPlugin.applyIfMissing(project.rootProject)
        project.pluginManager.apply(AsciidoctorJPlugin)

        project.extensions.create('guide', GuideExtension, project)

        addProjectEvaluatedListener(project, new GuideProjectEvaluatedListener())
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(GuidePlugin)) {
            project.pluginManager.apply(GuidePlugin)
        }
    }

    @Named('guide')
    @DependsOn(['base'])
    private class GuideProjectEvaluatedListener implements ProjectEvaluatedListener {
        @Override
        void projectEvaluated(Project project) {
            configureAsciidoctorTask(project)
            createGuideTask(project)
            createInitGuideTask(project)
            configurePublishing(project)
        }
    }

    @Named('guide')
    @DependsOn(['base', 'javadoc', 'groovydoc', 'kotlindoc', 'sourceHtml', 'sourceXref'])
    private class GuideAllProjectsEvaluatedListener implements AllProjectsEvaluatedListener {
        @Override
        void allProjectsEvaluated(Project rootProject) {
            GuideExtension extension = project.extensions.findByType(GuideExtension)

            project.tasks.named(CREATE_GUIDE_TASK_NAME, Copy, new Action<Copy>() {
                @Override
                void execute(Copy t) {
                    Task task = rootProject.tasks.findByName('aggregateJavadoc')
                    if (task?.enabled) {
                        t.dependsOn task
                        t.from(task.destinationDir) { into extension.javadocApiDir }
                    }

                    task = rootProject.tasks.findByName('aggregateGroovydoc')
                    if (task?.enabled) {
                        t.dependsOn task
                        t.from(task.destinationDir) { into extension.groovydocApiDir }
                    }

                    task = rootProject.tasks.findByName('dokkaHtmlCollector')
                    if (task?.enabled) {
                        t.dependsOn task
                        t.from(task.outputDirectory) { into extension.kotlindocApiDir }
                    }

                    task = rootProject.tasks.findByName(SourceHtmlPlugin.AGGREGATE_SOURCE_HTML_TASK_NAME)
                    if (task?.enabled) {
                        t.dependsOn task
                        t.from(task.destinationDir) { into extension.sourceHtmlDir }
                    }

                    task = rootProject.tasks.findByName('aggregateSourceXref')
                    if (task?.enabled) {
                        t.dependsOn task
                        t.from(task.outputDirectory) { into extension.sourceXrefDir }
                    }
                }
            })
        }
    }

    private void configureAsciidoctorTask(Project project) {
        project.tasks.named(ASCIIDOCTOR).configure(new Action<AsciidoctorTask>() {
            @Override
            void execute(AsciidoctorTask t) {
                ProjectConfigurationExtension config = resolveConfig(project.rootProject) ?: resolveConfig(project)

                Map attrs = [:]
                attrs.putAll(t.attributes)
                checkAttribute(attrs, t.attributes, 'toc', 'left')
                checkAttribute(attrs, t.attributes, 'doctype', 'book')
                checkAttribute(attrs, t.attributes, 'icons', 'font')
                checkAttribute(attrs, t.attributes, 'encoding', 'utf-8')
                checkAttribute(attrs, t.attributes, 'sectlink', true)
                checkAttribute(attrs, t.attributes, 'sectanchors', true)
                checkAttribute(attrs, t.attributes, 'numbered', true)
                checkAttribute(attrs, t.attributes, 'linkattrs', true)
                checkAttribute(attrs, t.attributes, 'imagesdir', IMAGES_DIR)
                checkAttribute(attrs, t.attributes, 'linkcss', true)
                checkAttribute(attrs, t.attributes, 'source-highlighter', 'coderay')
                checkAttribute(attrs, t.attributes, 'coderay-linenums-mode', 'table')
                checkAttribute(attrs, t.attributes, 'rootdir', project.rootProject.projectDir.absolutePath)
                checkAttribute(attrs, t.attributes, 'project-title', config.info.description)
                checkAttribute(attrs, t.attributes, 'project-inception-year', config.info.inceptionYear)
                checkAttribute(attrs, t.attributes, 'project-copyright-year', config.info.copyrightYear)
                checkAttribute(attrs, t.attributes, 'project-author', config.info.getAuthors().join(', '))
                checkAttribute(attrs, t.attributes, 'project-url', config.info.url)
                checkAttribute(attrs, t.attributes, 'project-scm', config.info.links.scm)
                checkAttribute(attrs, t.attributes, 'project-issue-tracker', config.info.links.issueTracker)
                checkAttribute(attrs, t.attributes, 'project-group', project.group)
                checkAttribute(attrs, t.attributes, 'project-version', project.version)
                checkAttribute(attrs, t.attributes, 'project-name', project.rootProject.name)

                checkAttribute(attrs, t.attributes, 'build-by', config.buildInfo.buildBy)
                checkAttribute(attrs, t.attributes, 'build-date', config.buildInfo.buildDate)
                checkAttribute(attrs, t.attributes, 'build-time', config.buildInfo.buildTime)
                checkAttribute(attrs, t.attributes, 'build-revision', config.buildInfo.buildRevision)
                checkAttribute(attrs, t.attributes, 'build-jdk', config.buildInfo.buildJdk)
                checkAttribute(attrs, t.attributes, 'build-created-by', config.buildInfo.buildCreatedBy)

                t.attributes(attrs)

                t.sourceDir = project.file(ASCIIDOCTOR_SRC_DIR)

                t.sources {
                    include 'index.adoc'
                }

                t.resources {
                    from project.file(ASCIIDOCTOR_RESOURCE_DIR)
                }
            }
        })
    }

    private static void checkAttribute(Map dest, Map src, String key, value) {
        if (!src.containsKey(key)) dest[key + '@'] = value
    }

    private void createGuideTask(Project project) {
        TaskProvider<Copy> guideTask = project.tasks.register(CREATE_GUIDE_TASK_NAME, Copy,
            new Action<Copy>() {
                @Override
                void execute(Copy t) {
                    t.group = 'Documentation'
                    t.description = 'Creates an Asciidoctor based guide.'
                    t.dependsOn project.tasks.named(ASCIIDOCTOR)
                    t.setDestinationDir(project.file("${project.buildDir}/guide"))
                    t.from(project.tasks.asciidoctor.outputDir)
                }
            })

        project.tasks.register(ZIP_GUIDE_TASK_NAME, Zip,
            new Action<Zip>() {
                @Override
                void execute(Zip t) {
                    t.dependsOn guideTask
                    t.group = 'Documentation'
                    t.description = 'An archive of the generated guide.'
                    t.setArchiveBaseName(project.rootProject.name + '-guide')
                    t.from guideTask.get().destinationDir
                }
            })

        addAllProjectsEvaluatedListener(project.rootProject,new GuideAllProjectsEvaluatedListener())
    }

    private void createInitGuideTask(Project project) {
        project.tasks.register(INIT_GUIDE_TASK_NAME, DefaultTask, new Action<DefaultTask>() {
            @Override
            void execute(DefaultTask t) {
                t.group = 'Project Setup'
                t.description = 'Initializes directories and files required by the guide.'
                t.outputs.dir(ASCIIDOCTOR_SRC_DIR)
                t.outputs.dir(ASCIIDOCTOR_RESOURCE_DIR)
                t.doFirst {
                    GuidePlugin.initGuide(project)
                }
            }
        })
    }

    @CompileDynamic
    private void configurePublishing(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)
        if (!config.docs.guide.publish.enabled) {
            return
        }

        Task createGuideTask = project.tasks.findByName(CREATE_GUIDE_TASK_NAME)

        project.gitPublish {
            repoUri = config.info.resolveScmLink()
            branch = config.docs.guide.publish.branch
            contents {
                from createGuideTask.outputs.files
            }
            commitMessage = config.docs.guide.publish.message
        }

        project.gitPublishCommit.dependsOn(createGuideTask)
    }

    static void initGuide(Project project) {
        GuideExtension extension = project.extensions.findByType(GuideExtension)

        project.file(ASCIIDOCTOR_RESOURCE_DIR).mkdirs()
        project.file("$ASCIIDOCTOR_RESOURCE_DIR/$IMAGES_DIR").mkdirs()
        File asciidocDir = project.file(ASCIIDOCTOR_SRC_DIR)
        asciidocDir.mkdirs()

        touchFile(project.file("${asciidocDir}/_links.adoc"))

        File index = touchFile(project.file("${asciidocDir}/index.adoc"))
        if (!index.text) {
            index.text = """|ifndef::imagesdir[]
                            |:imagesdir: ../resources/$IMAGES_DIR
                            |endif::[]
                            |ifndef::includedir[]
                            |:includedir: .
                            |endif::[]
                            |= {project-title}
                            |:author: {project-author}
                            |:revnumber: {project-version}
                            |:toclevels: 4
                            |:docinfo1:
                            |
                            |include::{includedir}/_links.adoc[]
                            |
                            |:leveloffset: 1
                            |include::{includedir}/introduction.adoc[]
                            |include::{includedir}/usage.adoc[]
                            |
                            |= Links
                            |
                            |link:${extension.javadocApiDir}/index.html[Javadoc, window="_blank"]
                            |
                            |link:${extension.sourceHtmlDir}/index.html[Source, window="_blank"]""".stripMargin('|')
        }

        File introduction = touchFile(project.file("${asciidocDir}/introduction.adoc"))
        if (!introduction.text) {
            introduction.text = """|
                                   |[[_introduction]]
                                   |= Introduction
                                   |
                                   |Lorem ipsum dolor sit amet
                                   |""".stripMargin('|')
        }

        File usage = touchFile(project.file("${asciidocDir}/usage.adoc"))
        if (!usage.text) {
            usage.text = """|
                                   |[[_usage]]
                                   |= Usage
                                   |
                                   |Lorem ipsum dolor sit amet
                                   |""".stripMargin('|')
        }
    }

    static File touchFile(File file) {
        file.parentFile.mkdirs()
        if (!file.exists()) {
            file.text = ''
        }
        file
    }
}
