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
import groovy.transform.PackageScope
import org.gradle.api.Action
import org.gradle.api.Project
import org.kordamp.gradle.ConfigureUtil
import org.kordamp.gradle.plugin.settings.PluginsSpec

/**
 * @author Andres Almiray
 * @since 0.35.0
 */
@PackageScope
@CompileStatic
class PluginsSpecImpl implements PluginsSpec {
    private final List<DirMatchingPluginsSpecImpl> pluginsByDir = []
    private final List<PathMatchingPluginsSpecImpl> pluginsByPath = []

    @Override
    void all(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PathMatchingPluginsSpec) Closure<Void> action) {
        PathMatchingPluginsSpecImpl spec = new PathMatchingPluginsSpecImpl('*')
        ConfigureUtil.configure(action, spec)
        pluginsByPath << spec
    }

    @Override
    void dir(String dir, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DirMatchingPluginsSpec) Closure<Void> action) {
        DirMatchingPluginsSpecImpl spec = new DirMatchingPluginsSpecImpl(dir)
        ConfigureUtil.configure(action, spec)
        pluginsByDir << spec
    }

    @Override
    void dirs(List<String> dirs, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DirMatchingPluginsSpec) Closure<Void> action) {
        DirMatchingPluginsSpecImpl spec = new DirMatchingPluginsSpecImpl(dirs)
        ConfigureUtil.configure(action, spec)
        pluginsByDir << spec
    }

    @Override
    void path(String path, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PathMatchingPluginsSpec) Closure<Void> action) {
        PathMatchingPluginsSpecImpl spec = new PathMatchingPluginsSpecImpl(path)
        ConfigureUtil.configure(action, spec)
        pluginsByPath << spec
    }

    @Override
    void paths(List<String> paths, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PathMatchingPluginsSpec) Closure<Void> action) {
        PathMatchingPluginsSpecImpl spec = new PathMatchingPluginsSpecImpl(paths)
        ConfigureUtil.configure(action, spec)
        pluginsByPath << spec
    }

    @Override
    void all(Action<? super PathMatchingPluginsSpec> action) {
        PathMatchingPluginsSpecImpl spec = new PathMatchingPluginsSpecImpl('*')
        action.execute(spec)
        pluginsByPath << spec
    }

    @Override
    void dir(String dir, Action<? super DirMatchingPluginsSpec> action) {
        DirMatchingPluginsSpecImpl spec = new DirMatchingPluginsSpecImpl(dir)
        action.execute(spec)
        pluginsByDir << spec
    }

    @Override
    void dirs(List<String> dirs, Action<? super DirMatchingPluginsSpec> action) {
        DirMatchingPluginsSpecImpl spec = new DirMatchingPluginsSpecImpl(dirs)
        action.execute(spec)
        pluginsByDir << spec
    }

    @Override
    void path(String path, Action<? super PathMatchingPluginsSpec> action) {
        PathMatchingPluginsSpecImpl spec = new PathMatchingPluginsSpecImpl(path)
        action.execute(spec)
        pluginsByPath << spec
    }

    @Override
    void paths(List<String> paths, Action<? super PathMatchingPluginsSpec> action) {
        PathMatchingPluginsSpecImpl spec = new PathMatchingPluginsSpecImpl(paths)
        action.execute(spec)
        pluginsByPath << spec
    }

    void apply(Project rootProject) {
        applyPluginsByDir(rootProject)
        applyPluginsByPath(rootProject)
        for (Project project : rootProject.childProjects.values()) {
            applyPluginsByDir(project)
            applyPluginsByPath(project)
        }
    }

    private void applyPluginsByDir(Project project) {
        for (DirMatchingPluginsSpecImpl spec : pluginsByDir) {
            spec.apply(project)
        }
    }

    private void applyPluginsByPath(Project project) {
        for (PathMatchingPluginsSpecImpl spec : pluginsByPath) {
            spec.apply(project)
        }
    }
}
