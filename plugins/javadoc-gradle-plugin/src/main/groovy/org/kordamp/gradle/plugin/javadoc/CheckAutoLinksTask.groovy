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
package org.kordamp.gradle.plugin.javadoc

import groovy.transform.CompileStatic
import org.gradle.api.tasks.TaskAction
import org.kordamp.gradle.AnsiConsole
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.tasks.AbstractReportingTask

import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig

/**
 * @author Andres Almiray
 * @since 0.17.0
 */
@CompileStatic
class CheckAutoLinksTask extends AbstractReportingTask {
    private static final boolean WINDOWS = System.getProperty('os.name').startsWith('Windows')
    private static final String SUCCESS = 'SUCCESS'
    private static final String REDIRECT = 'REDIRECT'
    private static final String ERROR = 'ERROR'

    @TaskAction
    void checkAutoLinks() {
        ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)

        List<String> links = effectiveConfig.javadoc.autoLinks.resolveLinks(project)
        if (!links) {
            println "There are no links to be checked."
            return
        }

        Map<String, Integer> counts = new LinkedHashMap<>()
        counts.put(SUCCESS, 0)
        counts.put(REDIRECT, 0)
        counts.put(ERROR, 0)

        AnsiConsole console = new AnsiConsole(project)
        for (String link : links) {
            try {
                URL url = new URL(link + 'package-list')
                HttpURLConnection con = (HttpURLConnection) url.openConnection()
                con.requestMethod = 'HEAD'
                con.connectTimeout = 60 * 1000
                con.connect()
                println(parseResponseCode(console, counts, con.responseCode) + ' ' + link)
            } catch (IOException ioe) {
                println(parseResponseCode(console, counts, 500) + ' ' + link)
            }
        }

        String str = console.erase("\nChecked ${links.size()}/")
        str += "${console.green(String.valueOf(counts.get(SUCCESS)))}/"
        str += "${console.red(String.valueOf(counts.get(ERROR)))}/"
        str += "${console.yellow(String.valueOf(counts.get(REDIRECT)))} "
        println str
    }

    private String parseResponseCode(AnsiConsole console, Map<String, Integer> counts, int code) {
        if (code < 300) {
            counts.put(SUCCESS, counts.get(SUCCESS) + 1)
            return console.green(WINDOWS ? '√' : '✔')
        } else if (code < 400) {
            counts.put(REDIRECT, counts.get(REDIRECT) + 1)
            return console.yellow('!')
        } else {
            counts.put(ERROR, counts.get(ERROR) + 1)
            return console.red(WINDOWS ? 'X' : '✘')
        }
    }
}
