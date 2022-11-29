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
package org.kordamp.gradle.plugin.project.tasks

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.apache.commons.lang3.StringUtils
import org.apache.maven.artifact.versioning.ArtifactVersion
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException
import org.apache.maven.artifact.versioning.Restriction
import org.apache.maven.artifact.versioning.VersionRange
import org.gradle.api.DefaultTask
import org.gradle.api.Transformer
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.file.Directory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.options.OptionValues
import org.kordamp.gradle.property.DirectoryState
import org.kordamp.gradle.property.EnumState
import org.kordamp.gradle.property.ListState
import org.kordamp.gradle.property.SimpleDirectoryState
import org.kordamp.gradle.property.SimpleEnumState
import org.kordamp.gradle.property.SimpleListState
import org.kordamp.gradle.property.SimpleStringState
import org.kordamp.gradle.property.StringState

import java.nio.file.Files

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING
import static java.util.Objects.requireNonNull
import static org.apache.maven.artifact.versioning.VersionRange.createFromVersionSpec
import static org.kordamp.gradle.util.StringUtils.isBlank
import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.43.0
 */
@CompileStatic
class CopyDependenciesTask extends DefaultTask {
    private final StringState configuration
    private final ListState includes
    private final ListState excludes
    private final EnumState<Layout> layout
    private final DirectoryState outputDirectory

