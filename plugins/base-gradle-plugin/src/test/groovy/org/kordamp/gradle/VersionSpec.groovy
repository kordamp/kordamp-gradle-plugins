/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class VersionSpec extends Specification {
    def "Literal #literal results in #major.#minor.#revision-#tag"() {
        when:
        Version version = Version.of(literal)

        then:
        version.major == major
        version.minor == minor
        version.revision == revision
        version.tag == tag

        where:
        literal          | major | minor | revision | tag
        '0.2.3'          | 0     | 2     | 3        | ''
        '0.2.3-TAG'      | 0     | 2     | 3        | 'TAG'
        '0.2.3-SNAPSHOT' | 0     | 2     | 3        | 'SNAPSHOT'
        '1.2.3'          | 1     | 2     | 3        | ''
        '1.2.3-TAG'      | 1     | 2     | 3        | 'TAG'
        '1.2.3-SNAPSHOT' | 1     | 2     | 3        | 'SNAPSHOT'
    }

    def "Literal #literal cannot be parsed"() {
        when:
        Version version = Version.of(literal)

        then:
        version == Version.ZERO

        where:
        literal << ['1', '1.2.3.4', '1.2.TAG', '1.2.3.TAG', 'garbage', '00.1.2', '01.2.3']
    }
}
