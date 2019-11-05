/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
package org.kordamp.gradle.plugin.kotlindoc

import groovy.transform.CompileStatic
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.kordamp.gradle.plugin.base.tasks.AbstractSettingsTask

/**
 * @author Andres Almiray
 * @since 0.30.0
 */
@CompileStatic
class KotlinCompilerSettingsTask extends AbstractSettingsTask {
    @TaskAction
    void report() {
        if (tasks) {
            tasks.each { t ->
                printTask((KotlinCompile) project.tasks.findByName(t))
            }
        } else if (task) {
            try {
                printTask((KotlinCompile) project.tasks.findByName(task))
            } catch (NullPointerException e) {
                throw new IllegalStateException("No matching '${this.task}' task was found")
            }
        } else {
            Set<KotlinCompile> compileTasks = new LinkedHashSet<>(project.tasks.withType(KotlinCompile))
            compileTasks.each { t ->
                printTask(t)
            }
        }
    }

    private void printTask(KotlinCompile task) {
        print(task.name + ':', 0)
        doPrintCollection('includes', task.includes, 1)
        doPrintCollection('excludes', task.excludes, 1)
        doPrintMapEntry('sourceCompatibility', task.sourceCompatibility, 1)
        doPrintMapEntry('targetCompatibility', task.targetCompatibility, 1)
        doPrintMapEntry('destinationDir', task.destinationDir, 1)
        doPrintMapEntry('javaPackagePrefix', task.javaPackagePrefix, 1)
        doPrintMapEntry('usePreciseJavaTracking', task.usePreciseJavaTracking, 1)
        doPrintMapEntry('incremental', task.incremental, 1)
        doPrintCollection('sourceFilesExtensions', task.sourceFilesExtensions, 1)
        if (task.kotlinOptions) {
            print('kotlinOptions:', 1)
            doPrintMapEntry('allWarningsAsErrors', task.kotlinOptions.allWarningsAsErrors, 2)
            doPrintCollection('freeCompilerArgs', task.kotlinOptions.freeCompilerArgs, 2)
            doPrintMapEntry('suppressWarnings', task.kotlinOptions.suppressWarnings, 2)
            doPrintMapEntry('verbose', task.kotlinOptions.verbose, 2)
            doPrintMapEntry('languageVersion', task.kotlinOptions.languageVersion, 2)
            doPrintMapEntry('javaParameters', task.kotlinOptions.javaParameters, 2)
            doPrintMapEntry('jdkHome', task.kotlinOptions.jdkHome, 2)
            doPrintMapEntry('jvmTarget', task.kotlinOptions.jvmTarget, 2)
            doPrintMapEntry('noJdk', task.kotlinOptions.noJdk, 2)
            doPrintMapEntry('noReflect', task.kotlinOptions.noReflect, 2)
            doPrintMapEntry('noStdlib', task.kotlinOptions.noStdlib, 2)
        }
        if (isShowPaths()) {
            doPrintCollection('classpath', task.classpath, 1)
            doPrintCollection('pluginClasspath', task.pluginClasspath, 1)
        }
        println ' '
    }
}