    CopyDependenciesTask() {
        configuration = SimpleStringState.of(this, 'copy.dependencies.configuration', (String) null)
        layout = SimpleEnumState.of(this, 'copy.dependencies.layout', Layout, Layout.FLAT)
        includes = SimpleListState.of(this, 'copy.dependencies.includes', [])
        excludes = SimpleListState.of(this, 'copy.dependencies.excludes', [])

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

    @Option(option = 'configuration', description = 'The name of the configuration (REQUIRED).')
    void setConfiguration(String configuration) {
        getConfiguration().set(configuration)
    }

    @Internal
    Property<String> getConfiguration() {
        configuration.property
    }

    @Input
    Provider<String> getResolvedConfiguration() {
        configuration.provider
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

    @Option(option = 'includes', description = 'Regex patterns of artifacts to be included (OPTIONAL).')
    void setIncludes(String includes) {
        if (isNotBlank(includes)) {
            getIncludes().set(includes.split(',').toList())
        }
    }

    @Internal
    ListProperty<String> getIncludes() {
        includes.property
    }

    @Input
    Provider<List<String>> getResolvedIncludes() {
        includes.provider
    }

    @Option(option = 'excludes', description = 'Regex patterns of artifacts to be excluded (OPTIONAL).')
    void setExcludes(String excludes) {
        if (isNotBlank(excludes)) {
            getExcludes().set(excludes.split(',').toList())
        }
    }

    @Internal
    ListProperty<String> getExcludes() {
        excludes.property
    }

    @Input
    Provider<List<String>> getResolvedExcludes() {
        excludes.provider
    }

    @TaskAction
    void copyDependencies() {
        String configuration = getResolvedConfiguration().orNull

        if (isBlank(configuration)) {
            throw new IllegalArgumentException("${getPath()}.configuration is blank")
        }

        Configuration c = project.configurations.findByName(configuration)
        if (!configuration) {
            throw new IllegalArgumentException("Configuration ${configuration} does not exist in project ${project.path}")
        }

        if (!c.canBeResolved) {
            throw new IllegalArgumentException("Configuration ${configuration} can not be resolved")
        }

        Configuration cfg = c.copyRecursive()
        project.logger.info("Resolving configuration ${cfg.name}.")
        cfg.resolve()

        project.logger.info("Configuration ${cfg.name} contains ${cfg.resolvedConfiguration.resolvedArtifacts.size()} artifacts.")
        Set<ResolvedArtifact> artifacts = []
        artifacts.addAll(cfg.resolvedConfiguration.resolvedArtifacts)

        Set<ResolvedArtifact> excluded = filterDependencies(artifacts, (List<String>) getResolvedExcludes().get())
        if (getResolvedIncludes().present) {
            excluded = filterDependencies(excluded, (List<String>) getResolvedIncludes().get())
        }

        File outputDir = getResolvedOutputDirectory().get().asFile
        outputDir.mkdirs()

        switch (getResolvedLayout().get()) {
            case Layout.DEFAULT:
                copyDefault(outputDir, excluded)
                break
            default:
                copyFlat(outputDir, excluded)
                break
        }
    }

    private void copyFlat(File outputDir, Set<ResolvedArtifact> artifacts) {
        artifacts.each { artifact ->
            Files.copy(artifact.file.toPath(), new File(outputDir, artifact.file.getName()).toPath(), REPLACE_EXISTING)
        }
    }

    @CompileDynamic
    private void copyDefault(File outputDir, Set<ResolvedArtifact> artifacts) {
        artifacts.each {
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

    private Set<ResolvedArtifact> filterDependencies(Set<ResolvedArtifact> dependencies, List<String> thePatterns) {
        Set<ResolvedArtifact> filtered = new LinkedHashSet<>(dependencies)

        if (thePatterns != null && thePatterns.size() > 0) {
            for (String pattern : thePatterns) {
                String[] subStrings = pattern.split(':')
                subStrings = StringUtils.stripAll(subStrings)
                String resultPattern = StringUtils.join(subStrings, ':')

                for (ResolvedArtifact artifact : dependencies) {
                    if (compareDependency(resultPattern, artifact)) {
                        filtered.remove(artifact)
                    }
                }
            }
        }
        return filtered
    }

    private boolean compareDependency(String pattern, ResolvedArtifact artifact) {
        ArtifactMatcher.Pattern am = new ArtifactMatcher.Pattern(pattern)
        try {
            return am.match(artifact)
        } catch (Exception e) {
            throw new IllegalStateException('Invalid Version Range: ', e)
        }
    }

    @CompileStatic
    private static final class ArtifactMatcher {
        /**
         * @author I don't know
         */
        static class Pattern {
            private String pattern
            private String[] parts

            Pattern(String pattern) {
                this.pattern = requireNonNull(pattern, 'pattern')

                parts = pattern.split(":", 5)

                if (parts.length == 5) {
                    throw new IllegalArgumentException('Pattern contains too many delimiters.')
                }

                for (String part : parts) {
                    if (isBlank(part)) {
                        throw new IllegalArgumentException('Pattern or one of its part is empty.')
                    }
                }
            }

            boolean match(ResolvedArtifact artifact) throws InvalidVersionSpecificationException {
                requireNonNull(artifact, 'artifact')

                switch (parts.length) {
                    case 4:
                        if (!matches(parts[3], artifact.classifier)) {
                            return false
                        }
                    case 3:
                        if (!matches(parts[2], artifact.moduleVersion.id.version)) {
                            if (!containsVersion(createFromVersionSpec(parts[2]),
                                new DefaultArtifactVersion(artifact.moduleVersion.id.version))) {
                                return false
                            }
                        }
                    case 2:
                        if (!matches(parts[1], artifact.moduleVersion.id.name)) {
                            return false
                        }
                    case 1:
                        return matches(parts[0], artifact.moduleVersion.id.group)
                    default:
                        throw new AssertionError()
                }
            }

            private boolean matches(String expression, String input) {
                String regex = expression.replace('.', '\\.')
                    .replace('*', '.*')
                    .replace(':', '\\:')
                    .replace('?', '.')
                    .replace('[', '\\[')
                    .replace(']', '\\]')
                    .replace('(', '\\(')
                    .replace(')', '\\)')

                // TODO: Check if this can be done better or prevented earlier.
                if (input == null) {
                    input = ''
                }

                return java.util.regex.Pattern.matches(regex, input)
            }

            /**
             * Copied from Artifact.VersionRange. This is tweaked to handle singular ranges properly. Currently the default
             * containsVersion method assumes a singular version means allow everything. This method assumes that "2.0.4" ==
             * "[2.0.4,)"
             *
             * @param allowedRange range of allowed versions.
             * @param theVersion the version to be checked.
             * @return true if the version is contained by the range.
             */
            boolean containsVersion(VersionRange allowedRange, ArtifactVersion theVersion) {
                boolean matched = false
                ArtifactVersion recommendedVersion = allowedRange.getRecommendedVersion()
                if (recommendedVersion == null) {
                    List<Restriction> restrictions = allowedRange.getRestrictions()
                    for (Restriction restriction : restrictions) {
                        if (restriction.containsVersion(theVersion)) {
                            matched = true
                            break
                        }
                    }
                } else {
                    // only singular versions ever have a recommendedVersion
                    @SuppressWarnings('unchecked')
                    int compareTo = recommendedVersion.compareTo(theVersion)
                    matched = (compareTo <= 0)
                }
                return matched
            }

            @Override
            String toString() {
                return pattern
            }
        }

        private Collection<Pattern> patterns = []

        private Collection<Pattern> ignorePatterns = []

        /**
         * Construct class by providing patterns as strings. Empty strings are ignored.
         *
         * @throws NullPointerException if any of the arguments is null
         */
        ArtifactMatcher(final Collection<String> patterns, final Collection<String> ignorePatterns) {
            requireNonNull(patterns, 'patterns')
            requireNonNull(ignorePatterns, 'ignorePatterns')

            for (String pattern : patterns) {
                if (pattern != null && !''.equals(pattern)) {
                    this.patterns.add(new Pattern(pattern))
                }
            }

            for (String ignorePattern : ignorePatterns) {
                if (ignorePattern != null && !''.equals(ignorePattern)) {
                    this.ignorePatterns.add(new Pattern(ignorePattern))
                }
            }
        }

        /**
         * Check if artifact matches patterns.
         *
         * @throws InvalidVersionSpecificationException
         */
        boolean match(ResolvedArtifact artifact)
            throws InvalidVersionSpecificationException {
            for (Pattern pattern : patterns) {
                if (pattern.match(artifact)) {
                    for (Pattern ignorePattern : ignorePatterns) {
                        if (ignorePattern.match(artifact)) {
                            return false
                        }
                    }
                    return true
                }
            }
            return false
        }
    }
}
