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
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.kordamp.gradle.plugin.base.plugins.MergeStrategy
import org.kordamp.gradle.util.ConfigureUtil

import static org.kordamp.gradle.util.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.22.0
 */
@CompileStatic
class CiManagement {
    String system
    String url
    MergeStrategy mergeStrategy

    final NotifierSetImpl notifiers

    CiManagement(ObjectFactory objects) {
        notifiers = new NotifierSetImpl(objects)
    }

    NotifierSet getNotifiers() {
        this.notifiers
    }

    void setMergeStrategy(String str) {
        mergeStrategy = MergeStrategy.of(str)
    }

    Map<String, Object> toMap() {
        new LinkedHashMap<String, Object>([
            system   : system,
            url      : url,
            notifiers: notifiers.toMap()
        ])
    }

    static void merge(CiManagement o1, CiManagement o2) {
        o1.system = o1.system ?: o2?.system
        o1.url = o1.url ?: o2?.url
        NotifierSetImpl.merge(o1.@notifiers, o2?.@notifiers)
    }

    boolean isEmpty() {
        isBlank(system) && isBlank(url)
    }

    void notifiers(Action<? super NotifierSet> action) {
        action.execute(notifiers)
    }

    void notifiers(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NotifierSet) Closure<Void> action) {
        ConfigureUtil.configure(action, notifiers)
    }
}
