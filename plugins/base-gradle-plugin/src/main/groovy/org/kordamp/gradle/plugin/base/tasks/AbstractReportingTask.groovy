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
import org.kordamp.gradle.AnsiConsole

import static org.kordamp.gradle.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.11.0
 */
@CompileStatic
abstract class AbstractReportingTask extends DefaultTask {
    protected void doPrint(value, int offset) {
        doPrint(new AnsiConsole(project), value, offset)
    }

    protected void doPrint(AnsiConsole console, value, int offset) {
        if (value instanceof Map) {
            doPrintMap(console, (Map) value, offset)
        } else if (value instanceof Collection) {
            doPrintCollection(console, value, offset)
        } else {
            doPrintElement(console, value, offset)
        }
    }

    protected void doPrintMap(AnsiConsole console, Map<String, ?> map, int offset) {
        map.each { key, value ->
            if (value instanceof Map) {
                if (!value.isEmpty()) {
                    println(('    ' * offset) + key + ':')
                    doPrintMap(console, value, offset + 1)
                }
            } else if (value instanceof Collection) {
                if (!value.isEmpty()) {
                    println(('    ' * offset) + key + ':')
                    doPrintCollection(console, (Collection) value, offset + 1)
                }
            } else if (isNotNullNorBlank(value)) {
                doPrintMapEntry(console, key, value, offset)
            }

            if (offset == 0) {
                println(' ')
            }
        }
    }

    protected void doPrintMapEntry(AnsiConsole console, String key, value, int offset) {
        println(('    ' * offset) + key + ': ' + formatValue(console, value, offset))
    }

    protected void doPrintCollection(AnsiConsole console, Collection<?> collection, int offset) {
        collection.each { value ->
            if (value instanceof Map) {
                if (!value.isEmpty()) {
                    doPrintMap(console, value, offset)
                }
            } else if (value instanceof Collection && !((Collection) value).empty) {
                if (!value.isEmpty()) {
                    doPrintCollection(console, (Collection) value, offset + 1)
                }
            } else if (isNotNullNorBlank(value)) {
                doPrintElement(console, value, offset)
            }
        }
    }

    protected void doPrintElement(AnsiConsole console, value, int offset) {
        println(('    ' * offset) + formatValue(console, value, offset))
    }

    protected boolean isNotNullNorBlank(value) {
        value != null || (value instanceof CharSequence && isNotBlank(String.valueOf(value)))
    }

    protected String formatValue(AnsiConsole console, value, int offset) {
        if (value instanceof Boolean) {
            Boolean b = (Boolean) value
            return b ? console.green(String.valueOf(b)) : console.red(String.valueOf(b))
        } else if (value instanceof Number) {
            return console.cyan(String.valueOf(value))
        } else {
            String s = String.valueOf(value)

            String r = parseAsBoolean(console, s)
            if (r != null) return r
            r = parseAsInteger(console, s)
            if (r != null) return r
            r = parseAsDouble(console, s)
            if (r != null) return r

            return console.yellow(s)
        }
    }

    protected String parseAsBoolean(AnsiConsole console, String s) {
        if ('true'.equalsIgnoreCase(s) || 'false'.equalsIgnoreCase(s)) {
            boolean b = Boolean.valueOf(s)
            return b ? console.green(String.valueOf(b)) : console.red(String.valueOf(b))
        } else {
            return null
        }
    }

    protected String parseAsInteger(AnsiConsole console, String s) {
        try {
            Integer.parseInt(s)
            return console.cyan(s)
        } catch (Exception e) {
            return null
        }
    }

    protected String parseAsDouble(AnsiConsole console, String s) {
        try {
            Double.parseDouble(s)
            return console.cyan(s)
        } catch (Exception e) {
            return null
        }
    }
}
