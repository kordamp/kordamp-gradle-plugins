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
package org.kordamp.gradle.plugin.settings.internal

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.kordamp.gradle.plugin.settings.PluginsSpec

import java.util.regex.Pattern

/**
 * @author Andres Almiray
 * @since 0.35.0
 */
@PackageScope
@CompileStatic
class PathMatchingPluginsSpecImpl extends AbstractPluginsSpec implements PluginsSpec.PathMatchingPluginsSpec {
    final Set<String> paths = new LinkedHashSet<>()

    PathMatchingPluginsSpecImpl(String path) {
        this([path])
    }

    PathMatchingPluginsSpecImpl(List<String> paths) {
        if (!paths) {
            throw new IllegalArgumentException('Empty argument list')
        }

        for (String path : paths) {
            if (isNotBlank(path?.trim())) {
                this.paths << path.trim()
            }
        }

        if (!this.paths) {
            throw new IllegalArgumentException('Empty argument list')
        }
    }

    void apply(Project project) {
        for (String path : paths) {
            if (path == project.path || pattern(path).matcher(project.path).matches()) {
                applyPluginsTo(project)
            }
        }
    }

    @Memoized
    protected Pattern pattern(String regex) {
        Pattern.compile(asRegex(regex))
    }
}
