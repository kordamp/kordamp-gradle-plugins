/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 the original author or authors.
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
package org.kordamp.gradle

import org.asciidoctor.gradle.AsciidoctorPlugin
import org.asciidoctor.gradle.AsciidoctorTask
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip
import org.kordamp.gradle.model.Information

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
class GuidePlugin implements Plugin<Project> {
    private static final String VISITED = GuidePlugin.class.name.replace('.', '_') + '_VISITED'

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
        project.plugins.apply(AsciidoctorPlugin)

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

        Information info = project.ext.mergedInfo

        asciidoctorTask.configure {
            attributes += [
                toc                    : 'left',
                doctype                : 'book',
                icons                  : 'font',
                encoding               : 'utf-8',
                sectlink               : true,
                sectanchors            : true,
                numbered               : true,
                linkattrs              : true,
                imagesdir              : 'images',
                linkcss                : true,
                'source-highlighter'   : 'coderay',
                'coderay-linenums-mode': 'table',
                'project-title'        : info.description,
                'project-author'       : info.resolveAuthors().join(', '),
                'project-url'          : info.url,
                'project-scm'          : info.links.scm,
                'project-issue-tracker': info.links.issueTracker,
                'project-group'        : project.group,
                'project-version'      : project.version,
                'project-name'         : project.rootProject.name
            ]

            sources {
                include 'index.adoc'
            }

            resources {
                from project.file('src/docs/resources')
            }
        }
    }

    private void createGuideTaskIfNeeded(Project project) {
        Task guideTask = project.tasks.findByName('guide')

        if (!guideTask) {
            guideTask = project.tasks.create('guide', Copy) {
                group 'Documentation'
                description 'Creates an Asciidoctor based guide.'
            }
        }

        guideTask.configure {
            dependsOn project.tasks.findByName(AsciidoctorPlugin.ASCIIDOCTOR)
            destinationDir project.file("${project.buildDir}/guide")
            from("${project.tasks.asciidoctor.outputDir}/html5")
        }

        Task guideZipTask = project.tasks.findByName('guideZip')
        if (!guideZipTask) {
            guideTask = project.tasks.create('guideZip', Zip) {
                dependsOn guideTask
                group 'Documentation'
                description 'An archive of the generated guide.'
                baseName = project.rootProject.name + '-guide'
                from guideTask.destinationDir
            }
        }

        project.rootProject.tasks.whenTaskAdded { Task task ->
            if (task.name != ApidocPlugin.AGGREGATE_APIDOCS_TASK_NAME) {
                return
            }

            guideTask.configure {
                dependsOn task
                from(task.destinationDir) { into 'api' }
            }
        }
    }

    private void createInitGuideTask(Project project) {
        project.tasks.create('initGuide', DefaultTask) {
            group 'Project Setup'
            description 'Initializes directories and files required by the guide.'
            outputs.dir('src/docs/asciidoc')
            outputs.dir('src/docs/resources')
            doFirst {
                GuidePlugin.initGuide(project)
            }
        }
    }

    static void initGuide(Project project) {
        project.file('src/docs/resources').mkdirs()
        File asciidocDir = project.file('src/docs/asciidoc')
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
                            |link:api/index.html[Javadoc, window="_blank"]""".stripMargin('|')
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
