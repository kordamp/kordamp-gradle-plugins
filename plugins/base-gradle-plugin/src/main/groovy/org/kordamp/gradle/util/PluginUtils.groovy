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
package org.kordamp.gradle.util

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.DocsType
import org.gradle.api.attributes.Usage
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.component.SoftwareComponentContainer
import org.gradle.api.file.FileTree
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact
import org.gradle.api.plugins.internal.JavaConfigurationVariantMapping
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.util.GradleVersion
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

import static StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.9.0
 */
@CompileStatic
class PluginUtils {
    static boolean isAndroidProject(Project project) {
        project.pluginManager.hasPlugin('com.android.library')
    }

    @CompileDynamic
    static TaskProvider<Task> resolveClassesTask(Project project) {
        if (isAndroidProject(project)) {
            return project.tasks.named('compileReleaseJavaWithJavac')
        }
        project.tasks.named('classes')
    }

    @CompileDynamic
    static FileTree resolveAllSource(Project project) {
        if (isAndroidProject(project)) {
            return project.android.sourceSets.main.java.sourceFiles
        }
        project.sourceSets.main.allSource
    }

    static boolean hasSourceSets(Project project) {
        isAndroidProject(project) || project.extensions.findByType(SourceSetContainer) != null
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

    static ProjectConfigurationExtension resolveConfig(Project project) {
        (ProjectConfigurationExtension) project.extensions.findByName(ProjectConfigurationExtension.CONFIG_NAME)
    }

    static boolean checkFlag(String flag, boolean defaultValue) {
        if (isBlank(System.getProperty(flag))) {
            return defaultValue
        }
        return Boolean.getBoolean(flag)
    }

    static boolean isGradleCompatible(int majorVersion) {
        Version version = Version.of(GradleVersion.current().baseVersion.version)
        version.major >= majorVersion
    }

    static boolean isGradleCompatible(String targetVersion) {
        Version current = Version.of(GradleVersion.current().baseVersion.version)
        Version target = Version.of(targetVersion)
        current.major >= target.major && current.minor >= target.minor
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

    static boolean isGradle7Compatible() {
        GradleVersion.current() >= GradleVersion.version('7.0')
    }

    static Configuration registerJarVariant(String feature,
                                            String classifier,
                                            TaskProvider<Jar> jarProvider,
                                            Project project,
                                            boolean addAttributes = true,
                                            String scope = 'runtime') {
        if (isGradle6Compatible()) {
            Configuration variant = project.configurations.maybeCreate(feature.uncapitalize() + 'Elements')
            variant.visible = false
            variant.description = feature + ' elements.'
            variant.canBeResolved = false
            variant.canBeConsumed = true
            if (addAttributes) {
                variant.attributes.attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage, Usage.JAVA_RUNTIME))
                variant.attributes.attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category, Category.DOCUMENTATION))
                variant.attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling, Bundling.EXTERNAL))
                variant.attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, project.objects.named(DocsType, classifier))
            }

            variant.outgoing.artifact(new LazyPublishArtifact(jarProvider))
            AdhocComponentWithVariants component = findJavaComponent(project.components)
            if (component != null) {
                component.addVariantsFromConfiguration(variant, new JavaConfigurationVariantMapping(scope, true))
            }

            return variant
        }

        return null
    }
}
