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
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
class Source extends AbstractFeature {
    private final Set<Project> projects = new LinkedHashSet<>()
    private final Set<TaskProvider<Jar>> sourceTasks = new LinkedHashSet<>()
    private final Set<Project> excludedProjects = new LinkedHashSet<>()

    Source(ProjectConfigurationExtension config, Project project) {
        super(config, project)
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    @CompileDynamic
    Map<String, Map<String, Object>> toMap() {
        Map map = [enabled: enabled]

        if (isRoot()) {
            if (enabled) {
                map.excludedProjects = excludedProjects
            }
        }

        ['source': map]
    }

    static void merge(Source o1, Source o2) {
        AbstractFeature.merge(o1, o2)
        o1.projects().addAll(o2.projects())
        o1.sourceTasks().addAll(o2.sourceTasks())
        o1.excludedProjects().addAll(o2.excludedProjects())
    }

    Set<Project> excludedProjects() {
        excludedProjects
    }

    Set<Project> projects() {
        projects
    }

    Set<TaskProvider<Jar>> sourceTasks() {
        sourceTasks
    }
}
