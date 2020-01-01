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
package org.kordamp.gradle.plugin.settings

import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

/**
 * @author Andres Almiray
 * @since 0.15.0
 */
@CompileStatic
class SettingsPlugin implements Plugin<Settings> {
    private Settings settings

    @Override
    void apply(Settings settings) {
        this.settings = settings

        if (!settings.extensions.findByType(ProjectsExtension)) {
            settings.extensions.create(ProjectsExtension.EXTENSION_NAME, ProjectsExtension)
        }

        settings.gradle.addBuildListener(new BuildAdapter() {
            @Override
            void settingsEvaluated(Settings s) {
                ProjectsExtension projects = (ProjectsExtension) settings.extensions.findByName(ProjectsExtension.EXTENSION_NAME)

                if ('two-level' == projects.layout?.toLowerCase()) {
                    processTwoLevelLayout()
                } else if ('multi-level' == projects.layout?.toLowerCase()) {
                    processMultiLevelLayout()
                } else if ('standard' == projects.layout?.toLowerCase()) {
                    processStandardLayout()
                } else {
                    println "Unknown project layout '${projects.layout}'. No subprojects will be added."
                }
            }
        })
    }

    private void processTwoLevelLayout() {
        ProjectsExtension projects = (ProjectsExtension) settings.extensions.findByName(ProjectsExtension.EXTENSION_NAME)

        if (projects.directories) {
            for (String parentDirName : projects.directories) {
                File parentDir = new File(settings.rootDir, parentDirName)
                if (!parentDir.exists()) {
                    println "Skipping ${parentDir} as it does not exist"
                    continue
                }

                doProcessTwoLevelLayout(projects, parentDir)
            }
        } else {
            settings.settingsDir.eachDir { File parentDir ->
                doProcessTwoLevelLayout(projects, parentDir)
            }
        }
    }

    private void doProcessTwoLevelLayout(ProjectsExtension projects, File parentDir) {
        parentDir.eachDir { File projectDir ->
            if (projects.excludes.contains(projectDir.name)) return

            File buildFile = resolveBuildFile(projects, projectDir, projectDir.name)
            SettingsPlugin.doIncludeProject(settings, parentDir, projectDir.name, buildFile)
        }
    }

    private void processMultiLevelLayout() {
        ProjectsExtension projects = (ProjectsExtension) settings.extensions.findByName(ProjectsExtension.EXTENSION_NAME)

        for (String path : projects.directories) {
            File projectDir = new File(settings.rootDir, path)
            if (!projectDir.exists()) {
                println "Skipping ${projectDir} as it does not exist"
                continue
            }
            SettingsPlugin.includeProject(settings, path)
        }
    }

    private void processStandardLayout() {
        ProjectsExtension projects = (ProjectsExtension) settings.extensions.findByName(ProjectsExtension.EXTENSION_NAME)

        settings.rootDir.eachDir { File projectDir ->
            if (projects.excludes.contains(projectDir.name)) return

            File buildFile = resolveBuildFile(projects, projectDir, projectDir.name)
            SettingsPlugin.doIncludeProject(settings, settings.rootDir, projectDir.name, buildFile)
        }
    }

    static void includeProject(Settings settings, File parentDir, String projectDirName) {
        ProjectsExtension projects = (ProjectsExtension) settings.extensions.findByName(ProjectsExtension.EXTENSION_NAME)

        File buildFile = resolveBuildFile(projects, parentDir, projectDirName)
        doIncludeProject(settings, settings.rootDir, parentDir.name, buildFile)
    }

    static void includeProject(Settings settings, File projectDir) {
        includeProject(settings, projectDir.parentFile, projectDir.name)
    }

    static void includeProject(Settings settings, String projectPath) {
        ProjectsExtension projects = (ProjectsExtension) settings.extensions.findByName(ProjectsExtension.EXTENSION_NAME)

        String[] parts = projectPath.split('/')
        String projectDirName = projectPath
        String projectName = parts[-1]

        if (projects.excludes.contains(projectName)) return

        File projectDir = new File(projectDirName)

        assert projectDir.isDirectory()

        File buildFile = resolveBuildFile(projects, projectDir, projectName)
        if (buildFile.exists()) {
            settings.include(projectName)
            settings.project(":${projectName}").projectDir = projectDir
            settings.project(":${projectName}").buildFileName = buildFile.name
        }
    }

    private static File resolveBuildFile(ProjectsExtension projects, File projectDir, String projectName) {
        if (projects.enforceNamingConvention) {
            if ('add'.equalsIgnoreCase(projects.fileNameTransformation)) {
                if (!isBlank(projects.prefix)) {
                    projectName = projects.prefix + projectName
                }
                if (!isBlank(projects.suffix)) {
                    projectName += projects.suffix
                }
            } else if ('remove'.equalsIgnoreCase(projects.fileNameTransformation)) {
                if (!isBlank(projects.prefix)) {
                    projectName -= projects.prefix
                }
                if (!isBlank(projects.suffix)) {
                    projectName -= projects.suffix
                }
            }
        }

        File buildFile = new File(projectDir, projects.enforceNamingConvention ? "${projectName}.gradle".toString() : 'build.gradle')
        if (!buildFile.exists()) {
            buildFile = new File(projectDir, projects.enforceNamingConvention ? "${projectName}.gradle.kts".toString() : 'build.gradle.kts')
        }
        buildFile
    }

    private static void doIncludeProject(Settings settings, File parentDir, String projectDirName, File buildFile) {
        if (buildFile.exists()) {
            File projectDir = new File(parentDir, projectDirName)

            settings.include(projectDirName)
            settings.project(":${projectDirName}").projectDir = projectDir
            settings.project(":${projectDirName}").buildFileName = buildFile.name
        }
    }

    private static boolean isBlank(String str) {
        if (str == null || str.length() == 0) {
            return true
        }
        for (char c : str.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                return false
            }
        }

        return true
    }
}
