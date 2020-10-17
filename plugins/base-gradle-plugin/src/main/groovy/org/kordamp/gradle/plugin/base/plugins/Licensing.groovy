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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.License
import org.kordamp.gradle.util.CollectionUtils
import org.kordamp.gradle.util.ConfigureUtil

import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
class Licensing extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.licensing'

    MergeStrategy mergeStrategy

    final LicenseSetImpl licenses
    final Set<String> excludedSourceSets = new LinkedHashSet<>()

    Licensing(ProjectConfigurationExtension config, Project project) {
        super(config, project, PLUGIN_ID)
        licenses = new LicenseSetImpl(project.objects)
    }

    LicenseSet getLicenses() {
        this.licenses
    }

    void setMergeStrategy(String str) {
        mergeStrategy = MergeStrategy.of(str)
    }

    @Override
    protected void normalizeEnabled() {
        if (!enabledSet) {
            setEnabled(isApplied())
        }
    }

    @Override
    protected AbstractFeature getParentFeature() {
        return project.rootProject.extensions.getByType(ProjectConfigurationExtension).licensing
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        map.mergeStrategy = mergeStrategy
        map.excludedSourceSets = excludedSourceSets
        map.licenses = licenses.licenses.collectEntries { License license ->
            [(license.licenseId?.name() ?: license.name): license.toMap()]
        }

        new LinkedHashMap<>('licensing': map)
    }

    void licenses(Action<? super LicenseSet> action) {
        action.execute(licenses)
    }

    void licenses(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = LicenseSet) Closure<Void> action) {
        ConfigureUtil.configure(action, licenses)
    }

    void excludeSourceSet(String s) {
        if (isNotBlank(s)) {
            excludedSourceSets << s
        }
    }

    static void merge(Licensing o1, Licensing o2) {
        AbstractFeature.merge(o1, o2)
        o1.mergeStrategy = o1.mergeStrategy ? o1.mergeStrategy : o2?.mergeStrategy
        CollectionUtils.merge(o1.excludedSourceSets, o2?.excludedSourceSets)
        switch (o1.mergeStrategy) {
            case MergeStrategy.OVERRIDE:
                if (o1.licenses.isEmpty()) {
                    o1.@licenses.addAll(o2.licenses)
                }
                break
            case MergeStrategy.PREPEND:
                List<License> l1 = o1.licenses.licenses ?: []
                List<License> l2 = o2?.licenses?.licenses ?: []
                o1.@licenses.@licenses.set(l1+l2)
                break
            case MergeStrategy.APPEND:
                List<License> l1 = o1.licenses.licenses ?: []
                List<License> l2 = o2?.licenses?.licenses ?: []
                o1.@licenses.@licenses.set(l2+l1)
                break
            case MergeStrategy.UNIQUE:
            default:
                LicenseSetImpl.merge(o1.@licenses, o2?.@licenses)
                break
        }
    }

    List<String> validate(ProjectConfigurationExtension extension) {
        List<String> errors = []

        if (!enabled) return errors

        if (!(mergeStrategy)) {
            mergeStrategy = MergeStrategy.UNIQUE
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

    static interface LicenseSet {
        Map<String, Map<String, Object>> toMap()

        List<License> getLicenses()

        void license(Action<? super License> action)

        void license(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = License) Closure<Void> action)

        List<String> validate(ProjectConfigurationExtension extension)

        boolean isEmpty()
    }

    private static class LicenseSetImpl implements LicenseSet {
        final ListProperty<License> licenses

        LicenseSetImpl(ObjectFactory objects) {
            licenses = objects.listProperty(License).convention(Providers.notDefined())
        }

        @Override
        @CompileDynamic
        Map<String, Map<String, Object>> toMap() {
            if (isEmpty()) return [:]

            licenses.get().collectEntries { License license ->
                [(license.id ?: license.name): license.toMap()]
            }
        }

        @Override
        List<License> getLicenses() {
            licenses.getOrElse([])
        }

        @Override
        void license(Action<? super License> action) {
            License license = new License()
            action.execute(license)
            licenses.add(license)
        }

        @Override
        void license(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = License) Closure<Void> action) {
            License license = new License()
            ConfigureUtil.configure(action, license)
            licenses.add(license)
        }

        @CompileDynamic
        static void merge(LicenseSetImpl o1, LicenseSetImpl o2) {
            Map<String, License> a = o1.licenses.collectEntries { [(it.name): it] }
            Map<String, License> b = o2?.licenses?.collectEntries { [(it.name): it] } ?: [:]

            a.each { k, license ->
                License.merge(license, b.remove(k))
            }
            a.putAll(b)
            o1.@licenses.set([])
            o1.@licenses.addAll(a.values())
        }

        void addAll(LicenseSet other) {
            if(!other?.isEmpty()) {
                licenses.addAll(other.licenses)
            }
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
            !licenses.present
        }
    }
}
