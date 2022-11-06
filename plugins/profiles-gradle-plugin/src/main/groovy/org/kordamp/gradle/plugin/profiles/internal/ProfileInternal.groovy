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
package org.kordamp.gradle.plugin.profiles.internal

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.PackageScope
import groovy.transform.ToString
import org.gradle.api.Action
import org.gradle.api.Project
import org.kordamp.gradle.plugin.profiles.Activation
import org.kordamp.gradle.plugin.profiles.Profile
import org.kordamp.gradle.util.ConfigureUtil

/**
 *
 * @author Andres Almiray
 * @since 0.35.0
 */
@PackageScope
@Canonical
@EqualsAndHashCode(includes = ['id'])
@ToString(includes = ['id'], includeNames = true)
@CompileStatic
class ProfileInternal implements Profile {
    final String id
    final Activation activation
    private Closure<Void> closure
    private Action<? super Project> action

    ProfileInternal(String id, Activation activation) {
        this.id = id
        this.activation = activation
    }

    ProfileInternal setClosure(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Project) Closure<Void> closure) {
        if (closure) {
            this.closure = closure
            this.action = null
        }
        this
    }

    ProfileInternal setAction(Action<? super Project> action) {
        if (action) {
            this.action = action
            this.closure = null
        }
        this
    }

    void activate(Project project) {
        if (isActive(project)) {
            project.logger.info("[profiles] Profile ${id} has been activated.")
            if (closure) {
                ConfigureUtil.configure(closure, project)
            } else if (action) {
                action.execute(project)
            }
        }
    }

    @Override
    boolean isActive(Project project) {
        isActiveById(project) || activation?.isActive(project)
    }

    boolean isActiveById(Project project) {
        String profileIds = String.valueOf(project.findProperty('profile')) ?: ''
        if (profileIds) {
            for (String profileId : profileIds.split(',')) {
                if (profileId?.trim() == id) return true
            }
        }
        false
    }
}
