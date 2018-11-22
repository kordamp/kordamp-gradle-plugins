/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 Andres Almiray.
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
import org.kordamp.gradle.plugin.base.model.mutable.MutableInformation
import org.kordamp.gradle.plugin.base.plugins.Apidoc
import org.kordamp.gradle.plugin.base.plugins.Bintray
import org.kordamp.gradle.plugin.base.plugins.Bom
import org.kordamp.gradle.plugin.base.plugins.BuildInfo
import org.kordamp.gradle.plugin.base.plugins.Groovydoc
import org.kordamp.gradle.plugin.base.plugins.Jacoco
import org.kordamp.gradle.plugin.base.plugins.Javadoc
import org.kordamp.gradle.plugin.base.plugins.Kotlindoc
import org.kordamp.gradle.plugin.base.plugins.License
import org.kordamp.gradle.plugin.base.plugins.Minpom
import org.kordamp.gradle.plugin.base.plugins.Publishing
import org.kordamp.gradle.plugin.base.plugins.Source
import org.kordamp.gradle.plugin.base.plugins.SourceHtml
import org.kordamp.gradle.plugin.base.plugins.SourceXref
import org.kordamp.gradle.plugin.base.plugins.Stats
import org.kordamp.gradle.plugin.base.plugins.mutable.MutableApidoc
import org.kordamp.gradle.plugin.base.plugins.mutable.MutableBintray
import org.kordamp.gradle.plugin.base.plugins.mutable.MutableBom
import org.kordamp.gradle.plugin.base.plugins.mutable.MutableBuildInfo
import org.kordamp.gradle.plugin.base.plugins.mutable.MutableGroovydoc
import org.kordamp.gradle.plugin.base.plugins.mutable.MutableJacoco
import org.kordamp.gradle.plugin.base.plugins.mutable.MutableJavadoc
import org.kordamp.gradle.plugin.base.plugins.mutable.MutableKotlindoc
import org.kordamp.gradle.plugin.base.plugins.mutable.MutableLicense
import org.kordamp.gradle.plugin.base.plugins.mutable.MutableMinpom
import org.kordamp.gradle.plugin.base.plugins.mutable.MutablePublishing
import org.kordamp.gradle.plugin.base.plugins.mutable.MutableSource
import org.kordamp.gradle.plugin.base.plugins.mutable.MutableSourceHtml
import org.kordamp.gradle.plugin.base.plugins.mutable.MutableSourceXref
import org.kordamp.gradle.plugin.base.plugins.mutable.MutableStats

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class ProjectConfigurationExtension {
    boolean release = false

    final Project project
    final MutableInformation info
    final MutableApidoc apidoc
    final MutableBom bom
    final MutableBintray bintray
    final MutableBuildInfo buildInfo
    final MutableGroovydoc groovydoc
    final MutableKotlindoc kotlindoc
    final MutableJacoco jacoco
    final MutableJavadoc javadoc
    final MutableLicense license
    final MutableMinpom minpom
    final MutablePublishing publishing
    final MutableSource source
    final MutableSourceHtml sourceHtml
    final MutableSourceXref sourceXref
    final MutableStats stats

    private boolean releaseSet

    ProjectConfigurationExtension(Project project) {
        this.project = project
        info = new MutableInformation(project)
        apidoc = new MutableApidoc(project)
        bom = new MutableBom(project)
        bintray = new MutableBintray(project)
        buildInfo = new MutableBuildInfo(project)
        groovydoc = new MutableGroovydoc(project)
        kotlindoc = new MutableKotlindoc(project)
        jacoco = new MutableJacoco(project)
        javadoc = new MutableJavadoc(project)
        license = new MutableLicense(project)
        minpom = new MutableMinpom(project)
        publishing = new MutablePublishing(project)
        source = new MutableSource(project)
        sourceHtml = new MutableSourceHtml(project)
        sourceXref = new MutableSourceXref(project)
        stats = new MutableStats(project)
    }

    void init() {
        bom.init()
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
        map.putAll(groovydoc.toMap())
        map.putAll(kotlindoc.toMap())
        map.putAll(jacoco.toMap())
        map.putAll(javadoc.toMap())
        map.putAll(license.toMap())
        map.putAll(minpom.toMap())
        map.putAll(publishing.toMap())
        map.putAll(source.toMap())
        map.putAll(sourceHtml.toMap())
        map.putAll(sourceXref.toMap())
        map.putAll(stats.toMap())

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

    License getLicense() {
        license
    }

    Minpom getMinpom() {
        minpom
    }

    Publishing getPublishing() {
        publishing
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

    void info(Action<? super MutableInformation> action) {
        action.execute(info)
    }

    void info(@DelegatesTo(MutableInformation) Closure action) {
        ConfigureUtil.configure(action, info)
    }

    void apidoc(Action<? super MutableApidoc> action) {
        action.execute(apidoc)
    }

    void apidoc(@DelegatesTo(MutableApidoc) Closure action) {
        ConfigureUtil.configure(action, apidoc)
    }

    void bom(Action<? super MutableBom> action) {
        action.execute(bom)
    }

    void bom(@DelegatesTo(MutableBom) Closure action) {
        ConfigureUtil.configure(action, bom)
    }

    void bintray(Action<? super MutableBintray> action) {
        action.execute(bintray)
    }

    void bintray(@DelegatesTo(MutableBintray) Closure action) {
        ConfigureUtil.configure(action, bintray)
    }

    void buildInfo(Action<? super MutableBuildInfo> action) {
        action.execute(buildInfo)
    }

    void buildInfo(@DelegatesTo(MutableBuildInfo) Closure action) {
        ConfigureUtil.configure(action, buildInfo)
    }

    void groovydoc(Action<? super MutableGroovydoc> action) {
        action.execute(groovydoc)
    }

    void groovydoc(@DelegatesTo(MutableGroovydoc) Closure action) {
        ConfigureUtil.configure(action, groovydoc)
    }

    void kotlindoc(Action<? super MutableKotlindoc> action) {
        action.execute(kotlindoc)
    }

    void kotlindoc(@DelegatesTo(MutableKotlindoc) Closure action) {
        ConfigureUtil.configure(action, kotlindoc)
    }

    void jacoco(Action<? super MutableJacoco> action) {
        action.execute(jacoco)
    }

    void jacoco(@DelegatesTo(MutableJacoco) Closure action) {
        ConfigureUtil.configure(action, jacoco)
    }

    void javadoc(Action<? super MutableJavadoc> action) {
        action.execute(javadoc)
    }

    void javadoc(@DelegatesTo(MutableJavadoc) Closure action) {
        ConfigureUtil.configure(action, javadoc)
    }

    void license(Action<? super MutableLicense> action) {
        action.execute(license)
    }

    void license(@DelegatesTo(MutableLicense) Closure action) {
        ConfigureUtil.configure(action, license)
    }

    void minpom(Action<? super Minpom> action) {
        action.execute(minpom)
    }

    void minpom(@DelegatesTo(Minpom) Closure action) {
        ConfigureUtil.configure(action, minpom)
    }

    void publishing(Action<? super MutablePublishing> action) {
        action.execute(publishing)
    }

    void publishing(@DelegatesTo(MutablePublishing) Closure action) {
        ConfigureUtil.configure(action, publishing)
    }

    void source(Action<? super MutableSource> action) {
        action.execute(source)
    }

    void source(@DelegatesTo(MutableSource) Closure action) {
        ConfigureUtil.configure(action, source)
    }

    void stats(Action<? super MutableStats> action) {
        action.execute(stats)
    }

    void stats(@DelegatesTo(MutableStats) Closure action) {
        ConfigureUtil.configure(action, stats)
    }

    void sourceHtml(Action<? super MutableSourceHtml> action) {
        action.execute(sourceHtml)
    }

    void sourceHtml(@DelegatesTo(MutableSourceHtml) Closure action) {
        ConfigureUtil.configure(action, sourceHtml)
    }

    void sourceXref(Action<? super MutableSourceXref> action) {
        action.execute(sourceXref)
    }

    void sourceXref(@DelegatesTo(MutableSourceXref) Closure action) {
        ConfigureUtil.configure(action, sourceXref)
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
        this.@groovydoc.copyInto(copy.@groovydoc)
        this.@kotlindoc.copyInto(copy.@kotlindoc)
        this.@jacoco.copyInto(copy.@jacoco)
        this.@javadoc.copyInto(copy.@javadoc)
        this.@license.copyInto(copy.@license)
        this.@minpom.copyInto(copy.@minpom)
        this.@publishing.copyInto(copy.@publishing)
        this.@source.copyInto(copy.@source)
        this.@sourceHtml.copyInto(copy.@sourceHtml)
        this.@sourceXref.copyInto(copy.@sourceXref)
        this.@stats.copyInto(copy.@stats)

        copy
    }

    ProjectConfigurationExtension merge(ProjectConfigurationExtension other) {
        ProjectConfigurationExtension merged = copyOf()
        merged.setRelease((boolean) (merged.@releaseSet ? merged.@release : other.@release))
        MutableInformation.merge(merged.@info, other.@info)
        MutableApidoc.merge(merged.@apidoc, other.@apidoc)
        MutableBom.merge(merged.@bom, other.@bom)
        MutableBintray.merge(merged.@bintray, other.@bintray)
        MutableBuildInfo.merge(merged.@buildInfo, other.@buildInfo)
        MutableGroovydoc.merge(merged.@groovydoc, other.@groovydoc)
        MutableKotlindoc.merge(merged.@kotlindoc, other.@kotlindoc)
        MutableJacoco.merge(merged.@jacoco, other.@jacoco)
        MutableJavadoc.merge(merged.@javadoc, other.@javadoc)
        MutableLicense.merge(merged.@license, other.@license)
        MutableMinpom.merge(merged.@minpom, other.@minpom)
        MutablePublishing.merge(merged.@publishing, other.@publishing)
        MutableSource.merge(merged.@source, other.@source)
        MutableSourceHtml.merge(merged.@sourceHtml, other.@sourceHtml)
        MutableSourceXref.merge(merged.@sourceXref, other.@sourceXref)
        MutableStats.merge(merged.@stats, other.@stats)

        merged
    }

    List<String> validate() {
        List<String> errors = []

        errors.addAll(this.@info.validate())
        errors.addAll(this.@bom.validate(this.@info))
        errors.addAll(this.@bintray.validate(this.@info))
        errors.addAll(this.@license.validate(this.@info))
        errors.addAll(this.@kotlindoc.validate())

        errors
    }

    void normalize() {
        info.normalize()
        bintray.normalize()
        groovydoc.normalize()
        kotlindoc.normalize()
    }
}
