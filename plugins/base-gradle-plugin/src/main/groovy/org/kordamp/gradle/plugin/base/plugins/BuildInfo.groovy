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
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
class BuildInfo extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.build-info'

    boolean clearTime
    boolean skipBuildBy
    boolean skipBuildDate
    boolean skipBuildTime
    boolean skipBuildRevision
    boolean skipBuildJdk
    boolean skipBuildOs
    boolean skipBuildCreatedBy

    String buildBy
    String buildDate
    String buildTime
    String buildRevision
    String buildJdk
    String buildOs
    String buildCreatedBy

    private boolean clearTimeSet
    private boolean skipBuildBySet
    private boolean skipBuildDateSet
    private boolean skipBuildTimeSet
    private boolean skipBuildRevisionSet
    private boolean skipBuildJdkSet
    private boolean skipBuildOsSet
    private boolean skipBuildCreatedBySet

    BuildInfo(ProjectConfigurationExtension config, Project project) {
        super(config, project, PLUGIN_ID)
    }

    @Override
    protected AbstractFeature getParentFeature() {
        return project.rootProject.extensions.getByType(ProjectConfigurationExtension).buildInfo
    }

    @Override
    protected void normalizeEnabled() {
        if (!enabledSet) {
            setEnabled(isApplied())
        }
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        if (!isRoot()) return [:]

        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        if (isVisible()) {
            map.clearTime = clearTime
            if (!skipBuildBy) map.buildBy = buildBy
            if (!skipBuildDate) map.buildDate = buildDate
            if (!skipBuildTime) map.buildTime = buildTime
            if (!skipBuildRevision) map.buildRevision = buildRevision
            if (!skipBuildJdk) map.buildJdk = buildJdk
            if (!skipBuildOs) map.buildOs = buildOs
            if (!skipBuildCreatedBy) map.buildCreatedBy = buildCreatedBy
        }

        new LinkedHashMap<>('buildInfo': map)
    }

    void setClearTime(boolean clearTime) {
        this.clearTime = clearTime
        this.clearTimeSet = true
    }

    boolean isClearTimeSet() {
        this.clearTimeSet
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

    void setSkipBuildOs(boolean skipBuildOs) {
        this.skipBuildOs = skipBuildOs
        this.skipBuildOsSet = true
    }

    boolean isSkipBuildOsSet() {
        this.skipBuildOsSet
    }

    void setSkipBuildCreatedBy(boolean skipBuildCreatedBy) {
        this.skipBuildCreatedBy = skipBuildCreatedBy
        this.skipBuildCreatedBySet = true
    }

    boolean isSkipBuildCreatedBySet() {
        this.skipBuildCreatedBySet
    }

    static void merge(BuildInfo o1, BuildInfo o2) {
        AbstractFeature.merge(o1, o2)
        o1.setClearTime((boolean) (o1.clearTimeSet ? o1.clearTime : o2.clearTime))
        o1.setSkipBuildBy((boolean) (o1.skipBuildBySet ? o1.skipBuildBy : o2.skipBuildBy))
        o1.setSkipBuildDate((boolean) (o1.skipBuildDateSet ? o1.skipBuildDate : o2.skipBuildDate))
        o1.setSkipBuildTime((boolean) (o1.skipBuildTimeSet ? o1.skipBuildTime : o2.skipBuildTime))
        o1.setSkipBuildRevision((boolean) (o1.skipBuildRevisionSet ? o1.skipBuildRevision : o2.skipBuildRevision))
        o1.setSkipBuildJdk((boolean) (o1.skipBuildJdkSet ? o1.skipBuildJdk : o2.skipBuildJdk))
        o1.setSkipBuildOs((boolean) (o1.skipBuildOsSet ? o1.skipBuildOs : o2.skipBuildOs))
        o1.setSkipBuildCreatedBy((boolean) (o1.skipBuildCreatedBySet ? o1.skipBuildCreatedBy : o2.skipBuildCreatedBy))
    }
}
