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
import org.kordamp.gradle.plugin.base.plugins.Guide
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedGuide
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedGuide.ResolvedPublish

import static org.kordamp.gradle.PropertyUtils.booleanProvider
import static org.kordamp.gradle.PropertyUtils.stringProvider

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@PackageScope
@CompileStatic
class GuideImpl extends AbstractFeature implements Guide {
    final PublishImpl publish

    private ResolvedGuide resolved

    GuideImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
        super(project, ownerConfig, parentConfig)

        publish = new PublishImpl(project, ownerConfig, parentConfig)
    }

    @Override
    void normalize() {
        super.normalize()
        publish.normalize()
    }

    @Override
    void validate(List<String> errors) {
        publish.validate(errors)
    }

    @Override
    void publish(Action<? super Publish> action) {
        action.execute(publish)
    }

    @Override
    void publish(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Publish) Closure<Void> action) {
        ConfigureUtil.configure(action, publish)
    }

    ResolvedGuide asResolved() {
        if (!resolved) {
            resolved = new ResolvedGuideImpl(project.providers,
                parentConfig?.asResolved()?.docs?.guide,
                this)
        }
        resolved
    }

    @PackageScope
    @CompileStatic
    static class ResolvedGuideImpl extends AbstractResolvedFeature implements ResolvedGuide {
        final Provider<Boolean> enabled

        private final GuideImpl self

        ResolvedGuideImpl(ProviderFactory providers, ResolvedGuide parent, GuideImpl self) {
            super(self.project)
            this.self = self

            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                true)
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<>([
                enabled: enabled.get(),
            ])
            map.putAll(getPublish().toMap())

            new LinkedHashMap<>('guide': map)
        }

        @Override
        ResolvedPublish getPublish() {
            self.publish.asResolved()
        }
    }

    @PackageScope
    @CompileStatic
    static class PublishImpl extends AbstractFeature implements Publish {
        final Property<String> branch
        final Property<String> message

        private ResolvedPublish resolved

        PublishImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
            super(project, ownerConfig, parentConfig)
            branch = project.objects.property(String)
            message = project.objects.property(String)
        }

        @Override
        void normalize() {
            if (!enabled.present) {
                enabled.set(project.pluginManager.hasPlugin('org.ajoberstar.git-publish'))
            }
        }

        ResolvedPublish asResolved() {
            if (!resolved) {
                resolved = new ResolvedPublishImpl(project.providers,
                    parentConfig?.asResolved()?.docs?.guide?.publish,
                    this)
            }
            resolved
        }
    }

    @PackageScope
    @CompileStatic
    static class ResolvedPublishImpl extends AbstractResolvedFeature implements ResolvedPublish {
        final Provider<Boolean> enabled
        final Provider<String> branch
        final Provider<String> message

        ResolvedPublishImpl(ProviderFactory providers, ResolvedPublish parent, PublishImpl self) {
            super(self.project)

            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                true)

            branch = stringProvider(providers,
                parent?.branch,
                self.branch,
                'gh-pages')

            message = stringProvider(providers,
                parent?.message,
                self.message,
                "Publish guide for ${self.project.version}")
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<>([
                enabled: enabled.get(),
                branch : branch.get(),
                message: message.get()
            ])

            new LinkedHashMap<>('publish': map)
        }
    }
}
