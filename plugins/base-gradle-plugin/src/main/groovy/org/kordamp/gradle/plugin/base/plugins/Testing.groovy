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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.test.tasks.FunctionalTest
import org.kordamp.gradle.plugin.test.tasks.IntegrationTest
import org.kordamp.gradle.util.AnsiConsole
import org.kordamp.gradle.util.ConfigureUtil

import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.14.0
 */
@CompileStatic
class Testing extends AbstractTestingFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.testing'

    boolean logging = true
    boolean aggregate = true
    boolean jar = false
    double timeThreshold = -1

    private boolean loggingSet = false
    private boolean aggregateSet = false
    private boolean jarSet = false

    final Integration integration
    final Functional functional
    final Colors colors

    private final Set<Test> testTasks = new LinkedHashSet<>()
    private final Set<IntegrationTest> integrationTasks = new LinkedHashSet<>()
    private final Set<FunctionalTest> functionalTestTasks = new LinkedHashSet<>()

    Testing(ProjectConfigurationExtension config, Project project) {
        super(config, project, PLUGIN_ID)
        integration = new Integration(this)
        functional = new Functional(this)
        colors = new Colors()
    }

    @Override
    protected AbstractFeature getParentFeature() {
        return project.rootProject.extensions.getByType(ProjectConfigurationExtension).testing
    }

    void setLogging(boolean logging) {
        this.logging = logging
        this.loggingSet = true
    }

    boolean isLoggingSet() {
        this.loggingSet
    }

    void setAggregate(boolean aggregate) {
        this.aggregate = aggregate
        this.aggregateSet = true
    }

    boolean isAggregateSet() {
        this.aggregateSet
    }

    void setJar(boolean jar) {
        this.jar = jar
        this.jarSet = true
    }

    boolean isJarSet() {
        this.jarSet
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        new LinkedHashMap<>('testing': new LinkedHashMap<>([
            enabled      : enabled,
            logging      : logging,
            jar          : jar,
            timeThreshold: timeThreshold,
            aggregate    : aggregate,
            integration  : integration.toMap(),
            functional   : functional.toMap(),
            colors       : colors.toMap()
        ]))
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

    void colors(Action<? super Colors> action) {
        action.execute(colors)
    }

    void colors(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Colors) Closure<Void> action) {
        ConfigureUtil.configure(action, colors)
    }

    static void merge(Testing o1, Testing o2) {
        AbstractFeature.merge(o1, o2)
        o1.setLogging((boolean) (o1.loggingSet ? o1.logging : o2.logging))
        o1.setTimeThreshold((o1.timeThreshold != -1 ? o1.timeThreshold : (o2.timeThreshold != -1 ? o2.timeThreshold : 2000d)))
        o1.setJar((boolean) (o1.jarSet ? o1.jar : o2.jar))
        o1.setAggregate((boolean) (o1.aggregateSet ? o1.aggregate : o2.aggregate))
        Integration.merge(o1.integration, o2.integration)
        Functional.merge(o1.functional, o2.functional)
        Colors.merge(o1.colors, o2.colors)
    }

    void postMerge() {
        integration.postMerge()
        functional.postMerge()
        colors.postMerge()
        super.postMerge()
    }

    Set<Test> testTasks() {
        testTasks
    }

    Set<IntegrationTest> integrationTasks() {
        integrationTasks
    }

    Set<FunctionalTest> functionalTestTasks() {
        functionalTestTasks
    }

    @CompileStatic
    static class Colors {
        String success
        String failure
        String ignored

        static final List<String> VALID_COLORS = [
            'black', 'red', 'green', 'yellow', 'blue', 'magenta', 'cyan', 'white'
        ]

        Map<String, Object> toMap() {
            new LinkedHashMap<String, Object>([
                success: success,
                failure: failure,
                ignored: ignored
            ])
        }

        static void merge(Colors o1, Colors o2) {
            o1.setSuccess((isNotBlank(o1.success) ? o1.success : (o2.success ?: 'green')).trim().toLowerCase())
            o1.setFailure((isNotBlank(o1.failure) ? o1.failure : (o2.failure ?: 'red')).trim().toLowerCase())
            o1.setIgnored((isNotBlank(o1.ignored) ? o1.ignored : (o2.ignored ?: 'yellow')).trim().toLowerCase())
        }

        void postMerge() {
            if (!(success in VALID_COLORS)) success = 'green'
            if (!(failure in VALID_COLORS)) failure = 'red'
            if (!(ignored in VALID_COLORS)) ignored = 'yellow'
        }

        @CompileDynamic
        String success(AnsiConsole console, CharSequence s) {
            console."${success}"(s)
        }

        @CompileDynamic
        String failure(AnsiConsole console, CharSequence s) {
            console."${failure}"(s)
        }

        @CompileDynamic
        String ignored(AnsiConsole console, CharSequence s) {
            console."${ignored}"(s)
        }
    }
}
