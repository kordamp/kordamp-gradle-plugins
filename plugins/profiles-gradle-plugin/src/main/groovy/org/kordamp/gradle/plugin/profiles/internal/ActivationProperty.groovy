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
package org.kordamp.gradle.plugin.profiles.internal

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.kordamp.gradle.plugin.profiles.Activation

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.35.0
 */
@PackageScope
@CompileStatic
class ActivationProperty implements Activation {
    final Property<String> key
    final Property<String> value

    @Inject
    ActivationProperty(ObjectFactory objects) {
        key = objects.property(String)
        value = objects.property(String)
    }

    boolean isActive(Project project) {
        if (!key.present) {
            throw new IllegalStateException("No value for 'key' has been set.")
        }

        if (!value.present) {
            String keyText = key.get()

            // 1. check if key starts with '!'
            boolean reverse = false
            if (keyText.startsWith('!')) {
                reverse = true
                keyText = keyText[1..-1]
            }

            // 2. check if prop is env., systemProp., or regular
            boolean result = false
            if (keyText.startsWith('env.')) {
                String property = keyText[4..-1].toUpperCase()
                result = System.getenv().get(property) != null
            } else if (keyText.startsWith('systemProp.')) {
                String property = keyText[11..-1]
                result = System.getProperty(property) != null
            } else {
                result = project.hasProperty(keyText)
            }
            return reverse ? !result : result
        } else {
            String keyText = key.get()
            String valueText = value.get()

            // 1. check if value starts with '!'
            boolean reverse = false
            if (valueText.startsWith('!')) {
                reverse = true
                valueText = valueText[1..-1]
            }

            // 2. check if prop is env., systemProp., or regular
            String actualValueText = null
            if (keyText.startsWith('env.')) {
                String property = keyText[4..-1].toUpperCase()
                actualValueText = System.getenv().get(property)
            } else if (keyText.startsWith('systemProp.')) {
                String property = keyText[11..-1]
                actualValueText = System.getProperty(property)
            } else {
                actualValueText = project.hasProperty(keyText) ? String.valueOf(project.findProperty(keyText)) : null
            }
            boolean result = actualValueText == valueText
            return reverse ? !result : result
        }
    }
}
