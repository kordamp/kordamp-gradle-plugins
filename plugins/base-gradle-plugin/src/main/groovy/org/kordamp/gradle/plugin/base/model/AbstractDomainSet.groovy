/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2020 Andres Almiray.
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


import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.kordamp.gradle.plugin.base.plugins.MergeStrategy

import java.util.function.Consumer

/**
 * @author Andres Almiray
 * @since 0.41.0
 */
@CompileStatic
@PackageScope
abstract class AbstractDomainSet<T> implements DomainSet<T> {
    MergeStrategy mergeStrategy

    private boolean inherits = true
    private boolean inheritsSet = false

    @Override
    void setMergeStrategy(String str) {
        mergeStrategy = MergeStrategy.of(str)
    }

    @Override
    void setMergeStrategy(MergeStrategy mergeStrategy) {
        this.mergeStrategy = mergeStrategy ?: MergeStrategy.UNIQUE
    }

    @Override
    void forEach(Consumer<? super T> consumer) {
        for (T t : getDomainObjects()) {
            consumer.accept(t)
        }
    }

    @Override
    Map<String, Object> toMap() {
        Map<String, Object> map = [:]

        map.inherits = isInherits()
        map.mergeStrategy = mergeStrategy

        if (isEmpty()) return map

        populateMap(map)
        map
    }

    void setInherits(boolean value) {
        if (!inheritsSet) {
            inheritsSet = true
            inherits = value
            if (!value) {
                clearDomainSet()
            }
        }
    }

    boolean isInherits() {
        this.inherits
    }

    protected abstract Collection<T> getDomainObjects()

    protected abstract void clearDomainSet()

    protected abstract void populateMap(Map<String, Object> map)
}