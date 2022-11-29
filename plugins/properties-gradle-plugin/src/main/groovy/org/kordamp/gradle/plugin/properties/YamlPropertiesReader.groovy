/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2022 Andres Almiray.
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
import org.yaml.snakeyaml.Yaml

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
class YamlPropertiesReader extends AbstractPropertiesReader {
    static void readProperties(File file, ExtraPropertiesExtension ext, boolean overwrite) {
        if (!file.exists()) return

        YamlPropertiesReader reader = new YamlPropertiesReader()

        Yaml yaml = new Yaml()
        for (Object doc : yaml.loadAll(new FileInputStream(file))) {
            Map<String, Object> map = (Map<String, Object>) doc
            map.each { key, value ->
                if (key == 'systemProp') {
                    reader.handleAsSystemProperty('', value, overwrite)
                } else {
                    reader.handleAsExtProperty(ext, key, value, overwrite)
                }
            }
        }
    }
}
