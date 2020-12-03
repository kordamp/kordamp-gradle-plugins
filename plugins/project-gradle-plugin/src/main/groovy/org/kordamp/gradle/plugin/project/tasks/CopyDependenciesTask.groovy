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
package org.kordamp.gradle.plugin.project.tasks

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.Transformer
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.Directory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.options.OptionValues
import org.kordamp.gradle.property.DirectoryState
import org.kordamp.gradle.property.EnumState
import org.kordamp.gradle.property.SimpleDirectoryState
import org.kordamp.gradle.property.SimpleEnumState
import org.kordamp.gradle.property.SimpleStringState
import org.kordamp.gradle.property.StringState

import java.nio.file.Files

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING
import static org.kordamp.gradle.util.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.43.0
 */
@CompileStatic
class CopyDependenciesTask extends DefaultTask {
    private final StringState configurationName
    private final EnumState<Layout> layout
    private final DirectoryState outputDirectory

    CopyDependenciesTask() {
        configurationName = SimpleStringState.of(this, 'copy.dependencies.configuration.name', (String) null)
        layout = SimpleEnumState.of(this, 'copy.dependencies.layout', Layout, Layout.FLAT)

        Provider<Directory> defaultDir = layout.provider.flatMap(new Transformer<Provider<? extends Directory>, Layout>() {
            @Override
            Provider<? extends Directory> transform(Layout layout) {
                project.layout.buildDirectory.dir('dependencies' + File.separator + layout.name().toLowerCase())
            }
        })

        outputDirectory = SimpleDirectoryState.of(this, 'copy.dependencies.output.directory', defaultDir)
    }

    enum Layout {
        FLAT, DEFAULT
    }

    @Option(option = 'configuration-name', description = 'The name of the configuration (REQUIRED).')
    void setConfigurationName(String configurationName) {
        getConfigurationName().set(configurationName)
    }

    @Internal
    Property<String> getConfigurationName() {
        configurationName.property
    }

    @Input
    Provider<String> getResolvedConfigurationName() {
        configurationName.provider
    }

    @Option(option = 'layout', description = 'The layout type (OPTIONAL).')
    void setLayout(Layout layout) {
        getLayout().set(layout)
    }

    @Internal
    Property<Layout> getLayout() {
        layout.property
    }

    @Input
    Provider<Layout> getResolvedLayout() {
        layout.provider
    }

    @OptionValues('layout')
    List<Layout> getAvailableLayouts() {
        return new ArrayList<Layout>(Arrays.asList(Layout.values()))
    }

    @Internal
    Property<Directory> getOutputDirectory() {
        outputDirectory.property
    }

    @Input
    Provider<Directory> getResolvedOutputDirectory() {
        outputDirectory.provider
    }

    @TaskAction
    void copyDependencies() {
        String configurationName = getResolvedConfigurationName().orNull

        if (isBlank(configurationName)) {
            throw new IllegalArgumentException("${getPath()}.configurationName is blank")
        }

        Configuration configuration = project.configurations.findByName(configurationName)
        if (!configuration) {
            throw new IllegalArgumentException("Configuration ${configurationName} does not exist in project ${project.path}")
        }

        if (!configuration.canBeResolved) {
            throw new IllegalArgumentException("Configuration ${configurationName} can not be resolved")
        }

        File outputDir = getResolvedOutputDirectory().get().asFile
        outputDir.mkdirs()

        switch (getResolvedLayout().get()) {
            case Layout.DEFAULT:
                copyDefault(outputDir, configuration)
                break
            default:
                copyFlat(outputDir, configuration)
                break
        }

    }

    private void copyFlat(File outputDir, Configuration configuration) {
        configuration.files.each { file ->
            Files.copy(file.toPath(), new File(outputDir, file.getName()).toPath(), REPLACE_EXISTING)
        }
    }

    @CompileDynamic
    private void copyDefault(File outputDir, Configuration configuration) {
        configuration.incoming.resolutionResult.allComponents.each {
            String g = it.moduleVersion.id.group.replace('.', '/')
            String a = it.moduleVersion.id.name
            String v = it.moduleVersion.version
            File src = configuration.files.find { it.name.startsWith("${a}-${v}".toString()) }
            if (src) {
                String S = File.separator
                File dest = new File("${outputDir.absolutePath}${S}${g}${S}${a}${S}${v}${S}${src.name}")
                dest.parentFile.mkdirs()
                Files.copy(src.toPath(), dest.toPath(), REPLACE_EXISTING)
            }
        }
    }
}
