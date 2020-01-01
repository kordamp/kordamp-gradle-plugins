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
package org.kordamp.gradle.plugin.base.tasks

import groovy.transform.CompileStatic
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskDependency
import org.gradle.api.tasks.options.Option

/**
 * @author Andres Almiray
 * @since 0.30.0
 */
@CompileStatic
class TaskSettingsTask extends AbstractReportingTask {
    @Input
    String task

    @Option(option = 'task', description = 'The task to generate the report for.')
    void setTask(String task) {
        this.task = task
    }

    @TaskAction
    void report() {
        if (task) {
            try {
                printTask(project.tasks.findByName(task))
            } catch (NullPointerException e) {
                throw new IllegalStateException("No matching '${this.task}' task was found")
            }
        }
    }

    private void printTask(Task task) {
        print(task.name + ':', 0)
        task.properties.sort().each { name, value ->
            if (value instanceof Property ||
                    value instanceof Provider ||
                    name in ['name']) {
                return
            }
            if (value instanceof FileCollection) {
                doPrintCollection(name.toString(), value, 1)
            } else if (value instanceof Collection) {
                doPrintCollection(name.toString(), (Collection) value, 1)
            } else if (value instanceof TaskDependency) {
                TaskDependency td = (TaskDependency) value
                doPrintCollection(name.toString(), td.getDependencies(task), 1)
            } else {
                doPrintMapEntry(name.toString(), value, 1)
            }
        }

        println ' '
    }
}
