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
import org.kordamp.gradle.CollectionUtils
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
    static final String PLUGIN_ID = 'org.kordamp.gradle.bintray'

    String repo
    String userOrg
    String name
    String githubRepo
    final Credentials credentials = new Credentials()
    List<String> publications = new ArrayList<>()

    boolean skipMavenSync = false
    boolean publish = false
    boolean publicDownloadNumbers = true

    private boolean skipMavenSyncSet
    private boolean publishSet
    private boolean publicDownloadNumbersSet

    Bintray(ProjectConfigurationExtension config, Project project) {
        super(config, project)
        doSetEnabled(false)
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        if (enabled) {
            map.name = getName()
            map.userOrg = userOrg
            map.repo = getRepo()
            map.githubRepo = getGithubRepo()
            map.publish = publish
            map.publicDownloadNumbers = publicDownloadNumbers
            map.skipMavenSync = skipMavenSync
            map.publications = resolvePublications()
            if (!credentials.empty) {
                map.credentials = new LinkedHashMap<String, Object>([
                    username: credentials.username,
                    password: ('*' * 12)
                ])
            }
        }

        new LinkedHashMap<>('bintray': map)
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

    void setPublish(boolean publish) {
        this.publish = publish
        this.publishSet = true
    }

    boolean isPublishSet() {
        this.publishSet
    }

    void setPublicDownloadNumbers(boolean publicDownloadNumbers) {
        this.publicDownloadNumbers = publicDownloadNumbers
        this.publicDownloadNumbersSet = true
    }

    boolean isPublicDownloadNumbersSet() {
        this.publicDownloadNumbersSet
    }

    void credentials(Action<? super Credentials> action) {
        action.execute(credentials)
    }

    void credentials(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Credentials) Closure<Void> action) {
        ConfigureUtil.configure(action, credentials)
    }

    void copyInto(Bintray copy) {
        super.copyInto(copy)

        copy.@skipMavenSync = skipMavenSync
        copy.@skipMavenSyncSet = skipMavenSyncSet
        copy.@publish = publish
        copy.@publishSet = publishSet
        copy.@publicDownloadNumbers = publicDownloadNumbers
        copy.@publicDownloadNumbersSet = publicDownloadNumbersSet
        copy.@repo = this.@repo
        copy.@name = this.@name
        copy.userOrg = userOrg
        copy.@githubRepo = this.@githubRepo
        copy.publications.addAll(publications)
        credentials.copyInto(copy.credentials)
    }

    static void merge(Bintray o1, Bintray o2) {
        AbstractFeature.merge(o1, o2)
        o1.setSkipMavenSync((boolean) (o1.skipMavenSyncSet ? o1.skipMavenSync : o2.skipMavenSync))
        o1.setPublish((boolean) (o1.publishSet ? o1.publish : o2.publish))
        o1.setPublicDownloadNumbers((boolean) (o1.publicDownloadNumbersSet ? o1.publicDownloadNumbers : o2.publicDownloadNumbers))
        o1.name = o1.@name ?: o2.@name
        o1.repo = o1.@repo ?: o2.@repo
        o1.userOrg = o1.userOrg ?: o2.userOrg
        o1.githubRepo = o1.@githubRepo ?: o2.githubRepo
        CollectionUtils.merge(o1.publications, o2.publications)
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

    List<String> resolvePublications() {
        List<String> pubs = new ArrayList<>(publications)
        if (!pubs) pubs << 'main'
        if (config.plugin.enabled) {
            pubs << config.plugin.pluginName + 'PluginMarkerMaven'
        }
        pubs.unique()
    }
}
