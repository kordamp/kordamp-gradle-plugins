/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2025 Andres Almiray.
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

class SimpleEnumStateSpec extends Specification {
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
            
            import groovy.transform.CompileStatic
            import org.gradle.api.DefaultTask
            import org.gradle.api.provider.Property
            import org.gradle.api.provider.Provider
            import org.gradle.api.tasks.Input
            import org.gradle.api.tasks.Internal
            import org.gradle.api.tasks.Optional
            import org.gradle.api.tasks.TaskAction
            import org.kordamp.gradle.property.SimpleEnumState
            import org.kordamp.gradle.property.EnumState
            
            enum InputEnum {
                TASK, PROPERTY, SYSTEM, ENVIRONMENT, UNSET
            }
            
            @CompileStatic
            class StateTestTask extends DefaultTask {
                private final EnumState<InputEnum> input
            
                StateTestTask() {
                    input = SimpleEnumState.of(this, 'test.input', InputEnum, InputEnum.UNSET)
                }
            
                @Internal
                Property<InputEnum> getInput() {
                    input.property
                }
            
                @Input
                @Optional
                Provider<InputEnum> getResolvedInput() {
                    input.provider
                }
            
                @TaskAction
                void doIt() {
                    // noop
                }
            }
            
            project.tasks.register('stateTest', StateTestTask)
        """
    }

    def "ENV_SYS_PROP with property set resolves property"() {
        given:
        buildFile << """
            project.tasks.stateTest.input = InputEnum.TASK
            assert project.tasks.stateTest.resolvedInput.get() == InputEnum.TASK
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Ptest.input=PROPERTY',
                '-Dtest.input=SYSTEM')
            .withEnvironment(['TEST_INPUT': 'ENVIRONMENT'])
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "ENV_SYS_PROP with ENV_SYS_PROP set resolves env"() {
        given:
        buildFile << """
            assert project.tasks.stateTest.resolvedInput.get() == InputEnum.ENVIRONMENT
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Ptest.input=PROPERTY',
                '-Dtest.input=SYSTEM')
            .withEnvironment(['TEST_INPUT': 'ENVIRONMENT'])
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "ENV_SYS_PROP with SYS_PROP set resolves sys"() {
        given:
        buildFile << """
            assert project.tasks.stateTest.resolvedInput.get() == InputEnum.SYSTEM
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Ptest.input=PROPERTY',
                '-Dtest.input=SYSTEM')
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }

    def "ENV_SYS_PROP with PROP set resolves prop"() {
        given:
        buildFile << """
            assert project.tasks.stateTest.resolvedInput.get() == InputEnum.PROPERTY
        """

        when:
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('clean',
                '-Dorg.kordamp.gradle.base.validate=false',
                '-Ptest.input=PROPERTY')
            .build()

        then:
        result.task(':clean').outcome == UP_TO_DATE
    }
}
