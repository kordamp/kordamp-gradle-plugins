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
package org.kordamp.gradle.util

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.internal.metaobject.ConfigureDelegate

import javax.annotation.Nullable

/**
 * Wraps {@code org.gradle.util.ConfigureUtil} to immediately bubble up any {@code MissingMethodException}s.
 *
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
class ConfigureUtil {
    static <T> T configureByMap(Map<?, ?> properties, T delegate) {
        try {
            org.gradle.util.ConfigureUtil.configureByMap(properties, delegate)
        } catch (MissingMethodException mme) {
            throw new GradleException(mme.message, mme)
        }
    }

    static <T> T configureByMap(Map<?, ?> properties, T delegate, Collection<?> mandatoryKeys) {
        try {
            org.gradle.util.ConfigureUtil.configureByMap(properties, delegate, mandatoryKeys)
        } catch (MissingMethodException mme) {
            throw new GradleException(mme.message, mme)
        }
    }

    static <T> T configure(@Nullable Closure configureClosure, T target) {
        try {
            org.gradle.util.ConfigureUtil.configure(configureClosure, target)
        } catch (MissingMethodException mme) {
            throw new GradleException(mme.message, mme)
        }
    }

    static <T> Action<T> configureUsing(@Nullable Closure configureClosure) {
        try {
            org.gradle.util.ConfigureUtil.configureUsing(configureClosure)
        } catch (MissingMethodException mme) {
            throw new GradleException(mme.message, mme)
        }
    }

    static <T> T configureSelf(@Nullable Closure configureClosure, T target) {
        try {
            org.gradle.util.ConfigureUtil.configureSelf(configureClosure, target)
        } catch (MissingMethodException mme) {
            throw new GradleException(mme.message, mme)
        }
    }

    static <T> T configureSelf(@Nullable Closure configureClosure, T target, ConfigureDelegate closureDelegate) {
        try {
            org.gradle.util.ConfigureUtil.configureSelf(configureClosure, target, closureDelegate)
        } catch (MissingMethodException mme) {
            throw new GradleException(mme.message, mme)
        }
    }
}
