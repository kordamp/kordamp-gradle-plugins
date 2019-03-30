/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
package org.kordamp.gradle

import org.gradle.api.Project

/**
 * @author Andres Almiray
 * @since 0.16.0
 */
class AnsiConsole {
    private boolean plain

    AnsiConsole(Project project) {
        plain = 'plain'.equalsIgnoreCase(System.getProperty('org.gradle.console'))
    }


    String green(CharSequence s) {
        (plain ? s : "\u001B[32m${s}\u001b[0m").toString()
    }

    String red(CharSequence s) {
        (plain ? s : "\u001B[31m${s}\u001b[0m").toString()
    }

    String yellow(CharSequence s) {
        (plain ? s : "\u001B[33m${s}\u001b[0m").toString()
    }

    String cyan(CharSequence s) {
        (plain ? s : "\u001B[36m${s}\u001b[0m").toString()
    }

    String erase(CharSequence s) {
        (plain ? s : "\u001b[2K${s}").toString()
    }
}
