/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
package org.kordamp.gradle.plugin.base.model

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.gradle.api.Project

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.9.0
 */
@CompileStatic
@Canonical
@ToString(includeNames = true)
class Scm {
    String url
    String connection
    String developerConnection

    @Override
    String toString() {
        toMap().toString()
    }

    @CompileDynamic
    Map<String, Object> toMap() {
        [
            url                : url,
            connection         : connection,
            developerConnection: developerConnection
        ]
    }

    void copyInto(Scm copy) {
        copy.url = url
        copy.connection = connection
        copy.developerConnection = developerConnection
    }

    static void merge(Scm o1, Scm o2) {
        o1.url = o1.url ?: o2.url
        o1.connection = o1.connection ?: o2.connection
        o1.developerConnection = o1.developerConnection ?: o2.developerConnection
    }

    List<String> validate(Project project) {
        List<String> errors = []

        if (isBlank(url)) {
            errors << "[${project.name}] Project links:url is blank".toString()
        }

        errors
    }

    boolean isEmpty() {
        isBlank(url)
    }
}
