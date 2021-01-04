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
package org.kordamp.gradle.plugin.clirr

import groovy.transform.CompileStatic
import org.kordamp.gradle.util.Version

/**
 *
 * @author Andres Almiray
 * @since 0.12.0
 */
@CompileStatic
class Versions {
    final Version current
    final Version previous

    static Versions of(Version current) {
        return of(current, true)
    }

    static Versions of(Version current, boolean semver) {
        return new Versions(current, semver)
    }

    private Versions(Version current, boolean semver) {
        this.current = current
        this.previous = calculatePrevious(current, semver)
    }

    private static Version calculatePrevious(Version version, boolean semver) {
        if (version == Version.ZERO) {
            return Version.ZERO
        }

        if (semver) {
            int major = version.major
            int minor = version.minor
            int revision = version.revision

            if (minor == 0) {
                if (revision == 0) {
                    return Version.ZERO
                } else {
                    return Version.of(major, minor, revision - 1)
                }
            } else {
                if (revision == 0) {
                    return Version.of(major, minor - 1, revision)
                } else {
                    return Version.of(major, minor, revision - 1)
                }
            }
        } else {
            int major = version.major
            int minor = version.minor
            int revision = version.revision

            if (revision == 0) {
                return Version.ZERO
            }

            return Version.of(major, minor, revision - 1)
        }
    }
}
