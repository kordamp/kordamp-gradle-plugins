/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2021 Andres Almiray.
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
package org.kordamp.gradle.plugin.base.extensions

import com.agorapulse.testing.fixt.Fixt
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.TempDir

class ConfigExtensionSpec extends Specification {

    @Shared Fixt fixt = Fixt.create(ConfigExtensionSpec)

    @TempDir File projectRoot

    void 'verify dsl'() {
        given:
            fixt.copyTo('extension-test', projectRoot)
        when:
            BuildResult result = GradleRunner.create()
                    .withProjectDir(projectRoot)
                    .forwardOutput()
                    .withPluginClasspath()
                    .withArguments('extensionTest', '--stacktrace')
                    .build()
        then:
            result.task(':extensionTest').outcome == TaskOutcome.SUCCESS
    }
}
