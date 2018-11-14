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
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
@EqualsAndHashCode(excludes = ['project'])
@ToString(includeNames = true, excludes = ['project'])
class BuildInfo {
    boolean enabled = true
    boolean skipBuildBy
    boolean skipBuildDate
    boolean skipBuildTime
    boolean skipBuildRevision
    boolean skipBuildJdk
    boolean skipBuildCreatedBy

    private boolean enabledSet
    private boolean skipBuildBySet
    private boolean skipBuildDateSet
    private boolean skipBuildTimeSet
    private boolean skipBuildRevisionSet
    private boolean skipBuildJdkSet
    private boolean skipBuildCreatedBySet

    void setEnabled(boolean enabled) {
        this.enabled = enabled
        this.enabledSet = true
    }

    boolean isEnabledSet() {
        this.enabledSet
    }

    void setSkipBuildBy(boolean skipBuildBy) {
        this.skipBuildBy = skipBuildBy
        this.skipBuildBySet = true
    }

    boolean isSkipBuildBySet() {
        this.skipBuildBySet
    }

    void setSkipBuildDate(boolean skipBuildDate) {
        this.skipBuildDate = skipBuildDate
        this.skipBuildDateSet = true
    }

    boolean isSkipBuildDateSet() {
        this.skipBuildDateSet
    }

    void setSkipBuildTime(boolean skipBuildTime) {
        this.skipBuildTime = skipBuildTime
        this.skipBuildTimeSet = true
    }

    boolean isSkipBuildTimeSet() {
        this.skipBuildTimeSet
    }

    void setSkipBuildRevision(boolean skipBuildRevision) {
        this.skipBuildRevision = skipBuildRevision
        this.skipBuildRevisionSet = true
    }

    boolean isSkipBuildRevisionSet() {
        this.skipBuildRevisionSet
    }

    void setSkipBuildJdk(boolean skipBuildJdk) {
        this.skipBuildJdk = skipBuildJdk
        this.skipBuildJdkSet = true
    }

    boolean isSkipBuildJdkSet() {
        this.skipBuildJdkSet
    }

    void setSkipBuildCreatedBy(boolean skipBuildCreatedBy) {
        this.skipBuildCreatedBy = skipBuildCreatedBy
        this.skipBuildCreatedBySet = true
    }

    boolean isSkipBuildCreatedBySet() {
        this.skipBuildCreatedBySet
    }

    void copyInto(BuildInfo copy) {
        copy.@enabled = enabled
        copy.@enabledSet = enabledSet
        copy.@skipBuildBy = skipBuildBy
        copy.@skipBuildBySet = skipBuildBySet
        copy.@skipBuildDate = skipBuildDate
        copy.@skipBuildDateSet = skipBuildDateSet
        copy.@skipBuildTime = skipBuildTime
        copy.@skipBuildTimeSet = skipBuildTimeSet
        copy.@skipBuildRevision = skipBuildRevision
        copy.@skipBuildRevisionSet = skipBuildRevisionSet
        copy.@skipBuildJdk = skipBuildJdk
        copy.@skipBuildJdkSet = skipBuildJdkSet
        copy.@skipBuildCreatedBy = skipBuildCreatedBy
        copy.@skipBuildCreatedBySet = skipBuildCreatedBySet
    }

    static void merge(BuildInfo o1, BuildInfo o2) {
        o1.setEnabled((boolean) (o1.enabledSet ? o1.enabled : o2.enabled))
        o1.setSkipBuildBy((boolean) (o1.skipBuildBySet ? o1.skipBuildBy : o2.skipBuildBy))
        o1.setSkipBuildDate((boolean) (o1.skipBuildDateSet ? o1.skipBuildDate : o2.skipBuildDate))
        o1.setSkipBuildTime((boolean) (o1.skipBuildTimeSet ? o1.skipBuildTime : o2.skipBuildTime))
        o1.setSkipBuildRevision((boolean) (o1.skipBuildRevisionSet ? o1.skipBuildRevision : o2.skipBuildRevision))
        o1.setSkipBuildJdk((boolean) (o1.skipBuildJdkSet ? o1.skipBuildJdk : o2.skipBuildJdk))
        o1.setSkipBuildCreatedBy((boolean) (o1.skipBuildCreatedBySet ? o1.skipBuildCreatedBy : o2.skipBuildCreatedBy))
    }
}
