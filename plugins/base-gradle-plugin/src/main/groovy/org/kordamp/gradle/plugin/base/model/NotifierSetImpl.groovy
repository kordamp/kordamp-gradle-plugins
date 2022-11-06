/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2022 Andres Almiray.
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
class NotifierSetImpl extends AbstractDomainSet<Notifier> implements NotifierSet {
    final ListProperty<Notifier> notifiers

    NotifierSetImpl(ObjectFactory objects) {
        notifiers = objects.listProperty(Notifier).convention(Providers.<List<Notifier>>notDefined())
    }

    @Override
    protected Collection<Notifier> getDomainObjects() {
        getNotifiers()
    }

    @Override
    protected void clearDomainSet() {
        notifiers.set([])
    }

    @Override
    protected void populateMap(Map<String, Object> map) {
        getNotifiers().collectEntries(map) { Notifier notifier ->
            [(notifier.id): notifier.toMap()]
        }
    }

    @Override
    List<Notifier> getNotifiers() {
        notifiers.getOrElse([])
    }

    @Override
    @CompileDynamic
    void notifier(Action<? super Notifier> action) {
        Notifier notifier = new Notifier()
        action.execute(notifier)
        notifiers.add(notifier)
    }

    @Override
    @CompileDynamic
    void notifier(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Notifier) Closure<Void> action) {
        Notifier notifier = new Notifier()
        ConfigureUtil.configure(action, notifier)
        notifiers.add(notifier)
    }

    @CompileDynamic
    static void merge(NotifierSetImpl o1, NotifierSetImpl o2) {
        o1.mergeStrategy = (o1.mergeStrategy ?: o2?.mergeStrategy) ?: MergeStrategy.UNIQUE

        switch (o1.mergeStrategy) {
            case MergeStrategy.OVERRIDE:
                if (o1.notifiers.isEmpty() && !o2?.notifiers?.isEmpty()) {
                    o1.@notifiers.addAll(o2.notifiers)
                }
                break
            case MergeStrategy.PREPEND:
                List<Notifier> l1 = o1.notifiers ?: []
                List<Notifier> l2 = o2?.notifiers ?: []
                o1.@notifiers.set(l1 + l2)
                break
            case MergeStrategy.APPEND:
                List<Notifier> l1 = o1.notifiers ?: []
                List<Notifier> l2 = o2?.notifiers ?: []
                o1.@notifiers.set(l2 + l1)
                break
            case MergeStrategy.UNIQUE:
            default:
                doMerge(o1, o2)
                break
        }
    }

    @CompileDynamic
    private static void doMerge(NotifierSetImpl o1, NotifierSetImpl o2) {
        Map<String, Notifier> a = o1.notifiers.collectEntries { [(it.name): it] }
        Map<String, Notifier> b = o2?.notifiers?.collectEntries { [(it.name): it] } ?: [:]

        a.each { k, notifier ->
            Notifier.merge(notifier, b.remove(k))
        }
        a.putAll(b)
        o1.@notifiers.set([])
        o1.@notifiers.addAll(a.values())
    }

    @Override
    boolean isEmpty() {
        !notifiers.present || notifiers.get().isEmpty()
    }
}
