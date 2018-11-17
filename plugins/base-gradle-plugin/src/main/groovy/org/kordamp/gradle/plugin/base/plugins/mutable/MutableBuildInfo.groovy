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
package org.kordamp.gradle.plugin.base.plugins.mutable

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.plugins.BuildInfo

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
@EqualsAndHashCode(excludes = ['project'])
class MutableBuildInfo extends AbstractFeature implements BuildInfo {
    boolean skipBuildBy
    boolean skipBuildDate
    boolean skipBuildTime
    boolean skipBuildRevision
    boolean skipBuildJdk
    boolean skipBuildCreatedBy

    String buildBy
    String buildDate
    String buildTime
    String buildRevision
    String buildJdk
    String buildCreatedBy

    private boolean skipBuildBySet
    private boolean skipBuildDateSet
    private boolean skipBuildTimeSet
    private boolean skipBuildRevisionSet
    private boolean skipBuildJdkSet
    private boolean skipBuildCreatedBySet

    MutableBuildInfo(Project project) {
        super(project)
    }

    @Override
    String toString() {
        isRoot() ? toMap().toString() : ''
    }

    @Override
    @CompileDynamic
    Map<String, Map<String, Object>> toMap() {
        if (!isRoot()) return [:]

        Map map = [enabled: enabled]

        if (enabled) {
            if (!skipBuildBy) map.buildBy = buildBy
            if (!skipBuildDate) map.buildDate = buildDate
            if (!skipBuildTime) map.buildTime = buildTime
            if (!skipBuildRevision) map.buildRevision = buildRevision
            if (!skipBuildJdk) map.buildJdk = buildJdk
            if (!skipBuildCreatedBy) map.buildCreatedBy = buildCreatedBy
        }

        ['buildInfo': map]
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

    void copyInto(MutableBuildInfo copy) {
        super.copyInto(copy)
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

    static void merge(MutableBuildInfo o1, MutableBuildInfo o2) {
        AbstractFeature.merge(o1, o2)
        o1.setSkipBuildBy((boolean) (o1.skipBuildBySet ? o1.skipBuildBy : o2.skipBuildBy))
        o1.setSkipBuildDate((boolean) (o1.skipBuildDateSet ? o1.skipBuildDate : o2.skipBuildDate))
        o1.setSkipBuildTime((boolean) (o1.skipBuildTimeSet ? o1.skipBuildTime : o2.skipBuildTime))
        o1.setSkipBuildRevision((boolean) (o1.skipBuildRevisionSet ? o1.skipBuildRevision : o2.skipBuildRevision))
        o1.setSkipBuildJdk((boolean) (o1.skipBuildJdkSet ? o1.skipBuildJdk : o2.skipBuildJdk))
        o1.setSkipBuildCreatedBy((boolean) (o1.skipBuildCreatedBySet ? o1.skipBuildCreatedBy : o2.skipBuildCreatedBy))
    }
}
