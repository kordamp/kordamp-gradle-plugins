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

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
@ToString(includeNames = true)
class Organization {
    String name
    String url

    @Override
    String toString() {
        toMap().toString()
    }

    Map<String, Object> toMap() {
        new LinkedHashMap<String, Object>([
            name: name,
            url : url
        ])
    }

    Organization copyOf() {
        Organization copy = new Organization()
        copyInto(copy)
        copy
    }

    void copyInto(Organization copy) {
        copy.name = name
        copy.url = url
    }

    static void merge(Organization o1, Organization o2) {
        o1.name = o1.name ?: o2?.name
        o1.url = o1.url ?: o2?.url
    }

    boolean isEmpty() {
        isBlank(name) && isBlank(url)
    }
}
