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
package org.kordamp.gradle

import org.gradle.api.Project
import org.gradle.api.logging.configuration.ConsoleOutput

/**
 * @author Andres Almiray
 * @since 0.16.0
 */
class AnsiConsole {
    private boolean plain

    AnsiConsole(Project project) {
        plain = project.gradle.startParameter.consoleOutput == ConsoleOutput.Plain ||
            'plain'.equalsIgnoreCase(System.getProperty('org.gradle.console'))
    }

    String black(CharSequence s) {
        (plain ? s : "\u001B[30m${s}\u001b[0m").toString()
    }

    String red(CharSequence s) {
        (plain ? s : "\u001B[31m${s}\u001b[0m").toString()
    }

    String green(CharSequence s) {
        (plain ? s : "\u001B[32m${s}\u001b[0m").toString()
    }

    String yellow(CharSequence s) {
        (plain ? s : "\u001B[33m${s}\u001b[0m").toString()
    }

    String blue(CharSequence s) {
        (plain ? s : "\u001B[34m${s}\u001b[0m").toString()
    }

    String magenta(CharSequence s) {
        (plain ? s : "\u001B[35m${s}\u001b[0m").toString()
    }

    String cyan(CharSequence s) {
        (plain ? s : "\u001B[36m${s}\u001b[0m").toString()
    }

    String white(CharSequence s) {
        (plain ? s : "\u001B[37m${s}\u001b[0m").toString()
    }

    String erase(CharSequence s) {
        (plain ? s : "\u001b[2K${s}").toString()
    }
}
