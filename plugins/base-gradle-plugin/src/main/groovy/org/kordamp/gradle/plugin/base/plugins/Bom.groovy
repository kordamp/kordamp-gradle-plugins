/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2021 Andres Almiray.
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
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.PomOptions
import org.kordamp.gradle.plugin.base.model.artifact.Dependency
import org.kordamp.gradle.plugin.base.model.artifact.DependencyManagement
import org.kordamp.gradle.plugin.base.model.artifact.internal.DependencyManagementImpl
import org.kordamp.gradle.util.CollectionUtils

import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.9.0
 */
@CompileStatic
class Bom extends AbstractFeature implements PomOptions, DependencyManagement {
    static final String PLUGIN_ID = 'org.kordamp.gradle.bom'

    final String packaging = 'pom'

    Set<String> excludes = new LinkedHashSet<>()
    Set<String> includes = new LinkedHashSet<>()

    Map<String, String> properties = new LinkedHashMap<>()

    boolean autoIncludes = true
    private boolean autoIncludesSet

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

    @Delegate
    private final DependencyManagement dependencyManagement

    Bom(ProjectConfigurationExtension config, Project project) {
        super(config, project, PLUGIN_ID)
        this.dependencyManagement = new DependencyManagementImpl(config, project)
    }

    @Override
    protected AbstractFeature getParentFeature() {
        return project.rootProject.extensions.getByType(ProjectConfigurationExtension).bom
    }

    @Override
    protected void normalizeEnabled() {
        if (!enabledSet) {
            setEnabled(isApplied())
            if (isApplied()) {
                if (isRoot() && project.childProjects.size() > 0) return
                ProjectConfigurationExtension config = project.extensions.getByType(ProjectConfigurationExtension)
                config.docs.javadoc.enabled = false
                config.docs.groovydoc.enabled = false
                config.docs.kotlindoc.enabled = false
                config.docs.scaladoc.enabled = false
                config.docs.sourceHtml.enabled = false
                config.docs.sourceXref.enabled = false
                config.artifacts.source.enabled = false
            }
        }
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        map.autoIncludes = autoIncludes
        map.dependencies = dependencies
        map.excludes = excludes
        map.includes = includes
        if (isNotBlank(parent)) {
            map.parent = parent
            map.overwriteInceptionYear = overwriteInceptionYear
            map.overwriteUrl = overwriteUrl
            map.overwriteLicenses = overwriteLicenses
            map.overwriteScm = overwriteScm
            map.overwriteOrganization = overwriteOrganization
            map.overwriteDevelopers = overwriteDevelopers
            map.overwriteContributors = overwriteContributors
            map.overwriteIssueManagement = overwriteIssueManagement
            map.overwriteCiManagement = overwriteCiManagement
            map.overwriteMailingLists = overwriteMailingLists
        }
        map.properties = properties

        new LinkedHashMap<>('bom': map)
    }

    void exclude(String str) {
        if (isNotBlank(str)) excludes << str.toString()
    }

    void include(String str) {
        if (isNotBlank(str)) includes << str.toString()
    }

    void setAutoIncludes(boolean autoIncludes) {
        this.autoIncludes = autoIncludes
        this.autoIncludesSet = true
    }

    boolean isAutoIncludesSet() {
        this.autoIncludesSet
    }

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

    static void merge(Bom o1, Bom o2) {
        AbstractFeature.merge(o1, o2)
        o1.excludes = CollectionUtils.merge(o1.excludes, o2.excludes, false)
        o1.includes = CollectionUtils.merge(o1.includes, o2.includes, false)
        o1.properties = CollectionUtils.merge(o1.properties, o2.properties, false)
        o1.setAutoIncludes((boolean) (o1.autoIncludesSet ? o1.autoIncludes : o2.autoIncludes))

        o1.parent = o1.parent ?: o2?.parent
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

    List<String> validate(ProjectConfigurationExtension extension) {
        List<String> errors = []

        if (!enabled) return errors

        errors
    }
}
