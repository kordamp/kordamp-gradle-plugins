/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2020 Andres Almiray.
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

import com.github.jezza.Toml
import com.github.jezza.TomlTable
import groovy.transform.CompileStatic
import org.gradle.api.plugins.ExtraPropertiesExtension

import java.util.function.BiConsumer

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
class TomlPropertiesReader extends AbstractPropertiesReader {
    static void readProperties(File file, ExtraPropertiesExtension ext, boolean overwrite) {
        if (!file.exists()) return

        TomlPropertiesReader reader = new TomlPropertiesReader()

        TomlTable toml = Toml.from(new FileInputStream(file))
        toml.entrySet().each { entry ->
            if (entry.key == 'systemProp') {
                reader.handleAsSystemProperty('', entry.value, overwrite)
            } else {
                reader.handleAsExtProperty(ext, entry.key, entry.value, overwrite)
            }
        }
    }

    @Override
    protected boolean isKeyValueHolder(Object value) {
        value instanceof TomlTable
    }

    @Override
    protected void handleKeyValues(Object value, BiConsumer<String, Object> handler) {
        ((TomlTable) value).entrySet().each { entry ->
            handler.accept(entry.key, entry.value)
        }
    }
}
