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
package org.kordamp.gradle

import groovy.transform.CompileStatic

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
class StringUtils {
    /**
     * Capitalizes a String (makes the first char uppercase) taking care
     * of blank strings and single character strings.
     *
     * @param str The String to be capitalized
     *
     * @return Capitalized version of the target string if it is not blank
     */
    static String capitalize(String str) {
        if (isBlank(str)) {
            return str
        }
        if (str.length() == 1) {
            return str.toUpperCase()
        }
        return str.substring(0, 1).toUpperCase(Locale.ENGLISH) + str.substring(1)
    }

    /**
     * <p>Determines whether a given string is <code>null</code>, empty,
     * or only contains whitespace. If it contains anything other than
     * whitespace then the string is not considered to be blank and the
     * method returns <code>false</code>.</p>
     *
     * @param str The string to test.
     *
     * @return <code>true</code> if the string is <code>null</code>, or
     * blank.
     */
    static boolean isBlank(String str) {
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

    /**
     * <p>Determines whether a given string is not <code>null</code>, empty,
     * or only contains whitespace. If it contains anything other than
     * whitespace then the string is not considered to be blank and the
     * method returns <code>true</code>.</p>
     *
     * @param str The string to test.
     *
     * @return <code>true</code> if the string is not <code>null</code>, nor
     * blank.
     */
    static boolean isNotBlank(String str) {
        return !isBlank(str)
    }

    static String getFilenameExtension(String path) {
        if (path == null) {
            return null
        }
        int extIndex = path.lastIndexOf(".")
        if (extIndex == -1) {
            return null
        }
        int folderIndex = path.lastIndexOf("/")
        if (folderIndex > extIndex) {
            return null
        }
        return path.substring(extIndex + 1)
    }

    static String getNaturalName(String name) {
        name = getShortName(name)
        if (isBlank(name)) return name
        List<String> words = new ArrayList<>()
        int i = 0
        char[] chars = name.toCharArray()
        for (char c : chars) {
            String w
            if (i >= words.size()) {
                w = ""
                words.add(i, w)
            } else {
                w = words.get(i)
            }

            if (Character.isLowerCase(c) || Character.isDigit(c)) {
                if (Character.isLowerCase(c) && w.length() == 0) {
                    c = Character.toUpperCase(c)
                } else if (w.length() > 1 && Character.isUpperCase(w.charAt(w.length() - 1))) {
                    w = ""
                    words.add(++i, w)
                }

                words.set(i, w + c)
            } else if (Character.isUpperCase(c)) {
                if ((i == 0 && w.length() == 0) || Character.isUpperCase(w.charAt(w.length() - 1))) {
                    words.set(i, w + c)
                } else {
                    words.add(++i, String.valueOf(c))
                }
            }
        }

        StringBuilder buf = new StringBuilder()
        for (Iterator<String> j = words.iterator(); j.hasNext();) {
            String word = j.next()
            buf.append(word)
            if (j.hasNext()) {
                buf.append(' ')
            }
        }
        return buf.toString()
    }

    static String getShortName(String className) {
        if (isBlank(className)) return className
        int i = className.lastIndexOf(".")
        if (i > -1) {
            className = className.substring(i + 1, className.length())
        }
        return className
    }

    static String getPropertyNameForLowerCaseHyphenSeparatedName(String name) {
        return getPropertyName(getClassNameForLowerCaseHyphenSeparatedName(name))
    }

    static String getClassNameForLowerCaseHyphenSeparatedName(String name) {
        // Handle null and empty strings.
        if (isBlank(name)) return name

        if (name.indexOf('-') > -1) {
            StringBuilder buf = new StringBuilder()
            String[] tokens = name.split("-")
            for (String token : tokens) {
                if (token == null || token.length() == 0) continue
                buf.append(capitalize(token))
            }
            return buf.toString()
        }

        return capitalize(name)
    }

    static String getPropertyName(String name) {
        if (isBlank(name)) return name
        // Strip any package from the name.
        int pos = name.lastIndexOf('.')
        if (pos != -1) {
            name = name.substring(pos + 1)
        }

        // Check whether the name begins with two upper case letters.
        if (name.length() > 1 && Character.isUpperCase(name.charAt(0)) && Character.isUpperCase(name.charAt(1))) {
            return name
        }

        String propertyName = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1)
        if (propertyName.indexOf(' ') > -1) {
            propertyName = propertyName.replaceAll("\\s", "")
        }
        return propertyName
    }
}
