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
package org.kordamp.gradle.util

import groovy.transform.CompileStatic

/**
 * @author Andres Almiray
 * @since 0.17.0
 */
@CompileStatic
class CollectionUtils {
    static <T> Set<T> merge(Set<T> s1, Set<T> s2, boolean mutate = true) {
        if (!mutate) return doMerge(s1, s2)
        if (s2) s1.addAll(s2)
        s1
    }

    private static <T> Set<T> doMerge(Set<T> s1, Set<T> s2) {
        Set<T> r = new LinkedHashSet<>()
        r.addAll(s1)
        if (s2) r.addAll(s2)
        r
    }

    static <T> List<T> merge(List<T> l1, List<T> l2, boolean mutate = true) {
        if (!mutate) return doMerge(l1, l2)
        List<T> r = doMerge(l1, l2)
        l1.clear()
        l1.addAll(r)
        l1
    }

    private static <T> List<T> doMerge(List<T> l1, List<T> l2) {
        List<T> r = new ArrayList<>()
        r.addAll(l1)
        if (l2) r.addAll(l2)
        r.unique()
    }

    static <K, V> Map<K, V> merge(Map<K, V> m1, Map<K, V> m2, boolean mutate = true) {
        if (!mutate) doMerge(m1, m2)
        Map<K, V> r = doMerge(m1, m2)
        m1.clear()
        m1.putAll(r)
        m1
    }

    private static <K, V> Map<K, V> doMerge(Map<K, V> m1, Map<K, V> m2) {
        Map<K, V> r = new LinkedHashMap<>()
        if (m2) r.putAll(m2)
        r.putAll(m1)
        r
    }
}
