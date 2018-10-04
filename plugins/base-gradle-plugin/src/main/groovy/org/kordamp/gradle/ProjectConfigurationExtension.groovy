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

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.kordamp.gradle.model.Information

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class ProjectConfigurationExtension {
    /**
     * Configures a {@code sourceJar} task per {@code SourceSet} if enabled.
     */
    boolean sources = true

    /**
     * Configures {@code javadoc} and {@code javadocJAr} tasks per {@code SourceSet} if enabled.
     */
    boolean apidocs = true

    /**
     * Configures a {@code minPom} task per {@code SourceSet} if enabled.
     */
    boolean minpom = true

    /**
     * Customizes Manifest and MetaInf entries of {@code jar} tasks if enabled.
     */
    boolean release = false

    final Information info

    private boolean sourcesSet
    private boolean apidocsSet
    private boolean minpomSet
    private boolean releaseSet

    ProjectConfigurationExtension(Project project) {
        info = new Information(project)
    }

    void info(Action<? super Information> action) {
        action.execute(info)
    }

    void setSources(boolean sources) {
        this.sources = sources
        this.sourcesSet = true
    }

    void setApidocs(boolean apidocs) {
        this.apidocs = apidocs
        this.apidocsSet = true
    }

    void setMinpom(boolean minpom) {
        this.minpom = minpom
        this.minpomSet = true
    }

    void setRelease(boolean release) {
        this.release = release
        this.releaseSet = true
    }

    boolean isSourcesSet() {
        return sourcesSet
    }

    boolean isApidocsSet() {
        return apidocsSet
    }

    boolean isMinpomSet() {
        return minpomSet
    }

    boolean isReleaseSet() {
        return releaseSet
    }

    static boolean sources(ProjectConfigurationExtension child, ProjectConfigurationExtension parent) {
        child.sourcesSet ? child.sources : parent.sources
    }

    static boolean apidocs(ProjectConfigurationExtension child, ProjectConfigurationExtension parent) {
        child.apidocsSet ? child.apidocs : parent.apidocs
    }

    static boolean minpom(ProjectConfigurationExtension child, ProjectConfigurationExtension parent) {
        child.minpomSet ? child.minpom : parent.minpom
    }

    static boolean release(ProjectConfigurationExtension child, ProjectConfigurationExtension parent) {
        child.releaseSet ? child.release : parent.release
    }
}
