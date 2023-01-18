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
package org.kordamp.gradle.plugin.clirr.reporters

import groovy.transform.CompileStatic
import net.sf.clirr.core.ApiDifference
import net.sf.clirr.core.Severity

/**
 *
 * @author Andres Almiray
 * @since 0.12.0
 */
@CompileStatic
class CountReporter implements Reporter {
    private int srcInfos = 0
    private int srcWarnings = 0
    private int srcErrors = 0

    @Override
    void report(final Map<String, List<ApiDifference>> differences) {
        differences.each { key, value ->
            value.each { difference ->
                final Severity srcSeverity = difference.sourceCompatibilitySeverity
                if (Severity.ERROR == srcSeverity) {
                    srcErrors += 1
                } else if (Severity.WARNING == srcSeverity) {
                    srcWarnings += 1
                } else if (Severity.INFO == srcSeverity) {
                    srcInfos += 1
                }
            }
        }
    }

    int getSrcInfos() {
        srcInfos
    }

    int getSrcWarnings() {
        srcWarnings
    }

    int getSrcErrors() {
        srcErrors
    }
}
