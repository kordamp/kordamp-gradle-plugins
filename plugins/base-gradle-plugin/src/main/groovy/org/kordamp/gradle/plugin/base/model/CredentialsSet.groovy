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
import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.gradle.api.Action
import org.gradle.util.ConfigureUtil

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
@ToString(includeNames = true)
class CredentialsSet {
    static final String GITHUB = 'github'
    static final String SONATYPE = 'sonatype'

    final Map<String, Credentials> credentialsMap = new LinkedHashMap<>()

    @Override
    String toString() {
        toMap().toString()
    }

    Map<String, Map<String, Object>> toMap() {
        if (isEmpty()) return [:]

        (Map<String, Map<String, Object>> ) credentialsMap.collectEntries { k, v ->
            [(k): v.toMap()]
        }
    }

    void github(Action<? super Credentials> action) {
        action.execute(credentialsMap.computeIfAbsent(GITHUB, { k -> new Credentials() }))
    }

    void sonatype(Action<? super Credentials> action) {
        action.execute(credentialsMap.computeIfAbsent(SONATYPE, { k -> new Credentials() }))
    }

    void github(@DelegatesTo(Credentials) Closure action) {
        ConfigureUtil.configure(action, credentialsMap.computeIfAbsent(GITHUB, { k -> new Credentials() }))
    }

    void sonatype(@DelegatesTo(Credentials) Closure action) {
        ConfigureUtil.configure(action, credentialsMap.computeIfAbsent(SONATYPE, { k -> new Credentials() }))
    }

    void named(Action<? super Credentials> action) {
        Credentials c = new Credentials()
        action.execute(c)
        credentialsMap.put(c.name, Credentials.merge(c, getCredentials(c.name)))
    }

    void named(@DelegatesTo(Credentials) Closure action) {
        Credentials c = new Credentials()
        ConfigureUtil.configure(action, c)
        credentialsMap.put(c.name, Credentials.merge(c, getCredentials(c.name)))
    }

    Credentials getGithub() {
        credentialsMap.get(GITHUB)
    }

    Credentials getSonatype() {
        credentialsMap.get(SONATYPE)
    }

    Credentials getCredentials(String name) {
        credentialsMap.get(name)
    }

    void copyInto(CredentialsSet credentialsSet) {
        credentialsMap.each { k, v -> credentialsSet.credentialsMap.put(k, v.copyOf()) }
    }

    static void merge(CredentialsSet o1, CredentialsSet o2) {
        if (!o1.credentialsMap) {
            if (o2?.credentialsMap) {
                o1.credentialsMap.putAll(o2.credentialsMap)
            }
        } else {
            o1.credentialsMap.each { k, v ->
                Credentials merged = Credentials.merge(v, o2?.credentialsMap?.get(k))
                o1.credentialsMap.put(k, merged)
            }
            o2?.credentialsMap.each { k, v ->
                if (!o1.credentialsMap.containsKey(k)) {
                    o1.credentialsMap.put(k, v.copyOf())
                }
            }
        }
    }

    boolean isEmpty() {
        credentialsMap.isEmpty()
    }
}
