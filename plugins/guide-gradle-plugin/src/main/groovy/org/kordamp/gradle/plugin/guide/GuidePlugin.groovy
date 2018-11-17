/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 Andres Almiray.
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
package org.kordamp.gradle.plugin.guide

import org.asciidoctor.gradle.AsciidoctorPlugin
import org.asciidoctor.gradle.AsciidoctorTask
import org.gradle.BuildAdapter
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip
import org.kordamp.gradle.plugin.apidoc.ApidocPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.sourcehtml.SourceHtmlPlugin

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
class GuidePlugin implements Plugin<Project> {
    private static final String VISITED = GuidePlugin.class.name.replace('.', '_') + '_VISITED'

    static final String CREATE_GUIDE_TASK_NAME = 'createGuide'
    static final String INIT_GUIDE_TASK_NAME = 'initGuide'
    static final String ZIP_GUIDE_TASK_NAME = 'zipGuide'
    static final String ASCIIDOCTOR_SRC_DIR = 'src/docs/asciidoc'
    static final String ASCIIDOCTOR_RESOURCE_DIR = 'src/docs/resources'

    Project project

    void apply(Project project) {
        this.project = project

        String visitedPropertyName = VISITED + '_' + project.name
        if (project.findProperty(visitedPropertyName)) {
            return
        }
        project.ext[visitedPropertyName] = true

        BasePlugin.applyIfMissing(project)
        ApidocPlugin.applyIfMissing(project.rootProject)
        SourceHtmlPlugin.applyIfMissing(project.rootProject)
        project.plugins.apply(AsciidoctorPlugin)

        project.extensions.create('guide', GuideExtension, project)

        project.afterEvaluate {
            configureAsciidoctorTask(project)
            createGuideTaskIfNeeded(project)
            createInitGuideTask(project)
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(GuidePlugin)) {
            project.plugins.apply(GuidePlugin)
        }
    }

    private void configureAsciidoctorTask(Project project) {
        AsciidoctorTask asciidoctorTask = project.tasks.findByName(AsciidoctorPlugin.ASCIIDOCTOR)

        ProjectConfigurationExtension mergedConfiguration = project.ext.mergedConfiguration

        Map attrs = [:]
        checkAttribute(attrs, asciidoctorTask.attributes, 'toc', 'left')
        checkAttribute(attrs, asciidoctorTask.attributes, 'doctype', 'book')
        checkAttribute(attrs, asciidoctorTask.attributes, 'icons', 'font')
        checkAttribute(attrs, asciidoctorTask.attributes, 'encoding', 'utf-8')
        checkAttribute(attrs, asciidoctorTask.attributes, 'sectlink', true)
        checkAttribute(attrs, asciidoctorTask.attributes, 'sectanchors', true)
        checkAttribute(attrs, asciidoctorTask.attributes, 'numbered', true)
        checkAttribute(attrs, asciidoctorTask.attributes, 'linkattrs', true)
        checkAttribute(attrs, asciidoctorTask.attributes, 'imagesdir', 'images')
        checkAttribute(attrs, asciidoctorTask.attributes, 'linkcss', true)
        checkAttribute(attrs, asciidoctorTask.attributes, 'source-highlighter', 'coderay')
        checkAttribute(attrs, asciidoctorTask.attributes, 'coderay-linenums-mode', 'table')
        checkAttribute(attrs, asciidoctorTask.attributes, 'project-title', mergedConfiguration.info.description)
        checkAttribute(attrs, asciidoctorTask.attributes, 'project-inception-year', mergedConfiguration.info.inceptionYear)
        checkAttribute(attrs, asciidoctorTask.attributes, 'project-copyright-year', mergedConfiguration.info.copyrightYear)
        checkAttribute(attrs, asciidoctorTask.attributes, 'project-author', mergedConfiguration.info.getAuthors().join(', '))
        checkAttribute(attrs, asciidoctorTask.attributes, 'project-url', mergedConfiguration.info.url)
        checkAttribute(attrs, asciidoctorTask.attributes, 'project-scm', mergedConfiguration.info.links.scm)
        checkAttribute(attrs, asciidoctorTask.attributes, 'project-issue-tracker', mergedConfiguration.info.links.issueTracker)
        checkAttribute(attrs, asciidoctorTask.attributes, 'project-group', project.group)
        checkAttribute(attrs, asciidoctorTask.attributes, 'project-version', project.version)
        checkAttribute(attrs, asciidoctorTask.attributes, 'project-name', project.rootProject.name)

        Map buildinfo = project.rootProject.findProperty('buildinfo') ?: [:]
        checkAttribute(attrs, asciidoctorTask.attributes, 'build-by', buildinfo.buildBy)
        checkAttribute(attrs, asciidoctorTask.attributes, 'build-date', buildinfo.buildDate)
        checkAttribute(attrs, asciidoctorTask.attributes, 'build-time', buildinfo.buildTime)
        checkAttribute(attrs, asciidoctorTask.attributes, 'build-revision', buildinfo.buildRevision)
        checkAttribute(attrs, asciidoctorTask.attributes, 'build-jdk', buildinfo.buildJdk)
        checkAttribute(attrs, asciidoctorTask.attributes, 'build-created-by', buildinfo.buildCreatedBy)

        asciidoctorTask.configure {
            attributes.putAll(attrs)

            sources {
                include 'index.adoc'
            }

            resources {
                from project.file(ASCIIDOCTOR_RESOURCE_DIR)
            }
        }
    }

