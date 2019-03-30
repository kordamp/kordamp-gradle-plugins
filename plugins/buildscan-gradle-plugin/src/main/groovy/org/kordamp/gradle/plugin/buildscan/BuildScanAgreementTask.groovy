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
package org.kordamp.gradle.plugin.buildscan

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

import static org.kordamp.gradle.plugin.buildscan.BuildScanPlugin.resolveGlobalScanFile
import static org.kordamp.gradle.plugin.buildscan.BuildScanPlugin.resolveProjectScanFile

/**
 * @author Andres Almiray
 * @since 0.16.0
 */
@CompileStatic
class BuildScanAgreementTask extends DefaultTask {
    @Input
    boolean remove = false

    private enum Location {
        PROJECT,
        GLOBAL
    }

    private Location location
    private String agree

    @Option(option = 'location', description = "The location of the agreement file. Valid values are 'project', 'global'")
    void setLocation(String location) {
        try {
            this.location = Location.valueOf(location.toUpperCase())
        } catch (Exception e) {
            project.logger.warn("Invalid build scan location '${location}")
        }
    }

    @Option(option = 'agree', description = "The value of the agreement. Must be either 'yes' or 'no' (without quotes)")
    void setAgree(String agree) {
        if ('yes'.equalsIgnoreCase(agree) || 'no'.equalsIgnoreCase(agree)) {
            this.agree = agree.toLowerCase()
        } else {
            throw new IllegalArgumentException("Invalid agreement value '${agree}'. Must be either 'yes' or 'no' (without quotes)")
        }
    }

    @TaskAction
    void processAgreement() {
        // validation
        if (!location) {
            throw new IllegalStateException("No location has been specified. Use --location when invoking this task.")
        }

        if (!remove) {
            if (!agree) {
                throw new IllegalStateException("No agreement has been set. Use --agree when invoking this tast.")
            }
        }

        File scanFile = resolveProjectScanFile(project)
        if (location == Location.GLOBAL) {
            scanFile = resolveGlobalScanFile(project)
        }

        if (remove) {
            println "Removing $scanFile.absolutePath"
            scanFile.delete()
            scanFile.deleteOnExit()
        } else {
            println "Setting build scan agreement to '$agree' at $scanFile.absolutePath"
            scanFile.parentFile.mkdirs()
            scanFile.text = agree
        }
    }
}
