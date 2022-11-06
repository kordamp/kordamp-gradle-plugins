/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2022 Andres Almiray.
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
class DirMatchingPluginsSpecImpl extends AbstractPluginsSpec implements PluginsSpec.DirMatchingPluginsSpec {
    final Set<String> dirs = new LinkedHashSet<>()
    final Set<String> excludedDirs = new LinkedHashSet<>()
    final Set<String> excludedPaths = new LinkedHashSet<>()

    DirMatchingPluginsSpecImpl(String dir) {
        this([dir])
    }

    DirMatchingPluginsSpecImpl(List<String> dirs) {
        if (!dirs) {
            throw new IllegalArgumentException('Empty argument list')
        }

        for (String dir : dirs) {
            if (isNotBlank(dir?.trim())) {
                this.dirs << dir.trim()
            }
        }

        if (!this.dirs) {
            throw new IllegalArgumentException('Empty argument list')
        }
    }

    @Override
    void excludeDir(String dir) {
        String s = dir?.trim()
        if (isNotBlank(s)) {
            excludedDirs << s
        }
    }

    @Override
    void excludePath(String path) {
        String s = path?.trim()
        if (isNotBlank(s)) {
            excludedPaths << s
        }
    }

    void apply(Project project) {
        String parentDir = project.projectDir.parentFile.name
        for (String dir : dirs) {
            if (parentDir == dir && !(excludedDirs.contains(project.projectDir.name))) {
                boolean excluded = excludedPaths.contains(project.path)
                if (!excluded) {
                    for (String exclude : excludedPaths) {
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

    @Memoized
    protected Pattern pattern(String regex) {
        Pattern.compile(asGlobRegex(regex))
    }
}