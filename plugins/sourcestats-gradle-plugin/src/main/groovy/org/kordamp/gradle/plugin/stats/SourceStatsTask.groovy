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
package org.kordamp.gradle.plugin.stats

import groovy.transform.CompileStatic
import groovy.xml.MarkupBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.stats.counter.CssCounter
import org.kordamp.gradle.plugin.stats.counter.HashCounter
import org.kordamp.gradle.plugin.stats.counter.JavaCounter
import org.kordamp.gradle.plugin.stats.counter.PropertiesCounter
import org.kordamp.gradle.plugin.stats.counter.SemiColonCounter
import org.kordamp.gradle.plugin.stats.counter.SqlCounter
import org.kordamp.gradle.plugin.stats.counter.XmlCounter
import org.kordamp.gradle.util.PluginUtils

import static org.kordamp.gradle.plugin.base.plugins.Stats.HTML
import static org.kordamp.gradle.plugin.base.plugins.Stats.TXT
import static org.kordamp.gradle.plugin.base.plugins.Stats.XML
import static org.kordamp.gradle.util.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.util.StringUtils.getFilenameExtension

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
@CacheableTask
class SourceStatsTask extends DefaultTask {
    @Optional @Input Map<String, String> counters = [:]
    @Optional @Input Map<String, Map<String, String>> paths = [:]
    @Optional @Input List<String> formats = []

    @PathSensitive(PathSensitivity.RELATIVE)
    @Optional @InputFile File reportDir

    @Internal
    File xmlReport

    @Internal
    int totalFiles = 0
    @Internal
    int totalLOC = 0

    @TaskAction
    void computeLoc() {
        if (!reportDir) reportDir = project.file("${project.reporting.baseDir.path}/stats")
        if (!xmlReport) xmlReport = project.file("${reportDir}/${project.name}.xml")
        reportDir.mkdirs()

        Map<String, Counter> counterInstances = resolveCounterInstances()

        ProjectConfigurationExtension config = resolveEffectiveConfig(project)

        Map<String, Map<String, String>> merged = [:]
        merged.putAll(config.stats.paths)
        // deep copy
        paths.each { key, val ->
            Map<String, String> map = [:]
            map.putAll(val)
            merged.put(key, map)
        }

        PluginUtils.resolveSourceDirs(project).each { File dir ->
            if (!dir.exists()) return
            dir.eachFileRecurse { File file ->
                if (file.file) {
                    String extension = getFilenameExtension(file.name)
                    Map map = merged.find { file.absolutePath =~ it.value.path && !it.value.extension }?.value
                    if (!map) map = merged.find {
                        file.absolutePath =~ it.value.path && extension == it.value.extension
                    }?.value
                    if (!map) map = merged.find { file.absolutePath =~ it.value.path }?.value
                    if (!map || (map.extension && extension != map.extension)) return
                    if (counterInstances.containsKey(extension)) {
                        SourceStatsTask.countLines(map, counterInstances[extension], file)
                    }
                }
            }
        }

        List<String> toBeRemoved = []
        merged.each { type, info ->
            if (info.files && info.lines) {
                totalFiles += info.files
                totalLOC += info.lines
            } else {
                toBeRemoved << type
            }
        }
        toBeRemoved.each { merged.remove(it) }

        if (totalFiles) {
            int max = 0
            merged.values().each { if (it.files) max = Math.max(max, it.name.size()) }
            max = Math.max(max, 22)
            merged = merged.sort { it.value.name }

            output(merged, max, totalFiles.toString(), totalLOC.toString(), new PrintWriter(System.out))
            xmlOutput(merged, totalFiles.toString(), totalLOC.toString())
            if (HTML in formats) htmlOutput(merged, totalFiles.toString(), totalLOC.toString())
            if (TXT in formats) output(merged, max, totalFiles.toString(), totalLOC.toString(), new PrintWriter(getOutputFile(TXT)))
        }
    }

    private static void countLines(Map<String, Object> work, Counter counter, File file) {
        int numFiles = work.get('files', 0)
        work.files = ++numFiles
        int lines = counter.count(file)
        int numLines = work.get('lines', 0)
        work.lines = numLines + lines
    }

