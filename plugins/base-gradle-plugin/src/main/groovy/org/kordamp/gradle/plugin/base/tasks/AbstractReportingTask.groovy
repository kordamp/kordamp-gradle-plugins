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
import org.gradle.api.DefaultTask

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.11.0
 */
@CompileStatic
abstract class AbstractReportingTask extends DefaultTask {
    protected static void doPrint(Map<String, ?> map, int offset) {
        map.each { key, value ->
            if (value instanceof Map) {
                if (!value.isEmpty()) {
                    println(('    ' * offset) + key + ':')
                    doPrint(value, offset + 1)
                }
            } else if (value instanceof Collection) {
                if (!value.isEmpty()) {
                    println(('    ' * offset) + key + ':')
                    doPrint((Collection) value, offset + 1)
                }
            } else if (isNotNullNorBlank(value)) {
                println(('    ' * offset) + key + ': ' + formatValue(value))
            }

            if (offset == 0) {
                println(' ')
            }
        }
    }

    protected static void doPrint(Collection<?> collection, int offset) {
        collection.each { value ->
            if (value instanceof Map) {
                if (!value.isEmpty()) {
                    doPrint(value, offset)
                }
            } else if (value instanceof Collection && !((Collection) value).empty) {
                if (!value.isEmpty()) {
                    doPrint(value, offset + 1)
                }
            } else if (isNotNullNorBlank(value)) {
                println(('    ' * offset) + formatValue(value))
            }
        }
    }

    protected static boolean isNotNullNorBlank(value) {
        value != null || (value instanceof CharSequence && !isBlank(String.valueOf(value)))
    }

    protected static String formatValue(value) {
        if (value instanceof Boolean) {
            Boolean b = (Boolean) value
            return (b ? '\u001b[32m' : '\u001b[31m') + String.valueOf(b) + '\u001b[0m'
        } else if (value instanceof Number) {
            return '\u001b[36m' + String.valueOf(value) + '\u001b[0m'
        } else {
            String s = String.valueOf(value)

            String r = parseAsBoolean(s)
            if (r != null) return r
            r = parseAsInteger(s)
            if (r != null) return r
            r = parseAsDouble(s)
            if (r != null) return r

            return '\u001b[33m' + s + '\u001b[0m'
        }
    }

    protected static String parseAsBoolean(String s) {
        if ('true'.equalsIgnoreCase(s) || 'false'.equalsIgnoreCase(s)) {
            boolean b = Boolean.valueOf(s)
            return (b ? '\u001b[32m' : '\u001b[31m') + String.valueOf(b) + '\u001b[0m'
        } else {
            return null
        }
    }

    protected static String parseAsInteger(String s) {
        try {
            Integer.parseInt(s)
            return '\u001b[36m' + s + '\u001b[0m'
        } catch (Exception e) {
            return null
        }
    }

    protected static String parseAsDouble(String s) {
        try {
            Double.parseDouble(s)
            return '\u001b[36m' + s + '\u001b[0m'
        } catch (Exception e) {
            return null
        }
    }
}
