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
import org.gradle.api.Action
import org.gradle.util.ConfigureUtil

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
@Canonical
@ToString(includeNames = true)
class PersonSet {
    final List<Person> people = []

    void person(Action<? super Person> action) {
        Person person = new Person()
        action.execute(person)
        people << person
    }

    void person(@DelegatesTo(Person) Closure action) {
        Person person = new Person()
        ConfigureUtil.configure(action, person)
        people << person
    }

    void copyInto(PersonSet personSet) {
        personSet.people.addAll(people.collect { it.copyOf() })
    }

    static void merge(PersonSet o1, PersonSet o2) {
        Map<String, Person> a = o1.people.collectEntries { [(it.name): it] }
        Map<String, Person> b = o2.people.collectEntries { [(it.name): it] }

        a.each { k, person ->
            Person.merge(person, b.remove(k))
        }
        a.putAll(b)
        o1.people.clear()
        o1.people.addAll(a.values())
    }

    void forEach(Closure action) {
        people.each(action)
    }

    boolean isEmpty() {
        people.isEmpty()
    }
}
