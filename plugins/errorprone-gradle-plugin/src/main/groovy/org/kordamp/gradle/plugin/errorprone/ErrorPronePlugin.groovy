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
package org.kordamp.gradle.plugin.errorprone

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.tasks.compile.JavaCompile
import org.kordamp.gradle.annotations.DependsOn
import org.kordamp.gradle.listener.ProjectEvaluatedListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.ErrorProne

import javax.inject.Named

import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addProjectEvaluatedListener
import static org.kordamp.gradle.util.PluginUtils.resolveConfig
import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.31.0
 */
@CompileStatic
class ErrorPronePlugin extends AbstractKordampPlugin {
    Project project

    ErrorPronePlugin() {
        super(ErrorProne.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        configureProject(project)
        project.childProjects.values().each {
            it.pluginManager.apply(ErrorPronePlugin)
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(ErrorPronePlugin)) {
            project.pluginManager.apply(ErrorPronePlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        project.pluginManager.apply(net.ltgt.gradle.errorprone.ErrorPronePlugin)

        project.pluginManager.withPlugin('java-base', new Action<AppliedPlugin>() {
            @Override
            @CompileDynamic
            void execute(AppliedPlugin appliedPlugin) {
                addProjectEvaluatedListener(project, new ErrorproneProjectEvaluatedListener())
            }
        })
    }

    @Named('errorprone')
    @DependsOn(['base'])
    private class ErrorproneProjectEvaluatedListener implements ProjectEvaluatedListener {
        @Override
        @CompileDynamic
        void projectEvaluated(Project project) {
            ProjectConfigurationExtension config = resolveConfig(project)
            setEnabled(config.quality.errorprone.enabled)

            project.dependencies {
                errorprone("com.google.errorprone:error_prone_core:${config.quality.errorprone.errorProneVersion}")
                errorproneJavac("com.google.errorprone:javac:${config.quality.errorprone.errorProneJavacVersion}")
            }

            project.tasks.withType(JavaCompile) { JavaCompile t ->
                t.options.errorprone.enabled = config.quality.errorprone.enabled
                t.options.errorprone.disableAllChecks = config.quality.errorprone.disableAllChecks
                t.options.errorprone.allErrorsAsWarnings = config.quality.errorprone.allErrorsAsWarnings
                t.options.errorprone.allDisabledChecksAsWarnings = config.quality.errorprone.allDisabledChecksAsWarnings
                t.options.errorprone.disableWarningsInGeneratedCode = config.quality.errorprone.disableWarningsInGeneratedCode
                t.options.errorprone.ignoreUnknownCheckNames = config.quality.errorprone.ignoreUnknownCheckNames
                t.options.errorprone.ignoreSuppressionAnnotations = config.quality.errorprone.ignoreSuppressionAnnotations
                t.options.errorprone.compilingTestOnlyCode = config.quality.errorprone.compilingTestOnlyCode
                if (isNotBlank(config.quality.errorprone.excludedPaths)) {
                    t.options.errorprone.excludedPaths = config.quality.errorprone.excludedPaths
                }
            }
        }
    }
}
