/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2025 Andres Almiray.
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
package org.kordamp.gradle.util

import groovy.transform.Canonical
import groovy.transform.CompileStatic

/**
 *
 * @author Andres Almiray
 * @since 0.40.0
 */
@Canonical
@CompileStatic
class TimeUtils {
    /**
     * Formats the given time.</p>
     *
     * <pre>
     * formatDuration(3.456) == '3.456 s'
     * formatDuration(67.45) == '01:07 m'
     * </pre>
     * @param time the given time in seconds.
     */
    static String formatDuration(double time) {
        if (time <= 0d) time = 0d

        String formatted = String.format('%.3f', time) + ' s'

        if (time >= 60d) {
            int seconds = (int) time
            String m = String.valueOf((int) (seconds / 60))
            String s = String.valueOf(seconds % 60)
            formatted = "${m.padLeft(2, '0')}:${s.padLeft(2, '0')}  m".toString()
        }

        formatted
    }
}
