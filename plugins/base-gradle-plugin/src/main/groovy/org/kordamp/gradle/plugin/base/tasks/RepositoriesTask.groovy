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
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.artifacts.repositories.FlatDirectoryArtifactRepository
import org.gradle.api.artifacts.repositories.IvyArtifactRepository
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.tasks.TaskAction

import static org.kordamp.gradle.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.11.0
 */
@CompileStatic
class RepositoriesTask extends AbstractReportingTask {
    @TaskAction
    void report() {
        Map<String, Map<String, ?>> repositories = [:]

        project.repositories.eachWithIndex { repository, index -> repositories.putAll(RepositoriesTask.doReport(repository, index)) }

        doPrint(repositories, 0)
    }

    @CompileStatic
    private static Map<String, Map<String, ?>> doReport(ArtifactRepository repository, int index) {
        Map<String, ?> map = [:]

        if (repository instanceof MavenArtifactRepository) {
            map = maven((MavenArtifactRepository) repository)
        } else if (repository instanceof IvyArtifactRepository) {
            map = ivy((IvyArtifactRepository) repository)
        } else if (repository instanceof FlatDirectoryArtifactRepository) {
            map = flatDir((FlatDirectoryArtifactRepository) repository)
        }

        [('repository ' + index): map]
    }

    private static Map<String, ?> maven(MavenArtifactRepository repository) {
        Map<String, ?> map = [type: 'maven']

        if (isNotBlank(repository.name)) {
            map.name = repository.name
        }
        map.url = repository.url
        map.artifactUrls = repository.artifactUrls

        map
    }

    private static Map<String, ?> ivy(IvyArtifactRepository repository) {
        Map<String, ?> map = [type: 'ivy']

        if (isNotBlank(repository.name)) {
            map.name = repository.name
        }
        map.url = repository.url

        map
    }

    private static Map<String, ?> flatDir(FlatDirectoryArtifactRepository repository) {
        Map<String, ?> map = [type: 'flatDir']

        if (isNotBlank(repository.name)) {
            map.name = repository.name
        }
        map.dirs = repository.dirs

        map
    }
}
