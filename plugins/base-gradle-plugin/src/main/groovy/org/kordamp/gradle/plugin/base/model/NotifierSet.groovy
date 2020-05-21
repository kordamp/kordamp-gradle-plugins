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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.kordamp.gradle.util.ConfigureUtil

/**
 * @author Andres Almiray
 * @since 0.22.0
 */
@CompileStatic
class NotifierSet {
    final List<Notifier> notifiers = []

    @CompileDynamic
    Map<String, Map<String, Object>> toMap() {
        if (isEmpty()) return [:]

        notifiers.collectEntries { Notifier notifier ->
            [(notifier.id ?: notifier.name): notifier.toMap()]
        }
    }

    void notifier(Action<? super Notifier> action) {
        Notifier notifier = new Notifier()
        action.execute(notifier)
        notifiers << notifier
    }

    void notifier(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Notifier) Closure<Void> action) {
        Notifier notifier = new Notifier()
        ConfigureUtil.configure(action, notifier)
        notifiers << notifier
    }

    void copyInto(NotifierSet notifierSet) {
        notifierSet.notifiers.addAll(notifiers.collect { it.copyOf() })
    }

    static void merge(NotifierSet o1, NotifierSet o2) {
        Map<String, Notifier> a = o1.notifiers.collectEntries { [(it.id): it] }
        Map<String, Notifier> b = o2.notifiers.collectEntries { [(it.id): it] }

        a.each { k, notifier ->
            Notifier.merge(notifier, b.remove(k))
        }
        a.putAll(b)
        o1.notifiers.clear()
        o1.notifiers.addAll(a.values())
    }

    void forEach(Closure<Void> action) {
        notifiers.each(action)
    }

    boolean isEmpty() {
        notifiers.isEmpty()
    }
}
