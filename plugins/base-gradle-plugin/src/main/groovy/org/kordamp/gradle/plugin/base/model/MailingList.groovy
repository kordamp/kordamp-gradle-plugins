/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2025 Andres Almiray.
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

import groovy.transform.CompileStatic
import org.kordamp.gradle.util.CollectionUtils

/**
 * @author Andres Almiray
 * @since 0.22.0
 */
@CompileStatic
class MailingList {
    String name
    String subscribe
    String unsubscribe
    String post
    String archive
    List<String> otherArchives = []

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

    static MailingList merge(MailingList o1, MailingList o2) {
        o1.name = o1.name ?: o2?.name
        o1.subscribe = o1.subscribe ?: o2?.subscribe
        o1.unsubscribe = o1.unsubscribe ?: o2?.unsubscribe
        o1.post = o1.post ?: o2?.post
        o1.archive = o1.archive ?: o2?.archive
        o1.otherArchives = CollectionUtils.merge(o1.otherArchives, o2?.otherArchives, false)
        o1
    }
}
