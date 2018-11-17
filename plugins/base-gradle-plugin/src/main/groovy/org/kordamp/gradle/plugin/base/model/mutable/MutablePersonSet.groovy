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
package org.kordamp.gradle.plugin.base.model.mutable

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.gradle.api.Action
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.plugin.base.model.PersonSet

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
@ToString(includeNames = true)
class MutablePersonSet implements PersonSet {
    final List<MutablePerson> people = []

    @Override
    String toString() {
        toMap().toString()
    }

    @CompileDynamic
    Map<String, Map<String, Object>> toMap() {
        if (isEmpty()) return [:]

        people.collectEntries { MutablePerson person ->
            [(person.id ?: person.name): person.toMap()]
        }
    }

    void person(Action<? super MutablePerson> action) {
        MutablePerson person = new MutablePerson()
        action.execute(person)
        people << person
    }

    void person(@DelegatesTo(MutablePerson) Closure action) {
        MutablePerson person = new MutablePerson()
        ConfigureUtil.configure(action, person)
        people << person
    }

    void copyInto(MutablePersonSet personSet) {
        personSet.people.addAll(people.collect { it.copyOf() })
    }

    static void merge(MutablePersonSet o1, MutablePersonSet o2) {
        Map<String, MutablePerson> a = o1.people.collectEntries { [(it.name): it] }
        Map<String, MutablePerson> b = o2.people.collectEntries { [(it.name): it] }

        a.each { k, person ->
            MutablePerson.merge(person, b.remove(k))
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
