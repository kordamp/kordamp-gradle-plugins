/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kordamp.gradle.plugin.base

import groovy.transform.CompileDynamic
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
import org.kordamp.gradle.plugin.base.plugins.Clirr
import org.kordamp.gradle.plugin.base.plugins.Groovydoc
import org.kordamp.gradle.plugin.base.plugins.Jacoco
import org.kordamp.gradle.plugin.base.plugins.Javadoc
import org.kordamp.gradle.plugin.base.plugins.Kotlindoc
import org.kordamp.gradle.plugin.base.plugins.Licensing
import org.kordamp.gradle.plugin.base.plugins.Minpom
import org.kordamp.gradle.plugin.base.plugins.Plugin
import org.kordamp.gradle.plugin.base.plugins.Publishing
import org.kordamp.gradle.plugin.base.plugins.Scaladoc
import org.kordamp.gradle.plugin.base.plugins.Source
import org.kordamp.gradle.plugin.base.plugins.SourceHtml
import org.kordamp.gradle.plugin.base.plugins.SourceXref
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
    final Apidoc apidoc
    final Bom bom
    final Bintray bintray
    final BuildInfo buildInfo
    final BuildScan buildScan
    final Clirr clirr
    final Groovydoc groovydoc
    final Kotlindoc kotlindoc
    final Jacoco jacoco
    final Javadoc javadoc
    final Licensing licensing
    final Minpom minpom
    final Plugin plugin
    final Publishing publishing
    final Scaladoc scaladoc
    final Source source
    final SourceHtml sourceHtml
    final SourceXref sourceXref
    final Stats stats
    final Testing testing

    private boolean releaseSet

    private final List<Action<? super Project>> projectActions = []
    private final List<Action<? super Project>> rootProjectActions = []

    ProjectConfigurationExtension(Project project) {
        this.project = project
        info = new Information(project)
        apidoc = new Apidoc(project)
        bom = new Bom(project)
        bintray = new Bintray(project)
        buildInfo = new BuildInfo(project)
        buildScan = new BuildScan(project)
        clirr = new Clirr(project)
        groovydoc = new Groovydoc(project)
        kotlindoc = new Kotlindoc(project)
        jacoco = new Jacoco(project)
        javadoc = new Javadoc(project)
        licensing = new Licensing(project)
        minpom = new Minpom(project)
        plugin = new Plugin(project)
        publishing = new Publishing(project)
        scaladoc = new Scaladoc(project)
        source = new Source(project)
        sourceHtml = new SourceHtml(project)
        sourceXref = new SourceXref(project)
        stats = new Stats(project)
        testing = new Testing(project)
    }

    ProjectConfigurationExtension(ProjectConfigurationExtension other) {
        this.project = other.project
        this.info = other.info
        this.apidoc = other.apidoc
        this.bom = other.bom
        this.bintray = other.bintray
        this.buildInfo = other.buildInfo
        this.buildScan = other.buildScan
        this.clirr = other.clirr
        this.groovydoc = other.groovydoc
        this.kotlindoc = other.kotlindoc
        this.jacoco = other.jacoco
        this.javadoc = other.javadoc
        this.licensing = other.licensing
        this.minpom = other.minpom
        this.plugin = other.plugin
        this.publishing = other.publishing
        this.scaladoc = other.scaladoc
        this.source = other.source
        this.sourceHtml = other.sourceHtml
        this.sourceXref = other.sourceXref
        this.stats = other.stats
        this.testing = other.testing
        setRelease(other.release)
        projectActions.addAll(other.projectActions)
        rootProjectActions.addAll(other.rootProjectActions)
    }

    protected void ready() {
        projectActions.each { Action action -> action.execute(project) }
    }

    protected void rootReady() {
        rootProjectActions.each { Action action -> action.execute(project.rootProject) }
    }

    String toString() {
        toMap().toString()
    }

    @CompileDynamic
    Map<String, Object> toMap() {
        Map<String, Object> map = [release: release]

        map.putAll(info.toMap())
        map.putAll(apidoc.toMap())
        map.putAll(bom.toMap())
        map.putAll(bintray.toMap())
        map.putAll(buildInfo.toMap())
        map.putAll(buildScan.toMap())
        map.putAll(clirr.toMap())
        map.putAll(groovydoc.toMap())
        map.putAll(kotlindoc.toMap())
        map.putAll(jacoco.toMap())
        map.putAll(javadoc.toMap())
        map.putAll(licensing.toMap())
        map.putAll(minpom.toMap())
        map.putAll(plugin.toMap())
        map.putAll(publishing.toMap())
        map.putAll(scaladoc.toMap())
        map.putAll(source.toMap())
        map.putAll(sourceHtml.toMap())
        map.putAll(sourceXref.toMap())
        map.putAll(stats.toMap())
        map.putAll(testing.toMap())

        map
    }

    Information getInfo() {
        info
    }

    Apidoc getApidoc() {
        apidoc
    }

    Bom getBom() {
        bom
    }

    Bintray getBintray() {
        bintray
    }

    BuildInfo getBuildInfo() {
        buildInfo
    }

    BuildScan getBuildScan() {
        buildScan
    }

    Clirr getClirr() {
        clirr
    }

    Groovydoc getGroovydoc() {
        groovydoc
    }

    Kotlindoc getKotlindoc() {
        kotlindoc
    }

    Jacoco getJacoco() {
        jacoco
    }

    Javadoc getJavadoc() {
        javadoc
    }

    Licensing getLicensing() {
        licensing
    }

    /**
     * @Deprecated Use #getLicensing() instead
     */
    Licensing getLicense() {
        project.logger.warn("ProjectConfigurationExtension.getLicense() has been deprecated, use getLicensing() instead")
        licensing
    }

    Minpom getMinpom() {
        minpom
    }

    Plugin getPlugin() {
        plugin
    }

    Publishing getPublishing() {
        publishing
    }

    Scaladoc getScaladoc() {
        scaladoc
    }

    Source getSource() {
        source
    }

    SourceHtml getSourceHtml() {
        sourceHtml
    }

    SourceXref getSourceXref() {
        sourceXref
    }

    Stats getStats() {
        stats
    }

    /**
     * @Deprecated Use #getTesting() instead
     */
    Testing getTest() {
        project.logger.warn("ProjectConfigurationExtension.getTest() has been deprecated, use getTesting() instead")
        testing
    }

    Testing getTesting() {
        testing
    }

    ProjectConfigurationExtension whenProjectReady(Action<? super Project> action) {
        projectActions.add(action)
        this
    }

    ProjectConfigurationExtension whenRootProjectReady(Action<? super Project> action) {
        rootProjectActions.add(action)
        this
    }

    void info(Action<? super Information> action) {
        action.execute(info)
    }

    void info(@DelegatesTo(Information) Closure action) {
        ConfigureUtil.configure(action, info)
    }

    void apidoc(Action<? super Apidoc> action) {
        action.execute(apidoc)
    }

    void apidoc(@DelegatesTo(Apidoc) Closure action) {
        ConfigureUtil.configure(action, apidoc)
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

    void buildScan(Action<? super BuildScan> action) {
        action.execute(buildScan)
    }

    void buildScan(@DelegatesTo(BuildScan) Closure action) {
        ConfigureUtil.configure(action, buildScan)
    }

    void clirr(Action<? super Clirr> action) {
        action.execute(clirr)
    }

    void clirr(@DelegatesTo(Clirr) Closure action) {
        ConfigureUtil.configure(action, clirr)
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

    void jacoco(Action<? super Jacoco> action) {
        action.execute(jacoco)
    }

    void jacoco(@DelegatesTo(Jacoco) Closure action) {
        ConfigureUtil.configure(action, jacoco)
    }

    void javadoc(Action<? super Javadoc> action) {
        action.execute(javadoc)
    }

    void javadoc(@DelegatesTo(Javadoc) Closure action) {
        ConfigureUtil.configure(action, javadoc)
    }

    void licensing(Action<? super Licensing> action) {
        action.execute(licensing)
    }

    void licensing(@DelegatesTo(Licensing) Closure action) {
        ConfigureUtil.configure(action, licensing)
    }

    /**
     * @Deprecated Use #licensing() instead
     */
    void license(Action<? super Licensing> action) {
        project.logger.warn("ProjectConfigurationExtension.license() has been deprecated, use licensing() instead")
        action.execute(licensing)
    }

    /**
     * @Deprecated Use #licensing() instead
     */
    void license(@DelegatesTo(Licensing) Closure action) {
        project.logger.warn("ProjectConfigurationExtension.license() has been deprecated, use licensing() instead")
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

    void scaladoc(Action<? super Scaladoc> action) {
        action.execute(scaladoc)
    }

    void scaladoc(@DelegatesTo(Scaladoc) Closure action) {
        ConfigureUtil.configure(action, scaladoc)
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

    /**
     * @Deprecated Use #testing() instead
     */
    void test(Action<? super Testing> action) {
        project.logger.warn("ProjectConfigurationExtension.test() has been deprecated, use testing() instead")
        action.execute(testing)
    }

    /**
     * @Deprecated Use #testing() instead
     */
    void test(@DelegatesTo(Testing) Closure action) {
        project.logger.warn("ProjectConfigurationExtension.test() has been deprecated, use testing() instead")
        ConfigureUtil.configure(action, testing)
    }

    void testing(Action<? super Testing> action) {
        action.execute(testing)
    }

    void testing(@DelegatesTo(Testing) Closure action) {
        ConfigureUtil.configure(action, testing)
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
        this.@apidoc.copyInto(copy.@apidoc)
        this.@bom.copyInto(copy.@bom)
        this.@bintray.copyInto(copy.@bintray)
        this.@buildInfo.copyInto(copy.@buildInfo)
        this.@buildScan.copyInto(copy.@buildScan)
        this.@clirr.copyInto(copy.@clirr)
        this.@groovydoc.copyInto(copy.@groovydoc)
        this.@kotlindoc.copyInto(copy.@kotlindoc)
        this.@jacoco.copyInto(copy.@jacoco)
        this.@javadoc.copyInto(copy.@javadoc)
        this.@licensing.copyInto(copy.@licensing)
        this.@minpom.copyInto(copy.@minpom)
        this.@plugin.copyInto(copy.@plugin)
        this.@publishing.copyInto(copy.@publishing)
        this.@scaladoc.copyInto(copy.@scaladoc)
        this.@source.copyInto(copy.@source)
        this.@sourceHtml.copyInto(copy.@sourceHtml)
        this.@sourceXref.copyInto(copy.@sourceXref)
        this.@stats.copyInto(copy.@stats)
        this.@testing.copyInto(copy.@testing)
        copy.projectActions.addAll(this.projectActions)
        copy.rootProjectActions.addAll(this.rootProjectActions)

        copy
    }

    ProjectConfigurationExtension merge(ProjectConfigurationExtension other) {
        ProjectConfigurationExtension merged = copyOf()
        merged.setRelease((boolean) (merged.@releaseSet ? merged.@release : other.@release))
        Information.merge(merged.@info, other.@info)
        Apidoc.merge(merged.@apidoc, other.@apidoc)
        Bom.merge(merged.@bom, other.@bom)
        Bintray.merge(merged.@bintray, other.@bintray)
        BuildInfo.merge(merged.@buildInfo, other.@buildInfo)
        BuildScan.merge(merged.@buildScan, other.@buildScan)
        Clirr.merge(merged.@clirr, other.@clirr)
        Groovydoc.merge(merged.@groovydoc, other.@groovydoc)
        Kotlindoc.merge(merged.@kotlindoc, other.@kotlindoc)
        Jacoco.merge(merged.@jacoco, other.@jacoco)
        Javadoc.merge(merged.@javadoc, other.@javadoc)
        Licensing.merge(merged.@licensing, other.@licensing)
        Minpom.merge(merged.@minpom, other.@minpom)
        Plugin.merge(merged.@plugin, other.@plugin)
        Publishing.merge(merged.@publishing, other.@publishing)
        Scaladoc.merge(merged.@scaladoc, other.@scaladoc)
        Source.merge(merged.@source, other.@source)
        SourceHtml.merge(merged.@sourceHtml, other.@sourceHtml)
        SourceXref.merge(merged.@sourceXref, other.@sourceXref)
        Stats.merge(merged.@stats, other.@stats)
        Testing.merge(merged.@testing, other.@testing)

        merged
    }

    List<String> validate() {
        List<String> errors = []

        errors.addAll(this.@info.validate(this))
        errors.addAll(this.@bom.validate(this))
        errors.addAll(this.@bintray.validate(this))
        errors.addAll(this.@licensing.validate(this))
        errors.addAll(this.@kotlindoc.validate(this))
        errors.addAll(this.@plugin.validate(this))

        errors
    }

    void normalize() {
        info.normalize()
        groovydoc.normalize()
        kotlindoc.normalize()
        licensing.normalize()
        plugin.normalize()
        publishing.normalize()
        scaladoc.normalize()
    }
}
