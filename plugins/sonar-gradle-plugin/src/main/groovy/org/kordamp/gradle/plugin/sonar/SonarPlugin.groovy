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
package org.kordamp.gradle.plugin.sonar

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.AppliedPlugin
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.sonarqube.gradle.SonarQubeExtension
import org.sonarqube.gradle.SonarQubePlugin
import org.sonarqube.gradle.SonarQubeProperties
import org.sonarqube.gradle.SonarQubeTask

import static org.kordamp.gradle.PluginUtils.resolveConfig
import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.StringUtils.isBlank
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * @author Andres Almiray
 * @since 0.32.0
 */
@CompileStatic
class SonarPlugin extends AbstractKordampPlugin {
    Project project

    SonarPlugin() {
        super(org.kordamp.gradle.plugin.base.plugins.Sonar.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        configureProject(project)
        if (isRootProject(project)) {
            configureRootProject(project)
            project.childProjects.values().each {
                it.pluginManager.apply(SonarPlugin)
            }
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(SonarPlugin)) {
            project.pluginManager.apply(SonarPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        project.pluginManager.apply(SonarQubePlugin)

        project.pluginManager.withPlugin('java-base', new Action<AppliedPlugin>() {
            @Override
            void execute(AppliedPlugin appliedPlugin) {
                project.afterEvaluate {
                    ProjectConfigurationExtension config = resolveEffectiveConfig(project)
                    setEnabled(config.quality.sonar.enabled)
                    config = resolveEffectiveConfig(project.rootProject)

                    SonarQubeExtension sonarExt = project.extensions.findByType(SonarQubeExtension)
                    sonarExt.skipProject = config.quality.sonar.excludedProjects.contains(project.name)
                }
            }
        })
    }

    private void configureRootProject(Project project) {
        project.gradle.addBuildListener(new BuildAdapter() {
            @Override
            void projectsEvaluated(Gradle gradle) {
                applySonarToRoot(resolveConfig(project), project)
            }
        })
    }

    @CompileDynamic
    private static void applySonarToRoot(ProjectConfigurationExtension config, Project project) {
        SonarQubeExtension sonarExt = project.extensions.findByType(SonarQubeExtension)
        sonarExt.properties(new Action<SonarQubeProperties>() {
            @Override
            void execute(SonarQubeProperties p) {
                config.quality.sonar.getConfigProperties().each { property, value ->
                    p.property(property, value)
                }
                p.property('sonar.host.url', config.quality.sonar.hostUrl)
                p.property('sonar.projectKey', config.quality.sonar.projectKey)
                p.property('sonar.exclusions', config.quality.sonar.excludes.join(','))
                if (config.coverage.jacoco.enabled) {
                    p.property('sonar.coverage.jacoco.xmlReportPaths', config.coverage.jacoco.aggregateReportXmlFile)
                }
                if (config.quality.detekt.enabled) {
                    p.property('sonar.kotlin.detekt.reportPaths', project.layout.buildDirectory.file('reports/detekt/aggregate.xml').get().asFile.absolutePath)
                }
                ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
                addIfUndefined('sonar.projectDescription', effectiveConfig.info.description, p)
                addIfUndefined('sonar.links.homepage', effectiveConfig.info.links.website, p)
                addIfUndefined('sonar.links.scm', effectiveConfig.info.scm.url, p)
                addIfUndefined('sonar.links.issue', effectiveConfig.info.issueManagement.url, p)
                addIfUndefined('sonar.links.ci', effectiveConfig.info.ciManagement.url, p)
            }

            private void addIfUndefined(final String sonarProperty, final String value, final SonarQubeProperties p) {
                final Map<String, Object> properties = p.getProperties()
                if (!properties.containsKey(sonarProperty) || isBlank(properties.get(sonarProperty) as String)) {
                    p.property(sonarProperty, value)
                }
            }
        })

        project.tasks.withType(SonarQubeTask, new Action<SonarQubeTask>() {
            @Override
            void execute(SonarQubeTask t) {
                t.setGroup('Quality')
                if (config.coverage.jacoco.enabled) {
                    t.dependsOn(project.tasks.named('aggregateJacocoReport'))
                }
                if (config.quality.detekt.enabled) {
                    t.dependsOn(project.tasks.named('aggregateDetekt'))
                }
            }
        })
    }
}
