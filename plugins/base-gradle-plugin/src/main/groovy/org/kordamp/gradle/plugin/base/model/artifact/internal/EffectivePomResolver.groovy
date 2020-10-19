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
package org.kordamp.gradle.plugin.base.model.artifact.internal

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.apache.maven.model.Dependency
import org.apache.maven.model.Model
import org.apache.maven.model.Parent
import org.apache.maven.model.Repository
import org.apache.maven.model.building.DefaultModelBuilder
import org.apache.maven.model.building.DefaultModelBuilderFactory
import org.apache.maven.model.building.DefaultModelBuildingRequest
import org.apache.maven.model.building.FileModelSource
import org.apache.maven.model.building.ModelBuildingException
import org.apache.maven.model.building.ModelBuildingResult
import org.apache.maven.model.building.ModelCache
import org.apache.maven.model.building.ModelProblem
import org.apache.maven.model.building.ModelSource
import org.apache.maven.model.resolution.InvalidRepositoryException
import org.apache.maven.model.resolution.ModelResolver
import org.apache.maven.model.resolution.UnresolvableModelException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

import java.util.concurrent.ConcurrentHashMap

/**
 * @author Andres Almiray
 * @since 0.41.0
 */
@CompileStatic
@PackageScope
class EffectivePomResolver {
    private final Project project
    private final ModelResolver modelResolver
    private final ModelCache modelCache = new InMemoryModelCache()

    EffectivePomResolver(Project project, ConfigurationContainer configurationContainer) {
        this.project = project
        this.modelResolver = new ConfigurationBasedModelResolver(project, configurationContainer)
    }

    Model resolve(File pom) {
        DefaultModelBuildingRequest request = new DefaultModelBuildingRequest()
        request.systemProperties = System.properties
        request.modelSource = new FileModelSource(pom)
        request.modelResolver = modelResolver
        request.modelCache = modelCache

        try {
            DefaultModelBuilder modelBuilder = new DefaultModelBuilderFactory().newInstance()
            ModelBuildingResult result = modelBuilder.build(request)
            List<ModelProblem> errors = collectErrors(result.problems)
            if (!errors.isEmpty()) {
                reportErrors(errors, pom)
            }
            return result.effectiveModel
        } catch (ModelBuildingException e) {
            project.logger.debug('Unexpected error when building Maven model', e);
            reportErrors(collectErrors(e.problems), pom)
        }
        null
    }

    private List<ModelProblem> collectErrors(List<ModelProblem> problems) {
        problems.grep { ModelProblem problem -> problem.severity == ModelProblem.Severity.ERROR }
    }

    private void reportErrors(List<ModelProblem> errors, File file) {
        StringBuilder message = new StringBuilder("Errors occurred while building the effective model from ${file}:".toString())
        for (ModelProblem error : errors) {
            message.append('\n    ')
                .append(error.message)
                .append(' in ')
                .append(error.modelId)
        }
        project.logger.error(message.toString())
    }

    private static class ConfigurationBasedModelResolver implements ModelResolver {
        private final Project project
        private final ConfigurationContainer configurationContainer
        private final Map<String, FileModelSource> cache = new LinkedHashMap<>()

        ConfigurationBasedModelResolver(Project project, ConfigurationContainer configurationContainer) {
            this.project = project
            this.configurationContainer = configurationContainer
        }

        @Override
        ModelSource resolveModel(String groupId, String artifactId, String version) throws UnresolvableModelException {
            resolve("${groupId}:${artifactId}:${version}".toString())
        }

        @Override
        ModelSource resolveModel(Parent parent) throws UnresolvableModelException {
            resolve("${parent.groupId}:${parent.artifactId}:${parent.version}".toString())
        }

        @Override
        ModelSource resolveModel(Dependency dependency) throws UnresolvableModelException {
            resolve("${dependency.groupId}:${dependency.artifactId}:${dependency.version}".toString())
        }

        private FileModelSource resolve(String coordinates) {
            cache.computeIfAbsent(coordinates + '@pom', { c -> resolveDependency(c) })
        }

        private FileModelSource resolveDependency(String coordinates) {
            org.gradle.api.artifacts.Dependency dependency = project.getDependencies().create(coordinates)
            Configuration configuration = configurationContainer.newConfiguration(dependency)
            new FileModelSource(configuration.resolve()[0])
        }

        @Override
        void addRepository(Repository repository) throws InvalidRepositoryException {
            // noop
        }

        @Override
        void addRepository(Repository repository, boolean replace) throws InvalidRepositoryException {
            // noop
        }

        @Override
        ModelResolver newCopy() {
            return this
        }
    }

    private static class InMemoryModelCache implements ModelCache {
        private final Map<Key, Object> cache = new ConcurrentHashMap<>()

        @Override
        void put(String groupId, String artifactId, String version, String tag, Object data) {
            cache.put(new Key(groupId, artifactId, version, tag), data)
        }

        @Override
        Object get(String groupId, String artifactId, String version, String tag) {
            cache.get(new Key(groupId, artifactId, version, tag))
        }

        @Canonical
        private static class Key {
            final String groupId
            final String artifactId
            final String version
            final String tag
        }
    }
}
