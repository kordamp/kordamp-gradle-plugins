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
package org.kordamp.gradle.plugin.base.model

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.gradle.api.Action
import org.gradle.util.ConfigureUtil

/**
 * @author Andres Almiray
 * @since 0.22.0
 */
@CompileStatic
@Canonical
@ToString(includeNames = true)
class MailingListSet {
    final List<MailingList> mailingLists = []

    @Override
    String toString() {
        toMap().toString()
    }

    @CompileDynamic
    Map<String, Map<String, Object>> toMap() {
        if (isEmpty()) return [:]

        mailingLists.collectEntries { MailingList mailingList ->
            [(mailingList.name): mailingList.toMap()]
        }
    }

    void mailingList(Action<? super MailingList> action) {
        MailingList mailingList = new MailingList()
        action.execute(mailingList)
        mailingLists << mailingList
    }

    void mailingList(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = MailingList) Closure action) {
        MailingList mailingList = new MailingList()
        ConfigureUtil.configure(action, mailingList)
        mailingLists << mailingList
    }

    void copyInto(MailingListSet mailingListSet) {
        mailingListSet.mailingLists.addAll(mailingLists.collect { it.copyOf() })
    }

    static void merge(MailingListSet o1, MailingListSet o2) {
        Map<String, MailingList> a = o1.mailingLists.collectEntries { [(it.name): it] }
        Map<String, MailingList> b = o2.mailingLists.collectEntries { [(it.name): it] }

        a.each { k, mailingList ->
            MailingList.merge(mailingList, b.remove(k))
        }
        a.putAll(b)
        o1.mailingLists.clear()
        o1.mailingLists.addAll(a.values())
    }

    void forEach(Closure action) {
        mailingLists.each(action)
    }

    boolean isEmpty() {
        mailingLists.isEmpty()
    }
}
