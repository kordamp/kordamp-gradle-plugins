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
import org.kordamp.gradle.plugin.base.model.Repository

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
@ToString(includeNames = true)
class MutableRepository implements Repository {
    String name
    String url
    final MutableCredentials credentials = new MutableCredentials()

    @Override
    String toString() {
        toMap().toString()
    }

    @CompileDynamic
    Map<String, Object> toMap() {
        Map map = [
            name: name,
            url : url
        ]

        if (!credentials.empty) {
            map.credentials = [
                username: credentials.username,
                password: ('*' * 12)
            ]
        }

        map
    }

    String getName() {
        !isBlank(this.@name) ? this.@name : url
    }

    void credentials(Action<? super MutableCredentials> action) {
        action.execute(credentials)
    }

    void credentials(@DelegatesTo(MutableCredentials) Closure action) {
        ConfigureUtil.configure(action, credentials)
    }

    MutableRepository copyOf() {
        MutableRepository copy = new MutableRepository()
        copyInto(copy)
        copy
    }

    void copyInto(MutableRepository copy) {
        copy.name = name
        copy.url = url
        credentials.copyInto(copy.credentials)
    }

    static void merge(MutableRepository o1, MutableRepository o2) {
        o1.name = o1.name ?: o2?.name
        o1.url = o1.url ?: o2?.url
        o1.credentials.merge(o1.credentials, o2.credentials)
    }

    boolean isEmpty() {
        isBlank(url)
    }
}
