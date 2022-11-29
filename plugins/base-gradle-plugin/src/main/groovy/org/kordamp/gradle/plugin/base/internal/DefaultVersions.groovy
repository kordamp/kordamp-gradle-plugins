/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2022 Andres Almiray.
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
package org.kordamp.gradle.plugin.base.internal

import groovy.transform.CompileStatic

/**
 *
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
final class DefaultVersions {
    private final ResourceBundle bundle = ResourceBundle.getBundle('org.kordamp.gradle.plugin.base.default_versions')

    static final DefaultVersions INSTANCE = new DefaultVersions()

    final String checkstyleVersion = bundle.getString('checkstyle.version')
    final String codenarcVersion = bundle.getString('codenarc.version')
    final String errorproneVersion = bundle.getString('errorprone.version')
    final String jacocoVersion = bundle.getString('jacoco.version')
    final String pmdVersion = bundle.getString('pmd.version')
    final String spotbugsVersion = bundle.getString('spotbugs.version')

    private DefaultVersions() {
        // nooop
    }
}
