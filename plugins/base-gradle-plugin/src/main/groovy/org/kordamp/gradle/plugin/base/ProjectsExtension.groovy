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
package org.kordamp.gradle.plugin.base

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.internal.ProjectsSpecImpl

/**
 * @author Andres Almiray
 * @since 0.35.0
 */
@CompileStatic
class ProjectsExtension {
    final Project project

    ProjectsExtension(Project project) {
        this.project = project
    }

    void all(Action<? super ProjectsSpec> action) {
        ProjectsSpecImpl projects = new ProjectsSpecImpl()
        action.execute(projects)
        project.allprojects(projects.asProjectConfigurer())
    }

    void subprojects(Action<? super ProjectsSpec> action) {
        ProjectsSpecImpl projects = new ProjectsSpecImpl()
        action.execute(projects)
        project.subprojects(projects.asProjectConfigurer())
    }
}
