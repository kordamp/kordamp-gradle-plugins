/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 the original author or authors.
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

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
@Canonical
@ToString(includeNames = true)
class Person {
    String id
    String name
    String email
    String url
    Organization organization
    List<String> roles = []

    Person copyOf() {
        Person copy = new Person()
        copy.id = id
        copy.name = name
        copy.email = email
        copy.url = url
        copy.organization = organization?.copyOf()
        copy.roles.addAll(roles)
        copy
    }

    static Person merge(Person o1, Person o2) {
        o1.id = o1.id ?: o2?.id
        o1.name = o1.name ?: o2?.name
        o1.email = o1.email ?: o2?.email
        o1.url = o1.url ?: o2?.url
        o1.roles.addAll((o1.roles + o2?.roles).unique())
        if (o1.organization) {
            Organization.merge(o1.organization, o2?.organization)
        } else {
            o1.organization = o2?.organization?.copyOf()
        }
        o1
    }
}
