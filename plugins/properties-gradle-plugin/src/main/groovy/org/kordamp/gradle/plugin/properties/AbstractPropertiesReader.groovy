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
package org.kordamp.gradle.plugin.properties

import groovy.transform.CompileStatic
import org.gradle.api.plugins.ExtraPropertiesExtension

import java.util.function.BiConsumer

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
abstract class AbstractPropertiesReader {
    protected boolean isKeyValueHolder(Object value) {
        value instanceof Map
    }

    protected void handleKeyValues(Object value, BiConsumer<String, Object> handler) {
        ((Map) value).each { key, val ->
            handler.accept(String.valueOf(key), val)
        }
    }

    protected void handleAsSystemProperty(String accumulatedKey, Object value, boolean overwrite) {
        if (isKeyValueHolder(value)) {
            handleKeyValues(value) { key, val ->
                handleAsSystemProperty((accumulatedKey ? accumulatedKey + '.' : '') + key, val, overwrite)
            }
        } else if (value instanceof Collection) {
            accumulatedKey = accumulatedKey ?: 'systemProp'
            if (System.hasProperty(accumulatedKey)) {
                if (overwrite) {
                    System.setProperty(accumulatedKey, ((Collection) value).join(','))
                }
            } else {
                System.setProperty(accumulatedKey, ((Collection) value).join(','))
            }
        } else {
            accumulatedKey = accumulatedKey ?: 'systemProp'
            if (System.hasProperty(accumulatedKey)) {
                if (overwrite) {
                    if (value != null) System.setProperty(accumulatedKey, String.valueOf(value))
                }
            } else {
                if (value != null) System.setProperty(accumulatedKey, String.valueOf(value))
            }
        }
    }

    protected void handleAsExtProperty(ExtraPropertiesExtension ext, String accumulatedKey, Object value, boolean overwrite) {
        if (isKeyValueHolder(value)) {
            handleKeyValues(value) { key, val ->
                handleAsExtProperty(ext, accumulatedKey + '.' + key, val, overwrite)
            }
        } else if (value instanceof Collection) {
            if (ext.has(accumulatedKey)) {
                if (overwrite) {
                    ext.set(accumulatedKey, ((Collection) value).join(','))
                }
            } else {
                ext.set(accumulatedKey, ((Collection) value).join(','))
            }
        } else {
            if (ext.has(accumulatedKey)) {
                if (overwrite) {
                    if (value != null) ext.set(accumulatedKey, value)
                }
            } else {
                if (value != null) ext.set(accumulatedKey, value)
            }
        }
    }
}
