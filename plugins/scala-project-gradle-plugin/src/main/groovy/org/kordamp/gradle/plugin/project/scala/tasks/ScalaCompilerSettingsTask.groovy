/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2021 Andres Almiray.
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
package org.kordamp.gradle.plugin.project.scala.tasks

import groovy.transform.CompileStatic
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.scala.ScalaCompile
import org.kordamp.gradle.plugin.base.tasks.AbstractSettingsTask

/**
 * @author Andres Almiray
 * @since 0.30.0
 */
@CompileStatic
class ScalaCompilerSettingsTask extends AbstractSettingsTask {
    @TaskAction
    void report() {
        if (tasks) {
            tasks.each { t ->
                printTask((ScalaCompile) project.tasks.findByName(t))
            }
        } else if (task) {
            try {
                printTask((ScalaCompile) project.tasks.findByName(task))
            } catch (NullPointerException e) {
                throw new IllegalStateException("No matching '${this.task}' task was found")
            }
        } else {
            Set<ScalaCompile> compileTasks = new LinkedHashSet<>(project.tasks.withType(ScalaCompile))
            compileTasks.each { t ->
                printTask(t)
            }
        }
    }

    private void printTask(ScalaCompile task) {
        print(task.name + ':', 0)
        doPrintCollection('includes', task.includes, 1)
        doPrintCollection('excludes', task.excludes, 1)
        doPrintMapEntry('sourceCompatibility', task.sourceCompatibility, 1)
        doPrintMapEntry('targetCompatibility', task.targetCompatibility, 1)
        doPrintMapEntry('destinationDir', task.destinationDir, 1)
        if (task.scalaCompileOptions) {
            print('scalaOptions:', 1)
            doPrintMapEntry('additionalParameters', task.scalaCompileOptions.additionalParameters, 2)
            doPrintMapEntry('debugLevel', task.scalaCompileOptions.debugLevel, 2)
            doPrintMapEntry('encoding', task.scalaCompileOptions.encoding, 2)
            doPrintMapEntry('loggingLevel', task.scalaCompileOptions.loggingLevel, 2)
            doPrintCollection('loggingPhases', task.scalaCompileOptions.loggingPhases, 2)
            doPrintMapEntry('deprecation', task.scalaCompileOptions.deprecation, 2)
            doPrintMapEntry('failOnError', task.scalaCompileOptions.failOnError, 2)
            doPrintMapEntry('force', task.scalaCompileOptions.force, 2)
            doPrintMapEntry('listFiles', task.scalaCompileOptions.listFiles, 2)
            doPrintMapEntry('optimize', task.scalaCompileOptions.optimize, 2)
            doPrintMapEntry('unchecked', task.scalaCompileOptions.unchecked, 2)
            print('scalaForkOptions:', 2)
            doPrintCollection('jvmArgs', task.scalaCompileOptions.forkOptions.jvmArgs, 3)
            doPrintMapEntry('memoryInitialSize', task.scalaCompileOptions.forkOptions.memoryInitialSize, 3)
            doPrintMapEntry('memoryMaximumSize', task.scalaCompileOptions.forkOptions.memoryMaximumSize, 3)
        }
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
            doPrintCollection('sourcepath', task.options.sourcepath, 1)
            doPrintCollection('annotationProcessorPath', task.options.annotationProcessorPath, 1)
            doPrintCollection('bootstrapClasspath', task.options.bootstrapClasspath, 1)
            doPrintCollection('classpath', task.classpath, 1)
            doPrintCollection('scalaClasspath', task.scalaClasspath, 1)
            doPrintCollection('zincClasspath', task.zincClasspath, 1)
        }
        println ' '
    }
}
