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
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Project

/**
 * @author Andres Almiray
 * @since 0.16.0
 */
@CompileStatic
@Canonical
class BuildScan extends AbstractFeature {
    BuildScan(Project project) {
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

        ['buildScan': map]
    }

    static void merge(BuildScan o1, BuildScan o2) {
        AbstractFeature.merge(o1, o2)
    }
}
