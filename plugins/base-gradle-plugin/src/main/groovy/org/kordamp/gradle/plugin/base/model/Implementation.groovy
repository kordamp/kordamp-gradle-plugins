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

import groovy.transform.CompileStatic

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
class Implementation {
    boolean enabled = true
    String title
    String version
    String vendor
    String vendorId
    String url

    private boolean enabledSet

    Map<String, Object> toMap() {
        new LinkedHashMap<String, Object>([
            enabled: enabled,
            title  : title,
            version: version,
            vendor : vendor,
            vendorId : vendorId,
            url : url
        ])
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled
        this.enabledSet = true
    }

    boolean isEnabledSet() {
        this.enabledSet
    }

    static void merge(Implementation o1, Implementation o2) {
        o1.setEnabled((boolean) (o1.enabledSet ? o1.enabled : o2.enabled))
        o1.title = o1.title ?: o2.title
        o1.version = o1.version ?: o2.version
        o1.vendor = o1.vendor ?: o2.vendor
        o1.vendorId = o1.vendorId ?: o2.vendorId
        o1.url = o1.url ?: o2.url
    }
}
