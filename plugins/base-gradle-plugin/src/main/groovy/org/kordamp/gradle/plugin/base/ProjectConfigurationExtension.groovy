/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 the original author or authors.
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

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.plugin.base.model.Information
import org.kordamp.gradle.plugin.base.plugins.Apidoc
import org.kordamp.gradle.plugin.base.plugins.Bintray
import org.kordamp.gradle.plugin.base.plugins.Groovydoc
import org.kordamp.gradle.plugin.base.plugins.Jacoco
import org.kordamp.gradle.plugin.base.plugins.Javadoc
import org.kordamp.gradle.plugin.base.plugins.License
import org.kordamp.gradle.plugin.base.plugins.Minpom
import org.kordamp.gradle.plugin.base.plugins.Publishing
import org.kordamp.gradle.plugin.base.plugins.Source
import org.kordamp.gradle.plugin.base.plugins.SourceHtml
import org.kordamp.gradle.plugin.base.plugins.Stats

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class ProjectConfigurationExtension {
    boolean release = false

    final Project project
    final Information info
    final Apidoc apidoc
    final Bintray bintray
    final Groovydoc groovydoc
    final Jacoco jacoco
    final Javadoc javadoc
    final License license
    final Minpom minpom
    final Publishing publishing
    final Source source
    final Stats stats
    final SourceHtml sourceHtml

    private boolean releaseSet

    ProjectConfigurationExtension(Project project) {
        this.project = project
        info = new Information(project)
        apidoc = new Apidoc(project)
        bintray = new Bintray(project)
        groovydoc = new Groovydoc(project)
        jacoco = new Jacoco(project)
        javadoc = new Javadoc(project)
        license = new License(project)
        minpom = new Minpom(project)
        publishing = new Publishing(project)
        source = new Source(project)
        stats = new Stats(project)
        sourceHtml = new SourceHtml(project)
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

    void bintray(Action<? super Bintray> action) {
        action.execute(bintray)
    }

    void bintray(@DelegatesTo(Bintray) Closure action) {
        ConfigureUtil.configure(action, bintray)
    }

    void groovydoc(Action<? super Groovydoc> action) {
        action.execute(groovydoc)
    }

    void groovydoc(@DelegatesTo(Groovydoc) Closure action) {
        ConfigureUtil.configure(action, groovydoc)
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

    void license(Action<? super License> action) {
        action.execute(license)
    }

    void license(@DelegatesTo(License) Closure action) {
        ConfigureUtil.configure(action, license)
    }

    void minpom(Action<? super Minpom> action) {
        action.execute(minpom)
    }

    void minpom(@DelegatesTo(Minpom) Closure action) {
        ConfigureUtil.configure(action, minpom)
    }

    void publishing(Action<? super Publishing> action) {
        action.execute(publishing)
    }

    void publishing(@DelegatesTo(Publishing) Closure action) {
        ConfigureUtil.configure(action, publishing)
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

    void setRelease(boolean release) {
        this.release = release
        this.releaseSet = true
    }

    boolean isReleaseSet() {
        return releaseSet
    }

    ProjectConfigurationExtension copyOf() {
        ProjectConfigurationExtension copy = new ProjectConfigurationExtension(project)
        copy.@release = release
        copy.@releaseSet = releaseSet
        info.copyInto(copy.info)
        apidoc.copyInto(copy.apidoc)
        bintray.copyInto(copy.bintray)
        groovydoc.copyInto(copy.groovydoc)
        jacoco.copyInto(copy.jacoco)
        javadoc.copyInto(copy.javadoc)
        license.copyInto(copy.license)
        minpom.copyInto(copy.minpom)
        publishing.copyInto(copy.publishing)
        source.copyInto(copy.source)
        stats.copyInto(copy.stats)
        sourceHtml.copyInto(copy.sourceHtml)

        copy
    }

    ProjectConfigurationExtension merge(ProjectConfigurationExtension other) {
        ProjectConfigurationExtension merged = copyOf()
        merged.setRelease((boolean) (releaseSet ? release : other.release))
        Information.merge(merged.info, other.info)
        Apidoc.merge(merged.apidoc, other.apidoc)
        Bintray.merge(merged.bintray, other.bintray)
        Groovydoc.merge(merged.groovydoc, other.groovydoc)
        Jacoco.merge(merged.jacoco, other.jacoco)
        Javadoc.merge(merged.javadoc, other.javadoc)
        License.merge(merged.license, other.license)
        Minpom.merge(merged.minpom, other.minpom)
        Publishing.merge(merged.publishing, other.publishing)
        Source.merge(merged.source, other.source)
        Stats.merge(merged.stats, other.stats)
        SourceHtml.merge(merged.sourceHtml, other.sourceHtml)

        merged
    }

    List<String> validate() {
        List<String> errors = []

        errors.addAll(info.validate())
        errors.addAll(bintray.validate(info))
        errors.addAll(license.validate(info))
        errors.addAll(publishing.validate(info))

        errors
    }
}
