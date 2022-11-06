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
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option

import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.20.0
 */
@CompileStatic
abstract class AbstractSettingsTask extends AbstractReportingTask {
    @Input @Optional String task
    @Input @Optional Set<String> tasks

    private final Property<Boolean> showPaths = project.objects.property(Boolean)

    @Option(option = 'show-paths', description = 'Display path information (OPTIONAL).')
    void setShowPaths(boolean showPaths) {
        this.showPaths.set(showPaths)
    }

    @Input
    boolean isShowPaths() {
        showPaths.getOrElse(false)
    }

    @Option(option = 'task', description = 'The task to generate the report for.')
    void setTask(String task) {
        this.task = task
    }

    @Option(option = 'tasks', description = 'The tasks to generate the report for.')
    void setTasks(String tasks) {
        if (isNotBlank(tasks)) {
            this.tasks = (tasks.split(',').collect { it.trim() }) as Set
        }
    }
}
