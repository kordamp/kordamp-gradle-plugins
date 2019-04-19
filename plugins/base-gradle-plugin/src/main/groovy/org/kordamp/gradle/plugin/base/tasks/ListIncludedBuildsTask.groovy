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
import org.gradle.api.initialization.IncludedBuild
import org.gradle.api.tasks.TaskAction
import org.kordamp.gradle.AnsiConsole

/**
 * @author Andres Almiray
 * @since 0.18.0
 */
@CompileStatic
class ListIncludedBuildsTask extends AbstractReportingTask {
    @TaskAction
    void report() {
        if (project != project.rootProject) return

        Project rootProject = project.rootProject

        AnsiConsole console = new AnsiConsole(rootProject)
        println('Total included builds: ' + console.cyan((rootProject.gradle.includedBuilds.size()).toString()) + '\n')
        rootProject.gradle.includedBuilds.each { printIncludedBuild(console, it) }
    }

    private void printIncludedBuild(AnsiConsole console, IncludedBuild build) {
        println(build.name + ':')

        Map<String, String> props = [:]
        props.putAll([
            projectDir: build.projectDir.toString()
        ])
        doPrintMap(console, props, 1)
        println(' ')
    }
}
