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
package org.kordamp.gradle.util

import groovy.transform.Canonical
import groovy.transform.CompileStatic

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 *
 * @author Andres Almiray
 * @since 0.12.0
 */
@Canonical
@CompileStatic
class Version {
    public static final Version ZERO = new Version(0, 0, 0, '')

    final int major
    final int minor
    final int revision
    final String tag

    private static final Pattern SEMVER = ~/([0-9]|[1-9][0-9]*)\.([0-9]|[1-9][0-9]*)\.([0-9]|[1-9][0-9]*)(\-.+)?/

    static Version of(String str) {
        Matcher matcher = SEMVER.matcher(str)
        if (matcher.matches()) {
            return new Version(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)),
                matcher.group(4) ?: ''
            )
        }

        return ZERO
    }

    static Version of(int major, int minor, int revision) {
        of(major, minor, revision, '')
    }

    static Version of(int major, int minor, int revision, String tag) {
        new Version(major, minor, revision, tag)
    }

    private Version(int major, int minor, int revision, String tag) {
        this.major = major
        this.minor = minor
        this.revision = revision
        this.tag = tag.startsWith('-') ? tag[1..-1] : tag
    }

    String toString() {
        List<String> v = [major.toString(), '.', minor.toString(), '.', revision.toString()]
        if (tag) {
            v << '-'
            v << tag
        }
        v.join('')
    }
}
