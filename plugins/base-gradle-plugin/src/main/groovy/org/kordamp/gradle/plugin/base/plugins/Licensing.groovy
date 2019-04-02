/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.License
import org.kordamp.gradle.plugin.base.model.LicenseSet

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
class Licensing extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.licensing'

    final LicenseSet licenses = new LicenseSet()

    Licensing(ProjectConfigurationExtension config, Project project) {
        super(config, project)
        doSetEnabled(project.plugins.findPlugin(PLUGIN_ID) != null)
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @CompileDynamic
    Map<String, Map<String, Object>> toMap() {
        Map map = [enabled: enabled]

        map.licenses = licenses.licenses.collectEntries { License license ->
            [(license.licenseId?.name() ?: license.name): license.toMap()]
        }

        ['licensing': map]
    }

    void normalize() {
        if (!enabledSet && isRoot()) {
            setEnabled(project.plugins.findPlugin(PLUGIN_ID) != null)
        }
    }

    void licenses(Action<? super LicenseSet> action) {
        action.execute(licenses)
    }

    void licenses(@DelegatesTo(LicenseSet) Closure action) {
        ConfigureUtil.configure(action, licenses)
    }

    void copyInto(Licensing copy) {
        super.copyInto(copy)
        licenses.copyInto(copy.licenses)
    }

    static void merge(Licensing o1, Licensing o2) {
        AbstractFeature.merge(o1, o2)
        LicenseSet.merge(o1.licenses, o2.licenses)
    }

    List<String> validate(ProjectConfigurationExtension extension) {
        List<String> errors = []

        if (!enabled) return errors

        errors = licenses.validate(extension)

        errors
    }

    List<License> allLicenses() {
        licenses.licenses
    }

    boolean isEmpty() {
        licenses.isEmpty()
    }

    @CompileDynamic
    List<String> resolveBintrayLicenseIds() {
        List<String> ids = allLicenses().collect { it.licenseId?.bintray() ?: '' }.unique()
        ids.remove('')
        ids
    }
}
