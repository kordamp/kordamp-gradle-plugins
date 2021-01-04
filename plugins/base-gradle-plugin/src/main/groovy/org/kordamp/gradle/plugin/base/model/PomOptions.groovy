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
package org.kordamp.gradle.plugin.base.model

import groovy.transform.CompileStatic

/**
 * @author Andres Almiray
 * @since 0.13.0
 */
@CompileStatic
interface PomOptions {
    String getParent()

    String getPackaging()

    boolean isOverwriteInceptionYear()

    boolean isOverwriteUrl()

    boolean isOverwriteLicenses()

    boolean isOverwriteScm()

    boolean isOverwriteOrganization()

    boolean isOverwriteDevelopers()

    boolean isOverwriteContributors()

    boolean isOverwriteIssueManagement()

    boolean isOverwriteCiManagement()

    boolean isOverwriteMailingLists()

    Map<String, String> getProperties()

    void setParent(String parent)

    void setOverwriteInceptionYear(boolean value)

    void setOverwriteUrl(boolean value)

    void setOverwriteLicenses(boolean value)

    void setOverwriteScm(boolean value)

    void setOverwriteOrganization(boolean value)

    void setOverwriteDevelopers(boolean value)

    void setOverwriteContributors(boolean value)

    void setOverwriteIssueManagement(boolean value)

    void setOverwriteCiManagement(boolean value)

    void setOverwriteMailingLists(boolean value)

    void setProperties(Map<String, String> props)

    Map<String, Object> toMap()
}
