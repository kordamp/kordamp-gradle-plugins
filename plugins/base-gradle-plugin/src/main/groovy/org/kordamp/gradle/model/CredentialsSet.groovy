/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 the original author or authors.
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
package org.kordamp.gradle.model

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.gradle.api.Action
import org.gradle.util.ConfigureUtil

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
@Canonical
@ToString(includeNames = true)
class CredentialsSet {
    final Map<String, Credentials> credentials = new LinkedHashMap<>()

    void github(Action<? super Credentials> action) {
        action.execute(credentials.computeIfAbsent('github', { k -> new Credentials() }))
    }

    void sonatype(Action<? super Credentials> action) {
        action.execute(credentials.computeIfAbsent('sonatype', { k -> new Credentials() }))
    }

    void github(@DelegatesTo(Credentials) Closure action) {
        ConfigureUtil.configure(action, credentials.computeIfAbsent('github', { k -> new Credentials() }))
    }

    void sonatype(@DelegatesTo(Credentials) Closure action) {
        ConfigureUtil.configure(action, credentials.computeIfAbsent('sonatype', { k -> new Credentials() }))
    }

    Credentials getGithub() {
        credentials.get('github')
    }

    Credentials getSonatype() {
        credentials.get('sonatype')
    }

    void copyInto(CredentialsSet credentialsSet) {
        credentials.each { k, v -> credentialsSet.credentials.put(k, v.copyOf()) }
    }

    static void merge(CredentialsSet o1, CredentialsSet o2) {
        Credentials github = Credentials.merge(o1?.getGithub(), o2?.getGithub())
        Credentials sonatype = Credentials.merge(o1?.getSonatype(), o2?.getSonatype())

        if (github) o1.credentials.put('github', github)
        if (sonatype) o1.credentials.put('sonatype', sonatype)
    }

    boolean isEmpty() {
        credentials.isEmpty()
    }
}
