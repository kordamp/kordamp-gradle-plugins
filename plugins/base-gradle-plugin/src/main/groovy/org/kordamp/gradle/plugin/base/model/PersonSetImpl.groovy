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
package org.kordamp.gradle.plugin.base.model

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Action
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.kordamp.gradle.plugin.base.plugins.MergeStrategy
import org.kordamp.gradle.util.ConfigureUtil

/**
 * @author Andres Almiray
 * @since 0.41.0
 */
@CompileStatic
@PackageScope
class PersonSetImpl extends AbstractDomainSet<Person> implements PersonSet {
    final ListProperty<Person> people

    PersonSetImpl(ObjectFactory objects) {
        people = objects.listProperty(Person).convention(Providers.notDefined())
    }

    @Override
    protected Collection<Person> getDomainObjects() {
        getPeople()
    }

    @Override
    protected void clearDomainSet() {
        people.set([])
    }

    @Override
    protected void populateMap(Map<String, Object> map) {
        getPeople().collectEntries(map) { Person person ->
            [(person.id ?: person.name): person.toMap()]
        }
    }

    @Override
    List<Person> getPeople() {
        people.getOrElse([])
    }

    @Override
    void person(Action<? super Person> action) {
        Person person = new Person()
        action.execute(person)
        people.add(person)
    }

    @Override
    void person(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Person) Closure<Void> action) {
        Person person = new Person()
        ConfigureUtil.configure(action, person)
        people.add(person)
    }

    static void merge(PersonSetImpl o1, PersonSetImpl o2) {
        o1.mergeStrategy = (o1.mergeStrategy ?: o2?.mergeStrategy) ?: MergeStrategy.UNIQUE

        switch (o1.mergeStrategy) {
            case MergeStrategy.OVERRIDE:
                if (o1.people.isEmpty() && !o2?.people?.isEmpty()) {
                    o1.@people.addAll(o2.people)
                }
                break
            case MergeStrategy.PREPEND:
                List<Person> l1 = o1.people ?: []
                List<Person> l2 = o2?.people ?: []
                o1.@people.set(l1 + l2)
                break
            case MergeStrategy.APPEND:
                List<Person> l1 = o1.people ?: []
                List<Person> l2 = o2?.people ?: []
                o1.@people.set(l2 + l1)
                break
            case MergeStrategy.UNIQUE:
            default:
                doMerge(o1, o2)
                break
        }
    }

    @CompileDynamic
    private static void doMerge(PersonSetImpl o1, PersonSetImpl o2) {
        Map<String, Person> a = o1.people.collectEntries { [(it.name): it] }
        Map<String, Person> b = o2?.people?.collectEntries { [(it.name): it] } ?: [:]

        a.each { k, person ->
            Person.merge(person, b.remove(k))
        }
        a.putAll(b)
        o1.@people.set([])
        o1.@people.addAll(a.values())
    }

    @Override
    boolean isEmpty() {
        !people.present || people.get().isEmpty()
    }
}
