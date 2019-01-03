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
package org.kordamp.gradle

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Project

/**
 * @author Andres Almiray
 * @since 0.9.0
 */
@CompileStatic
class PluginUtils {
    static boolean isAndroidProject(Project project) {
        androidPlugins().any { project.plugins.hasPlugin(it) }
    }

    /**
     * Returns the sourceSets associated with the project.
     *
     * Return type is {@code SourceSetContainer} for a Java/Groovy/Groovy project or
     * {@code AndroidSourceSet} if the project is Android related.
     */
    @CompileDynamic
    static resolveSourceSets(Project project) {
        if (isAndroidProject(project)) {
            return project.android.sourceSets
        }
        project.sourceSets
    }

    @CompileDynamic
    static resolveSourceSets(Collection<Project> projects) {
        projects.collect { resolveSourceSets(it) }
    }

    @CompileDynamic
    static resolveMainSourceDirs(Project project) {
        if (isAndroidProject(project)) {
            return project.android.sourceSets.main.javaDirectories.flatten()
        }
        project.sourceSets.main.allSource.srcDirs.flatten()
    }

    @CompileDynamic
    static resolveSourceDirs(Project project) {
        if (isAndroidProject(project)) {
            return project.android.sourceSets.javaDirectories.flatten()
        }
        project.sourceSets.collect { it.allSource.srcDirs }.flatten()
    }

    static resolveMainSourceDirs(Collection<Project> projects) {
        projects.collect { resolveMainSourceDirs(it) }.flatten()
    }

    static resolveSourceDirs(Collection<Project> projects) {
        projects.collect { resolveSourceDirs(it) }.flatten()
    }

    private static List<String> androidPlugins() {
        [
            'com.android.library',
            'com.android.feature',
            'com.android.instantapp',
            'com.android.application',
            'com.android.test'
        ]
    }
}
