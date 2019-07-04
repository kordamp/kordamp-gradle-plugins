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
package org.kordamp.gradle.plugin.base.tasks

import groovy.transform.CompileStatic
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.junit.JUnitOptions
import org.gradle.api.tasks.testing.junitplatform.JUnitPlatformOptions
import org.gradle.api.tasks.testing.testng.TestNGOptions

/**
 * @author Andres Almiray
 * @since 0.20.0
 */
@CompileStatic
class TestSettingsTask extends AbstractSettingsTask {
    @TaskAction
    void report() {
        if (tasks) {
            tasks.each { t ->
                printTask((Test) project.tasks.findByName(t))
            }
        } else if (task) {
            try {
                printTask((Test) project.tasks.findByName(task))
            } catch (NullPointerException e) {
                throw new IllegalStateException("No matching ${this.task} task was found")
            }
        } else {
            Set<Test> testTasks = new LinkedHashSet<>(project.tasks.withType(Test))
            testTasks.each { t ->
                printTask(t)
            }
        }
    }

    private void printTask(Test task) {
        print(task.name + ':', 0)
        doPrintCollection('includes', task.includes, 1)
        doPrintCollection('excludes', task.excludes, 1)
        doPrintMapEntry('debug', task.debug, 1)
        doPrintMapEntry('enableAssertions', task.enableAssertions, 1)
        doPrintMapEntry('executable', task.executable, 1)
        doPrintMapEntry('failFast', task.failFast, 1)
        doPrintMapEntry('forkEvery', task.forkEvery, 1)
        doPrintMapEntry('ignoreFailures', task.ignoreFailures, 1)
        doPrintCollection('jvmArgs', task.jvmArgs, 1)
        doPrintMapEntry('minHeapSize', task.minHeapSize, 1)
        doPrintMapEntry('maxHeapSize', task.maxHeapSize, 1)
        doPrintMapEntry('maxParallelForks', task.maxParallelForks, 1)
        doPrintMapEntry('scanForTestClasses', task.scanForTestClasses, 1)
        doPrintMap('systemProperties', task.systemProperties, 1)
        print('options:', 1)
        if (task.options instanceof JUnitOptions) {
            JUnitOptions o = (JUnitOptions) task.options
            doPrintMapEntry('useJUnit', true, 2)
            doPrintCollection('includeCategories', o.includeCategories, 2)
            doPrintCollection('excludeCategories', o.excludeCategories, 2)
        } else if (task.options instanceof JUnitPlatformOptions) {
            JUnitPlatformOptions o = (JUnitPlatformOptions) task.options
            doPrintMapEntry('useJUnitPlatform', true, 2)
            doPrintCollection('includeEngines', o.includeEngines, 2)
            doPrintCollection('includeTags', o.includeTags, 2)
            doPrintCollection('excludeEngines', o.excludeEngines, 2)
            doPrintCollection('excludeTags', o.excludeTags, 2)
        } else if (task.options instanceof TestNGOptions) {
            TestNGOptions o = (TestNGOptions) task.options
            doPrintMapEntry('useTestNG', true, 2)
            doPrintMapEntry('configFailurePolicy', o.configFailurePolicy, 2)
            doPrintCollection('includeGroups', o.includeGroups, 2)
            doPrintCollection('excludeGroups', o.excludeGroups, 2)
            doPrintMapEntry('groupByInstances', o.groupByInstances, 2)
            doPrintCollection('listeners', o.listeners, 2)
            doPrintMapEntry('parallel', o.parallel, 2)
            doPrintMapEntry('preserveOrder', o.preserveOrder, 2)
            doPrintMapEntry('suiteName', o.suiteName, 2)
            doPrintMapEntry('threadCount', o.threadCount, 2)
            doPrintMapEntry('useDefaultListeners', o.useDefaultListeners, 2)
        }
        if (isShowPaths()) {
            doPrintCollection('bootstrapClasspath', task.bootstrapClasspath, 1)
            doPrintCollection('classpath', task.classpath, 1)
        }
        println ' '
    }
}
