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
package org.kordamp.gradle.plugin.base.tasks

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.artifacts.repositories.FlatDirectoryArtifactRepository
import org.gradle.api.artifacts.repositories.IvyArtifactRepository
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.tasks.TaskAction
import org.kordamp.gradle.plugin.KordampPlugin

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.regex.Matcher

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.11.0
 */
@CompileStatic
class PluginsTask extends AbstractReportingTask {
    @TaskAction
    @CompileDynamic
    void report() {
        Map<String, Map<String, Object>> plugins = [:]

        Map<String, String> pluginMetadata = [:]
        Enumeration<URL> e = PluginsTask.classLoader.getResources('META-INF/gradle-plugins')
        while (e.hasMoreElements()) {
            extractMetadata(e.nextElement(), pluginMetadata)
        }

        project.plugins.eachWithIndex { plugin, index -> plugins.putAll(PluginsTask.doReport(plugin, index, pluginMetadata)) }

        doPrint(plugins, 0)
    }

    private static void extractMetadata(URL url, Map<String, String> pluginMetadata) {
        if (url.protocol != 'jar') return

        JarFile jarFile = new JarFile(url.toString()[9..url.toString().indexOf('!') - 1])
        Enumeration<JarEntry> entries = jarFile.entries()
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement()
            Matcher matcher = (entry.name =~ /META-INF\/gradle-plugins\/(.+)\.properties/)
            if (matcher.find()) {
                Properties props = new Properties()
                props.load(jarFile.getInputStream(entry))
                pluginMetadata.put((String) props.'implementation-class', matcher.group(1))
            }
        }
    }

    @CompileDynamic
    private static Map<String, Map<String, ?>> doReport(Plugin plugin, int index, Map<String, String> pluginMetadata) {
        Map<String, ?> map = [:]

        map.id = pluginMetadata[plugin.class.name]
        map.implementationClass = plugin.class.name
        if (plugin instanceof KordampPlugin) {
            map.enabled = plugin.enabled
        }

        [('plugin ' + index): map]
    }

    private static Map<String, ?> maven(MavenArtifactRepository repository) {
        Map<String, ?> map = [type: 'maven']

        if (!isBlank(repository.name)) {
            map.name = repository.name
        }
        map.url = repository.url
        map.artifactUrls = repository.artifactUrls

        map
    }

    private static Map<String, ?> ivy(IvyArtifactRepository repository) {
        Map<String, ?> map = [type: 'ivy']

        if (!isBlank(repository.name)) {
            map.name = repository.name
        }
        map.url = repository.url

        map
    }

    private static Map<String, ?> flatDir(FlatDirectoryArtifactRepository repository) {
        Map<String, ?> map = [type: 'flatDir']

        map.dirs = repository.dirs

        map
    }
}
