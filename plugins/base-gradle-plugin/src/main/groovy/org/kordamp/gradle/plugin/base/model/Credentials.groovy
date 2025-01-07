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

import static org.kordamp.gradle.util.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
class Credentials {
    String name
    String username
    String password

    Map<String, Object> toMap() {
        new LinkedHashMap<String, Object>([
            name    : name,
            username: username,
            password: password
        ])
    }

    static Credentials merge(Credentials o1, Credentials o2) {
        if (o1) {
            o1.name = o1.name ?: o2?.name
            o1.username = o1.username ?: o2?.username
            o1.password = o1.password ?: o2?.password
            return o1
        } else if (o2) {
            return o2
        } else {
            return null
        }
    }

    boolean isEmpty() {
        isBlank(username) && isBlank(password)
    }
}
