/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 Andres Almiray.
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
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import org.gradle.api.Project

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
@EqualsAndHashCode(excludes = ['project'])
abstract class AbstractFeature implements Feature {
    boolean enabled = true

    private boolean enabledSet

    protected final Project project

    AbstractFeature(Project project) {
        this.project = project
    }

    protected void doSetEnabled(boolean enabled) {
        this.enabled = enabled
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled
        this.enabledSet = true
    }

    boolean isEnabledSet() {
        this.enabledSet
    }

    void copyInto(AbstractFeature copy) {
        copy.@enabled = this.enabled
        copy.@enabledSet = this.enabledSet
    }

    final boolean isRoot() {
        project == project.rootProject
    }

    static void merge(AbstractFeature o1, AbstractFeature o2) {
        o1.setEnabled((boolean) (o1.enabledSet ? o1.enabled : o2.enabled))
    }
}
