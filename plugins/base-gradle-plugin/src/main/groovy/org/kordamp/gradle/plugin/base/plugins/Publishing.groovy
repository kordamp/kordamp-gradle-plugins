/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2025 Andres Almiray.
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
import org.kordamp.gradle.plugin.base.model.PomOptions
import org.kordamp.gradle.util.CollectionUtils
import org.kordamp.gradle.util.ConfigureUtil

import static org.kordamp.gradle.util.StringUtils.isBlank
import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
class Publishing extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.publishing'

    String releasesRepository
    String snapshotsRepository
    DefaultPomOptions pom = new DefaultPomOptions()
    List<String> publications = []
    List<String> scopes = []
    boolean useVersionExpressions = true
    boolean flattenPlatforms = false
    final Signing signing
    final MavenCentral mavenCentral

    private boolean useVersionExpressionsSet
    private boolean flattenPlatformsSet

    Publishing(ProjectConfigurationExtension config, Project project) {
        super(config, project, PLUGIN_ID)
        signing = new Signing(config, project)
        mavenCentral = new MavenCentral(config, project)
    }

    @Override
    protected AbstractFeature getParentFeature() {
        return project.rootProject.extensions.getByType(ProjectConfigurationExtension).publishing
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        map.putAll(signing.toMap())
        map.putAll(mavenCentral.toMap())
        map.releasesRepository = releasesRepository
        map.snapshotsRepository = snapshotsRepository
        map.publications = publications
        map.scopes = scopes
        map.useVersionExpressions = useVersionExpressions
        map.flattenPlatforms = flattenPlatforms
        map.pom = pom.toMap()

        new LinkedHashMap<>('publishing': map)
    }

    void postMerge() {
        if (!scopes) {
            scopes << 'compile'
            scopes << 'runtime'
        }
        if (isBlank(pom.packaging)) {
            pom.packaging = 'jar'
        }
        super.postMerge()
    }

    void pom(Action<? super PomOptions> action) {
        action.execute(pom)
    }

    void pom(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PomOptions) Closure<Void> action) {
        ConfigureUtil.configure(action, pom)
    }

    void signing(Action<? super Signing> action) {
        action.execute(signing)
    }

    void signing(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Signing) Closure<Void> action) {
        ConfigureUtil.configure(action, signing)
    }

    void mavenCentral(Action<? super MavenCentral> action) {
        action.execute(mavenCentral)
    }

    void mavenCentral(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = MavenCentral) Closure<Void> action) {
        ConfigureUtil.configure(action, mavenCentral)
    }

    void setUseVersionExpressions(boolean value) {
        this.useVersionExpressions = value
        this.useVersionExpressionsSet = true
    }

    boolean isUseVersionExpressionsSet() {
        this.useVersionExpressionsSet
    }

    void setFlattenPlatforms(boolean value) {
        this.flattenPlatforms = value
        this.flattenPlatformsSet = true
    }

    boolean isFlattenPlatformsSet() {
        this.flattenPlatformsSet
    }

    static void merge(Publishing o1, Publishing o2) {
        AbstractFeature.merge(o1, o2)
        o1.releasesRepository = o1.@releasesRepository ?: o2.@releasesRepository
        o1.snapshotsRepository = o1.@snapshotsRepository ?: o2.@snapshotsRepository
        Signing.merge(o1.signing, o2.signing)
        MavenCentral.merge(o1.mavenCentral, o2.mavenCentral)
        o1.@useVersionExpressions = o1.useVersionExpressionsSet ? o1.useVersionExpressions : o2.useVersionExpressions
        o1.@useVersionExpressionsSet = o1.useVersionExpressionsSet ?: o2.useVersionExpressionsSet
        o1.@flattenPlatforms = o1.flattenPlatformsSet ? o1.flattenPlatforms : o2.flattenPlatforms
        o1.@flattenPlatformsSet = o1.flattenPlatformsSet ?: o2.flattenPlatformsSet
        o1.publications = CollectionUtils.merge(o1.publications, o2?.publications, false)
        o1.scopes = CollectionUtils.merge(o1.scopes, o2?.scopes, false)
        DefaultPomOptions.merge(o1.pom, o2.pom)
    }

    @CompileStatic
    private static class DefaultPomOptions implements PomOptions {
        String packaging
        Map<String, String> properties = new LinkedHashMap<>()

        String parent
        boolean overwriteInceptionYear
        boolean overwriteUrl
        boolean overwriteLicenses
        boolean overwriteScm
        boolean overwriteOrganization
        boolean overwriteDevelopers
        boolean overwriteContributors
        boolean overwriteIssueManagement
        boolean overwriteCiManagement
        boolean overwriteMailingLists

        private boolean overwriteInceptionYearSet
        private boolean overwriteUrlSet
        private boolean overwriteLicensesSet
        private boolean overwriteScmSet
        private boolean overwriteOrganizationSet
        private boolean overwriteDevelopersSet
        private boolean overwriteContributorsSet
        private boolean overwriteIssueManagementSet
        private boolean overwriteCiManagementSet
        private boolean overwriteMailingListsSet

        boolean isOverwriteInceptionYearSet() {
            return overwriteInceptionYearSet
        }

        boolean isOverwriteUrlSet() {
            return overwriteUrlSet
        }

        boolean isOverwriteLicensesSet() {
            return overwriteLicensesSet
        }

        boolean isOverwriteScmSet() {
            return overwriteScmSet
        }

        boolean isOverwriteOrganizationSet() {
            return overwriteOrganizationSet
        }

        boolean isOverwriteDevelopersSet() {
            return overwriteDevelopersSet
        }

        boolean isOverwriteContributorsSet() {
            return overwriteContributorsSet
        }

        boolean isOverwriteIssueManagementSet() {
            return overwriteIssueManagementSet
        }

        boolean isOverwriteCiManagementSet() {
            return overwriteCiManagementSet
        }

        boolean isOverwriteMailingListsSet() {
            return overwriteMailingListsSet
        }

        @Override
        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<String, Object>(
                packaging: packaging,
                properties: properties)
            if (isNotBlank(parent)) {
                map.putAll([
                    parent                  : parent,
                    overwriteInceptionYear  : overwriteInceptionYear,
                    overwriteUrl            : overwriteUrl,
                    overwriteLicenses       : overwriteLicenses,
                    overwriteScm            : overwriteScm,
                    overwriteOrganization   : overwriteOrganization,
                    overwriteDevelopers     : overwriteDevelopers,
                    overwriteContributors   : overwriteContributors,
                    overwriteIssueManagement: overwriteIssueManagementSet,
                    overwriteCiManagement   : overwriteCiManagementSet,
                    overwriteMailingLists   : overwriteMailingListsSet
                ])
            }

            map
        }

        static void merge(DefaultPomOptions o1, DefaultPomOptions o2) {
            o1.packaging = (o1.packaging ?: o2.packaging) ?: 'jar'
            o1.properties = CollectionUtils.merge(o1.properties, o2.properties, false)
            o1.parent = o1.parent ?: o2.parent
            o1.setOverwriteInceptionYear((boolean) (o1.overwriteInceptionYearSet ? o1.overwriteInceptionYear : o2.overwriteInceptionYear))
            o1.setOverwriteUrl((boolean) (o1.overwriteUrlSet ? o1.overwriteUrl : o2.overwriteUrl))
            o1.setOverwriteLicenses((boolean) (o1.overwriteLicensesSet ? o1.overwriteLicenses : o2.overwriteLicenses))
            o1.setOverwriteScm((boolean) (o1.overwriteScmSet ? o1.overwriteScm : o2.overwriteScm))
            o1.setOverwriteOrganization((boolean) (o1.overwriteOrganizationSet ? o1.overwriteOrganization : o2.overwriteOrganization))
            o1.setOverwriteDevelopers((boolean) (o1.overwriteDevelopersSet ? o1.overwriteDevelopers : o2.overwriteDevelopers))
            o1.setOverwriteContributors((boolean) (o1.overwriteContributorsSet ? o1.overwriteContributors : o2.overwriteContributors))
            o1.setOverwriteIssueManagement((boolean) (o1.overwriteIssueManagementSet ? o1.overwriteIssueManagement : o2.overwriteIssueManagement))
            o1.setOverwriteCiManagement((boolean) (o1.overwriteCiManagementSet ? o1.overwriteCiManagement : o2.overwriteCiManagement))
            o1.setOverwriteMailingLists((boolean) (o1.overwriteMailingListsSet ? o1.overwriteMailingLists : o2.overwriteMailingLists))
        }
    }

    @CompileStatic
    static class Signing {
        Boolean enabled
        String keyId
        String secretKey
        String password

        private final ProjectConfigurationExtension config
        private final Project project

        Signing(ProjectConfigurationExtension config, Project project) {
            this.config = config
            this.project = project
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<String, Object>()

            map.enabled = getEnabled()
            map.keyId = this.keyId ? '************' : '**unset**'
            map.secretKey = this.secretKey ? '************' : '**unset**'
            map.password = password

            new LinkedHashMap<>('signing': map)
        }

        boolean getEnabled() {
            this.@enabled != null && this.@enabled
        }

        boolean isEnabledSet() {
            this.@enabled != null
        }

        static void merge(Signing o1, Signing o2) {
            o1.enabled = o1.isEnabledSet() ? o1.getEnabled() : o2.getEnabled()
            o1.keyId = o1.@keyId != null ? o1.keyId : o2.keyId
            o1.secretKey = o1.@secretKey != null ? o1.secretKey : o2.secretKey
            o1.password = o1.@password != null ? o1.password : o2.password
        }
    }

    @CompileStatic
    static class MavenCentral {
        Boolean enabled
        String username
        String password
        String publishingType = 'USER_MANAGED'
        int timeoutSeconds = 300

        private final ProjectConfigurationExtension config
        private final Project project

        MavenCentral(ProjectConfigurationExtension config, Project project) {
            this.config = config
            this.project = project
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<String, Object>()

            map.enabled = getEnabled()
            map.username = this.username ? '************' : '**unset**'
            map.password = this.password ? '************' : '**unset**'
            map.publishingType = this.publishingType
            map.timeoutSeconds = this.timeoutSeconds

            new LinkedHashMap<>('mavenCentral': map)
        }

        boolean getEnabled() {
            this.@enabled != null && this.@enabled
        }

        boolean isEnabledSet() {
            this.@enabled != null
        }

        static void merge(MavenCentral o1, MavenCentral o2) {
            o1.enabled = o1.isEnabledSet() ? o1.getEnabled() : o2.getEnabled()
            o1.username = o1.@username != null ? o1.username : o2.username
            o1.password = o1.@password != null ? o1.password : o2.password
            o1.publishingType = o1.@publishingType != null ? o1.publishingType : o2.publishingType
            o1.timeoutSeconds = o1.@timeoutSeconds != null ? o1.timeoutSeconds : o2.timeoutSeconds
        }
    }
}
