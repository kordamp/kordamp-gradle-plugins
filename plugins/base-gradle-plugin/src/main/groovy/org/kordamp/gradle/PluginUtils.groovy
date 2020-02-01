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
package org.kordamp.gradle

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.DocsType
import org.gradle.api.attributes.Usage
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.component.ConfigurationVariantDetails
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.component.SoftwareComponentContainer
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact
import org.gradle.api.plugins.internal.JavaConfigurationVariantMapping
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.util.GradleVersion
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.9.0
 */
@CompileStatic
class PluginUtils {
    static boolean isAndroidProject(Project project) {
        androidPlugins().any { project.pluginManager.hasPlugin(it) }
    }

    /**
     * Returns the sourceSets associated with the project.
     *
     * Return type is {@code SourceSetContainer} for a Java/Groovy/Groovy project or
     * {@code AndroidSourceSet} if the project is Android related.
     */
    @CompileDynamic
    static resolveSourceSets(Project project) {
        if (isAndroidProject(project)) {
            return project.android.sourceSets
        }
        project.extensions.findByType(SourceSetContainer)
    }

    @CompileDynamic
    static resolveSourceSets(Collection<Project> projects) {
        projects.collect { resolveSourceSets(it) }
    }

    @CompileDynamic
    static resolveMainSourceDirs(Project project) {
        if (isAndroidProject(project)) {
            return project.android.sourceSets.main.javaDirectories.flatten()
        }
        project.sourceSets.main.allSource.srcDirs.flatten()
    }

    @CompileDynamic
    static resolveSourceDirs(Project project) {
        if (isAndroidProject(project)) {
            return project.android.sourceSets.javaDirectories.flatten()
        }
        project.sourceSets.collect { it.allSource.srcDirs }.flatten()
    }

    static resolveMainSourceDirs(Collection<Project> projects) {
        projects.collect { resolveMainSourceDirs(it) }.flatten()
    }

    static resolveSourceDirs(Collection<Project> projects) {
        projects.collect { resolveSourceDirs(it) }.flatten()
    }

    private static List<String> androidPlugins() {
        [
            'com.android.library',
            'com.android.feature',
            'com.android.instantapp',
            'com.android.application',
            'com.android.test'
        ]
    }

    static ProjectConfigurationExtension resolveConfig(Project project) {
        (ProjectConfigurationExtension) project.extensions.findByName(ProjectConfigurationExtension.CONFIG_NAME)
    }

    static ProjectConfigurationExtension resolveEffectiveConfig(Project project) {
        (ProjectConfigurationExtension) project.extensions.findByName(ProjectConfigurationExtension.EFFECTIVE_CONFIG_NAME)
    }

    static boolean checkFlag(String flag, boolean defaultValue) {
        if (isBlank(System.getProperty(flag))) {
            return defaultValue
        }
        return Boolean.getBoolean(flag)
    }

    static boolean isGradleCompatible(Project project, int majorVersion) {
        Version version = Version.of(GradleVersion.current().baseVersion.version)
        version.major >= majorVersion
    }

    static boolean supportsApiConfiguration(Project project) {
        project.pluginManager.hasPlugin('java-library')
    }

    static AdhocComponentWithVariants findJavaComponent(SoftwareComponentContainer components) {
        SoftwareComponent component = components.findByName('java')
        if (component instanceof AdhocComponentWithVariants) {
            return (AdhocComponentWithVariants) component
        }
        null
    }

    static boolean isGradle6Compatible() {
        GradleVersion.current() >= GradleVersion.version('6.0')
    }

    static void registerJarVariant(String feature, String classifier, TaskProvider<Jar> jarProvider, Project project) {
        /*if (isGradle6Compatible()) {
            Configuration variant = project.configurations.maybeCreate(classifier + 'Elements')
            variant.visible = false
            variant.description = feature + ' elements for main.'
            variant.canBeResolved = false
            variant.canBeConsumed = true
            variant.attributes.attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage, Usage.JAVA_RUNTIME))
            variant.attributes.attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category, Category.DOCUMENTATION))
            variant.attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling, Bundling.EXTERNAL))
            variant.attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, project.objects.named(DocsType, classifier))

            variant.outgoing.artifact(new LazyPublishArtifact(jarProvider))
            AdhocComponentWithVariants component = findJavaComponent(project.components)
            if (component != null) {
                component.addVariantsFromConfiguration(variant, new JavaConfigurationVariantMapping('runtime', true))
            }
        }*/
    }
}
