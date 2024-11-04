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
package org.kordamp.gradle.property

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.SetProperty

import java.nio.file.Paths

import static org.kordamp.gradle.util.StringUtils.isBlank
import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * Utility methods for working with {@code org.gradle.api.provider.Property} and {@code org.gradle.api.provider.Provider} types.
 *
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
final class PropertyUtils {
    enum Priority {
        PROPERTY,
        PROVIDER
    }

    enum Order {
        ENV_SYS_PROP,
        ENV_PROP_SYS,
        SYS_ENV_PROP,
        SYS_PROP_ENV,
        PROP_ENV_SYS,
        PROP_SYS_ENV
    }

    enum Path {
        GLOBAL_PROJECT_OWNER((byte) 7),
        GLOBAL_PROJECT((byte) 6),
        GLOBAL_OWNER((byte) 5),
        GLOBAL((byte) 4),
        PROJECT_OWNER((byte) 3),
        PROJECT((byte) 2),
        OWNER((byte) 1);

        private byte flags

        Path(byte flags) {
            this.flags = flags
        }

        boolean isGlobal() {
            ((byte) (flags & 0x04)) == ((byte) 4)
        }

        boolean isProject() {
            ((byte) (flags & 0x02)) == ((byte) 2)
        }

        boolean isOwner() {
            ((byte) (flags & 0x01)) == ((byte) 1)
        }
    }

    static final String KEY_PROPERTY_ORDER = 'kordamp.property.order'
    static final String KEY_PROPERTY_PATH = 'kordamp.property.path'
    static final String KEY_PROPERTY_PRIORITY = 'kordamp.property.priority'

