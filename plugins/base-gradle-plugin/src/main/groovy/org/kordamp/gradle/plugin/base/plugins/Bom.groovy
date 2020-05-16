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

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.CollectionUtils
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.PomOptions
import org.kordamp.gradle.plugin.base.model.artifact.Dependency
import org.kordamp.gradle.plugin.base.model.artifact.DependencySpec
import org.kordamp.gradle.plugin.base.model.artifact.internal.DependencySpecImpl

import static org.kordamp.gradle.StringUtils.isBlank
import static org.kordamp.gradle.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.9.0
 */
@CompileStatic
class Bom extends AbstractFeature implements PomOptions {
    static final String PLUGIN_ID = 'org.kordamp.gradle.bom'

    final String packaging = 'pom'

    private final Map<String, Dependency> dependencies = [:]
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

    Bom(ProjectConfigurationExtension config, Project project) {
        super(config, project)
        doSetEnabled(project.plugins.findPlugin(PLUGIN_ID) != null)
    }

    void normalize() {
        if (!enabledSet) {
            setEnabled(project.plugins.findPlugin(PLUGIN_ID) != null)
        }
    }

    Map<String, Dependency> getDependencies() {
        Collections.unmodifiableMap(dependencies)
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        if (enabled) {
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
                map.overwriteIssueManagement = overwriteIssueManagementSet
                map.overwriteCiManagement = overwriteCiManagementSet
                map.overwriteMailingLists = overwriteMailingListsSet
            }
            map.properties = properties
        }

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

    void copyInto(Bom copy) {
        super.copyInto(copy)
        copy.@dependencies.putAll(dependencies)
        copy.excludes.addAll(excludes)
        copy.includes.addAll(includes)
        copy.properties.putAll(properties)
        copy.@autoIncludes = this.autoIncludes
        copy.@autoIncludesSet = this.autoIncludesSet

        copy.parent = this.parent
        copy.@overwriteInceptionYear = this.overwriteInceptionYear
        copy.@overwriteInceptionYearSet = this.overwriteInceptionYearSet
        copy.@overwriteUrl = this.overwriteUrl
        copy.@overwriteUrlSet = this.overwriteUrlSet
        copy.@overwriteLicenses = this.overwriteLicenses
        copy.@overwriteLicensesSet = this.overwriteLicensesSet
        copy.@overwriteScm = this.overwriteScm
        copy.@overwriteScmSet = this.overwriteScmSet
        copy.@overwriteOrganization = this.overwriteOrganization
        copy.@overwriteOrganizationSet = this.overwriteOrganizationSet
        copy.@overwriteDevelopers = this.overwriteDevelopers
        copy.@overwriteDevelopersSet = this.overwriteDevelopersSet
        copy.@overwriteContributors = this.overwriteContributors
        copy.@overwriteContributorsSet = this.overwriteContributorsSet
        copy.@overwriteIssueManagement = this.overwriteIssueManagement
        copy.@overwriteIssueManagementSet = this.overwriteIssueManagementSet
        copy.@overwriteCiManagement = this.overwriteCiManagement
        copy.@overwriteCiManagementSet = this.overwriteCiManagementSet
        copy.@overwriteMailingLists = this.overwriteMailingLists
        copy.@overwriteMailingListsSet = this.overwriteMailingListsSet
    }

    static void merge(Bom o1, Bom o2) {
        AbstractFeature.merge(o1, o2)
        CollectionUtils.merge(o1.@dependencies, o2.@dependencies)
        CollectionUtils.merge(o1.excludes, o2.excludes)
        CollectionUtils.merge(o1.includes, o2.includes)
        CollectionUtils.merge(o1.properties, o2.properties)
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

    void dependency(Dependency dependency) {
        if(dependency) {
            dependencies[dependency.name] = dependency
        }
    }

    Dependency dependency(String name, String notation) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Dependency name cannot be blank.')
        }
        DependencySpecImpl d = new DependencySpecImpl(name.trim())
        d.parse(project.rootProject, notation)
        d.validate(project)
        dependencies[d.name] = d.asDependency()
    }

    Dependency dependency(String name, Action<? super DependencySpec> action) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Dependency name cannot be blank.')
        }
        DependencySpecImpl d = new DependencySpecImpl(name.trim())
        action.execute(d)
        d.validate(project)
        dependencies[d.name] = d.asDependency()
    }

    Dependency dependency(String name, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DependencySpec) Closure<Void> action) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Dependency name cannot be blank.')
        }
        DependencySpecImpl d = new DependencySpecImpl(name.trim())
        ConfigureUtil.configure(action, d)
        d.validate(project)
        dependencies[d.name] = d.asDependency()
    }

    Dependency dependency(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Dependency name cannot be blank.')
        }

        if (dependencies.containsKey(name)) {
            return dependencies.get(name)
        }
        throw new IllegalArgumentException("Undeclared dependency ${name}.")
    }
}
