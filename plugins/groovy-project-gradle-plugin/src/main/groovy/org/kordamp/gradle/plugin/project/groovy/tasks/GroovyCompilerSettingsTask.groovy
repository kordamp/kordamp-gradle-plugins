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
package org.kordamp.gradle.plugin.project.groovy.tasks

import groovy.transform.CompileStatic
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.compile.GroovyCompile
import org.kordamp.gradle.plugin.base.tasks.AbstractSettingsTask

/**
 * @author Andres Almiray
 * @since 0.30.0
 */
@CompileStatic
class GroovyCompilerSettingsTask extends AbstractSettingsTask {
    @TaskAction
    void report() {
        if (tasks) {
            tasks.each { t ->
                printTask((GroovyCompile) project.tasks.findByName(t))
            }
        } else if (task) {
            try {
                printTask((GroovyCompile) project.tasks.findByName(task))
            } catch (NullPointerException e) {
                throw new IllegalStateException("No matching '${this.task}' task was found")
            }
        } else {
            Set<GroovyCompile> compileTasks = new LinkedHashSet<>(project.tasks.withType(GroovyCompile))
            compileTasks.each { t ->
                printTask(t)
            }
        }
    }

    private void printTask(GroovyCompile task) {
        print(task.name + ':', 0)
        doPrintCollection('includes', task.includes, 1)
        doPrintCollection('excludes', task.excludes, 1)
        doPrintMapEntry('sourceCompatibility', task.sourceCompatibility, 1)
        doPrintMapEntry('targetCompatibility', task.targetCompatibility, 1)
        doPrintMapEntry('destinationDir', task.destinationDir, 1)
        print('groovyOptions:', 1)
        doPrintMapEntry('configurationScript', task.groovyOptions.configurationScript, 2)
        doPrintMapEntry('encoding', task.groovyOptions.encoding, 2)
        doPrintMapEntry('failOnError', task.groovyOptions.failOnError, 2)
        doPrintCollection('fileExtensions', task.groovyOptions.fileExtensions, 2)
        doPrintMapEntry('fork', task.groovyOptions.fork, 2)
        doPrintMapEntry('javaAnnotationProcessing', task.groovyOptions.javaAnnotationProcessing, 2)
        doPrintMapEntry('keepStubs', task.groovyOptions.keepStubs, 2)
        doPrintMapEntry('listFiles', task.groovyOptions.listFiles, 2)
        doPrintMap('optimizationOptions', task.groovyOptions.optimizationOptions, 2)
        doPrintMapEntry('stubDir', task.groovyOptions.stubDir, 2)
        doPrintMapEntry('verbose', task.groovyOptions.verbose, 2)
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
