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

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.util.CollectionUtils

import static org.kordamp.gradle.util.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.39.0
 */
@CompileStatic
@Canonical
@ToString(includeNames = true)
class Plugin {
    Boolean enabled
    String id
    String name
    String displayName
    String description
    String implementationClass
    List<String> tags = []

    @Override
    String toString() {
        toMap().toString()
    }

    Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>([
            id                 : id,
            displayName        : displayName,
            description        : description,
            implementationClass: implementationClass,
            tags               : tags
        ])

        new LinkedHashMap<>((name): map)
    }

    static Plugin merge(Plugin o1, Plugin o2) {
        o1.id = o1.id ?: o2?.id
        o1.name = o1.name ?: o2?.name
        o1.displayName = o1.displayName ?: o2?.displayName
        o1.description = o1.description ?: o2?.description
        o1.implementationClass = o1.implementationClass ?: o2?.implementationClass
        o1.tags = CollectionUtils.merge(o1.tags, o2?.tags, false)

        o1
    }

    boolean getEnabled() {
        this.@enabled == null || this.@enabled
    }

    List<String> validate(ProjectConfigurationExtension extension) {
        List<String> errors = []

        if (isBlank(name)) {
            errors << "[${extension.project.name}] Plugin name is blank".toString()
        }
        if (isBlank(id)) {
            errors << "[${extension.project.name}] Plugin id is blank".toString()
        }
        // if (isBlank(displayName)) {
        //     errors << "[${extension.project.name}] Plugin displayName is blank".toString()
        // }
        // if (isBlank(description)) {
        //     errors << "[${extension.project.name}] Plugin description is blank".toString()
        // }
        if (isBlank(implementationClass)) {
            errors << "[${extension.project.name}] Plugin implementationClass is blank".toString()
        }
        if (!tags && !extension.info.tags) {
            errors << "[${extension.project.name}] Plugin has no tags defined".toString()
        }

        errors
    }

    List<String> resolveTags(ProjectConfigurationExtension extension) {
        tags ?: extension.info.tags
    }
}
