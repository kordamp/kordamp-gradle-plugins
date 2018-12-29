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
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.Credentials

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
class Bintray extends AbstractFeature {
    private static final String PLUGIN_ID = 'org.kordamp.gradle.bintray'

    String repo
    String userOrg
    String name
    String githubRepo
    final Credentials credentials = new Credentials()

    boolean skipMavenSync = false

    private boolean skipMavenSyncSet

    Bintray(Project project) {
        super(project)
        doSetEnabled(false)
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    @CompileDynamic
    Map<String, Map<String, Object>> toMap() {
        Map map = [enabled: enabled]

        if (enabled) {
            map.name = getName()
            map.userOrg = userOrg
            map.repo = getRepo()
            map.githubRepo = getGithubRepo()
            map.skipMavenSync = skipMavenSync
            if (!credentials.empty) {
                map.credentials = [
                    username: credentials.username,
                    password: ('*' * 12)
                ]
            }
        }

        ['bintray': map]
    }

    String getRepo() {
        repo ?: 'maven'
    }

    String getName() {
        this.@name ?: project.name
    }

    String getGithubRepo() {
        githubRepo ?: (userOrg && getName() ? "${userOrg}/${getName()}" : '')
    }

    void setSkipMavenSync(boolean skipMavenSync) {
        this.skipMavenSync = skipMavenSync
        this.skipMavenSyncSet = true
    }

    boolean isSkipMavenSyncSet() {
        this.skipMavenSyncSet
    }

    void credentials(Action<? super Credentials> action) {
        action.execute(credentials)
    }

    void credentials(@DelegatesTo(Credentials) Closure action) {
        ConfigureUtil.configure(action, credentials)
    }

    void copyInto(Bintray copy) {
        super.copyInto(copy)

        copy.@skipMavenSync = skipMavenSync
        copy.@skipMavenSyncSet = skipMavenSyncSet
        copy.@repo = this.@repo
        copy.@name = this.@name
        copy.userOrg = userOrg
        copy.@githubRepo = this.@githubRepo
        credentials.copyInto(copy.credentials)
    }

    static void merge(Bintray o1, Bintray o2) {
        AbstractFeature.merge(o1, o2)
        o1.setSkipMavenSync((boolean) (o1.skipMavenSyncSet ? o1.skipMavenSync : o2.skipMavenSync))
        o1.name = o1.@name ?: o2.@name
        o1.repo = o1.@repo ?: o2.@repo
        o1.userOrg = o1.userOrg ?: o2.userOrg
        o1.githubRepo = o1.@githubRepo ?: o2.githubRepo
        o1.credentials.merge(o1.credentials, o2.credentials)
    }

    List<String> validate(ProjectConfigurationExtension extension) {
        List<String> errors = []

        if (!enabled) return errors

        if (isBlank(userOrg)) {
            errors << "[${project.name}] Bintray userOrg is blank".toString()
        }

        if (isBlank(credentials.username)) {
            errors << "[${project.name}] Bintray credentials.username is blank".toString()
        }
        if (isBlank(credentials.password)) {
            errors << "[${project.name}] Bintray credentials.password is blank".toString()
        }

        errors.addAll(extension.info.links.validate(extension))

        errors
    }
}
