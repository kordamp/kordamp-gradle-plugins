/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2023 Andres Almiray.
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
        super(config, project, PLUGIN_ID)
        manifest = new Manifest(config, project)
    }

    @Override
    protected AbstractFeature getParentFeature() {
        return project.rootProject.extensions.getByType(ProjectConfigurationExtension).artifacts.jar
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(
            enabled: enabled
        )
        map.putAll(manifest.toMap())

        new LinkedHashMap<>(jar: map)
    }

    @Override
    void normalize() {
        super.normalize()

        if (!(manifest.classpathLayoutType in ['simple', 'repository'])) {
            project.logger.warn("The value 'custom' for jar.manifest.classpathLayoutType is not supported. Using 'simple' instead.")
            manifest.classpathLayoutType = 'simple'
        }
    }

    static void merge(Jar o1, Jar o2) {
        AbstractQualityFeature.merge(o1, o2)
        Manifest.merge(o1.manifest, o2.manifest)
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
            this.@enabled != null && this.@enabled
        }

        boolean getAddClasspath() {
            this.@addClasspath != null && this.@addClasspath
        }

        static Manifest merge(Manifest o1, Manifest o2) {
            o1.setEnabled(o1.@enabled != null ? o1.getEnabled() : o2.getEnabled())
            o1.setAddClasspath(o1.@addClasspath != null ? o1.getAddClasspath() : o2.getAddClasspath())
            o1.classpathPrefix = o1.classpathPrefix ?: o2.classpathPrefix
            o1.classpathLayoutType = o1.classpathLayoutType ?: o2.classpathLayoutType
            o1
        }
    }
}
