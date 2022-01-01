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
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.kordamp.gradle.plugin.base.model.Credentials

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@CompileStatic
interface Bintray extends Feature {
    String PLUGIN_ID = 'org.kordamp.gradle.bintray'

    Property<String> getRepo()

    Property<String> getUserOrg()

    Property<String> getName()

    Property<String> getGithubRepo()

    ListProperty<String> getPublications()

    Property<Boolean> getSkipMavenSync()

    Property<Boolean> getPublish()

    Property<Boolean> getPublicDownloadNumbers()

    void credentials(Action<? super Credentials> action)

    void credentials(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Credentials) Closure<Void> action)
}
