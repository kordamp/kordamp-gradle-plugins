/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 the original author or authors.
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
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
@Canonical
@ToString(includeNames = true)
class LicenseSet {
    final List<License> licenses = []

    void license(Action<? super License> action) {
        License license = new License()
        action.execute(license)
        licenses << license
    }

    void license(@DelegatesTo(License) Closure action) {
        License license = new License()
        ConfigureUtil.configure(action, license)
        licenses << license
    }

    void copyInto(LicenseSet licenseSet) {
        licenseSet.licenses.addAll(licenses.collect { it.copyOf() })
    }

    static void merge(LicenseSet o1, LicenseSet o2) {
        Map<String, License> a = o1.licenses.collectEntries { [(it.name): it] }
        Map<String, License> b = o2.licenses.collectEntries { [(it.name): it] }

        a.each { k, license ->
            License.merge(license, b.remove(k))
        }
        a.putAll(b)
        o1.licenses.clear()
        o1.licenses.addAll(a.values())
    }

    void forEach(Closure action) {
        licenses.each(action)
    }

    @CompileDynamic
    List<String> resolveBintrayLicenseIds() {
        List<String> ids = licenses.collect { it.id?.bintray() ?: '' }.unique()
        ids.remove('')
        ids
    }

    List<String> validate(Project project) {
        List<String> errors = []

        if (licenses.isEmpty()) {
            errors << "[${project.name}] No licenses have been defined".toString()
        }

        errors
    }
}
