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
package org.kordamp.gradle.plugin.inline

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.regex.Matcher
import java.util.regex.Pattern

import static org.kordamp.gradle.util.PluginUtils.checkFlag
import static org.kordamp.gradle.util.StringUtils.isBlank
import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.39.0
 */
@CompileStatic
class InlinePlugin implements Plugin<Settings> {
    static final String KORDAMP_INLINE_PROJECT_REGEX = 'org.kordamp.gradle.inline.project.regex'
    static final String KORDAMP_INLINE_PLUGINS = 'org.kordamp.gradle.inline.plugins'
    static final String KORDAMP_INLINE_ADAPT = 'org.kordamp.gradle.inline.adapt'
    static final String KORDAMP_INLINE_ENABLED = 'org.kordamp.gradle.inline.enabled'

    private Settings settings

    @Override
    void apply(Settings settings) {
        if (!checkFlag(KORDAMP_INLINE_ENABLED, true)) {
            return
        }

        this.settings = settings

        Configuration configuration = settings.buildscript.configurations.maybeCreate('inlinePlugins')
        configuration.extendsFrom(settings.buildscript.configurations.findByName('classpath'))
        configuration.canBeResolved = true
        configuration.visible = false

        List<String> taskNames = []
        Set<IncludedPlugin> plugins = []
        Set<ProjectRegex> regexes = []

        boolean projectRegexEnabled = checkFlag(KORDAMP_INLINE_PROJECT_REGEX, true)
        boolean pluginsEnabled = checkFlag(KORDAMP_INLINE_PLUGINS, true)
        boolean adaptEnabled = checkFlag(KORDAMP_INLINE_ADAPT, true)

        for (String task : settings.gradle.startParameter.taskNames) {
            if (projectRegexEnabled && ProjectRegex.isProjectRegex(task)) {
                ProjectRegex regex = ProjectRegex.parse(task)
                regexes << regex
            } else if (pluginsEnabled && IncludedCorePlugin.isPluginDefinition(task)) {
                IncludedCorePlugin plugin = IncludedCorePlugin.parse(task)
                plugins << plugin
                taskNames << plugin.taskName
            } else if (pluginsEnabled && IncludedExternalPlugin.isPluginDefinition(task)) {
                IncludedExternalPlugin plugin = IncludedExternalPlugin.parse(task)
                plugins << plugin
                taskNames << plugin.taskName
                settings.buildscript.dependencies.add('inlinePlugins', plugin.coordinates)
            } else {
                taskNames << task
            }
        }
        settings.gradle.startParameter.taskNames = taskNames

        Set<File> files = []
        if (plugins) {
            files = configuration.resolve()
            for (File file : files) {
                for (IncludedPlugin plugin : plugins) {
                    if (plugin instanceof IncludedExternalPlugin) {
                        if (file.name == ((IncludedExternalPlugin) plugin).fileName) {
                            findPluginDescriptors(file, plugin.pluginIds)
                        }
                    }
                }
            }
        }

        settings.gradle.addBuildListener(new BuildAdapter() {
            @Override
            void projectsLoaded(Gradle gradle) {
                if (plugins) {
                    includePlugins(plugins, files)
                }

                if (projectRegexEnabled) {
                    List<String> tasks = []
                    tasks.addAll(settings.gradle.startParameter.taskNames)
                    for (ProjectRegex regex : regexes) {
                        tasks.addAll(regex.expand(gradle.rootProject))
                    }

                    settings.gradle.startParameter.taskNames = tasks
                }
            }

            @Override
            void projectsEvaluated(Gradle gradle) {
                if (adaptEnabled) adaptProperties(gradle.rootProject)
            }
        })
    }

    private ClassLoader resolveClassLoader() {
        settings.plugins.findPlugin('org.kordamp.gradle.inline').class.classLoader
    }

    private void includePlugins(Set<IncludedPlugin> plugins, Set<File> files) {
        ClassLoader classLoader = resolveClassLoader()
        for (File file : files) {
            addToClassloader(classLoader, file)
        }

        PluginTargets targets = new PluginTargets()
        for (IncludedPlugin plugin : plugins) {
            plugin.apply(settings.gradle, targets)
        }
    }

    private void adaptProperties(Project project) {
        for (PropertyAdapter adapter : ServiceLoader.load(PropertyAdapter, resolveClassLoader())) {
            adaptProperties(adapter, project)
        }
    }

    private void adaptProperties(PropertyAdapter adapter, Project project) {
        project.logger.debug('Adapting {} with {}', project, adapter.class.name)
        adapter.adapt(project)
        for (Project p : project.childProjects.values()) {
            adaptProperties(adapter, p)
        }
    }

