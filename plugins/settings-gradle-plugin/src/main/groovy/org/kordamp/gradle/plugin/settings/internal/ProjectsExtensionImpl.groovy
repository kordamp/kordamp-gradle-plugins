/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2022 Andres Almiray.
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
package org.kordamp.gradle.plugin.settings.internal

import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.plugin.settings.PluginsSpec
import org.kordamp.gradle.plugin.settings.ProjectsExtension

/**
 * @author Andres Almiray
 * @since 0.35.0
 */
@CompileStatic
class ProjectsExtensionImpl implements ProjectsExtension {
    private static final Logger LOG = Logging.getLogger(Project)

    final PluginsSpecImpl plugins = new PluginsSpecImpl()

    final Property<String> layout
    final Property<Boolean> enforceNamingConvention
    final ListProperty<String> directories
    final ListProperty<String> excludes
    final Property<String> prefix
    final Property<String> suffix
    final Property<String> fileNameTransformation

    final Set<DirectorySpecImpl> directoryConditions = []
    final Set<PathSpecImpl> pathConditions = []

    private final Settings settings

    ProjectsExtensionImpl(Settings settings, ObjectFactory objects) {
        this.settings = settings
        layout = objects.property(String).convention('two-level')
        enforceNamingConvention = objects.property(Boolean).convention(true)
        directories = objects.listProperty(String).convention([])
        excludes = objects.listProperty(String).convention([])
        prefix = objects.property(String)
        suffix = objects.property(String)
        fileNameTransformation = objects.property(String)

        settings.gradle.addBuildListener(new BuildAdapter() {
            @Override
            void settingsEvaluated(Settings s) {
                if ('two-level' == layout.get().toLowerCase()) {
                    processTwoLevelLayout()
                } else if ('multi-level' == layout.get().toLowerCase()) {
                    processMultiLevelLayout()
                } else if ('standard' == layout.get().toLowerCase()) {
                    processStandardLayout()
                } else if ('explicit' == layout.get().toLowerCase()) {
                    processExplicitDirectoriesAndPaths()
                } else {
                    LOG.warn "Unknown project layout '${layout.orNull}'. No subprojects will be added."
                }
            }

            @Override
            void projectsLoaded(Gradle gradle) {
                applyPlugins(gradle.rootProject)
            }
        })
    }

    @Override
    void plugins(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PluginsSpec) Closure<Void> action) {
        ConfigureUtil.configure(action, plugins)
    }

    @Override
    void plugins(Action<? super PluginsSpec> action) {
        action.execute(plugins)
    }
    @Override
    DirectorySpec includeFromDir(String dir) {
        DirectorySpecImpl spec = new DirectorySpecImpl(dir)
        directoryConditions << spec
        spec
    }

    @Override
    PathSpec includeFromPath(String path) {
        PathSpecImpl spec = new PathSpecImpl(path)
        pathConditions << spec
        spec
    }

    private void applyPlugins(Project rootProject) {
        plugins.apply(rootProject)
    }

    private void processExplicitDirectoriesAndPaths() {
        for (DirectorySpecImpl spec : directoryConditions) {
            if (!spec.applies()) continue
            File parentDir = new File(settings.rootDir, spec.dir)
            if (!parentDir.exists()) {
                LOG.info "Skipping ${parentDir} as it does not exist."
                continue
            }

            excludes.addAll(spec.excludes)
            doProcessTwoLevelLayout(parentDir)
        }

        for (PathSpecImpl spec : pathConditions) {
            if (!spec.applies()) continue
            File projectDir = new File(settings.rootDir, spec.path)
            if (!projectDir.exists()) {
                LOG.info "Skipping ${projectDir} as it does not exist."
                continue
            }
            includeProject(spec.path)
        }
    }

    private void processTwoLevelLayout() {
        if (directories.get()) {
            for (String parentDirName : directories.get()) {
                File parentDir = new File(settings.rootDir, parentDirName)
                if (!parentDir.exists()) {
                    LOG.info "Skipping ${parentDir} as it does not exist."
                    continue
                }

                doProcessTwoLevelLayout(parentDir)
            }
        } else {
            settings.settingsDir.eachDir { File parentDir ->
                doProcessTwoLevelLayout(parentDir)
            }
        }
    }

    private void processMultiLevelLayout() {
        for (String path : directories.get()) {
            File projectDir = new File(settings.rootDir, path)
            if (!projectDir.exists()) {
                LOG.info "Skipping ${projectDir} as it does not exist."
                continue
            }
            includeProject(path)
        }
    }

    private void processStandardLayout() {
        settings.rootDir.eachDir { File projectDir ->
            if (excludes.get().contains(projectDir.name)) return

            File buildFile = resolveBuildFile(projectDir, projectDir.name)
            doIncludeProject(settings.rootDir, projectDir.name, buildFile)
        }
    }

    private void doProcessTwoLevelLayout(File parentDir) {
        parentDir.eachDir { File projectDir ->
            if (excludes.get().contains(projectDir.name)) return

            File buildFile = resolveBuildFile(projectDir, projectDir.name)
            doIncludeProject(parentDir, projectDir.name, buildFile)
        }
    }

    private void includeProject(File parentDir, String projectDirName) {
        File buildFile = resolveBuildFile(parentDir, projectDirName)
        doIncludeProject(settings.rootDir, parentDir.name, buildFile)
    }

    private void includeProject(File projectDir) {
        includeProject(projectDir.parentFile, projectDir.name)
    }

    private void includeProject(String projectPath) {
        String[] parts = projectPath.split(File.separator)
        String projectDirName = projectPath
        String projectName = parts[-1]

        if (excludes.get().contains(projectName)) return

        File projectDir = new File(projectDirName)

        assert projectDir.isDirectory()

        File buildFile = resolveBuildFile(projectDir, projectName)
        if (buildFile && buildFile.exists()) {
            settings.include(projectName)
            settings.project(':' + projectName).projectDir = projectDir
            settings.project(':' + projectName).buildFileName = buildFile.name
            LOG.info("[settings] Including project :${projectName} -> ${buildFile.absolutePath}")
        }
    }

    private File resolveBuildFile(File projectDir, String projectName) {
        if (enforceNamingConvention.get()) {
            if ('add'.equalsIgnoreCase(fileNameTransformation.orNull)) {
                if (!isBlank(prefix.orNull)) {
                    projectName = prefix.get() + projectName
                }
                if (!isBlank(suffix.orNull)) {
                    projectName += suffix.get()
                }
            } else if ('remove'.equalsIgnoreCase(fileNameTransformation.orNull)) {
                if (!isBlank(prefix.get())) {
                    projectName -= prefix.get()
                }
                if (!isBlank(suffix.get())) {
                    projectName -= suffix.get()
                }
            }
        }

        for (String fileName : [
            projectName + '.gradle'.toString(),
            'build.gradle',
            projectName + '.gradle.kts'.toString(),
            'build.gradle.kts']) {
            File buildFile = new File(projectDir, fileName)
            if (buildFile.exists()) {
                return buildFile
            }

        }
        null
    }

    private void doIncludeProject(File parentDir, String projectDirName, File buildFile) {
        if (buildFile && buildFile.exists()) {
            File projectDir = new File(parentDir, projectDirName)

            settings.include(projectDirName)
            settings.project(':' + projectDirName).projectDir = projectDir
            settings.project(':' + projectDirName).buildFileName = buildFile.name
            LOG.info("[settings] Including project :${projectDirName} -> ${buildFile.absolutePath}")
        }
    }

    private boolean isBlank(String str) {
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
