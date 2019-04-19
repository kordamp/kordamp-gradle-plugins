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

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.kordamp.gradle.AnsiConsole

/**
 * @author Andres Almiray
 * @since 0.18.0
 */
@CompileStatic
class ListProjectsTask extends AbstractReportingTask {
    private boolean absolute

    @Option(option = 'absolute', description = "Should paths be printed as absolutes or not. Defaults to 'false'")
    void setAbsolute(boolean absolute) {
        this.absolute = absolute
    }

    @TaskAction
    void report() {
        Project rootProject = project.rootProject

        AnsiConsole console = new AnsiConsole(project)
        println('Total projects: ' + console.cyan((rootProject.childProjects.size() + 1).toString()) + '\n')
        printProject(console, rootProject, true)
        rootProject.childProjects.values().each { printProject(console, it, false) }
    }

    private void printProject(AnsiConsole console, Project project, boolean isRoot) {
        println(project.name + ':')

        Map<String, String> props = [:]
        if (isRoot) props['root'] = 'true'
        props.putAll([
            path      : project.path,
            projectDir: adjustPath(isRoot, project.rootProject.projectDir.toString(), project.projectDir.toString()),
            buildFile : adjustPath(isRoot, project.rootProject.projectDir.toString(), project.buildFile.absolutePath),
            buildDir  : adjustPath(isRoot, project.rootProject.projectDir.toString(), project.buildDir.toString())
        ])
        doPrintMap(console, props, 1)
        println(' ')
    }

    private String adjustPath(boolean isRoot, String rootDir, String path) {
        if (!isRoot && !absolute) {
            return (path - rootDir)[1..-1]
        }
        return path
    }
}