    private static void checkAttribute(Map dest, Map src, String key, value) {
        if(!src.containsKey(key)) dest[key] = value
    }

    private void createGuideTaskIfNeeded(Project project) {
        Task guideTask = project.tasks.findByName(CREATE_GUIDE_TASK_NAME)

        if (!guideTask) {
            guideTask = project.tasks.create(CREATE_GUIDE_TASK_NAME, Copy) {
                group 'Documentation'
                description 'Creates an Asciidoctor based guide.'
            }
        }

        guideTask.configure {
            dependsOn project.tasks.findByName(AsciidoctorPlugin.ASCIIDOCTOR)
            destinationDir project.file("${project.buildDir}/guide")
            from("${project.tasks.asciidoctor.outputDir}/html5")
        }

        Task zipGuideTask = project.tasks.findByName(ZIP_GUIDE_TASK_NAME)
        if (!zipGuideTask) {
            zipGuideTask = project.tasks.create(ZIP_GUIDE_TASK_NAME, Zip) {
                dependsOn guideTask
                group 'Documentation'
                description 'An archive of the generated guide.'
                baseName = project.rootProject.name + '-guide'
                from guideTask.destinationDir
            }
        }

        project.rootProject.gradle.addBuildListener(new BuildAdapter() {
            @Override
            void projectsEvaluated(Gradle gradle) {
                GuideExtension extension = project.extensions.findByType(GuideExtension)

                Task task = project.rootProject.tasks.findByName(ApidocPlugin.AGGREGATE_JAVADOCS_TASK_NAME)
                if (task?.enabled) {
                    guideTask.configure {
                        dependsOn task
                        from(task.destinationDir) { into extension.javadocApiDir }
                    }
                }

                task = project.rootProject.tasks.findByName(ApidocPlugin.AGGREGATE_GROOVYDOCS_TASK_NAME)
                if (task?.enabled) {
                    guideTask.configure {
                        dependsOn task
                        from(task.destinationDir) { into extension.groovydocApiDir }
                    }
                }

                task = project.rootProject.tasks.findByName(SourceHtmlPlugin.AGGREGATE_SOURCE_HTML_TASK_NAME)
                if (task?.enabled) {
                    guideTask.configure {
                        dependsOn project.rootProject.tasks.findByName(SourceHtmlPlugin.AGGREGATE_SOURCE_HTML_TASK_NAME)
                        from(task.destinationDir) { into extension.sourceHtmlDir }
                    }
                }
            }
        })
    }

    private void createInitGuideTask(Project project) {
        project.tasks.create(INIT_GUIDE_TASK_NAME, DefaultTask) {
            group 'Project Setup'
            description 'Initializes directories and files required by the guide.'
            outputs.dir(ASCIIDOCTOR_SRC_DIR)
            outputs.dir(ASCIIDOCTOR_RESOURCE_DIR)
            doFirst {
                GuidePlugin.initGuide(project)
            }
        }
    }

    static void initGuide(Project project) {
        GuideExtension extension = project.extensions.findByType(GuideExtension)

        project.file(ASCIIDOCTOR_RESOURCE_DIR).mkdirs()
        File asciidocDir = project.file(ASCIIDOCTOR_SRC_DIR)
        asciidocDir.mkdirs()

        touchFile(project.file("${asciidocDir}/_links.adoc"))

        File index = touchFile(project.file("${asciidocDir}/index.adoc"))
        if (!index.text) {
            index.text = """|= {project-title}
                            |:author: {project-author}
                            |:revnumber: {project-version}
                            |:toclevels: 4
                            |:docinfo1:
                            |
                            |include::_links.adoc[]
                            |
                            |:leveloffset: 1
                            |include::introduction.adoc[]
                            |include::usage.adoc[]
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
