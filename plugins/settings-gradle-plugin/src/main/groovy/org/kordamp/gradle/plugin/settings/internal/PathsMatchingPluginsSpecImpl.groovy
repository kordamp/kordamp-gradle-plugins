/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2024 Andres Almiray.
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
package org.kordamp.gradle.plugin.settings.internal

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.kordamp.gradle.plugin.settings.PluginsSpec

/**
 * @author Andres Almiray
 * @since 0.41.0
 */
@PackageScope
@CompileStatic
class PathsMatchingPluginsSpecImpl extends PathMatchingPluginsSpecImpl implements PluginsSpec.PathsMatchingPluginsSpec {
    final Set<String> excludes = new LinkedHashSet<>()

    PathsMatchingPluginsSpecImpl(String path) {
        this([path])
    }

    PathsMatchingPluginsSpecImpl(List<String> paths) {
        super(paths)
    }

    @Override
    void excludePath(String path) {
        String s = path?.trim()
        if (isNotBlank(s)) {
            excludes << s
        }
    }

    void apply(Project project) {
        for (String path : paths) {
            if (path == project.path || pattern(path).matcher(project.path).matches()) {
                boolean excluded = excludes.contains(project.path)
                if (!excluded) {
                    for (String exclude : excludes) {
                        if (pattern(exclude).matcher(project.path).matches()) {
                            excluded = true
                            break
                        }
                    }
                }
                if (!excluded) applyPluginsTo(project)
            }
        }
    }
}
