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
package org.kordamp.gradle.plugin.profiles.internal

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.kordamp.gradle.plugin.profiles.Activation
import org.kordamp.gradle.plugin.profiles.ProfilesExtension
import org.kordamp.gradle.util.ConfigureUtil

import static org.apache.commons.lang3.StringUtils.isBlank
import static org.apache.commons.lang3.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.35.0
 */
@CompileStatic
class ProfilesExtensionImpl implements ProfilesExtension {
    final Map<String, ProfileInternal> profiles = [:]
    private final Project project
    private final ObjectFactory objects

    ProfilesExtensionImpl(Project project) {
        this.project = project
        this.objects = project.objects
    }

    @Override
    void profile(String id, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ProfileSpec) Closure<Void> action) {
        if (!isEnabled()) return

        if (isBlank(id)) {
            throw new IllegalStateException("Profile id cannot be blank.")
        }

        if (!profiles.containsKey(id)) {
            ProfileSpecImpl spec = new ProfileSpecImpl()
            ConfigureUtil.configure(action, spec)
            ProfileInternal profile = spec.toProfile(id)
            profiles.put(id, profile)
            profile.activate(project)
        } else {
            throw new IllegalStateException("Profile ${id} is not unique!")
        }
    }

    @Override
    void profile(String id, Action<? super ProfileSpec> action) {
        if (!isEnabled()) return

        if (isBlank(id)) {
            throw new IllegalStateException("Profile id cannot be blank.")
        }

        if (!profiles.containsKey(id)) {
            ProfileSpecImpl spec = new ProfileSpecImpl()
            action.execute(spec)
            ProfileInternal profile = spec.toProfile(id)
            profiles.put(id, profile)
            profile.activate(project)
        } else {
            throw new IllegalStateException("Profile ${id} is not unique!")
        }
    }

    private boolean isEnabled() {
        String value = System.getProperty('profiles.enabled')
        if (isNotBlank(value)) {
            return Boolean.parseBoolean(value)
        }
        true
    }

    Set<String> getActiveProfiles() {
        Set<String> activeProfiles = []
        if (!isEnabled()) return activeProfiles

        for (ProfileInternal profile : profiles.values()) {
            if (profile.isActive(project)) {
                activeProfiles << profile.id
            }
        }
        activeProfiles
    }

    Set<String> getProfiles() {
        Set<String> list = []
        if (!isEnabled()) return list

        for (ProfileInternal profile : profiles.values()) {
            list << profile.id
        }
        list
    }

    private class ProfileSpecImpl implements ProfileSpec {
        private Activation activation
        private Closure<Void> closure
        private Action<? super Project> action

        @Override
        void activation(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ActivationSpec) Closure<Void> action) {
            ActivationSpecImpl spec = new ActivationSpecImpl()
            ConfigureUtil.configure(action, spec)
            activation = spec.activation
        }

        @Override
        void activation(Action<? super ActivationSpec> action) {
            ActivationSpecImpl spec = new ActivationSpecImpl()
            action.execute(spec)
            activation = spec.activation
        }

        @Override
        void allActivations(Action<? super ActivationsSpec> action) {
            ActivationsSpecImpl spec = new ActivationsSpecImpl(true)
            action.execute(spec)
            activation = spec.asActivation()
        }

        @Override
        void allActivations(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ActivationsSpec) Closure<Void> action) {
            ActivationsSpecImpl spec = new ActivationsSpecImpl(true)
            ConfigureUtil.configure(action, spec)
            activation = spec.asActivation()
        }

        @Override
        void anyActivations(Action<? super ActivationsSpec> action) {
            ActivationsSpecImpl spec = new ActivationsSpecImpl(false)
            action.execute(spec)
            activation = spec.asActivation()
        }

        @Override
        void anyActivations(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ActivationsSpec) Closure<Void> action) {
            ActivationsSpecImpl spec = new ActivationsSpecImpl(false)
            ConfigureUtil.configure(action, spec)
            activation = spec.asActivation()
        }

        @Override
        void action(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Project) Closure<Void> action) {
            this.closure = action
        }

        @Override
        void action(Action<? super Project> action) {
            this.action = action
        }

        ProfileInternal toProfile(String id) {
            if (!closure && !action) {
                throw new IllegalMonitorStateException("Profile '${id}' has not declared an action.")
            }

            new ProfileInternal(id, activation)
                .setClosure(closure)
                .setAction(action)
        }
    }

    private class ActivationsSpecImpl implements ActivationsSpec {
        private final List<Activation> activations = []
        private final boolean all

        ActivationsSpecImpl(boolean all) {
            this.all = all
        }

        @Override
        void file(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = FileSpec) Closure<Void> action) {
            FileSpecImpl spec = new FileSpecImpl()
            ConfigureUtil.configure(action, spec)
            activations << spec.asActivation()
        }

        @Override
        void file(Action<? super FileSpec> action) {
            FileSpecImpl spec = new FileSpecImpl()
            action.execute(spec)
            activations << spec.asActivation()
        }

        @Override
        void jdk(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = JdkSpec) Closure<Void> action) {
            JdkSpecImpl spec = new JdkSpecImpl()
            ConfigureUtil.configure(action, spec)
            activations << spec.asActivation()
        }

        @Override
        void jdk(Action<? super JdkSpec> action) {
            JdkSpecImpl spec = new JdkSpecImpl()
            action.execute(spec)
            activations << spec.asActivation()
        }

        @Override
        void os(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = OsSpec) Closure<Void> action) {
            OsSpecImpl spec = new OsSpecImpl()
            ConfigureUtil.configure(action, spec)
            activations << spec.asActivation()
        }

        @Override
        void os(Action<? super OsSpec> action) {
            OsSpecImpl spec = new OsSpecImpl()
            action.execute(spec)
            activations << spec.asActivation()
        }

        @Override
        void property(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PropertySpec) Closure<Void> action) {
            PropertySpecImpl spec = new PropertySpecImpl()
            ConfigureUtil.configure(action, spec)
            activations << spec.asActivation()
        }

        @Override
        void property(Action<? super PropertySpec> action) {
            PropertySpecImpl spec = new PropertySpecImpl()
            action.execute(spec)
            activations << spec.asActivation()
        }

        @Override
        void custom(Activation activation) {
            if (activation) {
                activations << activation
            }
        }

        Activation asActivation() {
            new CompositeActivation(activations, all)
        }
    }

    private class CompositeActivation implements Activation {
        private final List<Activation> activations = []
        private final boolean all

        CompositeActivation(List<Activation> activations, boolean all) {
            this.activations.addAll(activations)
            this.all = all
        }

        @Override
        boolean isActive(Project project) {
            if (all) {
                for (Activation activation : activations) {
                    if (!activation.isActive(project)) return false
                }
                return true
            } else {
                boolean result = false
                for (Activation activation : activations) {
                    result = result || activation.isActive(project)
                }
                return result
            }
        }
    }

    private class ActivationSpecImpl implements ActivationSpec {
        private Activation activation

        @Override
        void file(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = FileSpec) Closure<Void> action) {
            FileSpecImpl spec = new FileSpecImpl()
            ConfigureUtil.configure(action, spec)
            activation = spec.asActivation()
        }

        @Override
        void file(Action<? super FileSpec> action) {
            FileSpecImpl spec = new FileSpecImpl()
            action.execute(spec)
            activation = spec.asActivation()
        }

        @Override
        void jdk(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = JdkSpec) Closure<Void> action) {
            JdkSpecImpl spec = new JdkSpecImpl()
            ConfigureUtil.configure(action, spec)
            activation = spec.asActivation()
        }

        @Override
        void jdk(Action<? super JdkSpec> action) {
            JdkSpecImpl spec = new JdkSpecImpl()
            action.execute(spec)
            activation = spec.asActivation()
        }

        @Override
        void os(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = OsSpec) Closure<Void> action) {
            OsSpecImpl spec = new OsSpecImpl()
            ConfigureUtil.configure(action, spec)
            activation = spec.asActivation()
        }

        @Override
        void os(Action<? super OsSpec> action) {
            OsSpecImpl spec = new OsSpecImpl()
            action.execute(spec)
            activation = spec.asActivation()
        }

        @Override
        void property(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PropertySpec) Closure<Void> action) {
            PropertySpecImpl spec = new PropertySpecImpl()
            ConfigureUtil.configure(action, spec)
            activation = spec.asActivation()
        }

        @Override
        void property(Action<? super PropertySpec> action) {
            PropertySpecImpl spec = new PropertySpecImpl()
            action.execute(spec)
            activation = spec.asActivation()
        }

        @Override
        void custom(Activation activation) {
            if (activation) {
                this.activation = activation
            }
        }
    }

    private class FileSpecImpl implements FileSpec {
        File exists
        File missing

        @Override
        void setExists(RegularFile file) {
            this.exists = file?.asFile
        }

        @Override
        void setExists(File file) {
            this.exists = file
        }

        @Override
        void setExists(String file) {
            if (file) this.exists = new File(file)
        }

        @Override
        void setMissing(RegularFile file) {
            this.missing = file?.asFile
        }

        @Override
        void setMissing(File file) {
            this.missing = file
        }

        @Override
        void setMissing(String file) {
            if (file) this.missing = new File(file)
        }

        Activation asActivation() {
            ActivationFile activation = objects.newInstance(ActivationFile)
            activation.getExists().set(exists)
            activation.getMissing().set(missing)
            activation
        }
    }

    private class JdkSpecImpl implements JdkSpec {
        String version

        Activation asActivation() {
            ActivationJdk activation = objects.newInstance(ActivationJdk)
            activation.getVersion().set(version)
            activation
        }
    }

    private class OsSpecImpl implements OsSpec {
        String arch
        String name
        String version
        String release
        String classifier
        List<String> classifierWithLikes = []

        Activation asActivation() {
            ActivationOs activation = objects.newInstance(ActivationOs)
            activation.getArch().set(arch)
            activation.getName().set(name)
            activation.getVersion().set(version)
            activation.getRelease().set(release)
            activation.getClassifier().set(classifier)
            if (classifierWithLikes) activation.getClassifierWithLikes().addAll(classifierWithLikes)
            activation
        }
    }

    private class PropertySpecImpl implements PropertySpec {
        String key
        String value

        Activation asActivation() {
            ActivationProperty activation = objects.newInstance(ActivationProperty)
            activation.getKey().set(key)
            activation.getValue().set(value)
            activation
        }
    }
}
