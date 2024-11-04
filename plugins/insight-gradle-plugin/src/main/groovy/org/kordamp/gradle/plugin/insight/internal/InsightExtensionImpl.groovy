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
package org.kordamp.gradle.plugin.insight.internal

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.provider.Providers
import org.gradle.api.invocation.Gradle
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.kordamp.gradle.plugin.insight.InsightExtension
import org.kordamp.gradle.plugin.insight.model.BuildReport
import org.kordamp.gradle.plugin.insight.model.Project
import org.kordamp.gradle.util.AnsiConsole
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.kordamp.gradle.property.PropertyUtils.booleanProvider

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@CompileStatic
class InsightExtensionImpl implements InsightExtension {
    final Property<Boolean> enabled
    final Provider<Boolean> resolvedEnabled
    final ColorsImpl colors
    final Set<? extends BuildReport> reports = new LinkedHashSet<>()

    private final Settings settings
    private final ObjectFactory objects

    InsightExtensionImpl(Settings settings, ObjectFactory objects, ProviderFactory providers) {
        this.settings = settings
        this.objects = objects

        this.enabled = objects.property(Boolean).convention(Providers.<Boolean>notDefined())
        this.resolvedEnabled = booleanProvider(providers, 'INSIGHT_ENABLED', 'insight.enabled', enabled, true)
        this.colors = objects.newInstance(ColorsImpl, objects, settings.gradle)
    }

    @Override
    void report(Class<? extends BuildReport> reportClass, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = BuildReport) Closure<Void> action) {
        BuildReport report = objects.newInstance(reportClass, objects)
        ConfigureUtil.configure(action, report)
        reports.add(report)
    }

    @Override
    public <T extends BuildReport> void report(Class<T> reportClass, Action<T> action) {
        BuildReport report = objects.newInstance(reportClass, objects)
        action.execute(report)
        reports.add(report)
    }

    @Override
    void colors(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Colors) Closure<Void> action) {
        ConfigureUtil.configure(action, colors)
    }

    @Override
    void colors(Action<? super Colors> action) {
        action.execute(colors)
    }

    @CompileStatic
    static class ColorsImpl implements Colors {
        final Property<String> success
        final Property<String> failure
        final Property<String> skipped
        final Property<String> partial

        private final AnsiConsole console

        static final List<String> VALID_COLORS = [
            'black', 'red', 'green', 'yellow', 'blue', 'magenta', 'cyan', 'white'
        ]

        @Inject
        ColorsImpl(ObjectFactory objects, Gradle gradle) {
            this.console = new AnsiConsole(gradle)
            this.success = objects.property(String).convention('green')
            this.failure = objects.property(String).convention('red')
            this.skipped = objects.property(String).convention('yellow')
            this.partial = objects.property(String).convention('cyan')
        }

        void normalize() {
            if (!(success.get() in VALID_COLORS)) success.set('green')
            if (!(failure.get() in VALID_COLORS)) failure.set('red')
            if (!(skipped.get() in VALID_COLORS)) skipped.set('yellow')
            if (!(partial.get() in VALID_COLORS)) partial.set('partial')
        }

        @CompileDynamic
        String state(Project.State state) {
            switch (state) {
                case Project.State.SUCCESS:
                    return console."${success.get()}"(state.name())
                case Project.State.PARTIAL:
                    return console."${partial.get()}"(state.name())
                case Project.State.FAILURE:
                    return console."${failure.get()}"(state.name())
                case Project.State.SKIPPED:
                    return console."${skipped.get()}"(state.name())
            }
        }

        @CompileDynamic
        String success(CharSequence s) {
            console."${success.get()}"(s)
        }

        @CompileDynamic
        String failure(CharSequence s) {
            console."${failure.get()}"(s)
        }

        @CompileDynamic
        String skipped(CharSequence s) {
            console."${skipped.get()}"(s)
        }

        @CompileDynamic
        String result(boolean failure) {
            failure ? console.red('FAILURE') : console.green('SUCCESS')
        }
    }
}
