/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2024 Andres Almiray.
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
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.apache.maven.model.Model
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyResolveDetails
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.artifact.Dependency
import org.kordamp.gradle.plugin.base.model.artifact.DependencyManagement
import org.kordamp.gradle.plugin.base.model.artifact.DependencySpec
import org.kordamp.gradle.plugin.base.model.artifact.Platform
import org.kordamp.gradle.plugin.base.model.artifact.PlatformSpec
import org.kordamp.gradle.util.Cache
import org.kordamp.gradle.util.CollectionUtils
import org.kordamp.gradle.util.ConfigureUtil

import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Consumer

import static org.kordamp.gradle.util.StringUtils.isBlank
import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.41.0
 */
@CompileStatic
class DependencyManagementImpl implements DependencyManagement {
    final Project project
    protected final ProjectConfigurationExtension config
    private final Map<String, Dependency> dependencies = [:]
    private final List<DeferredPlatformModule> deferredPlatformModules = new CopyOnWriteArrayList<>()

    private boolean resolved

    private final ConfigurationContainer configurationContainer
    private final EffectivePomResolver effectivePomResolver

    DependencyManagementImpl(ProjectConfigurationExtension config, Project project) {
        this.config = config
        this.project = project
        this.configurationContainer = new ConfigurationContainer(project)
        this.effectivePomResolver = new EffectivePomResolver(project, configurationContainer)
    }

    @Override
    Map<String, Dependency> getDependencies() {
        Collections.unmodifiableMap(dependencies)
    }

    @CompileDynamic
    @Override
    Map<String, Collection<Object>> toMap() {
        [dependencyManagement: dependencies.values()*.toMap()]
    }

    static void merge(DependencyManagementImpl o1, DependencyManagementImpl o2) {
        CollectionUtils.merge(o1.@dependencies, o2.@dependencies)
    }

    @Override
    Dependency dependency(Dependency dependency) {
        if (dependency) {
            dependencies[dependency.name] = dependency
        }

        dependency
    }

    @Override
    Dependency dependency(String gavNotation) {
        if (isBlank(gavNotation)) {
            throw new IllegalArgumentException('Dependency notation cannot be blank.')
        }
        DependencySpecImpl d = (DependencySpecImpl) DependencyUtils.parsePartialDependency(project.rootProject, gavNotation)
        d.validate(project)
        dependencies[d.name] = d.asDependency()
    }

