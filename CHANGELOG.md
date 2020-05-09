# Changelog

## [0.36.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.36.0) (2020-05-09)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.35.0...0.36.0)

**Implemented enhancements:**

- \[Base\] Add varargs variants for dirs\(\) and paths\(\) in projects DSL [\#295](https://github.com/kordamp/kordamp-gradle-plugins/issues/295)
- \[Settings\] Add varargs variants for dirs\(\) and paths\(\) [\#294](https://github.com/kordamp/kordamp-gradle-plugins/issues/294)
- \[Profiles\] Support multiple activations per profile [\#299](https://github.com/kordamp/kordamp-gradle-plugins/issues/299)
- \[Settings\] support long project paths [\#298](https://github.com/kordamp/kordamp-gradle-plugins/issues/298)
- \[Guide\] upgrade asciidoctorj-tabbed-code-extension to latest [\#292](https://github.com/kordamp/kordamp-gradle-plugins/issues/292)

**Fixed bugs:**

- Can't import this project into IDEA 2020.1 on windows 10 [\#296](https://github.com/kordamp/kordamp-gradle-plugins/issues/296)
- \[Publishing\] PluginMarker publication should not be enhanced [\#293](https://github.com/kordamp/kordamp-gradle-plugins/issues/293)
- groovydocJar tasks produce Gradle warnings messages for Gradle 6.3 [\#291](https://github.com/kordamp/kordamp-gradle-plugins/issues/291)

**Merged pull requests:**

- disable fail-fast in github actions [\#300](https://github.com/kordamp/kordamp-gradle-plugins/pull/300) ([kortov](https://github.com/kortov))

## [0.35.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.35.0) (2020-05-02)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.34.0...0.35.0)

**Implemented enhancements:**

- kordamp.org via HTTPS [\#283](https://github.com/kordamp/kordamp-gradle-plugins/issues/283)
- \[Settings\] Make subproject's folders more meaningful [\#237](https://github.com/kordamp/kordamp-gradle-plugins/issues/237)
- \[Minpom\] Register minpom task and disable it by default [\#290](https://github.com/kordamp/kordamp-gradle-plugins/issues/290)
- \[Jar\] Only update JAR metaInf/manifest if config.artifacts.jar is enabled [\#289](https://github.com/kordamp/kordamp-gradle-plugins/issues/289)
- \[Profiles\] Support Maven like profiles [\#288](https://github.com/kordamp/kordamp-gradle-plugins/issues/288)
- better sonar integration [\#287](https://github.com/kordamp/kordamp-gradle-plugins/issues/287)
- \[Bom\] Add an includes list [\#286](https://github.com/kordamp/kordamp-gradle-plugins/issues/286)
- \[Publishing\] Let packaging be configurable [\#285](https://github.com/kordamp/kordamp-gradle-plugins/issues/285)

## [0.34.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.34.0) (2020-04-25)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.33.0...0.34.0)

**Implemented enhancements:**

- \[Base\] Move Jar, Source and Minpom to a new aggregating section \(artifacts\) [\#281](https://github.com/kordamp/kordamp-gradle-plugins/issues/281)
- \[Jar\] Additional properties for Class-Path manifest entry [\#280](https://github.com/kordamp/kordamp-gradle-plugins/issues/280)
- \[BuildInfo\] Add an entry for Build-Os [\#279](https://github.com/kordamp/kordamp-gradle-plugins/issues/279)
- \[Functional\] Let the test directory be configurable [\#278](https://github.com/kordamp/kordamp-gradle-plugins/issues/278)
- \[Integration\] Let the test directory be configurable [\#277](https://github.com/kordamp/kordamp-gradle-plugins/issues/277)
- \[BOM\] Support dependencies in scope = import [\#276](https://github.com/kordamp/kordamp-gradle-plugins/issues/276)

**Merged pull requests:**

- Bump detekt from 1.7.2 to 1.8.0 [\#282](https://github.com/kordamp/kordamp-gradle-plugins/pull/282) ([ursjoss](https://github.com/ursjoss))

## [0.33.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.33.0) (2020-03-28)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.32.0...0.33.0)

**Implemented enhancements:**

- Improve documentation for the images \(guide\) [\#262](https://github.com/kordamp/kordamp-gradle-plugins/issues/262)
- Update versioning plugin [\#212](https://github.com/kordamp/kordamp-gradle-plugins/issues/212)
- Bump detekt from 1.7.0 to 1.7.1 [\#274](https://github.com/kordamp/kordamp-gradle-plugins/pull/274) ([ursjoss](https://github.com/ursjoss))
- Bump detekt from 1.6.0 to 1.7.0 [\#273](https://github.com/kordamp/kordamp-gradle-plugins/pull/273) ([ursjoss](https://github.com/ursjoss))
- \[Guide\] - Improve Documentation & initGuide [\#272](https://github.com/kordamp/kordamp-gradle-plugins/pull/272) ([ursjoss](https://github.com/ursjoss))

**Fixed bugs:**

- Bug: Upgrade to nemerosa:versioning:2.12.0 breaks gitPublishReset [\#267](https://github.com/kordamp/kordamp-gradle-plugins/issues/267)
- \[SourceHTML\] Kotlin/Scala sourcets are not supported [\#266](https://github.com/kordamp/kordamp-gradle-plugins/issues/266)
- Spotless plugin produces jgit incompatibility with kordamp buildinfo [\#173](https://github.com/kordamp/kordamp-gradle-plugins/issues/173)
- \[Kotlindoc\] Missing "dokkaRuntime" configuration [\#139](https://github.com/kordamp/kordamp-gradle-plugins/issues/139)

**Closed issues:**

- \[Sonar\] - Next steps [\#271](https://github.com/kordamp/kordamp-gradle-plugins/issues/271)
- Support Gradle 4.10.1 [\#260](https://github.com/kordamp/kordamp-gradle-plugins/issues/260)

**Merged pull requests:**

- Bump detekt from 1.7.1 to 1.7.2 [\#275](https://github.com/kordamp/kordamp-gradle-plugins/pull/275) ([ursjoss](https://github.com/ursjoss))
- \[Kotlindoc\] - fix aggregation of kotlindoc for multi-module project [\#270](https://github.com/kordamp/kordamp-gradle-plugins/pull/270) ([ursjoss](https://github.com/ursjoss))
- \[267\] - temmporarily downgrade version plugin to 2.11.0 [\#269](https://github.com/kordamp/kordamp-gradle-plugins/pull/269) ([ursjoss](https://github.com/ursjoss))
- \[kotlindoc\] Fix copy/paste issue [\#268](https://github.com/kordamp/kordamp-gradle-plugins/pull/268) ([ursjoss](https://github.com/ursjoss))
- \[kotlindoc\] Improvements [\#265](https://github.com/kordamp/kordamp-gradle-plugins/pull/265) ([ursjoss](https://github.com/ursjoss))
- Fix ant-patterns in guide [\#264](https://github.com/kordamp/kordamp-gradle-plugins/pull/264) ([ursjoss](https://github.com/ursjoss))
- upgraded to the latest Coveralls plugin [\#239](https://github.com/kordamp/kordamp-gradle-plugins/pull/239) ([musketyr](https://github.com/musketyr))
- Bump detekt from 1.5.0 to 1.6.0 [\#263](https://github.com/kordamp/kordamp-gradle-plugins/pull/263) ([ursjoss](https://github.com/ursjoss))

## [0.32.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.32.0) (2020-02-01)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.31.2...0.32.0)

**Implemented enhancements:**

- \[License\] Allow specific sourceSets to be skipped [\#255](https://github.com/kordamp/kordamp-gradle-plugins/issues/255)
- ability to publish POM project [\#253](https://github.com/kordamp/kordamp-gradle-plugins/issues/253)
- \[Spotbugs\] Allow specific sourceSets to be skipped [\#246](https://github.com/kordamp/kordamp-gradle-plugins/issues/246)
- \[Detekt\] Allow specific sourceSets to be skipped [\#245](https://github.com/kordamp/kordamp-gradle-plugins/issues/245)
- \[Pmd\] Allow specific sourceSets to be skipped [\#244](https://github.com/kordamp/kordamp-gradle-plugins/issues/244)
- \[Codenarc\] Allow specific sourceSets to be skipped [\#243](https://github.com/kordamp/kordamp-gradle-plugins/issues/243)
- \[Checkstyle\] Allow specific sourceSets to be skipped [\#242](https://github.com/kordamp/kordamp-gradle-plugins/issues/242)
- \[Javadoc\] Allow javadoc.io links in javadoc.autoLinks [\#241](https://github.com/kordamp/kordamp-gradle-plugins/issues/241)
- Detekt: configuration Parameter failFast [\#252](https://github.com/kordamp/kordamp-gradle-plugins/pull/252) ([ursjoss](https://github.com/ursjoss))

**Fixed bugs:**

- \[License\] Skip checking files in build directory [\#240](https://github.com/kordamp/kordamp-gradle-plugins/issues/240)
- Improper usage of @DelegatesTo [\#238](https://github.com/kordamp/kordamp-gradle-plugins/issues/238)
- if there are no tests in the subproject then aggregateJacocoMerge fails [\#236](https://github.com/kordamp/kordamp-gradle-plugins/issues/236)
- Fix detektPlugin: Apply detekt to projects with plugin kotlin-base \(not java-base\) [\#251](https://github.com/kordamp/kordamp-gradle-plugins/pull/251) ([ursjoss](https://github.com/ursjoss))

**Closed issues:**

- Task with name 'aggregateJacocoReport' not found in root project ''. [\#254](https://github.com/kordamp/kordamp-gradle-plugins/issues/254)
- Remove BuildScan plugin [\#249](https://github.com/kordamp/kordamp-gradle-plugins/issues/249)
- Remove Apidoc plugin [\#248](https://github.com/kordamp/kordamp-gradle-plugins/issues/248)
- Refactor duplication of aggregate configuration [\#247](https://github.com/kordamp/kordamp-gradle-plugins/issues/247)

**Merged pull requests:**

- \[Sonar\] Remove obsolete childProject configuration [\#259](https://github.com/kordamp/kordamp-gradle-plugins/pull/259) ([ursjoss](https://github.com/ursjoss))

## [0.31.2](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.31.2) (2020-01-04)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.31.1...0.31.2)

**Fixed bugs:**

- Problem with sourceJar after upgrade to 0.31.1 [\#235](https://github.com/kordamp/kordamp-gradle-plugins/issues/235)
- problem with subproject of the same name [\#215](https://github.com/kordamp/kordamp-gradle-plugins/issues/215)

## [0.31.1](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.31.1) (2020-01-01)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.31.0...0.31.1)

**Fixed bugs:**

- \[Javadoc\] JavadocJar is not added to main publication [\#234](https://github.com/kordamp/kordamp-gradle-plugins/issues/234)

## [0.31.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.31.0) (2020-01-01)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.30.4...0.31.0)

**Implemented enhancements:**

- Standardize aggregate configurations [\#232](https://github.com/kordamp/kordamp-gradle-plugins/issues/232)
- \[ErrorProne\] Add plugin support [\#231](https://github.com/kordamp/kordamp-gradle-plugins/issues/231)
- \[SpotBugs\] Add plugin support [\#230](https://github.com/kordamp/kordamp-gradle-plugins/issues/230)
- \[Jacoco\] Rename aggregate tasks [\#224](https://github.com/kordamp/kordamp-gradle-plugins/issues/224)
- \[SourceXref\] Register aggregate tasks even if disabled [\#223](https://github.com/kordamp/kordamp-gradle-plugins/issues/223)
- \[SourceHtml\] Register aggregate tasks even if disabled [\#222](https://github.com/kordamp/kordamp-gradle-plugins/issues/222)
- \[Scaladoc\] Register aggregate tasks even if disabled [\#221](https://github.com/kordamp/kordamp-gradle-plugins/issues/221)
- \[Kotlindoc\] Register aggregate tasks even if disabled [\#220](https://github.com/kordamp/kordamp-gradle-plugins/issues/220)
- \[Apidoc\] Register aggregate tasks even if disabled [\#219](https://github.com/kordamp/kordamp-gradle-plugins/issues/219)
- \[Guide\] Include output of aggregateXref if enabled [\#218](https://github.com/kordamp/kordamp-gradle-plugins/issues/218)
- \[DSL\] Move coverage plugins to a 'coverage' block [\#217](https://github.com/kordamp/kordamp-gradle-plugins/issues/217)
- \[DSL\] Move documentation plugins to a 'docs' block [\#216](https://github.com/kordamp/kordamp-gradle-plugins/issues/216)
- support for BinTray publish flag [\#214](https://github.com/kordamp/kordamp-gradle-plugins/issues/214)
- include Coveralls plugin [\#213](https://github.com/kordamp/kordamp-gradle-plugins/issues/213)
- \[Base\] Add a task for displaying extension settings [\#211](https://github.com/kordamp/kordamp-gradle-plugins/issues/211)
- Support for static analysis tools [\#210](https://github.com/kordamp/kordamp-gradle-plugins/issues/210)
- \[Guide\] Configure publication via Git [\#207](https://github.com/kordamp/kordamp-gradle-plugins/issues/207)

**Fixed bugs:**

- \[Jacoco\] Report aggregation does not occur if a test fails [\#209](https://github.com/kordamp/kordamp-gradle-plugins/issues/209)
- \[Testing\] Report aggregation does not occur if a test fails [\#208](https://github.com/kordamp/kordamp-gradle-plugins/issues/208)

**Closed issues:**

- \[Jacoco\] Rename merge\* to aggregate\* [\#233](https://github.com/kordamp/kordamp-gradle-plugins/issues/233)
- \[Scaladoc\] Rename aggregateScaladocs to aggregateScaladoc [\#228](https://github.com/kordamp/kordamp-gradle-plugins/issues/228)
- \[Groovydoc\] Rename aggregateGroovydocs to aggregateGroovydoc [\#227](https://github.com/kordamp/kordamp-gradle-plugins/issues/227)
- \[Javadoc\] Rename aggregateJavadocs to aggregateJavadoc [\#226](https://github.com/kordamp/kordamp-gradle-plugins/issues/226)
- \[Apidoc\] Deprecate plugin [\#225](https://github.com/kordamp/kordamp-gradle-plugins/issues/225)
- Deprecate build-scan plugin [\#206](https://github.com/kordamp/kordamp-gradle-plugins/issues/206)

**Merged pull requests:**

- Fix the SourceSet path created by the integration-test plugin in docu… [\#229](https://github.com/kordamp/kordamp-gradle-plugins/pull/229) ([seakayone](https://github.com/seakayone))

## [0.30.4](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.30.4) (2019-12-05)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.30.3...0.30.4)

**Fixed bugs:**

- \[Project\] Re-entrant apply still failing [\#205](https://github.com/kordamp/kordamp-gradle-plugins/issues/205)

## [0.30.3](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.30.3) (2019-11-30)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.30.2...0.30.3)

**Fixed bugs:**

- \[Publishing\] MPE when exclusions are defined [\#204](https://github.com/kordamp/kordamp-gradle-plugins/issues/204)
- \[project\] Avoid re-entrant calls to apply [\#203](https://github.com/kordamp/kordamp-gradle-plugins/issues/203)

## [0.30.2](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.30.2) (2019-11-29)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.30.0...0.30.2)

**Fixed bugs:**

- \[Publishing\] Dependencies are not added to generated POM [\#202](https://github.com/kordamp/kordamp-gradle-plugins/issues/202)

## [0.30.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.30.0) (2019-11-29)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.29.0...0.30.0)

**Implemented enhancements:**

- \[Base\] Split language support into their own projects [\#201](https://github.com/kordamp/kordamp-gradle-plugins/issues/201)
- \[Base\] Add a task to display Kotlin compiler settings [\#200](https://github.com/kordamp/kordamp-gradle-plugins/issues/200)
- \[Base\] Add a task to display WAR settings [\#197](https://github.com/kordamp/kordamp-gradle-plugins/issues/197)
- \[Base\] Add a task to display TAR settings [\#196](https://github.com/kordamp/kordamp-gradle-plugins/issues/196)
- \[Base\] Add a task for displaying settings of any Task [\#194](https://github.com/kordamp/kordamp-gradle-plugins/issues/194)
- \[functional-test\] Support api/implementation configurations [\#193](https://github.com/kordamp/kordamp-gradle-plugins/issues/193)
- \[integration-test\] Support api/implementation configurations [\#192](https://github.com/kordamp/kordamp-gradle-plugins/issues/192)
- \[Jacoco\] Add a task that generates all reports [\#191](https://github.com/kordamp/kordamp-gradle-plugins/issues/191)
- \[Base\] Display Scala compiler settings [\#187](https://github.com/kordamp/kordamp-gradle-plugins/issues/187)
- \[Base\] Add a task to display ZIP settings [\#186](https://github.com/kordamp/kordamp-gradle-plugins/issues/186)
- \[Base\] Add a task to display JAR settings [\#185](https://github.com/kordamp/kordamp-gradle-plugins/issues/185)
- \[Base\] Display destinationDir property in GroovyCompilerSettingsTask [\#184](https://github.com/kordamp/kordamp-gradle-plugins/issues/184)
- \[Base\] Display destinationDir property in JavaCompilerSettingsTask [\#183](https://github.com/kordamp/kordamp-gradle-plugins/issues/183)
- Improve support for integration/functional test written in kotlin [\#188](https://github.com/kordamp/kordamp-gradle-plugins/pull/188) ([ursjoss](https://github.com/ursjoss))

**Fixed bugs:**

- 0.30.0-Beta breaks an integration test scenario by not seeing the main source set [\#199](https://github.com/kordamp/kordamp-gradle-plugins/issues/199)
- Problem with `gradlew jacocoRootMerge` with gradle project using kotlin DSL [\#198](https://github.com/kordamp/kordamp-gradle-plugins/issues/198)
- 0.30.0-SNAPSHOT breaks dependency resolution between modules [\#195](https://github.com/kordamp/kordamp-gradle-plugins/issues/195)
- \[Plugin\] `{pluginName}PluginMarkerMaven` publication is not properly configured [\#167](https://github.com/kordamp/kordamp-gradle-plugins/issues/167)

**Closed issues:**

- Exception in Intellij when using IntegrationTestPlugin together with the IdeaPlugin [\#189](https://github.com/kordamp/kordamp-gradle-plugins/issues/189)

**Merged pull requests:**

- Remove the IdeaPlugin based configuration for integration/functional tests to avoid an exception in IntelliJ [\#190](https://github.com/kordamp/kordamp-gradle-plugins/pull/190) ([ursjoss](https://github.com/ursjoss))

## [0.29.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.29.0) (2019-10-26)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.28.0...0.29.0)

**Implemented enhancements:**

- Simplify configuration example for Kotlin DSL for release [\#179](https://github.com/kordamp/kordamp-gradle-plugins/pull/179) ([ursjoss](https://github.com/ursjoss))

**Fixed bugs:**

- \[Scaladoc\] ScaladocJar is not attached to publication [\#182](https://github.com/kordamp/kordamp-gradle-plugins/issues/182)
- \[Kotlindoc\] KotlindocJar is not attached to publication [\#181](https://github.com/kordamp/kordamp-gradle-plugins/issues/181)
- \[Groovydoc\] GroovydocJar is not attached to publication [\#180](https://github.com/kordamp/kordamp-gradle-plugins/issues/180)

## [0.28.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.28.0) (2019-10-19)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.27.0...0.28.0)

**Fixed bugs:**

- Javadoc JAR is not published [\#178](https://github.com/kordamp/kordamp-gradle-plugins/issues/178)
- Blank vendor is giving errors when organization.name is not blank [\#177](https://github.com/kordamp/kordamp-gradle-plugins/issues/177)

## [0.27.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.27.0) (2019-09-27)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.26.0...0.27.0)

**Fixed bugs:**

- \[Javadoc\] MPE when Groovydoc.replaceJavadoc is enabled [\#174](https://github.com/kordamp/kordamp-gradle-plugins/issues/174)
- \[Javadoc\] Normalize path for local URLs in AutoLinks.calculateLocalJavadocLink [\#171](https://github.com/kordamp/kordamp-gradle-plugins/issues/171)
- Fix setting `-Xdoclint:none` and `-quiet` options for Javadoc [\#172](https://github.com/kordamp/kordamp-gradle-plugins/pull/172) ([tlinkowski](https://github.com/tlinkowski))

## [0.26.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.26.0) (2019-08-30)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.25.0...0.26.0)

**Implemented enhancements:**

- \[Javadoc\] AutoLinks fail on multi-project builds [\#169](https://github.com/kordamp/kordamp-gradle-plugins/issues/169)
- \[Base\] Add a task for displaying JavaExec settings [\#160](https://github.com/kordamp/kordamp-gradle-plugins/issues/160)
- \[Kotlindoc\] Allow aggregation [\#80](https://github.com/kordamp/kordamp-gradle-plugins/issues/80)
- \[Javadoc\] Add 'api' configuration to DEFAULT\_CONFIGURATIONS for AutoLinks [\#168](https://github.com/kordamp/kordamp-gradle-plugins/pull/168) ([tlinkowski](https://github.com/tlinkowski))

**Fixed bugs:**

- \[BuildInfo, Jar\] Disabling buildInfo doesn't skip manifest attribute insertion [\#163](https://github.com/kordamp/kordamp-gradle-plugins/issues/163)
- \[Kotlindoc\] Default output directory is incorrect for multi-project builds [\#161](https://github.com/kordamp/kordamp-gradle-plugins/issues/161)
- \[Kotlindoc\] GroovyCastException for kotlindoc.externalDocumentationLinks [\#155](https://github.com/kordamp/kordamp-gradle-plugins/issues/155)
- \[Plugin\] Apply BasePlugin if missing [\#165](https://github.com/kordamp/kordamp-gradle-plugins/pull/165) ([tlinkowski](https://github.com/tlinkowski))

**Merged pull requests:**

- Document applying org.kordamp.gradle.jacoco plugin [\#162](https://github.com/kordamp/kordamp-gradle-plugins/pull/162) ([tlinkowski](https://github.com/tlinkowski))

## [0.25.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.25.0) (2019-07-30)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.24.0...0.25.0)

**Implemented enhancements:**

- \[Publishing\] Add a publications property [\#159](https://github.com/kordamp/kordamp-gradle-plugins/issues/159)
- Use project.pluginManager instead of project.plugins whenever possible [\#157](https://github.com/kordamp/kordamp-gradle-plugins/issues/157)

**Fixed bugs:**

- \[Jar\] NPE when resolving effectiveConfig on rootProject [\#153](https://github.com/kordamp/kordamp-gradle-plugins/issues/153)
- \[Base\] info.people.person is missing methods to configure its organization [\#152](https://github.com/kordamp/kordamp-gradle-plugins/issues/152)

**Closed issues:**

- \[Base\] Register sourceSet tasks only when `java-base` plugin is applied [\#158](https://github.com/kordamp/kordamp-gradle-plugins/issues/158)

**Merged pull requests:**

- Documented applying com.github.ben-manes.versions [\#156](https://github.com/kordamp/kordamp-gradle-plugins/pull/156) ([tlinkowski](https://github.com/tlinkowski))
- Add `maxDepth\(1\)` to `walkTopDown\(\)` [\#154](https://github.com/kordamp/kordamp-gradle-plugins/pull/154) ([tlinkowski](https://github.com/tlinkowski))

## [0.24.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.24.0) (2019-07-08)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.23.0...0.24.0)

**Implemented enhancements:**

- \[Base\] Add a task for listing configurations [\#148](https://github.com/kordamp/kordamp-gradle-plugins/issues/148)
- \[Base\] Add a task for displaying Configuration settings [\#147](https://github.com/kordamp/kordamp-gradle-plugins/issues/147)
- \[Base\] Add a task for displaying SourceSet configuration [\#146](https://github.com/kordamp/kordamp-gradle-plugins/issues/146)
- \[Base\] Add a task for listing sourceSets [\#145](https://github.com/kordamp/kordamp-gradle-plugins/issues/145)

**Fixed bugs:**

- \[Kotlindoc\] NPE during project configuration [\#150](https://github.com/kordamp/kordamp-gradle-plugins/issues/150)
-  \[Base\] TestSettingsTask throws NPE if given sourceSet name does not exist [\#144](https://github.com/kordamp/kordamp-gradle-plugins/issues/144)
- \[Base\] GroovyCompilerSettingsTask throws NPE if given sourceSet name does not exist [\#143](https://github.com/kordamp/kordamp-gradle-plugins/issues/143)
- \[Base\] JavaCompilerSettingsTask throws NPE if given sourceSet name does not exist [\#142](https://github.com/kordamp/kordamp-gradle-plugins/issues/142)
-  \[functional-test\] MPE when Groovy plugin is applied [\#141](https://github.com/kordamp/kordamp-gradle-plugins/issues/141)
- \[integration-test\] MPE when Groovy plugin is applied [\#140](https://github.com/kordamp/kordamp-gradle-plugins/issues/140)

**Merged pull requests:**

- Fixed a typo in "licenseFormatGradle" [\#149](https://github.com/kordamp/kordamp-gradle-plugins/pull/149) ([tlinkowski](https://github.com/tlinkowski))

## [0.23.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.23.0) (2019-07-01)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.22.0...0.23.0)

**Implemented enhancements:**

- \[Licensing\] Add mappings and a task for Gradle build files [\#137](https://github.com/kordamp/kordamp-gradle-plugins/issues/137)
- \[Licensing\] Add mapping for Kotlin files [\#136](https://github.com/kordamp/kordamp-gradle-plugins/issues/136)
- \[Scaladoc\] Customize classifier [\#130](https://github.com/kordamp/kordamp-gradle-plugins/issues/130)

**Fixed bugs:**

- \[Base\] Redundant config extension merging [\#134](https://github.com/kordamp/kordamp-gradle-plugins/issues/134)
- \[Plugin\] Unwanted plugin declaration added [\#132](https://github.com/kordamp/kordamp-gradle-plugins/issues/132)

**Merged pull requests:**

- Fixed a minor typo [\#138](https://github.com/kordamp/kordamp-gradle-plugins/pull/138) ([tlinkowski](https://github.com/tlinkowski))
- \[base\] Fixed org.kordamp.gradle.plugin.base.plugins.Plugin.merge [\#133](https://github.com/kordamp/kordamp-gradle-plugins/pull/133) ([tlinkowski](https://github.com/tlinkowski))
- Add lines setting root project name in sample settings.gradle\(.kts\) [\#89](https://github.com/kordamp/kordamp-gradle-plugins/pull/89) ([tlinkowski](https://github.com/tlinkowski))

## [0.22.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.22.0) (2019-06-15)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.21.0...0.22.0)

**Implemented enhancements:**

- \[Base\] Map all POM elements [\#129](https://github.com/kordamp/kordamp-gradle-plugins/issues/129)

**Fixed bugs:**

- \[Plugin\] Exception occurs with v0.21.0 [\#128](https://github.com/kordamp/kordamp-gradle-plugins/issues/128)

**Closed issues:**

- Remove deprecated plugins [\#127](https://github.com/kordamp/kordamp-gradle-plugins/issues/127)

## [0.21.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.21.0) (2019-06-04)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.20.0...0.21.0)

**Implemented enhancements:**

- \[Base\] Make secrets list configurable [\#126](https://github.com/kordamp/kordamp-gradle-plugins/issues/126)
- Support adding/removing of pre-/postfixes for build.gradle filenames [\#125](https://github.com/kordamp/kordamp-gradle-plugins/issues/125)
- Enhance the signing activation [\#124](https://github.com/kordamp/kordamp-gradle-plugins/issues/124)
- Mask Secrets in projectProperties [\#123](https://github.com/kordamp/kordamp-gradle-plugins/issues/123)

**Fixed bugs:**

- \[Base\] CME when showing paths in compiler insight tasks [\#122](https://github.com/kordamp/kordamp-gradle-plugins/issues/122)

## [0.20.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.20.0) (2019-05-13)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.19.0...0.20.0)

**Implemented enhancements:**

- \[Publishing\] Display publication settings [\#121](https://github.com/kordamp/kordamp-gradle-plugins/issues/121)
- \[Base\] Display Test task compiler settings [\#120](https://github.com/kordamp/kordamp-gradle-plugins/issues/120)
- \[Base\] Display Groovy compiler settings  [\#119](https://github.com/kordamp/kordamp-gradle-plugins/issues/119)
- \[Base\] Display Java compiler settings [\#118](https://github.com/kordamp/kordamp-gradle-plugins/issues/118)

## [0.19.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.19.0) (2019-04-24)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.18.0...0.19.0)

**Fixed bugs:**

- \[Plugin\] POM file is missing information [\#116](https://github.com/kordamp/kordamp-gradle-plugins/issues/116)

## [0.18.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.18.0) (2019-04-20)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.17.0...0.18.0)

**Implemented enhancements:**

- \[Guide\] Update files created by initGuide [\#115](https://github.com/kordamp/kordamp-gradle-plugins/issues/115)
- \[Jacoco\] Automatically disable if no tests are available [\#112](https://github.com/kordamp/kordamp-gradle-plugins/issues/112)
- \[Base\] Add a task to list included builds [\#111](https://github.com/kordamp/kordamp-gradle-plugins/issues/111)
- \[Base\] Add a task to list projects [\#110](https://github.com/kordamp/kordamp-gradle-plugins/issues/110)
- \[Testing\] Add a Test aggregating task [\#108](https://github.com/kordamp/kordamp-gradle-plugins/issues/108)
- \[Settings\] Expose project inclusion methods [\#107](https://github.com/kordamp/kordamp-gradle-plugins/issues/107)

**Fixed bugs:**

- \[Guide\] Include values from config.buildInfo [\#114](https://github.com/kordamp/kordamp-gradle-plugins/issues/114)
- \[Clirr\] NPE when applying plugin to project without 'jar' task [\#109](https://github.com/kordamp/kordamp-gradle-plugins/issues/109)

**Closed issues:**

- \[Guide\] Upgrade to asciidoctor-gradle-plugin 2.1.0 [\#113](https://github.com/kordamp/kordamp-gradle-plugins/issues/113)

## [0.17.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.17.0) (2019-04-11)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.16.0...0.17.0)

**Implemented enhancements:**

- \[Javadoc\] Automatically calculate javadoc links based on dependencies [\#104](https://github.com/kordamp/kordamp-gradle-plugins/issues/104)

**Fixed bugs:**

- \[Javadoc\] Additional links are not honored [\#103](https://github.com/kordamp/kordamp-gradle-plugins/issues/103)
- \[Javadoc\] Custom properties are ignored [\#102](https://github.com/kordamp/kordamp-gradle-plugins/issues/102)

**Closed issues:**

- \[functional-test\] Deprecate usages of compile/runtime test configurations [\#106](https://github.com/kordamp/kordamp-gradle-plugins/issues/106)
- \[integration-test\] Deprecate usages of compile/runtime test configurations [\#105](https://github.com/kordamp/kordamp-gradle-plugins/issues/105)

## [0.16.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.16.0) (2019-04-02)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.15.0...0.16.0)

**Implemented enhancements:**

- \[BuildScan\] Disable by default; enable if applied and not explicitly disabled [\#101](https://github.com/kordamp/kordamp-gradle-plugins/issues/101)
- \[Clirr\] Disable by default; enable if applied and not explicitly disabled [\#100](https://github.com/kordamp/kordamp-gradle-plugins/issues/100)
- \[Bom\] Disable by default; enable if applied and not explicitly disabled [\#99](https://github.com/kordamp/kordamp-gradle-plugins/issues/99)
- \[Groovydoc\] Automatically calculate matching Javadoc link [\#98](https://github.com/kordamp/kordamp-gradle-plugins/issues/98)
- \[Javadoc\] Automatically calculate matching Javadoc link [\#97](https://github.com/kordamp/kordamp-gradle-plugins/issues/97)
- \[BuildScan\] Add a plugin for configuring build scans [\#95](https://github.com/kordamp/kordamp-gradle-plugins/issues/95)
- \[Plugin\] Configure java-gradle-plugin and publish-plugin plugins [\#94](https://github.com/kordamp/kordamp-gradle-plugins/issues/94)
- \[Settings\] Two-level layout could use less configuration [\#92](https://github.com/kordamp/kordamp-gradle-plugins/issues/92)

**Fixed bugs:**

- \[Publishing\] Generated POM files are missing information [\#91](https://github.com/kordamp/kordamp-gradle-plugins/issues/91)
- \[Base\] Honor console settings when printing reach output [\#93](https://github.com/kordamp/kordamp-gradle-plugins/issues/93)

**Closed issues:**

- Feature request: support for creating Gradle SuperPOM [\#96](https://github.com/kordamp/kordamp-gradle-plugins/issues/96)

## [0.15.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.15.0) (2019-03-11)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.14.0...0.15.0)

**Implemented enhancements:**

- Feature request: plugin for the contents of settings.gradle\(.kts\) [\#88](https://github.com/kordamp/kordamp-gradle-plugins/issues/88)
- \[Scaladoc\] Include scaladoc JARs in publication [\#81](https://github.com/kordamp/kordamp-gradle-plugins/issues/81)

**Fixed bugs:**

- \[Settings\] Plugin doesn't seem to work in Kotlin DSL [\#90](https://github.com/kordamp/kordamp-gradle-plugins/issues/90)
- \[Base\] NPE when invoking :plugins on a project [\#87](https://github.com/kordamp/kordamp-gradle-plugins/issues/87)

**Closed issues:**

- \[Jacoco\] Allow skipping modules that have no tests [\#85](https://github.com/kordamp/kordamp-gradle-plugins/issues/85)
- \[Test\] Rename to Testing [\#84](https://github.com/kordamp/kordamp-gradle-plugins/issues/84)
- \[Lincense\] Rename to licensing [\#83](https://github.com/kordamp/kordamp-gradle-plugins/issues/83)
- \[Base\] Required \(?\) "license" block isn't properly documented [\#82](https://github.com/kordamp/kordamp-gradle-plugins/issues/82)
- \[Scaladoc\] Allow aggregation [\#79](https://github.com/kordamp/kordamp-gradle-plugins/issues/79)
- Support generation of Scala docs [\#78](https://github.com/kordamp/kordamp-gradle-plugins/issues/78)

**Merged pull requests:**

- Fix a typo [\#86](https://github.com/kordamp/kordamp-gradle-plugins/pull/86) ([tlinkowski](https://github.com/tlinkowski))

## [0.14.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.14.0) (2019-02-28)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.13.0...0.14.0)

**Implemented enhancements:**

- Add a task for aggregating functional test reports [\#76](https://github.com/kordamp/kordamp-gradle-plugins/issues/76)
- Add a task for aggregating integration test reports [\#75](https://github.com/kordamp/kordamp-gradle-plugins/issues/75)
- Add a task for aggregating unit test reports [\#74](https://github.com/kordamp/kordamp-gradle-plugins/issues/74)
- \[Base\] Report tasks should use ANSI colors in their outpus [\#73](https://github.com/kordamp/kordamp-gradle-plugins/issues/73)
- \[Stats\] Include Kotlin Script in the default counter settings [\#72](https://github.com/kordamp/kordamp-gradle-plugins/issues/72)

## [0.13.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.13.0) (2019-01-29)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.12.0...0.13.0)

**Implemented enhancements:**

- \[Minpom\] Support \<parent\> section [\#70](https://github.com/kordamp/kordamp-gradle-plugins/issues/70)
- \[BOM\] Support \<parent\> section [\#69](https://github.com/kordamp/kordamp-gradle-plugins/issues/69)
- \[Publishing\] Support \<parent\> section [\#68](https://github.com/kordamp/kordamp-gradle-plugins/issues/68)
- \[BOM\] Add an option for disable implicit includes [\#67](https://github.com/kordamp/kordamp-gradle-plugins/issues/67)

**Fixed bugs:**

- \[Stats\] Duplicate numbers are reported [\#71](https://github.com/kordamp/kordamp-gradle-plugins/issues/71)
- \[BOM\] Can't publish artifacts to directory [\#66](https://github.com/kordamp/kordamp-gradle-plugins/issues/66)
- \[Publishing\] Use 'main' instead of 'mainPublication' [\#65](https://github.com/kordamp/kordamp-gradle-plugins/issues/65)
- \[Publishing\] Can't publish artifacts to directory [\#64](https://github.com/kordamp/kordamp-gradle-plugins/issues/64)

## [0.12.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.12.0) (2018-12-31)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.11.0...0.12.0)

**Implemented enhancements:**

- Move Clirr plugin [\#63](https://github.com/kordamp/kordamp-gradle-plugins/issues/63)
- \[SourceXref\] Allow some projects to be excluded from aggregate [\#62](https://github.com/kordamp/kordamp-gradle-plugins/issues/62)
- \[SourceHtml\] Allow some projects to be excluded from aggregate [\#61](https://github.com/kordamp/kordamp-gradle-plugins/issues/61)
- \[Source\] Allow some projects to be excluded from aggregate [\#60](https://github.com/kordamp/kordamp-gradle-plugins/issues/60)
- \[Apidoc\] Allow some projects to be excluded from aggregate [\#59](https://github.com/kordamp/kordamp-gradle-plugins/issues/59)
- \[Source\] Add an aggregate JAR task [\#58](https://github.com/kordamp/kordamp-gradle-plugins/issues/58)
- \[Jacoco\] Allow additional source/class directories to be defined [\#57](https://github.com/kordamp/kordamp-gradle-plugins/issues/57)
- \[SourceXref\] Add a JAR task for regular and aggregate sources [\#54](https://github.com/kordamp/kordamp-gradle-plugins/issues/54)
- \[SourceHtml\] Add a JAR task for regular and aggregate sources [\#53](https://github.com/kordamp/kordamp-gradle-plugins/issues/53)

**Fixed bugs:**

- Could not get unknown property 'integrationTest' for SourceSet container [\#56](https://github.com/kordamp/kordamp-gradle-plugins/issues/56)
- No signature of method: static org.kordamp.gradle.PluginUtils.resolveMainSourceDirs\(\) [\#55](https://github.com/kordamp/kordamp-gradle-plugins/issues/55)

## [0.11.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.11.0) (2018-12-20)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.10.0...0.11.0)

**Implemented enhancements:**

- \[Base\] Add a task that displays configured properties [\#52](https://github.com/kordamp/kordamp-gradle-plugins/issues/52)
- \[Base\] Add a task that displays applied extensions [\#51](https://github.com/kordamp/kordamp-gradle-plugins/issues/51)
- \[Base\] Add a task that displays applied plugins [\#50](https://github.com/kordamp/kordamp-gradle-plugins/issues/50)
- \[Base\] Add a task that displays configured repositories [\#49](https://github.com/kordamp/kordamp-gradle-plugins/issues/49)
- Consider making the build reproducible by default [\#47](https://github.com/kordamp/kordamp-gradle-plugins/issues/47)

## [0.10.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.10.0) (2018-12-08)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.9.0...0.10.0)

**Implemented enhancements:**

- \[License\] Allow non FLOSS licenses to be specified [\#42](https://github.com/kordamp/kordamp-gradle-plugins/issues/42)

**Fixed bugs:**

- Missing documentation on config.release flag [\#48](https://github.com/kordamp/kordamp-gradle-plugins/issues/48)
- \[Bintray\] Plugin is disabled by defualt on subprojects [\#46](https://github.com/kordamp/kordamp-gradle-plugins/issues/46)

## [0.9.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.9.0) (2018-11-23)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.8.0...0.9.0)

**Implemented enhancements:**

- \[Base\] Suppor the SCM block in POM files [\#45](https://github.com/kordamp/kordamp-gradle-plugins/issues/45)
- Add a plugin for BOM file generation [\#44](https://github.com/kordamp/kordamp-gradle-plugins/issues/44)
- Split project plugin for closed and open source usages [\#43](https://github.com/kordamp/kordamp-gradle-plugins/issues/43)
- \[Guide\] Allow attribute values to be overriden in a document [\#41](https://github.com/kordamp/kordamp-gradle-plugins/issues/41)
- \[Publishing\] Allow posting publications to release/snapshot repositories [\#39](https://github.com/kordamp/kordamp-gradle-plugins/issues/39)
- \[Publishing\] Configure artifact signing [\#38](https://github.com/kordamp/kordamp-gradle-plugins/issues/38)

**Fixed bugs:**

- \[Jar\] Value for buildJdk is missing [\#40](https://github.com/kordamp/kordamp-gradle-plugins/issues/40)

## [0.8.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.8.0) (2018-11-17)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.7.0...0.8.0)

**Implemented enhancements:**

- \[BuildInfo\] Allow buildTime to be set to 0 [\#37](https://github.com/kordamp/kordamp-gradle-plugins/issues/37)
- \[BuildInfo\] Add options to skip calculation of some properties [\#36](https://github.com/kordamp/kordamp-gradle-plugins/issues/36)
- Add a task to calculate the effective settings [\#35](https://github.com/kordamp/kordamp-gradle-plugins/issues/35)
- \[Bintray\] Add option to skip auto-sync with Maven Central [\#34](https://github.com/kordamp/kordamp-gradle-plugins/issues/34)

**Fixed bugs:**

- Error parsing SourceXref.javaVersion [\#33](https://github.com/kordamp/kordamp-gradle-plugins/issues/33)

## [0.7.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.7.0) (2018-11-08)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.6.0...0.7.0)

**Implemented enhancements:**

- Support Xref reports [\#32](https://github.com/kordamp/kordamp-gradle-plugins/issues/32)
- Support generation of Kotlin docs [\#31](https://github.com/kordamp/kordamp-gradle-plugins/issues/31)
- Groovydoc should include all Java sources [\#29](https://github.com/kordamp/kordamp-gradle-plugins/issues/29)
- SourceJar task should be a dependency of 'assemble' task [\#28](https://github.com/kordamp/kordamp-gradle-plugins/issues/28)
- GroovydocJar task should be a dependency of 'assemble' task [\#27](https://github.com/kordamp/kordamp-gradle-plugins/issues/27)
- JavadocJar task should be a dependency of 'assemble' task [\#26](https://github.com/kordamp/kordamp-gradle-plugins/issues/26)
- Provide Gradle Kotlin DSL examples in documentation [\#25](https://github.com/kordamp/kordamp-gradle-plugins/issues/25)

**Fixed bugs:**

- SourceStats task is not available on root project when there are no children [\#30](https://github.com/kordamp/kordamp-gradle-plugins/issues/30)

## [0.6.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.6.0) (2018-10-31)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.5.0...0.6.0)

**Implemented enhancements:**

- Support \<timezone\> and \<properties\> blocks for Developer/Contributor [\#23](https://github.com/kordamp/kordamp-gradle-plugins/issues/23)
- GuidePlugin should copy the aggregate source HTML report [\#22](https://github.com/kordamp/kordamp-gradle-plugins/issues/22)
- Create a guide project [\#10](https://github.com/kordamp/kordamp-gradle-plugins/issues/10)

**Fixed bugs:**

- SourceHtml plugin fails with projects that do not have /src/main/java directory [\#21](https://github.com/kordamp/kordamp-gradle-plugins/issues/21)

## [0.5.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.5.0) (2018-10-09)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.4.0...0.5.0)

**Implemented enhancements:**

- Let license aliases to be defined using the DSL [\#20](https://github.com/kordamp/kordamp-gradle-plugins/issues/20)
- Let 'maven' be the defualt value for bintray.repo [\#17](https://github.com/kordamp/kordamp-gradle-plugins/issues/17)
- Add common license aliases [\#15](https://github.com/kordamp/kordamp-gradle-plugins/issues/15)
- Make xml and txt formats default for config.stats [\#12](https://github.com/kordamp/kordamp-gradle-plugins/issues/12)
- Move every plugin to its own package [\#9](https://github.com/kordamp/kordamp-gradle-plugins/issues/9)
- Add a source-stats plugin [\#8](https://github.com/kordamp/kordamp-gradle-plugins/issues/8)
- Add a source-html plugin [\#5](https://github.com/kordamp/kordamp-gradle-plugins/issues/5)
- Add an aggregate license report [\#4](https://github.com/kordamp/kordamp-gradle-plugins/issues/4)

**Fixed bugs:**

- Disable license tasks if license.enabled is set to false [\#19](https://github.com/kordamp/kordamp-gradle-plugins/issues/19)
- Values from config are eagerly applied [\#18](https://github.com/kordamp/kordamp-gradle-plugins/issues/18)
- Aggregate dependency-license report doesn't handle multiple licenses per dependency [\#16](https://github.com/kordamp/kordamp-gradle-plugins/issues/16)
- Verify asciidoctor.attributes before overriding their values [\#14](https://github.com/kordamp/kordamp-gradle-plugins/issues/14)
- Fix aggregate source stats generation [\#13](https://github.com/kordamp/kordamp-gradle-plugins/issues/13)
- Disable bintray tasks if config.bintray.enabled is false [\#11](https://github.com/kordamp/kordamp-gradle-plugins/issues/11)
- Do not configure tasks per SourceSet [\#7](https://github.com/kordamp/kordamp-gradle-plugins/issues/7)
- Do not use evaluationDependsOnChildren\(\) [\#6](https://github.com/kordamp/kordamp-gradle-plugins/issues/6)
- Missing license name in generated POM [\#3](https://github.com/kordamp/kordamp-gradle-plugins/issues/3)

## [0.4.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.4.0) (2018-10-06)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.3.0...0.4.0)

## [0.3.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.3.0) (2018-10-04)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.2.0...0.3.0)

**Implemented enhancements:**

- Project plugin does not propagate to children [\#2](https://github.com/kordamp/kordamp-gradle-plugins/issues/2)

**Fixed bugs:**

- License information is missing [\#1](https://github.com/kordamp/kordamp-gradle-plugins/issues/1)

## [0.2.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.2.0) (2018-10-03)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.1.0...0.2.0)

## [0.1.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.1.0) (2018-10-01)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/ad85f56a35eddff099d9efba5b1277214c500f57...0.1.0)



\* *This Changelog was automatically generated by [github_changelog_generator](https://github.com/github-changelog-generator/github-changelog-generator)*
