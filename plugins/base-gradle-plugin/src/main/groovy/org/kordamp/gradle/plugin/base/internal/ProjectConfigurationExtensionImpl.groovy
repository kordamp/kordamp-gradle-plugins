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
package org.kordamp.gradle.plugin.base.internal

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.PluginUtils
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.ResolvedProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.ResolvedProjectConfigurationExtension.ResolvedArtifacts
import org.kordamp.gradle.plugin.base.ResolvedProjectConfigurationExtension.ResolvedCoverage
import org.kordamp.gradle.plugin.base.ResolvedProjectConfigurationExtension.ResolvedQuality
import org.kordamp.gradle.plugin.base.ResolvedProjectConfigurationExtension.ResolvedDocs
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
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedCheckstyle
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedCodenarc
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedCoveralls
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedDetekt
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedErrorProne
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedGroovydoc
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedGuide
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedJacoco
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedJar
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedJavadoc
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedKotlindoc
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedMinpom
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedPmd
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedScaladoc
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedSonar
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedSource
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedSourceHtml
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedSourceXref
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedSpotbugs

import static org.kordamp.gradle.PropertyUtils.booleanProvider

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@CompileStatic
class ProjectConfigurationExtensionImpl implements ProjectConfigurationExtension {
    private static final String ORG_KORDAMP_GRADLE_BASE_VALIDATE = 'org.kordamp.gradle.base.validate'

    private final Project project
    private final ProjectConfigurationExtensionImpl parentConfig

    final Property<Boolean> release
    final TestingImpl testing
    final ArtifactsImpl artifacts
    final CoverageImpl coverage
    final QualityImpl quality
    final DocsImpl docs

    ProjectConfigurationExtensionImpl(Project project, ProjectConfigurationExtensionImpl parentConfig) {
        this.project = project
        this.parentConfig = parentConfig

        release = project.objects.property(Boolean).convention(false)
        testing = new TestingImpl(project, this, parentConfig)
        artifacts = new ArtifactsImpl(project, this, parentConfig)
        coverage = new CoverageImpl(project, this, parentConfig)
        quality = new QualityImpl(project, this, parentConfig)
        docs = new DocsImpl(project, this, parentConfig)

        project.afterEvaluate(new Action<Project>() {
            @Override
            void execute(Project p) {
                validateConfig()
            }
        })
    }

    ResolvedProjectConfigurationExtension asResolved() {
        null
    }

    private void validateConfig() {
        normalize()

        if (PluginUtils.checkFlag(ORG_KORDAMP_GRADLE_BASE_VALIDATE, true)) {
            List<String> errors = []

            validate(errors)

            if (errors) {
                errors.each { project.logger.error(it) }
                throw new GradleException("Project ${project.name} has not been properly configured")
            }
        }
    }

    @Override
    void info(Action<? super Information> action) {
        action.execute(info)
    }

