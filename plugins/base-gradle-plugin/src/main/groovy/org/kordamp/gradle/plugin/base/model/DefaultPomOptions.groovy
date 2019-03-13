/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
package org.kordamp.gradle.plugin.base.model

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import static org.kordamp.gradle.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.13.0
 */
@CompileStatic
class DefaultPomOptions implements PomOptions {
    String parent
    boolean overwriteInceptionYear
    boolean overwriteUrl
    boolean overwriteLicenses
    boolean overwriteScm
    boolean overwriteOrganization
    boolean overwriteDevelopers
    boolean overwriteContributors

    private boolean overwriteInceptionYearSet
    private boolean overwriteUrlSet
    private boolean overwriteLicensesSet
    private boolean overwriteScmSet
    private boolean overwriteOrganizationSet
    private boolean overwriteDevelopersSet
    private boolean overwriteContributorsSet

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

    @Override
    @CompileDynamic
    Map<String, Object> toMap() {
        if (isNotBlank(parent)) {
            [
                parent                : parent,
                overwriteInceptionYear: overwriteInceptionYear,
                overwriteUrl          : overwriteUrl,
                overwriteLicenses     : overwriteLicenses,
                overwriteScm          : overwriteScm,
                overwriteOrganization : overwriteOrganization,
                overwriteDevelopers   : overwriteDevelopers,
                overwriteContributors : overwriteContributors
            ]
        } else {
            [:]
        }
    }

    void copyInto(DefaultPomOptions copy) {
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
    }

    static void merge(DefaultPomOptions o1, DefaultPomOptions o2) {
        o1.parent = o1.parent ?: o2?.parent
        o1.setOverwriteInceptionYear((boolean) (o1.overwriteInceptionYearSet ? o1.overwriteInceptionYear : o2.overwriteInceptionYear))
        o1.setOverwriteUrl((boolean) (o1.overwriteUrlSet ? o1.overwriteUrl : o2.overwriteUrl))
        o1.setOverwriteLicenses((boolean) (o1.overwriteLicensesSet ? o1.overwriteLicenses : o2.overwriteLicenses))
        o1.setOverwriteScm((boolean) (o1.overwriteScmSet ? o1.overwriteScm : o2.overwriteScm))
        o1.setOverwriteOrganization((boolean) (o1.overwriteOrganizationSet ? o1.overwriteOrganization : o2.overwriteOrganization))
        o1.setOverwriteDevelopers((boolean) (o1.overwriteDevelopersSet ? o1.overwriteDevelopers : o2.overwriteDevelopers))
        o1.setOverwriteContributors((boolean) (o1.overwriteContributorsSet ? o1.overwriteContributors : o2.overwriteContributors))
    }
}