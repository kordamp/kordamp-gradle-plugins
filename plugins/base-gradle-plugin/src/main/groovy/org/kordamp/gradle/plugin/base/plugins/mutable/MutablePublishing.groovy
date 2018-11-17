/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 Andres Almiray.
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
package org.kordamp.gradle.plugin.base.plugins.mutable

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.plugins.Publishing

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
@EqualsAndHashCode(excludes = ['project'])
class MutablePublishing extends AbstractFeature implements Publishing {
    String releasesRepoUrl
    String snapshotsRepoUrl

    MutablePublishing(Project project) {
        super(project)
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    @CompileDynamic
    Map<String, Map<String, Object>> toMap() {
        Map map = [enabled: enabled]

        if (enabled) {
            map.releasesRepoUrl = releasesRepoUrl
            map.snapshotsRepoUrl = snapshotsRepoUrl
        }

        ['publishing': map]
    }

    void copyInto(MutablePublishing copy) {
        super.copyInto(copy)

        copy.@releasesRepoUrl = releasesRepoUrl
        copy.@snapshotsRepoUrl = snapshotsRepoUrl
    }

    static void merge(MutablePublishing o1, MutablePublishing o2) {
        AbstractFeature.merge(o1, o2)
        o1.releasesRepoUrl = o1.@releasesRepoUrl ?: o2.@releasesRepoUrl
        o1.snapshotsRepoUrl = o1.@snapshotsRepoUrl ?: o2.@snapshotsRepoUrl
    }
}
