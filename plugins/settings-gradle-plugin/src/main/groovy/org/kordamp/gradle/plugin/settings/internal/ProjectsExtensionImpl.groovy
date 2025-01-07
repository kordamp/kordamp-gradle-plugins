/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2025 Andres Almiray.
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
import groovy.transform.Memoized
import org.gradle.BuildAdapter
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.provider.Providers
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.kordamp.gradle.plugin.settings.PluginsSpec
import org.kordamp.gradle.plugin.settings.ProjectsExtension
import org.kordamp.gradle.util.Cache
import org.kordamp.gradle.util.ConfigureUtil

import java.util.regex.Pattern

import static org.kordamp.gradle.util.GlobUtils.asGlobRegex

/**
 * @author Andres Almiray
 * @since 0.35.0
 */
@CompileStatic
class ProjectsExtensionImpl implements ProjectsExtension {
    private static final Logger LOG = Logging.getLogger(Project)

    final PluginsSpecImpl plugins = new PluginsSpecImpl()

    final Property<Layout> layout
    final Property<Boolean> enforceNamingConvention
    final Property<Boolean> useLongPaths
    final Property<Boolean> cache
    final ListProperty<String> directories
    final MapProperty<String, String> directoriesWithPrefixSuffix
    final SetProperty<String> excludes
    final Property<String> prefix
    final Property<String> suffix
    final Property<FileNameTransformation> fileNameTransformation

    final Set<DirectorySpecImpl> directoryConditions = []
    final Set<PathSpecImpl> pathConditions = []

    private final Settings settings

    ProjectsExtensionImpl(Settings settings, ObjectFactory objects) {
        this.settings = settings
        layout = objects.property(Layout).convention(Layout.TWO_LEVEL)
        enforceNamingConvention = objects.property(Boolean).convention(true)
        useLongPaths = objects.property(Boolean).convention(false)
        cache = objects.property(Boolean).convention(false)
        directories = objects.listProperty(String).convention([])
        directoriesWithPrefixSuffix = objects.mapProperty(String, String).convention([:])
        excludes = objects.setProperty(String).convention(new LinkedHashSet<String>())
        prefix = objects.property(String).convention(Providers.<String>notDefined())
        suffix = objects.property(String).convention(Providers.<String>notDefined())
        fileNameTransformation = objects.property(FileNameTransformation).convention(Providers.<FileNameTransformation>notDefined())

        settings.gradle.addBuildListener(new BuildAdapter() {
            @Override
            void settingsEvaluated(Settings s) {
                Cache.Key key = Cache.key(s.settingsDir.absolutePath + '-projects')
                if (!resolveFromCache(key)) {
                    resolveFromConfig()
                    writeConfigToCache(key)
                }
                Cache.getInstance().touch(settings.gradle, key)
            }

            @Override
            void projectsLoaded(Gradle gradle) {
                applyPlugins(gradle.rootProject)
            }
        })
    }

    private boolean resolveFromCache(Cache.Key key) {
        long settingsLastModified = findSettingsFile()?.lastModified() ?: Long.MIN_VALUE

        LOG.info("[settings] Caching of project structure is ${cache.get()? 'enabled': 'disabled'}.")

        if (cache.get() &&
            Cache.getInstance().has(settings.gradle, key) &&
            Cache.getInstance().lastModified(settings.gradle, key) > settingsLastModified) {

            LOG.info("[settings] Reading project structure from cache. ${key.getAbsolutePath(settings.gradle)}")

            return Cache.getInstance().read(settings.gradle, key) { BufferedReader reader ->
                reader.readLines().each { String line ->
                    String[] parts = line.split('#')
                    String projectPath = parts[0]
                    String projectName = parts[1]
                    File projectDir = new File(parts[2])
                    File buildFile = new File(parts[3])
                    settings.include(projectPath)
                    settings.project(projectPath).name = projectName
                    settings.project(projectPath).projectDir = projectDir
                    settings.project(projectPath).buildFileName = buildFile.name
                    LOG.info("[settings] Including project ${projectPath} -> ${buildFile.absolutePath}")
                }
            }
        }

        false
    }

    private File findSettingsFile() {
        [
            settings.gradle.startParameter.settingsFile,
            new File(settings.settingsDir, 'settings.gradle'),
            new File(settings.settingsDir, 'settings.gradle.kts')
        ].find { it?.exists() } ?: null
    }

