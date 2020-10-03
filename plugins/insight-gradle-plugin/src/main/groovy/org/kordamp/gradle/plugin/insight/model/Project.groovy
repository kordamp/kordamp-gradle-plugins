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
package org.kordamp.gradle.plugin.insight.model

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.Sortable
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
class Project {
    final String name
    final String path
    long startEvaluate
    long endEvaluate
    final Map<String, Task> tasks = new LinkedHashMap<>()
    final Set<String> tasksToBeExecuted = new LinkedHashSet<>()

    private final org.gradle.api.Project gradleProject
    private double execDuration = -1d

    Project(String path, String name, org.gradle.api.Project gradleProject) {
        this.path = path
        this.name = name
        this.gradleProject = gradleProject
    }

    double getConfDuration() {
        Math.max(0, endEvaluate - startEvaluate) / 1000d
    }

    double getExecDuration() {
        if (execDuration < 0) {
            execDuration = calculateExecDuration()
        }
        execDuration
    }

    enum State {
        SUCCESS, PARTIAL, FAILURE, SKIPPED, HIDDEN
    }

    State getState() {
        if (tasksToBeExecuted.size() == 0 && tasks.size() == 0) {
            return State.HIDDEN
        }

        if (gradleProject.state.failure || tasks.values().any { it.state == Task.State.FAILURE }) {
            return State.FAILURE
        }

        if (tasksToBeExecuted.size() != tasks.size()) {
            return tasks.size() == 0 ? State.SKIPPED : State.PARTIAL
        }

        if (gradleProject.state.executed || tasks.values().every { it.state == Task.State.SUCCESS }) {
            return State.SUCCESS
        }

        return State.SKIPPED
    }

    @CompileDynamic
    private double calculateExecDuration() {
        Set<TimeSlot> slots = new TreeSet<>()
        tasks.values().each { task ->
            TimeSlot slot = new TimeSlot(start: task.beforeExecute, end: task.afterExecute)
            TimeSlot existing = slots.find({ s -> s.intersects(slot) })
            if (existing) {
                existing.expand(slot)
            } else {
                slots << slot
            }
        }

        slots ? (Double) slots.sum { it.duration } : 0d
    }

    @CompileStatic
    @Canonical
    @Sortable(includes = ['start'])
    private static class TimeSlot {
        long start = Long.MAX_VALUE
        long end = Long.MIN_VALUE

        boolean intersects(TimeSlot other) {
            other.start <= end && start <= other.end
        }

        void expand(TimeSlot other) {
            start = Math.min(start, other.start)
            end = Math.max(end, other.end)
        }

        double getDuration() {
            Math.max(0, end - start) / 1000d
        }
    }
}
