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
import org.kordamp.gradle.util.CollectionUtils

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
class License {
    LicenseId id
    String name
    String url
    String distribution = 'repo'
    String comments
    boolean primary
    List<String> aliases = []

    Map<String, Object> toMap() {
        new LinkedHashMap<String, Object>([
            id          : this.@id?.name(),
            name        : name,
            url         : url,
            distribution: distribution,
            comments    : comments,
            primary     : primary,
            aliases     : aliases
        ])
    }

    void setLicenseId(LicenseId id) {
        this.@id = id
    }

    LicenseId getLicenseId() {
        this.@id
    }

    void setId(String id) {
        this.@id = LicenseId.findByLiteral(id.trim())
    }

    String getId() {
        this.@id?.spdx()
    }

    String getName() {
        this.@id?.spdx() ?: name
    }

    String getUrl() {
        this.@id?.url() ?: url
    }

    static void merge(License o1, License o2) {
        o1.@id = o1.@id ?: o2?.@id
        o1.name = o1.@name ?: o2?.name
        o1.url = o1.@url ?: o2?.url
        o1.distribution = o1.distribution ?: o2?.distribution
        o1.comments = o1.comments ?: o2?.comments
        o1.primary = o1.primary ?: o2?.primary
        o1.aliases = CollectionUtils.merge(o1.aliases, o2?.aliases, false)
    }
}
