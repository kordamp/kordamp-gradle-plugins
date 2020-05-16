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
import org.kordamp.gradle.CollectionUtils
import org.kordamp.gradle.ConfigureUtil
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.DefaultPomOptions
import org.kordamp.gradle.plugin.base.model.PomOptions

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
class Publishing extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.publishing'

    String releasesRepository
    String snapshotsRepository
    boolean signing = false
    DefaultPomOptions pom = new DefaultPomOptions()
    List<String> publications = []
    List<String> scopes = []
    boolean useVersionExpressions = true
    boolean flattenPlatforms = false

    private boolean signingSet
    private boolean useVersionExpressionsSet
    private boolean flattenPlatformsSet

    Publishing(ProjectConfigurationExtension config, Project project) {
        super(config, project)
        doSetEnabled(project.plugins.findPlugin(PLUGIN_ID) != null)
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        if (enabled) {
            map.signing = signing
            map.releasesRepository = releasesRepository
            map.snapshotsRepository = snapshotsRepository
            map.publications = publications
            map.scopes = scopes
            map.useVersionExpressions = useVersionExpressions
            map.flattenPlatforms = flattenPlatforms
            map.pom = pom.toMap()
        }

        new LinkedHashMap<>('publishing': map)
    }

    void normalize() {
        if (!enabledSet && isRoot()) {
            setEnabled(project.plugins.findPlugin(PLUGIN_ID) != null)
        }
    }

    void postMerge() {
        if (!scopes) {
            scopes << 'compile'
            scopes << 'runtime'
        }
        if (isBlank(pom.packaging)) {
            pom.packaging = 'jar'
        }
    }

    void pom(Action<? super PomOptions> action) {
        action.execute(pom)
    }

    void pom(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PomOptions) Closure<Void> action) {
        ConfigureUtil.configure(action, pom)
    }

    void setSigning(boolean value) {
        this.signing = value
        this.signingSet = true
    }

    boolean isSigningSet() {
        this.signingSet
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

    void copyInto(Publishing copy) {
        super.copyInto(copy)

        copy.@releasesRepository = releasesRepository
        copy.@snapshotsRepository = snapshotsRepository
        copy.@signing = this.signing
        copy.@signingSet = this.signingSet
        copy.publications.addAll(publications)
        copy.scopes.addAll(scopes)
        copy.@useVersionExpressions = this.useVersionExpressions
        copy.@useVersionExpressionsSet = this.useVersionExpressionsSet
        copy.@flattenPlatforms = this.flattenPlatforms
        copy.@flattenPlatformsSet = this.flattenPlatformsSet
        this.@pom.copyInto(copy.@pom)
    }

    static void merge(Publishing o1, Publishing o2) {
        AbstractFeature.merge(o1, o2)
        o1.releasesRepository = o1.@releasesRepository ?: o2.@releasesRepository
        o1.snapshotsRepository = o1.@snapshotsRepository ?: o2.@snapshotsRepository
        o1.@signing = o1.signingSet ? o1.signing : o2.signing
        o1.@signingSet = o1.signingSet ?: o2.signingSet
        o1.@useVersionExpressions = o1.useVersionExpressionsSet ? o1.useVersionExpressions : o2.useVersionExpressions
        o1.@useVersionExpressionsSet = o1.useVersionExpressionsSet ?: o2.useVersionExpressionsSet
        o1.@flattenPlatforms = o1.flattenPlatformsSet ? o1.flattenPlatforms : o2.flattenPlatforms
        o1.@flattenPlatformsSet = o1.flattenPlatformsSet ?: o2.flattenPlatformsSet
        CollectionUtils.merge(o1.publications, o2?.publications)
        CollectionUtils.merge(o1.scopes, o2?.scopes)
        DefaultPomOptions.merge(o1.pom, o2.pom)
    }
}
