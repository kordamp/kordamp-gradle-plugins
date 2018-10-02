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
package org.kordamp.gradle.model

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

    Person merge(Person other) {
        Person copy = new Person()
        copy.id = id ?: other?.id
        copy.name = name ?: other?.name
        copy.email = email ?: other?.email
        copy.url = url ?: other?.url
        copy.roles.addAll((roles + other?.roles).unique())
        copy.organization?.merge(organization, other?.organization)
        copy
    }
}