    @Override
    void info(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Information) Closure<Void> action) {
        ConfigureUtil.configure(action, info)
    }

    @Override
    void bom(Action<? super Bom> action) {
        action.execute(bom)
    }

    @Override
    void bom(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Bom) Closure<Void> action) {
        ConfigureUtil.configure(action, bom)
    }

    @Override
    void bintray(Action<? super Bintray> action) {
        action.execute(bintray)
    }

    @Override
    void bintray(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Bintray) Closure<Void> action) {
        ConfigureUtil.configure(action, bintray)
    }

    @Override
    void buildInfo(Action<? super BuildInfo> action) {
        action.execute(buildInfo)
    }

    @Override
    void buildInfo(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = BuildInfo) Closure<Void> action) {
        ConfigureUtil.configure(action, buildInfo)
    }

    @Override
    void clirr(Action<? super Clirr> action) {
        action.execute(clirr)
    }

    @Override
    void clirr(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Clirr) Closure<Void> action) {
        ConfigureUtil.configure(action, clirr)
    }

    @Override
    void licensing(Action<? super Licensing> action) {
        action.execute(licensing)
    }

    @Override
    void licensing(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Licensing) Closure<Void> action) {
        ConfigureUtil.configure(action, licensing)
    }

    @Override
    void plugin(Action<? super Plugin> action) {
        action.execute(plugin)
    }

    @Override
    void plugin(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Plugin) Closure<Void> action) {
        ConfigureUtil.configure(action, plugin)
    }

    @Override
    void publishing(Action<? super Publishing> action) {
        action.execute(publishing)
    }

    @Override
    void publishing(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Publishing) Closure<Void> action) {
        ConfigureUtil.configure(action, publishing)
    }

    @Override
    void stats(Action<? super Stats> action) {
        action.execute(stats)
    }

    @Override
    void stats(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Stats) Closure<Void> action) {
        ConfigureUtil.configure(action, stats)
    }

    @Override
    void testing(Action<? super Testing> action) {
        action.execute(testing)
    }

    @Override
    void testing(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Testing) Closure<Void> action) {
        ConfigureUtil.configure(action, testing)
    }

    @Override
    void artifacts(Action<? super Artifacts> action) {
        action.execute(artifacts)
    }

    @Override
    void artifacts(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifacts) Closure<Void> action) {
        ConfigureUtil.configure(action, artifacts)
    }

    @Override
    void docs(Action<? super Docs> action) {
        action.execute(docs)
    }

    @Override
    void docs(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Docs) Closure<Void> action) {
        ConfigureUtil.configure(action, docs)
    }

    @Override
    void coverage(Action<? super Coverage> action) {
        action.execute(coverage)
    }

    @Override
    void coverage(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Coverage) Closure<Void> action) {
        ConfigureUtil.configure(action, coverage)
    }

    @Override
    void quality(Action<? super Quality> action) {
        action.execute(quality)
    }

    @Override
    void quality(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Quality) Closure<Void> action) {
        ConfigureUtil.configure(action, quality)
    }

    @PackageScope
    @CompileStatic
    static class QualityImpl extends AbstractFeature implements Quality {
        final CheckstyleImpl checkstyle
        final CodenarcImpl codenarc
        final DetektImpl detekt
        final ErrorProneImpl errorprone
        final PmdImpl pmd
        final SonarImpl sonar
        final SpotbugsImpl spotbugs

        private ResolvedQuality resolved

        QualityImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
            super(project, ownerConfig, parentConfig)

            checkstyle = new CheckstyleImpl(project, ownerConfig, parentConfig)
            codenarc = new CodenarcImpl(project, ownerConfig, parentConfig)
            detekt = new DetektImpl(project, ownerConfig, parentConfig)
            errorprone = new ErrorProneImpl(project, ownerConfig, parentConfig)
            pmd = new PmdImpl(project, ownerConfig, parentConfig)
            sonar = new SonarImpl(project, ownerConfig, parentConfig)
            spotbugs = new SpotbugsImpl(project, ownerConfig, parentConfig)
        }

        @Override
        void normalize() {
            checkstyle.normalize()
            codenarc.normalize()
            detekt.normalize()
            errorprone.normalize()
            pmd.normalize()
            sonar.normalize()
            spotbugs.normalize()
        }

        @Override
        void validate(List<String> errors) {
            checkstyle.validate(errors)
            codenarc.validate(errors)
            detekt.validate(errors)
            errorprone.validate(errors)
            pmd.validate(errors)
            sonar.validate(errors)
            spotbugs.validate(errors)
        }

        @Override
        void checkstyle(Action<? super Checkstyle> action) {
            action.execute(checkstyle)
        }

        @Override
        void checkstyle(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Checkstyle) Closure<Void> action) {
            ConfigureUtil.configure(action, checkstyle)
        }

        @Override
        void codenarc(Action<? super Codenarc> action) {
            action.execute(codenarc)
        }

        @Override
        void codenarc(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Codenarc) Closure<Void> action) {
            ConfigureUtil.configure(action, codenarc)
        }

        @Override
        void detekt(Action<? super Detekt> action) {
            action.execute(detekt)
        }

        @Override
        void detekt(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Detekt) Closure<Void> action) {
            ConfigureUtil.configure(action, detekt)
        }

        @Override
        void errorprone(Action<? super ErrorProne> action) {
            action.execute(errorprone)
        }

        @Override
        void errorprone(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ErrorProne) Closure<Void> action) {
            ConfigureUtil.configure(action, errorprone)
        }

        @Override
        void pmd(Action<? super Pmd> action) {
            action.execute(pmd)
        }

        @Override
        void pmd(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Pmd) Closure<Void> action) {
            ConfigureUtil.configure(action, pmd)
        }

        @Override
        void sonar(Action<? super Sonar> action) {
            action.execute(sonar)
        }

        @Override
        void sonar(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Sonar) Closure<Void> action) {
            ConfigureUtil.configure(action, sonar)
        }

        @Override
        void spotbugs(Action<? super Spotbugs> action) {
            action.execute(spotbugs)
        }

        @Override
        void spotbugs(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Spotbugs) Closure<Void> action) {
            ConfigureUtil.configure(action, spotbugs)
        }

        ResolvedQuality asResolved() {
            if (!resolved) {
                resolved = new ResolvedQualityImpl(project.providers,
                    parentConfig?.asResolved()?.quality,
                    this)
            }
            resolved
        }
    }

    @PackageScope
    @CompileStatic
    static class ResolvedQualityImpl extends AbstractResolvedFeature implements ResolvedQuality {
        final Provider<Boolean> enabled
        private final QualityImpl self

        ResolvedQualityImpl(ProviderFactory providers, ResolvedQuality parent, QualityImpl self) {
            super(self.project)
            this.self = self

            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                true)
        }

        @Override
        ResolvedCheckstyle getCheckstyle() {
            self.checkstyle.asResolved()
        }

        @Override
        ResolvedCodenarc getCodenarc() {
            self.codenarc.asResolved()
        }

        @Override
        ResolvedDetekt getDetekt() {
            self.detekt.asResolved()
        }

        @Override
        ResolvedErrorProne getErrorprone() {
            self.errorprone.asResolved()
        }

        @Override
        ResolvedPmd getPmd() {
            self.pmd.asResolved()
        }

        @Override
        ResolvedSpotbugs getSpotbugs() {
            self.spotbugs.asResolved()
        }

        @Override
        ResolvedSonar getSonar() {
            self.sonar.asResolved()
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<String, Object>()

            map.putAll(getCheckstyle().toMap())
            map.putAll(getCodenarc().toMap())
            map.putAll(getDetekt().toMap())
            map.putAll(getErrorprone().toMap())
            map.putAll(getPmd().toMap())
            map.putAll(getSpotbugs().toMap())
            map.putAll(getSonar().toMap())

            new LinkedHashMap<>('quality': map)
        }
    }

    @PackageScope
    @CompileStatic
    static class DocsImpl extends AbstractFeature implements Docs {
        final GuideImpl guide
        final GroovydocImpl groovydoc
        final KotlindocImpl kotlindoc
        final JavadocImpl javadoc
        final ScaladocImpl scaladoc
        final SourceHtmlImpl sourceHtml
        final SourceXrefImpl sourceXref

        private ResolvedDocs resolved

        DocsImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
            super(project, ownerConfig, parentConfig)

            guide = new GuideImpl(project, ownerConfig, parentConfig)
            groovydoc = new GroovydocImpl(project, ownerConfig, parentConfig)
            kotlindoc = new KotlindocImpl(project, ownerConfig, parentConfig)
            javadoc = new JavadocImpl(project, ownerConfig, parentConfig)
            scaladoc = new ScaladocImpl(project, ownerConfig, parentConfig)
            sourceHtml = new SourceHtmlImpl(project, ownerConfig, parentConfig)
            sourceXref = new SourceXrefImpl(project, ownerConfig, parentConfig)
        }

        @Override
        void normalize() {
            guide.normalize()
            groovydoc.normalize()
            kotlindoc.normalize()
            javadoc.normalize()
            scaladoc.normalize()
            sourceHtml.normalize()
            sourceXref.normalize()
        }

        @Override
        void validate(List<String> errors) {
            guide.validate(errors)
            groovydoc.validate(errors)
            kotlindoc.validate(errors)
            javadoc.validate(errors)
            scaladoc.validate(errors)
            sourceHtml.validate(errors)
            sourceXref.validate(errors)
        }

        @Override
        void guide(Action<? super Guide> action) {
            action.execute(guide)
        }

        @Override
        void guide(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Guide) Closure<Void> action) {
            ConfigureUtil.configure(action, guide)
        }

        @Override
        void groovydoc(Action<? super Groovydoc> action) {
            action.execute(groovydoc)
        }

        @Override
        void groovydoc(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Groovydoc) Closure<Void> action) {
            ConfigureUtil.configure(action, groovydoc)
        }

        @Override
        void kotlindoc(Action<? super Kotlindoc> action) {
            action.execute(kotlindoc)
        }

        @Override
        void kotlindoc(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Kotlindoc) Closure<Void> action) {
            ConfigureUtil.configure(action, kotlindoc)
        }

        @Override
        void javadoc(Action<? super Javadoc> action) {
            action.execute(javadoc)
        }

        @Override
        void javadoc(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Javadoc) Closure<Void> action) {
            ConfigureUtil.configure(action, javadoc)
        }

        @Override
        void scaladoc(Action<? super Scaladoc> action) {
            action.execute(scaladoc)
        }

        @Override
        void scaladoc(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Scaladoc) Closure<Void> action) {
            ConfigureUtil.configure(action, scaladoc)
        }

        @Override
        void sourceHtml(Action<? super SourceHtml> action) {
            action.execute(sourceHtml)
        }

        @Override
        void sourceHtml(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SourceHtml) Closure<Void> action) {
            ConfigureUtil.configure(action, sourceHtml)
        }

        @Override
        void sourceXref(Action<? super SourceXref> action) {
            action.execute(sourceXref)
        }

        @Override
        void sourceXref(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SourceXref) Closure<Void> action) {
            ConfigureUtil.configure(action, sourceXref)
        }

        ResolvedDocs asResolved() {
            if (!resolved) {
                resolved = new ResolvedDocsImpl(project.providers,
                    parentConfig?.asResolved()?.docs,
                    this)
            }
            resolved
        }
    }

    @PackageScope
    @CompileStatic
    static class ResolvedDocsImpl extends AbstractResolvedFeature implements ResolvedDocs {
        final Provider<Boolean> enabled
        private final DocsImpl self

        ResolvedDocsImpl(ProviderFactory providers, ResolvedDocs parent, DocsImpl self) {
            super(self.project)
            this.self = self

            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                true)
        }

        @Override
        ResolvedGuide getGuide() {
            self.guide.asResolved()
        }

        @Override
        ResolvedGroovydoc getGroovydoc() {
            self.groovydoc.asResolved()
        }

        @Override
        ResolvedKotlindoc getKotlindoc() {
            self.kotlindoc.asResolved()
        }

        @Override
        ResolvedJavadoc getJavadoc() {
            self.javadoc.asResolved()
        }

        @Override
        ResolvedScaladoc getScaladoc() {
            self.scaladoc.asResolved()
        }

        @Override
        ResolvedSourceHtml getSourceHtml() {
            self.sourceHtml.asResolved()
        }

        @Override
        ResolvedSourceXref getSourceXref() {
            self.sourceXref.asResolved()
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<String, Object>()

            map.putAll(getGuide().toMap())
            map.putAll(getGroovydoc().toMap())
            map.putAll(getKotlindoc().toMap())
            map.putAll(getJavadoc().toMap())
            map.putAll(getScaladoc().toMap())
            map.putAll(getSourceHtml().toMap())
            map.putAll(getSourceXref().toMap())

            new LinkedHashMap<>('docs': map)
        }
    }

    @PackageScope
    @CompileStatic
    static class CoverageImpl extends AbstractFeature implements Coverage {
        final CoverallsImpl coveralls
        final JacocoImpl jacoco

        private ResolvedCoverage resolved

        CoverageImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
            super(project, ownerConfig, parentConfig)

            coveralls = new CoverallsImpl(project, ownerConfig, parentConfig)
            jacoco = new JacocoImpl(project, ownerConfig, parentConfig)
        }

        @Override
        void normalize() {
            coveralls.normalize()
            jacoco.normalize()
        }

        @Override
        void validate(List<String> errors) {
            coveralls.validate(errors)
            jacoco.validate(errors)
        }

        @Override
        void coveralls(Action<? super Coveralls> action) {
            action.execute(coveralls)
        }

        @Override
        void coveralls(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Coveralls) Closure<Void> action) {
            ConfigureUtil.configure(action, coveralls)
        }

        @Override
        void jacoco(Action<? super Jacoco> action) {
            action.execute(jacoco)
        }

        @Override
        void jacoco(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Jacoco) Closure<Void> action) {
            ConfigureUtil.configure(action, jacoco)
        }

        ResolvedCoverage asResolved() {
            if (!resolved) {
                resolved = new ResolvedCoverageImpl(project.providers,
                    parentConfig?.asResolved()?.coverage,
                    this)
            }
            resolved
        }
    }

    @PackageScope
    @CompileStatic
    static class ResolvedCoverageImpl extends AbstractResolvedFeature implements ResolvedCoverage {
        final Provider<Boolean> enabled
        private final CoverageImpl self

        ResolvedCoverageImpl(ProviderFactory providers, ResolvedCoverage parent, CoverageImpl self) {
            super(self.project)
            this.self = self

            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                true)
        }

        @Override
        ResolvedCoveralls getCoveralls() {
            self.coveralls.asResolved()
        }

        @Override
        ResolvedJacoco getJacoco() {
            self.jacoco.asResolved()
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<String, Object>()

            map.putAll(getCoveralls().toMap())
            map.putAll(getJacoco().toMap())

            new LinkedHashMap<>('coverage': map)
        }
    }

    @PackageScope
    @CompileStatic
    static class ArtifactsImpl extends AbstractFeature implements Artifacts {
        final JarImpl jar
        final MinpomImpl minpom
        final SourceImpl source

        private ResolvedArtifacts resolved

        ArtifactsImpl(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
            super(project, ownerConfig, parentConfig)

            jar = new JarImpl(project, ownerConfig, parentConfig)
            minpom = new MinpomImpl(project, ownerConfig, parentConfig)
            source = new SourceImpl(project, ownerConfig, parentConfig)
        }

        @Override
        void normalize() {
            jar.normalize()
            minpom.normalize()
            source.normalize()
        }

        @Override
        void validate(List<String> errors) {
            jar.validate(errors)
            minpom.validate(errors)
            source.validate(errors)
        }

        @Override
        void jar(Action<? super Jar> action) {
            action.execute(jar)
        }

        @Override
        void jar(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Jar) Closure<Void> action) {
            ConfigureUtil.configure(action, jar)
        }

        @Override
        void minpom(Action<? super Minpom> action) {
            action.execute(minpom)
        }

        @Override
        void minpom(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Minpom) Closure<Void> action) {
            ConfigureUtil.configure(action, minpom)
        }

        @Override
        void source(Action<? super Source> action) {
            action.execute(source)
        }

        @Override
        void source(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Source) Closure<Void> action) {
            ConfigureUtil.configure(action, source)
        }

        ResolvedArtifacts asResolved() {
            if (!resolved) {
                resolved = new ResolvedArtifactsImpl(project.providers,
                    parentConfig?.asResolved()?.artifacts,
                    this)
            }
            resolved
        }
    }

    @PackageScope
    @CompileStatic
    static class ResolvedArtifactsImpl extends AbstractResolvedFeature implements ResolvedArtifacts {
        final Provider<Boolean> enabled
        private final ArtifactsImpl self

        ResolvedArtifactsImpl(ProviderFactory providers, ResolvedArtifacts parent, ArtifactsImpl self) {
            super(self.project)
            this.self = self

            enabled = booleanProvider(providers,
                parent?.enabled,
                self.enabled,
                true)
        }

        @Override
        ResolvedJar getJar() {
            self.jar.asResolved()
        }

        @Override
        ResolvedMinpom getMinpom() {
            self.minpom.asResolved()
        }

        @Override
        ResolvedSource getSource() {
            self.source.asResolved()
        }

        @Override
        Map<String, Map<String, Object>> toMap() {
            Map<String, Object> map = new LinkedHashMap<String, Object>()

            map.putAll(getJar().toMap())
            map.putAll(getMinpom().toMap())
            map.putAll(getSource().toMap())

            new LinkedHashMap<>('artifacts': map)
        }
    }
}
