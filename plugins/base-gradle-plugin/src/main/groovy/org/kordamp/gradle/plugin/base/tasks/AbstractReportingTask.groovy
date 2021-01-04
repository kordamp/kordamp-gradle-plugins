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
package org.kordamp.gradle.plugin.base.tasks

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.options.Option
import org.kordamp.gradle.util.AnsiConsole

import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.11.0
 */
@CompileStatic
abstract class AbstractReportingTask extends DefaultTask {
    private static final String SECRET_KEYWORDS = 'password,secret,credential,token,apikey,login'
    private static final String KEY_SECRET_KEYWORDS = 'kordamp.secret.keywords'

    private boolean showSecrets

    @Option(option = 'show-secrets', description = 'Show secret values instead of masked values [OPTIONAL].')
    void setShowSecrets(boolean showSecrets) {
        this.showSecrets = showSecrets
    }

    protected final AnsiConsole console = new AnsiConsole(project)

    protected void print(String value, int offset) {
        doPrintElement(value, offset)
    }

    protected void doPrint(value, int offset) {
        if (value instanceof Map) {
            doPrintMap((Map) value, offset)
        } else if (value instanceof Collection) {
            doPrintCollection(value, offset)
        } else if (value?.class?.array) {
            doPrintArray((Object[]) value, offset)
        } else {
            doPrintElement(value, offset)
        }
    }

    protected void doPrintMap(Map<String, ?> map, int offset) {
        if (map != null) {
            map.each { key, value ->
                if (value instanceof Map) {
                    if (!value.isEmpty()) {
                        println(('    ' * offset) + key + ':')
                        doPrintMap(value, offset + 1)
                    }
                } else if (value instanceof Collection) {
                    if (!value.isEmpty()) {
                        println(('    ' * offset) + key + ':')
                        doPrintCollection((Collection) value, offset + 1)
                    }
                } else if (value?.class?.array) {
                    if (((Object[]) value).size()) {
                        println(('    ' * offset) + key + ':')
                        doPrintArray((Object[]) value, offset + 1)
                    }
                } else if (isNotNullNorBlank(value)) {
                    doPrintMapEntry(key, value, offset)
                }

                if (offset == 0) {
                    println(' ')
                }
            }
        }
    }

    protected boolean isSecret(String key) {
        String lower = key.toLowerCase()

        for (String keyword : System.getProperty(KEY_SECRET_KEYWORDS, SECRET_KEYWORDS).split(',')) {
            if (lower.contains(keyword.trim().toLowerCase())) return true
        }

        return false
    }

    protected void doPrintMapEntry(String key, value, int offset) {
        if (value instanceof Map) {
            doPrintMap(key, (Map) value, offset)
        } else if (value instanceof Collection) {
            doPrintCollection(key, value, offset)
        } else if (value?.class?.array) {
            doPrintArray(key, (Object[]) value, offset)
        } else {
            String result = formatValue(unwrapValue(value), isSecret(key))
            if (isNotNullNorBlank(result)) println(('    ' * offset) + key + ': ' + result)
        }
    }

    protected void doPrintCollection(Collection<?> collection, int offset) {
        if (collection != null) {
            collection.each { value ->
                if (value instanceof Map) {
                    if (!value.isEmpty()) {
                        doPrintMap(value, offset)
                    }
                } else if (value instanceof Collection) {
                    if (!value.isEmpty()) {
                        doPrintCollection((Collection) value, offset + 1)
                    }
                } else if (value?.class?.array) {
                    if (((Object[]) value).size()) {
                        doPrintArray((Object[]) value, offset + 1)
                    }
                } else if (isNotNullNorBlank(value)) {
                    doPrintElement(value, offset)
                }
            }
        }
    }

    protected void doPrintArray(Object[] array, int offset) {
        if (array != null) {
            array.each { value ->
                if (value instanceof Map) {
                    if (!value.isEmpty()) {
                        doPrintMap(value, offset)
                    }
                } else if (value instanceof Collection) {
                    if (!value.isEmpty()) {
                        doPrintCollection((Collection) value, offset + 1)
                    }
                } else if (value?.class?.array) {
                    if (((Object[]) value).size()) {
                        doPrintArray((Object[]) value, offset + 1)
                    }
                } else if (isNotNullNorBlank(value)) {
                    doPrintElement(value, offset)
                }
            }
        }
    }

    protected void doPrintMap(String key, Map<String, ?> map, int offset) {
        if (map != null && !map.isEmpty()) {
            println(('    ' * offset) + key + ':')
            doPrintMap(map, offset + 1)
        }
    }

    protected void doPrintCollection(String key, Collection<?> collection, int offset) {
        if (collection != null && !collection.isEmpty()) {
            println(('    ' * offset) + key + ':')
            doPrintCollection(collection, offset + 1)
        }
    }

    protected void doPrintCollection(String key, FileCollection collection, int offset) {
        if (collection != null && !collection.isEmpty()) {
            println(('    ' * offset) + key + ':')
            doPrintCollection(collection.getFiles(), offset + 1)
        }
    }

    protected void doPrintArray(String key, Object[] array, int offset) {
        if (array != null && array.size()) {
            println(('    ' * offset) + key + ':')
            doPrintArray(array, offset + 1)
        }
    }

    protected void doPrintElement(value, int offset) {
        String result = formatValue(value)
        if (isNotNullNorBlank(result)) println(('    ' * offset) + result)
    }

    protected boolean isNotNullNorBlank(value) {
        if (value instanceof CharSequence) {
            return isNotBlank(String.valueOf(value))
        }
        value != null
    }

    protected String formatValue(value) {
        formatValue(unwrapValue(value), false)
    }

    protected String formatValue(Object value, boolean secret) {
        if (value instanceof Boolean) {
            Boolean b = (Boolean) value
            return b ? console.green(String.valueOf(b)) : console.red(String.valueOf(b))
        } else if (value instanceof Number) {
            return console.cyan(String.valueOf(value))
        } else if (value != null) {
            String s = String.valueOf(value)
            s = secret && !showSecrets ? '*' * 12 : s

            String r = parseAsBoolean(s)
            if (r != null) return r
            r = parseAsInteger(s)
            if (r != null) return r
            r = parseAsDouble(s)
            if (r != null) return r

            return secret ? console.magenta(s) : console.yellow(s)
        }
        return value
    }

    protected String parseAsBoolean(String s) {
        if ('true'.equalsIgnoreCase(s) || 'false'.equalsIgnoreCase(s)) {
            boolean b = Boolean.valueOf(s)
            return b ? console.green(String.valueOf(b)) : console.red(String.valueOf(b))
        } else {
            return null
        }
    }

    protected String parseAsInteger(String s) {
        try {
            Integer.parseInt(s)
            return console.cyan(s)
        } catch (Exception e) {
            return null
        }
    }

    protected String parseAsDouble(String s) {
        try {
            Double.parseDouble(s)
            return console.cyan(s)
        } catch (Exception e) {
            return null
        }
    }

    protected Object unwrapValue(Object value) {
        if (value instanceof Property) {
            return ((Property) value).getOrNull()
        } else if (value instanceof Provider) {
            return ((Provider) value).getOrNull()
        } else {
            value
        }
    }
}
