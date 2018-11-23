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

    final Map<String, MutableCredentials> credentialsMap = new LinkedHashMap<>()

    @Override
    String toString() {
        toMap().toString()
    }

    @CompileDynamic
    Map<String, Map<String, Object>> toMap() {
        if (isEmpty()) return [:]

        credentialsMap.collectEntries { k, v ->
            [(k): v.toMap()]
        }
    }

    void github(Action<? super MutableCredentials> action) {
        action.execute(credentialsMap.computeIfAbsent(GITHUB, { k -> new MutableCredentials() }))
    }

    void sonatype(Action<? super MutableCredentials> action) {
        action.execute(credentialsMap.computeIfAbsent(SONATYPE, { k -> new MutableCredentials() }))
    }

    void github(@DelegatesTo(MutableCredentials) Closure action) {
        ConfigureUtil.configure(action, credentialsMap.computeIfAbsent(GITHUB, { k -> new MutableCredentials() }))
    }

    void sonatype(@DelegatesTo(MutableCredentials) Closure action) {
        ConfigureUtil.configure(action, credentialsMap.computeIfAbsent(SONATYPE, { k -> new MutableCredentials() }))
    }

    void named(Action<? super MutableCredentials> action) {
        MutableCredentials c = new MutableCredentials()
        action.execute(c)
        credentialsMap.put(c.name, MutableCredentials.merge(c, getCredentials(c.name)))
    }

    void named(@DelegatesTo(MutableCredentials) Closure action) {
        MutableCredentials c = new MutableCredentials()
        ConfigureUtil.configure(action, c)
        credentialsMap.put(c.name, MutableCredentials.merge(c, getCredentials(c.name)))
    }

    @Override
    MutableCredentials getGithub() {
        credentialsMap.get(GITHUB)
    }

    @Override
    MutableCredentials getSonatype() {
        credentialsMap.get(SONATYPE)
    }

    MutableCredentials getCredentials(String name) {
        credentialsMap.get(name)
    }

    void copyInto(MutableCredentialsSet credentialsSet) {
        credentialsMap.each { k, v -> credentialsSet.credentialsMap.put(k, v.copyOf()) }
    }

    static void merge(MutableCredentialsSet o1, MutableCredentialsSet o2) {
        if (!o1.credentialsMap) {
            if (o2?.credentialsMap) {
                o1.credentialsMap.putAll(o2.credentialsMap)
            }
        } else {
            o1.credentialsMap.each { k, v ->
                MutableCredentials merged = MutableCredentials.merge(v, o2?.credentialsMap?.get(k))
                o1.credentialsMap.put(k, merged)
            }
            o2?.credentialsMap.each { k, v ->
                if (!o1.credentialsMap.containsKey(k)) {
                    o1.credentialsMap.put(k, v.copyOf())
                }
            }
        }
    }

    @Override
    boolean isEmpty() {
        credentialsMap.isEmpty()
    }
}
