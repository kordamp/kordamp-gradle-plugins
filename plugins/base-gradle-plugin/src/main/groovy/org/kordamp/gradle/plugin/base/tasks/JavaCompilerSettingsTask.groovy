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
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.compile.JavaCompile

/**
 * @author Andres Almiray
 * @since 0.20.0
 */
@CompileStatic
class JavaCompilerSettingsTask extends AbstractSettingsTask {
    @TaskAction
    void report() {
        if (tasks) {
            tasks.each { t ->
                printTask((JavaCompile) project.tasks.findByName(t))
            }
        } else if (task) {
            printTask((JavaCompile) project.tasks.findByName(task))
        } else {
            project.tasks.withType(JavaCompile).each { t ->
                printTask(t)
            }
        }
    }

    private void printTask(JavaCompile task) {
        print(task.name + ':', 0)
        doPrintCollection('includes', task.includes, 1)
        doPrintCollection('excludes', task.excludes, 1)
        doPrintMapEntry('sourceCompatibility', task.sourceCompatibility, 1)
        doPrintMapEntry('targetCompatibility', task.targetCompatibility, 1)
        print('options:', 1)
        doPrintCollection('compilerArgs', task.options.compilerArgs, 2)
        doPrintMapEntry('debug', task.options.debug, 2)
        print('debugOptions:', 2)
        doPrintMapEntry('debugLevel', task.options.debugOptions.debugLevel, 3)
        doPrintMapEntry('deprecation', task.options.deprecation, 2)
        doPrintMapEntry('encoding', task.options.encoding, 2)
        doPrintMapEntry('extensionDirs', task.options.extensionDirs, 2)
        doPrintMapEntry('failOnError', task.options.failOnError, 2)
        doPrintMapEntry('fork', task.options.fork, 2)
        print('forkOptions:', 2)
        doPrintMapEntry('executable', task.options.forkOptions.executable, 3)
        doPrintMapEntry('javaHome', task.options.forkOptions.javaHome, 3)
        doPrintCollection('jvmArgs', task.options.forkOptions.jvmArgs, 3)
        doPrintMapEntry('memoryInitialSize', task.options.forkOptions.memoryInitialSize, 3)
        doPrintMapEntry('memoryMaximumSize', task.options.forkOptions.memoryMaximumSize, 3)
        doPrintMapEntry('incremental', task.options.incremental, 2)
        doPrintMapEntry('listFiles', task.options.listFiles, 2)
        doPrintMapEntry('verbose', task.options.verbose, 2)
        doPrintMapEntry('warnings', task.options.warnings, 2)
        if (isShowPaths()) {
            doPrintCollection('annotationProcessorPath', task.options.annotationProcessorPath, 1)
            doPrintCollection('bootstrapClasspath', task.options.bootstrapClasspath, 1)
            doPrintCollection('classpath', task.classpath, 1)
            doPrintCollection('sourcepath', task.options.sourcepath, 1)
        }
        println ' '
    }
}
