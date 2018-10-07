/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 the original author or authors.
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
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.gradle.api.Project
import org.gradle.api.Task

import static org.kordamp.gradle.StringUtils.getNaturalName
import static org.kordamp.gradle.StringUtils.getPropertyNameForLowerCaseHyphenSeparatedName

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
@CompileStatic
@Canonical
@EqualsAndHashCode(excludes = ['project'])
@ToString(includeNames = true, excludes = ['project'])
class Stats {
    static final String XML = 'xml'
    static final String HTML = 'html'
    static final String TXT = 'txt'

    boolean enabled = true
    Map<String, String> counters = [:]
    Map<String, Map<String, String>> paths = [:]
    List<String> formats = []

    private final Set<Project> projects = new LinkedHashSet<>()
    private final Set<Task> statsTasks = new LinkedHashSet<>()

    private boolean enabledSet

    private final Project project

    Stats(Project project) {
        this.project = project
        paths.putAll(defaultPaths())
    }

    static Map<String, Map<String, String>> defaultPaths() {
        Map<String, Map<String, String>> basePaths = [:]

        [
            java      : 'Java',
            groovy    : 'Groovy',
            scala     : 'Scala',
            kt        : 'Kotlin',
            js        : 'Javascript',
            css       : 'CSS',
            scss      : 'SASS',
            xml       : 'XML',
            html      : 'HTML',
            fxml      : 'FXML',
            properties: 'Properties',
            sql       : 'SQL',
            yaml      : 'YAML',
            clojure   : 'Clojure'
        ].each { extension, name ->
            ['test', 'integration-test', 'functional-test'].each { source ->
                String classifier = getPropertyNameForLowerCaseHyphenSeparatedName(source)
                basePaths[classifier + extension.capitalize()] = [name: name + ' ' + getNaturalName(classifier) + ' Sources', path: 'src/' + source, extension: extension]
            }
        }

        basePaths.java = [name: 'Java Sources', path: '.*', extension: 'java']
        basePaths.groovy = [name: 'Groovy Sources', path: '.*', extension: 'groovy']
        basePaths.scala = [name: 'Scala Sources', path: '.*', extension: 'scala']
        basePaths.kt = [name: 'Kotlin Sources', path: '.*', extension: 'kt']
        basePaths.js = [name: 'Javascript Sources', path: '.*', extension: 'js']
        basePaths.css = [name: 'CSS Sources', path: '.*', extension: 'css']
        basePaths.scss = [name: 'SASS Sources', path: '.*', extension: 'scss']
        basePaths.xml = [name: 'XML Sources', path: '.*', extension: 'xml']
        basePaths.html = [name: 'HTML Sources', path: '.*', extension: 'html']
        basePaths.fxml = [name: 'FXML Sources', path: '.*', extension: 'fxml']
        basePaths.properties = [name: 'Properties', path: '.*', extension: 'properties']
        basePaths.sql = [name: 'SQL', path: '.*', extension: 'sql']
        basePaths.yaml = [name: 'Yaml', path: '.*', extension: 'yaml']
        basePaths.clj = [name: 'Clojure', path: '.*', extension: 'clj']

        basePaths
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled
        this.enabledSet = true
    }

    boolean isEnabledSet() {
        this.enabledSet
    }

    void copyInto(Stats copy) {
        copy.@enabled = enabled
        copy.@enabledSet = enabledSet
        copy.counters.putAll(counters)
        copy.paths.putAll(paths)
        copy.formats.addAll(formats)
    }

    static void merge(Stats o1, Stats o2) {
        o1.setEnabled((boolean) (o1.enabledSet ? o1.enabled : o2.enabled))
        o1.counters.putAll(o2.counters)
        o1.paths.putAll(o2.paths)
        o1.formats.addAll(o2.formats)
        o1.projects().addAll(o2.projects())
        o1.statsTasks().addAll(o2.statsTasks())
    }

    Set<Project> projects() {
        projects
    }

    Set<Task> statsTasks() {
        statsTasks
    }
}
