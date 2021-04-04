/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2021 Andres Almiray.
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
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.model.Information
import org.kordamp.gradle.plugin.base.model.artifact.DependencyManagement
import org.kordamp.gradle.plugin.base.plugins.Bintray
import org.kordamp.gradle.plugin.base.plugins.Bom
import org.kordamp.gradle.plugin.base.plugins.BuildInfo
import org.kordamp.gradle.plugin.base.plugins.Checkstyle
import org.kordamp.gradle.plugin.base.plugins.Clirr
import org.kordamp.gradle.plugin.base.plugins.Codenarc
import org.kordamp.gradle.plugin.base.plugins.Coveralls
import org.kordamp.gradle.plugin.base.plugins.Cpd
import org.kordamp.gradle.plugin.base.plugins.Dependencies

import org.kordamp.gradle.plugin.base.plugins.Detekt
import org.kordamp.gradle.plugin.base.plugins.ErrorProne
import org.kordamp.gradle.plugin.base.plugins.Groovydoc
import org.kordamp.gradle.plugin.base.plugins.Guide
import org.kordamp.gradle.plugin.base.plugins.Jacoco
import org.kordamp.gradle.plugin.base.plugins.Jar
import org.kordamp.gradle.plugin.base.plugins.Javadoc
import org.kordamp.gradle.plugin.base.plugins.Kotlindoc
import org.kordamp.gradle.plugin.base.plugins.Licensing
import org.kordamp.gradle.plugin.base.plugins.Minpom
import org.kordamp.gradle.plugin.base.plugins.Plugins
import org.kordamp.gradle.plugin.base.plugins.Pmd
import org.kordamp.gradle.plugin.base.plugins.Publishing
import org.kordamp.gradle.plugin.base.plugins.Reproducible
import org.kordamp.gradle.plugin.base.plugins.Scaladoc
import org.kordamp.gradle.plugin.base.plugins.Sonar
import org.kordamp.gradle.plugin.base.plugins.Source
import org.kordamp.gradle.plugin.base.plugins.SourceHtml
import org.kordamp.gradle.plugin.base.plugins.SourceXref
import org.kordamp.gradle.plugin.base.plugins.Spotbugs
import org.kordamp.gradle.plugin.base.plugins.Stats
import org.kordamp.gradle.plugin.base.plugins.Testing
import org.kordamp.gradle.util.ConfigureUtil

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class ProjectConfigurationExtension {
    static final String CONFIG_NAME = 'config'

    boolean release = false

    final Project project
    final Information info
    final Dependencies dependencyManagement
    final Bom bom
    final Bintray bintray
    final BuildInfo buildInfo
    final Clirr clirr
    final Licensing licensing
    final Reproducible reproducible
    final Plugins plugins
    final Publishing publishing
    final Stats stats
    final Testing testing
    final Artifacts artifacts
    final Docs docs
    final Coverage coverage
    final Quality quality

    private boolean releaseSet

    ProjectConfigurationExtension(Project project) {
        this.project = project
        info = new Information(this, project)
        dependencyManagement = new Dependencies(this, project)
        bom = new Bom(this, project)
        bintray = new Bintray(this, project)
        buildInfo = new BuildInfo(this, project)
        clirr = new Clirr(this, project)
        licensing = new Licensing(this, project)
        reproducible = new Reproducible(this, project)
        plugins = new Plugins(this, project)
        publishing = new Publishing(this, project)
        stats = new Stats(this, project)
        testing = new Testing(this, project)

        artifacts = new Artifacts(this, project)
        docs = new Docs(this, project)
        coverage = new Coverage(this, project)
        quality = new Quality(this, project)
    }

    Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(release: release)

        map.putAll(info.toMap())
        map.putAll(dependencyManagement.toMap())
        if (buildInfo.visible) map.putAll(buildInfo.toMap())
        map.putAll(artifacts.toMap())
        if (publishing.visible) map.putAll(publishing.toMap())
        if (bom.visible) map.putAll(bom.toMap())
        if (licensing.visible) map.putAll(licensing.toMap())
        if (reproducible.visible) map.putAll(reproducible.toMap())
        map.putAll(docs.toMap())
        map.putAll(coverage.toMap())
        map.putAll(quality.toMap())
        if (testing.visible) map.putAll(testing.toMap())
        if (clirr.visible) map.putAll(clirr.toMap())
        if (plugins.visible) map.putAll(plugins.toMap())
        if (plugins.visible) map.putAll(plugins.toMap())
        if (stats.visible) map.putAll(stats.toMap())

        map
    }

    void info(Action<? super Information> action) {
        action.execute(info)
    }

    void info(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Information) Closure<Void> action) {
        ConfigureUtil.configure(action, info)
    }

    void dependencyManagement(Action<? super DependencyManagement> action) {
        action.execute(dependencyManagement)
    }

    void dependencyManagement(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DependencyManagement) Closure<Void> action) {
        ConfigureUtil.configure(action, dependencyManagement)
    }

    DependencyManagement getDependencyManagement() {
        this.dependencyManagement
    }

    @Deprecated
    Dependencies getDependencies() {
        println("The method config.dependencies is deprecated and will be removed in the future. Use config.dependencyManagement instead")
        dependencyManagement
    }

    @Deprecated
    void dependencies(Action<? super Dependencies> action) {
        println("The method config.dependencies is deprecated and will be removed in the future. Use config.dependencyManagement instead")
        action.execute(dependencyManagement)
    }

    @Deprecated
    void dependencies(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Dependencies) Closure<Void> action) {
        println("The method config.dependencies is deprecated and will be removed in the future. Use config.dependencyManagement instead")
        ConfigureUtil.configure(action, dependencyManagement)
    }

    void bom(Action<? super Bom> action) {
        action.execute(bom)
    }

    void bom(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Bom) Closure<Void> action) {
        ConfigureUtil.configure(action, bom)
    }

    @Deprecated
    void bintray(Action<? super Bintray> action) {
        println("The method config.bintray is deprecated and will be removed in the future.")
        action.execute(bintray)
    }

    @Deprecated
    void bintray(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Bintray) Closure<Void> action) {
        println("The method config.bintray is deprecated and will be removed in the future.")
        ConfigureUtil.configure(action, bintray)
    }

    void buildInfo(Action<? super BuildInfo> action) {
        action.execute(buildInfo)
    }

    void buildInfo(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = BuildInfo) Closure<Void> action) {
        ConfigureUtil.configure(action, buildInfo)
    }

    void clirr(Action<? super Clirr> action) {
        action.execute(clirr)
    }

    void clirr(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Clirr) Closure<Void> action) {
        ConfigureUtil.configure(action, clirr)
    }

    void licensing(Action<? super Licensing> action) {
        action.execute(licensing)
    }

    void licensing(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Licensing) Closure<Void> action) {
        ConfigureUtil.configure(action, licensing)
    }

    void reproducible(Action<? super Reproducible> action) {
        action.execute(reproducible)
    }

    void reproducible(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Reproducible) Closure<Void> action) {
        ConfigureUtil.configure(action, reproducible)
    }

    void plugins(Action<? super Plugins> action) {
        action.execute(plugins)
    }

    void plugins(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Plugins) Closure<Void> action) {
        ConfigureUtil.configure(action, plugins)
    }

    void publishing(Action<? super Publishing> action) {
        action.execute(publishing)
    }

    void publishing(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Publishing) Closure<Void> action) {
        ConfigureUtil.configure(action, publishing)
    }

    void stats(Action<? super Stats> action) {
        action.execute(stats)
    }

    void stats(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Stats) Closure<Void> action) {
        ConfigureUtil.configure(action, stats)
    }

    void testing(Action<? super Testing> action) {
        action.execute(testing)
    }

    void testing(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Testing) Closure<Void> action) {
        ConfigureUtil.configure(action, testing)
    }

    void artifacts(Action<? super Artifacts> action) {
        action.execute(artifacts)
    }

    void artifacts(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifacts) Closure<Void> action) {
        ConfigureUtil.configure(action, artifacts)
    }

    void docs(Action<? super Docs> action) {
        action.execute(docs)
    }

    void docs(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Docs) Closure<Void> action) {
        ConfigureUtil.configure(action, docs)
    }

    void coverage(Action<? super Coverage> action) {
        action.execute(coverage)
    }

    void coverage(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Coverage) Closure<Void> action) {
        ConfigureUtil.configure(action, coverage)
    }

    void quality(Action<? super Quality> action) {
        action.execute(quality)
    }

    void quality(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Quality) Closure<Void> action) {
        ConfigureUtil.configure(action, quality)
    }

    void setRelease(boolean release) {
        this.release = release
        this.releaseSet = true
    }

    boolean isReleaseSet() {
        return releaseSet
    }

    ProjectConfigurationExtension merge(ProjectConfigurationExtension other) {
        this.setRelease((boolean) (this.@releaseSet ? this.@release : other.@release))
        Information.merge(this.@info, other.@info)
        Dependencies.merge(this.@dependencyManagement, other.@dependencyManagement)
        Bom.merge(this.@bom, other.@bom)
        Bintray.merge(this.@bintray, other.@bintray)
        BuildInfo.merge(this.@buildInfo, other.@buildInfo)
        Clirr.merge(this.@clirr, other.@clirr)
        Licensing.merge(this.@licensing, other.@licensing)
        Reproducible.merge(this.@reproducible, other.@reproducible)
        Plugins.merge(this.@plugins, other.@plugins)
        Publishing.merge(this.@publishing, other.@publishing)
        Stats.merge(this.@stats, other.@stats)
        Testing.merge(this.@testing, other.@testing)
        Artifacts.merge(this.@artifacts, other.@artifacts)
        Docs.merge(this.@docs, other.@docs)
        Coverage.merge(this.@coverage, other.@coverage)
        Quality.merge(this.@quality, other.@quality)

        this.postMerge()
    }

    List<String> validate() {
        List<String> errors = []

        errors.addAll(this.@info.validate(this))
        errors.addAll(this.@bom.validate(this))
        errors.addAll(this.@bintray.validate(this))
        errors.addAll(this.@licensing.validate(this))
        errors.addAll(this.@reproducible.validate(this))
        errors.addAll(this.@plugins.validate(this))
        errors.addAll(this.@quality.validate(this))

        errors
    }

    ProjectConfigurationExtension normalize() {
        info.normalize()
        buildInfo.normalize()
        artifacts.normalize()
        bintray.normalize()
        publishing.normalize()
        bom.normalize()
        licensing.normalize()
        reproducible.normalize()
        testing.normalize()
        clirr.normalize()
        plugins.normalize()
        stats.normalize()
        docs.normalize()
        coverage.normalize()
        quality.normalize()
        this
    }

    ProjectConfigurationExtension postMerge() {
        buildInfo.postMerge()
        artifacts.postMerge()
        bintray.postMerge()
        publishing.postMerge()
        bom.postMerge()
        licensing.postMerge()
        reproducible.postMerge()
        testing.postMerge()
        clirr.postMerge()
        plugins.postMerge()
        stats.postMerge()
        docs.postMerge()
        coverage.postMerge()
        quality.postMerge()
        this
    }

    @CompileStatic
    static class Quality {
        final Checkstyle checkstyle
        final Codenarc codenarc
        final Detekt detekt
        final ErrorProne errorprone
        final Pmd pmd
        final Cpd cpd
        final Spotbugs spotbugs
        final Sonar sonar

        private final ProjectConfigurationExtension config
        private final Project project

        Quality(ProjectConfigurationExtension config, Project project) {
            this.config = config
            this.project = project
            checkstyle = new Checkstyle(config, project)
            codenarc = new Codenarc(config, project)
            detekt = new Detekt(config, project)
            errorprone = new ErrorProne(config, project)
            pmd = new Pmd(config, project)
            cpd = new Cpd(config, project)
            sonar = new Sonar(config, project)
            spotbugs = new Spotbugs(config, project)
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<String, Object>()

            if (checkstyle.visible) map.putAll(checkstyle.toMap())
            if (codenarc.visible) map.putAll(codenarc.toMap())
            if (detekt.visible) map.putAll(detekt.toMap())
            if (errorprone.visible) map.putAll(errorprone.toMap())
            if (pmd.visible) map.putAll(pmd.toMap())
            if (cpd.visible) map.putAll(cpd.toMap())
            if (sonar.visible) map.putAll(sonar.toMap())
            if (spotbugs.visible) map.putAll(spotbugs.toMap())

            map ? new LinkedHashMap<>('quality': map) : [:]
        }

        void checkstyle(Action<? super Checkstyle> action) {
            action.execute(checkstyle)
        }

        void checkstyle(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Checkstyle) Closure<Void> action) {
            ConfigureUtil.configure(action, checkstyle)
        }

        void codenarc(Action<? super Codenarc> action) {
            action.execute(codenarc)
        }

        void codenarc(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Codenarc) Closure<Void> action) {
            ConfigureUtil.configure(action, codenarc)
        }

        void detekt(Action<? super Detekt> action) {
            action.execute(detekt)
        }

        void detekt(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Detekt) Closure<Void> action) {
            ConfigureUtil.configure(action, detekt)
        }

        void errorprone(Action<? super ErrorProne> action) {
            action.execute(errorprone)
        }

        void errorprone(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ErrorProne) Closure<Void> action) {
            ConfigureUtil.configure(action, errorprone)
        }

        void pmd(Action<? super Pmd> action) {
            action.execute(pmd)
        }

        void pmd(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Pmd) Closure<Void> action) {
            ConfigureUtil.configure(action, pmd)
        }

        void cpd(Action<? super Cpd> action) {
            action.execute(cpd)
        }

        void cpd(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Cpd) Closure<Void> action) {
            ConfigureUtil.configure(action, cpd)
        }

        void sonar(Action<? super Sonar> action) {
            action.execute(sonar)
        }

        void sonar(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Sonar) Closure<Void> action) {
            ConfigureUtil.configure(action, sonar)
        }

        void spotbugs(Action<? super Spotbugs> action) {
            action.execute(spotbugs)
        }

        void spotbugs(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Spotbugs) Closure<Void> action) {
            ConfigureUtil.configure(action, spotbugs)
        }

        static Quality merge(Quality o1, Quality o2) {
            Checkstyle.merge(o1.@checkstyle, o2.@checkstyle)
            Codenarc.merge(o1.@codenarc, o2.@codenarc)
            Detekt.merge(o1.@detekt, o2.@detekt)
            ErrorProne.merge(o1.@errorprone, o2.@errorprone)
            Pmd.merge(o1.@pmd, o2.@pmd)
            Cpd.merge(o1.@cpd, o2.@cpd)
            Sonar.merge(o1.@sonar, o2.@sonar)
            Spotbugs.merge(o1.@spotbugs, o2.@spotbugs)
            o1
        }

        List<String> validate(ProjectConfigurationExtension extension) {
            List<String> errors = []

            errors.addAll(sonar.validate(extension))

            errors
        }

        Quality normalize() {
            checkstyle.normalize()
            codenarc.normalize()
            detekt.normalize()
            errorprone.normalize()
            pmd.normalize()
            cpd.normalize()
            sonar.normalize()
            spotbugs.normalize()
            this
        }

        Quality postMerge() {
            checkstyle.postMerge()
            codenarc.postMerge()
            detekt.postMerge()
            errorprone.postMerge()
            pmd.postMerge()
            cpd.postMerge()
            sonar.postMerge()
            spotbugs.postMerge()
            this
        }
    }

    @CompileStatic
    static class Coverage {
        final Coveralls coveralls
        final Jacoco jacoco

        private final ProjectConfigurationExtension config
        private final Project project

        Coverage(ProjectConfigurationExtension config, Project project) {
            this.config = config
            this.project = project
            coveralls = new Coveralls(config, project)
            jacoco = new Jacoco(config, project)
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<String, Object>()

            if (jacoco.visible) map.putAll(jacoco.toMap())
            if (coveralls.visible) map.putAll(coveralls.toMap())

            map ? new LinkedHashMap<>('coverage': map) : [:]
        }

        void coveralls(Action<? super Coveralls> action) {
            action.execute(coveralls)
        }

        void coveralls(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Coveralls) Closure<Void> action) {
            ConfigureUtil.configure(action, coveralls)
        }

        void jacoco(Action<? super Jacoco> action) {
            action.execute(jacoco)
        }

        void jacoco(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Jacoco) Closure<Void> action) {
            ConfigureUtil.configure(action, jacoco)
        }

        static Coverage merge(Coverage o1, Coverage o2) {
            Jacoco.merge(o1.@jacoco, o2.@jacoco)
            Coveralls.merge(o1.@coveralls, o2.@coveralls)
            o1
        }

        Coverage normalize() {
            jacoco.normalize()
            coveralls.normalize()
            this
        }

        Coverage postMerge() {
            jacoco.postMerge()
            coveralls.postMerge()
            this
        }
    }

    @CompileStatic
    static class Docs {
        final Guide guide
        final Groovydoc groovydoc
        final Kotlindoc kotlindoc
        final Javadoc javadoc
        final Scaladoc scaladoc
        final SourceHtml sourceHtml
        final SourceXref sourceXref

        private final ProjectConfigurationExtension config
        private final Project project

        Docs(ProjectConfigurationExtension config, Project project) {
            this.config = config
            this.project = project
            guide = new Guide(config, project)
            groovydoc = new Groovydoc(config, project)
            kotlindoc = new Kotlindoc(config, project)
            javadoc = new Javadoc(config, project)
            scaladoc = new Scaladoc(config, project)
            sourceHtml = new SourceHtml(config, project)
            sourceXref = new SourceXref(config, project)
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<String, Object>()

            if (javadoc.visible) map.putAll(javadoc.toMap())
            if (groovydoc.visible) map.putAll(groovydoc.toMap())
            if (kotlindoc.visible) map.putAll(kotlindoc.toMap())
            if (scaladoc.visible) map.putAll(scaladoc.toMap())
            if (sourceHtml.visible) map.putAll(sourceHtml.toMap())
            if (sourceXref.visible) map.putAll(sourceXref.toMap())
            if (guide.visible) map.putAll(guide.toMap())

            map ? new LinkedHashMap<>('docs': map) : [:]
        }

        void guide(Action<? super Guide> action) {
            action.execute(guide)
        }

        void guide(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Guide) Closure<Void> action) {
            ConfigureUtil.configure(action, guide)
        }

        void groovydoc(Action<? super Groovydoc> action) {
            action.execute(groovydoc)
        }

        void groovydoc(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Groovydoc) Closure<Void> action) {
            ConfigureUtil.configure(action, groovydoc)
        }

        void kotlindoc(Action<? super Kotlindoc> action) {
            action.execute(kotlindoc)
        }

        void kotlindoc(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Kotlindoc) Closure<Void> action) {
            ConfigureUtil.configure(action, kotlindoc)
        }

        void javadoc(Action<? super Javadoc> action) {
            action.execute(javadoc)
        }

        void javadoc(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Javadoc) Closure<Void> action) {
            ConfigureUtil.configure(action, javadoc)
        }

        void scaladoc(Action<? super Scaladoc> action) {
            action.execute(scaladoc)
        }

        void scaladoc(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Scaladoc) Closure<Void> action) {
            ConfigureUtil.configure(action, scaladoc)
        }

        void sourceHtml(Action<? super SourceHtml> action) {
            action.execute(sourceHtml)
        }

        void sourceHtml(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SourceHtml) Closure<Void> action) {
            ConfigureUtil.configure(action, sourceHtml)
        }

        void sourceXref(Action<? super SourceXref> action) {
            action.execute(sourceXref)
        }

        void sourceXref(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SourceXref) Closure<Void> action) {
            ConfigureUtil.configure(action, sourceXref)
        }

        static Docs merge(Docs o1, Docs o2) {
            Guide.merge(o1.@guide, o2.@guide)
            Groovydoc.merge(o1.@groovydoc, o2.@groovydoc)
            Kotlindoc.merge(o1.@kotlindoc, o2.@kotlindoc)
            Javadoc.merge(o1.@javadoc, o2.@javadoc)
            Scaladoc.merge(o1.@scaladoc, o2.@scaladoc)
            SourceHtml.merge(o1.@sourceHtml, o2.@sourceHtml)
            SourceXref.merge(o1.@sourceXref, o2.@sourceXref)
            o1
        }

        Docs normalize() {
            guide.normalize()
            groovydoc.normalize()
            kotlindoc.normalize()
            javadoc.normalize()
            scaladoc.normalize()
            sourceHtml.normalize()
            sourceXref.normalize()
            this
        }

        Docs postMerge() {
            guide.postMerge()
            groovydoc.postMerge()
            kotlindoc.postMerge()
            javadoc.postMerge()
            scaladoc.postMerge()
            sourceHtml.postMerge()
            sourceXref.postMerge()
            this
        }
    }

    @CompileStatic
    static class Artifacts {
        final Jar jar
        final Minpom minpom
        final Source source

        private final ProjectConfigurationExtension config
        private final Project project

        Artifacts(ProjectConfigurationExtension config, Project project) {
            this.config = config
            this.project = project
            jar = new Jar(config, project)
            minpom = new Minpom(config, project)
            source = new Source(config, project)
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<String, Object>()

            if (jar.visible) map.putAll(jar.toMap())
            if (minpom.visible) map.putAll(minpom.toMap())
            if (source.visible) map.putAll(source.toMap())

            map ? new LinkedHashMap<>('artifacts': map) : [:]
        }

        void jar(Action<? super Jar> action) {
            action.execute(jar)
        }

        void jar(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Jar) Closure<Void> action) {
            ConfigureUtil.configure(action, jar)
        }

        void minpom(Action<? super Minpom> action) {
            action.execute(minpom)
        }

        void minpom(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Minpom) Closure<Void> action) {
            ConfigureUtil.configure(action, minpom)
        }

        void source(Action<? super Source> action) {
            action.execute(source)
        }

        void source(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Source) Closure<Void> action) {
            ConfigureUtil.configure(action, source)
        }

        static Artifacts merge(Artifacts o1, Artifacts o2) {
            Jar.merge(o1.@jar, o2.@jar)
            Minpom.merge(o1.@minpom, o2.@minpom)
            Source.merge(o1.@source, o2.@source)
            o1
        }

        Artifacts normalize() {
            jar.normalize()
            minpom.normalize()
            source.normalize()
            this
        }

        Artifacts postMerge() {
            jar.postMerge()
            minpom.postMerge()
            source.postMerge()
            this
        }
    }
}
