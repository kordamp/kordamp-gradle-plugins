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
package org.kordamp.gradle.plugin.inline

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TupleConstructor
import org.gradle.BuildAdapter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.BasePlugin
import org.kordamp.gradle.util.Cache

import java.util.function.Consumer
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.regex.Matcher
import java.util.regex.Pattern

import static org.kordamp.gradle.util.GlobUtils.asGlobRegex
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
    static final String KORDAMP_INLINE_PLUGIN_GROUPS = 'org.kordamp.gradle.inline.plugin.groups'
    static final String GRADLE_PLUGIN_GROUP = 'org.gradle'

    private Settings settings

    private final Set<String> corePlugins = new TreeSet<>()
    private final Map<String, Properties> pluginGroups = new LinkedHashMap<>()

    @Override
    void apply(Settings settings) {
        if (!checkFlag(KORDAMP_INLINE_ENABLED, true)) {
            return
        }

        this.settings = settings

        findDefaultPlugins()
        findPluginGroupsInClasspath()
        findPluginGroupsInGradleUserHome(settings.gradle.gradleUserHomeDir)
        sortPluginGroups()

        Configuration configuration = settings.buildscript.configurations.maybeCreate('inlinePlugins')
        configuration.extendsFrom(settings.buildscript.configurations.findByName('classpath'))
        configuration.canBeResolved = true
        configuration.visible = false

        List<String> taskNames = []
        Set<IncludedPlugin> plugins = []
        Stack<ProjectRegex> regexes = new Stack<>()

        boolean projectRegexEnabled = checkFlag(KORDAMP_INLINE_PROJECT_REGEX, true)
        boolean pluginsEnabled = checkFlag(KORDAMP_INLINE_PLUGINS, true)
        boolean adaptEnabled = checkFlag(KORDAMP_INLINE_ADAPT, true)

        List<String> args = []
        boolean regexFound = false
        for (String task : settings.gradle.startParameter.taskNames) {
            PluginAlias pluginAlias = ExternalPlugin.isAliasedPluginDefinition(task, pluginGroups)

            if (projectRegexEnabled && ProjectRegex.isProjectRegex(task)) {
                if (regexFound) {
                    regexes.peek().args.addAll(args)
                    args.clear()
                }
                ProjectRegex regex = ProjectRegex.parse(task)
                regexes << regex
                regexFound = true
            } else if (pluginsEnabled && CorePlugin.isPluginDefinition(task)) {
                if (regexFound) {
                    regexes.peek().args.addAll(args)
                    args.clear()
                    regexFound = false
                }
                CorePlugin plugin = CorePlugin.parse(task)
                if (plugin.isGradleCorePlugin(corePlugins)) {
                    plugins << plugin
                    taskNames << plugin.taskName
                } else if (pluginAlias) {
                    ExternalPlugin p = ExternalPlugin.of(pluginAlias)
                    plugins << p
                    taskNames << p.taskName
                    settings.buildscript.dependencies.add('inlinePlugins', p.coordinates)
                } else {
                    // unmatched
                    // TODO: warn?
                    taskNames << task
                }
            } else if (pluginsEnabled && pluginAlias) {
                if (regexFound) {
                    regexes.peek().args.addAll(args)
                    args.clear()
                    regexFound = false
                }
                ExternalPlugin p = ExternalPlugin.of(pluginAlias)
                plugins << p
                taskNames << p.taskName
                settings.buildscript.dependencies.add('inlinePlugins', p.coordinates)
            } else if (pluginsEnabled && ExternalPlugin.isPluginDefinition(task)) {
                if (regexFound) {
                    regexes.peek().args.addAll(args)
                    args.clear()
                    regexFound = false
                }
                ExternalPlugin plugin = ExternalPlugin.parse(task)
                plugins << plugin
                taskNames << plugin.taskName
                settings.buildscript.dependencies.add('inlinePlugins', plugin.coordinates)
            } else {
                args << task
            }
        }

        if (regexFound) {
            regexes.peek().args.addAll(args)
            args.clear()
        }
        // any remaining args?
        if (args) taskNames.addAll(args)
        settings.gradle.startParameter.taskNames = taskNames

        Set<File> files = []
        if (plugins) {
            files = configuration.resolve()
            for (File file : files) {
                for (IncludedPlugin plugin : plugins) {
                    if (plugin instanceof ExternalPlugin) {
                        if (((ExternalPlugin) plugin).fileNameMatches(file.name)) {
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
                    for (ProjectRegex regex : regexes) {
                        tasks.addAll(regex.expand(gradle.rootProject))
                    }
                    tasks.addAll(settings.gradle.startParameter.taskNames)

                    settings.gradle.startParameter.taskNames = tasks
                }
            }

            @Override
            void projectsEvaluated(Gradle gradle) {
                if (adaptEnabled) adaptProperties(gradle.rootProject)
            }
        })
    }

    private void findDefaultPlugins() {
        Cache.Key key = Cache.key(settings.gradle.gradleHomeDir.absolutePath + '-plugins.txt')

        Consumer<InputStream> reader = { InputStream r -> r.text.split('\n').each { pluginId -> corePlugins.add(pluginId) } }

        if (!Cache.getInstance().get(settings.gradle, key, reader)) {
            computeDefaultPlugins()
            Cache.getInstance().write(settings.gradle, key) { w -> w.write(corePlugins.join('\n')) }
        }
    }

    private void computeDefaultPlugins() {
        Enumeration<URL> e = InlinePlugin.classLoader.getResources('META-INF/gradle-plugins')
        while (e.hasMoreElements()) {
            extractMetadata(e.nextElement(), corePlugins)
        }
        e = BasePlugin.classLoader.getResources('META-INF/gradle-plugins')
        while (e.hasMoreElements()) {
            extractMetadata(e.nextElement(), corePlugins)
        }
    }

    private void extractMetadata(URL url, Set<String> corePlugins) {
        if (url.protocol != 'jar') return

        JarFile jarFile = new JarFile(url.toString()[9..url.toString().indexOf('!') - 1])
        Enumeration<JarEntry> entries = jarFile.entries()
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement()
            Matcher matcher = (entry.name =~ /META-INF\/gradle-plugins\/(.+)\.properties/)
            if (matcher.matches()) {
                Properties props = new Properties()
                props.load(jarFile.getInputStream(entry))
                String implementationClass = (String) props.'implementation-class'
                String pluginId = matcher.group(1) - 'org.gradle.'

                if (implementationClass.startsWith(GRADLE_PLUGIN_GROUP)) {
                    corePlugins.add(pluginId)
                }
            }
        }
    }

    private void findPluginGroupsInClasspath() {
        Enumeration<URL> e = InlinePlugin.classLoader.getResources('META-INF/org.kordamp.gradle.inline')
        while (e.hasMoreElements()) {
            URL url = e.nextElement()
            if (url.protocol != 'jar') continue

            JarFile jarFile = new JarFile(url.toString()[9..url.toString().indexOf('!') - 1])
            Enumeration<JarEntry> entries = jarFile.entries()
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement()
                Matcher matcher = (entry.name =~ /META-INF\/org.kordamp.gradle.inline\/(.+)\.properties/)
                if (matcher.matches()) {
                    Properties props = new Properties()
                    props.load(jarFile.getInputStream(entry))
                    String pluginGroup = matcher.group(1)
                    if (pluginGroups.containsKey(pluginGroup)) {
                        Properties other = pluginGroups.get(pluginGroup)
                        other.putAll(props)
                    } else {
                        pluginGroups.put(pluginGroup, props)
                    }
                }
            }
        }
    }

    private void findPluginGroupsInGradleUserHome(File gradleUserHomeDir) {
        File inlinePluginDir = new File(gradleUserHomeDir, 'org.kordamp.gradle.inline')
        if (inlinePluginDir.exists() && inlinePluginDir.directory) {
            inlinePluginDir.listFiles(new FilenameFilter() {
                @Override
                boolean accept(File dir, String name) {
                    return name.endsWith('.properties')
                }
            }).each { File file ->
                Properties props = new Properties()
                file.withInputStream { ins -> props.load(ins) }
                String pluginGroup = file.name - '.properties'
                if (pluginGroups.containsKey(pluginGroup)) {
                    Properties other = pluginGroups.get(pluginGroup)
                    other.putAll(props)
                } else {
                    pluginGroups.put(pluginGroup, props)
                }
            }
        }
    }

    private void sortPluginGroups() {
        String[] groupOrder = System.getProperty(KORDAMP_INLINE_PLUGIN_GROUPS, '').split(',')

        Map<String, Properties> map = new LinkedHashMap<>()
        Map<String, Properties> copy = new LinkedHashMap<>(pluginGroups)
        for (String group : groupOrder) {
            Properties props = copy.remove(group.trim())
            if (props) {
                map.put(group.trim(), props)
            }
        }
        map.putAll(copy)

        pluginGroups.clear()
        pluginGroups.putAll(map)
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
        final List<String> pathOrName = []

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
                    pathOrName.addAll(targets.split(','))
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
            for (String p : pathOrName) {
                String s = p.trim()
                if (s == project.name ||
                    Pattern.compile('^' + s + '$').matcher(project.name).matches() ||
                    s == project.path ||
                    Pattern.compile(asGlobRegex(s, true)).matcher(project.path).matches()) {
                    return true
                }
            }
            return false
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
    private static class CorePlugin extends IncludedPlugin {
        static boolean isPluginDefinition(String str) {
            String[] parts = str.split(':')
            parts.length == 2 && isNotBlank(parts[0]) && isNotBlank(parts[1])
        }

        static CorePlugin parse(String str) {
            String[] parts = str.split(':')
            new CorePlugin(parts[0], parts[1])
        }

        private CorePlugin(String pluginId, String taskName) {
            super(taskName)
            this.pluginIds << pluginId
        }

        private isGradleCorePlugin(Set<String> pluginIds) {
            Collection<String> intersection = pluginIds.intersect(this.pluginIds)
            return intersection != null && !intersection.isEmpty()
        }
    }

    @Canonical
    @CompileStatic
    private static class ExternalPlugin extends IncludedPlugin {
        final String groupId
        final String artifactId
        final String version

        static boolean isPluginDefinition(String str) {
            String[] parts = str.split(':')
            if (parts.length == 3) {
                return isNotBlank(parts[0]) && isNotBlank(parts[1]) && isNotBlank(parts[2])
            }
            return parts.length == 4 && isNotBlank(parts[0]) && isNotBlank(parts[1]) && isNotBlank(parts[2]) && isNotBlank(parts[3])
        }

        static PluginAlias isAliasedPluginDefinition(String str, Map<String, Properties> pluginGroups) {
            String[] parts = str.split(':')
            if (parts.length == 2 && isNotBlank(parts[0]) && isNotBlank(parts[1])) {
                String alias = parts[0]
                for (Map.Entry<String, Properties> entry : pluginGroups.entrySet()) {
                    Properties props = entry.value
                    if (props.containsKey(alias)) {
                        return new PluginAlias(alias, entry.key, props.getProperty(alias), 'latest.release', parts[1])
                    }
                }
            } else if (parts.length == 3 && isNotBlank(parts[0]) && isNotBlank(parts[1]) && isNotBlank(parts[2])) {
                String alias = parts[0]
                for (Map.Entry<String, Properties> entry : pluginGroups.entrySet()) {
                    Properties props = entry.value
                    if (props.containsKey(alias)) {
                        return new PluginAlias(alias, entry.key, props.getProperty(alias), parts[1], parts[2])
                    }
                }
            }
            null
        }

        static ExternalPlugin parse(String str) {
            String[] parts = str.split(':')
            if (parts.length == 3) {
                return of(parts[0], parts[1], parts[2])
            }
            return new ExternalPlugin(parts[0], parts[1], parts[2], parts[3])
        }

        static ExternalPlugin of(String groupId, String artifactId, String taskName) {
            return new ExternalPlugin(groupId, artifactId, 'latest.release', taskName)
        }

        static ExternalPlugin of(PluginAlias pluginAlias) {
            return new ExternalPlugin(pluginAlias.groupId, pluginAlias.artifactId, pluginAlias.version, pluginAlias.taskName)
        }

        private ExternalPlugin(String groupId, String artifactId, String version, String taskName) {
            super(taskName)
            this.groupId = groupId
            this.artifactId = artifactId
            this.version = version
        }

        String getCoordinates() {
            "${groupId}:${artifactId}:${version}".toString()
        }

        boolean fileNameMatches(String fileName) {
            if (version != 'latest.release') {
                return fileName == "${artifactId}-${version}.jar".toString()
            }
            return fileName.startsWith(artifactId + '-') && fileName.endsWith('.jar')
        }
    }

    @CompileStatic
    @TupleConstructor
    @ToString(includeNames = true)
    private static class PluginAlias {
        final String alias
        final String groupId
        final String artifactId
        final String version
        final String taskName
    }

    @Canonical
    @CompileStatic
    private static class ProjectRegex {
        final String regex
        final String taskName
        final List<String> args = []

        static boolean isProjectRegex(String str) {
            String[] parts = str.split(':')
            parts.length == 3 && isBlank(parts[0]) &&
                isNotBlank(parts[1]) && isNotBlank(parts[2]) &&
                parts[1] =~ /[\*\+\?\|\[\]\{\}\^\$]/
        }

        static ProjectRegex parse(String str) {
            String[] parts = str.split(':')
            new ProjectRegex(':' + parts[1], parts[2])
        }

        private ProjectRegex(String regex, String taskName) {
            this.regex = regex
            this.taskName = taskName
        }

        List<String> expand(Project rootProject) {
            List<String> taskNames = []

            if (matches(rootProject)) {
                taskNames << rootProject.path + ':' + taskName
                taskNames.addAll(args)
            }
            for (Project p : rootProject.childProjects.values()) {
                if (matches(p)) {
                    taskNames << p.path + ':' + taskName
                    taskNames.addAll(args)
                }
            }

            taskNames
        }

        boolean matches(Project project) {
            return regex == project.path || Pattern.compile(asGlobRegex(regex, true)).matcher(project.path).matches()
        }
    }
}
