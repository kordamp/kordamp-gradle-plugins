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
package org.kordamp.gradle.plugin.profiles

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.RegularFile

/**
 *
 * @author Andres Almiray
 * @since 0.35.0
 */
@CompileStatic
interface ProfilesExtension {
    void profile(String id, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ProfileSpec) Closure<Void> action)

    void profile(String id, Action<? super ProfileSpec> action)

    static interface ProfileSpec {
        void activation(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ActivationSpec) Closure<Void> action)

        void activation(Action<? super ActivationSpec> action)

        void action(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Project) Closure<Void> action)

        void action(Action<? super Project> action)
    }

    static interface ActivationSpec {
        void file(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = FileSpec) Closure<Void> action)

        void file(Action<? super FileSpec> action)

        void jdk(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = JdkSpec) Closure<Void> action)

        void jdk(Action<? super JdkSpec> action)

        void os(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = OsSpec) Closure<Void> action)

        void os(Action<? super OsSpec> action)

        void property(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PropertySpec) Closure<Void> action)

        void property(Action<? super PropertySpec> action)

        void custom(Activation activation)
    }

    static interface FileSpec {
        void setExists(RegularFile file)

        void setExists(File file)

        void setExists(String file)

        void setMissing(RegularFile file)

        void setMissing(File file)

        void setMissing(String file)
    }

    static interface JdkSpec {
        void setVersion(String version)
    }

    static interface OsSpec {
        void setArch(String s)

        void setName(String s)

        void setVersion(String s)

        void setRelease(String s)

        void setClassifier(String s)

        void setClassifierWithLikes(List<String> l)
    }

    static interface PropertySpec {
        void setKey(String key)

        void setValue(String value)
    }
}
