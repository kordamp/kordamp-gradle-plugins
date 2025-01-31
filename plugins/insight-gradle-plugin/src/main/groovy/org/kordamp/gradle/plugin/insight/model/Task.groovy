/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2025 Andres Almiray.
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
package org.kordamp.gradle.plugin.insight.model

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 *
 * @author Andres Almiray
 * @since 0.40.0
 */
@CompileStatic
@Canonical
@ToString(includeNames = true)
@EqualsAndHashCode(includes = ['name', 'path'])
class Task {
    final String name
    final String path

    boolean cacheable

    long beforeExecute
    long afterExecute

    boolean executed
    boolean skipped
    boolean upToDate
    boolean didWork
    boolean noSource
    boolean fromCache
    boolean failed
    boolean actionable

    Task(String path, String name) {
        this.path = path
        this.name = name
    }

    double getExecDuration() {
        Math.max(0, afterExecute - beforeExecute) / 1000d
    }

    enum State {
        SUCCESS, FAILURE, SKIPPED
    }

    State getState() {
        if (failed) return State.FAILURE
        if (upToDate || fromCache || didWork || executed) return State.SUCCESS
        return State.SKIPPED
    }
}
