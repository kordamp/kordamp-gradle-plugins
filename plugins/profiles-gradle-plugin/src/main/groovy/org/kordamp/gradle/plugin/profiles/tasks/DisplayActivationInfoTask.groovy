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
package org.kordamp.gradle.plugin.profiles.tasks

import groovy.transform.CompileStatic
import org.gradle.api.tasks.TaskAction
import org.kordamp.gradle.plugin.base.tasks.AbstractReportingTask
import org.kordamp.gradle.plugin.profiles.internal.ActivationJdk
import org.kordamp.gradle.plugin.profiles.internal.ActivationOs

/**
 * @author Andres Almiray
 * @since 0.35.0
 */
@CompileStatic
class DisplayActivationInfoTask extends AbstractReportingTask {
    @TaskAction
    void report() {
        doPrintMapEntry('JDK', ActivationJdk.detectedVersionAsMap(), 0)
        doPrintMapEntry('OS', ActivationOs.detectedOsAsMap(project), 0)
    }
}
