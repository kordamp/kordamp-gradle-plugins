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
package org.kordamp.gradle.plugin.stats

import org.gradle.api.Project
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.scala.ScalaPlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Ignore
import spock.lang.Specification

/**
 * @author Andres Almiray
 */
@SuppressWarnings('MethodName')
@Ignore('Rewrite using TestKit properly')
class SourceStatsTaskSpec extends Specification {
    private static final String STATS = 'stats'

    Project project
    File testRootDir

    void "Calculate stats on basic Java project"() {
        given:
        testRootDir = new File('src/test/projects/basic_java')
        project = ProjectBuilder.builder().withName('test')
            .withProjectDir(testRootDir).build()
        project.apply(plugin: JavaPlugin)
        project.apply(plugin: SourceStatsPlugin)


        SourceStatsTask task = project.tasks.findByName(STATS)

        when:
        task.computeLoc()

        then:
        2 == task.totalFiles
        13 == task.totalLOC
    }

    void "Calculate stats on basic Scala project"() {
        given:
        testRootDir = new File('src/test/projects/basic_scala')
        project = ProjectBuilder.builder().withName('test')
            .withProjectDir(testRootDir).build()
        project.apply(plugin: ScalaPlugin)
        project.apply(plugin: SourceStatsPlugin)
        SourceStatsTask task = project.tasks.findByName(STATS)

        when:
        task.computeLoc()

        then:
        2 == task.totalFiles
        13 == task.totalLOC
    }

    void "Calculate stats on basic project"() {
        given:
        testRootDir = new File('src/test/projects/basic_all')
        project = ProjectBuilder.builder().withName('test')
            .withProjectDir(testRootDir).build()
        project.apply(plugin: JavaPlugin)
        project.apply(plugin: SourceStatsPlugin)
        SourceStatsTask task = project.tasks.findByName(STATS)

        when:
        task.computeLoc()

        then:
        12 == task.totalFiles
        44 == task.totalLOC
    }

    void "Calculate stats on basic Griffon project"() {
        given:
        testRootDir = new File('src/test/projects/basic_griffon')
        project = ProjectBuilder.builder().withName('test')
            .withProjectDir(testRootDir).build()
        project.apply(plugin: GroovyPlugin)
        project.apply(plugin: SourceStatsPlugin)
        project.sourceSets.main.groovy.srcDirs = [
            'griffon-app/conf',
            'griffon-app/controllers',
            'griffon-app/models',
            'griffon-app/views',
            'griffon-app/services',
            'griffon-app/lifecycle',
            'src/main/groovy'
        ]
        project.sourceSets.main.resources.srcDirs = [
            'griffon-app/resources',
            'griffon-app/i18n',
            'src/main/resources'
        ]
        project.sourceSets.maybeCreate('integrationTest').groovy.srcDirs = [
            'src/integration-test/groovy'
        ]

        SourceStatsTask task = project.tasks.findByName(STATS)
        task.paths = [
            model     : [name: 'Models', path: 'griffon-app/models'],
            view      : [name: 'Views', path: 'griffon-app/views'],
            controller: [name: 'Controllers', path: 'griffon-app/controllers'],
            service   : [name: 'Services', path: 'griffon-app/services'],
            config    : [name: 'Config', path: 'griffon-app/conf'],
            lifecycle : [name: 'Lifecycle', path: 'griffon-app/lifecycle']
        ]

        when:
        task.computeLoc()

        then:
        16 == task.totalFiles
        151 == task.totalLOC
    }
}
