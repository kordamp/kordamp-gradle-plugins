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
import org.kordamp.gradle.plugin.base.model.RepositorySet

/**
 * @author Andres Almiray
 * @since 0.9.0
 */
@CompileStatic
@Canonical
@ToString(includeNames = true)
class MutableRepositorySet implements RepositorySet {
    final List<MutableRepository> repositories = []

    @Override
    String toString() {
        toMap().toString()
    }

    @CompileDynamic
    Map<String, Map<String, Object>> toMap() {
        if (isEmpty()) return [:]

        repositories.collectEntries { MutableRepository repository ->
            [(repository.name): repository.toMap()]
        }
    }

    MutableRepository getRepository(String name) {
        repositories.find { it.name == name }
    }

    void repository(Action<? super MutableRepository> action) {
        MutableRepository repository = new MutableRepository()
        action.execute(repository)
        repositories << repository
    }

    void repository(@DelegatesTo(MutableRepository) Closure action) {
        MutableRepository repository = new MutableRepository()
        ConfigureUtil.configure(action, repository)
        repositories << repository
    }

    void copyInto(MutableRepositorySet repositorySet) {
        repositorySet.repositories.addAll(repositories.collect { it.copyOf() })
    }

    static void merge(MutableRepositorySet o1, MutableRepositorySet o2) {
        Map<String, MutableRepository> a = o1.repositories.collectEntries { [(it.name): it] }
        Map<String, MutableRepository> b = o2.repositories.collectEntries { [(it.name): it] }

        a.each { k, repository ->
            MutableRepository.merge(repository, b.remove(k))
        }
        a.putAll(b)
        o1.repositories.clear()
        o1.repositories.addAll(a.values())
    }

    void forEach(Closure action) {
        repositories.each(action)
    }

    boolean isEmpty() {
        repositories.isEmpty()
    }
}
