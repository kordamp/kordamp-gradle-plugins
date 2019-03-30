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
package org.kordamp.gradle.plugin.buildscan

import com.gradle.scan.plugin.BuildScanExtension
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * @author Andres Almiray
 * @since 0.16.0
 */
@CompileStatic
class BuildScanPlugin extends AbstractKordampPlugin {
    public static final String BUILD_SCAN_AGREE = 'build.scan.agree'
    Project project

    void apply(Project project) {
        this.project = project

        if (isRootProject(project)) {
            configureProject(project)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        project.plugins.apply(com.gradle.scan.plugin.BuildScanPlugin)

        project.tasks.register('setBuildScanAgreement', BuildScanAgreementTask,
            new Action<BuildScanAgreementTask>() {
                @Override
                void execute(BuildScanAgreementTask t) {
                    t.group = 'BuildScan'
                    t.description = 'Sets the value of the build scan agreement'
                    t.remove = false
                }
            })

        project.tasks.register('removetBuildScanAgreement', BuildScanAgreementTask,
            new Action<BuildScanAgreementTask>() {
                @Override
                void execute(BuildScanAgreementTask t) {
                    t.group = 'BuildScan'
                    t.description = 'Removes the value of the build scan agreement'
                    t.remove = true
                }
            })


        project.tasks.register('listBuildScanAgreements', ListBuildScanAgreementTask,
            new Action<ListBuildScanAgreementTask>() {
                @Override
                void execute(ListBuildScanAgreementTask t) {
                    t.group = 'BuildScan'
                    t.description = 'List the value of each build scan agreement'
                }
            })

        project.afterEvaluate {
            configureBuildScan(project)
        }
    }

    private void configureBuildScan(Project project) {
        BuildScanExtension buildScan = project.extensions.findByType(BuildScanExtension)
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)
        setEnabled(effectiveConfig.buildScan.enabled)

        if (!buildScan.termsOfServiceUrl) {
            buildScan.termsOfServiceUrl = 'https://gradle.com/terms-of-service'
        }

        if (!enabled) {
            buildScan.termsOfServiceAgree = 'no'
            return
        }

        // 1. check if explicit set
        if (buildScan.termsOfServiceAgree) {
            // done
            return
        }

        // 2. check if set via System prop
        if (System.getProperty(BUILD_SCAN_AGREE)) {
            String answer = System.getProperty(BUILD_SCAN_AGREE)
            if ('yes'.equalsIgnoreCase(answer) || 'no'.equalsIgnoreCase(answer)) {
                buildScan.termsOfServiceAgree = answer
            } else {
                project.logger.error("Invalid value for System property '${BUILD_SCAN_AGREE}'\nExpecting 'yes' or 'no', found '$answer'")
                buildScan.termsOfServiceAgree = 'no'
            }
            // done
            return
        }

        // 3. check if set at project level
        File scanFile = resolveProjectScanFile(project)
        if (scanFile.exists()) {
            String answer = scanFile.text.trim()
            if ('yes'.equalsIgnoreCase(answer) || 'no'.equalsIgnoreCase(answer)) {
                buildScan.termsOfServiceAgree = answer
            } else {
                project.logger.error("Invalid content in ${scanFile.absolutePath}\nExpecting 'yes' or 'no', found '$answer'")
                buildScan.termsOfServiceAgree = 'no'
            }

            // done
            return
        }

        // 4. check if set at global level
        scanFile = resolveGlobalScanFile(project)
        if (scanFile.exists()) {
            String answer = scanFile.text.trim()
            if ('yes'.equalsIgnoreCase(answer) || 'no'.equalsIgnoreCase(answer)) {
                buildScan.termsOfServiceAgree = answer
            } else {
                project.logger.error("Invalid content in ${scanFile.absolutePath}\nExpecting 'yes' or 'no', found '$answer'")
                buildScan.termsOfServiceAgree = 'no'
            }

            // done
            return
        }

        // no explicit agreement
        buildScan.termsOfServiceAgree = null
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(BuildScanPlugin)) {
            project.plugins.apply(BuildScanPlugin)
        }
    }

    static File resolveProjectScanFile(Project project) {
        new File("${project.projectDir}/.gradle/build-scan-agree.txt")
    }

    static File resolveGlobalScanFile(Project project) {
        new File("${project.gradle.gradleUserHomeDir}/build-scans/${project.projectDir.name}/build-scan-agree.txt")
    }
}
