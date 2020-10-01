/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2020 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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
package org.kordamp.gradle.plugin.insight.model

import groovy.transform.Canonical
import groovy.transform.CompileStatic

import java.time.ZonedDateTime

/**
 *
 * @author Andres Almiray
 * @since 0.40.0
 */
@Canonical
@CompileStatic
class Build {
    final long startTime = System.currentTimeMillis()
    final Map<String, Project> projects = new LinkedHashMap<>()
    long endTime
    long settingsEvaluated
    long projectsLoaded
    long projectsEvaluated
    long graphPopulated
    boolean failure
    ZonedDateTime start = ZonedDateTime.now()
    ZonedDateTime end
    String rootProjectName

    void setEnd(ZonedDateTime e) {
        this.end = e
        endTime = e.toInstant().toEpochMilli()
    }

    double getBuildDuration() {
        Math.max(0, endTime - startTime) / 1000d
    }

    double getProjectLoadDuration() {
        Math.max(0, projectsLoaded - settingsEvaluated) / 1000d
    }

    double getProjectEvalDuration() {
        Math.max(0, projectsEvaluated - projectsLoaded) / 1000d
    }
}
