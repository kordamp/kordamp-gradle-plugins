/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 the original author or authors.
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

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Aggregator for all Kordamp plugins.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
class ProjectPlugin implements Plugin<Project> {
    Project project

    void apply(Project project) {
        this.project = project

        BasePlugin.applyIfMissing(project)
        JacocoPlugin.applyIfMissing(project)
        BuildInfoPlugin.applyIfMissing(project)
        SourceJarPlugin.applyIfMissing(project)
        ApidocPlugin.applyIfMissing(project)
        MinPomPlugin.applyIfMissing(project)
        JarPlugin.applyIfMissing(project)
        PublishingPlugin.applyIfMissing(project)
        BintrayPlugin.applyIfMissing(project)
    }
}
