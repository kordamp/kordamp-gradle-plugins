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
package org.kordamp.gradle

import groovy.transform.CompileStatic
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.SetProperty

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@CompileStatic
class PropertyUtils {
    static Provider<Boolean> booleanProvider(ProviderFactory providers,
                                             Provider<Boolean> parentProvider,
                                             Property<Boolean> property,
                                             boolean defaultValue) {
        providers.provider {
            property.getOrElse(parentProvider ? parentProvider.get() : defaultValue)
        }
    }

    static Provider<String> stringProvider(ProviderFactory providers,
                                           Provider<String> parentProvider,
                                           Property<String> property,
                                           String defaultValue) {
        providers.provider {
            property.getOrElse(parentProvider ? parentProvider.get() : defaultValue)
        }
    }

    static <T> Provider<T> objectProvider(ProviderFactory providers,
                                          Provider<T> parentProvider,
                                          Property<T> property,
                                          T defaultValue) {
        providers.provider {
            property.getOrElse(parentProvider ? parentProvider.get() : defaultValue)
        }
    }

    static Provider<Integer> intProvider(ProviderFactory providers,
                                         Provider<Integer> parentProvider,
                                         Property<Integer> property,
                                         int defaultValue) {
        providers.provider {
            property.getOrElse(parentProvider ? parentProvider.get() : defaultValue)
        }
    }

    static Provider<Long> longProvider(ProviderFactory providers,
                                       Provider<Long> parentProvider,
                                       Property<Long> property,
                                       long defaultValue) {
        providers.provider {
            property.getOrElse(parentProvider ? parentProvider.get() : defaultValue)
        }
    }

    static Provider<File> fileProvider(ProviderFactory providers,
                                       Provider<File> parentProvider,
                                       RegularFileProperty property,
                                       File defaultValue) {
        providers.provider {
            property.getAsFile().getOrElse(parentProvider ? parentProvider.get() : defaultValue)
        }
    }

    static Provider<File> directoryProvider(ProviderFactory providers,
                                            Provider<File> parentProvider,
                                            DirectoryProperty property,
                                            File defaultValue) {
        providers.provider {
            property.getAsFile().getOrElse(parentProvider ? parentProvider.get() : defaultValue)
        }
    }

    static <K, V> Provider<Map<K, V>> mapProvider(ProviderFactory providers,
                                                  Provider<Map<K, V>> parentProvider,
                                                  MapProperty<K, V> property,
                                                  Map<K, V> defaultValue) {
        providers.provider {
            // TODO: review as this does not return a live view
            Map<K, V> map = new LinkedHashMap<>(defaultValue)

            if (parentProvider) {
                map.putAll(parentProvider.get())
            }
            if (property.present) {
                map.putAll(property.get())
            }

            map
        }
    }

    static <E> Provider<List<E>> listProvider(ProviderFactory providers,
                                              Provider<List<E>> parentProvider,
                                              ListProperty<E> property,
                                              List<E> defaultValue) {
        providers.provider {
            // TODO: review as this does not return a live view
            List<E> list = new ArrayList<>(defaultValue)

            if (parentProvider) {
                list.addAll(parentProvider.get())
            }
            if (property.present) {
                for (E e : property.get()) {
                    if (!list.contains(e)) {
                        list.add(e);
                    }
                }
            }

            list
        }
    }

    static <E> Provider<Set<E>> setProvider(ProviderFactory providers,
                                            Provider<Set<E>> parentProvider,
                                            SetProperty<E> property,
                                            Set<E> defaultValue) {
        providers.provider {
            // TODO: review as this does not return a live view
            Set<E> set = new LinkedHashSet<>(defaultValue)

            if (parentProvider) {
                set.addAll(parentProvider.get())
            }
            if (property.present) {
                set.addAll(property.get())
            }

            set
        }
    }
}
