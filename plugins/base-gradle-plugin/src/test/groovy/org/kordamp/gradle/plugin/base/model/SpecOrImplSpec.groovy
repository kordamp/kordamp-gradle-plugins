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
        org.kordamp.gradle.plugin.base.model.Specification spec = new org.kordamp.gradle.plugin.base.model.Specification()
        org.kordamp.gradle.plugin.base.model.Specification expected = new org.kordamp.gradle.plugin.base.model.Specification()

        when:
        org.kordamp.gradle.plugin.base.model.Specification actual = spec.copyOf()

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
        org.kordamp.gradle.plugin.base.model.Specification spec = new org.kordamp.gradle.plugin.base.model.Specification()
        org.kordamp.gradle.plugin.base.model.Specification expected = new org.kordamp.gradle.plugin.base.model.Specification()
        expected.enabled = enabled

        when:
        spec.enabled = enabled
        org.kordamp.gradle.plugin.base.model.Specification actual = spec.copyOf()

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
        org.kordamp.gradle.plugin.base.model.Specification spec1 = new org.kordamp.gradle.plugin.base.model.Specification()
        org.kordamp.gradle.plugin.base.model.Specification spec2 = new org.kordamp.gradle.plugin.base.model.Specification()
        org.kordamp.gradle.plugin.base.model.Specification expected = new org.kordamp.gradle.plugin.base.model.Specification()
        expected.enabled = true

        when:
        org.kordamp.gradle.plugin.base.model.Specification actual = spec1.copyOf()
        org.kordamp.gradle.plugin.base.model.Specification.merge(actual, spec2)

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
        org.kordamp.gradle.plugin.base.model.Specification spec1 = new org.kordamp.gradle.plugin.base.model.Specification(enabled: enabled1)
        org.kordamp.gradle.plugin.base.model.Specification spec2 = new org.kordamp.gradle.plugin.base.model.Specification(enabled: enabled2)
        org.kordamp.gradle.plugin.base.model.Specification expected = new org.kordamp.gradle.plugin.base.model.Specification(enabled: enabled)

        when:
        org.kordamp.gradle.plugin.base.model.Specification actual = spec1.copyOf()
        org.kordamp.gradle.plugin.base.model.Specification.merge(actual, spec2)

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
        org.kordamp.gradle.plugin.base.model.Specification spec1 = new org.kordamp.gradle.plugin.base.model.Specification()
        org.kordamp.gradle.plugin.base.model.Specification spec2 = new org.kordamp.gradle.plugin.base.model.Specification(enabled: enabled2)
        org.kordamp.gradle.plugin.base.model.Specification expected = new org.kordamp.gradle.plugin.base.model.Specification(enabled: enabled)

        when:
        org.kordamp.gradle.plugin.base.model.Specification actual = spec1.copyOf()
        org.kordamp.gradle.plugin.base.model.Specification.merge(actual, spec2)

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