    private void resolveFromConfig() {
        LOG.info('[settings] Resolving project structure from configuration')

        switch (layout.get()) {
            case Layout.TWO_LEVEL:
                processTwoLevelLayout()
                break
            case Layout.MULTI_LEVEL:
                processMultiLevelLayout()
                break
            case Layout.STANDARD:
                processStandardLayout()
                break
            case Layout.EXPLICIT:
                break
        }
        // Always process explicit
        processExplicitDirectoriesAndPaths()
        cleanup()
    }

    private void writeConfigToCache(Cache.Key key) {
        LOG.info("[settings] Writing project structure to cache. ${key.getAbsolutePath(settings.gradle)}")

        Cache.getInstance().write(settings.gradle, key) { BufferedWriter writer ->
            writeProjectToCache(settings.rootProject, writer)
        }
    }

    private void writeProjectToCache(ProjectDescriptor project, BufferedWriter writer) {
        writer.writeLine([
            project.path,
            project.name,
            project.projectDir.absolutePath,
            project.buildFile.absolutePath
        ].join('#'))
        for (ProjectDescriptor child : project.children) {
            writeProjectToCache(child, writer)
        }
    }

    @Override
    void setLayout(String layout) {
        this.layout.set(Layout.valueOf(layout.trim().toUpperCase().replace('-', '_')))
    }

    @Override
    void setFileNameTransformation(String transformation) {
        this.fileNameTransformation.set(FileNameTransformation.valueOf(transformation.trim().toUpperCase()))
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
    DirectorySpec includeProjects(String dir) {
        DirectorySpecImpl spec = new DirectorySpecImpl(dir)
        directoryConditions << spec
        spec
    }

    @Override
    PathSpec includeProject(String dir) {
        PathSpecImpl spec = new PathSpecImpl(dir)
        pathConditions << spec
        spec
    }

    @Override
    void directories(Map<String, String> dirs) {
        directoriesWithPrefixSuffix.putAll(dirs)
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
            doIncludeProject(spec.path)
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
        } else if(directoriesWithPrefixSuffix.get()) {
            Map<String, String> dirs = directoriesWithPrefixSuffix.get()
            for (String parentDirName : dirs.keySet()) {
                File parentDir = new File(settings.rootDir, parentDirName)
                if (!parentDir.exists()) {
                    LOG.info "Skipping ${parentDir} as it does not exist."
                    continue
                }

                doProcessTwoLevelLayout(parentDir, dirs.get(parentDirName))
            }
        } else {
            settings.settingsDir.eachDir { File parentDir ->
                if (!skipDirectoryDiscoveryFor(parentDir)) {
                    doProcessTwoLevelLayout(parentDir)
                }
            }
        }
    }

    private void processMultiLevelLayout() {
        if (directories.get()) {
            for (String path : directories.get()) {
                File projectDir = new File(settings.rootDir, path)
                if (!projectDir.exists()) {
                    LOG.info "Skipping ${projectDir} as it does not exist."
                    continue
                }
                doIncludeProject(path)
            }
        } else if(directoriesWithPrefixSuffix.get()) {
            Map<String, String> dirs = directoriesWithPrefixSuffix.get()
            for (String parentDirName : dirs.keySet()) {
                File parentDir = new File(settings.rootDir, parentDirName)
                if (!parentDir.exists()) {
                    LOG.info "Skipping ${parentDir} as it does not exist."
                    continue
                }

                doProcessTwoLevelLayout(parentDir, dirs.get(parentDirName))
            }
        }  else {
            settings.settingsDir.eachDir { File projectDir ->
                discoverProject(projectDir)
            }
        }
    }

    private void discoverProject(File projectDir) {
        if (!skipDirectoryDiscoveryFor(projectDir)) {
            // Is there a matching build file?
            String projectDirName = projectDir.name
            File buildFile = resolveBuildFile(projectDir, projectDirName)
            // yes -> include and stop
            if (buildFile && buildFile.exists()) {
                String projectPath = resolveProjectPath(projectDir, projectDirName)

                settings.include(projectPath)
                settings.project(projectPath).name = projectDirName
                settings.project(projectPath).projectDir = projectDir
                settings.project(projectPath).buildFileName = buildFile.name
                LOG.info("[settings] Including project ${projectPath} -> ${buildFile.absolutePath}")
            } else {
                // no -> continue search
                projectDir.eachDir { File dir ->
                    discoverProject(dir)
                }
            }
        }
    }

    private void processStandardLayout() {
        settings.rootDir.eachDir { File projectDir ->
            if (skipDirectoryDiscoveryFor(projectDir) || isProjectExcluded(projectDir.name, projectDir.name)) return

            File buildFile = resolveBuildFile(projectDir, projectDir.name)
            doIncludeProject(settings.rootDir, projectDir.name, buildFile)
        }
    }

