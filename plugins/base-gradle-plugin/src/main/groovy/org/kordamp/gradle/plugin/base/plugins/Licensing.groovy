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
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.Canonical
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

    String mergeStrategy

    final LicenseSet licenses = new LicenseSet()

    Licensing(ProjectConfigurationExtension config, Project project) {
        super(config, project)
        doSetEnabled(project.plugins.findPlugin(PLUGIN_ID) != null)
    }

    @Override
    String toString() {
        toMap().toString()
    }

    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        map.mergeStrategy = mergeStrategy
        map.licenses = licenses.licenses.collectEntries { License license ->
            [(license.licenseId?.name() ?: license.name): license.toMap()]
        }

        new LinkedHashMap<>('licensing': map)
    }

    void normalize() {
        if (!enabledSet && isRoot()) {
            setEnabled(project.plugins.findPlugin(PLUGIN_ID) != null)
        }
    }

    void licenses(Action<? super LicenseSet> action) {
        action.execute(licenses)
    }

    void licenses(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = LicenseSet) Closure action) {
        ConfigureUtil.configure(action, licenses)
    }

    void copyInto(Licensing copy) {
        super.copyInto(copy)
        copy.@mergeStrategy = this.@mergeStrategy
        licenses.copyInto(copy.licenses)
    }

    static void merge(Licensing o1, Licensing o2) {
        AbstractFeature.merge(o1, o2)
        o1.mergeStrategy = o1.mergeStrategy? o1.mergeStrategy : o2?.mergeStrategy
        switch (o1.mergeStrategy) {
            case 'overwrite':
                break
            case 'merge':
            default:
                LicenseSet.merge(o1.licenses, o2.licenses)
                break
        }
    }

    List<String> validate(ProjectConfigurationExtension extension) {
        List<String> errors = []

        if (!enabled) return errors

        if (!(mergeStrategy in ['merge', 'overwrite'])) {
            errors << "Invalid value for licensing.mergeStrategy '${mergeStrategy}'. It should be one of merge, overwrite".toString()
        }

        errors = licenses.validate(extension)

        errors
    }

    List<License> allLicenses() {
        licenses.licenses
    }

    boolean isEmpty() {
        licenses.isEmpty()
    }

    List<String> resolveBintrayLicenseIds() {
        List<String> ids = allLicenses().collect { it.licenseId?.bintray() ?: '' }.unique()
        ids.remove('')
        ids
    }
}
