package org.kordamp.gradle.model

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
        SpecOrImpl actual = spec1.merge(spec2)

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
        SpecOrImpl actual = spec1.merge(spec2)

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
        SpecOrImpl actual = spec1.merge(spec2)

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
