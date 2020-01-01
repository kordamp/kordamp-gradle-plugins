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
package org.kordamp.gradle.plugin.buildscan

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.kordamp.gradle.AnsiConsole

import static org.kordamp.gradle.plugin.buildscan.BuildScanPlugin.resolveGlobalScanFile
import static org.kordamp.gradle.plugin.buildscan.BuildScanPlugin.resolveProjectScanFile

/**
 * @author Andres Almiray
 * @since 0.16.0
 */
@CompileStatic
@Deprecated
class ListBuildScanAgreementTask extends DefaultTask {
    @TaskAction
    void processAgreement() {
        println "Project: ${parseAgreement(resolveProjectScanFile(project))}"
        println "Global:  ${parseAgreement(resolveGlobalScanFile(project))}"
    }

    private String parseAgreement(File file) {
        AnsiConsole console = new AnsiConsole(project)

        if (file?.exists()) {
            String s = file.text
            if ('yes'.equalsIgnoreCase(s)) {
                return console.green(s)
            } else if ('no'.equalsIgnoreCase(s)) {
                return console.red(s)
            } else {
                return console.yellow('invalid')
            }
        } else {
            console.yellow('unset')
        }
    }
}
