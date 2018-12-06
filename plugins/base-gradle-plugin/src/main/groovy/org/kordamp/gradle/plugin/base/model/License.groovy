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
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
@ToString(includeNames = true)
class License {
    LicenseId id
    String name
    String url
    String distribution = 'repo'
    String comments
    boolean primary
    List<String> aliases = []

    @Override
    String toString() {
        toMap().toString()
    }

    @CompileDynamic
    Map<String, Object> toMap() {
        [
            id          : id?.name(),
            name        : name,
            url         : url,
            distribution: distribution,
            comments    : comments,
            primary     : primary,
            aliases     : aliases
        ]
    }

    void setId(LicenseId id) {
        this.id = id
    }

    String getName() {
        id?.spdx() ?: name
    }

    String getUrl() {
        id?.url() ?: url
    }

    void setId(CharSequence id) {
        this.id = LicenseId.findByLiteral(id.toString().trim())
    }

    License copyOf() {
        License copy = new License()
        copy.id = id
        copy.@name = this.@name
        copy.@url = this.@url
        copy.distribution = distribution
        copy.comments = comments
        copy.primary
        copy.aliases.addAll(aliases)
        copy
    }

    static void merge(License o1, License o2) {
        o1.id = o1.id ?: o2?.id
        o1.name = o1.@name ?: o2?.name
        o1.url = o1.@url ?: o2?.url
        o1.distribution = o1.distribution ?: o2?.distribution
        o1.comments = o1.comments ?: o2?.comments
        o1.primary = o1.primary ?: o2?.primary
        o1.aliases = (o1.aliases + o2?.aliases).unique()
    }
}
