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

    static final String KEY_PROPERTY_ORDER = 'property.order'
    static final String KEY_PROPERTY_PATH = 'property.path'

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
    static Provider<Boolean> booleanProvider(ProviderFactory providers,
                                             Property<Boolean> property,
                                             Provider<Boolean> provider,
                                             boolean defaultValue) {
        providers.provider {
            property.getOrElse(provider ? provider.getOrElse(defaultValue) : defaultValue)
        }
    }

    /**
     * Creates a hierarchical String property.
     * Returns the {@code property}'s value if present, otherwise check's if the supplied
     * {@code provider} is not null and returns its value, otherwise returns the {@code defaultValue}.
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
            property.getOrElse(provider ? provider.getOrElse(defaultValue) : defaultValue)
        }
    }

    /**
     * Creates a hierarchical Object property.
     * Returns the {@code property}'s value if present, otherwise check's if the supplied
     * {@code provider} is not null and returns its value, otherwise returns the {@code defaultValue}.
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
            property.getOrElse(provider ? provider.getOrElse(defaultValue) : defaultValue)
        }
    }

    /**
     * Creates a hierarchical int property.
     * Returns the {@code property}'s value if present, otherwise check's if the supplied
     * {@code provider} is not null and returns its value, otherwise returns the {@code defaultValue}.
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
            property.getOrElse(provider ? provider.getOrElse(defaultValue) : defaultValue)
        }
    }

    /**
     * Creates a hierarchical long property.
     * Returns the {@code property}'s value if present, otherwise check's if the supplied
     * {@code provider} is not null and returns its value, otherwise returns the {@code defaultValue}.
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
            property.getOrElse(provider ? provider.getOrElse(defaultValue) : defaultValue)
        }
    }

    /**
     * Creates a hierarchical File property.
     * Returns the {@code property}'s value if present, otherwise check's if the supplied
     * {@code provider} is not null and returns its value, otherwise returns the {@code defaultValue}.
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
            property.getOrElse(provider ? provider.getOrElse(defaultValue) : defaultValue)
        }
    }

    /**
     * Creates a hierarchical File (directory) property.
     * Returns the {@code property}'s value if present, otherwise check's if the supplied
     * {@code provider} is not null and returns its value, otherwise returns the {@code defaultValue}.
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
            property.getOrElse(provider ? provider.getOrElse(defaultValue) : defaultValue)
        }
    }

    /**
     * Creates a hierarchical Map property.
     * Returns merged contents of {@code defaultValue}, provider's value, and property's value, in that order.
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

            if (provider) {
                map.putAll(provider.get())
            }
            if (property.present) {
                map.putAll(property.get())
            }

            map
        }
    }

    /**
     * Creates a hierarchical List property.
     * Returns merged contents of {@code defaultValue}, provider's value, and property's value, in that order.
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

            list
        }
    }

    /**
     * Creates a hierarchical Set property.
     * Returns merged contents of {@code defaultValue}, provider's value, and property's value, in that order.
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

            if (provider) {
                set.addAll(provider.get())
            }
            if (property.present) {
                set.addAll(property.get())
            }

            set
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
    static Provider<String> stringProvider(String envKey,
                                           String propertyKey,
                                           Property<String> property,
                                           Order order,
                                           Path path,
                                           Project project,
                                           Object owner,
                                           String defaultValue) {
        project.providers.provider {
            if (property.present) return property.get()
            String value = resolveValue(envKey, propertyKey, order, path, project, owner)
            isNotBlank(value) ? value : defaultValue
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
    static Provider<String> stringProvider(String key,
                                           Property<String> property,
                                           Order order,
                                           Path path,
                                           Project project,
                                           Object owner,
                                           String defaultValue) {
        stringProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
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
    static Provider<String> stringProvider(String envKey,
                                           String propertyKey,
                                           Property<String> property,
                                           Project project,
                                           Object owner) {
        stringProvider(envKey, propertyKey, property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, null)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.
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
        stringProvider(toEnv(key), toProperty(key), property, order, resolvePropertyPath(), project, owner, null)
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
    static Provider<String> stringProvider(String key,
                                           Property<String> property,
                                           Project project,
                                           Object owner) {
        stringProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, null)
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
    static Provider<String> stringProvider(String key,
                                           Property<String> property,
                                           Project project,
                                           Object owner,
                                           String defaultValue) {
        stringProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
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
    static Provider<Boolean> booleanProvider(String envKey,
                                             String propertyKey,
                                             Property<Boolean> property,
                                             Order order,
                                             Path path,
                                             Project project,
                                             Object owner,
                                             boolean defaultValue) {
        project.providers.provider {
            if (property.present) return property.get()
            String value = resolveValue(envKey, propertyKey, order, path, project, owner)
            if (isNotBlank(value)) {
                return Boolean.parseBoolean(value)
            }
            defaultValue
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
    static Provider<Boolean> booleanProvider(String key,
                                             Property<Boolean> property,
                                             Order order,
                                             Path path,
                                             Project project,
                                             Object owner,
                                             boolean defaultValue) {
        booleanProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
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
    static Provider<Boolean> booleanProvider(String envKey,
                                             String propertyKey,
                                             Property<Boolean> property,
                                             Project project,
                                             Object owner) {
        booleanProvider(envKey, propertyKey, property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, false)
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
    static Provider<Boolean> booleanProvider(String key,
                                             Property<Boolean> property,
                                             Project project,
                                             Object owner) {
        booleanProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, false)
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
    static Provider<Boolean> booleanProvider(String key,
                                             Property<Boolean> property,
                                             Project project,
                                             Object owner,
                                             boolean defaultValue) {
        booleanProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
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
    static Provider<Boolean> booleanProvider(String key,
                                             Property<Boolean> property,
                                             Order order,
                                             Project project,
                                             Object owner) {
        booleanProvider(toEnv(key), toProperty(key), property, order, resolvePropertyPath(), project, owner, false)
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not set.
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
            if (property.present) return property.get()
            String value = resolveValue(envKey, propertyKey, order, path, project, owner)
            if (isNotBlank(value)) {
                return Integer.parseInt(value)
            }
            defaultValue
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
    static Provider<Integer> integerProvider(String key,
                                             Property<Integer> property,
                                             Order order,
                                             Path path,
                                             Project project,
                                             Object owner,
                                             int defaultValue) {
        integerProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
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
    static Provider<Integer> integerProvider(String envKey,
                                             String propertyKey,
                                             Property<Integer> property,
                                             Project project,
                                             Object owner) {
        integerProvider(envKey, propertyKey, property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, 0)
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
    static Provider<Integer> integerProvider(String key,
                                             Property<Integer> property,
                                             Project project,
                                             Object owner) {
        integerProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, 0)
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
    static Provider<Integer> integerProvider(String key,
                                             Property<Integer> property,
                                             Project project,
                                             Object owner,
                                             int defaultValue) {
        integerProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
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
    static Provider<Integer> integerProvider(String key,
                                             Property<Integer> property,
                                             Order order,
                                             Project project,
                                             Object owner) {
        integerProvider(toEnv(key), toProperty(key), property, order, resolvePropertyPath(), project, owner, 0)
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
    static Provider<Long> longProvider(String envKey,
                                       String propertyKey,
                                       Property<Long> property,
                                       Order order,
                                       Path path,
                                       Project project,
                                       Object owner,
                                       long defaultValue) {
        project.providers.provider {
            if (property.present) return property.get()
            String value = resolveValue(envKey, propertyKey, order, path, project, owner)
            if (isNotBlank(value)) {
                return Long.parseLong(value)
            }
            defaultValue
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
    static Provider<Long> longProvider(String key,
                                       Property<Long> property,
                                       Order order,
                                       Path path,
                                       Project project,
                                       Object owner,
                                       long defaultValue) {
        longProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
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
    static Provider<Long> longProvider(String envKey,
                                       String propertyKey,
                                       Property<Long> property,
                                       Project project,
                                       Object owner) {
        longProvider(envKey, propertyKey, property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, 0L)
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
    static Provider<Long> longProvider(String key,
                                       Property<Long> property,
                                       Project project,
                                       Object owner) {
        longProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, 0L)
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
    static Provider<Long> longProvider(String key,
                                       Property<Long> property,
                                       Project project,
                                       Object owner,
                                       long defaultValue) {
        longProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
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
    static Provider<Long> longProvider(String key,
                                       Property<Long> property,
                                       Order order,
                                       Project project,
                                       Object owner) {
        longProvider(toEnv(key), toProperty(key), property, order, resolvePropertyPath(), project, owner, 0L)
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
    static Provider<RegularFile> fileProvider(String envKey,
                                              String propertyKey,
                                              RegularFileProperty property,
                                              Order order,
                                              Path path,
                                              Project project,
                                              Object owner,
                                              RegularFile defaultValue) {
        project.providers.provider {
            if (property.present) return property.get()
            String value = resolveValue(envKey, propertyKey, order, path, project, owner)
            if (isNotBlank(value)) {
                RegularFileProperty p = project.objects.fileProperty()
                p.set(Paths.get(value).toFile())
                return p.get()
            }
            defaultValue
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
    static Provider<RegularFile> fileProvider(String key,
                                              RegularFileProperty property,
                                              Order order,
                                              Path path,
                                              Project project,
                                              Object owner,
                                              RegularFile defaultValue) {
        fileProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
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
    static Provider<RegularFile> fileProvider(String envKey,
                                              String propertyKey,
                                              RegularFileProperty property,
                                              Project project,
                                              Object owner) {
        fileProvider(envKey, propertyKey, property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, null)
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
    static Provider<RegularFile> fileProvider(String key,
                                              RegularFileProperty property,
                                              Project project,
                                              Object owner) {
        fileProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, null)
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
    static Provider<RegularFile> fileProvider(String key,
                                              RegularFileProperty property,
                                              Project project,
                                              Object owner,
                                              RegularFile defaultValue) {
        fileProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
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
    static Provider<RegularFile> fileProvider(String key,
                                              RegularFileProperty property,
                                              Order order,
                                              Project project,
                                              Object owner) {
        fileProvider(toEnv(key), toProperty(key), property, order, resolvePropertyPath(), project, owner, null)
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
    static Provider<Directory> directoryProvider(String envKey,
                                                 String propertyKey,
                                                 DirectoryProperty property,
                                                 Order order,
                                                 Path path,
                                                 Project project,
                                                 Object owner,
                                                 Directory defaultValue) {
        project.providers.provider {
            if (property.present) return property.get()
            String value = resolveValue(envKey, propertyKey, order, path, project, owner)
            if (isNotBlank(value)) {
                DirectoryProperty p = project.objects.directoryProperty()
                p.set(Paths.get(value).toFile())
                return p.get()
            }
            defaultValue
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
    static Provider<Directory> directoryProvider(String key,
                                                 DirectoryProperty property,
                                                 Order order,
                                                 Path path,
                                                 Project project,
                                                 Object owner,
                                                 Directory defaultValue) {
        directoryProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
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
    static Provider<Directory> directoryProvider(String envKey,
                                                 String propertyKey,
                                                 DirectoryProperty property,
                                                 Project project,
                                                 Object owner) {
        directoryProvider(envKey, propertyKey, property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, null)
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
    static Provider<Directory> directoryProvider(String key,
                                                 DirectoryProperty property,
                                                 Project project,
                                                 Object owner) {
        directoryProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, null)
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
    static Provider<Directory> directoryProvider(String key,
                                                 DirectoryProperty property,
                                                 Project project,
                                                 Object owner,
                                                 Directory defaultValue) {
        directoryProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
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
    static Provider<Directory> directoryProvider(String key,
                                                 DirectoryProperty property,
                                                 Order order,
                                                 Project project,
                                                 Object owner) {
        directoryProvider(toEnv(key), toProperty(key), property, order, resolvePropertyPath(), project, owner, null)
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
    static Provider<Set<String>> setProvider(String envKey,
                                             String propertyKey,
                                             SetProperty<String> property,
                                             Order order,
                                             Path path,
                                             Project project,
                                             Object owner,
                                             Set<String> defaultValue) {
        project.providers.provider {
            if (property.present) return property.get()
            String value = resolveValue(envKey, propertyKey, order, path, project, owner)
            if (isNotBlank(value)) {
                Set<String> set = new LinkedHashSet<>()
                for (String v : value.split(',')) {
                    if (isNotBlank(v)) set.add(v.trim())
                }
                return set
            }
            defaultValue
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
    static Provider<Set<String>> setProvider(String key,
                                             SetProperty<String> property,
                                             Order order,
                                             Path path,
                                             Project project,
                                             Object owner,
                                             Set<String> defaultValue) {
        setProvider(toEnv(key), toProperty(key), property, order, path, project, owner, defaultValue)
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
    static Provider<Set<String>> setProvider(String envKey,
                                             String propertyKey,
                                             SetProperty<String> property,
                                             Project project,
                                             Object owner) {
        setProvider(envKey, propertyKey, property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, Collections.<String> emptySet())
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
    static Provider<Set<String>> setProvider(String key,
                                             SetProperty<String> property,
                                             Project project,
                                             Object owner) {
        setProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, Collections.<String> emptySet())
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
    static Provider<Set<String>> setProvider(String key,
                                             SetProperty<String> property,
                                             Project project,
                                             Object owner,
                                             Set<String> defaultValue) {
        setProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
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
    static Provider<Set<String>> setProvider(String key,
                                             SetProperty<String> property,
                                             Order order,
                                             Project project,
                                             Object owner) {
        setProvider(toEnv(key), toProperty(key), property, order, resolvePropertyPath(), project, owner, Collections.<String> emptySet())
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

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not list.
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
    static Provider<List<String>> listProvider(String envKey,
                                               String propertyKey,
                                               ListProperty<String> property,
                                               Project project,
                                               Object owner) {
        listProvider(envKey, propertyKey, property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, Collections.<String> emptyList())
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
    static Provider<List<String>> listProvider(String key,
                                               ListProperty<String> property,
                                               Project project,
                                               Object owner) {
        listProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, Collections.<String> emptyList())
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
    static Provider<List<String>> listProvider(String key,
                                               ListProperty<String> property,
                                               Project project,
                                               Object owner,
                                               List<String> defaultValue) {
        listProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
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
    static Provider<List<String>> listProvider(String key,
                                               ListProperty<String> property,
                                               Order order,
                                               Project project,
                                               Object owner) {
        listProvider(toEnv(key), toProperty(key), property, order, resolvePropertyPath(), project, owner, Collections.<String> emptyList())
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
    static Provider<Map<String, String>> mapProvider(String envKey,
                                                     String propertyKey,
                                                     MapProperty<String, String> property,
                                                     Order order,
                                                     Path path,
                                                     Project project,
                                                     Object owner,
                                                     Map<String, String> defaultValue) {
        project.providers.provider {
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
            defaultValue
        }
    }

    /**
     * Creates a {@code Provider} that resolves values from additional sources if the property's value is not map.
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
    static Provider<Map<String, String>> mapProvider(String envKey,
                                                     String propertyKey,
                                                     MapProperty<String, String> property,
                                                     Project project,
                                                     Object owner) {
        mapProvider(envKey, propertyKey, property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, Collections.<String, String> emptyMap())
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
    static Provider<Map<String, String>> mapProvider(String key,
                                                     MapProperty<String, String> property,
                                                     Project project,
                                                     Object owner) {
        mapProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, Collections.<String, String> emptyMap())
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
    static Provider<Map<String, String>> mapProvider(String key,
                                                     MapProperty<String, String> property,
                                                     Project project,
                                                     Object owner,
                                                     Map<String, String> defaultValue) {
        mapProvider(toEnv(key), toProperty(key), property, resolvePropertyOrder(), resolvePropertyPath(), project, owner, defaultValue)
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
    static Provider<Map<String, String>> mapProvider(String key,
                                                     MapProperty<String, String> property,
                                                     Order order,
                                                     Project project,
                                                     Object owner) {
        mapProvider(toEnv(key), toProperty(key), property, order, resolvePropertyPath(), project, owner, Collections.<String, String> emptyMap())
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
        key.toLowerCase().replace('_', '.')
    }

    // == Kordamp specific providers ==

    static Provider<Boolean> booleanProvider(String key,
                                             Property<Boolean> property,
                                             Provider<Boolean> provider,
                                             Order order,
                                             Path path,
                                             boolean projectAccess,
                                             Project project,
                                             boolean defaultValue) {
        project.providers.provider {
            if (property.present) return property.get()
            String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
            if (isNotBlank(value)) {
                return Boolean.parseBoolean(value)
            }
            provider.getOrElse(defaultValue)
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
            if (property.present) return property.get()
            String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
            if (isNotBlank(value)) {
                return Integer.parseInt(value)
            }
            provider.getOrElse(defaultValue)
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
            if (property.present) return property.get()
            String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
            if (isNotBlank(value)) {
                return Long.parseLong(value)
            }
            provider.getOrElse(defaultValue)
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
            if (property.present) return property.get()
            String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
            if (isNotBlank(value)) {
                return value
            }
            provider.getOrElse(defaultValue)
        }
    }

    static Provider<Directory> integerProvider(String key,
                                               DirectoryProperty property,
                                               Provider<Directory> provider,
                                               Order order,
                                               Path path,
                                               boolean projectAccess,
                                               Project project,
                                               Directory defaultValue) {
        project.providers.provider {
            if (property.present) return property.get()
            String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
            if (isNotBlank(value)) {
                DirectoryProperty p = project.objects.directoryProperty()
                p.set(Paths.get(value).toFile())
                return p.get()
            }
            provider.getOrElse(defaultValue)
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
            if (property.present) return property.get()
            String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
            if (isNotBlank(value)) {
                DirectoryProperty p = project.objects.directoryProperty()
                p.set(Paths.get(value).toFile())
                return p.get()
            }
            provider.getOrElse(defaultValue)
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
            if (property.present) return property.get()
            String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
            if (isNotBlank(value)) {
                RegularFileProperty p = project.objects.fileProperty()
                p.set(Paths.get(value).toFile())
                return p.get()
            }
            provider.getOrElse(defaultValue)
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
            if (property.present) return property.get()
            String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
            if (isNotBlank(value)) {
                Set<String> set = new LinkedHashSet<>()
                for (String v : value.split(',')) {
                    if (isNotBlank(v)) set.add(v.trim())
                }
                return set
            }
            provider.getOrElse(defaultValue)
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
            if (property.present) return property.get()
            String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
            if (isNotBlank(value)) {
                List<String> list = new ArrayList<>()
                for (String v : value.split(',')) {
                    if (isNotBlank(v)) list.add(v.trim())
                }
                return list
            }
            provider.getOrElse(defaultValue)
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
            if (property.present) return property.get()
            String value = resolveValue(toEnv(key), toProperty(key), order, path, projectAccess, project, null)
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
            provider.getOrElse(defaultValue)
        }
    }
}
