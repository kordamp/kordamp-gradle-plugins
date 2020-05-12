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
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil
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
class ProjectConfigurationExtension {
    static final String CONFIG_NAME = 'config'
    static final String EFFECTIVE_CONFIG_NAME = 'effectiveConfig'

    boolean release = false

    final Project project
    final Information info
    final Bom bom
    final Bintray bintray
    final BuildInfo buildInfo
    final Clirr clirr
    final Licensing licensing
    final Plugin plugin
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
        bom = new Bom(this, project)
        bintray = new Bintray(this, project)
        buildInfo = new BuildInfo(this, project)
        clirr = new Clirr(this, project)
        licensing = new Licensing(this, project)
        plugin = new Plugin(this, project)
        publishing = new Publishing(this, project)
        stats = new Stats(this, project)
        testing = new Testing(this, project)

        artifacts = new Artifacts(this, project)
        docs = new Docs(this, project)
        coverage = new Coverage(this, project)
        quality = new Quality(this, project)
    }

    ProjectConfigurationExtension(ProjectConfigurationExtension other) {
        this(other.project)
        setRelease(other.release)
        other.info.copyInto(info)
        other.bom.copyInto(bom)
        other.bintray.copyInto(bintray)
        other.buildInfo.copyInto(buildInfo)
        other.clirr.copyInto(clirr)
        other.licensing.copyInto(licensing)
        other.plugin.copyInto(plugin)
        other.publishing.copyInto(publishing)
        other.stats.copyInto(stats)
        other.testing.copyInto(testing)
        other.artifacts.copyInto(artifacts)
        other.docs.copyInto(docs)
        other.coverage.copyInto(coverage)
        other.quality.copyInto(quality)
    }

    Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(release: release)

        map.putAll(info.toMap())
        map.putAll(buildInfo.toMap())
        map.putAll(artifacts.toMap())
        map.putAll(bintray.toMap())
        map.putAll(publishing.toMap())
        map.putAll(bom.toMap())
        map.putAll(licensing.toMap())
        map.putAll(docs.toMap())
        map.putAll(coverage.toMap())
        map.putAll(quality.toMap())
        map.putAll(testing.toMap())
        map.putAll(clirr.toMap())
        map.putAll(plugin.toMap())
        map.putAll(stats.toMap())

        map
    }

    @Deprecated
    Groovydoc getGroovydoc() {
        println("The method config.groovydoc is deprecated and will be removed in the future. Use config.docs.groovydoc instead")
        docs.groovydoc
    }

    @Deprecated
    Kotlindoc getKotlindoc() {
        println("The method config.kotlindoc is deprecated and will be removed in the future. Use config.docs.kotlindoc instead")
        docs.kotlindoc
    }

    @Deprecated
    Jacoco getJacoco() {
        println("The method config.jacoco is deprecated and will be removed in the future. Use config.coverage.jacoco instead")
        coverage.jacoco
    }

    @Deprecated
    Javadoc getJavadoc() {
        println("The method config.javadoc is deprecated and will be removed in the future. Use config.docs.javadoc instead")
        docs.javadoc
    }

    @Deprecated
    Scaladoc getScaladoc() {
        println("The method config.scaladoc is deprecated and will be removed in the future. Use config.docs.scaladoc instead")
        docs.scaladoc
    }

    @Deprecated
    SourceHtml getSourceHtml() {
        println("The method config.sourceHtml is deprecated and will be removed in the future. Use config.docs.sourceHtml instead")
        docs.sourceHtml
    }

    @Deprecated
    SourceXref getSourceXref() {
        println("The method config.sourceXref is deprecated and will be removed in the future. Use config.docs.sourceXref instead")
        docs.sourceXref
    }

    @Deprecated
    Source getSource() {
        println("The method config.source is deprecated and will be removed in the future. Use config.artifacts.source instead")
        artifacts.source
    }

    @Deprecated
    Minpom getMinpom() {
        println("The method config.minpom is deprecated and will be removed in the future. Use config.artifacts.minpom instead")
        artifacts.minpom
    }

    void info(Action<? super Information> action) {
        action.execute(info)
    }

    void info(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Information) Closure action) {
        ConfigureUtil.configure(action, info)
    }

    void bom(Action<? super Bom> action) {
        action.execute(bom)
    }

    void bom(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Bom) Closure action) {
        ConfigureUtil.configure(action, bom)
    }

    void bintray(Action<? super Bintray> action) {
        action.execute(bintray)
    }

    void bintray(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Bintray) Closure<Void> action) {
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

    @Deprecated
    void groovydoc(Action<? super Groovydoc> action) {
        println("The method config.groovydoc() is deprecated and will be removed in the future. Use config.docs.groovydoc() instead")
        docs.groovydoc(action)
    }

    @Deprecated
    void groovydoc(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Groovydoc) Closure<Void> action) {
        println("The method config.groovydoc() is deprecated and will be removed in the future. Use config.docs.groovydoc() instead")
        docs.groovydoc(action)
    }

    @Deprecated
    void kotlindoc(Action<? super Kotlindoc> action) {
        println("The method config.kotlindoc() is deprecated and will be removed in the future. Use config.docs.kotlindoc() instead")
        docs.kotlindoc(action)
    }

    @Deprecated
    void kotlindoc(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Kotlindoc) Closure<Void> action) {
        println("The method config.kotlindoc() is deprecated and will be removed in the future. Use config.docs.kotlindoc() instead")
        docs.kotlindoc(action)
    }

    @Deprecated
    void jacoco(Action<? super Jacoco> action) {
        println("The method config.jacoco() is deprecated and will be removed in the future. Use config.coverage.jacoco() instead")
        coverage.jacoco(action)
    }

    @Deprecated
    void jacoco(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Jacoco) Closure<Void> action) {
        println("The method config.jacoco() is deprecated and will be removed in the future. Use config.coverage.jacoco() instead")
        coverage.jacoco(action)
    }

    @Deprecated
    void javadoc(Action<? super Javadoc> action) {
        println("The method config.javadoc() is deprecated and will be removed in the future. Use config.docs.javadoc() instead")
        docs.javadoc(action)
    }

    @Deprecated
    void javadoc(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Javadoc) Closure<Void> action) {
        println("The method config.javadoc() is deprecated and will be removed in the future. Use config.docs.javadoc() instead")
        docs.javadoc(action)
    }

    void licensing(Action<? super Licensing> action) {
        action.execute(licensing)
    }

    void licensing(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Licensing) Closure<Void> action) {
        ConfigureUtil.configure(action, licensing)
    }

    @Deprecated
    void minpom(Action<? super Minpom> action) {
        println("The method config.minpom() is deprecated and will be removed in the future. Use config.artifacts.minpom() instead")
        artifacts.minpom(action)
    }

    @Deprecated
    void minpom(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Minpom) Closure<Void> action) {
        println("The method config.minpom() is deprecated and will be removed in the future. Use config.artifacts.minpom() instead")
        artifacts.minpom(action)
    }

    void plugin(Action<? super Plugin> action) {
        action.execute(plugin)
    }

    void plugin(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Plugin) Closure<Void> action) {
        ConfigureUtil.configure(action, plugin)
    }

    void publishing(Action<? super Publishing> action) {
        action.execute(publishing)
    }

    void publishing(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Publishing) Closure<Void> action) {
        ConfigureUtil.configure(action, publishing)
    }

    @Deprecated
    void scaladoc(Action<? super Scaladoc> action) {
        println("The method config.scaladoc() is deprecated and will be removed in the future. Use config.docs.scaladoc() instead")
        docs.scaladoc(action)
    }

    @Deprecated
    void scaladoc(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Scaladoc) Closure<Void> action) {
        println("The method config.scaladoc() is deprecated and will be removed in the future. Use config.docs.scaladoc() instead")
        docs.scaladoc(action)
    }

    @Deprecated
    void source(Action<? super Source> action) {
        println("The method config.source() is deprecated and will be removed in the future. Use config.artifacts.source() instead")
        artifacts.source(action)
    }

    @Deprecated
    void source(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Source) Closure<Void> action) {
        println("The method config.source() is deprecated and will be removed in the future. Use config.artifacts.source() instead")
        artifacts.source(action)
    }

    void stats(Action<? super Stats> action) {
        action.execute(stats)
    }

    void stats(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Stats) Closure<Void> action) {
        ConfigureUtil.configure(action, stats)
    }

    @Deprecated
    void sourceHtml(Action<? super SourceHtml> action) {
        println("The method config.sourceHtml() is deprecated and will be removed in the future. Use config.docs.sourceHtml() instead")
        docs.sourceHtml(action)
    }

    @Deprecated
    void sourceHtml(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SourceHtml) Closure<Void> action) {
        println("The method config.sourceHtml() is deprecated and will be removed in the future. Use config.docs.sourceHtml() instead")
        docs.sourceHtml(action)
    }

    @Deprecated
    void sourceXref(Action<? super SourceXref> action) {
        println("The method config.sourceXref() is deprecated and will be removed in the future. Use config.docs.sourceXref() instead")
        docs.sourceXref(action)
    }

    @Deprecated
    void sourceXref(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SourceXref) Closure<Void> action) {
        println("The method config.sourceXref() is deprecated and will be removed in the future. Use config.docs.sourceXref() instead")
        docs.sourceXref(action)
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

    ProjectConfigurationExtension copyOf() {
        ProjectConfigurationExtension copy = new ProjectConfigurationExtension(project)

        copy.@release = this.@release
        copy.@releaseSet = this.@releaseSet
        this.@info.copyInto(copy.@info)
        this.@bom.copyInto(copy.@bom)
        this.@bintray.copyInto(copy.@bintray)
        this.@buildInfo.copyInto(copy.@buildInfo)
        this.@clirr.copyInto(copy.@clirr)
        this.@licensing.copyInto(copy.@licensing)
        this.@plugin.copyInto(copy.@plugin)
        this.@publishing.copyInto(copy.@publishing)
        this.@stats.copyInto(copy.@stats)
        this.@testing.copyInto(copy.@testing)
        this.@artifacts.copyInto(copy.@artifacts)
        this.@docs.copyInto(copy.@docs)
        this.@coverage.copyInto(copy.@coverage)
        this.@quality.copyInto(copy.@quality)

        copy
    }

    ProjectConfigurationExtension merge(ProjectConfigurationExtension other) {
        ProjectConfigurationExtension copy = copyOf()
        copy.setRelease((boolean) (copy.@releaseSet ? copy.@release : other.@release))
        Information.merge(copy.@info, other.@info)
        Bom.merge(copy.@bom, other.@bom)
        Bintray.merge(copy.@bintray, other.@bintray)
        BuildInfo.merge(copy.@buildInfo, other.@buildInfo)
        Clirr.merge(copy.@clirr, other.@clirr)
        Licensing.merge(copy.@licensing, other.@licensing)
        Plugin.merge(copy.@plugin, other.@plugin)
        Publishing.merge(copy.@publishing, other.@publishing)
        Stats.merge(copy.@stats, other.@stats)
        Testing.merge(copy.@testing, other.@testing)
        Artifacts.merge(copy.@artifacts, other.@artifacts)
        Docs.merge(copy.@docs, other.@docs)
        Coverage.merge(copy.@coverage, other.@coverage)
        Quality.merge(copy.@quality, other.@quality)

        copy.postMerge()
    }

    List<String> validate() {
        List<String> errors = []

        errors.addAll(this.@info.validate(this))
        errors.addAll(this.@bom.validate(this))
        errors.addAll(this.@bintray.validate(this))
        errors.addAll(this.@licensing.validate(this))
        errors.addAll(this.@plugin.validate(this))
        errors.addAll(this.@quality.validate(this))

        errors
    }

    ProjectConfigurationExtension normalize() {
        info.normalize()
        bom.normalize()
        clirr.normalize()
        stats.normalize()
        licensing.normalize()
        plugin.normalize()
        publishing.normalize()
        artifacts.normalize()
        docs.normalize()
        quality.normalize()
        this
    }

    ProjectConfigurationExtension postMerge() {
        testing.postMerge()
        docs.postMerge()
        quality.postMerge()
        coverage.postMerge()
        stats.postMerge()
        this
    }

    @CompileStatic
    static class Quality {
        final Checkstyle checkstyle
        final Codenarc codenarc
        final Detekt detekt
        final ErrorProne errorprone
        final Pmd pmd
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
            sonar = new Sonar(config, project)
            spotbugs = new Spotbugs(config, project)
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<String, Object>()

            map.putAll(checkstyle.toMap())
            map.putAll(codenarc.toMap())
            map.putAll(detekt.toMap())
            map.putAll(errorprone.toMap())
            map.putAll(pmd.toMap())
            map.putAll(sonar.toMap())
            map.putAll(spotbugs.toMap())

            new LinkedHashMap<>('quality': map)
        }

        void copyInto(Quality copy) {
            checkstyle.copyInto(copy.checkstyle)
            codenarc.copyInto(copy.codenarc)
            detekt.copyInto(copy.detekt)
            errorprone.copyInto(copy.errorprone)
            pmd.copyInto(copy.pmd)
            sonar.copyInto(copy.sonar)
            spotbugs.copyInto(copy.spotbugs)
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

        Quality copyOf() {
            Quality copy = new Quality(config, project)
            this.checkstyle.copyInto(copy.@checkstyle)
            this.codenarc.copyInto(copy.@codenarc)
            this.detekt.copyInto(copy.@detekt)
            this.errorprone.copyInto(copy.@detekt)
            this.pmd.copyInto(copy.@pmd)
            this.sonar.copyInto(copy.@sonar)
            this.spotbugs.copyInto(copy.@spotbugs)
            copy
        }

        static Quality merge(Quality o1, Quality o2) {
            Checkstyle.merge(o1.@checkstyle, o2.@checkstyle)
            Codenarc.merge(o1.@codenarc, o2.@codenarc)
            Detekt.merge(o1.@detekt, o2.@detekt)
            ErrorProne.merge(o1.@errorprone, o2.@errorprone)
            Pmd.merge(o1.@pmd, o2.@pmd)
            Sonar.merge(o1.@sonar, o2.@sonar)
            Spotbugs.merge(o1.@spotbugs, o2.@spotbugs)

            o1
        }

        Quality postMerge() {
            this
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
            sonar.normalize()
            spotbugs.normalize()
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

            map.putAll(jacoco.toMap())
            map.putAll(coveralls.toMap())

            new LinkedHashMap<>('coverage': map)
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

        void copyInto(Coverage copy) {
            coveralls.copyInto(copy.@coveralls)
            jacoco.copyInto(copy.@jacoco)
        }

        Coverage copyOf() {
            Coverage copy = new Coverage(config, project)
            this.@coveralls.copyInto(copy.@coveralls)
            this.@jacoco.copyInto(copy.@jacoco)
            copy
        }

        static Coverage merge(Coverage o1, Coverage o2) {
            Coveralls.merge(o1.@coveralls, o2.@coveralls)
            Jacoco.merge(o1.@jacoco, o2.@jacoco)
            o1
        }

        Coverage postMerge() {
            jacoco.postMerge()
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

            map.putAll(javadoc.toMap())
            map.putAll(groovydoc.toMap())
            map.putAll(kotlindoc.toMap())
            map.putAll(scaladoc.toMap())
            map.putAll(sourceHtml.toMap())
            map.putAll(sourceXref.toMap())
            map.putAll(guide.toMap())

            new LinkedHashMap<>('docs': map)
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

        void copyInto(Docs copy) {
            guide.copyInto(copy.guide)
            groovydoc.copyInto(copy.groovydoc)
            kotlindoc.copyInto(copy.kotlindoc)
            javadoc.copyInto(copy.javadoc)
            scaladoc.copyInto(copy.scaladoc)
            sourceHtml.copyInto(copy.sourceHtml)
            sourceXref.copyInto(copy.sourceXref)
        }

        Docs copyOf() {
            Docs copy = new Docs(config, project)
            this.@guide.copyInto(copy.@guide)
            this.@groovydoc.copyInto(groovydoc)
            this.@kotlindoc.copyInto(kotlindoc)
            this.@javadoc.copyInto(javadoc)
            this.@scaladoc.copyInto(scaladoc)
            this.@sourceHtml.copyInto(sourceHtml)
            this.@sourceXref.copyInto(sourceXref)
            copy
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

        Docs postMerge() {
            javadoc.postMerge()
            groovydoc.postMerge()
            kotlindoc.postMerge()
            scaladoc.postMerge()
            this
        }

        Docs normalize() {
            groovydoc.normalize()
            kotlindoc.normalize()
            scaladoc.normalize()
            guide.normalize()
            sourceHtml.normalize()
            sourceXref.normalize()
            this
        }

        List<String> validate() {
            List<String> errors = []
            errors.addAll(this.@kotlindoc.validate(config))
            errors
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

            map.putAll(jar.toMap())
            map.putAll(minpom.toMap())
            map.putAll(source.toMap())

            new LinkedHashMap<>('artifacts': map)
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

        void copyInto(Artifacts copy) {
            jar.copyInto(copy.jar)
            minpom.copyInto(copy.minpom)
            source.copyInto(copy.source)
        }

        Artifacts copyOf() {
            Artifacts copy = new Artifacts(config, project)
            this.@jar.copyInto(jar)
            this.@minpom.copyInto(minpom)
            this.@source.copyInto(source)
            copy
        }

        static Artifacts merge(Artifacts o1, Artifacts o2) {
            Jar.merge(o1.@jar, o2.@jar)
            Minpom.merge(o1.@minpom, o2.@minpom)
            Source.merge(o1.@source, o2.@source)
            o1
        }

        Artifacts normalize() {
            jar.normalize()
            source.normalize()
            this
        }
    }
}
