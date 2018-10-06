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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc

import static org.kordamp.gradle.BasePlugin.isRootProject

/**
 * Configures a {@code javadocJar} for each {@code SourceSet}.
 * <strong>NOTE:</strong> any sources with the word "test" will be skipped.
 * Applies the {@code maven-publish} plugin if it has not been applied before.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
class ApidocPlugin implements Plugin<Project> {
    static final String AGGREGATE_APIDOCS_TASK_NAME = 'aggregateApidocs'
    static final String AGGREGATE_APIDOCS_JAR_TASK_NAME = 'aggregateApidocsJar'

    private static final String VISITED = ApidocPlugin.class.name.replace('.', '_') + '_VISITED'

    Project project

    void apply(Project project) {
        this.project = project

        if (isRootProject(project)) {
            applyPlugins(project)
            project.childProjects.values().each { prj ->
                applyPlugins(prj)
            }
        } else {
            applyPlugins(project)
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(ApidocPlugin)) {
            project.plugins.apply(ApidocPlugin)
        }
    }

    private void applyPlugins(Project project) {
        String visitedPropertyName = VISITED + '_' + project.name
        if (project.findProperty(visitedPropertyName)) {
            return
        }
        project.ext[visitedPropertyName] = true

        BasePlugin.applyIfMissing(project)
        JavadocPlugin.applyIfMissing(project)
        // GroovydocPlugin.applyIfMissing(project)

        project.afterEvaluate { Project prj ->
            ProjectConfigurationExtension mergedConfiguration = prj.rootProject.ext.mergedConfiguration

            if (!mergedConfiguration.apidoc.enabled) {
                return
            }

            if (isRootProject(project) && !project.childProjects.isEmpty()) {
                project.evaluationDependsOnChildren()

                List<Javadoc> javadocs = []

                project.tasks.withType(Javadoc) { Javadoc javadoc -> if (javadoc.name != AGGREGATE_APIDOCS_TASK_NAME) javadocs << javadoc }

                project.childProjects.values().each { Project p ->
                    p.tasks.withType(Javadoc) { Javadoc javadoc -> javadocs << javadoc }
                }

                javadocs = javadocs.unique()

                List<Task> aggregateTasks = createAggregateJavadocsTask(project)

                if (javadocs) {
                    aggregateTasks[0].configure {
                        enabled true
                        dependsOn javadocs
                        source javadocs.source
                        classpath = project.files(javadocs.classpath)
                    }
                    aggregateTasks[1].configure {
                        enabled true
                        from aggregateTasks[0].destinationDir
                    }
                }
            }
        }
    }

    private List<Task> createAggregateJavadocsTask(Project project) {
        Task aggregateJavadocs = project.tasks.create(AGGREGATE_APIDOCS_TASK_NAME, Javadoc) {
            enabled false
            group JavaBasePlugin.DOCUMENTATION_GROUP
            description 'Aggregates API docs for all projects.'
            destinationDir project.file("${project.buildDir}/docs/javadoc")
        }

        Task aggregateJavadocsJar = project.tasks.create(AGGREGATE_APIDOCS_JAR_TASK_NAME, Jar) {
            enabled false
            dependsOn aggregateJavadocs
            group JavaBasePlugin.DOCUMENTATION_GROUP
            description "An archive of the aggregate API docs"
            classifier 'javadoc'
        }

        [aggregateJavadocs, aggregateJavadocsJar]
    }
}
