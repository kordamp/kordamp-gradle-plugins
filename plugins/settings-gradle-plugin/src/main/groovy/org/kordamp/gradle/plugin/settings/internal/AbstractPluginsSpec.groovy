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
package org.kordamp.gradle.plugin.settings.internal

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.kordamp.gradle.plugin.settings.PluginsSpec

/**
 * @author Andres Almiray
 * @since 0.35.0
 */
@PackageScope
@CompileStatic
abstract class AbstractPluginsSpec {
    final Set<PluginIdSpecImpl> ids = new LinkedHashSet<>()

    PluginsSpec.PluginIdSpec id(String pluginId) {
        String s = pluginId ? pluginId.trim() : pluginId
        if (isNotBlank(s)) {
            PluginIdSpecImpl spec = new PluginIdSpecImpl(s)
            ids << spec
            return spec
        }
        null
    }

    protected void applyPluginsTo(Project project) {
        for (PluginIdSpecImpl spec : ids) {
            if (spec.applies()) {
                project.logger.debug("[settings] Applying plugin ${spec.id} to ${project}")
                project.pluginManager.apply(spec.id)
            }
        }
    }

    protected String asGlobRegex(String globPattern) {
        if (globPattern == '*') return '^.*$'

        StringBuilder result = new StringBuilder(globPattern.length() + 2)
        result.append('^')
        for (int index = 0; index < globPattern.length(); index++) {
            char character = globPattern.charAt(index)
            switch (character) {
                case '*':
                    result.append('.*')
                    break
                case '?':
                    result.append('.')
                    break
                case '$':
                case '(':
                case ')':
                case '.':
                case '[':
                case '\\':
                case ']':
                case '^':
                case '{':
                case '|':
                case '}':
                    result.append('\\')
                default:
                    result.append(character)
                    break
            }
        }
        result.append('$')
        return result.toString()
    }

    protected boolean isBlank(String str) {
        if (str == null || str.length() == 0) {
            return true
        }
        for (char c : str.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                return false
            }
        }

        return true
    }

    protected boolean isNotBlank(String str) {
        !isBlank(str)
    }
}
