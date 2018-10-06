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
package org.kordamp.gradle.plugins

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.model.Credentials
import org.kordamp.gradle.model.Information

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
@Canonical
@EqualsAndHashCode(excludes = ['project'])
@ToString(includeNames = true, excludes = ['project'])
class Bintray {
    String repo
    String userOrg
    String name
    String githubRepo
    final Credentials credentials = new Credentials()
    boolean enabled = true

    private boolean enabledSet
    private final Project project

    Bintray(Project project) {
        this.project = project
    }

    String getName() {
        name ?: project.name
    }

    String getGithubRepo() {
        githubRepo ?: (userOrg && getName() ? "${userOrg}/${getName()}" : '')
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

    static void merge(Bintray o1, Bintray o2) {
        o1.setEnabled((boolean)(o1.enabledSet ? o1.enabled : o2.enabled))
        o1.repo = o1.repo ?: o2.repo
        o1.userOrg = o1.userOrg ?: o2.userOrg
        o1.githubRepo = o1.githubRepo ?: o2.githubRepo
        o1.credentials.merge(o1.credentials, o2.credentials)
    }

    List<String> validate(Information info) {
        List<String> errors = []

        if (!enabled) return errors

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

        errors.addAll(info.links.validate(project))

        errors
    }
}
