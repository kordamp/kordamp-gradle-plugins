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
package org.kordamp.gradle.plugin.settings

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ProjectsExtensionSpec extends Specification {
    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    File settingsFile

    def setup() {
        settingsFile = testProjectDir.newFile('settings.gradle')
        settingsFile << """
            buildscript {
                repositories {
                    gradlePluginPortal()
                    flatDir { dirs ${System.getProperty('jars.dir')} }
                }
                dependencies {
                    classpath "org.kordamp.gradle:base-gradle-plugin:${System.getProperty('project.version')}"
                    classpath "org.kordamp.gradle:settings-gradle-plugin:${System.getProperty('project.version')}"
                }
            }
            apply plugin: 'org.kordamp.gradle.settings'
            rootProject.name = 'test'
        """
    }

    def "Verify two-level layout [#index]"() {
        given:
        File docs = testProjectDir.newFolder('docs')
        File subprojects = testProjectDir.newFolder('subprojects')
        // guide
        createProject(docs, 'guide', naming, kotlin)
        // projects
        createProject(subprojects, 'project1', naming, kotlin)
        createProject(subprojects, 'project2', naming, kotlin)
        createProject(subprojects, 'project3', naming, kotlin)

        when:
        settingsFile << """
            projects {
                layout = 'two-level'
                directories = ['docs', 'subprojects']
                enforceNamingConvention = $naming
            }
        """
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('projects')
            .build()

        then:
        assert result.output.contains("""
            |Root project 'test'
            |+--- Project ':guide'
            |+--- Project ':project1'
            |+--- Project ':project2'
            |\\--- Project ':project3'
        """.stripMargin('|').trim())

        where:
        index | naming | kotlin
        1     | false  | false
        2     | true   | false
        3     | false  | true
        4     | true   | true
    }

    def "Verify standard layout [#index]"() {
        given:
        // guide
        createBuildFile(testProjectDir.newFolder('guide'), 'guide', naming, kotlin)
        // projects
        createBuildFile(testProjectDir.newFolder('project1'), 'project1', naming, kotlin)
        createBuildFile(testProjectDir.newFolder('project2'), 'project2', naming, kotlin)
        createBuildFile(testProjectDir.newFolder('project3'), 'project3', naming, kotlin)

        when:
        settingsFile << """
            projects {
                layout = 'standard'
                enforceNamingConvention = $naming
            }
        """
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('projects')
            .build()

        then:
        assert result.output.contains("""
            |Root project 'test'
            |+--- Project ':guide'
            |+--- Project ':project1'
            |+--- Project ':project2'
            |\\--- Project ':project3'
        """.stripMargin('|').trim())

        where:
        index | naming | kotlin
        1     | false  | false
        2     | true   | false
        3     | false  | true
        4     | true   | true
    }

    def "Verify multi-level layout [#index]"() {
        given:
        // guide
        createBuildFile(testProjectDir.newFolder('guide'), 'guide', naming, kotlin)
        // projects
        File subprojects = testProjectDir.newFolder('subprojects')
        // projects
        createProject(subprojects, 'project1', naming, kotlin)
        createProject(subprojects, 'project2', naming, kotlin)
        createProject(subprojects, 'project3', naming, kotlin)

        when:
        settingsFile << """
            projects {
                layout = 'multi-level'
                enforceNamingConvention = $naming
                directories = [
                    'guide',
                    'subprojects/project1',
                    'subprojects/project2',
                    'subprojects/project3'
                ]
            }
        """
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('projects')
            .build()

        then:
        assert result.output.contains("""
            |Root project 'test'
            |+--- Project ':guide'
            |+--- Project ':project1'
            |+--- Project ':project2'
            |\\--- Project ':project3'
        """.stripMargin('|').trim())

        where:
        index | naming | kotlin
        1     | false  | false
        2     | true   | false
        3     | false  | true
        4     | true   | true
    }

    def "Verify explicit layout [#index]"() {
        given:
        // guide
        createBuildFile(testProjectDir.newFolder('guide'), 'guide', naming, kotlin)
        // projects
        File subprojects = testProjectDir.newFolder('subprojects')
        // projects
        createProject(subprojects, 'project1', naming, kotlin)
        createProject(subprojects, 'project2', naming, kotlin)
        createProject(subprojects, 'project3', naming, kotlin)

        when:
        settingsFile << """
            projects {
                layout = 'explicit'
                enforceNamingConvention = $naming
                includeProject('guide')
                includeProjects('subprojects')
            }
        """
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('projects')
            .build()

        then:
        assert result.output.contains("""
            |Root project 'test'
            |+--- Project ':guide'
            |+--- Project ':project1'
            |+--- Project ':project2'
            |\\--- Project ':project3'
        """.stripMargin('|').trim())

        where:
        index | naming | kotlin
        1     | false  | false
        2     | true   | false
        3     | false  | true
        4     | true   | true
    }

    def "Verify add filename transformation layout [#index]"() {
        given:
        File subprojects = testProjectDir.newFolder('subprojects')
        // projects
        createProject(subprojects, 'project1', prefix, suffix)
        createProject(subprojects, 'project2', prefix, suffix)
        createProject(subprojects, 'project3', prefix, suffix)

        when:
        settingsFile << """
            projects {
                layout = 'two-level'
                directories = ['docs', 'subprojects']
                enforceNamingConvention = true
                fileNameTransformation = 'add'
                prefix = "$prefix"
                suffix = "$suffix"
            }
        """
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('projects')
            .build()

        then:
        assert result.output.contains("""
            |Root project 'test'
            |+--- Project ':project1'
            |+--- Project ':project2'
            |\\--- Project ':project3'
        """.stripMargin('|').trim())

        where:
        index | prefix | suffix
        1     | ''     | ''
        2     | 'p-'   | ''
        3     | ''     | '-s'
        4     | 'p-'   | '-s'
    }

    def "Verify remove filename transformation layout [#index]"() {
        given:
        File subprojects = testProjectDir.newFolder('subprojects')
        // projects
        createProject(subprojects, prefix + 'project1' + suffix, 'project1')
        createProject(subprojects, prefix + 'project2' + suffix, 'project2')
        createProject(subprojects, prefix + 'project3' + suffix, 'project3')

        when:
        settingsFile << """
            projects {
                layout = 'two-level'
                directories = ['docs', 'subprojects']
                enforceNamingConvention = true
                fileNameTransformation = 'remove'
                prefix = "$prefix"
                suffix = "$suffix"
            }
        """
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('projects')
            .build()

        then:
        assert result.output.contains("""
            |Root project 'test'
            |+--- Project ':${prefix}project1${suffix}'
            |+--- Project ':${prefix}project2${suffix}'
            |\\--- Project ':${prefix}project3${suffix}'
        """.stripMargin('|').trim())

        where:
        index | prefix | suffix
        1     | ''     | ''
        2     | 'p-'   | ''
        3     | ''     | '-s'
        4     | 'p-'   | '-s'
    }

    def "Verify exclude project2 [#index]"() {
        given:
        File subprojects = testProjectDir.newFolder('subprojects')
        createProject(subprojects, 'project1', false, false)
        createProject(subprojects, 'project2', false, false)
        createProject(subprojects, 'project3', false, false)

        when:
        settingsFile << """
            projects {
                layout = 'explicit'
                enforceNamingConvention = false
                includeProjects('subprojects').exclude("${exclusion}")
            }
        """
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('projects')
            .build()

        then:
        assert result.output.contains("""
            |Root project 'test'
            |+--- Project ':project1'
            |\\--- Project ':project3'
        """.stripMargin('|').trim())

        where:
        index | exclusion
        1     | 'project2'
        2     | '**/project2'
        3     | '**/*t2'
    }

    private void createProject(File parentDir, String filename, boolean naming, boolean kotlin) {
        File projectDir = new File(parentDir, filename)
        projectDir.mkdirs()
        createBuildFile(projectDir, filename, naming, kotlin)
    }

    private void createProject(File parentDir, String filename, String prefix, String suffix) {
        File projectDir = new File(parentDir, filename)
        projectDir.mkdirs()
        createBuildFile(projectDir, filename, prefix, suffix)
    }

    private void createProject(File parentDir, String projectDirName, String filename) {
        File projectDir = new File(parentDir, projectDirName)
        projectDir.mkdirs()
        createBuildFile(projectDir, filename, '', '')
    }

    private void createBuildFile(File projectDir, String filename, boolean naming, boolean kotlin) {
        File buildFile = new File(projectDir, (naming ? filename : 'build') + '.gradle' + (kotlin ? '.kts' : ''))
        buildFile.createNewFile()
    }

    private void createBuildFile(File projectDir, String filename, String prefix, String suffix) {
        File buildFile = new File(projectDir, prefix + filename + suffix + '.gradle')
        buildFile.createNewFile()
        println buildFile.absolutePath
    }
}
