/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
import org.kordamp.gradle.plugin.base.model.DefaultPomOptions
import org.kordamp.gradle.plugin.base.model.PomOptions

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
class Publishing extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.publishing'

    String releasesRepository
    String snapshotsRepository
    boolean signing = false
    DefaultPomOptions pom = new DefaultPomOptions()
    List<String> publications = []

    private boolean signingSet

    Publishing(ProjectConfigurationExtension config, Project project) {
        super(config, project)
        doSetEnabled(project.plugins.findPlugin(PLUGIN_ID) != null)
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        if (enabled) {
            map.signing = signing
            map.releasesRepository = releasesRepository
            map.snapshotsRepository = snapshotsRepository
            map.publications = publications
            map.pom = pom.toMap()
        }

        new LinkedHashMap<>('publishing': map)
    }

    void normalize() {
        if (!enabledSet && isRoot()) {
            setEnabled(project.plugins.findPlugin(PLUGIN_ID) != null)
        }
    }

    void pom(Action<? super PomOptions> action) {
        action.execute(pom)
    }

    void pom(@DelegatesTo(PomOptions) Closure action) {
        ConfigureUtil.configure(action, pom)
    }

    void setSigning(boolean signing) {
        this.signing = signing
        this.signingSet = true
    }

    boolean isSigningSet() {
        this.signingSet
    }

    void copyInto(Publishing copy) {
        super.copyInto(copy)

        copy.@releasesRepository = releasesRepository
        copy.@snapshotsRepository = snapshotsRepository
        copy.@signing = this.signing
        copy.@signingSet = this.signingSet
        copy.publications.addAll(publications)
        this.@pom.copyInto(copy.@pom)
    }

    static void merge(Publishing o1, Publishing o2) {
        AbstractFeature.merge(o1, o2)
        o1.releasesRepository = o1.@releasesRepository ?: o2.@releasesRepository
        o1.snapshotsRepository = o1.@snapshotsRepository ?: o2.@snapshotsRepository
        o1.@signing = o1.signingSet ? o1.signing : o2.signing
        o1.@signingSet = o1.signingSet ?: o2.signingSet
        CollectionUtils.merge(o1.publications, o2?.publications)
        DefaultPomOptions.merge(o1.pom, o2.pom)
    }
}
