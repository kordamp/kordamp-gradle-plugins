# Changelog

## [v0.44.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.44.0) (2021-02-27)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.43.0...v0.44.0)

**Implemented enhancements:**

- \[Javadoc\] Copy doc-files content found in source files [\#449](https://github.com/kordamp/kordamp-gradle-plugins/issues/449)

**Fixed bugs:**

- \[jacoco\] Using exclusion causes NPE [\#446](https://github.com/kordamp/kordamp-gradle-plugins/issues/446)

**Closed issues:**

- \[Bintray\] Deprecate plugin [\#447](https://github.com/kordamp/kordamp-gradle-plugins/issues/447)

## [v0.43.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.43.0) (2021-01-30)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.42.1...v0.43.0)

**Implemented enhancements:**

- \[Scaladoc\] Add option to generate an empty JAR [\#445](https://github.com/kordamp/kordamp-gradle-plugins/issues/445)
- \[Groovydoc\] Add option to generate an empty JAR [\#444](https://github.com/kordamp/kordamp-gradle-plugins/issues/444)
- \[Javadoc\] Add option to generate an empty JAR [\#443](https://github.com/kordamp/kordamp-gradle-plugins/issues/443)
- \[Source\] Add option to generate an empty JAR [\#442](https://github.com/kordamp/kordamp-gradle-plugins/issues/442)
- \[Jar\] Add Build-Jdk-Spec to manifest [\#441](https://github.com/kordamp/kordamp-gradle-plugins/issues/441)
- \[Base\] Add a task to display the contents of the archives configuration [\#440](https://github.com/kordamp/kordamp-gradle-plugins/issues/440)
- \[Javadoc\] Add an option to disable JDK autolink [\#438](https://github.com/kordamp/kordamp-gradle-plugins/issues/438)
- Add a plugin for enabling reproducible builds [\#435](https://github.com/kordamp/kordamp-gradle-plugins/issues/435)

**Fixed bugs:**

- \[Settings\] Exclude buildSrc when standard layout is used [\#439](https://github.com/kordamp/kordamp-gradle-plugins/issues/439)
- Jacoco Coverage for root src missing After Upgrading to \>= 0.37.0 [\#433](https://github.com/kordamp/kordamp-gradle-plugins/issues/433)
- Adding tasks only if they don't exist yet [\#431](https://github.com/kordamp/kordamp-gradle-plugins/issues/431)
- \[coveralls\] Coveralls stopped working in 0.38.0 and later [\#419](https://github.com/kordamp/kordamp-gradle-plugins/issues/419)

**Closed issues:**

- subproject not created [\#437](https://github.com/kordamp/kordamp-gradle-plugins/issues/437)
- Project in a subdirectory not loaded [\#436](https://github.com/kordamp/kordamp-gradle-plugins/issues/436)
- \[BuildInfo\] Use ISO 8601 instead of RCF 822 for time format [\#434](https://github.com/kordamp/kordamp-gradle-plugins/issues/434)

## [v0.42.1](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.42.1) (2020-12-06)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.42.0...v0.42.1)

**Implemented enhancements:**

- \[Project\] Add a task to copy dependencies [\#430](https://github.com/kordamp/kordamp-gradle-plugins/issues/430)

**Fixed bugs:**

- \[JavaProject\] optional dependencies are not available in test scope [\#429](https://github.com/kordamp/kordamp-gradle-plugins/issues/429)
- \[Base\] NPE when computing author list [\#428](https://github.com/kordamp/kordamp-gradle-plugins/issues/428)
- \[Settings\] ProjectsExtension.getLayout\(\) has the wrong type [\#427](https://github.com/kordamp/kordamp-gradle-plugins/issues/427)

## [v0.42.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.42.0) (2020-11-28)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.41.0...v0.42.0)

**Implemented enhancements:**

- \[licensing\] HTML report for aggregateLicenseReport task [\#421](https://github.com/kordamp/kordamp-gradle-plugins/issues/421)
- \[licensing\] Adding exclusion/inclusion patterns in DSL [\#417](https://github.com/kordamp/kordamp-gradle-plugins/issues/417)
- \[Insight\] Display the number of modules in the summary report [\#416](https://github.com/kordamp/kordamp-gradle-plugins/issues/416)
- bundle reports after failure [\#426](https://github.com/kordamp/kordamp-gradle-plugins/pull/426) ([musketyr](https://github.com/musketyr))

**Fixed bugs:**

- \[Publishing\] Platforms are added as direct dependencies in generated POMs [\#425](https://github.com/kordamp/kordamp-gradle-plugins/issues/425)
- \[Testing\] Test times are reported with the wrong color [\#424](https://github.com/kordamp/kordamp-gradle-plugins/issues/424)
- \[Licensing\] Skip format/checks on Gradle/Maven files found in build directory [\#423](https://github.com/kordamp/kordamp-gradle-plugins/issues/423)
- \[licensing\] aggregateLicenseReport produces empty report by default [\#420](https://github.com/kordamp/kordamp-gradle-plugins/issues/420)
- \[sonar\] Invalid config if only login is configured [\#418](https://github.com/kordamp/kordamp-gradle-plugins/issues/418)
- \[Base\] Credentials can't be modified [\#413](https://github.com/kordamp/kordamp-gradle-plugins/issues/413)

**Closed issues:**

- \[Licensing\] Support mappings property in DSL [\#422](https://github.com/kordamp/kordamp-gradle-plugins/issues/422)
- Upgrading kordamp from 0.40.0 to 0.41.0 causes ProjectConfigurationException [\#415](https://github.com/kordamp/kordamp-gradle-plugins/issues/415)

**Merged pull requests:**

- Document change from ticket \#402 \(setLayout\) [\#414](https://github.com/kordamp/kordamp-gradle-plugins/pull/414) ([ursjoss](https://github.com/ursjoss))

## [v0.41.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.41.0) (2020-10-31)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.40.0...v0.41.0)

**Implemented enhancements:**

- \[Inline\] Allow project names to be used as vlaues for `inline.target` [\#412](https://github.com/kordamp/kordamp-gradle-plugins/issues/412)
- \[Base\] Allow Kordamp's dependency handler to receive full GAV coordinates [\#411](https://github.com/kordamp/kordamp-gradle-plugins/issues/411)
- \[JavaProject\] Support optional dependencies [\#410](https://github.com/kordamp/kordamp-gradle-plugins/issues/410)
- \[Base\] Support additional manifest entries for Implementation [\#409](https://github.com/kordamp/kordamp-gradle-plugins/issues/409)
- \[Settings\] Allow explicit project exclusions with all layouts [\#408](https://github.com/kordamp/kordamp-gradle-plugins/issues/408)
- \[Settings\] The use of when should be discouraged [\#407](https://github.com/kordamp/kordamp-gradle-plugins/issues/407)
- \[Settings\] Allow autodiscovery of projects with `multi-level` layout [\#404](https://github.com/kordamp/kordamp-gradle-plugins/issues/404)
- \[Settings\] Cache project structure [\#403](https://github.com/kordamp/kordamp-gradle-plugins/issues/403)
- \[Settings\] Rename inclusion methods of explicit layout [\#402](https://github.com/kordamp/kordamp-gradle-plugins/issues/402)
- \[Settings\] Allow additional exclusions in the plugins DSL [\#401](https://github.com/kordamp/kordamp-gradle-plugins/issues/401)
- \[Base\] Add merge strategy to other domain sets [\#400](https://github.com/kordamp/kordamp-gradle-plugins/issues/400)
- \[Inline\] Support aliased plugins [\#397](https://github.com/kordamp/kordamp-gradle-plugins/issues/397)
- \[Inline\] Support including plugins without defining version number [\#396](https://github.com/kordamp/kordamp-gradle-plugins/issues/396)
- \[Inline\] Add a flag to disable the inline plugin [\#394](https://github.com/kordamp/kordamp-gradle-plugins/issues/394)
- \[Inline\] Add a flag to disable adapting task properties [\#393](https://github.com/kordamp/kordamp-gradle-plugins/issues/393)
- \[Inline\] Add a flag to disable plugin inlining [\#392](https://github.com/kordamp/kordamp-gradle-plugins/issues/392)
- \[Inline\] Add a flag to disable project regex expansion [\#391](https://github.com/kordamp/kordamp-gradle-plugins/issues/391)
- \[Base\] Rework DependencyHandler extension [\#390](https://github.com/kordamp/kordamp-gradle-plugins/issues/390)
- \[Base\] Let dependencyManagement block force versions of matching configured dependencies [\#389](https://github.com/kordamp/kordamp-gradle-plugins/issues/389)
- \[Base\] Rename dependencies DSL block to dependencyManagement [\#388](https://github.com/kordamp/kordamp-gradle-plugins/issues/388)
- \[IntegrationTest\] Add a flag to include output of test task in compileClasspath [\#387](https://github.com/kordamp/kordamp-gradle-plugins/issues/387)
- \[Testing\] Support a testJar task with `-tests` classifier [\#384](https://github.com/kordamp/kordamp-gradle-plugins/issues/384)
- \[Base\] Display plugin version when invoking `plugins` task [\#383](https://github.com/kordamp/kordamp-gradle-plugins/issues/383)

**Fixed bugs:**

- \[Licensing\] MergeStrategy does not work [\#399](https://github.com/kordamp/kordamp-gradle-plugins/issues/399)
- \[Base\] EffectiveSettings task no longer displays the `info` section [\#398](https://github.com/kordamp/kordamp-gradle-plugins/issues/398)
- \[Inline\] Expanded project regexes cannot handle arguments [\#395](https://github.com/kordamp/kordamp-gradle-plugins/issues/395)
- Gradle deprecation warning with insight plugin. [\#381](https://github.com/kordamp/kordamp-gradle-plugins/issues/381)
- \[licensing\] InvalidUserDataException with missing LICENSE\_HEADER despite enabled = false [\#331](https://github.com/kordamp/kordamp-gradle-plugins/issues/331)

**Closed issues:**

- \[Sonar\] Align properties with Maven plugin [\#406](https://github.com/kordamp/kordamp-gradle-plugins/issues/406)
- \[Sonar\] Review property overrides [\#405](https://github.com/kordamp/kordamp-gradle-plugins/issues/405)

**Merged pull requests:**

- Fix guide documentation to go along with c3fbe2c0 [\#380](https://github.com/kordamp/kordamp-gradle-plugins/pull/380) ([ursjoss](https://github.com/ursjoss))

## [v0.40.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.40.0) (2020-10-04)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.39.0...v0.40.0)

**Implemented enhancements:**

- \[Base\] allow property/provider resolution to be prioritized [\#379](https://github.com/kordamp/kordamp-gradle-plugins/issues/379)
- \[Dokka\] Upgrade to dokka 1.4.10 [\#378](https://github.com/kordamp/kordamp-gradle-plugins/issues/378)
- \[Testing\] Add a time threshold for flagging slow tests [\#377](https://github.com/kordamp/kordamp-gradle-plugins/issues/377)
- Improve documentation of parameters [\#374](https://github.com/kordamp/kordamp-gradle-plugins/issues/374)
- \[Base\] Support enum property states [\#371](https://github.com/kordamp/kordamp-gradle-plugins/issues/371)
- Don't fail with validation errors when importing into IDEA [\#370](https://github.com/kordamp/kordamp-gradle-plugins/issues/370)
- \[Testing\] Allow color scheme customizations [\#369](https://github.com/kordamp/kordamp-gradle-plugins/issues/369)
- Add a build summary similar to Maven's reactor [\#368](https://github.com/kordamp/kordamp-gradle-plugins/issues/368)
- \[Inline\] Support expanding project targets with regex [\#367](https://github.com/kordamp/kordamp-gradle-plugins/issues/367)
- \[License\] Check license headers for Maven files [\#366](https://github.com/kordamp/kordamp-gradle-plugins/issues/366)
- \[guide\] List which plugins applies a plugin [\#365](https://github.com/kordamp/kordamp-gradle-plugins/issues/365)
- \[Toolchains\] Support Maven style toolchains [\#360](https://github.com/kordamp/kordamp-gradle-plugins/issues/360)

**Fixed bugs:**

- UnsupportedOperationException when using Kotlin listOf for person roles [\#373](https://github.com/kordamp/kordamp-gradle-plugins/issues/373)
- \[Base\] ListState does not handle empty value as it should [\#372](https://github.com/kordamp/kordamp-gradle-plugins/issues/372)
- Deactivate external package lists for dokka [\#364](https://github.com/kordamp/kordamp-gradle-plugins/issues/364)
- No test report for integrationTest when they fail [\#362](https://github.com/kordamp/kordamp-gradle-plugins/issues/362)

**Closed issues:**

- Remove deprecated code [\#376](https://github.com/kordamp/kordamp-gradle-plugins/issues/376)
- Publishing plugin does not add Javadoc [\#358](https://github.com/kordamp/kordamp-gradle-plugins/issues/358)
- Sonarqube coverage is 0% [\#357](https://github.com/kordamp/kordamp-gradle-plugins/issues/357)

## [v0.39.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.39.0) (2020-06-28)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.38.0...v0.39.0)

**Implemented enhancements:**

- \[Inline\] Support adapting plugin properties [\#346](https://github.com/kordamp/kordamp-gradle-plugins/issues/346)
- \[Inline\] Support invocation of inline plugins [\#344](https://github.com/kordamp/kordamp-gradle-plugins/issues/344)
- \[Plugin\] Conditionally disable plugin-marker publication [\#117](https://github.com/kordamp/kordamp-gradle-plugins/issues/117)

**Fixed bugs:**

- \[Plugin\] Do not enhance pluginMarker publication [\#355](https://github.com/kordamp/kordamp-gradle-plugins/issues/355)
- \[Javadoc\] JavadocJar is not published with main publication [\#354](https://github.com/kordamp/kordamp-gradle-plugins/issues/354)
- \[Bom\] Publication should not include -sources nor -javadoc artifacts [\#353](https://github.com/kordamp/kordamp-gradle-plugins/issues/353)
- \[Base\] buildInfo section is not visible [\#352](https://github.com/kordamp/kordamp-gradle-plugins/issues/352)
- \[Bom\] Typos found in map output [\#351](https://github.com/kordamp/kordamp-gradle-plugins/issues/351)
- \[Cpd\] Typos found in map output [\#350](https://github.com/kordamp/kordamp-gradle-plugins/issues/350)
- \[Base\] NPE in PropertyUtils.resolvePath when argument is null [\#349](https://github.com/kordamp/kordamp-gradle-plugins/issues/349)
- \[Base\] AbstactReportingTask prints out empty values when it shouldn't [\#348](https://github.com/kordamp/kordamp-gradle-plugins/issues/348)
- \[Base\] PropertyUtils.toProperty does not honor case [\#347](https://github.com/kordamp/kordamp-gradle-plugins/issues/347)
- \[Plugin\] Cannot handle multiple plugin identifiers [\#151](https://github.com/kordamp/kordamp-gradle-plugins/issues/151)

**Closed issues:**

- \[Plugin\] Unclear gradle-plugin attribute settings for Bintray [\#164](https://github.com/kordamp/kordamp-gradle-plugins/issues/164)

## [v0.38.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.38.0) (2020-06-07)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.37.1...v0.38.0)

**Implemented enhancements:**

- \[Jacoco\] Add a flag to exercise projects defined as dependencies [\#338](https://github.com/kordamp/kordamp-gradle-plugins/issues/338)

**Fixed bugs:**

- \[Base\] NPE in EffectiveSettingsTask when querying for an invisible section [\#339](https://github.com/kordamp/kordamp-gradle-plugins/issues/339)
- \[Jacoco\] Integration, Functional, and aggregate reports don't have any classes [\#337](https://github.com/kordamp/kordamp-gradle-plugins/issues/337)
- \[Base\] Testing plugin shows as disabled even if there are tests [\#336](https://github.com/kordamp/kordamp-gradle-plugins/issues/336)
- \[Sonar\] Sonarcloud reports 0% coverage even though local reports have coverage [\#335](https://github.com/kordamp/kordamp-gradle-plugins/issues/335)

**Closed issues:**

- Update code snippets in guide [\#340](https://github.com/kordamp/kordamp-gradle-plugins/issues/340)

## [0.37.1](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.37.1) (2020-05-31)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.37.0...0.37.1)

**Fixed bugs:**

- \[detekt\] configuration broken in 0.37.0 [\#332](https://github.com/kordamp/kordamp-gradle-plugins/issues/332)
- \[detekt\] Fix resolveConfigFile in static context [\#333](https://github.com/kordamp/kordamp-gradle-plugins/pull/333) ([ursjoss](https://github.com/ursjoss))

## [v0.37.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.37.0) (2020-05-30)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.36.0...v0.37.0)

**Implemented enhancements:**

- \[License\] Add support for TOML and YAML files [\#330](https://github.com/kordamp/kordamp-gradle-plugins/issues/330)
- \[Stats\] Add support for TOML files [\#329](https://github.com/kordamp/kordamp-gradle-plugins/issues/329)
- \[Base\] EffectiveSettings should only display information for configured plugins [\#327](https://github.com/kordamp/kordamp-gradle-plugins/issues/327)
- \[CPD\] Support Cpd [\#325](https://github.com/kordamp/kordamp-gradle-plugins/issues/325)
- \[Base\] Refactor duplicate definition of Detekt version [\#324](https://github.com/kordamp/kordamp-gradle-plugins/issues/324)
- Provide aliases for commonly used Maven goals [\#322](https://github.com/kordamp/kordamp-gradle-plugins/issues/322)
- \[Properties\] Support Yaml and Toml property sources [\#321](https://github.com/kordamp/kordamp-gradle-plugins/issues/321)
- Remove effectiveConfig [\#320](https://github.com/kordamp/kordamp-gradle-plugins/issues/320)
- \[Pmd\] Support sourceSetName aware config files [\#318](https://github.com/kordamp/kordamp-gradle-plugins/issues/318)
- \[Detekt\] Support sourceSetName aware config files [\#317](https://github.com/kordamp/kordamp-gradle-plugins/issues/317)
- \[Checkstyle\] Support sourceSetName aware config files [\#316](https://github.com/kordamp/kordamp-gradle-plugins/issues/316)
- \[Minpom\] Take platforms into account [\#315](https://github.com/kordamp/kordamp-gradle-plugins/issues/315)
- \[Publishing\] Publish only compile + runtime dependencies by default [\#312](https://github.com/kordamp/kordamp-gradle-plugins/issues/312)
- \[Base\] DSL for central dependency configuration [\#311](https://github.com/kordamp/kordamp-gradle-plugins/issues/311)
- \[Publishing\] Collect versions as version expressions in \<properties\> [\#310](https://github.com/kordamp/kordamp-gradle-plugins/issues/310)
- \[Bom\] Collect versions as version expressions in \<properties\> [\#309](https://github.com/kordamp/kordamp-gradle-plugins/issues/309)
- \[Bom\] Do not define scope [\#308](https://github.com/kordamp/kordamp-gradle-plugins/issues/308)
- \[Publishing\] Add support for \<properties\> in genererated POM [\#306](https://github.com/kordamp/kordamp-gradle-plugins/issues/306)
- \[Bom\] Add support for \<properties\> in genererated POM [\#305](https://github.com/kordamp/kordamp-gradle-plugins/issues/305)
- \[Codenarc\] Improving flexibility of CodeNarc plugin [\#304](https://github.com/kordamp/kordamp-gradle-plugins/issues/304)
- \[JavaProject\] Add a task to display configured platforms [\#303](https://github.com/kordamp/kordamp-gradle-plugins/issues/303)
- \[JavaProject\] Simplify platform dependency configuration [\#302](https://github.com/kordamp/kordamp-gradle-plugins/issues/302)
- Bump detekt from 1.8.0 to 1.9.1 [\#323](https://github.com/kordamp/kordamp-gradle-plugins/pull/323) ([ursjoss](https://github.com/ursjoss))

**Fixed bugs:**

- License badge link on main page is broken [\#328](https://github.com/kordamp/kordamp-gradle-plugins/issues/328)
- \[Base\] TaskSettings should print out values of Property/Provider elements [\#326](https://github.com/kordamp/kordamp-gradle-plugins/issues/326)
- \[Jar\] Calculating ClassPath manifest entry needs update [\#314](https://github.com/kordamp/kordamp-gradle-plugins/issues/314)
- \[Jar\] JAR Manifest can't be customized due to missing action configuration [\#313](https://github.com/kordamp/kordamp-gradle-plugins/issues/313)
- \[Base\] Improvement/explanation of projects DSL [\#307](https://github.com/kordamp/kordamp-gradle-plugins/issues/307)
- In Groovydoc plugin link to Groovy API is hardcoded to version 2.5.6 [\#301](https://github.com/kordamp/kordamp-gradle-plugins/issues/301)

**Closed issues:**

- Alternative to afterEvaluate [\#319](https://github.com/kordamp/kordamp-gradle-plugins/issues/319)

## [v0.36.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.36.0) (2020-05-09)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.35.0...v0.36.0)

**Implemented enhancements:**

- \[Profiles\] Support multiple activations per profile [\#299](https://github.com/kordamp/kordamp-gradle-plugins/issues/299)
- \[Settings\] support long project paths [\#298](https://github.com/kordamp/kordamp-gradle-plugins/issues/298)
- \[Base\] Add varargs variants for dirs\(\) and paths\(\) in projects DSL [\#295](https://github.com/kordamp/kordamp-gradle-plugins/issues/295)
- \[Settings\] Add varargs variants for dirs\(\) and paths\(\) [\#294](https://github.com/kordamp/kordamp-gradle-plugins/issues/294)
- \[Guide\] upgrade asciidoctorj-tabbed-code-extension to latest [\#292](https://github.com/kordamp/kordamp-gradle-plugins/issues/292)

**Fixed bugs:**

- Can't import this project into IDEA 2020.1 on windows 10 [\#296](https://github.com/kordamp/kordamp-gradle-plugins/issues/296)
- \[Publishing\] PluginMarker publication should not be enhanced [\#293](https://github.com/kordamp/kordamp-gradle-plugins/issues/293)
- groovydocJar tasks produce Gradle warnings messages for Gradle 6.3 [\#291](https://github.com/kordamp/kordamp-gradle-plugins/issues/291)

**Merged pull requests:**

- disable fail-fast in github actions [\#300](https://github.com/kordamp/kordamp-gradle-plugins/pull/300) ([kortov](https://github.com/kortov))

## [v0.35.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.35.0) (2020-05-02)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.34.0...v0.35.0)

**Implemented enhancements:**

- \[Minpom\] Register minpom task and disable it by default [\#290](https://github.com/kordamp/kordamp-gradle-plugins/issues/290)
- \[Jar\] Only update JAR metaInf/manifest if config.artifacts.jar is enabled [\#289](https://github.com/kordamp/kordamp-gradle-plugins/issues/289)
- \[Profiles\] Support Maven like profiles [\#288](https://github.com/kordamp/kordamp-gradle-plugins/issues/288)
- better sonar integration [\#287](https://github.com/kordamp/kordamp-gradle-plugins/issues/287)
- \[Bom\] Add an includes list [\#286](https://github.com/kordamp/kordamp-gradle-plugins/issues/286)
- \[Publishing\] Let packaging be configurable [\#285](https://github.com/kordamp/kordamp-gradle-plugins/issues/285)
- kordamp.org via HTTPS [\#283](https://github.com/kordamp/kordamp-gradle-plugins/issues/283)
- \[Settings\] Make subproject's folders more meaningful [\#237](https://github.com/kordamp/kordamp-gradle-plugins/issues/237)

## [v0.34.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.34.0) (2020-04-25)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.33.0...v0.34.0)

**Implemented enhancements:**

- \[Base\] Move Jar, Source and Minpom to a new aggregating section \(artifacts\) [\#281](https://github.com/kordamp/kordamp-gradle-plugins/issues/281)
- \[Jar\] Additional properties for Class-Path manifest entry [\#280](https://github.com/kordamp/kordamp-gradle-plugins/issues/280)
- \[BuildInfo\] Add an entry for Build-Os [\#279](https://github.com/kordamp/kordamp-gradle-plugins/issues/279)
- \[Functional\] Let the test directory be configurable [\#278](https://github.com/kordamp/kordamp-gradle-plugins/issues/278)
- \[Integration\] Let the test directory be configurable [\#277](https://github.com/kordamp/kordamp-gradle-plugins/issues/277)
- \[BOM\] Support dependencies in scope = import [\#276](https://github.com/kordamp/kordamp-gradle-plugins/issues/276)

**Merged pull requests:**

- Bump detekt from 1.7.2 to 1.8.0 [\#282](https://github.com/kordamp/kordamp-gradle-plugins/pull/282) ([ursjoss](https://github.com/ursjoss))

## [v0.33.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.33.0) (2020-03-28)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.32.0...v0.33.0)

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
- \[267\] - temporarily downgrade version plugin to 2.11.0 [\#269](https://github.com/kordamp/kordamp-gradle-plugins/pull/269) ([ursjoss](https://github.com/ursjoss))
- \[kotlindoc\] Fix copy/paste issue [\#268](https://github.com/kordamp/kordamp-gradle-plugins/pull/268) ([ursjoss](https://github.com/ursjoss))
- \[kotlindoc\] Improvements [\#265](https://github.com/kordamp/kordamp-gradle-plugins/pull/265) ([ursjoss](https://github.com/ursjoss))
- Fix ant-patterns in guide [\#264](https://github.com/kordamp/kordamp-gradle-plugins/pull/264) ([ursjoss](https://github.com/ursjoss))
- Bump detekt from 1.5.0 to 1.6.0 [\#263](https://github.com/kordamp/kordamp-gradle-plugins/pull/263) ([ursjoss](https://github.com/ursjoss))
- upgraded to the latest Coveralls plugin [\#239](https://github.com/kordamp/kordamp-gradle-plugins/pull/239) ([musketyr](https://github.com/musketyr))

## [v0.32.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.32.0) (2020-02-01)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.31.2...v0.32.0)

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
- Improve support for integration/functional test written in kotlin [\#188](https://github.com/kordamp/kordamp-gradle-plugins/pull/188) ([ursjoss](https://github.com/ursjoss))

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

## [0.31.2](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.31.2) (2020-01-04)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.31.1...0.31.2)

**Fixed bugs:**

- Problem with sourceJar after upgrade to 0.31.1 [\#235](https://github.com/kordamp/kordamp-gradle-plugins/issues/235)
- problem with subproject of the same name [\#215](https://github.com/kordamp/kordamp-gradle-plugins/issues/215)

## [0.31.1](https://github.com/kordamp/kordamp-gradle-plugins/tree/0.31.1) (2020-01-01)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.31.0...0.31.1)

**Fixed bugs:**

- \[Javadoc\] JavadocJar is not added to main publication [\#234](https://github.com/kordamp/kordamp-gradle-plugins/issues/234)

## [v0.31.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.31.0) (2020-01-01)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/0.30.4...v0.31.0)

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

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.30.0...0.30.2)

**Fixed bugs:**

- \[Plugin\] `{pluginName}PluginMarkerMaven` publication is not properly configured [\#167](https://github.com/kordamp/kordamp-gradle-plugins/issues/167)
- \[Publishing\] Dependencies are not added to generated POM [\#202](https://github.com/kordamp/kordamp-gradle-plugins/issues/202)

## [v0.30.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.30.0) (2019-11-29)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.29.0...v0.30.0)

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

**Fixed bugs:**

- 0.30.0-Beta breaks an integration test scenario by not seeing the main source set [\#199](https://github.com/kordamp/kordamp-gradle-plugins/issues/199)
- Problem with `gradlew jacocoRootMerge` with gradle project using kotlin DSL [\#198](https://github.com/kordamp/kordamp-gradle-plugins/issues/198)
- 0.30.0-SNAPSHOT breaks dependency resolution between modules [\#195](https://github.com/kordamp/kordamp-gradle-plugins/issues/195)

**Closed issues:**

- Exception in Intellij when using IntegrationTestPlugin together with the IdeaPlugin [\#189](https://github.com/kordamp/kordamp-gradle-plugins/issues/189)

**Merged pull requests:**

- Remove the IdeaPlugin based configuration for integration/functional tests to avoid an exception in IntelliJ [\#190](https://github.com/kordamp/kordamp-gradle-plugins/pull/190) ([ursjoss](https://github.com/ursjoss))

## [v0.29.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.29.0) (2019-10-26)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.28.0...v0.29.0)

**Fixed bugs:**

- \[Scaladoc\] ScaladocJar is not attached to publication [\#182](https://github.com/kordamp/kordamp-gradle-plugins/issues/182)
- \[Kotlindoc\] KotlindocJar is not attached to publication [\#181](https://github.com/kordamp/kordamp-gradle-plugins/issues/181)
- \[Groovydoc\] GroovydocJar is not attached to publication [\#180](https://github.com/kordamp/kordamp-gradle-plugins/issues/180)

## [v0.28.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.28.0) (2019-10-19)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.27.0...v0.28.0)

**Fixed bugs:**

- Javadoc JAR is not published [\#178](https://github.com/kordamp/kordamp-gradle-plugins/issues/178)
- Blank vendor is giving errors when organization.name is not blank [\#177](https://github.com/kordamp/kordamp-gradle-plugins/issues/177)

## [v0.27.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.27.0) (2019-09-27)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.26.0...v0.27.0)

**Fixed bugs:**

- \[Javadoc\] MPE when Groovydoc.replaceJavadoc is enabled [\#174](https://github.com/kordamp/kordamp-gradle-plugins/issues/174)
- \[Javadoc\] Normalize path for local URLs in AutoLinks.calculateLocalJavadocLink [\#171](https://github.com/kordamp/kordamp-gradle-plugins/issues/171)
- Fix setting `-Xdoclint:none` and `-quiet` options for Javadoc [\#172](https://github.com/kordamp/kordamp-gradle-plugins/pull/172) ([tlinkowski](https://github.com/tlinkowski))

## [v0.26.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.26.0) (2019-08-30)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.25.0...v0.26.0)

**Implemented enhancements:**

- \[Javadoc\] AutoLinks fail on multi-project builds [\#169](https://github.com/kordamp/kordamp-gradle-plugins/issues/169)
- \[Base\] Add a task for displaying JavaExec settings [\#160](https://github.com/kordamp/kordamp-gradle-plugins/issues/160)
- \[Kotlindoc\] Allow aggregation [\#80](https://github.com/kordamp/kordamp-gradle-plugins/issues/80)
- Simplify configuration example for Kotlin DSL for release [\#179](https://github.com/kordamp/kordamp-gradle-plugins/pull/179) ([ursjoss](https://github.com/ursjoss))
- \[Javadoc\] Add 'api' configuration to DEFAULT\_CONFIGURATIONS for AutoLinks [\#168](https://github.com/kordamp/kordamp-gradle-plugins/pull/168) ([tlinkowski](https://github.com/tlinkowski))

**Fixed bugs:**

- \[BuildInfo, Jar\] Disabling buildInfo doesn't skip manifest attribute insertion [\#163](https://github.com/kordamp/kordamp-gradle-plugins/issues/163)
- \[Kotlindoc\] Default output directory is incorrect for multi-project builds [\#161](https://github.com/kordamp/kordamp-gradle-plugins/issues/161)
- \[Kotlindoc\] GroovyCastException for kotlindoc.externalDocumentationLinks [\#155](https://github.com/kordamp/kordamp-gradle-plugins/issues/155)
- \[Plugin\] Apply BasePlugin if missing [\#165](https://github.com/kordamp/kordamp-gradle-plugins/pull/165) ([tlinkowski](https://github.com/tlinkowski))

**Merged pull requests:**

- Document applying org.kordamp.gradle.jacoco plugin [\#162](https://github.com/kordamp/kordamp-gradle-plugins/pull/162) ([tlinkowski](https://github.com/tlinkowski))

## [v0.25.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.25.0) (2019-07-30)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.24.0...v0.25.0)

**Implemented enhancements:**

- \[Publishing\] Add a publications property [\#159](https://github.com/kordamp/kordamp-gradle-plugins/issues/159)
- Use project.pluginManager instead of project.plugins whenever possible [\#157](https://github.com/kordamp/kordamp-gradle-plugins/issues/157)

**Fixed bugs:**

- \[Jar\] NPE when resolving effectiveConfig on rootProject [\#153](https://github.com/kordamp/kordamp-gradle-plugins/issues/153)
- \[Base\] info.people.person is missing methods to configure its organization [\#152](https://github.com/kordamp/kordamp-gradle-plugins/issues/152)

**Closed issues:**

- \[Base\] Register sourceSet tasks only when `java-base` plugin is applied [\#158](https://github.com/kordamp/kordamp-gradle-plugins/issues/158)

**Merged pull requests:**

- Add `maxDepth\(1\)` to `walkTopDown\(\)` [\#154](https://github.com/kordamp/kordamp-gradle-plugins/pull/154) ([tlinkowski](https://github.com/tlinkowski))

## [v0.24.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.24.0) (2019-07-08)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.23.0...v0.24.0)

**Implemented enhancements:**

- \[Base\] Add a task for listing configurations [\#148](https://github.com/kordamp/kordamp-gradle-plugins/issues/148)
- \[Base\] Add a task for displaying Configuration settings [\#147](https://github.com/kordamp/kordamp-gradle-plugins/issues/147)
- \[Base\] Add a task for displaying SourceSet configuration [\#146](https://github.com/kordamp/kordamp-gradle-plugins/issues/146)
- \[Base\] Add a task for listing sourceSets [\#145](https://github.com/kordamp/kordamp-gradle-plugins/issues/145)
- \[Licensing\] Add mapping for Kotlin files [\#136](https://github.com/kordamp/kordamp-gradle-plugins/issues/136)

**Fixed bugs:**

- \[Kotlindoc\] NPE during project configuration [\#150](https://github.com/kordamp/kordamp-gradle-plugins/issues/150)
-  \[Base\] TestSettingsTask throws NPE if given sourceSet name does not exist [\#144](https://github.com/kordamp/kordamp-gradle-plugins/issues/144)
- \[Base\] GroovyCompilerSettingsTask throws NPE if given sourceSet name does not exist [\#143](https://github.com/kordamp/kordamp-gradle-plugins/issues/143)
- \[Base\] JavaCompilerSettingsTask throws NPE if given sourceSet name does not exist [\#142](https://github.com/kordamp/kordamp-gradle-plugins/issues/142)
-  \[functional-test\] MPE when Groovy plugin is applied [\#141](https://github.com/kordamp/kordamp-gradle-plugins/issues/141)
- \[integration-test\] MPE when Groovy plugin is applied [\#140](https://github.com/kordamp/kordamp-gradle-plugins/issues/140)

**Merged pull requests:**

- Documented applying com.github.ben-manes.versions [\#156](https://github.com/kordamp/kordamp-gradle-plugins/pull/156) ([tlinkowski](https://github.com/tlinkowski))
- Fixed a typo in "licenseFormatGradle" [\#149](https://github.com/kordamp/kordamp-gradle-plugins/pull/149) ([tlinkowski](https://github.com/tlinkowski))

## [v0.23.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.23.0) (2019-07-01)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.22.0...v0.23.0)

**Implemented enhancements:**

- \[Licensing\] Add mappings and a task for Gradle build files [\#137](https://github.com/kordamp/kordamp-gradle-plugins/issues/137)
- \[Scaladoc\] Customize classifier [\#130](https://github.com/kordamp/kordamp-gradle-plugins/issues/130)

**Fixed bugs:**

- \[Base\] Redundant config extension merging [\#134](https://github.com/kordamp/kordamp-gradle-plugins/issues/134)
- \[Plugin\] Unwanted plugin declaration added [\#132](https://github.com/kordamp/kordamp-gradle-plugins/issues/132)

**Merged pull requests:**

- Fixed a minor typo [\#138](https://github.com/kordamp/kordamp-gradle-plugins/pull/138) ([tlinkowski](https://github.com/tlinkowski))
- \[base\] Fixed org.kordamp.gradle.plugin.base.plugins.Plugin.merge [\#133](https://github.com/kordamp/kordamp-gradle-plugins/pull/133) ([tlinkowski](https://github.com/tlinkowski))

## [v0.22.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.22.0) (2019-06-15)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.21.0...v0.22.0)

**Implemented enhancements:**

- \[Base\] Map all POM elements [\#129](https://github.com/kordamp/kordamp-gradle-plugins/issues/129)
- \[Base\] Display Java compiler settings [\#118](https://github.com/kordamp/kordamp-gradle-plugins/issues/118)

**Fixed bugs:**

- \[Plugin\] Exception occurs with v0.21.0 [\#128](https://github.com/kordamp/kordamp-gradle-plugins/issues/128)

**Closed issues:**

- Remove deprecated plugins [\#127](https://github.com/kordamp/kordamp-gradle-plugins/issues/127)

## [v0.21.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.21.0) (2019-06-04)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.20.0...v0.21.0)

**Implemented enhancements:**

- \[Base\] Make secrets list configurable [\#126](https://github.com/kordamp/kordamp-gradle-plugins/issues/126)
- Support adding/removing of pre-/postfixes for build.gradle filenames [\#125](https://github.com/kordamp/kordamp-gradle-plugins/issues/125)
- Enhance the signing activation [\#124](https://github.com/kordamp/kordamp-gradle-plugins/issues/124)
- Mask Secrets in projectProperties [\#123](https://github.com/kordamp/kordamp-gradle-plugins/issues/123)

**Fixed bugs:**

- \[Base\] CME when showing paths in compiler insight tasks [\#122](https://github.com/kordamp/kordamp-gradle-plugins/issues/122)

## [v0.20.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.20.0) (2019-05-13)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.19.0...v0.20.0)

**Implemented enhancements:**

- \[Publishing\] Display publication settings [\#121](https://github.com/kordamp/kordamp-gradle-plugins/issues/121)
- \[Base\] Display Test task compiler settings [\#120](https://github.com/kordamp/kordamp-gradle-plugins/issues/120)
- \[Base\] Display Groovy compiler settings  [\#119](https://github.com/kordamp/kordamp-gradle-plugins/issues/119)

## [v0.19.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.19.0) (2019-04-24)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.18.0...v0.19.0)

**Fixed bugs:**

- \[Plugin\] POM file is missing information [\#116](https://github.com/kordamp/kordamp-gradle-plugins/issues/116)

## [v0.18.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.18.0) (2019-04-20)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.17.0...v0.18.0)

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

## [v0.17.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.17.0) (2019-04-11)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.16.0...v0.17.0)

**Implemented enhancements:**

- \[Javadoc\] Automatically calculate javadoc links based on dependencies [\#104](https://github.com/kordamp/kordamp-gradle-plugins/issues/104)

**Fixed bugs:**

- \[Javadoc\] Additional links are not honored [\#103](https://github.com/kordamp/kordamp-gradle-plugins/issues/103)
- \[Javadoc\] Custom properties are ignored [\#102](https://github.com/kordamp/kordamp-gradle-plugins/issues/102)

**Closed issues:**

- \[functional-test\] Deprecate usages of compile/runtime test configurations [\#106](https://github.com/kordamp/kordamp-gradle-plugins/issues/106)
- \[integration-test\] Deprecate usages of compile/runtime test configurations [\#105](https://github.com/kordamp/kordamp-gradle-plugins/issues/105)

## [v0.16.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.16.0) (2019-04-02)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.15.0...v0.16.0)

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

- \[Base\] Honor console settings when printing reach output [\#93](https://github.com/kordamp/kordamp-gradle-plugins/issues/93)
- \[Publishing\] Generated POM files are missing information [\#91](https://github.com/kordamp/kordamp-gradle-plugins/issues/91)
- \[Settings\] Plugin doesn't seem to work in Kotlin DSL [\#90](https://github.com/kordamp/kordamp-gradle-plugins/issues/90)

**Closed issues:**

- Feature request: support for creating Gradle SuperPOM [\#96](https://github.com/kordamp/kordamp-gradle-plugins/issues/96)

**Merged pull requests:**

- Add lines setting root project name in sample settings.gradle\(.kts\) [\#89](https://github.com/kordamp/kordamp-gradle-plugins/pull/89) ([tlinkowski](https://github.com/tlinkowski))

## [v0.15.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.15.0) (2019-03-11)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.14.0...v0.15.0)

**Implemented enhancements:**

- Feature request: plugin for the contents of settings.gradle\(.kts\) [\#88](https://github.com/kordamp/kordamp-gradle-plugins/issues/88)
- \[Scaladoc\] Include scaladoc JARs in publication [\#81](https://github.com/kordamp/kordamp-gradle-plugins/issues/81)

**Fixed bugs:**

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

## [v0.14.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.14.0) (2019-02-28)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.13.0...v0.14.0)

**Implemented enhancements:**

- Add a task for aggregating functional test reports [\#76](https://github.com/kordamp/kordamp-gradle-plugins/issues/76)
- Add a task for aggregating integration test reports [\#75](https://github.com/kordamp/kordamp-gradle-plugins/issues/75)
- Add a task for aggregating unit test reports [\#74](https://github.com/kordamp/kordamp-gradle-plugins/issues/74)
- \[Base\] Report tasks should use ANSI colors in their outpus [\#73](https://github.com/kordamp/kordamp-gradle-plugins/issues/73)
- \[Stats\] Include Kotlin Script in the default counter settings [\#72](https://github.com/kordamp/kordamp-gradle-plugins/issues/72)

## [v0.13.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.13.0) (2019-01-29)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.12.0...v0.13.0)

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

## [v0.12.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.12.0) (2018-12-31)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.11.0...v0.12.0)

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

## [v0.11.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.11.0) (2018-12-20)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.10.0...v0.11.0)

**Implemented enhancements:**

- \[Base\] Add a task that displays configured properties [\#52](https://github.com/kordamp/kordamp-gradle-plugins/issues/52)
- \[Base\] Add a task that displays applied extensions [\#51](https://github.com/kordamp/kordamp-gradle-plugins/issues/51)
- \[Base\] Add a task that displays applied plugins [\#50](https://github.com/kordamp/kordamp-gradle-plugins/issues/50)
- \[Base\] Add a task that displays configured repositories [\#49](https://github.com/kordamp/kordamp-gradle-plugins/issues/49)
- Consider making the build reproducible by default [\#47](https://github.com/kordamp/kordamp-gradle-plugins/issues/47)

## [v0.10.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.10.0) (2018-12-08)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.9.0...v0.10.0)

**Implemented enhancements:**

- \[License\] Allow non FLOSS licenses to be specified [\#42](https://github.com/kordamp/kordamp-gradle-plugins/issues/42)

**Fixed bugs:**

- Missing documentation on config.release flag [\#48](https://github.com/kordamp/kordamp-gradle-plugins/issues/48)
- \[Bintray\] Plugin is disabled by defualt on subprojects [\#46](https://github.com/kordamp/kordamp-gradle-plugins/issues/46)

## [v0.9.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.9.0) (2018-11-23)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.8.0...v0.9.0)

**Implemented enhancements:**

- \[Base\] Suppor the SCM block in POM files [\#45](https://github.com/kordamp/kordamp-gradle-plugins/issues/45)
- Split project plugin for closed and open source usages [\#43](https://github.com/kordamp/kordamp-gradle-plugins/issues/43)
- \[Guide\] Allow attribute values to be overriden in a document [\#41](https://github.com/kordamp/kordamp-gradle-plugins/issues/41)
- \[Publishing\] Allow posting publications to release/snapshot repositories [\#39](https://github.com/kordamp/kordamp-gradle-plugins/issues/39)
- \[Publishing\] Configure artifact signing [\#38](https://github.com/kordamp/kordamp-gradle-plugins/issues/38)

**Fixed bugs:**

- \[Jar\] Value for buildJdk is missing [\#40](https://github.com/kordamp/kordamp-gradle-plugins/issues/40)

## [v0.8.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.8.0) (2018-11-17)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.7.0...v0.8.0)

**Implemented enhancements:**

- Add a plugin for BOM file generation [\#44](https://github.com/kordamp/kordamp-gradle-plugins/issues/44)
- \[BuildInfo\] Allow buildTime to be set to 0 [\#37](https://github.com/kordamp/kordamp-gradle-plugins/issues/37)
- \[BuildInfo\] Add options to skip calculation of some properties [\#36](https://github.com/kordamp/kordamp-gradle-plugins/issues/36)
- Add a task to calculate the effective settings [\#35](https://github.com/kordamp/kordamp-gradle-plugins/issues/35)
- \[Bintray\] Add option to skip auto-sync with Maven Central [\#34](https://github.com/kordamp/kordamp-gradle-plugins/issues/34)

**Fixed bugs:**

- Error parsing SourceXref.javaVersion [\#33](https://github.com/kordamp/kordamp-gradle-plugins/issues/33)

## [v0.7.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.7.0) (2018-11-08)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.6.0...v0.7.0)

**Implemented enhancements:**

- Support Xref reports [\#32](https://github.com/kordamp/kordamp-gradle-plugins/issues/32)
- Support generation of Kotlin docs [\#31](https://github.com/kordamp/kordamp-gradle-plugins/issues/31)
- Groovydoc should include all Java sources [\#29](https://github.com/kordamp/kordamp-gradle-plugins/issues/29)
- SourceJar task should be a dependency of 'assemble' task [\#28](https://github.com/kordamp/kordamp-gradle-plugins/issues/28)
- GroovydocJar task should be a dependency of 'assemble' task [\#27](https://github.com/kordamp/kordamp-gradle-plugins/issues/27)
- JavadocJar task should be a dependency of 'assemble' task [\#26](https://github.com/kordamp/kordamp-gradle-plugins/issues/26)
- Provide Gradle Kotlin DSL examples in documentation [\#25](https://github.com/kordamp/kordamp-gradle-plugins/issues/25)
- Create a guide project [\#10](https://github.com/kordamp/kordamp-gradle-plugins/issues/10)

**Fixed bugs:**

- SourceStats task is not available on root project when there are no children [\#30](https://github.com/kordamp/kordamp-gradle-plugins/issues/30)

## [v0.6.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.6.0) (2018-10-31)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.5.0...v0.6.0)

**Implemented enhancements:**

- Support \<timezone\> and \<properties\> blocks for Developer/Contributor [\#23](https://github.com/kordamp/kordamp-gradle-plugins/issues/23)
- GuidePlugin should copy the aggregate source HTML report [\#22](https://github.com/kordamp/kordamp-gradle-plugins/issues/22)

**Fixed bugs:**

- SourceHtml plugin fails with projects that do not have /src/main/java directory [\#21](https://github.com/kordamp/kordamp-gradle-plugins/issues/21)

## [v0.5.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.5.0) (2018-10-09)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.4.0...v0.5.0)

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

## [v0.4.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.4.0) (2018-10-06)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.3.0...v0.4.0)

## [v0.3.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.3.0) (2018-10-04)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.2.0...v0.3.0)

**Implemented enhancements:**

- Project plugin does not propagate to children [\#2](https://github.com/kordamp/kordamp-gradle-plugins/issues/2)

**Fixed bugs:**

- License information is missing [\#1](https://github.com/kordamp/kordamp-gradle-plugins/issues/1)

## [v0.2.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.2.0) (2018-10-03)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/v0.1.0...v0.2.0)

## [v0.1.0](https://github.com/kordamp/kordamp-gradle-plugins/tree/v0.1.0) (2018-10-01)

[Full Changelog](https://github.com/kordamp/kordamp-gradle-plugins/compare/ad85f56a35eddff099d9efba5b1277214c500f57...v0.1.0)



\* *This Changelog was automatically generated by [github_changelog_generator](https://github.com/github-changelog-generator/github-changelog-generator)*
