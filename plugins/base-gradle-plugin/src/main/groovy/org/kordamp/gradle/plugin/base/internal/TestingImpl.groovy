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
package org.kordamp.gradle.plugin.base.internal

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.plugin.base.plugins.Testing
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedTesting
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedTesting.ResolvedFunctional
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedTesting.ResolvedIntegration

import static org.kordamp.gradle.PropertyUtils.booleanProvider
import static org.kordamp.gradle.PropertyUtils.stringProvider

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@PackageScope
@CompileStatic
class TestingImpl extends AbstractFeature implements Testing {
    final Property<Boolean> logging
    final Property<Boolean> aggregate
    final IntegrationImpl integration
    final FunctionalImpl functional

    private ResolvedTesting resolved

    TestingImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
        super(project, ownerConfig, parentConfig)

        logging = project.objects.property(Boolean)
        aggregate = project.objects.property(Boolean)

        integration = new IntegrationImpl(project, ownerConfig, parentConfig)
        functional = new FunctionalImpl(project, ownerConfig, parentConfig)
    }

    void integration(Action<? super Integration> action) {
        action.execute(integration)
    }

    void integration(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Integration) Closure<Void> action) {
        ConfigureUtil.configure(action, integration)
    }

    void functional(Action<? super Functional> action) {
        action.execute(functional)
    }

    void functional(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Functional) Closure<Void> action) {
        ConfigureUtil.configure(action, functional)
    }

    @Override
    void normalize() {
        super.normalize()
        integration.normalize()
        functional.normalize()
    }

    @Override
    void validate(List<String> errors) {
        integration.validate(errors)
        functional.validate(errors)
    }

    ResolvedTesting asResolved() {
        if (!resolved) {
            resolved = new ResolvedTestingImpl(project.providers,
                parentConfig?.asResolved()?.testing,
                this)
        }
        resolved
    }

    @PackageScope
    @CompileStatic
    static class ResolvedTestingImpl extends AbstractResolvedFeature implements ResolvedTesting {
        final Provider<Boolean> enabled
        final Provider<Boolean> logging
        final Provider<Boolean> aggregate

        private TestingImpl self

        ResolvedTestingImpl(ProviderFactory providers, ResolvedTesting parent, TestingImpl self) {
            super(self.project)
            this.self = self

            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                true)

            logging = booleanProvider(providers,
                parent?.logging,
                self.logging,
                true)

            aggregate = booleanProvider(providers,
                parent?.aggregate,
                self.aggregate,
                true)
        }

        @Override
        ResolvedIntegration getIntegration() {
            self.integration.asResolved()
        }

        @Override
        ResolvedFunctional getFunctional() {
            self.functional.asResolved()
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<>([
                enabled  : enabled.get(),
                logging  : logging.get(),
                aggregate: aggregate.get()
            ])
            map.putAll(getIntegration().toMap())
            map.putAll(getFunctional().toMap())

            new LinkedHashMap<>('testing': map)
        }
    }

    @PackageScope
    @CompileStatic
    static class IntegrationImpl extends AbstractFeature implements Integration {
        final Property<Boolean> logging
        final Property<Boolean> aggregate
        final Property<String> baseDir

        private ResolvedIntegration resolved

        IntegrationImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
            super(project, ownerConfig, parentConfig)
            logging = project.objects.property(Boolean)
            aggregate = project.objects.property(Boolean)
            baseDir = project.objects.property(String)
        }

        ResolvedIntegration asResolved() {
            if (!resolved) {
                resolved = new ResolvedIntegrationImpl(project.providers,
                    parentConfig?.asResolved()?.testing?.integration,
                    this)
            }
            resolved
        }
    }

    @PackageScope
    @CompileStatic
    static class ResolvedIntegrationImpl extends AbstractResolvedFeature implements ResolvedIntegration {
        final Provider<Boolean> enabled
        final Provider<Boolean> logging
        final Provider<Boolean> aggregate
        final Provider<String> baseDir

        ResolvedIntegrationImpl(ProviderFactory providers, ResolvedIntegration parent, IntegrationImpl self) {
            super(self.project)

            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                true)

            logging = booleanProvider(providers,
                parent?.logging,
                self.logging,
                true)

            aggregate = booleanProvider(providers,
                parent?.aggregate,
                self.aggregate,
                true)

            baseDir = stringProvider(providers,
                parent?.baseDir,
                self.baseDir,
                'src/integration-test')
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<>([
                enabled  : enabled.get(),
                logging  : logging.get(),
                aggregate: aggregate.get(),
                baseDir  : baseDir.get()
            ])

            new LinkedHashMap<>('integration': map)
        }
    }

    @PackageScope
    @CompileStatic
    static class FunctionalImpl extends AbstractFeature implements Functional {
        final Property<Boolean> logging
        final Property<Boolean> aggregate
        final Property<String> baseDir

        private ResolvedFunctional resolved

        FunctionalImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
            super(project, ownerConfig, parentConfig)
            logging = project.objects.property(Boolean)
            aggregate = project.objects.property(Boolean)
            baseDir = project.objects.property(String)
        }

        ResolvedFunctional asResolved() {
            if (!resolved) {
                resolved = new ResolvedFunctionalImpl(project.providers,
                    parentConfig?.asResolved()?.testing?.functional,
                    this)
            }
            resolved
        }
    }

    @PackageScope
    @CompileStatic
    static class ResolvedFunctionalImpl extends AbstractResolvedFeature implements ResolvedFunctional {
        final Provider<Boolean> enabled
        final Provider<Boolean> logging
        final Provider<Boolean> aggregate
        final Provider<String> baseDir

        ResolvedFunctionalImpl(ProviderFactory providers, ResolvedFunctional parent, FunctionalImpl self) {
            super(self.project)

            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                true)

            logging = booleanProvider(providers,
                parent?.logging,
                self.logging,
                true)

            aggregate = booleanProvider(providers,
                parent?.aggregate,
                self.aggregate,
                true)

            baseDir = stringProvider(providers,
                parent?.baseDir,
                self.baseDir,
                'src/functional-test')
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<>([
                enabled  : enabled.get(),
                logging  : logging.get(),
                aggregate: aggregate.get(),
                baseDir  : baseDir.get()
            ])

            new LinkedHashMap<>('functional': map)
        }
    }
}
