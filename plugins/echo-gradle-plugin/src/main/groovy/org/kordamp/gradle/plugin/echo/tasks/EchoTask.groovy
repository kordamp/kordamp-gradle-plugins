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
package org.kordamp.gradle.plugin.echo.tasks

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.kordamp.gradle.property.SimpleStringState
import org.kordamp.gradle.property.StringState

/**
 * @author Andres Almiray
 * @since 0.39.0
 */
@CompileStatic
class EchoTask extends DefaultTask {
    private final StringState message

    EchoTask() {
        message = SimpleStringState.of(this, 'echo.message', '')
    }

    @Option(option='echo-message', description = 'The message to write')
    void setMessage(String message) {
        getMessage().set(message)
    }

    @Internal
    Property<String> getMessage() {
        message.property
    }

    @Input
    @Optional
    Provider<String> getResolvedMessage() {
        message.provider
    }

    @TaskAction
    void echo() {
        println getResolvedMessage().get()
    }
}