    @CompileStatic
    private Map<String, Counter> resolveCounterInstances() {
        Map<String, Counter> instances = [:]
        counters.collect { key, classname ->
            instances[key] = (Counter) Class.forName(classname, true, SourceStatsTask.classLoader).newInstance()
        }

        if (!instances.java) instances.java = new JavaCounter()
        if (!instances.groovy) instances.groovy = new JavaCounter()
        if (!instances.js) instances.js = new JavaCounter()
        if (!instances.scala) instances.scala = new JavaCounter()
        if (!instances.kt) instances.kt = new JavaCounter()
        if (!instances.kts) instances.kt = new JavaCounter()
        if (!instances.css) instances.css = new CssCounter()
        if (!instances.scss) instances.scss = new JavaCounter()
        if (!instances.xml) instances.xml = new XmlCounter()
        if (!instances.html) instances.html = new XmlCounter()
        if (!instances.fxml) instances.fxml = new XmlCounter()
        if (!instances.sql) instances.sql = new SqlCounter()
        if (!instances.yaml) instances.yaml = new HashCounter()
        if (!instances.clj) instances.clj = new SemiColonCounter()
        if (!instances.get('properties')) instances.put('properties', new PropertiesCounter())

        instances
    }

    private void output(Map<String, Map<String, Object>> work, int max, String totalFiles, String totalLOC, Writer out) {
        int padFiles = Math.max(totalFiles.toString().length(), 6)
        int padLocs = Math.max(totalLOC.toString().length(), 6)

        out.println '    +-' + ('-' * max) + '-+--------+--------+'
        out.println '    | ' + 'Name'.padRight(max, ' ') + ' | ' +
            'Files'.padLeft(padFiles, ' ') + ' | ' +
            'LOC'.padLeft(padLocs, ' ') + ' |'
        out.println '    +-' + ('-' * max) + '-+--------+--------+'

        work.each { type, info ->
            if (info.files) {
                out.println '    | ' +
                    info.name.padRight(max, ' ') + ' | ' +
                    info.files.toString().padLeft(padFiles, ' ') + ' | ' +
                    info.lines.toString().padLeft(padLocs, ' ') + ' |'
            }
        }

        out.println '    +-' + ('-' * max) + '-+--------+--------+'
        out.println '    | ' + 'Totals'.padRight(max, ' ') + ' | ' + totalFiles.padLeft(padFiles, ' ') + ' | ' + totalLOC.padLeft(padLocs, ' ') + ' |'
        out.println '    +-' + ('-' * max) + '-+--------+--------+\n'

        out.flush()
    }

    private void xmlOutput(Map<String, Map<String, Object>> work, String totalFiles, String totalLOC) {
        new MarkupBuilder(new FileWriter(getOutputFile(XML))).stats {
            work.each { type, info ->
                if (info.files) {
                    category(name: info.name) {
                        fileCount(info.files.toString())
                        loc(info.lines.toString())
                    }
                }
            }
            category {
                name('Total')
                fileCount(totalFiles)
                loc(totalLOC)
            }
        }
    }

    private void htmlOutput(Map<String, Map<String, Object>> work, String totalFiles, String totalLOC) {
        int i = 0
        new MarkupBuilder(new FileWriter(getOutputFile(HTML))).html {
            table(border: 1) {
                tr {
                    th('Name')
                    th(align: 'right', 'Files')
                    th(align: 'right', 'LOC')
                }
                work.each { type, info ->
                    if (info.files) {
                        tr(style: (i++) % 2 ? 'background-color:lightblue' : 'background-color:FFF') {
                            td(info.name)
                            td(align: 'right', info.files.toString())
                            td(align: 'right', info.lines.toString())
                        }
                    }
                }
                tr(style: 'background-color:lightgreen') {
                    b {
                        td('Total')
                        td(align: 'right', totalFiles)
                        td(align: 'right', totalLOC)
                    }
                }
            }
        }
    }

    @CompileStatic
    private getOutputFile(String suffix) {
        reportDir.mkdirs()
        new File(reportDir, project.name + '.' + suffix)
    }
}