    private boolean skipDirectoryDiscoveryFor(File dir) {
        dir.name.charAt(0) == '.' ||
            (dir.name in ['buildSrc', 'gradle'] && settings.rootProject.projectDir == dir.parentFile) ||
            (dir.name == 'build' && settings.findProject(dir.parentFile))
    }

    private void doProcessTwoLevelLayout(File parentDir, String prefixSuffix = null) {
        parentDir.eachDir { File projectDir ->
            if (isProjectExcluded("${projectDir.parentFile.name}/${projectDir.name}", projectDir.name)) return

            File buildFile = resolveBuildFile(projectDir, projectDir.name)
            doIncludeProject(parentDir, projectDir.name, buildFile)
        }
    }

    private void doIncludeProject(File parentDir, String projectDirName) {
        File buildFile = resolveBuildFile(parentDir, projectDirName)
        doIncludeProject(settings.rootDir, parentDir.name, buildFile)
    }

    private void doIncludeProject(File projectDir) {
        doIncludeProject(projectDir.parentFile, projectDir.name)
    }

    private String[] splitByFileSeparator(String path) {
        if (path.contains('/')) {
            return path.split('/')
        }
        return path.split(Pattern.quote(File.separator))
    }

    private void doIncludeProject(String path) {
        String[] parts = splitByFileSeparator(path)
        String projectDirName = path
        String projectName = parts[-1]

        if (isProjectExcluded(projectDirName, projectName)) return

        File projectDir = new File(settings.rootDir, projectDirName)

        assert projectDir.isDirectory()

        File buildFile = resolveBuildFile(projectDir, projectName)
        if (buildFile && buildFile.exists()) {
            String projectPath = resolveProjectPath(projectDir, projectName)

            settings.include(projectPath)
            settings.project(projectPath).name = projectName
            settings.project(projectPath).projectDir = projectDir
            settings.project(projectPath).buildFileName = buildFile.name
            LOG.info("[settings] Including project ${projectPath} -> ${buildFile.absolutePath}")
        }
    }

    private boolean isProjectExcluded(String projectDir, String projectName) {
        if (excludes.get().contains(projectName)) return true

        for (String exclude : excludes.get()) {
            if (pattern(exclude).matcher(projectDir).matches()) return true
        }
        for (String exclude : excludes.get()) {
            if (pattern(exclude).matcher(projectName).matches()) return true
        }

        false
    }

    @Memoized
    private Pattern pattern(String regex) {
        Pattern.compile(asGlobRegex(regex))
    }

    private File resolveBuildFile(File projectDir, String projectName) {
        if (enforceNamingConvention.get()) {
            if (FileNameTransformation.ADD == fileNameTransformation.orNull) {
                if (!isBlank(prefix.orNull)) {
                    projectName = prefix.get() + projectName
                }
                if (!isBlank(suffix.orNull)) {
                    projectName += suffix.get()
                }
            } else if (FileNameTransformation.REMOVE == fileNameTransformation.orNull) {
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
            String projectPath = resolveProjectPath(projectDir, projectDirName)

            settings.include(projectPath)
            settings.project(projectPath).name = projectDirName
            settings.project(projectPath).projectDir = projectDir
            settings.project(projectPath).buildFileName = buildFile.name
            LOG.info("[settings] Including project ${projectPath} -> ${buildFile.absolutePath}")
        }
    }

    private String resolveProjectPath(File projectDir, String projectName) {
        String projectPath = ':' + projectName
        if (getUseLongPaths().get()) {
            projectPath = projectDir.absolutePath - settings.rootDir.absolutePath
            if (projectPath.endsWith(File.separator)) projectPath = projectPath[0..-2]
            projectPath = projectPath.replace(File.separator, ':')
        }
        projectPath
    }

    private void cleanup() {
        ProjectDescriptor pd = settings.rootProject
        Set<ProjectDescriptor> children = new LinkedHashSet<>(pd.children)
        for (ProjectDescriptor child : children) {
            purgeEmptyProjects(child, pd)
        }
    }

    private void purgeEmptyProjects(ProjectDescriptor project, ProjectDescriptor parent) {
        if (!project.children) {
            if (!project.buildFile.exists()) {
                parent.children.remove(project)
            }
        } else {
            Set<ProjectDescriptor> children = new LinkedHashSet<>(project.children)
            for (ProjectDescriptor child : children) {
                purgeEmptyProjects(child, project)
            }

            if (!project.buildFile.exists()) {
                parent.children.remove(project)
                parent.children.addAll(project.children)
            }
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
