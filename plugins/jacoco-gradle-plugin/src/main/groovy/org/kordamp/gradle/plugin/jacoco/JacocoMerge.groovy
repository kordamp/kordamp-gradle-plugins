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
package org.kordamp.gradle.plugin.jacoco

import groovy.transform.CompileStatic
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.jacoco.AntJacocoMerge

/**
 *
 * @author Andres Almiray
 * @since 0.32.0
 */
@CompileStatic
class JacocoMerge extends org.gradle.testing.jacoco.tasks.JacocoMerge {
    @TaskAction
    void merge() {
        List<File> files = []
        getExecutionData().files.each { File file ->
            if (file.exists()) {
                files << file
            }
        }

        new AntJacocoMerge(getAntBuilder()).execute(
            getJacocoClasspath(),
            project.objects.fileCollection().from(files),
            getDestinationFile())
    }
}
