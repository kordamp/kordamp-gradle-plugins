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
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
@Canonical
@ToString(includeNames = true)
class Bintray {
    String repo
    String userOrg
    String name
    String githubRepo
    final Credentials credentials = new Credentials()
    boolean enabled = true

    private Project project
    private boolean enabledSet

    Bintray(Project project) {
        this.project = project
    }

    String getName() {
        name ?: project.name
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled
        this.enabledSet = true
    }

    boolean isEnabledSet() {
        this.enabledSet
    }

    void credentials(Action<? super Credentials> action) {
        action.execute(credentials)
    }

    void credentials(@DelegatesTo(Credentials) Closure action) {
        ConfigureUtil.configure(action, credentials)
    }

    void copyInto(Bintray copy) {
        copy.@enabled = enabled
        copy.@enabledSet = enabledSet
        copy.repo = repo
        copy.userOrg = userOrg
        copy.githubRepo = githubRepo
        credentials.copyInto(copy.credentials)
    }

    void merge(Bintray o1, Bintray o2) {
        setEnabled(o1?.enabledSet ? o1.enabled : o2?.enabled)
        repo = o1?.repo ?: o2?.repo
        userOrg = o1?.userOrg ?: o2?.userOrg
        githubRepo = o1?.githubRepo ?: o2?.githubRepo
        credentials.merge(o1?.credentials, o2?.credentials)
    }

    List<String> validate(Project project) {
        List<String> errors = []

        if (isBlank(repo)) {
            errors << "[${project.name}] Bintray repo is blank".toString()
        }
        if (isBlank(userOrg)) {
            errors << "[${project.name}] Bintray userOrg is blank".toString()
        }

        if (isBlank(credentials.username)) {
            errors << "[${project.name}] Bintray credentials.username is blank".toString()
        }
        if (isBlank(credentials.password)) {
            errors << "[${project.name}] Bintray credentials.password is blank".toString()
        }

        errors
    }
}
