/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2023 Andres Almiray.
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

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.License
import org.kordamp.gradle.plugin.base.model.LicenseSet
import org.kordamp.gradle.plugin.base.model.LicenseSetImpl
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

    final LicenseSetImpl licenses
    Set<String> excludes = new LinkedHashSet<>()
    Set<String> includes = new LinkedHashSet<>()
    Map<String, String> mappings = [:]
    final Set<String> excludedSourceSets = new LinkedHashSet<>()

    Licensing(ProjectConfigurationExtension config, Project project) {
        super(config, project, PLUGIN_ID)
        licenses = new LicenseSetImpl(project.objects)
    }

    LicenseSet getLicenses() {
        this.licenses
    }

    @Override
    protected AbstractFeature getParentFeature() {
        return project.rootProject.extensions.getByType(ProjectConfigurationExtension).licensing
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        map.excludedSourceSets = excludedSourceSets
        map.licenses = licenses.toMap()
        map.excludes = excludes
        map.includes = includes
        map.mappings = mappings

        new LinkedHashMap<>('licensing': map)
    }

    void licenses(Action<? super LicenseSet> action) {
        action.execute(licenses)
    }

    void licenses(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = LicenseSet) Closure<Void> action) {
        ConfigureUtil.configure(action, licenses)
    }

    void include(String str) {
        includes << str
    }

    void exclude(String str) {
        excludes << str
    }

    void addMapping(String ext, String style) {
        if (isNotBlank(ext) && isNotBlank(style)) {
            mappings[ext] = style
        }
    }

    void excludeSourceSet(String s) {
        if (isNotBlank(s)) {
            excludedSourceSets << s
        }
    }

    static void merge(Licensing o1, Licensing o2) {
        AbstractFeature.merge(o1, o2)
        CollectionUtils.merge(o1.excludedSourceSets, o2?.excludedSourceSets)
        LicenseSetImpl.merge(o1.@licenses, o2?.@licenses)
        o1.excludes = CollectionUtils.merge(o1.excludes, o2.excludes, false)
        o1.includes = CollectionUtils.merge(o1.includes, o2.includes, false)
        o1.mappings = CollectionUtils.merge(o1.mappings, o2?.mappings, false)
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
}
