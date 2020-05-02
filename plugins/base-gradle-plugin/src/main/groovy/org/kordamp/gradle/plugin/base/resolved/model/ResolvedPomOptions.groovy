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
package org.kordamp.gradle.plugin.base.resolved.model

import org.gradle.api.provider.Provider

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
interface ResolvedPomOptions {
    Provider<String> getParent()

    Provider<String> getPackaging()

    Provider<Boolean> getOverwriteInceptionYear()

    Provider<Boolean> getOverwriteUrl()

    Provider<Boolean> getOverwriteLicenses()

    Provider<Boolean> getOverwriteScm()

    Provider<Boolean> getOverwriteOrganization()

    Provider<Boolean> getOverwriteDevelopers()

    Provider<Boolean> getOverwriteContributors()

    Provider<Boolean> getOverwriteIssueManagement()

    Provider<Boolean> getOverwriteCiManagement()

    Provider<Boolean> getOverwriteMailingLists()
}
