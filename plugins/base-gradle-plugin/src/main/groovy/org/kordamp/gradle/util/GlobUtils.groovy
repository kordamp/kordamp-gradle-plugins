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
 * @author Leonard Bruenings
 * @since 0.41.0
 */
@Canonical
@CompileStatic
class GlobUtils {
    static String asGlobRegex(String globPattern, boolean isPathSyntax = false) {
        if (globPattern == '*') return '^.*$'

        StringBuilder result = new StringBuilder(globPattern.length() + 2)
        result.append('^')
        for (int index = 0; index < globPattern.length(); index++) {
            char character = globPattern.charAt(index)
            switch (character) {
                case '*':
                    if (next(globPattern, index + 1) == '*') {
                        // crosses directory boundaries
                        result.append('.*')
                        index++
                    } else {
                        // within directory boundary
                        if (isPathSyntax) {
                            result.append('[^:]*')
                        } else {
                            result.append('[^/]*')
                        }
                    }
                    break
                case '?':
                    if (isPathSyntax) {
                        result.append('[^:]')
                    } else {
                        result.append('[^/]')
                    }
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

    private static char next(String glob, int i) {
        if (i < glob.length()) {
            return glob.charAt(i)
        }
        return 0
    }
}
