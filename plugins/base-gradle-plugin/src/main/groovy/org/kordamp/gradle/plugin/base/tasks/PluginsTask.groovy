package org.kordamp.gradle.plugin.base.tasks

import org.gradle.api.Plugin
import org.gradle.api.artifacts.repositories.FlatDirectoryArtifactRepository
import org.gradle.api.artifacts.repositories.IvyArtifactRepository
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.tasks.TaskAction

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.regex.Matcher

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.11.0
 */
class PluginsTask extends AbstractReportingTask {
    @TaskAction
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
                pluginMetadata[props.'implementation-class'] = matcher.group(1)
            }
        }
    }

    private static Map<String, Map<String, Object>> doReport(Plugin plugin, int index, Map<String, String> pluginMetadata) {
        Map<String, Object> map = [:]

        map.id = pluginMetadata[plugin.class.name]
        map.implementationClass = plugin.class

        [('plugin ' + index): map]
    }

    private static Map<String, Object> maven(MavenArtifactRepository repository) {
        Map<String, Object> map = [type: 'maven']

        if (!isBlank(repository.name)) {
            map.name = repository.name
        }
        map.url = repository.url
        map.artifactUrls = repository.artifactUrls

        map
    }

    private static Map<String, Object> ivy(IvyArtifactRepository repository) {
        Map<String, Object> map = [type: 'ivy']

        if (!isBlank(repository.name)) {
            map.name = repository.name
        }
        map.url = repository.url

        map
    }

    private static Map<String, Object> flatDir(FlatDirectoryArtifactRepository repository) {
        Map<String, Object> map = [type: 'flatDir']

        map.dirs = repository.dirs

        map
    }
}
