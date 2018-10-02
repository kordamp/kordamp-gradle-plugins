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
package org.kordamp.gradle.model

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.gradle.api.Project

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
@Canonical
@ToString(includeNames = true)
class Links {
    String website
    String issueTracker
    String scm

    void copyInto(Links copy) {
        copy.website = website
        copy.issueTracker = issueTracker
        copy.scm = scm
    }

    void merge(Links o1, Links o2) {
        website = o1.website ?: o2.website
        issueTracker = o1.issueTracker ?: o2.issueTracker
        scm = o1.scm ?: o2.scm
    }

    List<String> validate(Project project) {
        List<String> errors = []

        if(isBlank(website)) {
            errors << "[${project.name}] Project website is blank".toString()
        }

        errors
    }
}