    @CompileDynamic
    private void addToClassloader(ClassLoader classLoader, File file) {
        if (classLoader instanceof URLClassLoader) {
            URLClassLoader cl = (URLClassLoader) classLoader
            URL url = file.toURI().toURL()
            if (cl.getURLs().find { it.toExternalForm().endsWith(file.name) }) return
            cl.addURL(url)
        }
    }

    private static void findPluginDescriptors(File file, Set<String> pluginIds) {
        if (!file.name.endsWith('.jar')) return

        JarFile jarFile = new JarFile(file)
        Enumeration<JarEntry> entries = jarFile.entries()
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement()
            Matcher matcher = (entry.name =~ /META-INF\/gradle-plugins\/(.+)\.properties/)
            if (matcher.matches()) {
                Properties props = new Properties()
                props.load(jarFile.getInputStream(entry))
                if (props.containsKey('implementation-class')) {
                    pluginIds << matcher.group(1)
                    break
                }
            }
        }
    }

    @CompileStatic
    private static class PluginTargets {
        final boolean any
        final boolean all
        final boolean subprojects
        final List<String> paths = []

        PluginTargets() {
            String targets = System.getProperty('inline.target')
            if (targets && targets.trim()) {
                if (targets == 'all') {
                    any = false
                    all = true
                    subprojects = false
                } else if (targets == 'subprojects') {
                    any = false
                    all = false
                    subprojects = true
                } else {
                    any = false
                    all = false
                    subprojects = false
                    paths.addAll(targets.split(','))
                }
            } else {
                any = true
                all = false
                subprojects = false
            }
        }

        boolean matches(Project project) {
            if (any || all) return true
            if (subprojects && project != project.rootProject) return true
            for (String p : paths) {
                String path = p.trim()
                if (path == project.path || Pattern.compile(asRegex(path)).matcher(project.path).matches()) {
                    return true
                }
            }
            return false
        }

        private String asRegex(String wildcard) {
            StringBuilder result = new StringBuilder(wildcard.length())
            result.append('^')
            for (int index = 0; index < wildcard.length(); index++) {
                char character = wildcard.charAt(index)
                switch (character) {
                    case '*':
                        result.append('.*')
                        break;
                    case '?':
                        result.append('.')
                        break;
                    case '$':
                    case '(':
                    case ')':
                    case '.':
                    case '[':
                    case '\\':
                    case ']':
                    case '^':
                    case '{':
                    case '|':
                    case '}':
                        result.append('\\')
                    default:
                        result.append(character)
                        break;
                }
            }
            result.append('$')
            return result.toString()
        }
    }

    @Canonical
    @CompileStatic
    private static abstract class IncludedPlugin {
        final String taskName
        final Set<String> pluginIds = new LinkedHashSet<>()

        protected IncludedPlugin(String taskName) {
            this.taskName = taskName
        }

        void apply(Gradle gradle, PluginTargets targets) {
            if (targets.any) {
                applyTo(findTargetProject(gradle), false)
            } else if (targets.all) {
                Project project = gradle.rootProject
                applyTo(project, true)
            } else if (targets.subprojects) {
                for (Project p : gradle.rootProject.childProjects.values()) {
                    applyTo(p, true)
                }
            } else {
                matchAndApply(targets, gradle.rootProject)
            }
        }

        private void applyTo(Project project, boolean recurse) {
            for (String pluginId : pluginIds) {
                project.pluginManager.apply(pluginId)
            }
            if (recurse) {
                for (Project p : project.childProjects.values()) {
                    applyTo(p, recurse)
                }
            }
        }

        private void matchAndApply(PluginTargets targets, Project project) {
            if (targets.matches(project)) {
                for (String pluginId : pluginIds) {
                    project.pluginManager.apply(pluginId)
                }
            }
            for (Project p : project.childProjects.values()) {
                matchAndApply(targets, p)
            }
        }

        private Project findTargetProject(Gradle gradle) {
            if (gradle.startParameter.projectDir) {
                Project p = findTargetProjectByMatchingProjectDir(gradle.rootProject, gradle.startParameter.projectDir)
                return p ?: gradle.rootProject
            }

            if (gradle.startParameter.buildFile) {
                Project p = findTargetProjectByMatchingBuildFile(gradle.rootProject, gradle.startParameter.buildFile)
                return p ?: gradle.rootProject
            }

            if (gradle.startParameter.currentDir) {
                Project p = findTargetProjectByMatchingCurrentDir(gradle.rootProject, gradle.startParameter.currentDir)
                return p ?: gradle.rootProject
            }

            return gradle.rootProject
        }

        private Project findTargetProjectByMatchingProjectDir(Project project, File projectDir) {
            if (project.projectDir.absolutePath == projectDir.absolutePath) {
                return project
            }

            for (Project p : project.childProjects.values()) {
                Project child = findTargetProjectByMatchingProjectDir(p, projectDir)
                if (child != null) return child
            }

            return null
        }

        private Project findTargetProjectByMatchingBuildFile(Project project, File buildFile) {
            if (project.buildFile.absolutePath == buildFile.absolutePath) {
                return project
            }

            for (Project p : project.childProjects.values()) {
                Project child = findTargetProjectByMatchingBuildFile(p, buildFile)
                if (child != null) return child
            }

            return null
        }

        private Project findTargetProjectByMatchingCurrentDir(Project project, File projectDir) {
            if (project.projectDir.absolutePath == projectDir.absolutePath) {
                return project
            }

            for (Project p : project.childProjects.values()) {
                Project child = findTargetProjectByMatchingCurrentDir(p, projectDir)
                if (child != null) return child
            }

            return null
        }
    }

    @Canonical
    @CompileStatic
    private static class IncludedCorePlugin extends IncludedPlugin {
        static boolean isPluginDefinition(String str) {
            String[] parts = str.split(':')
            parts.size() == 2 && isNotBlank(parts[0]) && isNotBlank(parts[1])
        }

        static IncludedCorePlugin parse(String str) {
            String[] parts = str.split(':')
            new IncludedCorePlugin(parts[0], parts[1])
        }

        private IncludedCorePlugin(String pluginId, String taskName) {
            super(taskName)
            this.pluginIds << pluginId
        }
    }

    @Canonical
    @CompileStatic
    private static class IncludedExternalPlugin extends IncludedPlugin {
        final String groupId
        final String artifactId
        final String version

        static boolean isPluginDefinition(String str) {
            String[] parts = str.split(':')
            parts.size() == 4 && isNotBlank(parts[0]) && isNotBlank(parts[1]) && isNotBlank(parts[2]) && isNotBlank(parts[3])
        }

        static IncludedExternalPlugin parse(String str) {
            String[] parts = str.split(':')
            new IncludedExternalPlugin(parts[0], parts[1], parts[2], parts[3])
        }

        private IncludedExternalPlugin(String groupId, String artifactId, String version, String taskName) {
            super(taskName)
            this.groupId = groupId
            this.artifactId = artifactId
            this.version = version
        }

        String getCoordinates() {
            "${groupId}:${artifactId}:${version}".toString()
        }

        String getFileName() {
            "${artifactId}-${version}.jar".toString()
        }
    }

    @Canonical
    @CompileStatic
    private static class ProjectRegex {
        final String regex
        final String taskName

        static boolean isProjectRegex(String str) {
            String[] parts = str.split(':')
            parts.size() == 3 && isBlank(parts[0]) && isNotBlank(parts[1]) && isNotBlank(parts[2])
        }

        static ProjectRegex parse(String str) {
            String[] parts = str.split(':')
            new ProjectRegex(':' + parts[1], parts[2])
        }

        private ProjectRegex(String regex, String taskName) {
            this.regex = regex
            this.taskName = taskName
        }

        Set<String> expand(Project rootProject) {
            Set<String> taskNames = []

            if (matches(rootProject)) {
                taskNames << rootProject.path + ':' + taskName
            }
            for (Project p : rootProject.childProjects.values()) {
                if (matches(p)) {
                    taskNames << p.path + ':' + taskName
                }
            }

            taskNames
        }

        boolean matches(Project project) {
            return regex == project.path || Pattern.compile(asRegex(regex)).matcher(project.path).matches()
        }

        private String asRegex(String wildcard) {
            StringBuilder result = new StringBuilder(wildcard.length())
            result.append('^')
            for (int index = 0; index < wildcard.length(); index++) {
                char character = wildcard.charAt(index)
                switch (character) {
                    case '*':
                        result.append('.*')
                        break;
                    case '?':
                        result.append('.')
                        break;
                    case '$':
                    case '(':
                    case ')':
                    case '.':
                    case '[':
                    case '\\':
                    case ']':
                    case '^':
                    case '{':
                    case '|':
                    case '}':
                        result.append('\\')
                    default:
                        result.append(character)
                        break;
                }
            }
            result.append('$')
            return result.toString()
        }
    }
}
