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
package org.kordamp.gradle.plugin.base.util;

import org.gradle.api.plugins.ExtensionAware;

public class ExtensionTypeAndName<E extends ExtensionAware> {

    private final String name;
    private final Class<E> extensionType;

    ExtensionTypeAndName(String name, Class<E> extensionType) {
        this.name = name;
        this.extensionType = extensionType;
    }

    public String getName() {
        return name;
    }

    public Class<E> getExtensionType() {
        return extensionType;
    }

}
