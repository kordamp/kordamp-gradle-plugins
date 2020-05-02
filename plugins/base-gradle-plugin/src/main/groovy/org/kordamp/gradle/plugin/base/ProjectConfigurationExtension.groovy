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
package org.kordamp.gradle.plugin.base

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.kordamp.gradle.plugin.base.model.Information
import org.kordamp.gradle.plugin.base.plugins.Bintray
import org.kordamp.gradle.plugin.base.plugins.Bom
import org.kordamp.gradle.plugin.base.plugins.BuildInfo
import org.kordamp.gradle.plugin.base.plugins.Checkstyle
import org.kordamp.gradle.plugin.base.plugins.Clirr
import org.kordamp.gradle.plugin.base.plugins.Codenarc
import org.kordamp.gradle.plugin.base.plugins.Coveralls
import org.kordamp.gradle.plugin.base.plugins.Detekt
import org.kordamp.gradle.plugin.base.plugins.ErrorProne
import org.kordamp.gradle.plugin.base.plugins.Feature
import org.kordamp.gradle.plugin.base.plugins.Groovydoc
import org.kordamp.gradle.plugin.base.plugins.Guide
import org.kordamp.gradle.plugin.base.plugins.Jacoco
import org.kordamp.gradle.plugin.base.plugins.Jar
import org.kordamp.gradle.plugin.base.plugins.Javadoc
import org.kordamp.gradle.plugin.base.plugins.Kotlindoc
import org.kordamp.gradle.plugin.base.plugins.Licensing
import org.kordamp.gradle.plugin.base.plugins.Minpom
import org.kordamp.gradle.plugin.base.plugins.Plugin
import org.kordamp.gradle.plugin.base.plugins.Pmd
import org.kordamp.gradle.plugin.base.plugins.Publishing
import org.kordamp.gradle.plugin.base.plugins.Scaladoc
import org.kordamp.gradle.plugin.base.plugins.Sonar
import org.kordamp.gradle.plugin.base.plugins.Source
import org.kordamp.gradle.plugin.base.plugins.SourceHtml
import org.kordamp.gradle.plugin.base.plugins.SourceXref
import org.kordamp.gradle.plugin.base.plugins.Spotbugs
import org.kordamp.gradle.plugin.base.plugins.Stats
import org.kordamp.gradle.plugin.base.plugins.Testing

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
interface ProjectConfigurationExtension {
    Property<Boolean> getRelease()

    void info(Action<? super Information> action)

    void info(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Information) Closure<Void> action)

    void bom(Action<? super Bom> action)

    void bom(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Bom) Closure<Void> action)

    void bintray(Action<? super Bintray> action)

    void bintray(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Bintray) Closure<Void> action)

    void buildInfo(Action<? super BuildInfo> action)

    void buildInfo(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = BuildInfo) Closure<Void> action)

    void clirr(Action<? super Clirr> action)

    void clirr(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Clirr) Closure<Void> action)

    void licensing(Action<? super Licensing> action)

    void licensing(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Licensing) Closure<Void> action)

    void plugin(Action<? super Plugin> action)

    void plugin(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Plugin) Closure<Void> action)

    void publishing(Action<? super Publishing> action)

    void publishing(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Publishing) Closure<Void> action)

    void stats(Action<? super Stats> action)

    void stats(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Stats) Closure<Void> action)

    void testing(Action<? super Testing> action)

    void testing(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Testing) Closure<Void> action)

    void artifacts(Action<? super Artifacts> action)

    void artifacts(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifacts) Closure<Void> action)

    void docs(Action<? super Docs> action)

    void docs(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Docs) Closure<Void> action)

    void coverage(Action<? super Coverage> action)

    void coverage(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Coverage) Closure<Void> action)

    void quality(Action<? super Quality> action)

    void quality(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Quality) Closure<Void> action)

    @CompileStatic
    interface Quality extends Feature {
        void checkstyle(Action<? super Checkstyle> action)

        void checkstyle(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Checkstyle) Closure<Void> action)

        void codenarc(Action<? super Codenarc> action)

        void codenarc(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Codenarc) Closure<Void> action)

        void detekt(Action<? super Detekt> action)

        void detekt(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Detekt) Closure<Void> action)

        void errorprone(Action<? super ErrorProne> action)

        void errorprone(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ErrorProne) Closure<Void> action)

        void pmd(Action<? super Pmd> action)

        void pmd(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Pmd) Closure<Void> action)

        void sonar(Action<? super Sonar> action)

        void sonar(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Sonar) Closure<Void> action)

        void spotbugs(Action<? super Spotbugs> action)

        void spotbugs(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Spotbugs) Closure<Void> action)
    }

    @CompileStatic
    interface Coverage extends Feature {
        void coveralls(Action<? super Coveralls> action)

        void coveralls(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Coveralls) Closure<Void> action)

        void jacoco(Action<? super Jacoco> action)

        void jacoco(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Jacoco) Closure<Void> action)
    }

    @CompileStatic
    interface Docs extends Feature {
        void guide(Action<? super Guide> action)

        void guide(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Guide) Closure<Void> action)

        void groovydoc(Action<? super Groovydoc> action)

        void groovydoc(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Groovydoc) Closure<Void> action)

        void kotlindoc(Action<? super Kotlindoc> action)

        void kotlindoc(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Kotlindoc) Closure<Void> action)

        void javadoc(Action<? super Javadoc> action)

        void javadoc(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Javadoc) Closure<Void> action)

        void scaladoc(Action<? super Scaladoc> action)

        void scaladoc(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Scaladoc) Closure<Void> action)

        void sourceHtml(Action<? super SourceHtml> action)

        void sourceHtml(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SourceHtml) Closure<Void> action)

        void sourceXref(Action<? super SourceXref> action)

        void sourceXref(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SourceXref) Closure<Void> action)
    }

    @CompileStatic
    interface Artifacts extends Feature {
        void jar(Action<? super Jar> action)

        void jar(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Jar) Closure<Void> action)

        void minpom(Action<? super Minpom> action)

        void minpom(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Minpom) Closure<Void> action)

        void source(Action<? super Source> action)

        void source(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Source) Closure<Void> action)
    }
}
