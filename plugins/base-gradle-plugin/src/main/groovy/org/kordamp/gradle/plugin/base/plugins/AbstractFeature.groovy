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
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
abstract class AbstractFeature implements Feature {
    boolean enabled = true

    private boolean enabledSet

    protected final ProjectConfigurationExtension config
    protected final Project project

    AbstractFeature(ProjectConfigurationExtension config, Project project) {
        this.config = config
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

    @CompileDynamic
    protected boolean isApplied() {
        isApplied(project)
    }

    @CompileDynamic
    protected boolean isApplied(Project project) {
        ExtraPropertiesExtension ext = project.extensions.findByType(ExtraPropertiesExtension)
        ext.has('VISITED_' + getClass().PLUGIN_ID.replace('.', '_') + '_' + project.path.replace(':', '#'))
    }
}
