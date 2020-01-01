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
package org.kordamp.gradle.plugin.clirr

import org.kordamp.gradle.Version
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class VersionsSpec extends Specification {
    def "Version #current has previous version #previous (semver=#semver)"() {
        when:
        Versions versions = Versions.of(current, semver)

        then:
        versions.previous == previous

        where:
        current                 | semver || previous
        Version.ZERO            | false  || Version.ZERO
        Version.of('2.0.0')     | false  || Version.ZERO
        Version.of('2.0.0-TAG') | false  || Version.ZERO
        Version.of('2.0.4')     | false  || Version.of('2.0.3')
        Version.of('2.0.4-TAG') | false  || Version.of('2.0.3')
        Version.of('2.1.0')     | false  || Version.ZERO
        Version.of('2.1.0-TAG') | false  || Version.ZERO
        Version.of('2.1.3')     | false  || Version.of('2.1.2')
        Version.of('2.1.3-TAG') | false  || Version.of('2.1.2')
        Version.ZERO            | true   || Version.ZERO
        Version.of('2.0.0')     | true   || Version.ZERO
        Version.of('2.0.0-TAG') | true   || Version.ZERO
        Version.of('2.0.4')     | true   || Version.of('2.0.3')
        Version.of('2.0.4-TAG') | true   || Version.of('2.0.3')
        Version.of('2.1.0')     | true   || Version.of('2.0.0')
        Version.of('2.1.0-TAG') | true   || Version.of('2.0.0')
        Version.of('2.1.3')     | true   || Version.of('2.1.2')
        Version.of('2.1.3-TAG') | true   || Version.of('2.1.2')
        Version.of('0.9.5')     | true   || Version.of('0.9.4')
        Version.of('0.9.5-TAG') | true   || Version.of('0.9.4')
        Version.of('0.9.0')     | true   || Version.of('0.8.0')
        Version.of('0.9.0-TAG') | true   || Version.of('0.8.0')
    }
}
