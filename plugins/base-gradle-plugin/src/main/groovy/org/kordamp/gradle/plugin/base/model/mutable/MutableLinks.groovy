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
package org.kordamp.gradle.plugin.base.model.mutable

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.model.Links

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
@ToString(includeNames = true)
class MutableLinks implements Links {
    String website
    String issueTracker
    String scm

    @Override
    String toString() {
        toMap().toString()
    }

    @CompileDynamic
    Map<String, Object> toMap() {
        [
            website     : website,
            issueTracker: issueTracker,
            scm         : scm
        ]
    }

    void copyInto(MutableLinks copy) {
        copy.website = website
        copy.issueTracker = issueTracker
        copy.scm = scm
    }

    static void merge(MutableLinks o1, MutableLinks o2) {
        o1.website = o1.website ?: o2.website
        o1.issueTracker = o1.issueTracker ?: o2.issueTracker
        o1.scm = o1.scm ?: o2.scm
    }

    List<String> validate(Project project) {
        List<String> errors = []

        if (isBlank(website)) {
            errors << "[${project.name}] Project links:website is blank".toString()
        }
        if (isBlank(issueTracker)) {
            errors << "[${project.name}] Project links:issueTracker is blank".toString()
        }
        if (isBlank(scm)) {
            errors << "[${project.name}] Project links:scm is blank".toString()
        }

        errors
    }
}
