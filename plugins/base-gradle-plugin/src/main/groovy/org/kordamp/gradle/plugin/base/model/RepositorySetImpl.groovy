/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2021 Andres Almiray.
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
import org.gradle.api.provider.ListProperty
import org.kordamp.gradle.plugin.base.plugins.MergeStrategy
import org.kordamp.gradle.util.ConfigureUtil

/**
 * @author Andres Almiray
 * @since 0.41.0
 */
@CompileStatic
@PackageScope
class RepositorySetImpl extends AbstractDomainSet<Repository> implements RepositorySet {
    final ListProperty<Repository> repositories

    RepositorySetImpl(ObjectFactory objects) {
        repositories = objects.listProperty(Repository).convention(Providers.notDefined())
    }

    @Override
    protected Collection<Repository> getDomainObjects() {
        getRepositories()
    }

    @Override
    protected void clearDomainSet() {
        repositories.set([])
    }

    @Override
    protected void populateMap(Map<String, Object> map) {
        getRepositories().collectEntries(map) { Repository repository ->
            [(repository.name): repository.toMap()]
        }
    }

    @Override
    List<Repository> getRepositories() {
        repositories.getOrElse([])
    }

    @Override
    void repository(Action<? super Repository> action) {
        Repository repository = new Repository()
        action.execute(repository)
        repositories.add(repository)
    }

    @Override
    void repository(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Repository) Closure<Void> action) {
        Repository repository = new Repository()
        ConfigureUtil.configure(action, repository)
        repositories.add(repository)
    }

    @Override
    Repository getRepository(String name) {
        getRepositories().find { it.name == name }
    }

    static void merge(RepositorySetImpl o1, RepositorySetImpl o2) {
        o1.mergeStrategy = (o1.mergeStrategy ?: o2.mergeStrategy) ?: MergeStrategy.UNIQUE

        switch (o1.mergeStrategy) {
            case MergeStrategy.OVERRIDE:
                if (o1.repositories.isEmpty() && !o2?.repositories?.isEmpty()) {
                    o1.@repositories.addAll(o2.repositories)
                }
                break
            case MergeStrategy.PREPEND:
                List<Repository> l1 = o1.repositories ?: []
                List<Repository> l2 = o2?.repositories ?: []
                o1.@repositories.set(l1 + l2)
                break
            case MergeStrategy.APPEND:
                List<Repository> l1 = o1.repositories ?: []
                List<Repository> l2 = o2?.repositories ?: []
                o1.@repositories.set(l2 + l1)
                break
            case MergeStrategy.UNIQUE:
            default:
                doMerge(o1, o2)
                break
        }
    }

    @CompileDynamic
    private static void doMerge(RepositorySetImpl o1, RepositorySetImpl o2) {
        Map<String, Repository> a = o1.repositories.collectEntries { [(it.name): it] }
        Map<String, Repository> b = o2?.repositories?.collectEntries { [(it.name): it] } ?: [:]

        a.each { k, repository ->
            Repository.merge(repository, b.remove(k))
        }
        a.putAll(b)
        o1.@repositories.set([])
        o1.@repositories.addAll(a.values())
    }

    @Override
    boolean isEmpty() {
        !repositories.present || repositories.get().isEmpty()
    }
}