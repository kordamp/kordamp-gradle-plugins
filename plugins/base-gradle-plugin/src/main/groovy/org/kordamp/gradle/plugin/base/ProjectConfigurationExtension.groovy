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
import org.kordamp.gradle.plugin.base.plugins.Apidoc
import org.kordamp.gradle.plugin.base.plugins.Bintray
import org.kordamp.gradle.plugin.base.plugins.Bom
import org.kordamp.gradle.plugin.base.plugins.BuildInfo
import org.kordamp.gradle.plugin.base.plugins.BuildScan
import org.kordamp.gradle.plugin.base.plugins.Checkstyle
import org.kordamp.gradle.plugin.base.plugins.Clirr
import org.kordamp.gradle.plugin.base.plugins.Codenarc
import org.kordamp.gradle.plugin.base.plugins.Coveralls
import org.kordamp.gradle.plugin.base.plugins.Detekt
import org.kordamp.gradle.plugin.base.plugins.Groovydoc
import org.kordamp.gradle.plugin.base.plugins.Guide
import org.kordamp.gradle.plugin.base.plugins.Jacoco
import org.kordamp.gradle.plugin.base.plugins.Javadoc
import org.kordamp.gradle.plugin.base.plugins.Kotlindoc
import org.kordamp.gradle.plugin.base.plugins.Licensing
import org.kordamp.gradle.plugin.base.plugins.Minpom
import org.kordamp.gradle.plugin.base.plugins.Plugin
import org.kordamp.gradle.plugin.base.plugins.Pmd
import org.kordamp.gradle.plugin.base.plugins.Publishing
import org.kordamp.gradle.plugin.base.plugins.Scaladoc
import org.kordamp.gradle.plugin.base.plugins.Source
import org.kordamp.gradle.plugin.base.plugins.SourceHtml
import org.kordamp.gradle.plugin.base.plugins.SourceXref
import org.kordamp.gradle.plugin.base.plugins.Spotbugs
import org.kordamp.gradle.plugin.base.plugins.Stats
import org.kordamp.gradle.plugin.base.plugins.Testing
import org.kordamp.gradle.plugin.base.plugins.ErrorProne

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
    final BuildScan buildScan
    final Clirr clirr
    final Licensing licensing
    final Minpom minpom
    final Plugin plugin
    final Publishing publishing
    final Source source
    final Stats stats
    final Testing testing
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
        buildScan = new BuildScan(this, project)
        clirr = new Clirr(this, project)
        licensing = new Licensing(this, project)
        minpom = new Minpom(this, project)
        plugin = new Plugin(this, project)
        publishing = new Publishing(this, project)
        source = new Source(this, project)
        stats = new Stats(this, project)
        testing = new Testing(this, project)

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
        other.@buildScan.copyInto(buildScan)
        other.clirr.copyInto(clirr)
        other.licensing.copyInto(licensing)
        other.minpom.copyInto(minpom)
        other.plugin.copyInto(plugin)
        other.publishing.copyInto(publishing)
        other.source.copyInto(source)
        other.stats.copyInto(stats)
        other.testing.copyInto(testing)
        other.docs.copyInto(docs)
        other.coverage.copyInto(coverage)
        other.quality.copyInto(quality)
    }

    Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(release: release)

        map.putAll(info.toMap())
        map.putAll(buildInfo.toMap())
        map.putAll(minpom.toMap())
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
        map.putAll(source.toMap())
        map.putAll(stats.toMap())

        map
    }

    @Deprecated
    Apidoc getApidoc() {
        println("The method config.apidoc is deprecated and will be removed in the future. Use config.docs.apidoc instead")
        docs.apidoc
    }

    @Deprecated
    BuildScan getBuildScan() {
        buildScan
    }

    @Deprecated
    Groovydoc getGroovydoc() {
        println("The method config.groovydoc is deprecated and will be removed in the future. Use config.docs.groovydoc instead")
        docs.groovydoc
    }

    @Deprecated
    Kotlindoc getKotlindoc() {
        println("The method config.apidoc is deprecated and will be removed in the future. Use config.docs.apidoc instead")
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

    void info(Action<? super Information> action) {
        action.execute(info)
    }

    void info(@DelegatesTo(Information) Closure action) {
        ConfigureUtil.configure(action, info)
    }

    @Deprecated
    void apidoc(Action<? super Apidoc> action) {
        println("The method config.apidoc() is deprecated and will be removed in the future. Use config.docs.apidoc() instead")
        docs.apidoc(action)
    }

    @Deprecated
    void apidoc(@DelegatesTo(Apidoc) Closure action) {
        println("The method config.apidoc() is deprecated and will be removed in the future. Use config.docs.apidoc() instead")
        docs.apidoc(action)
    }

    void bom(Action<? super Bom> action) {
        action.execute(bom)
    }

    void bom(@DelegatesTo(Bom) Closure action) {
        ConfigureUtil.configure(action, bom)
    }

    void bintray(Action<? super Bintray> action) {
        action.execute(bintray)
    }

    void bintray(@DelegatesTo(Bintray) Closure action) {
        ConfigureUtil.configure(action, bintray)
    }

    void buildInfo(Action<? super BuildInfo> action) {
        action.execute(buildInfo)
    }

    void buildInfo(@DelegatesTo(BuildInfo) Closure action) {
        ConfigureUtil.configure(action, buildInfo)
    }

    @Deprecated
    void buildScan(Action<? super BuildScan> action) {
        action.execute(buildScan)
    }

    @Deprecated
    void buildScan(@DelegatesTo(BuildScan) Closure action) {
        ConfigureUtil.configure(action, buildScan)
    }

    void clirr(Action<? super Clirr> action) {
        action.execute(clirr)
    }

    void clirr(@DelegatesTo(Clirr) Closure action) {
        ConfigureUtil.configure(action, clirr)
    }

    @Deprecated
    void groovydoc(Action<? super Groovydoc> action) {
        println("The method config.groovydoc() is deprecated and will be removed in the future. Use config.docs.groovydoc() instead")
        docs.groovydoc(action)
    }

    @Deprecated
    void groovydoc(@DelegatesTo(Groovydoc) Closure action) {
        println("The method config.groovydoc() is deprecated and will be removed in the future. Use config.docs.groovydoc() instead")
        docs.groovydoc(action)
    }

    @Deprecated
    void kotlindoc(Action<? super Kotlindoc> action) {
        println("The method config.kotlindoc() is deprecated and will be removed in the future. Use config.docs.kotlindoc() instead")
        docs.kotlindoc(action)
    }

    @Deprecated
    void kotlindoc(@DelegatesTo(Kotlindoc) Closure action) {
        println("The method config.kotlindoc() is deprecated and will be removed in the future. Use config.docs.kotlindoc() instead")
        docs.kotlindoc(action)
    }

    @Deprecated
    void jacoco(Action<? super Jacoco> action) {
        println("The method config.jacoco() is deprecated and will be removed in the future. Use config.coverage.jacoco() instead")
        coverage.jacoco(action)
    }

    @Deprecated
    void jacoco(@DelegatesTo(Jacoco) Closure action) {
        println("The method config.jacoco() is deprecated and will be removed in the future. Use config.coverage.jacoco() instead")
        coverage.jacoco(action)
    }

    @Deprecated
    void javadoc(Action<? super Javadoc> action) {
        println("The method config.javadoc() is deprecated and will be removed in the future. Use config.docs.javadoc() instead")
        docs.javadoc(action)
    }

    @Deprecated
    void javadoc(@DelegatesTo(Javadoc) Closure action) {
        println("The method config.javadoc() is deprecated and will be removed in the future. Use config.docs.javadoc() instead")
        docs.javadoc(action)
    }

    void licensing(Action<? super Licensing> action) {
        action.execute(licensing)
    }

    void licensing(@DelegatesTo(Licensing) Closure action) {
        ConfigureUtil.configure(action, licensing)
    }

    void minpom(Action<? super Minpom> action) {
        action.execute(minpom)
    }

    void minpom(@DelegatesTo(Minpom) Closure action) {
        ConfigureUtil.configure(action, minpom)
    }

    void plugin(Action<? super Plugin> action) {
        action.execute(plugin)
    }

    void plugin(@DelegatesTo(Plugin) Closure action) {
        ConfigureUtil.configure(action, plugin)
    }

    void publishing(Action<? super Publishing> action) {
        action.execute(publishing)
    }

    void publishing(@DelegatesTo(Publishing) Closure action) {
        ConfigureUtil.configure(action, publishing)
    }

    @Deprecated
    void scaladoc(Action<? super Scaladoc> action) {
        println("The method config.scaladoc() is deprecated and will be removed in the future. Use config.docs.scaladoc() instead")
        docs.scaladoc(action)
    }

    @Deprecated
    void scaladoc(@DelegatesTo(Scaladoc) Closure action) {
        println("The method config.scaladoc() is deprecated and will be removed in the future. Use config.docs.scaladoc() instead")
        docs.scaladoc(action)
    }

    void source(Action<? super Source> action) {
        action.execute(source)
    }

    void source(@DelegatesTo(Source) Closure action) {
        ConfigureUtil.configure(action, source)
    }

    void stats(Action<? super Stats> action) {
        action.execute(stats)
    }

    void stats(@DelegatesTo(Stats) Closure action) {
        ConfigureUtil.configure(action, stats)
    }

    @Deprecated
    void sourceHtml(Action<? super SourceHtml> action) {
        println("The method config.sourceHtml() is deprecated and will be removed in the future. Use config.docs.sourceHtml() instead")
        docs.sourceHtml(action)
    }

    @Deprecated
    void sourceHtml(@DelegatesTo(SourceHtml) Closure action) {
        println("The method config.sourceHtml() is deprecated and will be removed in the future. Use config.docs.sourceHtml() instead")
        docs.sourceHtml(action)
    }

    @Deprecated
    void sourceXref(Action<? super SourceXref> action) {
        println("The method config.sourceXref() is deprecated and will be removed in the future. Use config.docs.sourceXref() instead")
        docs.sourceXref(action)
    }

    @Deprecated
    void sourceXref(@DelegatesTo(SourceXref) Closure action) {
        println("The method config.sourceXref() is deprecated and will be removed in the future. Use config.docs.sourceXref() instead")
        docs.sourceXref(action)
    }

    void testing(Action<? super Testing> action) {
        action.execute(testing)
    }

    void testing(@DelegatesTo(Testing) Closure action) {
        ConfigureUtil.configure(action, testing)
    }

    void docs(Action<? super Docs> action) {
        action.execute(docs)
    }

    void docs(@DelegatesTo(Docs) Closure action) {
        ConfigureUtil.configure(action, docs)
    }

    void coverage(Action<? super Coverage> action) {
        action.execute(coverage)
    }

    void coverage(@DelegatesTo(Coverage) Closure action) {
        ConfigureUtil.configure(action, coverage)
    }

    void quality(Action<? super Quality> action) {
        action.execute(quality)
    }

    void quality(@DelegatesTo(Quality) Closure action) {
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
        this.@buildScan.copyInto(copy.@buildScan)
        this.@clirr.copyInto(copy.@clirr)
        this.@licensing.copyInto(copy.@licensing)
        this.@minpom.copyInto(copy.@minpom)
        this.@plugin.copyInto(copy.@plugin)
        this.@publishing.copyInto(copy.@publishing)
        this.@source.copyInto(copy.@source)
        this.@stats.copyInto(copy.@stats)
        this.@testing.copyInto(copy.@testing)
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
        BuildScan.merge(copy.@buildScan, other.@buildScan)
        Clirr.merge(copy.@clirr, other.@clirr)
        Licensing.merge(copy.@licensing, other.@licensing)
        Minpom.merge(copy.@minpom, other.@minpom)
        Plugin.merge(copy.@plugin, other.@plugin)
        Publishing.merge(copy.@publishing, other.@publishing)
        Source.merge(copy.@source, other.@source)
        Stats.merge(copy.@stats, other.@stats)
        Testing.merge(copy.@testing, other.@testing)
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

        errors
    }

    ProjectConfigurationExtension normalize() {
        info.normalize()
        bom.normalize()
        buildScan.normalize()
        clirr.normalize()
        licensing.normalize()
        plugin.normalize()
        publishing.normalize()
        docs.normalize()
        coverage.normalize()
        quality.normalize()
        this
    }

    ProjectConfigurationExtension postMerge() {
        docs.postMerge()
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
        final Spotbugs spotbugs

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
            spotbugs = new Spotbugs(config, project)
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<String, Object>()

            map.putAll(checkstyle.toMap())
            map.putAll(codenarc.toMap())
            map.putAll(detekt.toMap())
            map.putAll(errorprone.toMap())
            map.putAll(pmd.toMap())
            map.putAll(spotbugs.toMap())

            new LinkedHashMap<>('quality': map)
        }

        void copyInto(Quality copy) {
            checkstyle.copyInto(copy.checkstyle)
            codenarc.copyInto(copy.codenarc)
            detekt.copyInto(copy.detekt)
            errorprone.copyInto(copy.errorprone)
            pmd.copyInto(copy.pmd)
            spotbugs.copyInto(copy.spotbugs)
        }

        void checkstyle(Action<? super Checkstyle> action) {
            action.execute(checkstyle)
        }

        void checkstyle(@DelegatesTo(Checkstyle) Closure action) {
            ConfigureUtil.configure(action, checkstyle)
        }

        void codenarc(Action<? super Codenarc> action) {
            action.execute(codenarc)
        }

        void codenarc(@DelegatesTo(Codenarc) Closure action) {
            ConfigureUtil.configure(action, codenarc)
        }

        void detekt(Action<? super Detekt> action) {
            action.execute(detekt)
        }

        void detekt(@DelegatesTo(Detekt) Closure action) {
            ConfigureUtil.configure(action, detekt)
        }

        void errorprone(Action<? super ErrorProne> action) {
            action.execute(errorprone)
        }

        void errorprone(@DelegatesTo(ErrorProne) Closure action) {
            ConfigureUtil.configure(action, errorprone)
        }

        void pmd(Action<? super Pmd> action) {
            action.execute(pmd)
        }

        void pmd(@DelegatesTo(Pmd) Closure action) {
            ConfigureUtil.configure(action, pmd)
        }

        void spotbugs(Action<? super Spotbugs> action) {
            action.execute(spotbugs)
        }

        void spotbugs(@DelegatesTo(Spotbugs) Closure action) {
            ConfigureUtil.configure(action, spotbugs)
        }

        Quality copyOf() {
            Quality copy = new Quality(config, project)
            this.@checkstyle.copyInto(copy.@checkstyle)
            this.@codenarc.copyInto(copy.@codenarc)
            this.@detekt.copyInto(copy.@detekt)
            this.@errorprone.copyInto(copy.@detekt)
            this.@pmd.copyInto(copy.@pmd)
            this.@spotbugs.copyInto(copy.@spotbugs)
            copy
        }

        static Quality merge(Quality o1, Quality o2) {
            Checkstyle.merge(o1.@checkstyle, o2.@checkstyle)
            Codenarc.merge(o1.@codenarc, o2.@codenarc)
            Detekt.merge(o1.@detekt, o2.@detekt)
            ErrorProne.merge(o1.@errorprone, o2.@errorprone)
            Pmd.merge(o1.@pmd, o2.@pmd)
            Spotbugs.merge(o1.@spotbugs, o2.@spotbugs)

            o1
        }

        Quality postMerge() {
            this
        }

        Quality normalize() {
            checkstyle.normalize()
            codenarc.normalize()
            detekt.normalize()
            errorprone.normalize()
            pmd.normalize()
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

        void coveralls(@DelegatesTo(Coveralls) Closure action) {
            ConfigureUtil.configure(action, coveralls)
        }

        void jacoco(Action<? super Jacoco> action) {
            action.execute(jacoco)
        }

        void jacoco(@DelegatesTo(Jacoco) Closure action) {
            ConfigureUtil.configure(action, jacoco)
        }

        void copyInto(Coverage copy) {
            coveralls.copyInto(copy.coveralls)
            jacoco.copyInto(jacoco)
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

        Coverage normalize() {
            jacoco.normalize()
            this
        }
    }

    @CompileStatic
    static class Docs {
        final Apidoc apidoc
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
            apidoc = new Apidoc(config, project)
            groovydoc = new Groovydoc(config, project)
            kotlindoc = new Kotlindoc(config, project)
            javadoc = new Javadoc(config, project)
            scaladoc = new Scaladoc(config, project)
            sourceHtml = new SourceHtml(config, project)
            sourceXref = new SourceXref(config, project)
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<String, Object>()

            map.putAll(apidoc.toMap())
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

        void guide(@DelegatesTo(Guide) Closure action) {
            ConfigureUtil.configure(action, guide)
        }

        void apidoc(Action<? super Apidoc> action) {
            action.execute(apidoc)
        }

        void apidoc(@DelegatesTo(Apidoc) Closure action) {
            ConfigureUtil.configure(action, apidoc)
        }

        void groovydoc(Action<? super Groovydoc> action) {
            action.execute(groovydoc)
        }

        void groovydoc(@DelegatesTo(Groovydoc) Closure action) {
            ConfigureUtil.configure(action, groovydoc)
        }

        void kotlindoc(Action<? super Kotlindoc> action) {
            action.execute(kotlindoc)
        }

        void kotlindoc(@DelegatesTo(Kotlindoc) Closure action) {
            ConfigureUtil.configure(action, kotlindoc)
        }

        void javadoc(Action<? super Javadoc> action) {
            action.execute(javadoc)
        }

        void javadoc(@DelegatesTo(Javadoc) Closure action) {
            ConfigureUtil.configure(action, javadoc)
        }

        void scaladoc(Action<? super Scaladoc> action) {
            action.execute(scaladoc)
        }

        void scaladoc(@DelegatesTo(Scaladoc) Closure action) {
            ConfigureUtil.configure(action, scaladoc)
        }

        void sourceHtml(Action<? super SourceHtml> action) {
            action.execute(sourceHtml)
        }

        void sourceHtml(@DelegatesTo(SourceHtml) Closure action) {
            ConfigureUtil.configure(action, sourceHtml)
        }

        void sourceXref(Action<? super SourceXref> action) {
            action.execute(sourceXref)
        }

        void sourceXref(@DelegatesTo(SourceXref) Closure action) {
            ConfigureUtil.configure(action, sourceXref)
        }

        void copyInto(Docs copy) {
            guide.copyInto(copy.guide)
            apidoc.copyInto(copy.apidoc)
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
            this.@apidoc.copyInto(apidoc)
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
            Apidoc.merge(o1.@apidoc, o2.@apidoc)
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
            kotlindoc.postMerge()
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
}
