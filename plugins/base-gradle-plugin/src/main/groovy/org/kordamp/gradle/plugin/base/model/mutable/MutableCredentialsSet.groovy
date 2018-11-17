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
import org.kordamp.gradle.plugin.base.model.CredentialsSet

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
@ToString(includeNames = true)
class MutableCredentialsSet implements CredentialsSet {
    static final String GITHUB = 'github'
    static final String SONATYPE = 'sonatype'

    final Map<String, MutableCredentials> credentials = new LinkedHashMap<>()

    @Override
    String toString() {
        toMap().toString()
    }

    @CompileDynamic
    Map<String, Map<String, Object>> toMap() {
        if (isEmpty()) return [:]

        [
            github  : github.toMap(),
            sonatype: sonatype.toMap()
        ]
    }

    void github(Action<? super MutableCredentials> action) {
        action.execute(credentials.computeIfAbsent(GITHUB, { k -> new MutableCredentials() }))
    }

    void sonatype(Action<? super MutableCredentials> action) {
        action.execute(credentials.computeIfAbsent(SONATYPE, { k -> new MutableCredentials() }))
    }

    void github(@DelegatesTo(MutableCredentials) Closure action) {
        ConfigureUtil.configure(action, credentials.computeIfAbsent(GITHUB, { k -> new MutableCredentials() }))
    }

    void sonatype(@DelegatesTo(MutableCredentials) Closure action) {
        ConfigureUtil.configure(action, credentials.computeIfAbsent(SONATYPE, { k -> new MutableCredentials() }))
    }

    @Override
    MutableCredentials getGithub() {
        credentials.get(GITHUB)
    }

    @Override
    MutableCredentials getSonatype() {
        credentials.get(SONATYPE)
    }

    void copyInto(MutableCredentialsSet credentialsSet) {
        credentials.each { k, v -> credentialsSet.credentials.put(k, v.copyOf()) }
    }

    static void merge(MutableCredentialsSet o1, MutableCredentialsSet o2) {
        MutableCredentials github = MutableCredentials.merge(o1?.getGithub(), o2?.getGithub())
        MutableCredentials sonatype = MutableCredentials.merge(o1?.getSonatype(), o2?.getSonatype())

        if (github) o1.credentials.put(GITHUB, github)
        if (sonatype) o1.credentials.put(SONATYPE, sonatype)
    }

    @Override
    boolean isEmpty() {
        credentials.isEmpty()
    }
}
