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
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.util.ConfigureUtil

/**
 * @author Andres Almiray
 * @since 0.34.0
 */
@CompileStatic
class Jar extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.jar'

    final Manifest manifest

    Jar(ProjectConfigurationExtension config, Project project) {
        super(config, project)
        doSetEnabled(project.plugins.findPlugin(PLUGIN_ID) != null)
        manifest = new Manifest(config, project)
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(
            enabled: enabled
        )
        map.putAll(manifest.toMap())

        new LinkedHashMap<>(jar: map)
    }

    void normalize() {
        if (!enabledSet) {
            if (isRoot()) {
                if (project.childProjects.isEmpty()) {
                    setEnabled(isApplied())
                } else {
                    setEnabled(project.childProjects.values().any { p -> isApplied(p) })
                }
            } else {
                setEnabled(isApplied())
            }
        }

        if (!(manifest.classpathLayoutType in ['simple', 'repository'])) {
            project.logger.warn("The value 'custom' for jar.manifest.classpathLayoutType is not supported. Using 'simple' instead.")
            manifest.classpathLayoutType = 'simple'
        }
    }

    void copyInto(Jar copy) {
        super.copyInto(copy)
        manifest.copyInto(copy.manifest)
    }

    static void merge(Jar o1, Jar o2) {
        AbstractQualityFeature.merge(o1, o2)
        o1.manifest.merge(o2.manifest)
    }

    void manifest(Action<? super Manifest> action) {
        action.execute(manifest)
    }

    void manifest(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Manifest) Closure<Void> action) {
        ConfigureUtil.configure(action, manifest)
    }

    @CompileStatic
    static class Manifest {
        Boolean enabled
        Boolean addClasspath
        String classpathPrefix = ''
        String classpathLayoutType = 'simple'

        private final ProjectConfigurationExtension config
        private final Project project

        Manifest(ProjectConfigurationExtension config, Project project) {
            this.config = config
            this.project = project
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<String, Object>()

            map.enabled = getEnabled()
            map.addClasspath = getAddClasspath()
            map.classpathPrefix = getClasspathPrefix()
            map.classpathLayoutType = getClasspathLayoutType()

            new LinkedHashMap<>('manifest': map)
        }

        boolean getEnabled() {
            this.@enabled == null || this.@enabled
        }

        boolean getAddClasspath() {
            this.@addClasspath == null || this.@addClasspath
        }

        void copyInto(Manifest copy) {
            copy.enabled = this.@enabled
            copy.addClasspath = this.@addClasspath
            copy.classpathPrefix = this.classpathPrefix
            copy.classpathLayoutType = this.classpathLayoutType
        }

        Manifest copyOf() {
            Manifest copy = new Manifest(config, project)
            copyInto(copy)
            copy
        }

        Manifest merge(Manifest other) {
            Manifest copy = copyOf()
            copy.setEnabled(copy.@enabled != null ? copy.getEnabled() : other.getEnabled())
            copy.setAddClasspath(copy.@addClasspath != null ? copy.getAddClasspath() : other.getAddClasspath())
            copy.classpathPrefix = copy.classpathPrefix ?: other.classpathPrefix
            copy.classpathLayoutType = copy.classpathLayoutType ?: other.classpathLayoutType
            copy
        }
    }
}
