/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.kordamp.gradle.CollectionUtils

/**
 * @author Andres Almiray
 * @since 0.22.0
 */
@CompileStatic
@Canonical
@ToString(includeNames = true)
class MailingList {
    String name
    String subscribe
    String unsubscribe
    String post
    String archive
    List<String> otherArchives = []

    @Override
    String toString() {
        toMap().toString()
    }

    Map<String, Object> toMap() {
        new LinkedHashMap<String, Object>([
            name         : name,
            subscribe    : subscribe,
            unsubscribe  : unsubscribe,
            post         : post,
            archive      : archive,
            otherArchives: otherArchives
        ])
    }

    MailingList copyOf() {
        MailingList copy = new MailingList()
        copy.name = name
        copy.subscribe = subscribe
        copy.unsubscribe = unsubscribe
        copy.post = post
        copy.archive = archive
        List<String> rls = new ArrayList<>(copy.otherArchives)
        copy.otherArchives.clear()
        copy.otherArchives.addAll(rls + otherArchives)
        copy
    }

    static MailingList merge(MailingList o1, MailingList o2) {
        o1.name = o1.name ?: o2?.name
        o1.subscribe = o1.subscribe ?: o2?.subscribe
        o1.unsubscribe = o1.unsubscribe ?: o2?.unsubscribe
        o1.post = o1.post ?: o2?.post
        o1.archive = o1.archive ?: o2?.archive
        CollectionUtils.merge(o1.otherArchives, o2?.otherArchives)
        o1
    }
}
