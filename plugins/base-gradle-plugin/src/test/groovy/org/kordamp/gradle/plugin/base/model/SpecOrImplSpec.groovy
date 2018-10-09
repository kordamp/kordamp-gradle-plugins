/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kordamp.gradle.plugin.base.model

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Andres Almiray
 */
@Unroll
class SpecOrImplSpec extends Specification {
    def "Verify copy method with defaults"() {
        given:
        SpecOrImpl spec = new SpecOrImpl()
        SpecOrImpl expected = new SpecOrImpl()

        when:
        SpecOrImpl actual = spec.copyOf()

        then:
        actual == expected
        !actual.enabledSet
        actual.@enabled == expected.@enabled
        actual.@enabledSet == expected.@enabledSet
        actual.@title == expected.@title
        actual.@version == expected.@version
        actual.@vendor == expected.@vendor
    }

    def "Verify copy method (enabled = #enabled)"() {
        given:
        SpecOrImpl spec = new SpecOrImpl()
        SpecOrImpl expected = new SpecOrImpl()
        expected.enabled = enabled

        when:
        spec.enabled = enabled
        SpecOrImpl actual = spec.copyOf()

        then:
        actual == expected
        actual.enabledSet
        actual.@enabled == expected.@enabled
        actual.@enabledSet == expected.@enabledSet
        actual.@title == expected.@title
        actual.@version == expected.@version
        actual.@vendor == expected.@vendor

        where:
        enabled << [false, true]
    }

    def "Verify merge method with defaults"() {
        given:
        SpecOrImpl spec1 = new SpecOrImpl()
        SpecOrImpl spec2 = new SpecOrImpl()
        SpecOrImpl expected = new SpecOrImpl()
        expected.enabled = true

        when:
        SpecOrImpl actual = spec1.copyOf()
        SpecOrImpl.merge(actual, spec2)

        then:
        actual == expected
        actual.enabledSet
        actual.@enabled == expected.@enabled
        actual.@enabledSet == expected.@enabledSet
        actual.@title == expected.@title
        actual.@version == expected.@version
        actual.@vendor == expected.@vendor
    }

    def "Verify merge method (enabled1 = #enabled1, enabled2 = #enabled2, enabled = #enabled)"() {
        given:
        SpecOrImpl spec1 = new SpecOrImpl(enabled: enabled1)
        SpecOrImpl spec2 = new SpecOrImpl(enabled: enabled2)
        SpecOrImpl expected = new SpecOrImpl(enabled: enabled)

        when:
        SpecOrImpl actual = spec1.copyOf()
        SpecOrImpl.merge(actual, spec2)

        then:
        actual == expected
        actual.enabledSet
        actual.@enabled == expected.@enabled
        actual.@enabledSet == expected.@enabledSet
        actual.@title == expected.@title
        actual.@version == expected.@version
        actual.@vendor == expected.@vendor

        where:
        enabled1 | enabled2 || enabled
        false    | false    || false
        true     | false    || true
        false    | true     || false
        true     | true     || true
    }

    def "Verify merge method (enabled2 = #enabled2, enabled = #enabled)"() {
        given:
        SpecOrImpl spec1 = new SpecOrImpl()
        SpecOrImpl spec2 = new SpecOrImpl(enabled: enabled2)
        SpecOrImpl expected = new SpecOrImpl(enabled: enabled)

        when:
        SpecOrImpl actual = spec1.copyOf()
        SpecOrImpl.merge(actual, spec2)

        then:
        actual == expected
        actual.enabledSet
        actual.@enabled == expected.@enabled
        actual.@enabledSet == expected.@enabledSet
        actual.@title == expected.@title
        actual.@version == expected.@version
        actual.@vendor == expected.@vendor

        where:
        enabled2 || enabled
        false    || false
        true     || true
    }
}
