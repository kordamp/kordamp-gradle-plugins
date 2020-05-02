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
package org.kordamp.gradle.plugin.base.internal

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.plugin.base.plugins.Jar
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedJar
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedJar.ResolvedManifest

import static org.kordamp.gradle.PropertyUtils.booleanProvider
import static org.kordamp.gradle.PropertyUtils.stringProvider

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@PackageScope
@CompileStatic
class JarImpl extends AbstractFeature implements Jar {
    final ManifestImpl manifest

    private ResolvedJar resolved

    JarImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
        super(project, ownerConfig, parentConfig)

        manifest = new ManifestImpl(project, ownerConfig, parentConfig)
    }

    @Override
    void normalize() {
        if (!enabled.present) {
            if (isRoot()) {
                if (project.childProjects.isEmpty()) {
                    enabled.set(project.pluginManager.hasPlugin('java') && isApplied())
                } else {
                    enabled.set(project.childProjects.values().any { p -> p.pluginManager.hasPlugin('java') && isApplied(p) })
                }
            } else {
                enabled.set(project.pluginManager.hasPlugin('java') && isApplied())
            }
        }
        manifest.normalize()
    }

    @Override
    void validate(List<String> errors) {
        manifest.validate(errors)
    }

    @Override
    void manifest(Action<? super Manifest> action) {
        action.execute(manifest)
    }

    @Override
    void manifest(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Manifest) Closure<Void> action) {
        ConfigureUtil.configure(action, manifest)
    }

    ResolvedJar asResolved() {
        if (!resolved) {
            resolved = new ResolvedJarImpl(project.providers,
                parentConfig?.asResolved()?.artifacts?.jar,
                this)
        }
        resolved
    }

    @PackageScope
    @CompileStatic
    static class ResolvedJarImpl extends AbstractResolvedFeature implements ResolvedJar {
        final Provider<Boolean> enabled

        private final JarImpl self

        ResolvedJarImpl(ProviderFactory providers, ResolvedJar parent, JarImpl self) {
            super(self.project)
            this.self = self

            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                true)
        }

        @Override
        ResolvedManifest getManifest() {
            self.manifest.asResolved()
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<>([
                enabled: enabled.get(),
            ])
            map.putAll(getManifest().toMap())

            new LinkedHashMap<>('jar': map)
        }
    }

    @PackageScope
    @CompileStatic
    static class ManifestImpl extends AbstractFeature implements Manifest {
        final Property<Boolean> addClasspath
        final Property<String> classpathPrefix
        final Property<String> classpathLayoutType

        private ResolvedManifest resolved

        ManifestImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
            super(project, ownerConfig, parentConfig)

            addClasspath = project.objects.property(Boolean)
            classpathPrefix = project.objects.property(String)
            classpathLayoutType = project.objects.property(String)
        }

        @Override
        void normalize() {
            if (!(['simple', 'repository'].contains(classpathLayoutType.orNull))) {
                project.logger.warn("[${project.path}] Unsupported value for config.artifacts.jar.manifest.classpathLayoutType. Using 'simple' instead.")
                classpathLayoutType.set('simple')
            }
        }

        ResolvedManifest asResolved() {
            if (!resolved) {
                resolved = new ResolvedManifestImpl(project.providers,
                    parentConfig?.asResolved()?.artifacts?.jar?.manifest,
                    this)
            }

            resolved
        }
    }

    @PackageScope
    @CompileStatic
    static class ResolvedManifestImpl extends AbstractResolvedFeature implements ResolvedManifest {
        final Provider<Boolean> enabled
        final Provider<Boolean> addClasspath
        final Provider<String> classpathPrefix
        final Provider<String> classpathLayoutType

        ResolvedManifestImpl(ProviderFactory providers, ResolvedManifest parent, ManifestImpl self) {
            super(self.project)
            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                true)

            addClasspath = booleanProvider(providers,
                parent?.addClasspath,
                self.addClasspath,
                false)

            classpathPrefix = stringProvider(providers,
                parent?.classpathPrefix,
                self.classpathPrefix,
                '')

            classpathLayoutType = stringProvider(providers,
                parent?.classpathLayoutType,
                self.classpathLayoutType,
                'simple')
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<>([
                enabled  : enabled.get(),
                addClasspath  : addClasspath.get(),
                classpathPrefix: classpathPrefix.get(),
                classpathLayoutType  : classpathLayoutType.get()
            ])

            new LinkedHashMap<>('manifest': map)
        }
    }
}
