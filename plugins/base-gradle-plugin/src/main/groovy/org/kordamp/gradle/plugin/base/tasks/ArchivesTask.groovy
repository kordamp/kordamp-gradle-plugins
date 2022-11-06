/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2022 Andres Almiray.
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
package org.kordamp.gradle.plugin.base.tasks

import groovy.transform.CompileStatic
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.artifacts.PublishArtifactSet
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.artifacts.repositories.FlatDirectoryArtifactRepository
import org.gradle.api.artifacts.repositories.IvyArtifactRepository
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.tasks.TaskAction

import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.43.0
 */
@CompileStatic
class ArchivesTask extends AbstractReportingTask {
    @TaskAction
    void report() {
        Map<String, Map<String, ?>> repositories = [:]

        PublishArtifactSet artifacts = project.configurations.findByName('archives').allArtifacts

        println('Total archives: ' + console.cyan((artifacts.size()).toString()) + '\n')
        doPrintArtifacts(artifacts.sort { it.name }, 0)
    }

    private void doPrintArtifacts(Collection<PublishArtifact> artifacts, int offset) {
        artifacts.eachWithIndex { PublishArtifact artifact, int i ->
            doPrint("artifact ${i}:", offset)
            doPrintMap([
                name: artifact.name,
                type: artifact.type,
                extension: artifact.extension,
                classifier: artifact.classifier,
                date: artifact.date,
                file: artifact.file
            ], offset+1)
        }
    }
}
