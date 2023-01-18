/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2023 Andres Almiray.
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
package org.kordamp.gradle.property

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class PropertyUtilsSpec extends Specification {
    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile
    File settingsFile

    def setup() {
        settingsFile = testProjectDir.newFile('settings.gradle')
        settingsFile << """
            rootProject.name = 'test'
        """

        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'base'
                id 'org.kordamp.gradle.base'
            }
        """
    }

    def "ENV_SYS_PROP with property and ENV_SYS_PROP and PROVIDER priority set resolves ENV"() {
        given:
        buildFile << """
            import org.kordamp.gradle.property.PropertyUtils

            System.setProperty('kordamp.property.priority', 'PROVIDER')
            ext.stringProperty = objects.property(String)
            stringProperty.set('property')
            ext.stringProvider = PropertyUtils.stringProvider(
                'foo.bar', stringProperty, project, project)
            assert stringProvider.get() == 'env'
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Pfoo.bar=prop',
                '-Dfoo.bar=sys')
            .withEnvironment(['FOO_BAR': 'env'])
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "ENV_SYS_PROP with property and ENV_SYS_PROP set resolves property"() {
        given:
        buildFile << """
            import org.kordamp.gradle.property.PropertyUtils

            ext.stringProperty = objects.property(String)
            stringProperty.set('property')
            ext.stringProvider = PropertyUtils.stringProvider(
                'foo.bar', stringProperty, project, project)
            assert stringProvider.get() == 'property'
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Pfoo.bar=prop',
                '-Dfoo.bar=sys')
            .withEnvironment(['FOO_BAR': 'env'])
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "ENV_SYS_PROP with unset property and ENV_SYS_PROP set resolves ENV"() {
        given:
        buildFile << """
            import org.kordamp.gradle.property.PropertyUtils
            
            ext.stringProperty = objects.property(String)
            ext.stringProvider = PropertyUtils.stringProvider(
                'foo.bar', stringProperty, project, project)
            assert stringProvider.get() == 'env'
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Pfoo.bar=prop',
                '-Dfoo.bar=sys')
            .withEnvironment(['FOO_BAR': 'env'])
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "ENV_SYS_PROP with unset property and SYS_PROP set resolves SYS"() {
        given:
        buildFile << """
            import org.kordamp.gradle.property.PropertyUtils
            
            ext.stringProperty = objects.property(String)
            ext.stringProvider = PropertyUtils.stringProvider(
                'foo.bar', stringProperty, project, project)
            assert stringProvider.get() == 'sys'
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Pfoo.bar=prop',
                '-Dfoo.bar=sys')
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "ENV_SYS_PROP with unset property and PROP set resolves PROP"() {
        given:
        buildFile << """
            import org.kordamp.gradle.property.PropertyUtils
            
            ext.stringProperty = objects.property(String)
            ext.stringProvider = PropertyUtils.stringProvider(
                'foo.bar', stringProperty, project, project)
            assert stringProvider.get() == 'prop'
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Pfoo.bar=prop')
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "ENV_PROP_SYS with unset property and ENV_PROP_SYS set resolves ENV"() {
        given:
        buildFile << """
            import org.kordamp.gradle.property.PropertyUtils
            import static org.kordamp.gradle.property.PropertyUtils.Order.*
            
            ext.stringProperty = objects.property(String)
            ext.stringProvider = PropertyUtils.stringProvider(
                'foo.bar', stringProperty, ENV_PROP_SYS, project, project)
            assert stringProvider.get() == 'env'
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Pfoo.bar=prop',
                '-Dfoo.bar=sys')
            .withEnvironment(['FOO_BAR': 'env'])
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "ENV_PROP_SYS with unset property and PROP_SYS set resolves PROP"() {
        given:
        buildFile << """
            import org.kordamp.gradle.property.PropertyUtils
            import static org.kordamp.gradle.property.PropertyUtils.Order.*
            
            ext.stringProperty = objects.property(String)
            ext.stringProvider = PropertyUtils.stringProvider(
                'foo.bar', stringProperty, ENV_PROP_SYS, project, project)
            assert stringProvider.get() == 'prop'
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Pfoo.bar=prop',
                '-Dfoo.bar=sys')
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "ENV_PROP_SYS with unset property and SYS set resolves SYS"() {
        given:
        buildFile << """
            import org.kordamp.gradle.property.PropertyUtils
            import static org.kordamp.gradle.property.PropertyUtils.Order.*
            
            ext.stringProperty = objects.property(String)
            ext.stringProvider = PropertyUtils.stringProvider(
                'foo.bar', stringProperty, ENV_PROP_SYS, project, project)
            assert stringProvider.get() == 'sys'
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Dfoo.bar=sys')
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "SYS_ENV_PROP with unset property and SYS_ENV_PROP set resolves SYS"() {
        given:
        buildFile << """
            import org.kordamp.gradle.property.PropertyUtils
            import static org.kordamp.gradle.property.PropertyUtils.Order.*
            
            ext.stringProperty = objects.property(String)
            ext.stringProvider = PropertyUtils.stringProvider(
                'foo.bar', stringProperty, SYS_ENV_PROP, project, project)
            assert stringProvider.get() == 'sys'
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Pfoo.bar=prop',
                '-Dfoo.bar=sys')
            .withEnvironment(['FOO_BAR': 'env'])
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "SYS_ENV_PROP with unset property and ENV_PROP set resolves ENV"() {
        given:
        buildFile << """
            import org.kordamp.gradle.property.PropertyUtils
            import static org.kordamp.gradle.property.PropertyUtils.Order.*
            
            ext.stringProperty = objects.property(String)
            ext.stringProvider = PropertyUtils.stringProvider(
                'foo.bar', stringProperty, SYS_ENV_PROP, project, project)
            assert stringProvider.get() == 'env'
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Pfoo.bar=prop')
            .withEnvironment(['FOO_BAR': 'env'])
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "SYS_ENV_PROP with unset property and PROP set resolves PROP"() {
        given:
        buildFile << """
            import org.kordamp.gradle.property.PropertyUtils
            import static org.kordamp.gradle.property.PropertyUtils.Order.*
            
            ext.stringProperty = objects.property(String)
            ext.stringProvider = PropertyUtils.stringProvider(
                'foo.bar', stringProperty, SYS_ENV_PROP, project, project)
            assert stringProvider.get() == 'prop'
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Pfoo.bar=prop')
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "SYS_PROP_ENV with unset property and SYS_PROP_ENV set resolves SYS"() {
        given:
        buildFile << """
            import org.kordamp.gradle.property.PropertyUtils
            import static org.kordamp.gradle.property.PropertyUtils.Order.*
            
            ext.stringProperty = objects.property(String)
            ext.stringProvider = PropertyUtils.stringProvider(
                'foo.bar', stringProperty, SYS_PROP_ENV, project, project)
            assert stringProvider.get() == 'sys'
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Pfoo.bar=prop',
                '-Dfoo.bar=sys')
            .withEnvironment(['FOO_BAR': 'env'])
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "SYS_PROP_ENV with unset property and PROP_ENV set resolves PROP"() {
        given:
        buildFile << """
            import org.kordamp.gradle.property.PropertyUtils
            import static org.kordamp.gradle.property.PropertyUtils.Order.*
            
            ext.stringProperty = objects.property(String)
            ext.stringProvider = PropertyUtils.stringProvider(
                'foo.bar', stringProperty, SYS_PROP_ENV, project, project)
            assert stringProvider.get() == 'prop'
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Pfoo.bar=prop')
            .withEnvironment(['FOO_BAR': 'env'])
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "SYS_PROP_ENV with unset property and ENV set resolves ENV"() {
        given:
        buildFile << """
            import org.kordamp.gradle.property.PropertyUtils
            import static org.kordamp.gradle.property.PropertyUtils.Order.*
            
            ext.stringProperty = objects.property(String)
            ext.stringProvider = PropertyUtils.stringProvider(
                'foo.bar', stringProperty, SYS_PROP_ENV, project, project)
            assert stringProvider.get() == 'env'
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false')
            .withEnvironment(['FOO_BAR': 'env'])
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "PROP_ENV_SYS with unset property and PROP_ENV_SYS set resolves PROP"() {
        given:
        buildFile << """
            import org.kordamp.gradle.property.PropertyUtils
            import static org.kordamp.gradle.property.PropertyUtils.Order.*
            
            ext.stringProperty = objects.property(String)
            ext.stringProvider = PropertyUtils.stringProvider(
                'foo.bar', stringProperty, PROP_ENV_SYS, project, project)
            assert stringProvider.get() == 'prop'
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Pfoo.bar=prop',
                '-Dfoo.bar=sys')
            .withEnvironment(['FOO_BAR': 'env'])
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "PROP_ENV_SYS with unset property and ENV_SYS set resolves ENV"() {
        given:
        buildFile << """
            import org.kordamp.gradle.property.PropertyUtils
            import static org.kordamp.gradle.property.PropertyUtils.Order.*
            
            ext.stringProperty = objects.property(String)
            ext.stringProvider = PropertyUtils.stringProvider(
                'foo.bar', stringProperty, PROP_ENV_SYS, project, project)
            assert stringProvider.get() == 'env'
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Dfoo.bar=sys')
            .withEnvironment(['FOO_BAR': 'env'])
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "PROP_ENV_SYS with unset property and SYS set resolves SYS"() {
        given:
        buildFile << """
            import org.kordamp.gradle.property.PropertyUtils
            import static org.kordamp.gradle.property.PropertyUtils.Order.*
            
            ext.stringProperty = objects.property(String)
            ext.stringProvider = PropertyUtils.stringProvider(
                'foo.bar', stringProperty, PROP_ENV_SYS, project, project)
            assert stringProvider.get() == 'sys'
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Dfoo.bar=sys')
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "PROP_SYS_ENV with unset property and PROP_SYS_ENV set resolves PROP"() {
        given:
        buildFile << """
            import org.kordamp.gradle.property.PropertyUtils
            import static org.kordamp.gradle.property.PropertyUtils.Order.*
            
            ext.stringProperty = objects.property(String)
            ext.stringProvider = PropertyUtils.stringProvider(
                'foo.bar', stringProperty, PROP_SYS_ENV, project, project)
            assert stringProvider.get() == 'prop'
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Pfoo.bar=prop',
                '-Dfoo.bar=sys')
            .withEnvironment(['FOO_BAR': 'env'])
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "PROP_SYS_ENV with unset property and SYS_ENV set resolves SYS"() {
        given:
        buildFile << """
            import org.kordamp.gradle.property.PropertyUtils
            import static org.kordamp.gradle.property.PropertyUtils.Order.*
            
            ext.stringProperty = objects.property(String)
            ext.stringProvider = PropertyUtils.stringProvider(
                'foo.bar', stringProperty, PROP_SYS_ENV, project, project)
            assert stringProvider.get() == 'sys'
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Dfoo.bar=sys')
            .withEnvironment(['FOO_BAR': 'env'])
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "PROP_SYS_ENV with unset property and ENV set resolves ENV"() {
        given:
        buildFile << """
            import org.kordamp.gradle.property.PropertyUtils
            import static org.kordamp.gradle.property.PropertyUtils.Order.*
            
            ext.stringProperty = objects.property(String)
            ext.stringProvider = PropertyUtils.stringProvider(
                'foo.bar', stringProperty, PROP_SYS_ENV, project, project)
            assert stringProvider.get() == 'env'
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false')
            .withEnvironment(['FOO_BAR': 'env'])
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "ENV_SYS_PROP with unset property and prefixed ENV_SYS_PROP set resolves prefixed ENV"() {
        given:
        buildFile << """
            import org.kordamp.gradle.property.PropertyUtils
            
            ext.stringProperty = objects.property(String)
            ext.stringProvider = PropertyUtils.stringProvider(
                'foo.bar', stringProperty, project, project)
            assert stringProvider.get() == 'prefix_env'
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Ptest.foo.bar=prefix_prop',
                '-Dtest.foo.bar=prefix_sys',
                '-Pfoo.bar=prop',
                '-Dfoo.bar=sys')
            .withEnvironment([
                'TEST_FOO_BAR': 'prefix_env',
                'FOO_BAR'     : 'env'
            ])
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "ENV_SYS_PROP with unset property and prefixed SYS_PROP set resolves prefixed SYS"() {
        given:
        buildFile << """
            import org.kordamp.gradle.property.PropertyUtils
            
            ext.stringProperty = objects.property(String)
            ext.stringProvider = PropertyUtils.stringProvider(
                'foo.bar', stringProperty, project, project)
            assert stringProvider.get() == 'prefix_sys'
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Ptest.foo.bar=prefix_prop',
                '-Dtest.foo.bar=prefix_sys',
                '-Pfoo.bar=prop',
                '-Dfoo.bar=sys')
            .withEnvironment(['FOO_BAR': 'env'])
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "ENV_SYS_PROP with unset property and prefixed PROP set resolves prefixed PROP"() {
        given:
        buildFile << """
            import org.kordamp.gradle.property.PropertyUtils
            
            ext.stringProperty = objects.property(String)
            ext.stringProvider = PropertyUtils.stringProvider(
                'foo.bar', stringProperty, project, project)
            assert stringProvider.get() == 'prefix_prop'
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Ptest.foo.bar=prefix_prop',
                '-Pfoo.bar=prop',
                '-Dfoo.bar=sys')
            .withEnvironment(['FOO_BAR': 'env'])
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }
}
