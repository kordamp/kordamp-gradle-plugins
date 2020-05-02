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
package org.kordamp.gradle.plugin.base.internal

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.kordamp.gradle.plugin.base.plugins.QualityFeature

import static org.kordamp.gradle.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@PackageScope
@CompileStatic
abstract class AbstractQualityFeature extends AbstractAggregatingFeature implements QualityFeature {
    final Property<Boolean> ignoreFailures
    final Property<String> toolVersion
    final SetProperty<String> excludedSourceSets

    AbstractQualityFeature(Project project, ProjectConfigurationExtensionImpl ownerConfig, ProjectConfigurationExtensionImpl parentConfig) {
        super(project, ownerConfig, parentConfig)

        ignoreFailures = project.objects.property(Boolean)
        toolVersion = project.objects.property(String)
        excludedSourceSets = project.objects.setProperty(String)
    }

    @Override
    void excludeSourceSet(String sourceSetName) {
        if (isNotBlank(sourceSetName)) {
            excludedSourceSets << sourceSetName
        }
    }
}