    /**
     * Creates a hierarchical boolean property.
     * Returns the {@code property}'s value if present, otherwise check's if the supplied
     * {@code provider} is not null and returns its value, otherwise returns the {@code defaultValue}.
     *
     * @param providers the {@code Provider} factory used to create a {@code Provider}.
     * @param property the property to use
     * @param provider the provider to use
     * @param defaultValue the default value in case neither property nor provider has one
     * @return property's value, provider's value, or the defaultValue, in that order.
     */
    @CompileDynamic
    static <E extends Enum<E>> Provider<E> enumProvider(ProviderFactory providers,
                                                        Property<E> property,
                                                        Provider<E> provider,
                                                        E defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    return property.getOrElse(provider ? provider.getOrElse(defaultValue) : defaultValue)
                case Priority.PROVIDER:
                    return provider ? provider.getOrElse(property.getOrElse(defaultValue)) : property.getOrElse(defaultValue)
            }
        }
    }

    static <E extends Enum<E>> Provider<E> enumProvider(ProviderFactory providers,
                                                        Property<E> property,
                                                        Provider<E> provider,
                                                        Provider<E> defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    return property.orElse(provider ? provider.orElse(defaultValue) : defaultValue).get()
                case Priority.PROVIDER:
                    return (provider ? provider.orElse(property.orElse(defaultValue)) : property.orElse(defaultValue)).get()
            }
        }
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.
     *
     * @param envKey the environment variable to check.
     * @param propertyKey the system and project property to check.
     * @param property the property to check value from.
     * @param order resolving order to use.
     * @param path resolving path to use.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on the {@code order} argument.
     */
    @CompileDynamic
    static <E extends Enum<E>> Provider<E> enumProvider(String envKey,
                                                        String propertyKey,
                                                        Class<E> enumType,
                                                        Property<E> property,
                                                        Order order,
                                                        Path path,
                                                        Project project,
                                                        Object owner,
                                                        E defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    if (isNotBlank(value)) {
                        return enumType.valueOf(value.toUpperCase())
                    }
                    return defaultValue
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    if (isNotBlank(value)) {
                        return enumType.valueOf(value.toUpperCase())
                    }
                    return property.getOrElse(defaultValue)
            }
        }
    }

    @CompileDynamic
    static <E extends Enum<E>> Provider<E> enumProvider(String envKey,
                                                        String propertyKey,
                                                        Class<E> enumType,
                                                        Property<E> property,
                                                        Order order,
                                                        Path path,
                                                        Project project,
                                                        Object owner,
                                                        Provider<E> defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    if (isNotBlank(value)) {
                        return enumType.valueOf(value.toUpperCase())
                    }
                    return defaultValue?.get()
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    if (isNotBlank(value)) {
                        return enumType.valueOf(value.toUpperCase())
                    }
                    return property.orElse(defaultValue).get()
            }
        }
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param order resolving order to use.
     * @param path resolving path to use.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on the {@code order} argument.
     */
    static <E extends Enum<E>> Provider<E> enumProvider(String key,
                                                        Class<E> enumType,
                                                        Property<E> property,
                                                        Order order,
                                                        Path path,
                                                        Project project,
                                                        Object owner,
                                                        E defaultValue) {
        enumProvider(toEnv(key), toProperty(key), enumType, property, order, path, project, owner, defaultValue)
    }

    static <E extends Enum<E>> Provider<E> enumProvider(String key,
                                                        Class<E> enumType,
                                                        Property<E> property,
                                                        Order order,
                                                        Path path,
                                                        Project project,
                                                        Object owner,
                                                        Provider<E> defaultValue) {
        enumProvider(toEnv(key), toProperty(key), enumType, property, order, path, project, owner, defaultValue)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.
     * The value of {@code Order} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_ORDER} System
     * property. Defaults to {@code ENV_SYS_PROP}.
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     *
     * @param envKey the environment variable to check.
     * @param propertyKey the system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static <E extends Enum<E>> Provider<E> enumProvider(String envKey,
                                                        String propertyKey,
                                                        Class<E> enumType,
                                                        Property<E> property,
                                                        Project project,
                                                        Object owner) {
        enumProvider(envKey, propertyKey, enumType, property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, (E) null)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static <E extends Enum<E>> Provider<E> enumProvider(String key,
                                                        Class<E> enumType,
                                                        Property<E> property,
                                                        Project project,
                                                        Object owner) {
        enumProvider(toEnv(key), toProperty(key), enumType, property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, (E) null)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static <E extends Enum<E>> Provider<E> enumProvider(String key,
                                                        Class<E> enumType,
                                                        Property<E> property,
                                                        Project project,
                                                        Object owner,
                                                        E defaultValue) {
        enumProvider(toEnv(key), toProperty(key), enumType, property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
    }

    static <E extends Enum<E>> Provider<E> enumProvider(String key,
                                                        Class<E> enumType,
                                                        Property<E> property,
                                                        Project project,
                                                        Object owner,
                                                        Provider<E> defaultValue) {
        enumProvider(toEnv(key), toProperty(key), enumType, property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.
     * The value of {@code Order} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_ORDER} System
     * property. Defaults to {@code ENV_SYS_PROP}.
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static <E extends Enum<E>> Provider<E> enumProvider(String key,
                                                        Class<E> enumType,
                                                        Property<E> property,
                                                        Order order,
                                                        Project project,
                                                        Object owner) {
        enumProvider(toEnv(key), toProperty(key), enumType, property, order, resolvePropertyPath(), project, owner, (E) null)
    }

    static <E extends Enum<E>> Provider<E> enumProvider(ProviderFactory providers,
                                                        String key,
                                                        Class<E> enumType,
                                                        Provider<E> property,
                                                        E defaultValue) {
        enumProvider(providers, toEnv(key), toProperty(key), enumType, property, resolvePropertyOrder(), defaultValue)
    }

    static <E extends Enum<E>> Provider<E> enumProvider(ProviderFactory providers,
                                                        String key,
                                                        Class<E> enumType,
                                                        Provider<E> property,
                                                        Provider<E> defaultValue) {
        enumProvider(providers, toEnv(key), toProperty(key), enumType, property, resolvePropertyOrder(), defaultValue)
    }

    static <E extends Enum<E>> Provider<E> enumProvider(ProviderFactory providers,
                                                        String key,
                                                        Class<E> enumType,
                                                        Provider<E> property,
                                                        Order order,
                                                        E defaultValue) {
        enumProvider(providers, toEnv(key), toProperty(key), enumType, property, order, defaultValue)
    }

    static <E extends Enum<E>> Provider<E> enumProvider(ProviderFactory providers,
                                                        String key,
                                                        Class<E> enumType,
                                                        Provider<E> property,
                                                        Order order,
                                                        Provider<E> defaultValue) {
        enumProvider(providers, toEnv(key), toProperty(key), enumType, property, order, defaultValue)
    }

    static <E extends Enum<E>> Provider<E> enumProvider(ProviderFactory providers,
                                                        String envKey,
                                                        String propertyKey,
                                                        Class<E> enumType,
                                                        Provider<E> property,
                                                        E defaultValue) {

        enumProvider(providers, envKey, propertyKey, enumType, property, resolvePropertyOrder(), defaultValue)
    }

    static <E extends Enum<E>> Provider<E> enumProvider(ProviderFactory providers,
                                                        String envKey,
                                                        String propertyKey,
                                                        Class<E> enumType,
                                                        Provider<E> property,
                                                        Provider<E> defaultValue) {

        enumProvider(providers, envKey, propertyKey, enumType, property, resolvePropertyOrder(), defaultValue)
    }

    @CompileDynamic
    static <E extends Enum<E>> Provider<E> enumProvider(ProviderFactory providers,
                                                        String envKey,
                                                        String propertyKey,
                                                        Class<E> enumType,
                                                        Provider<E> property,
                                                        Order order,
                                                        E defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    if (isNotBlank(value)) {
                        return enumType.valueOf(value.toUpperCase())
                    }
                    return defaultValue
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    if (isNotBlank(value)) {
                        return enumType.valueOf(value.toUpperCase())
                    }
                    return property.getOrElse(defaultValue)
            }
        }
    }

    @CompileDynamic
    static <E extends Enum<E>> Provider<E> enumProvider(ProviderFactory providers,
                                                        String envKey,
                                                        String propertyKey,
                                                        Class<E> enumType,
                                                        Provider<E> property,
                                                        Order order,
                                                        Provider<E> defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    if (isNotBlank(value)) {
                        return enumType.valueOf(value.toUpperCase())
                    }
                    return defaultValue?.get()
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    if (isNotBlank(value)) {
                        return enumType.valueOf(value.toUpperCase())
                    }
                    return property.orElse(defaultValue).get()
            }
        }
    }

    static Provider<Boolean> booleanProvider(ProviderFactory providers,
                                             String key,
                                             Provider<Boolean> property,
                                             boolean defaultValue) {
        booleanProvider(providers, toEnv(key), toProperty(key), resolvePropertyOrder(), property, defaultValue)
    }

    static Provider<Boolean> booleanProvider(ProviderFactory providers,
                                             String key,
                                             Provider<Boolean> property,
                                             Order order,
                                             boolean defaultValue) {
        booleanProvider(providers, toEnv(key), toProperty(key), order, property, defaultValue)
    }

    static Provider<Boolean> booleanProvider(ProviderFactory providers,
                                             String envKey,
                                             String propertyKey,
                                             Provider<Boolean> property,
                                             boolean defaultValue) {
        booleanProvider(providers, envKey, propertyKey, resolvePropertyOrder(), property, defaultValue)
    }

    static Provider<Boolean> booleanProvider(ProviderFactory providers,
                                             String envKey,
                                             String propertyKey,
                                             Order order,
                                             Provider<Boolean> property,
                                             boolean defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return Boolean.parseBoolean(value)
                    }
                    return defaultValue
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return Boolean.parseBoolean(value)
                    }
                    return property.getOrElse(defaultValue)
            }
        }
    }

    static Provider<Boolean> booleanProvider(ProviderFactory providers,
                                             String key,
                                             Provider<Boolean> property,
                                             Provider<Boolean> defaultValue) {
        booleanProvider(providers, toEnv(key), toProperty(key), resolvePropertyOrder(), property, defaultValue)
    }

    static Provider<Boolean> booleanProvider(ProviderFactory providers,
                                             String key,
                                             Provider<Boolean> property,
                                             Order order,
                                             Provider<Boolean> defaultValue) {
        booleanProvider(providers, toEnv(key), toProperty(key), order, property, defaultValue)
    }

    static Provider<Boolean> booleanProvider(ProviderFactory providers,
                                             String envKey,
                                             String propertyKey,
                                             Provider<Boolean> property,
                                             Provider<Boolean> defaultValue) {
        booleanProvider(providers, envKey, propertyKey, resolvePropertyOrder(), property, defaultValue)
    }

    static Provider<Boolean> booleanProvider(ProviderFactory providers,
                                             String envKey,
                                             String propertyKey,
                                             Order order,
                                             Provider<Boolean> property,
                                             Provider<Boolean> defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return Boolean.parseBoolean(value)
                    }
                    return defaultValue?.get()
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return Boolean.parseBoolean(value)
                    }
                    return property.orElse(defaultValue).get()
            }
        }
    }

    static Provider<Integer> integerProvider(ProviderFactory providers,
                                             String key,
                                             Provider<Integer> property,
                                             int defaultValue) {
        integerProvider(providers, toEnv(key), toProperty(key), property, resolvePropertyOrder(), defaultValue)
    }

    static Provider<Integer> integerProvider(ProviderFactory providers,
                                             String key,
                                             Provider<Integer> property,
                                             Order order,
                                             int defaultValue) {
        integerProvider(providers, toEnv(key), toProperty(key), property, order, defaultValue)
    }

    static Provider<Integer> integerProvider(ProviderFactory providers,
                                             String envKey,
                                             String propertyKey,
                                             Provider<Integer> property,
                                             int defaultValue) {
        integerProvider(providers, envKey, propertyKey, property, resolvePropertyOrder(), defaultValue)
    }

    static Provider<Integer> integerProvider(ProviderFactory providers,
                                             String envKey,
                                             String propertyKey,
                                             Provider<Integer> property,
                                             Order order,
                                             int defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return Integer.parseInt(value)
                    }
                    return defaultValue
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return Integer.parseInt(value)
                    }
                    return property.getOrElse(defaultValue)
            }
        }
    }

    static Provider<Integer> integerProvider(ProviderFactory providers,
                                             String key,
                                             Provider<Integer> property,
                                             Provider<Integer> defaultValue) {
        integerProvider(providers, toEnv(key), toProperty(key), property, resolvePropertyOrder(), defaultValue)
    }

    static Provider<Integer> integerProvider(ProviderFactory providers,
                                             String key,
                                             Provider<Integer> property,
                                             Order order,
                                             Provider<Integer> defaultValue) {
        integerProvider(providers, toEnv(key), toProperty(key), property, order, defaultValue)
    }

    static Provider<Integer> integerProvider(ProviderFactory providers,
                                             String envKey,
                                             String propertyKey,
                                             Provider<Integer> property,
                                             Provider<Integer> defaultValue) {
        integerProvider(providers, envKey, propertyKey, property, resolvePropertyOrder(), defaultValue)
    }

    static Provider<Integer> integerProvider(ProviderFactory providers,
                                             String envKey,
                                             String propertyKey,
                                             Provider<Integer> property,
                                             Order order,
                                             Provider<Integer> defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return Integer.parseInt(value)
                    }
                    return defaultValue?.get()
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return Integer.parseInt(value)
                    }
                    return property.orElse(defaultValue).get()
            }
        }
    }

    static Provider<Long> longProvider(ProviderFactory providers,
                                       String key,
                                       Provider<Long> property,
                                       long defaultValue) {
        longProvider(providers, toEnv(key), toProperty(key), property, resolvePropertyOrder(), defaultValue)
    }

    static Provider<Long> longProvider(ProviderFactory providers,
                                       String key,
                                       Provider<Long> property,
                                       Order order,
                                       long defaultValue) {
        longProvider(providers, toEnv(key), toProperty(key), property, order, defaultValue)
    }

    static Provider<Long> longProvider(ProviderFactory providers,
                                       String envKey,
                                       String propertyKey,
                                       Provider<Long> property,
                                       long defaultValue) {
        longProvider(providers, envKey, propertyKey, property, resolvePropertyOrder(), defaultValue)
    }

    static Provider<Long> longProvider(ProviderFactory providers,
                                       String envKey,
                                       String propertyKey,
                                       Provider<Long> property,
                                       Order order,
                                       long defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return Long.parseLong(value)
                    }
                    return defaultValue
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return Long.parseLong(value)
                    }
                    return property.getOrElse(defaultValue)
            }
        }
    }

    static Provider<Long> longProvider(ProviderFactory providers,
                                       String key,
                                       Provider<Long> property,
                                       Provider<Long> defaultValue) {
        longProvider(providers, toEnv(key), toProperty(key), property, resolvePropertyOrder(), defaultValue)
    }

    static Provider<Long> longProvider(ProviderFactory providers,
                                       String key,
                                       Provider<Long> property,
                                       Order order,
                                       Provider<Long> defaultValue) {
        longProvider(providers, toEnv(key), toProperty(key), property, order, defaultValue)
    }

    static Provider<Long> longProvider(ProviderFactory providers,
                                       String envKey,
                                       String propertyKey,
                                       Provider<Long> property,
                                       Provider<Long> defaultValue) {
        longProvider(providers, envKey, propertyKey, property, resolvePropertyOrder(), defaultValue)
    }

    static Provider<Long> longProvider(ProviderFactory providers,
                                       String envKey,
                                       String propertyKey,
                                       Provider<Long> property,
                                       Order order,
                                       Provider<Long> defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return Long.parseLong(value)
                    }
                    return defaultValue?.get()
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return Long.parseLong(value)
                    }
                    return property.orElse(defaultValue).get()
            }
        }
    }

    static Provider<String> stringProvider(ProviderFactory providers,
                                           String key,
                                           Provider<String> property,
                                           String defaultValue) {
        stringProvider(providers, toEnv(key), toProperty(key), property, resolvePropertyOrder(), defaultValue)
    }

    static Provider<String> stringProvider(ProviderFactory providers,
                                           String key,
                                           Provider<String> property,
                                           Order order,
                                           String defaultValue) {
        stringProvider(providers, toEnv(key), toProperty(key), property, order, defaultValue)
    }

    static Provider<String> stringProvider(ProviderFactory providers,
                                           String envKey,
                                           String propertyKey,
                                           Provider<String> property,
                                           String defaultValue) {
        stringProvider(providers, envKey, propertyKey, property, resolvePropertyOrder(), defaultValue)
    }

    static Provider<String> stringProvider(ProviderFactory providers,
                                           String envKey,
                                           String propertyKey,
                                           Provider<String> property,
                                           Order order,
                                           String defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return value
                    }
                    return defaultValue
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return value
                    }
                    return property.getOrElse(defaultValue)
            }
        }
    }

    static Provider<String> stringProvider(ProviderFactory providers,
                                           String key,
                                           Provider<String> property,
                                           Provider<String> defaultValue) {
        stringProvider(providers, toEnv(key), toProperty(key), property, resolvePropertyOrder(), defaultValue)
    }

    static Provider<String> stringProvider(ProviderFactory providers,
                                           String key,
                                           Provider<String> property,
                                           Order order,
                                           Provider<String> defaultValue) {
        stringProvider(providers, toEnv(key), toProperty(key), property, order, defaultValue)
    }

    static Provider<String> stringProvider(ProviderFactory providers,
                                           String envKey,
                                           String propertyKey,
                                           Provider<String> property,
                                           Provider<String> defaultValue) {
        stringProvider(providers, envKey, propertyKey, property, resolvePropertyOrder(), defaultValue)
    }

    static Provider<String> stringProvider(ProviderFactory providers,
                                           String envKey,
                                           String propertyKey,
                                           Provider<String> property,
                                           Order order,
                                           Provider<String> defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return value
                    }
                    return defaultValue?.get()
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return value
                    }
                    return property.orElse(defaultValue).get()
            }
        }
    }

    static Provider<List<String>> listProvider(ProviderFactory providers,
                                               String key,
                                               Provider<List<String>> property,
                                               List<String> defaultValue) {
        listProvider(providers, toEnv(key), toProperty(key), property, resolvePropertyOrder(), defaultValue)
    }

    static Provider<List<String>> listProvider(ProviderFactory providers,
                                               String key,
                                               Provider<List<String>> property,
                                               Order order,
                                               List<String> defaultValue) {
        listProvider(providers, toEnv(key), toProperty(key), property, order, defaultValue)
    }

    static Provider<List<String>> listProvider(ProviderFactory providers,
                                               String envKey,
                                               String propertyKey,
                                               Provider<List<String>> property,
                                               List<String> defaultValue) {
        listProvider(providers, envKey, propertyKey, property, resolvePropertyOrder(), defaultValue)
    }

    static Provider<List<String>> listProvider(ProviderFactory providers,
                                               String envKey,
                                               String propertyKey,
                                               Provider<List<String>> property,
                                               Order order,
                                               List<String> defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return convertToList(value)
                    }
                    return defaultValue
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return convertToList(value)
                    }
                    return property.getOrElse(defaultValue)
            }
        }
    }

    static Provider<List<String>> listProvider(ProviderFactory providers,
                                               String key,
                                               Provider<List<String>> property,
                                               Provider<List<String>> defaultValue) {
        listProvider(providers, toEnv(key), toProperty(key), property, resolvePropertyOrder(), defaultValue)
    }

    static Provider<List<String>> listProvider(ProviderFactory providers,
                                               String key,
                                               Provider<List<String>> property,
                                               Order order,
                                               Provider<List<String>> defaultValue) {
        listProvider(providers, toEnv(key), toProperty(key), property, order, defaultValue)
    }

    static Provider<List<String>> listProvider(ProviderFactory providers,
                                               String envKey,
                                               String propertyKey,
                                               Provider<List<String>> property,
                                               Provider<List<String>> defaultValue) {
        listProvider(providers, envKey, propertyKey, property, resolvePropertyOrder(), defaultValue)
    }

    static Provider<List<String>> listProvider(ProviderFactory providers,
                                               String envKey,
                                               String propertyKey,
                                               Provider<List<String>> property,
                                               Order order,
                                               Provider<List<String>> defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return convertToList(value)
                    }
                    return defaultValue?.get()
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return convertToList(value)
                    }
                    return property.orElse(defaultValue).get()
            }
        }
    }

    static Provider<Set<String>> setProvider(ProviderFactory providers,
                                             String key,
                                             Provider<Set<String>> property,
                                             Set<String> defaultValue) {
        setProvider(providers, toEnv(key), toProperty(key), property, resolvePropertyOrder(), defaultValue)
    }

    static Provider<Set<String>> setProvider(ProviderFactory providers,
                                             String key,
                                             Provider<Set<String>> property,
                                             Order order,
                                             Set<String> defaultValue) {
        setProvider(providers, toEnv(key), toProperty(key), property, order, defaultValue)
    }

    static Provider<Set<String>> setProvider(ProviderFactory providers,
                                             String envKey,
                                             String propertyKey,
                                             Provider<Set<String>> property,
                                             Set<String> defaultValue) {
        setProvider(providers, envKey, propertyKey, property, resolvePropertyOrder(), defaultValue)
    }

    static Provider<Set<String>> setProvider(ProviderFactory providers,
                                             String envKey,
                                             String propertyKey,
                                             Provider<Set<String>> property,
                                             Order order,
                                             Set<String> defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return convertToSet(value)
                    }
                    return defaultValue
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return convertToSet(value)
                    }
                    return property.getOrElse(defaultValue)
            }
        }
    }

    static Provider<Set<String>> setProvider(ProviderFactory providers,
                                             String key,
                                             Provider<Set<String>> property,
                                             Provider<Set<String>> defaultValue) {
        setProvider(providers, toEnv(key), toProperty(key), property, resolvePropertyOrder(), defaultValue)
    }

    static Provider<Set<String>> setProvider(ProviderFactory providers,
                                             String key,
                                             Provider<Set<String>> property,
                                             Order order,
                                             Provider<Set<String>> defaultValue) {
        setProvider(providers, toEnv(key), toProperty(key), property, order, defaultValue)
    }

    static Provider<Set<String>> setProvider(ProviderFactory providers,
                                             String envKey,
                                             String propertyKey,
                                             Provider<Set<String>> property,
                                             Provider<Set<String>> defaultValue) {
        setProvider(providers, envKey, propertyKey, property, resolvePropertyOrder(), defaultValue)
    }

    static Provider<Set<String>> setProvider(ProviderFactory providers,
                                             String envKey,
                                             String propertyKey,
                                             Provider<Set<String>> property,
                                             Order order,
                                             Provider<Set<String>> defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return convertToSet(value)
                    }
                    return defaultValue?.get()
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return convertToSet(value)
                    }
                    return property.orElse(defaultValue).get()
            }
        }
    }

    static Provider<Map<String, String>> mapProvider(ProviderFactory providers,
                                                     String key,
                                                     Provider<Map<String, String>> property,
                                                     Map<String, String> defaultValue) {
        mapProvider(providers, toEnv(key), toProperty(key), property, resolvePropertyOrder(), defaultValue)
    }

    static Provider<Map<String, String>> mapProvider(ProviderFactory providers,
                                                     String key,
                                                     Provider<Map<String, String>> property,
                                                     Order order,
                                                     Map<String, String> defaultValue) {
        mapProvider(providers, toEnv(key), toProperty(key), property, order, defaultValue)
    }

    static Provider<Map<String, String>> mapProvider(ProviderFactory providers,
                                                     String envKey,
                                                     String propertyKey,
                                                     Provider<Map<String, String>> property,
                                                     Map<String, String> defaultValue) {
        mapProvider(providers, envKey, propertyKey, property, resolvePropertyOrder(), defaultValue)
    }

    static Provider<Map<String, String>> mapProvider(ProviderFactory providers,
                                                     String envKey,
                                                     String propertyKey,
                                                     Provider<Map<String, String>> property,
                                                     Order order,
                                                     Map<String, String> defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return convertToMap(value)
                    }
                    return defaultValue
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return convertToMap(value)
                    }
                    return property.getOrElse(defaultValue)
            }
        }
    }

    static Provider<Map<String, String>> mapProvider(ProviderFactory providers,
                                                     String key,
                                                     Provider<Map<String, String>> property,
                                                     Provider<Map<String, String>> defaultValue) {
        mapProvider(providers, toEnv(key), toProperty(key), property, resolvePropertyOrder(), defaultValue)
    }

    static Provider<Map<String, String>> mapProvider(ProviderFactory providers,
                                                     String key,
                                                     Provider<Map<String, String>> property,
                                                     Order order,
                                                     Provider<Map<String, String>> defaultValue) {
        mapProvider(providers, toEnv(key), toProperty(key), property, order, defaultValue)
    }

    static Provider<Map<String, String>> mapProvider(ProviderFactory providers,
                                                     String envKey,
                                                     String propertyKey,
                                                     Provider<Map<String, String>> property,
                                                     Provider<Map<String, String>> defaultValue) {
        mapProvider(providers, envKey, propertyKey, property, resolvePropertyOrder(), defaultValue)
    }

    static Provider<Map<String, String>> mapProvider(ProviderFactory providers,
                                                     String envKey,
                                                     String propertyKey,
                                                     Provider<Map<String, String>> property,
                                                     Order order,
                                                     Provider<Map<String, String>> defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return convertToMap(value)
                    }
                    return defaultValue?.get()
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order)
                    if (isNotBlank(value)) {
                        return convertToMap(value)
                    }
                    return property.orElse(defaultValue).get()
            }
        }
    }

    /**
     * Creates a hierarchical boolean property.
     * Returns the {@code property}'s value if present, otherwise check's if the supplied
     * {@code provider} is not null and returns its value, otherwise returns the {@code defaultValue}.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param providers the {@code Provider} factory used to create a {@code Provider}.
     * @param property the property to use
     * @param provider the provider to use
     * @param defaultValue the default value in case neither property nor provider has one
     * @return property's value, provider's value, or the defaultValue, in that order.
     */
    static Provider<Boolean> booleanProvider(ProviderFactory providers,
                                             Property<Boolean> property,
                                             Provider<Boolean> provider,
                                             boolean defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    return property.getOrElse(provider ? provider.getOrElse(defaultValue) : defaultValue)
                case Priority.PROVIDER:
                    return provider ? provider.getOrElse(property.getOrElse(defaultValue)) : property.getOrElse(defaultValue)
            }
        }
    }

    static Provider<Boolean> booleanProvider(ProviderFactory providers,
                                             Property<Boolean> property,
                                             Provider<Boolean> provider,
                                             Provider<Boolean> defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    return property.orElse(provider ? provider.orElse(defaultValue) : defaultValue).get()
                case Priority.PROVIDER:
                    return (provider ? provider.orElse(property.orElse(defaultValue)) : property.orElse(defaultValue)).get()
            }
        }
    }

    /**
     * Creates a hierarchical String property.
     * Returns the {@code property}'s value if present, otherwise check's if the supplied
     * {@code provider} is not null and returns its value, otherwise returns the {@code defaultValue}.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param providers the {@code Provider} factory used to create a {@code Provider}.
     * @param property the property to use
     * @param provider the provider to use
     * @param defaultValue the default value in case neither property nor provider has one
     * @return property's value, provider's value, or the defaultValue, in that order.
     */
    static Provider<String> stringProvider(ProviderFactory providers,
                                           Property<String> property,
                                           Provider<String> provider,
                                           String defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    return property.getOrElse(provider ? provider.getOrElse(defaultValue) : defaultValue)
                case Priority.PROVIDER:
                    return provider ? provider.getOrElse(property.getOrElse(defaultValue)) : property.getOrElse(defaultValue)
            }
        }
    }

    static Provider<String> stringProvider(ProviderFactory providers,
                                           Property<String> property,
                                           Provider<String> provider,
                                           Provider<String> defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    return property.orElse(provider ? provider.orElse(defaultValue) : defaultValue).get()
                case Priority.PROVIDER:
                    return (provider ? provider.orElse(property.orElse(defaultValue)) : property.orElse(defaultValue)).get()
            }
        }
    }

    /**
     * Creates a hierarchical Object property.
     * Returns the {@code property}'s value if present, otherwise check's if the supplied
     * {@code provider} is not null and returns its value, otherwise returns the {@code defaultValue}.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param providers the {@code Provider} factory used to create a {@code Provider}.
     * @param property the property to use
     * @param provider the provider to use
     * @param defaultValue the default value in case neither property nor provider has one
     * @return property's value, provider's value, or the defaultValue, in that order.
     */
    static <T> Provider<T> objectProvider(ProviderFactory providers,
                                          Property<T> property,
                                          Provider<T> provider,
                                          T defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    return property.getOrElse(provider ? provider.getOrElse(defaultValue) : defaultValue)
                case Priority.PROVIDER:
                    return provider ? provider.getOrElse(property.getOrElse(defaultValue)) : property.getOrElse(defaultValue)
            }
        }
    }

    static <T> Provider<T> objectProvider(ProviderFactory providers,
                                          Property<T> property,
                                          Provider<T> provider,
                                          Provider<T> defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    return property.orElse(provider ? provider.orElse(defaultValue) : defaultValue).get()
                case Priority.PROVIDER:
                    return (provider ? provider.orElse(property.orElse(defaultValue)) : property.orElse(defaultValue)).get()
            }
        }
    }

    /**
     * Creates a hierarchical int property.
     * Returns the {@code property}'s value if present, otherwise check's if the supplied
     * {@code provider} is not null and returns its value, otherwise returns the {@code defaultValue}.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param providers the {@code Provider} factory used to create a {@code Provider}.
     * @param property the property to use
     * @param provider the provider to use
     * @param defaultValue the default value in case neither property nor provider has one
     * @return property's value, provider's value, or the defaultValue, in that order.
     */
    static Provider<Integer> integerProvider(ProviderFactory providers,
                                             Property<Integer> property,
                                             Provider<Integer> provider,
                                             int defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    return property.getOrElse(provider ? provider.getOrElse(defaultValue) : defaultValue)
                case Priority.PROVIDER:
                    return provider ? provider.getOrElse(property.getOrElse(defaultValue)) : property.getOrElse(defaultValue)
            }
        }
    }

    static Provider<Integer> integerProvider(ProviderFactory providers,
                                             Property<Integer> property,
                                             Provider<Integer> provider,
                                             Provider<Integer> defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    return property.orElse(provider ? provider.orElse(defaultValue) : defaultValue).get()
                case Priority.PROVIDER:
                    return (provider ? provider.orElse(property.orElse(defaultValue)) : property.orElse(defaultValue)).get()
            }
        }
    }

    /**
     * Creates a hierarchical long property.
     * Returns the {@code property}'s value if present, otherwise check's if the supplied
     * {@code provider} is not null and returns its value, otherwise returns the {@code defaultValue}.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param providers the {@code Provider} factory used to create a {@code Provider}.
     * @param property the property to use
     * @param provider the provider to use
     * @param defaultValue the default value in case neither property nor provider has one
     * @return property's value, provider's value, or the defaultValue, in that order.
     */
    static Provider<Long> longProvider(ProviderFactory providers,
                                       Property<Long> property,
                                       Provider<Long> provider,
                                       long defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    return property.getOrElse(provider ? provider.getOrElse(defaultValue) : defaultValue)
                case Priority.PROVIDER:
                    return provider ? provider.getOrElse(property.getOrElse(defaultValue)) : property.getOrElse(defaultValue)
            }
        }
    }

    static Provider<Long> longProvider(ProviderFactory providers,
                                       Property<Long> property,
                                       Provider<Long> provider,
                                       Provider<Long> defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    return property.orElse(provider ? provider.orElse(defaultValue) : defaultValue).get()
                case Priority.PROVIDER:
                    return (provider ? provider.orElse(property.orElse(defaultValue)) : property.orElse(defaultValue)).get()
            }
        }
    }

    /**
     * Creates a hierarchical File property.
     * Returns the {@code property}'s value if present, otherwise check's if the supplied
     * {@code provider} is not null and returns its value, otherwise returns the {@code defaultValue}.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param providers the {@code Provider} factory used to create a {@code Provider}.
     * @param property the property to use
     * @param provider the provider to use
     * @param defaultValue the default value in case neither property nor provider has one
     * @return property's value, provider's value, or the defaultValue, in that order.
     */
    static Provider<RegularFile> fileProvider(ProviderFactory providers,
                                              RegularFileProperty property,
                                              Provider<RegularFile> provider,
                                              RegularFile defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    return property.getOrElse(provider ? provider.getOrElse(defaultValue) : defaultValue)
                case Priority.PROVIDER:
                    return provider ? provider.getOrElse(property.getOrElse(defaultValue)) : property.getOrElse(defaultValue)
            }
        }
    }

    static Provider<RegularFile> fileProvider(ProviderFactory providers,
                                              RegularFileProperty property,
                                              Provider<RegularFile> provider,
                                              Provider<RegularFile> defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    return property.orElse(provider ? provider.orElse(defaultValue) : defaultValue).get()
                case Priority.PROVIDER:
                    return (provider ? provider.orElse(property.orElse(defaultValue)) : property.orElse(defaultValue)).get()
            }
        }
    }

    /**
     * Creates a hierarchical File (directory) property.
     * Returns the {@code property}'s value if present, otherwise check's if the supplied
     * {@code provider} is not null and returns its value, otherwise returns the {@code defaultValue}.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param providers the {@code Provider} factory used to create a {@code Provider}.
     * @param property the property to use
     * @param provider the provider to use
     * @param defaultValue the default value in case neither property nor provider has one
     * @return property's value, provider's value, or the defaultValue, in that order.
     */
    static Provider<Directory> directoryProvider(ProviderFactory providers,
                                                 DirectoryProperty property,
                                                 Provider<Directory> provider,
                                                 Directory defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    return property.getOrElse(provider ? provider.getOrElse(defaultValue) : defaultValue)
                case Priority.PROVIDER:
                    return provider ? provider.getOrElse(property.getOrElse(defaultValue)) : property.getOrElse(defaultValue)
            }
        }
    }

    static Provider<Directory> directoryProvider(ProviderFactory providers,
                                                 DirectoryProperty property,
                                                 Provider<Directory> provider,
                                                 Provider<Directory> defaultValue) {
        providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    return property.orElse(provider ? provider.orElse(defaultValue) : defaultValue).get()
                case Priority.PROVIDER:
                    return (provider ? provider.orElse(property.orElse(defaultValue)) : property.orElse(defaultValue)).get()
            }
        }
    }

    /**
     * Creates a hierarchical Map property.
     * Returns merged contents of {@code defaultValue}, provider's value, and property's value, in that order.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param providers the {@code Provider} factory used to create a {@code Provider}.
     * @param property the property to use
     * @param provider the provider to use
     * @param defaultValue the default value in case neither property nor provider has one
     * @return merged contents of the supplied arguments.
     */
    static <K, V> Provider<Map<K, V>> mapProvider(ProviderFactory providers,
                                                  MapProperty<K, V> property,
                                                  Provider<Map<K, V>> provider,
                                                  Map<K, V> defaultValue) {
        providers.provider {
            Map<K, V> map = new LinkedHashMap<>(defaultValue)

            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (provider) {
                        map.putAll(provider.get())
                    }
                    if (property.present) {
                        map.putAll(property.get())
                    }
                    break
                case Priority.PROVIDER:
                    if (property.present) {
                        map.putAll(property.get())
                    }
                    if (provider) {
                        map.putAll(provider.get())
                    }
            }

            return map
        }
    }

    static <K, V> Provider<Map<K, V>> mapProvider(ProviderFactory providers,
                                                  MapProperty<K, V> property,
                                                  Provider<Map<K, V>> provider,
                                                  Provider<Map<K, V>> defaultValue) {
        providers.provider {
            Map<K, V> map = new LinkedHashMap<>()
            if (defaultValue && defaultValue.present) {
                map.putAll(defaultValue.get())
            }

            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (provider) {
                        map.putAll(provider.get())
                    }
                    if (property.present) {
                        map.putAll(property.get())
                    }
                    break
                case Priority.PROVIDER:
                    if (property.present) {
                        map.putAll(property.get())
                    }
                    if (provider) {
                        map.putAll(provider.get())
                    }
            }

            return map
        }
    }

    /**
     * Creates a hierarchical List property.
     * Returns merged contents of {@code defaultValue}, provider's value, and property's value, in that order.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param providers the {@code Provider} factory used to create a {@code Provider}.
     * @param property the property to use
     * @param provider the provider to use
     * @param defaultValue the default value in case neither property nor provider has one
     * @return merged contents of the supplied arguments.
     */
    static <E> Provider<List<E>> listProvider(ProviderFactory providers,
                                              ListProperty<E> property,
                                              Provider<List<E>> provider,
                                              List<E> defaultValue) {
        providers.provider {
            List<E> list = new ArrayList<>(defaultValue)

            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (provider) {
                        list.addAll(provider.get())
                    }
                    if (property.present) {
                        for (E e : property.get()) {
                            if (!list.contains(e)) {
                                list.add(e)
                            }
                        }
                    }
                    break
                case Priority.PROVIDER:
                    if (property.present) {
                        for (E e : property.get()) {
                            if (!list.contains(e)) {
                                list.add(e)
                            }
                        }
                    }
                    if (provider) {
                        list.addAll(provider.get())
                    }
            }

            return list
        }
    }

    static <E> Provider<List<E>> listProvider(ProviderFactory providers,
                                              ListProperty<E> property,
                                              Provider<List<E>> provider,
                                              Provider<List<E>> defaultValue) {
        providers.provider {
            List<E> list = new ArrayList<>()
            if (defaultValue && defaultValue.present) {
                list.addAll(defaultValue.get())
            }

            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (provider) {
                        list.addAll(provider.get())
                    }
                    if (property.present) {
                        for (E e : property.get()) {
                            if (!list.contains(e)) {
                                list.add(e)
                            }
                        }
                    }
                    break
                case Priority.PROVIDER:
                    if (property.present) {
                        for (E e : property.get()) {
                            if (!list.contains(e)) {
                                list.add(e)
                            }
                        }
                    }
                    if (provider) {
                        list.addAll(provider.get())
                    }
            }

            return list
        }
    }

    /**
     * Creates a hierarchical Set property.
     * Returns merged contents of {@code defaultValue}, provider's value, and property's value, in that order.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param providers the {@code Provider} factory used to create a {@code Provider}.
     * @param property the property to use
     * @param provider the provider to use
     * @param defaultValue the default value in case neither property nor provider has one
     * @return merged contents of the supplied arguments.
     */
    static <E> Provider<Set<E>> setProvider(ProviderFactory providers,
                                            SetProperty<E> property,
                                            Provider<Set<E>> provider,
                                            Set<E> defaultValue) {
        providers.provider {
            Set<E> set = new LinkedHashSet<>(defaultValue)

            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (provider) {
                        set.addAll(provider.get())
                    }
                    if (property.present) {
                        for (E e : property.get()) {
                            if (!set.contains(e)) {
                                set.add(e)
                            }
                        }
                    }
                    break
                case Priority.PROVIDER:
                    if (property.present) {
                        for (E e : property.get()) {
                            if (!set.contains(e)) {
                                set.add(e)
                            }
                        }
                    }
                    if (provider) {
                        set.addAll(provider.get())
                    }
            }

            return set
        }
    }

    static <E> Provider<Set<E>> setProvider(ProviderFactory providers,
                                            SetProperty<E> property,
                                            Provider<Set<E>> provider,
                                            Provider<Set<E>> defaultValue) {
        providers.provider {
            Set<E> set = new LinkedHashSet<>()
            if (defaultValue && defaultValue.present) {
                set.addAll(defaultValue.get())
            }

            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (provider) {
                        set.addAll(provider.get())
                    }
                    if (property.present) {
                        for (E e : property.get()) {
                            if (!set.contains(e)) {
                                set.add(e)
                            }
                        }
                    }
                    break
                case Priority.PROVIDER:
                    if (property.present) {
                        for (E e : property.get()) {
                            if (!set.contains(e)) {
                                set.add(e)
                            }
                        }
                    }
                    if (provider) {
                        set.addAll(provider.get())
                    }
            }

            return set
        }
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param envKey the environment variable to check.
     * @param propertyKey the system and project property to check.
     * @param property the property to check value from.
     * @param order resolving order to use.
     * @param path resolving path to use.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on the {@code order} argument.
     */
    static Provider<String> stringProvider(String envKey,
                                           String propertyKey,
                                           Property<String> property,
                                           Order order,
                                           Path path,
                                           Project project,
                                           Object owner,
                                           String defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    return isNotBlank(value) ? value : defaultValue
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    return isNotBlank(value) ? value : property.getOrElse(defaultValue)
            }
        }
    }

    static Provider<String> stringProvider(String envKey,
                                           String propertyKey,
                                           Property<String> property,
                                           Order order,
                                           Path path,
                                           Project project,
                                           Object owner,
                                           Provider<String> defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    return isNotBlank(value) ? value : defaultValue?.get()
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    return isNotBlank(value) ? value : (defaultValue ? property.orElse(defaultValue).get() : property.get())
            }
        }
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param order resolving order to use.
     * @param path resolving path to use.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on the {@code order} argument.
     */
    static Provider<String> stringProvider(String key,
                                           Property<String> property,
                                           Order order,
                                           Path path,
                                           Project project,
                                           Object owner,
                                           String defaultValue) {
        stringProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
    }

    static Provider<String> stringProvider(String key,
                                           Property<String> property,
                                           Order order,
                                           Path path,
                                           Project project,
                                           Object owner,
                                           Provider<String> defaultValue) {
        stringProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Order} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_ORDER} System
     * property. Defaults to {@code ENV_SYS_PROP}.
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     *
     * @param envKey the environment variable to check.
     * @param propertyKey the system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<String> stringProvider(String envKey,
                                           String propertyKey,
                                           Property<String> property,
                                           Project project,
                                           Object owner) {
        stringProvider(envKey, propertyKey, property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, (String) null)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<String> stringProvider(String key,
                                           Property<String> property,
                                           Order order,
                                           Project project,
                                           Object owner) {
        stringProvider(toEnv(key), toProperty(key), property, order, resolvePropertyPath(), project, owner, (String) null)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Order} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_ORDER} System
     * property. Defaults to {@code ENV_SYS_PROP}.
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<String> stringProvider(String key,
                                           Property<String> property,
                                           Project project,
                                           Object owner) {
        stringProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, (String) null)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Order} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_ORDER} System
     * property. Defaults to {@code ENV_SYS_PROP}.
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<String> stringProvider(String key,
                                           Property<String> property,
                                           Project project,
                                           Object owner,
                                           String defaultValue) {
        stringProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
    }

    static Provider<String> stringProvider(String key,
                                           Property<String> property,
                                           Project project,
                                           Object owner,
                                           Provider<String> defaultValue) {
        stringProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param envKey the environment variable to check.
     * @param propertyKey the system and project property to check.
     * @param property the property to check value from.
     * @param order resolving order to use.
     * @param path resolving path to use.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on the {@code order} argument.
     */
    static Provider<Boolean> booleanProvider(String envKey,
                                             String propertyKey,
                                             Property<Boolean> property,
                                             Order order,
                                             Path path,
                                             Project project,
                                             Object owner,
                                             boolean defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    return isNotBlank(value) ? Boolean.parseBoolean(value) : defaultValue
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    return isNotBlank(value) ? Boolean.parseBoolean(value) : property.getOrElse(defaultValue)
            }
        }
    }

    static Provider<Boolean> booleanProvider(String envKey,
                                             String propertyKey,
                                             Property<Boolean> property,
                                             Order order,
                                             Path path,
                                             Project project,
                                             Object owner,
                                             Provider<Boolean> defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    return isNotBlank(value) ? Boolean.parseBoolean(value) : defaultValue.get()
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    return isNotBlank(value) ? Boolean.parseBoolean(value) : property.orElse(defaultValue).get()
            }
        }
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param order resolving order to use.
     * @param path resolving path to use.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on the {@code order} argument.
     */
    static Provider<Boolean> booleanProvider(String key,
                                             Property<Boolean> property,
                                             Order order,
                                             Path path,
                                             Project project,
                                             Object owner,
                                             boolean defaultValue) {
        booleanProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
    }

    static Provider<Boolean> booleanProvider(String key,
                                             Property<Boolean> property,
                                             Order order,
                                             Path path,
                                             Project project,
                                             Object owner,
                                             Provider<Boolean> defaultValue) {
        booleanProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Order} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_ORDER} System
     * property. Defaults to {@code ENV_SYS_PROP}.
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     *
     * @param envKey the environment variable to check.
     * @param propertyKey the system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<Boolean> booleanProvider(String envKey,
                                             String propertyKey,
                                             Property<Boolean> property,
                                             Project project,
                                             Object owner) {
        booleanProvider(envKey, propertyKey, property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, false)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<Boolean> booleanProvider(String key,
                                             Property<Boolean> property,
                                             Project project,
                                             Object owner) {
        booleanProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, false)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<Boolean> booleanProvider(String key,
                                             Property<Boolean> property,
                                             Project project,
                                             Object owner,
                                             boolean defaultValue) {
        booleanProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
    }

    static Provider<Boolean> booleanProvider(String key,
                                             Property<Boolean> property,
                                             Project project,
                                             Object owner,
                                             Provider<Boolean> defaultValue) {
        booleanProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Order} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_ORDER} System
     * property. Defaults to {@code ENV_SYS_PROP}.
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<Boolean> booleanProvider(String key,
                                             Property<Boolean> property,
                                             Order order,
                                             Project project,
                                             Object owner) {
        booleanProvider(toEnv(key), toProperty(key), property, order, resolvePropertyPath(), project, owner, false)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     *
     * @param envKey the environment variable to check.
     * @param propertyKey the system and project property to check.
     * @param property the property to check value from.
     * @param order resolving order to use.
     * @param path resolving path to use.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on the {@code order} argument.
     */
    static Provider<Integer> integerProvider(String envKey,
                                             String propertyKey,
                                             Property<Integer> property,
                                             Order order,
                                             Path path,
                                             Project project,
                                             Object owner,
                                             int defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    return isNotBlank(value) ? Integer.parseInt(value) : defaultValue
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    return isNotBlank(value) ? Integer.parseInt(value) : property.getOrElse(defaultValue)
            }
        }
    }

    static Provider<Integer> integerProvider(String envKey,
                                             String propertyKey,
                                             Property<Integer> property,
                                             Order order,
                                             Path path,
                                             Project project,
                                             Object owner,
                                             Provider<Integer> defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    return isNotBlank(value) ? Integer.parseInt(value) : defaultValue.get()
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    return isNotBlank(value) ? Integer.parseInt(value) : property.orElse(defaultValue).get()
            }
        }
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param order resolving order to use.
     * @param path resolving path to use.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on the {@code order} argument.
     */
    static Provider<Integer> integerProvider(String key,
                                             Property<Integer> property,
                                             Order order,
                                             Path path,
                                             Project project,
                                             Object owner,
                                             int defaultValue) {
        integerProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
    }

    static Provider<Integer> integerProvider(String key,
                                             Property<Integer> property,
                                             Order order,
                                             Path path,
                                             Project project,
                                             Object owner,
                                             Provider<Integer> defaultValue) {
        integerProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Order} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_ORDER} System
     * property. Defaults to {@code ENV_SYS_PROP}.
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     *
     * @param envKey the environment variable to check.
     * @param propertyKey the system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<Integer> integerProvider(String envKey,
                                             String propertyKey,
                                             Property<Integer> property,
                                             Project project,
                                             Object owner) {
        integerProvider(envKey, propertyKey, property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, 0)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<Integer> integerProvider(String key,
                                             Property<Integer> property,
                                             Project project,
                                             Object owner) {
        integerProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, 0)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<Integer> integerProvider(String key,
                                             Property<Integer> property,
                                             Project project,
                                             Object owner,
                                             int defaultValue) {
        integerProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
    }

    static Provider<Integer> integerProvider(String key,
                                             Property<Integer> property,
                                             Project project,
                                             Object owner,
                                             Provider<Integer> defaultValue) {
        integerProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Order} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_ORDER} System
     * property. Defaults to {@code ENV_SYS_PROP}.
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<Integer> integerProvider(String key,
                                             Property<Integer> property,
                                             Order order,
                                             Project project,
                                             Object owner) {
        integerProvider(toEnv(key), toProperty(key), property, order, resolvePropertyPath(), project, owner, 0)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param envKey the environment variable to check.
     * @param propertyKey the system and project property to check.
     * @param property the property to check value from.
     * @param order resolving order to use.
     * @param path resolving path to use.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on the {@code order} argument.
     */
    static Provider<Long> longProvider(String envKey,
                                       String propertyKey,
                                       Property<Long> property,
                                       Order order,
                                       Path path,
                                       Project project,
                                       Object owner,
                                       long defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    return isNotBlank(value) ? Long.parseLong(value) : defaultValue
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    return isNotBlank(value) ? Long.parseLong(value) : property.getOrElse(defaultValue)
            }
        }
    }

    static Provider<Long> longProvider(String envKey,
                                       String propertyKey,
                                       Property<Long> property,
                                       Order order,
                                       Path path,
                                       Project project,
                                       Object owner,
                                       Provider<Long> defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    return isNotBlank(value) ? Long.parseLong(value) : defaultValue.get()
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    return isNotBlank(value) ? Long.parseLong(value) : property.orElse(defaultValue).get()
            }
        }
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param order resolving order to use.
     * @param path resolving path to use.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on the {@code order} argument.
     */
    static Provider<Long> longProvider(String key,
                                       Property<Long> property,
                                       Order order,
                                       Path path,
                                       Project project,
                                       Object owner,
                                       long defaultValue) {
        longProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
    }

    static Provider<Long> longProvider(String key,
                                       Property<Long> property,
                                       Order order,
                                       Path path,
                                       Project project,
                                       Object owner,
                                       Provider<Long> defaultValue) {
        longProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Order} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_ORDER} System
     * property. Defaults to {@code ENV_SYS_PROP}.
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     *
     * @param envKey the environment variable to check.
     * @param propertyKey the system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<Long> longProvider(String envKey,
                                       String propertyKey,
                                       Property<Long> property,
                                       Project project,
                                       Object owner) {
        longProvider(envKey, propertyKey, property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, 0L)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<Long> longProvider(String key,
                                       Property<Long> property,
                                       Project project,
                                       Object owner) {
        longProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, 0L)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<Long> longProvider(String key,
                                       Property<Long> property,
                                       Project project,
                                       Object owner,
                                       long defaultValue) {
        longProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
    }

    static Provider<Long> longProvider(String key,
                                       Property<Long> property,
                                       Project project,
                                       Object owner,
                                       Provider<Long> defaultValue) {
        longProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Order} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_ORDER} System
     * property. Defaults to {@code ENV_SYS_PROP}.
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<Long> longProvider(String key,
                                       Property<Long> property,
                                       Order order,
                                       Project project,
                                       Object owner) {
        longProvider(toEnv(key), toProperty(key), property, order, resolvePropertyPath(), project, owner, 0L)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param envKey the environment variable to check.
     * @param propertyKey the system and project property to check.
     * @param property the property to check value from.
     * @param order resolving order to use.
     * @param path resolving path to use.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on the {@code order} argument.
     */
    static Provider<RegularFile> fileProvider(String envKey,
                                              String propertyKey,
                                              RegularFileProperty property,
                                              Order order,
                                              Path path,
                                              Project project,
                                              Object owner,
                                              RegularFile defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    if (isNotBlank(value)) {
                        RegularFileProperty p = project.objects.fileProperty()
                        p.set(Paths.get(value).toFile())
                        return p.get()
                    }
                    return defaultValue
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    if (isNotBlank(value)) {
                        RegularFileProperty p = project.objects.fileProperty()
                        p.set(Paths.get(value).toFile())
                        return p.get()
                    }
                    return property.getOrElse(defaultValue)
            }
        }
    }

    static Provider<RegularFile> fileProvider(String envKey,
                                              String propertyKey,
                                              RegularFileProperty property,
                                              Order order,
                                              Path path,
                                              Project project,
                                              Object owner,
                                              Provider<RegularFile> defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    if (isNotBlank(value)) {
                        RegularFileProperty p = project.objects.fileProperty()
                        p.set(Paths.get(value).toFile())
                        return p.get()
                    }
                    return defaultValue.get()
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    if (isNotBlank(value)) {
                        RegularFileProperty p = project.objects.fileProperty()
                        p.set(Paths.get(value).toFile())
                        return p.get()
                    }
                    return property.orElse(defaultValue).get()
            }
        }
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param order resolving order to use.
     * @param path resolving path to use.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on the {@code order} argument.
     */
    static Provider<RegularFile> fileProvider(String key,
                                              RegularFileProperty property,
                                              Order order,
                                              Path path,
                                              Project project,
                                              Object owner,
                                              RegularFile defaultValue) {
        fileProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
    }

    static Provider<RegularFile> fileProvider(String key,
                                              RegularFileProperty property,
                                              Order order,
                                              Path path,
                                              Project project,
                                              Object owner,
                                              Provider<RegularFile> defaultValue) {
        fileProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Order} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_ORDER} System
     * property. Defaults to {@code ENV_SYS_PROP}.
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     *
     * @param envKey the environment variable to check.
     * @param propertyKey the system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<RegularFile> fileProvider(String envKey,
                                              String propertyKey,
                                              RegularFileProperty property,
                                              Project project,
                                              Object owner) {
        fileProvider(envKey, propertyKey, property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, (RegularFile) null)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<RegularFile> fileProvider(String key,
                                              RegularFileProperty property,
                                              Project project,
                                              Object owner) {
        fileProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, (RegularFile) null)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<RegularFile> fileProvider(String key,
                                              RegularFileProperty property,
                                              Project project,
                                              Object owner,
                                              RegularFile defaultValue) {
        fileProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
    }

    static Provider<RegularFile> fileProvider(String key,
                                              RegularFileProperty property,
                                              Project project,
                                              Object owner,
                                              Provider<RegularFile> defaultValue) {
        fileProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Order} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_ORDER} System
     * property. Defaults to {@code ENV_SYS_PROP}.
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<RegularFile> fileProvider(String key,
                                              RegularFileProperty property,
                                              Order order,
                                              Project project,
                                              Object owner) {
        fileProvider(toEnv(key), toProperty(key), property, order, resolvePropertyPath(), project, owner, (RegularFile) null)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param envKey the environment variable to check.
     * @param propertyKey the system and project property to check.
     * @param property the property to check value from.
     * @param order resolving order to use.
     * @param path resolving path to use.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on the {@code order} argument.
     */
    static Provider<Directory> directoryProvider(String envKey,
                                                 String propertyKey,
                                                 DirectoryProperty property,
                                                 Order order,
                                                 Path path,
                                                 Project project,
                                                 Object owner,
                                                 Directory defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    if (isNotBlank(value)) {
                        DirectoryProperty p = project.objects.directoryProperty()
                        p.set(Paths.get(value).toFile())
                        return p.get()
                    }
                    return defaultValue
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    if (isNotBlank(value)) {
                        DirectoryProperty p = project.objects.directoryProperty()
                        p.set(Paths.get(value).toFile())
                        return p.get()
                    }
                    return property.getOrElse(defaultValue)
            }
        }
    }

    static Provider<Directory> directoryProvider(String envKey,
                                                 String propertyKey,
                                                 DirectoryProperty property,
                                                 Order order,
                                                 Path path,
                                                 Project project,
                                                 Object owner,
                                                 Provider<Directory> defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    if (isNotBlank(value)) {
                        DirectoryProperty p = project.objects.directoryProperty()
                        p.set(Paths.get(value).toFile())
                        return p.get()
                    }
                    return defaultValue.get()
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    if (isNotBlank(value)) {
                        DirectoryProperty p = project.objects.directoryProperty()
                        p.set(Paths.get(value).toFile())
                        return p.get()
                    }
                    return property.orElse(defaultValue).get()
            }
        }
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param order resolving order to use.
     * @param path resolving path to use.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on the {@code order} argument.
     */
    static Provider<Directory> directoryProvider(String key,
                                                 DirectoryProperty property,
                                                 Order order,
                                                 Path path,
                                                 Project project,
                                                 Object owner,
                                                 Directory defaultValue) {
        directoryProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
    }

    static Provider<Directory> directoryProvider(String key,
                                                 DirectoryProperty property,
                                                 Order order,
                                                 Path path,
                                                 Project project,
                                                 Object owner,
                                                 Provider<Directory> defaultValue) {
        directoryProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Order} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_ORDER} System
     * property. Defaults to {@code ENV_SYS_PROP}.
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     *
     * @param envKey the environment variable to check.
     * @param propertyKey the system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<Directory> directoryProvider(String envKey,
                                                 String propertyKey,
                                                 DirectoryProperty property,
                                                 Project project,
                                                 Object owner) {
        directoryProvider(envKey, propertyKey, property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, (Directory) null)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<Directory> directoryProvider(String key,
                                                 DirectoryProperty property,
                                                 Project project,
                                                 Object owner) {
        directoryProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, (Directory) null)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<Directory> directoryProvider(String key,
                                                 DirectoryProperty property,
                                                 Project project,
                                                 Object owner,
                                                 Directory defaultValue) {
        directoryProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
    }

    static Provider<Directory> directoryProvider(String key,
                                                 DirectoryProperty property,
                                                 Project project,
                                                 Object owner,
                                                 Provider<Directory> defaultValue) {
        directoryProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Order} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_ORDER} System
     * property. Defaults to {@code ENV_SYS_PROP}.
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<Directory> directoryProvider(String key,
                                                 DirectoryProperty property,
                                                 Order order,
                                                 Project project,
                                                 Object owner) {
        directoryProvider(toEnv(key), toProperty(key), property, order, resolvePropertyPath(), project, owner, (Directory) null)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param envKey the environment variable to check.
     * @param propertyKey the system and project property to check.
     * @param property the property to check value from.
     * @param order resolving order to use.
     * @param path resolving path to use.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on the {@code order} argument.
     */
    @CompileDynamic
    static Provider<Set<String>> setProvider(String envKey,
                                             String propertyKey,
                                             SetProperty<String> property,
                                             Order order,
                                             Path path,
                                             Project project,
                                             Object owner,
                                             Set<String> defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    if (isNotBlank(value)) {
                        Set<String> set = new LinkedHashSet<>()
                        for (String v : value.split(',')) {
                            if (isNotBlank(v)) set.add(v.trim())
                        }
                        return set
                    }
                    return defaultValue
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    if (isNotBlank(value)) {
                        Set<String> set = new LinkedHashSet<>()
                        for (String v : value.split(',')) {
                            if (isNotBlank(v)) set.add(v.trim())
                        }
                        return set
                    }
                    return property.getOrElse(defaultValue)
            }
        }
    }

    @CompileDynamic
    static Provider<Set<String>> setProvider(String envKey,
                                             String propertyKey,
                                             SetProperty<String> property,
                                             Order order,
                                             Path path,
                                             Project project,
                                             Object owner,
                                             Provider<Set<String>> defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    if (isNotBlank(value)) {
                        Set<String> set = new LinkedHashSet<>()
                        for (String v : value.split(',')) {
                            if (isNotBlank(v)) set.add(v.trim())
                        }
                        return set
                    }
                    return defaultValue.get()
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    if (isNotBlank(value)) {
                        Set<String> set = new LinkedHashSet<>()
                        for (String v : value.split(',')) {
                            if (isNotBlank(v)) set.add(v.trim())
                        }
                        return set
                    }
                    return property.orElse(defaultValue).get()
            }
        }
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param order resolving order to use.
     * @param path resolving path to use.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on the {@code order} argument.
     */
    static Provider<Set<String>> setProvider(String key,
                                             SetProperty<String> property,
                                             Order order,
                                             Path path,
                                             Project project,
                                             Object owner,
                                             Set<String> defaultValue) {
        setProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
    }

    static Provider<Set<String>> setProvider(String key,
                                             SetProperty<String> property,
                                             Order order,
                                             Path path,
                                             Project project,
                                             Object owner,
                                             Provider<Set<String>> defaultValue) {
        setProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Order} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_ORDER} System
     * property. Defaults to {@code ENV_SYS_PROP}.
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     *
     * @param envKey the environment variable to check.
     * @param propertyKey the system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<Set<String>> setProvider(String envKey,
                                             String propertyKey,
                                             SetProperty<String> property,
                                             Project project,
                                             Object owner) {
        setProvider(envKey, propertyKey, property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, Collections.<String> emptySet())
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<Set<String>> setProvider(String key,
                                             SetProperty<String> property,
                                             Project project,
                                             Object owner) {
        setProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, Collections.<String> emptySet())
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<Set<String>> setProvider(String key,
                                             SetProperty<String> property,
                                             Project project,
                                             Object owner,
                                             Set<String> defaultValue) {
        setProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
    }

    static Provider<Set<String>> setProvider(String key,
                                             SetProperty<String> property,
                                             Project project,
                                             Object owner,
                                             Provider<Set<String>> defaultValue) {
        setProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Order} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_ORDER} System
     * property. Defaults to {@code ENV_SYS_PROP}.
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<Set<String>> setProvider(String key,
                                             SetProperty<String> property,
                                             Order order,
                                             Project project,
                                             Object owner) {
        setProvider(toEnv(key), toProperty(key), property, order, resolvePropertyPath(), project, owner, Collections.<String> emptySet())
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param envKey the environment variable to check.
     * @param propertyKey the system and project property to check.
     * @param property the property to check value from.
     * @param order resolving order to use.
     * @param path resolving path to use.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on the {@code order} argument.
     */
    @CompileDynamic
    static Provider<List<String>> listProvider(String envKey,
                                               String propertyKey,
                                               ListProperty<String> property,
                                               Order order,
                                               Path path,
                                               Project project,
                                               Object owner,
                                               List<String> defaultValue) {
        project.providers.provider {
            if (property.present) return property.get()
            String value = resolveValue(envKey, propertyKey, order, path, project, owner)
            if (isNotBlank(value)) {
                List<String> list = new ArrayList<>()
                for (String v : value.split(',')) {
                    if (isNotBlank(v)) list.add(v.trim())
                }
                return list
            }
            defaultValue
        }
    }

    @CompileDynamic
    static Provider<List<String>> listProvider(String envKey,
                                               String propertyKey,
                                               ListProperty<String> property,
                                               Order order,
                                               Path path,
                                               Project project,
                                               Object owner,
                                               Provider<List<String>> defaultValue) {
        project.providers.provider {
            if (property.present) return property.get()
            String value = resolveValue(envKey, propertyKey, order, path, project, owner)
            if (isNotBlank(value)) {
                List<String> list = new ArrayList<>()
                for (String v : value.split(',')) {
                    if (isNotBlank(v)) list.add(v.trim())
                }
                return list
            }
            defaultValue.get()
        }
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param order resolving order to use.
     * @param path resolving path to use.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if list or a value resolved based on the {@code order} argument.
     */
    static Provider<List<String>> listProvider(String key,
                                               ListProperty<String> property,
                                               Order order,
                                               Path path,
                                               Project project,
                                               Object owner,
                                               List<String> defaultValue) {
        listProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
    }

    static Provider<List<String>> listProvider(String key,
                                               ListProperty<String> property,
                                               Order order,
                                               Path path,
                                               Project project,
                                               Object owner,
                                               Provider<List<String>> defaultValue) {
        listProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Order} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_ORDER} System
     * property. Defaults to {@code ENV_SYS_PROP}.
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     *
     * @param envKey the environment variable to check.
     * @param propertyKey the system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<List<String>> listProvider(String envKey,
                                               String propertyKey,
                                               ListProperty<String> property,
                                               Project project,
                                               Object owner) {
        listProvider(envKey, propertyKey, property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, Collections.<String> emptyList())
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<List<String>> listProvider(String key,
                                               ListProperty<String> property,
                                               Project project,
                                               Object owner) {
        listProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, Collections.<String> emptyList())
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<List<String>> listProvider(String key,
                                               ListProperty<String> property,
                                               Project project,
                                               Object owner,
                                               List<String> defaultValue) {
        listProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
    }

    static Provider<List<String>> listProvider(String key,
                                               ListProperty<String> property,
                                               Project project,
                                               Object owner,
                                               Provider<List<String>> defaultValue) {
        listProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Order} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_ORDER} System
     * property. Defaults to {@code ENV_SYS_PROP}.
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<List<String>> listProvider(String key,
                                               ListProperty<String> property,
                                               Order order,
                                               Project project,
                                               Object owner) {
        listProvider(toEnv(key), toProperty(key), property, order, resolvePropertyPath(), project, owner, Collections.<String> emptyList())
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param envKey the environment variable to check.
     * @param propertyKey the system and project property to check.
     * @param property the property to check value from.
     * @param order resolving order to use.
     * @param path resolving path to use.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on the {@code order} argument.
     */
    @CompileDynamic
    static Provider<Map<String, String>> mapProvider(String envKey,
                                                     String propertyKey,
                                                     MapProperty<String, String> property,
                                                     Order order,
                                                     Path path,
                                                     Project project,
                                                     Object owner,
                                                     Map<String, String> defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    if (isNotBlank(value)) {
                        Map<String, String> map = new LinkedHashMap<>()
                        for (String val : value.split(',')) {
                            String[] kv = val.split('=')
                            if (kv.length == 2) {
                                map.put(kv[0], kv[1])
                            }
                        }
                        return map
                    }
                    return defaultValue
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    if (isNotBlank(value)) {
                        Map<String, String> map = new LinkedHashMap<>()
                        for (String val : value.split(',')) {
                            String[] kv = val.split('=')
                            if (kv.length == 2) {
                                map.put(kv[0], kv[1])
                            }
                        }
                        return map
                    }
                    return property.getOrElse(defaultValue)
            }
        }
    }

    @CompileDynamic
    static Provider<Map<String, String>> mapProvider(String envKey,
                                                     String propertyKey,
                                                     MapProperty<String, String> property,
                                                     Order order,
                                                     Path path,
                                                     Project project,
                                                     Object owner,
                                                     Provider<Map<String, String>> defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    if (isNotBlank(value)) {
                        Map<String, String> map = new LinkedHashMap<>()
                        for (String val : value.split(',')) {
                            String[] kv = val.split('=')
                            if (kv.length == 2) {
                                map.put(kv[0], kv[1])
                            }
                        }
                        return map
                    }
                    return defaultValue.get()
                case Priority.PROVIDER:
                    String value = resolveValue(envKey, propertyKey, order, path, project, owner)
                    if (isNotBlank(value)) {
                        Map<String, String> map = new LinkedHashMap<>()
                        for (String val : value.split(',')) {
                            String[] kv = val.split('=')
                            if (kv.length == 2) {
                                map.put(kv[0], kv[1])
                            }
                        }
                        return map
                    }
                    return property.orElse(defaultValue).get()
            }
        }
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param order resolving order to use.
     * @param path resolving path to use.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if map or a value resolved based on the {@code order} argument.
     */
    static Provider<Map<String, String>> mapProvider(String key,
                                                     MapProperty<String, String> property,
                                                     Order order,
                                                     Path path,
                                                     Project project,
                                                     Object owner,
                                                     Map<String, String> defaultValue) {
        mapProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
    }

    static Provider<Map<String, String>> mapProvider(String key,
                                                     MapProperty<String, String> property,
                                                     Order order,
                                                     Path path,
                                                     Project project,
                                                     Object owner,
                                                     Provider<Map<String, String>> defaultValue) {
        mapProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Order} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_ORDER} System
     * property. Defaults to {@code ENV_SYS_PROP}.
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     *
     * @param envKey the environment variable to check.
     * @param propertyKey the system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<Map<String, String>> mapProvider(String envKey,
                                                     String propertyKey,
                                                     MapProperty<String, String> property,
                                                     Project project,
                                                     Object owner) {
        mapProvider(envKey, propertyKey, property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, Collections.<String, String> emptyMap())
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<Map<String, String>> mapProvider(String key,
                                                     MapProperty<String, String> property,
                                                     Project project,
                                                     Object owner) {
        mapProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, Collections.<String, String> emptyMap())
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<Map<String, String>> mapProvider(String key,
                                                     MapProperty<String, String> property,
                                                     Project project,
                                                     Object owner,
                                                     Map<String, String> defaultValue) {
        mapProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
    }

    static Provider<Map<String, String>> mapProvider(String key,
                                                     MapProperty<String, String> property,
                                                     Project project,
                                                     Object owner,
                                                     Provider<Map<String, String>> defaultValue) {
        mapProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.</p>
     * The resolution priority between {@code property} and {@code provider} is governed by the
     * {@code PropertyUtils.KEY_PROPERTY_PRIORITY} System property whose default value is {@code PROPERTY}.
     *
     * The value of {@code Order} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_ORDER} System
     * property. Defaults to {@code ENV_SYS_PROP}.
     * The value of {@code Path} will be resolved from the {@code PropertyUtils.KEY_PROPERTY_PATH} System
     * property. Defaults to {@code GLOBAL_PROJECT_OWNER}.
     * The value of key will be converted to uppercase and '.' replaced with '_' for constructing the environment variable key.
     * The value of key will be converted to lowercase and '_' replaced with '.' for constructing the property key.
     *
     * @param key the environment variable, system and project property to check.
     * @param property the property to check value from.
     * @param project a project.
     * @param owner the Object that owns the property.
     * @return the property's value if set or a value resolved based on project wide {@code order} setting.
     */
    static Provider<Map<String, String>> mapProvider(String key,
                                                     MapProperty<String, String> property,
                                                     Order order,
                                                     Project project,
                                                     Object owner) {
        mapProvider(toEnv(key), toProperty(key), property, order, resolvePropertyPath(), project, owner, Collections.<String, String> emptyMap())
    }

    private static Priority resolvePropertyPriority() {
        String value = System.getProperty(KEY_PROPERTY_PRIORITY)
        try {
            return Priority.valueOf(value.toUpperCase())
        } catch (Exception ignored) {
            return Priority.PROPERTY
        }
    }

    private static Order resolvePropertyOrder() {
        String value = System.getProperty(KEY_PROPERTY_ORDER)
        try {
            return Order.valueOf(value.toUpperCase())
        } catch (Exception ignored) {
            return Order.ENV_SYS_PROP
        }
    }

    private static Path resolvePropertyPath() {
        String value = System.getProperty(KEY_PROPERTY_PATH)
        try {
            return Path.valueOf(value.toUpperCase())
        } catch (Exception ignored) {
            return Path.GLOBAL_PROJECT_OWNER
        }
    }

    private static String resolveValue(String envKey,
                                       String propertyKey,
                                       Order order,
                                       Path path,
                                       Project project,
                                       Object owner) {
        resolveValue(envKey,
            propertyKey,
            order,
            path,
            false,
            project,
            owner)
    }

    private static String resolveValue(String envKey,
                                       String propertyKey,
                                       Order order) {
        order = order ?: resolvePropertyOrder()

        switch (order) {
            case Order.ENV_SYS_PROP:
            case Order.ENV_PROP_SYS:
            case Order.PROP_ENV_SYS: ES:
            {
                String value = System.getenv(envKey)
                if (isBlank(value)) value = System.getProperty(propertyKey)
                return value
            }
            case Order.SYS_ENV_PROP:
            case Order.SYS_PROP_ENV:
            case Order.PROP_SYS_ENV: SE:
            {
                String value = System.getProperty(propertyKey)
                if (isBlank(value)) value = System.getenv(envKey)
                return value
            }
        }
    }

    private static String resolveValue(String envKey,
                                       String propertyKey,
                                       Order order,
                                       Path path,
                                       boolean projectAccess,
                                       Project project,
                                       Object owner) {
        order = order ?: resolvePropertyOrder()
        path = path ?: resolvePropertyPath()
        String _path = resolvePath(owner)
        String projectName = project.name.replace(' ', '_').replace('-', '_')

        switch (order) {
            case Order.ENV_SYS_PROP: ESP:
            {
                String value = null
                if (path.owner && isNotBlank(_path)) {
                    value = System.getenv(normalizePath(_path, '_').toUpperCase() + envKey)
                    if (isBlank(value)) value = System.getProperty(normalizePath(_path, '.') + propertyKey)
                    if (isBlank(value)) value = (project.findProperty(normalizePath(_path, '.') + propertyKey) as String)
                }
                if (path.project) {
                    if (isBlank(value)) value = System.getenv(projectName.toUpperCase() + '_' + envKey)
                    if (isBlank(value)) value = System.getProperty(projectName + '.' + propertyKey)
                    if (isBlank(value)) value = (project.findProperty(projectName + '.' + propertyKey) as String)
                }
                if (path.global) {
                    if (isBlank(value)) value = System.getenv(envKey)
                    if (isBlank(value)) value = System.getProperty(propertyKey)
                    if (isBlank(value)) value = (project.findProperty(propertyKey) as String)
                }
                if (isBlank(value) && projectAccess) {
                    value = (project.findProperty(propertyKey) as String)
                }
                return value
            }
            case Order.ENV_PROP_SYS: EPS:
            {
                String value = null
                if (path.owner && isNotBlank(_path)) {
                    value = System.getenv(normalizePath(_path, '_').toUpperCase() + envKey)
                    if (isBlank(value)) value = (project.findProperty(normalizePath(_path, '.') + propertyKey) as String)
                    if (isBlank(value)) value = System.getProperty(normalizePath(_path, '.') + propertyKey)
                }
                if (path.project) {
                    if (isBlank(value)) value = System.getenv(projectName.toUpperCase() + '_' + envKey)
                    if (isBlank(value)) value = (project.findProperty(projectName + '.' + propertyKey) as String)
                    if (isBlank(value)) value = System.getProperty(projectName + '.' + propertyKey)
                }
                if (path.global) {
                    if (isBlank(value)) value = System.getenv(envKey)
                    if (isBlank(value)) value = (project.findProperty(propertyKey) as String)
                    if (isBlank(value)) value = System.getProperty(propertyKey)
                }
                if (isBlank(value) && projectAccess) {
                    value = (project.findProperty(propertyKey) as String)
                }
                return value
            }
            case Order.SYS_ENV_PROP: SEP:
            {
                String value = null
                if (path.owner && isNotBlank(_path)) {
                    value = System.getProperty(normalizePath(_path, '.') + propertyKey)
                    if (isBlank(value)) value = System.getenv(normalizePath(_path, '_').toUpperCase() + envKey)
                    if (isBlank(value)) value = (project.findProperty(normalizePath(_path, '.') + propertyKey) as String)
                }
                if (path.project) {
                    if (isBlank(value)) value = System.getProperty(projectName + '.' + propertyKey)
                    if (isBlank(value)) value = System.getenv(projectName.toUpperCase() + '_' + envKey)
                    if (isBlank(value)) value = (project.findProperty(projectName + '.' + propertyKey) as String)
                }
                if (path.global) {
                    if (isBlank(value)) value = System.getProperty(propertyKey)
                    if (isBlank(value)) value = System.getenv(envKey)
                    if (isBlank(value)) value = (project.findProperty(propertyKey) as String)
                }
                if (isBlank(value) && projectAccess) {
                    value = (project.findProperty(propertyKey) as String)
                }
                return value
            }
            case Order.SYS_PROP_ENV: SPE:
            {
                String value = null
                if (path.owner && isNotBlank(_path)) {
                    value = System.getProperty(normalizePath(_path, '.') + propertyKey)
                    if (isBlank(value)) value = (project.findProperty(normalizePath(_path, '.') + propertyKey) as String)
                    if (isBlank(value)) value = System.getenv(normalizePath(_path, '_').toUpperCase() + envKey)
                }
                if (path.project) {
                    if (isBlank(value)) value = System.getProperty(projectName + '.' + propertyKey)
                    if (isBlank(value)) value = (project.findProperty(projectName + '.' + propertyKey) as String)
                    if (isBlank(value)) value = System.getenv(projectName.toUpperCase() + '_' + envKey)
                }
                if (path.global) {
                    if (isBlank(value)) value = System.getProperty(propertyKey)
                    if (isBlank(value)) value = (project.findProperty(propertyKey) as String)
                    if (isBlank(value)) value = System.getenv(envKey)
                }
                if (isBlank(value) && projectAccess) {
                    value = (project.findProperty(propertyKey) as String)
                }
                return value
            }
            case Order.PROP_ENV_SYS: PES:
            {
                String value = null
                if (path.owner && isNotBlank(_path)) {
                    value = (project.findProperty(normalizePath(_path, '.') + propertyKey) as String)
                    if (isBlank(value)) value = System.getenv(normalizePath(_path, '_').toUpperCase() + envKey)
                    if (isBlank(value)) value = System.getProperty(normalizePath(_path, '.') + propertyKey)
                }
                if (path.project) {
                    if (isBlank(value)) value = (project.findProperty(projectName + '.' + propertyKey) as String)
                    if (isBlank(value)) value = System.getenv(projectName.toUpperCase() + '_' + envKey)
                    if (isBlank(value)) value = System.getProperty(projectName + '.' + propertyKey)
                }
                if (path.global) {
                    if (isBlank(value)) value = (project.findProperty(propertyKey) as String)
                    if (isBlank(value)) value = System.getenv(envKey)
                    if (isBlank(value)) value = System.getProperty(propertyKey)
                }
                if (isBlank(value) && projectAccess) {
                    value = (project.findProperty(propertyKey) as String)
                }
                return value
            }
            case Order.PROP_SYS_ENV: PSE:
            {
                String value = null
                if (path.owner && isNotBlank(_path)) {
                    value = (project.findProperty(normalizePath(_path, '.') + propertyKey) as String)
                    if (isBlank(value)) value = System.getProperty(normalizePath(_path, '.') + propertyKey)
                    if (isBlank(value)) value = System.getenv(normalizePath(_path, '_').toUpperCase() + envKey)
                }
                if (path.project) {
                    if (isBlank(value)) value = (project.findProperty(projectName + '.' + propertyKey) as String)
                    if (isBlank(value)) value = System.getProperty(projectName + '.' + propertyKey)
                    if (isBlank(value)) value = System.getenv(projectName.toUpperCase() + '_' + envKey)
                }
                if (path.global) {
                    if (isBlank(value)) value = (project.findProperty(propertyKey) as String)
                    if (isBlank(value)) value = System.getProperty(propertyKey)
                    if (isBlank(value)) value = System.getenv(envKey)
                }
                if (isBlank(value) && projectAccess) {
                    value = (project.findProperty(propertyKey) as String)
                }
                return value
            }
        }
    }

    @CompileDynamic
    private static String resolvePath(Object object) {
        if (object instanceof PathAware) {
            return ((PathAware) object).path
        } else if (object instanceof Task) {
            return ((Task) object).path
        } else if (object?.metaClass?.respondsTo('getPath')) {
            return object.getPath()
        }
        return object ? object.class.name.replaceAll('.', ':') : ''
    }

    private static String normalizePath(String path, String delimiter) {
        if (':' == path || '' == path) {
            return ''
        }
        return path[1..-1].replace(':', delimiter).replace(' ', '_') + delimiter
    }

    private static String toEnv(String key) {
        key.toUpperCase().replace('.', '_')
    }

    private static String toProperty(String key) {
        key.uncapitalize().replace('_', '.')
    }

    // == Kordamp specific providers ==

    @CompileDynamic
    static <E extends Enum<E>> Provider<E> enumProvider(String key,
                                                        Class<E> enumType,
                                                        Property<E> property,
                                                        Provider<E> provider,
                                                        Order order,
                                                        Path path,
                                                        boolean projectAccess,
                                                        Project project,
                                                        E defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return enumType.valueOf(value.toUpperCase())
                    }
                    return provider.getOrElse(defaultValue)
                case Priority.PROVIDER:
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return enumType.valueOf(value.toUpperCase())
                    }
                    return property.getOrElse(provider.getOrElse(defaultValue))
            }
        }
    }

    @CompileDynamic
    static <E extends Enum<E>> Provider<E> enumProvider(String key,
                                                        Class<E> enumType,
                                                        Property<E> property,
                                                        Provider<E> provider,
                                                        Order order,
                                                        Path path,
                                                        boolean projectAccess,
                                                        Project project,
                                                        Provider<E> defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return enumType.valueOf(value.toUpperCase())
                    }
                    return provider.orElse(defaultValue).get()
                case Priority.PROVIDER:
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return enumType.valueOf(value.toUpperCase())
                    }
                    return property.orElse(provider.orElse(defaultValue)).get()
            }
        }
    }

    static Provider<Boolean> booleanProvider(String key,
                                             Property<Boolean> property,
                                             Provider<Boolean> provider,
                                             Order order,
                                             Path path,
                                             boolean projectAccess,
                                             Project project,
                                             boolean defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return Boolean.parseBoolean(value)
                    }
                    return provider.getOrElse(defaultValue)
                case Priority.PROVIDER:
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return Boolean.parseBoolean(value)
                    }
                    return property.getOrElse(provider.getOrElse(defaultValue))
            }
        }
    }

    static Provider<Boolean> booleanProvider(String key,
                                             Property<Boolean> property,
                                             Provider<Boolean> provider,
                                             Order order,
                                             Path path,
                                             boolean projectAccess,
                                             Project project,
                                             Provider<Boolean> defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return Boolean.parseBoolean(value)
                    }
                    return provider.orElse(defaultValue).get()
                case Priority.PROVIDER:
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return Boolean.parseBoolean(value)
                    }
                    return property.orElse(provider.orElse(defaultValue)).get()
            }
        }
    }

    static Provider<Integer> integerProvider(String key,
                                             Property<Integer> property,
                                             Provider<Integer> provider,
                                             Order order,
                                             Path path,
                                             boolean projectAccess,
                                             Project project,
                                             int defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return Integer.parseInt(value)
                    }
                    return provider.getOrElse(defaultValue)
                case Priority.PROVIDER:
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return Integer.parseInt(value)
                    }
                    return property.getOrElse(provider.getOrElse(defaultValue))
            }
        }
    }

    static Provider<Integer> integerProvider(String key,
                                             Property<Integer> property,
                                             Provider<Integer> provider,
                                             Order order,
                                             Path path,
                                             boolean projectAccess,
                                             Project project,
                                             Provider<Integer> defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return Integer.parseInt(value)
                    }
                    return provider.orElse(defaultValue).get()
                case Priority.PROVIDER:
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return Integer.parseInt(value)
                    }
                    return property.orElse(provider.orElse(defaultValue)).get()
            }
        }
    }

    static Provider<Long> longProvider(String key,
                                       Property<Long> property,
                                       Provider<Long> provider,
                                       Order order,
                                       Path path,
                                       boolean projectAccess,
                                       Project project,
                                       long defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return Long.parseLong(value)
                    }
                    return provider.getOrElse(defaultValue)
                case Priority.PROVIDER:
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return Long.parseLong(value)
                    }
                    return property.getOrElse(provider.getOrElse(defaultValue))
            }
        }
    }

    static Provider<Long> longProvider(String key,
                                       Property<Long> property,
                                       Provider<Long> provider,
                                       Order order,
                                       Path path,
                                       boolean projectAccess,
                                       Project project,
                                       Provider<Long> defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return Long.parseLong(value)
                    }
                    return provider.orElse(defaultValue).get()
                case Priority.PROVIDER:
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return Long.parseLong(value)
                    }
                    return property.orElse(provider.orElse(defaultValue)).get()
            }
        }
    }

    static Provider<String> stringProvider(String key,
                                           Property<String> property,
                                           Provider<String> provider,
                                           Order order,
                                           Path path,
                                           boolean projectAccess,
                                           Project project,
                                           String defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return value
                    }
                    return provider.getOrElse(defaultValue)
                case Priority.PROVIDER:
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return value
                    }
                    return property.getOrElse(provider.getOrElse(defaultValue))
            }
        }
    }

    static Provider<String> stringProvider(String key,
                                           Property<String> property,
                                           Provider<String> provider,
                                           Order order,
                                           Path path,
                                           boolean projectAccess,
                                           Project project,
                                           Provider<String> defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return value
                    }
                    return provider.orElse(defaultValue).get()
                case Priority.PROVIDER:
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return value
                    }
                    return property.orElse(provider.orElse(defaultValue)).get()
            }
        }
    }

    static Provider<Directory> directoryProvider(String key,
                                                 DirectoryProperty property,
                                                 Provider<Directory> provider,
                                                 Order order,
                                                 Path path,
                                                 boolean projectAccess,
                                                 Project project,
                                                 Directory defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        DirectoryProperty p = project.objects.directoryProperty()
                        p.set(Paths.get(value).toFile())
                        return p.get()
                    }
                    return provider.getOrElse(defaultValue)
                case Priority.PROVIDER:
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        DirectoryProperty p = project.objects.directoryProperty()
                        p.set(Paths.get(value).toFile())
                        return p.get()
                    }
                    return property.getOrElse(provider.getOrElse(defaultValue))
            }
        }
    }

    static Provider<Directory> directoryProvider(String key,
                                                 DirectoryProperty property,
                                                 Provider<Directory> provider,
                                                 Order order,
                                                 Path path,
                                                 boolean projectAccess,
                                                 Project project,
                                                 Provider<Directory> defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        DirectoryProperty p = project.objects.directoryProperty()
                        p.set(Paths.get(value).toFile())
                        return p.get()
                    }
                    return provider.orElse(defaultValue).get()
                case Priority.PROVIDER:
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        DirectoryProperty p = project.objects.directoryProperty()
                        p.set(Paths.get(value).toFile())
                        return p.get()
                    }
                    return property.orElse(provider.orElse(defaultValue)).get()
            }
        }
    }

    static Provider<RegularFile> fileProvider(String key,
                                              RegularFileProperty property,
                                              Provider<RegularFile> provider,
                                              Order order,
                                              Path path,
                                              boolean projectAccess,
                                              Project project,
                                              RegularFile defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        RegularFileProperty p = project.objects.fileProperty()
                        p.set(Paths.get(value).toFile())
                        return p.get()
                    }
                    return provider.getOrElse(defaultValue)
                case Priority.PROVIDER:
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        RegularFileProperty p = project.objects.fileProperty()
                        p.set(Paths.get(value).toFile())
                        return p.get()
                    }
                    return property.getOrElse(provider.getOrElse(defaultValue))
            }
        }
    }

    static Provider<RegularFile> fileProvider(String key,
                                              RegularFileProperty property,
                                              Provider<RegularFile> provider,
                                              Order order,
                                              Path path,
                                              boolean projectAccess,
                                              Project project,
                                              Provider<RegularFile> defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        RegularFileProperty p = project.objects.fileProperty()
                        p.set(Paths.get(value).toFile())
                        return p.get()
                    }
                    return provider.orElse(defaultValue).get()
                case Priority.PROVIDER:
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        RegularFileProperty p = project.objects.fileProperty()
                        p.set(Paths.get(value).toFile())
                        return p.get()
                    }
                    return property.orElse(provider.orElse(defaultValue)).get()
            }
        }
    }

    @CompileDynamic
    static Provider<Set<String>> setProvider(String key,
                                             SetProperty<String> property,
                                             Provider<Set<String>> provider,
                                             Order order,
                                             Path path,
                                             boolean projectAccess,
                                             Project project,
                                             Set<String> defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return convertToSet(value)
                    }
                    return provider.getOrElse(defaultValue)
                case Priority.PROVIDER:
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return convertToSet(value)
                    }
                    return property.getOrElse(provider.getOrElse(defaultValue))
            }
        }
    }

    @CompileDynamic
    static Provider<Set<String>> setProvider(String key,
                                             SetProperty<String> property,
                                             Provider<Set<String>> provider,
                                             Order order,
                                             Path path,
                                             boolean projectAccess,
                                             Project project,
                                             Provider<Set<String>> defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return convertToSet(value)
                    }
                    return provider.orElse(defaultValue).get()
                case Priority.PROVIDER:
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return convertToSet(value)
                    }
                    return property.orElse(provider.orElse(defaultValue)).get()
            }
        }
    }

    @CompileDynamic
    static Provider<List<String>> listProvider(String key,
                                               ListProperty<String> property,
                                               Provider<List<String>> provider,
                                               Order order,
                                               Path path,
                                               boolean projectAccess,
                                               Project project,
                                               List<String> defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return convertToList(value)
                    }
                    return provider.getOrElse(defaultValue)
                case Priority.PROVIDER:
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return convertToList(value)
                    }
                    return property.getOrElse(provider.getOrElse(defaultValue))
            }
        }
    }

    @CompileDynamic
    static Provider<List<String>> listProvider(String key,
                                               ListProperty<String> property,
                                               Provider<List<String>> provider,
                                               Order order,
                                               Path path,
                                               boolean projectAccess,
                                               Project project,
                                               Provider<List<String>> defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return convertToList(value)
                    }
                    return provider.orElse(defaultValue).get()
                case Priority.PROVIDER:
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return convertToList(value)
                    }
                    return property.orElse(provider.orElse(defaultValue)).get()
            }
        }
    }

    @CompileDynamic
    static Provider<Map<String, String>> mapProvider(String key,
                                                     MapProperty<String, String> property,
                                                     Provider<Map<String, String>> provider,
                                                     Order order,
                                                     Path path,
                                                     boolean projectAccess,
                                                     Project project,
                                                     Map<String, String> defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return convertToMap(value)
                    }
                    return provider.getOrElse(defaultValue)
                case Priority.PROVIDER:
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return convertToMap(value)
                    }
                    return property.getOrElse(provider.getOrElse(defaultValue))
            }
        }
    }

    @CompileDynamic
    static Provider<Map<String, String>> mapProvider(String key,
                                                     MapProperty<String, String> property,
                                                     Provider<Map<String, String>> provider,
                                                     Order order,
                                                     Path path,
                                                     boolean projectAccess,
                                                     Project project,
                                                     Provider<Map<String, String>> defaultValue) {
        project.providers.provider {
            switch (resolvePropertyPriority()) {
                case Priority.PROPERTY:
                    if (property.present) return property.get()
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return convertToMap(value)
                    }
                    return provider.orElse(defaultValue).get()
                case Priority.PROVIDER:
                    String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
                    if (isNotBlank(value)) {
                        return convertToMap(value)
                    }
                    return property.orElse(provider.orElse(defaultValue)).get()
            }
        }
    }

    private static LinkedHashSet<String> convertToSet(String value) {
        Set<String> set = new LinkedHashSet<>()
        for (String v : value.split(',')) {
            if (isNotBlank(v)) set.add(v.trim())
        }
        return set
    }

    private static ArrayList<String> convertToList(String value) {
        List<String> list = new ArrayList<>()
        for (String v : value.split(',')) {
            if (isNotBlank(v)) list.add(v.trim())
        }
        return list
    }

    private static Map<String, String> convertToMap(String value) {
        Map<String, String> map = new LinkedHashMap<>()
        for (String val : value.split(',')) {
            String[] kv = val.split('=')
            if (kv.length == 2) {
                map.put(kv[0], kv[1])
            }
        }
        return map
    }
}
