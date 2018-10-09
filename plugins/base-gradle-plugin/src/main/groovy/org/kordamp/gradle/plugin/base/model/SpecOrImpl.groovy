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
package org.kordamp.gradle.plugin.base.model

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
@Canonical
@ToString(includeNames = true)
class SpecOrImpl {
    boolean enabled = true
    String title
    String version
    String vendor

    private boolean enabledSet

    void setEnabled(boolean enabled) {
        this.enabled = enabled
        this.enabledSet = true
    }

    boolean isEnabledSet() {
        this.enabledSet
    }

    SpecOrImpl copyOf() {
        SpecOrImpl copy = new SpecOrImpl()
        copy.@enabled = enabled
        copy.@enabledSet = enabledSet
        copy.title = title
        copy.version = version
        copy.vendor = vendor
        copy
    }

    static void merge(SpecOrImpl o1, SpecOrImpl o2) {
        o1.setEnabled((boolean) (o1.enabledSet ? o1.enabled : o2.enabled))
        o1.title = o1.title ?: o2.title
        o1.version = o1.version ?: o2.version
        o1.vendor = o1.vendor ?: o2.vendor
    }
}
