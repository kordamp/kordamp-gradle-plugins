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
package org.kordamp.gradle.plugin.base.tasks


import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.tasks.TaskAction
import org.kordamp.gradle.plugin.KordampPlugin

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.regex.Matcher

/**
 * @author Andres Almiray
 * @since 0.11.0
 */
@CompileStatic
class PluginsTask extends AbstractReportingTask {
    @TaskAction
    void report() {
        Map<String, Map<String, Object>> plugins = new LinkedHashMap<String, Map<String, Object>>()

        Map<String, String> pluginMetadata = new LinkedHashMap<String, String>()
        Enumeration<URL> e = PluginsTask.classLoader.getResources('META-INF/gradle-plugins')
        while (e.hasMoreElements()) {
            extractMetadata(e.nextElement(), pluginMetadata)
        }
        e = org.gradle.api.plugins.BasePlugin.classLoader.getResources('META-INF/gradle-plugins')
        while (e.hasMoreElements()) {
            extractMetadata(e.nextElement(), pluginMetadata)
        }

        project.plugins.eachWithIndex { plugin, index -> plugins.putAll(PluginsTask.doReport(plugin, index, pluginMetadata)) }

        println('Total plugins: ' + console.cyan((plugins.size()).toString()) + '\n')
        doPrint(plugins, 0)
    }

    private void extractMetadata(URL url, Map<String, String> pluginMetadata) {
        if (url.protocol != 'jar') return

        JarFile jarFile = new JarFile(url.toString()[9..url.toString().indexOf('!') - 1])
        Enumeration<JarEntry> entries = jarFile.entries()
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement()
            Matcher matcher = (entry.name =~ /META-INF\/gradle-plugins\/(.+)\.properties/)
            if (matcher.matches()) {
                Properties props = new Properties()
                props.load(jarFile.getInputStream(entry))
                pluginMetadata.put((String) props.'implementation-class', matcher.group(1))
            }
        }
    }

    private static Map<String, Map<String, Object>> doReport(Plugin plugin, int index, Map<String, String> pluginMetadata) {
        Map<String, Object> map = new LinkedHashMap<>()

        map.id = (pluginMetadata[plugin.class.name] ?: plugin.class.name) - 'org.gradle.'
        map.implementationClass = plugin.class.name
        if (plugin instanceof KordampPlugin) {
            map.enabled = plugin.enabled
        }

        new LinkedHashMap<>([('plugin ' + index): map])
    }
}
