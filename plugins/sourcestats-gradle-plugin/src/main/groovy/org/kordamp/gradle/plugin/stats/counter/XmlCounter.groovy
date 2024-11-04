/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2024 Andres Almiray.
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
package org.kordamp.gradle.plugin.stats.counter

import groovy.transform.CompileStatic
import org.kordamp.gradle.plugin.stats.Counter

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
@CompileStatic
class XmlCounter implements Counter {
    String OPEN_CARET_CLOSE_CARET = /^(.*)<!--(.*)-->(.*)$/

    @Override
    int count(File file) {
        int loc = 0
        int comment = 0
        file.eachLine { line ->
            if (!line.trim().length() || line ==~ EMPTY) return

            def m = line =~ OPEN_CARET_CLOSE_CARET
            if (m.find() && m.group(1) ==~ EMPTY && m.group(3) ==~ EMPTY) return
            int open = line.indexOf('<!--')
            int close = line.indexOf('-->')

            if (open != -1 && (close - open) <= 1) comment++
            else if (close != -1 && comment) {
                comment--
                if (!comment) return
            }

            if (!comment) loc++
        }

        loc
    }
}
