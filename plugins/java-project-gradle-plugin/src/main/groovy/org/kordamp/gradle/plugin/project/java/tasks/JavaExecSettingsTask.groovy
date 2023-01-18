/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2023 Andres Almiray.
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
package org.kordamp.gradle.plugin.project.java.tasks

import groovy.transform.CompileStatic
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.kordamp.gradle.plugin.base.tasks.AbstractReportingTask

/**
 * @author Andres Almiray
 * @since 0.30.0
 */
@CompileStatic
class JavaExecSettingsTask extends AbstractReportingTask {
    @Input
    @Optional
    String task

    private final Property<Boolean> showPaths = project.objects.property(Boolean)
    private final Property<Boolean> showEnvironment = project.objects.property(Boolean)
    private final Property<Boolean> showSystemProperties = project.objects.property(Boolean)

    @Option(option = 'show-paths', description = 'Display path information (OPTIONAL).')
    void setShowPaths(boolean showPaths) {
        this.showPaths.set(showPaths)
    }

    @Input
    boolean isShowPaths() {
        showPaths.getOrElse(false)
    }

    @Option(option = 'show-environment', description = 'Display environment information (OPTIONAL).')
    void setShowEnvironment(boolean showEnvironment) {
        this.showEnvironment.set(showEnvironment)
    }

    @Input
    boolean isShowEnvironment() {
        showEnvironment.getOrElse(false)
    }

    @Option(option = 'show-system-properties', description = 'Display system properties (OPTIONAL).')
    void setShowSystemProperties(boolean showSystemProperties) {
        this.showSystemProperties.set(showSystemProperties)
    }

    @Input
    boolean isShowSystemProperties() {
        showSystemProperties.getOrElse(false)
    }

    @Option(option = 'task', description = 'The task to generate the report for.')
    void setTask(String task) {
        this.task = task
    }

    @TaskAction
    void report() {
        if (task) {
            try {
                printTask((JavaExec) project.tasks.findByName(task))
            } catch (NullPointerException e) {
                throw new IllegalStateException("No matching '${this.task}' task was found")
            }
        } else {
            Set<JavaExec> testTasks = new LinkedHashSet<>(project.tasks.withType(JavaExec))
            testTasks.each { t ->
                printTask(t)
            }
        }
    }

    private void printTask(JavaExec task) {
        print(task.name + ':', 0)
        doPrintMapEntry('main', task.main, 1)
        doPrintMapEntry('executable', task.executable, 1)
        doPrintMapEntry('minHeapSize', task.minHeapSize, 1)
        doPrintMapEntry('maxHeapSize', task.maxHeapSize, 1)
        doPrintMapEntry('debug', task.debug, 1)
        doPrintMapEntry('ignoreExitValue', task.ignoreExitValue, 1)
        doPrintMapEntry('workingDir', task.workingDir, 1)
        doPrintMapEntry('javaVersion', task.javaVersion, 1)
        doPrintMapEntry('defaultCharacterEncoding', task.defaultCharacterEncoding, 1)
        doPrintCollection('args', task.args, 1)
        doPrintCollection('jvmArgs', task.jvmArgs, 1)
        doPrintCollection('allJvmArgs', task.allJvmArgs, 1)
        doPrintCollection('commandLine', task.commandLine, 1)

        if (isShowEnvironment()) {
            doPrintMap('environment', task.environment.sort(), 1)
        }

        if (isShowSystemProperties()) {
            doPrintMap('systemProperties', task.systemProperties.sort(), 1)
        }

        if (isShowPaths()) {
            doPrintCollection('bootstrapClasspath', task.bootstrapClasspath, 1)
            doPrintCollection('classpath', task.classpath, 1)
        }

        println ' '
    }
}
