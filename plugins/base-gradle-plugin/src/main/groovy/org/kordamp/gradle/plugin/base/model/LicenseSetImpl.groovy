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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.plugins.MergeStrategy
import org.kordamp.gradle.util.ConfigureUtil

/**
 * @author Andres Almiray
 * @since 0.41.0
 */
@CompileStatic
class LicenseSetImpl extends AbstractDomainSet<License> implements LicenseSet {
    final ListProperty<License> licenses

    LicenseSetImpl(ObjectFactory objects) {
        licenses = objects.listProperty(License).convention(Providers.<List<License>>notDefined())
    }

    @Override
    protected Collection<License> getDomainObjects() {
        getLicenses()
    }

    @Override
    protected void clearDomainSet() {
        licenses.set([])
    }

    @Override
    protected void populateMap(Map<String, Object> map) {
        getLicenses().collectEntries(map) { License license ->
            [(license.id ?: license.name): license.toMap()]
        }
    }

    @Override
    List<License> getLicenses() {
        licenses.getOrElse([])
    }

    @Override
    @CompileDynamic
    void license(Action<? super License> action) {
        License license = new License()
        action.execute(license)
        licenses.add(license)
    }

    @Override
    @CompileDynamic
    void license(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = License) Closure<Void> action) {
        License license = new License()
        ConfigureUtil.configure(action, license)
        licenses.add(license)
    }

    @CompileDynamic
    static void merge(LicenseSetImpl o1, LicenseSetImpl o2) {
        o1.mergeStrategy = (o1.mergeStrategy ?: o2?.mergeStrategy) ?: MergeStrategy.UNIQUE

        switch (o1.mergeStrategy) {
            case MergeStrategy.OVERRIDE:
                if (o1.licenses.isEmpty() && !o2?.licenses?.isEmpty()) {
                    o1.@licenses.addAll(o2.licenses)
                }
                break
            case MergeStrategy.PREPEND:
                List<License> l1 = o1.licenses ?: []
                List<License> l2 = o2?.licenses ?: []
                o1.@licenses.set(l1 + l2)
                break
            case MergeStrategy.APPEND:
                List<License> l1 = o1.licenses ?: []
                List<License> l2 = o2?.licenses ?: []
                o1.@licenses.set(l2 + l1)
                break
            case MergeStrategy.UNIQUE:
            default:
                doMerge(o1, o2)
                break
        }
    }

    @CompileDynamic
    private static void doMerge(LicenseSetImpl o1, LicenseSetImpl o2) {
        Map<String, License> a = o1.licenses.collectEntries { [(it.name): it] }
        Map<String, License> b = o2?.licenses?.collectEntries { [(it.name): it] } ?: [:]

        a.each { k, license ->
            License.merge(license, b.remove(k))
        }
        a.putAll(b)
        o1.@licenses.set([])
        o1.@licenses.addAll(a.values())
    }

    @Override
    List<String> validate(ProjectConfigurationExtension extension) {
        List<String> errors = []

        if (isEmpty()) {
            errors << "[${extension.project.name}] No licenses have been defined".toString()
        }

        errors
    }

    @Override
    boolean isEmpty() {
        !licenses.present || licenses.get().isEmpty()
    }
}