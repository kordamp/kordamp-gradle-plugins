/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2024 Andres Almiray.
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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Action
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.kordamp.gradle.plugin.base.plugins.MergeStrategy
import org.kordamp.gradle.util.ConfigureUtil

/**
 * @author Andres Almiray
 * @since 0.41.0
 */
@CompileStatic
@PackageScope
class CredentialsSetImpl extends AbstractDomainSet<Credentials> implements CredentialsSet {
    static final String GITHUB = 'github'
    static final String SONATYPE = 'sonatype'

    final MapProperty<String, Credentials> credentialsMap

    CredentialsSetImpl(ObjectFactory objects) {
        credentialsMap = objects.mapProperty(String, Credentials).convention(Providers.<Map<String, Credentials>>notDefined())
    }

    @Override
    protected Collection<Credentials> getDomainObjects() {
        getCredentials().values()
    }

    @Override
    protected void clearDomainSet() {
        credentialsMap.set([:])
    }

    @Override
    protected void populateMap(Map<String, Object> map) {
        getCredentials().collectEntries(map) { String k, Credentials v -> [(k): v.toMap()] }
    }

    @Override
    Map<String, Credentials> getCredentials() {
        credentialsMap.getOrElse([:])
    }

    @Override
    void github(Action<? super Credentials> action) {
        action.execute(computeIfAbsent(GITHUB))
    }

    @Override
    void sonatype(Action<? super Credentials> action) {
        action.execute(computeIfAbsent(SONATYPE))
    }

    @Override
    void github(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Credentials) Closure<Void> action) {
        ConfigureUtil.configure(action, computeIfAbsent(GITHUB))
    }

    @Override
    void sonatype(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Credentials) Closure<Void> action) {
        ConfigureUtil.configure(action, computeIfAbsent(SONATYPE))
    }

    @Override
    void named(Action<? super Credentials> action) {
        Credentials c = new Credentials()
        action.execute(c)
        credentialsMap.put(c.name, Credentials.merge(c, getCredentials(c.name)))
    }

    @Override
    void named(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Credentials) Closure<Void> action) {
        Credentials c = new Credentials()
        ConfigureUtil.configure(action, c)
        credentialsMap.put(c.name, Credentials.merge(c, getCredentials(c.name)))
    }

    Credentials computeIfAbsent(String key) {
        Credentials c = null
        if (credentialsMap.present) {
            c = (Credentials) (credentialsMap.get().get(key) ?: new Credentials())
            credentialsMap.get().put(key, c)
        } else {
            c = new Credentials()
            Map<String, Credentials> map = [(key): c]
            credentialsMap.set(map)
        }
        c
    }

    @Override
    Credentials getGithub() {
        getCredentials().get(GITHUB)
    }

    @Override
    Credentials getSonatype() {
        getCredentials().get(SONATYPE)
    }

    @Override
    Credentials getCredentials(String name) {
        getCredentials().get(name)
    }

    static void merge(CredentialsSetImpl o1, CredentialsSetImpl o2) {
        o1.mergeStrategy = (o1.mergeStrategy ?: o2?.mergeStrategy) ?: MergeStrategy.UNIQUE

        switch (o1.mergeStrategy) {
            case MergeStrategy.OVERRIDE:
                if (o1.credentials.isEmpty() && !o2?.credentials?.isEmpty()) {
                    o1.@credentialsMap.putAll(o2.credentials)
                }
                break
            case MergeStrategy.PREPEND:
                Map<String, Credentials> l1 = o1.credentials ?: [:]
                Map<String, Credentials> l2 = o2?.credentials ?: [:]
                o1.@credentialsMap.set([:])
                o1.@credentialsMap.putAll(l1 + l2)
                break
            case MergeStrategy.APPEND:
                Map<String, Credentials> l1 = o1.credentials ?: [:]
                Map<String, Credentials> l2 = o2?.credentials ?: [:]
                o1.@credentialsMap.set([:])
                o1.@credentialsMap.putAll(l2 + l1)
                break
            case MergeStrategy.UNIQUE:
            default:
                doMerge(o1, o2)
                break
        }
    }

    @CompileDynamic
    private static void doMerge(CredentialsSetImpl o1, CredentialsSetImpl o2) {
        if (!o1.credentials) {
            if (o2?.credentials) {
                o1.credentialsMap.set([:])
                o1.credentialsMap.putAll(o2.credentials)
            }
        } else {
            o1.credentials.each { String k, Credentials v ->
                Credentials merged = Credentials.merge(v, o2?.getCredentials(k))
                if (merged) o1.credentialsMap.put(k, merged)
            }
            o2?.credentials?.each { String k, Credentials v ->
                if (!o1.credentials.containsKey(k)) {
                    o1.credentialsMap.put(k, v)
                }
            }
        }
    }

    @Override
    boolean isEmpty() {
        !credentialsMap.present || credentialsMap.get().isEmpty()
    }
}
