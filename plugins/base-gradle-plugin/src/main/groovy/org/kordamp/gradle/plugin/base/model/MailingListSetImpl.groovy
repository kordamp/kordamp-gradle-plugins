/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2023 Andres Almiray.
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
class MailingListSetImpl extends AbstractDomainSet<MailingList> implements MailingListSet {
    final ListProperty<MailingList> mailingLists

    MailingListSetImpl(ObjectFactory objects) {
        mailingLists = objects.listProperty(MailingList).convention(Providers.<List<MailingList>>notDefined())
    }

    @Override
    protected Collection<MailingList> getDomainObjects() {
        getMailingLists()
    }

    @Override
    protected void clearDomainSet() {
        mailingLists.set([])
    }

    @Override
    protected void populateMap(Map<String, Object> map) {
        getMailingLists().collectEntries(map) { MailingList mailingList ->
            [(mailingList.name): mailingList.toMap()]
        }
    }

    @Override
    List<MailingList> getMailingLists() {
        mailingLists.getOrElse([])
    }

    @Override
    @CompileDynamic
    void mailingList(Action<? super MailingList> action) {
        MailingList mailingList = new MailingList()
        action.execute(mailingList)
        mailingLists.add(mailingList)
    }

    @Override
    @CompileDynamic
    void mailingList(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = MailingList) Closure<Void> action) {
        MailingList mailingList = new MailingList()
        ConfigureUtil.configure(action, mailingList)
        mailingLists.add(mailingList)
    }

    @CompileDynamic
    static void merge(MailingListSetImpl o1, MailingListSetImpl o2) {
        o1.mergeStrategy = (o1.mergeStrategy ?: o2?.mergeStrategy) ?: MergeStrategy.UNIQUE

        switch (o1.mergeStrategy) {
            case MergeStrategy.OVERRIDE:
                if (o1.mailingLists.isEmpty() && !o2?.mailingLists?.isEmpty()) {
                    o1.@mailingLists.addAll(o2.mailingLists)
                }
                break
            case MergeStrategy.PREPEND:
                List<MailingList> l1 = o1.mailingLists ?: []
                List<MailingList> l2 = o2?.mailingLists ?: []
                o1.@mailingLists.set(l1 + l2)
                break
            case MergeStrategy.APPEND:
                List<MailingList> l1 = o1.mailingLists ?: []
                List<MailingList> l2 = o2?.mailingLists ?: []
                o1.@mailingLists.set(l2 + l1)
                break
            case MergeStrategy.UNIQUE:
            default:
                doMerge(o1, o2)
                break
        }
    }

    @CompileDynamic
    private static void doMerge(MailingListSetImpl o1, MailingListSetImpl o2) {
        Map<String, MailingList> a = o1.mailingLists.collectEntries { [(it.name): it] }
        Map<String, MailingList> b = o2?.mailingLists?.collectEntries { [(it.name): it] } ?: [:]

        a.each { k, mailingList ->
            MailingList.merge(mailingList, b.remove(k))
        }
        a.putAll(b)
        o1.@mailingLists.set([])
        o1.@mailingLists.addAll(a.values())
    }

    @Override
    boolean isEmpty() {
        !mailingLists.present || mailingLists.get().isEmpty()
    }
}