    @Override
    Dependency dependency(String name, String gavNotation) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Dependency name cannot be blank.')
        }
        DependencySpecImpl d = (DependencySpecImpl) DependencyUtils.parseDependency(project.rootProject, name.trim(), gavNotation)
        d.validate(project)
        dependencies[d.name] = d.asDependency()
    }

    @Override
    Dependency dependency(String name, String gavNotation, Action<? super DependencySpec> action) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Dependency name cannot be blank.')
        }
        DependencySpecImpl d = (DependencySpecImpl) DependencyUtils.parseDependency(project.rootProject, name.trim(), gavNotation)
        action.execute(d)
        d.validate(project)
        dependencies[d.name] = d.asDependency()
    }

    @Override
    Dependency dependency(String name, String gavNotation, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DependencySpec) Closure<Void> action) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Dependency name cannot be blank.')
        }
        DependencySpecImpl d = (DependencySpecImpl) DependencyUtils.parseDependency(project.rootProject, name.trim(), gavNotation)
        ConfigureUtil.configure(action, d)
        d.validate(project)
        dependencies[d.name] = d.asDependency()
    }

    @Override
    Dependency dependency(String gavNotation, Action<? super DependencySpec> action) {
        if (isBlank(gavNotation)) {
            throw new IllegalArgumentException('Dependency gavNotation cannot be blank.')
        }
        DependencySpecImpl d = (DependencySpecImpl) DependencyUtils.parsePartialDependency(project.rootProject, gavNotation.trim())
        action.execute(d)
        d.validate(project)
        dependencies[d.name] = d.asDependency()
    }

    @Override
    Dependency dependency(String gavNotation, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DependencySpec) Closure<Void> action) {
        if (isBlank(gavNotation)) {
            throw new IllegalArgumentException('Dependency gavNotation cannot be blank.')
        }
        DependencySpecImpl d = (DependencySpecImpl) DependencyUtils.parsePartialDependency(project.rootProject, gavNotation.trim())
        ConfigureUtil.configure(action, d)
        d.validate(project)
        dependencies[d.name] = d.asDependency()
    }

    @Override
    Platform platform(String gavNotation) {
        if (isBlank(gavNotation)) {
            throw new IllegalArgumentException('Platform notation cannot be blank.')
        }
        PlatformSpecImpl d = (PlatformSpecImpl) DependencyUtils.parsePartialPlatform(project.rootProject, gavNotation)
        d.validate(project)
        dependencies[d.name] = d.asPlatform()
    }

    @Override
    Platform platform(String name, String gavNotation) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Platform name cannot be blank.')
        }
        PlatformSpecImpl d = (PlatformSpecImpl) DependencyUtils.parsePlatform(project.rootProject, name.trim(), gavNotation)
        d.validate(project)
        dependencies[d.name] = d.asPlatform()
    }

    @Override
    Platform platform(String name, String gavNotation, Action<? super PlatformSpec> action) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Platform name cannot be blank.')
        }
        PlatformSpecImpl d = (PlatformSpecImpl) DependencyUtils.parsePlatform(project.rootProject, name.trim(), gavNotation)
        action.execute(d)
        d.validate(project)
        dependencies[d.name] = d.asPlatform()
    }

    @Override
    Platform platform(String name, String gavNotation, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PlatformSpec) Closure<Void> action) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Platform name cannot be blank.')
        }
        PlatformSpecImpl d = (PlatformSpecImpl) DependencyUtils.parsePlatform(project.rootProject, name.trim(), gavNotation)
        ConfigureUtil.configure(action, d)
        d.validate(project)
        dependencies[d.name] = d.asPlatform()
    }

    @Override
    Dependency getDependency(String nameOrGa) {
        if (nameOrGa.contains(':')) {
            String[] parts = nameOrGa.split(':')
            if (parts.length != 2) {
                throw new IllegalArgumentException('Invalid groupId:artifactId input: ' + nameOrGa)
            }
            Dependency dependency = findDependencyByGA(parts[0], parts[1])
            if (dependency) {
                return dependency
            }
            throw new IllegalArgumentException("Undeclared dependency ${nameOrGa}.")
        }

        Dependency dependency = findDependencyByName(nameOrGa)
        if (dependency) {
            return dependency
        }
        throw new IllegalArgumentException("Undeclared dependency ${nameOrGa}.")
    }

    @Override
    Dependency findDependency(String nameOrGa) {
        if (nameOrGa.contains(':')) {
            String[] parts = nameOrGa.split(':')
            if (parts.length != 2) {
                throw new IllegalArgumentException('Invalid groupId:artifactId input: ' + nameOrGa)
            }
            return findDependencyByGA(parts[0], parts[1])
        }

        return findDependencyByName(nameOrGa)
    }

    @Override
    Dependency findDependencyByName(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException('Dependency name cannot be blank.')
        }

        if (dependencies.containsKey(name)) {
            return dependencies.get(name)
        }
        if (project != project.rootProject) {
            return project.rootProject.extensions
                .findByType(ProjectConfigurationExtension)
                .dependencyManagement
                .findDependencyByName(name)
        }
        null
    }

    @Override
    Dependency findDependencyByGA(String groupId, String artifactId) {
        if (isBlank(groupId)) {
            throw new IllegalArgumentException('Dependency groupId cannot be blank.')
        }
        if (isBlank(artifactId)) {
            throw new IllegalArgumentException('Dependency artifactId cannot be blank.')
        }

        for (Dependency dependency : dependencies.values()) {
            if (dependency.groupId == groupId &&
                (dependency.artifactId == artifactId || dependency.modules.contains(artifactId))) {
                return dependency
            }
        }
        if (project != project.rootProject) {
            return project.rootProject.extensions
                .findByType(ProjectConfigurationExtension)
                .dependencyManagement
                .findDependencyByGA(groupId, artifactId)
        }
        null
    }

    @Override
    Platform findPlatform(String nameOrGa) {
        Dependency d = findDependency(nameOrGa)
        if (d instanceof Platform) {
            return (Platform) d
        }
        null
    }

    @Override
    Platform getPlatform(String nameOrGa) {
        Dependency d = getDependency(nameOrGa)
        if (d instanceof Platform) {
            return (Platform) d
        }
        throw new IllegalArgumentException("${nameOrGa} is not a platform.")
    }

    @Override
    Platform findPlatformByName(String name) {
        Dependency d = findPlatformByName(name)
        if (d instanceof Platform) {
            return (Platform) d
        }
        null
    }

    @Override
    Platform findPlatformByGA(String groupId, String artifactId) {
        Dependency d = findDependencyByGA(groupId, artifactId)
        if (d instanceof Platform) {
            return (Platform) d
        }
        null
    }

    @Override
    String gav(String nameOrGa) {
        getDependency(nameOrGa).gav
    }

    @Override
    String gav(String nameOrGa, String moduleName) {
        if (isBlank(nameOrGa)) {
            throw new IllegalArgumentException('Dependency name cannot be blank.')
        }
        if (isBlank(moduleName)) {
            throw new IllegalArgumentException('Dependency moduleName cannot be blank.')
        }

        if (nameOrGa.contains(':')) {
            return getDependency(nameOrGa).gav(moduleName)
        }

        if (dependencies.containsKey(nameOrGa)) {
            if (dependencies.get(nameOrGa).artifactId == moduleName) {
                return dependencies.get(nameOrGa).gav
            }
            return dependencies.get(nameOrGa).gav(moduleName)
        }

        if (project != project.rootProject) {
            return project.rootProject.extensions
                .findByType(ProjectConfigurationExtension)
                .dependencyManagement
                .gav(nameOrGa, moduleName)
        }

        throw new IllegalArgumentException("Undeclared depedency ${nameOrGa}.")
    }

    @Override
    String ga(String nameOrGa, String moduleName) {
        if (isBlank(nameOrGa)) {
            throw new IllegalArgumentException('Dependency name cannot be blank.')
        }
        if (isBlank(moduleName)) {
            throw new IllegalArgumentException('Dependency moduleName cannot be blank.')
        }

        if (nameOrGa.contains(':')) {
            return getDependency(nameOrGa).ga(moduleName)
        }

        if (dependencies.containsKey(nameOrGa)) {
            if (dependencies.get(nameOrGa).artifactId == moduleName) {
                return dependencies.get(nameOrGa).ga
            }
            return dependencies.get(nameOrGa).ga(moduleName)
        }

        if (project != project.rootProject) {
            return project.rootProject.extensions
                .findByType(ProjectConfigurationExtension)
                .dependencyManagement
                .ga(nameOrGa, moduleName)
        }

        throw new IllegalArgumentException("Undeclared depedency ${nameOrGa}.")
    }

    @Override
    void resolve() {
        if (!resolved) {
            doResolve()
            resolved = true
        }
    }

    void registerDeferredPlatformModule(Project project, String configuration, Platform platform, String moduleName, Closure configurer) {
        deferredPlatformModules << new DeferredPlatformModule(project, configuration, platform, moduleName, configurer)
    }

    private boolean doResolve() {
        Set<Platform> ps = new LinkedHashSet<>()
        dependencies.values().each { d ->
            if (d instanceof Platform) {
                ps << (Platform) d
            }
        }

        resolvePlatforms(ps)
        applyDeferredPlatformModules()

        project.configurations.all(new Action<Configuration>() {
            @Override
            void execute(Configuration c) {
                c.resolutionStrategy.eachDependency(new Action<DependencyResolveDetails>() {
                    @Override
                    void execute(DependencyResolveDetails d) {
                        Dependency dependency = config.dependencyManagement.findDependencyByGA(d.requested.group, d.requested.name)
                        if (dependency && dependency.version != d.requested.version) {
                            if (isBlank(d.requested.version)) {
                                project.logger.info("dependencyManagement suggests ${dependency.gav}")
                            } else {
                                project.logger.info("dependencyManagement forces ${dependency.gav}, requested ${dependency.ga}:${d.requested.version}")
                            }
                            d.useVersion(dependency.version)
                        }
                    }
                })
            }
        })
    }

    private void resolvePlatforms(Set<Platform> platforms) {
        for (Platform platform : platforms) {
            if (!platform.moduleNames) {
                resolvePlatform(platform)
            }
        }
    }

    private void resolvePlatform(Platform platform) {
        try {
            File bomFile = project.configurations.detachedConfiguration(
                project.dependencies.create(platform.asGav() + '@pom')
            ).singleFile

            Map<String, String> modules = [:]

            Cache.Key key = Cache.getInstance().key(keyValueFor(platform, bomFile))

            Consumer<BufferedReader> reader = { BufferedReader r ->
                r.readLines().each { String line ->
                    String[] parts = line.split('=')
                    modules.put(parts[0], parts[1])
                }
            }

            if (!Cache.getInstance().read(project.gradle, key, reader)) {
                Model model = effectivePomResolver.resolve(bomFile)
                if (!model) {
                    throw new IllegalStateException("Errors occurred while resolving platform ${platform.asGav()}")
                }

                harvestModules(model.dependencyManagement, modules)
                harvestModules(model.dependencies, modules)

                Cache.getInstance().write(project.gradle, key) { w ->
                    modules.each { k, v -> w.write("${k}=${v}\n".toString()) }
                }
            }
            ((PlatformImpl) platform).setModules(modules)
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    private Object keyValueFor(Platform platform, File file) {
        if (platform.version.endsWith('-SNAPSHOT')) {
            return platform.gav + file.lastModified()
        }
        return platform.gav
    }

    private void harvestModules(org.apache.maven.model.DependencyManagement dependencyManagement, Map<String, String> modules) {
        if (!dependencyManagement) return
        harvestModules(dependencyManagement.dependencies, modules)
    }

    private void harvestModules(List<org.apache.maven.model.Dependency> dependencies, Map<String, String> modules) {
        if (!dependencies) return
        dependencies.each { dependency ->
            if (isNotBlank(dependency.version)) {
                modules.put(dependency.artifactId, "${dependency.groupId}:${dependency.artifactId}:${dependency.version}".toString())
            }
        }
    }

    private void applyDeferredPlatformModules() {
        for (DeferredPlatformModule module : deferredPlatformModules) {
            Configuration configuration = module.project.configurations.findByName(module.configurationName)
            if (configuration) {
                if (module.configurer) {
                    module.project.dependencies.add(module.configurationName, module.platform.asGav(module.moduleName), module.configurer)
                } else {
                    module.project.dependencies.add(module.configurationName, module.platform.asGav(module.moduleName))
                }
            } else {
                throw new IllegalArgumentException("Target configuration '${module.configurationName}' was not found")
            }
        }

        deferredPlatformModules.clear()
    }

    @Canonical
    private static class DeferredPlatformModule {
        final Project project
        final String configurationName
        final Platform platform
        final String moduleName
        final Closure configurer
    }
}
