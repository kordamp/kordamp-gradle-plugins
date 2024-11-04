/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2024 Andres Almiray.
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

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.AppliedPlugin
import org.kordamp.gradle.annotations.DependsOn
import org.kordamp.gradle.listener.AllProjectsEvaluatedListener
import org.kordamp.gradle.listener.ProjectEvaluatedListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.Sonar
import org.sonarqube.gradle.SonarExtension
import org.sonarqube.gradle.SonarQubePlugin
import org.sonarqube.gradle.SonarProperties
import org.sonarqube.gradle.SonarTask

import javax.inject.Named

import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addAllProjectsEvaluatedListener
import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addProjectEvaluatedListener
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject
import static org.kordamp.gradle.util.PluginUtils.resolveConfig
import static org.kordamp.gradle.util.StringUtils.isBlank
import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.32.0
 */
@CompileStatic
class SonarPlugin extends AbstractKordampPlugin {
    Project project

    SonarPlugin() {
        super(Sonar.PLUGIN_ID)
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
                addProjectEvaluatedListener(project, new SonarProjectEvaluatedListener())
            }
        })
    }

    private void configureRootProject(Project project) {
        addAllProjectsEvaluatedListener(project, new SonarAllProjectsEvaluatedListener())
    }

    @Named('sonar')
    @DependsOn(['base'])
    private class SonarProjectEvaluatedListener implements ProjectEvaluatedListener {
        @Override
        void projectEvaluated(Project project) {
            ProjectConfigurationExtension config = resolveConfig(project)
            setEnabled(config.quality.sonar.enabled)
            ProjectConfigurationExtension rootConfig = resolveConfig(project.rootProject)

            SonarExtension sonarExt = project.extensions.findByType(SonarExtension)
            sonarExt.skipProject = rootConfig.quality.sonar.excludedProjects.contains(project.name)
        }
    }

    @Named('sonar')
    @DependsOn(['jacoco', 'codenarc', 'checkstyle'])
    private class SonarAllProjectsEvaluatedListener implements AllProjectsEvaluatedListener {
        @Override
        void allProjectsEvaluated(Project rootProject) {
            applySonarToRoot(rootProject)
        }
    }

    private static void applySonarToRoot(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)
        SonarExtension sonarExt = project.extensions.findByType(SonarExtension)
        sonarExt.properties(new Action<SonarProperties>() {
            @Override
            void execute(SonarProperties p) {
                config.quality.sonar.getConfigProperties().each { property, value ->
                    p.property(property, value)
                }
                addIfUndefined('sonar.host.url', config.quality.sonar.hostUrl, p)
                addIfUndefined('sonar.projectKey', config.quality.sonar.projectKey, p)
                addIfUndefined('sonar.exclusions', config.quality.sonar.excludes.join(','), p)
                if (config.coverage.jacoco.enabled) {
                    p.property('sonar.coverage.jacoco.xmlReportPaths', config.coverage.jacoco.aggregateReportXmlFile.absolutePath)
                }
                if (config.quality.spotbugs.enabled) {
                    p.property('sonar.java.spotbugs.reportPaths', resolveBuiltFile(project, 'reports/spotbugs/aggregateSpotbugs.xml'))
                }
                if (config.quality.checkstyle.enabled) {
                    p.property('sonar.java.checkstyle.reportPaths', resolveBuiltFile(project, 'reports/checkstyle/aggregate.xml'))
                }
                if (config.quality.codenarc.enabled) {
                    p.property('sonar.groovy.codenarc.reportPaths', resolveBuiltFile(project, 'reports/codenarc/aggregate.xml'))
                }
                addIfUndefined('sonar.projectName', config.info.name, p)
                addIfUndefined('sonar.projectDescription', config.info.description, p)
                addIfUndefined('sonar.links.homepage', config.info.links.website, p)
                addIfUndefined('sonar.links.scm', config.info.scm.url, p)
                addIfUndefined('sonar.links.issue', config.info.issueManagement.url, p)
                addIfUndefined('sonar.links.ci', config.info.ciManagement.url, p)
                addIfUndefined('sonar.organization', config.quality.sonar.organization, p)
                addIfUndefined('sonar.login', config.quality.sonar.login, p)
                addIfUndefined('sonar.password', config.quality.sonar.password, p)
            }

            private String resolveBuiltFile(Project p, String path) {
                p.layout.buildDirectory.file(path).get().asFile.absolutePath
            }

            private void addIfUndefined(String sonarProperty, String value, SonarProperties p) {
                Map<String, Object> properties = p.getProperties()
                if (!properties.containsKey(sonarProperty) || isBlank(properties.get(sonarProperty) as String) && isNotBlank(value)) {
                    p.property(sonarProperty, value)
                }
            }
        })

        project.tasks.withType(SonarTask, new Action<SonarTask>() {
            @Override
            void execute(SonarTask t) {
                t.setGroup('Quality')
                if (!config.coverage.coveralls.standalone) {
                    if (config.coverage.jacoco.enabled) {
                        t.dependsOn(project.tasks.named('aggregateJacocoReport'))
                    }
                    if (config.quality.codenarc.enabled) {
                        t.dependsOn(project.tasks.named('aggregateCodenarc'))
                    }
                    if (config.quality.checkstyle.enabled) {
                        t.dependsOn(project.tasks.named('aggregateCheckstyle'))
                    }
                    if (config.quality.spotbugs.enabled) {
                        t.dependsOn(project.tasks.named('aggregateSpotbugs'))
                    }
                }
            }
        })
    }
}
