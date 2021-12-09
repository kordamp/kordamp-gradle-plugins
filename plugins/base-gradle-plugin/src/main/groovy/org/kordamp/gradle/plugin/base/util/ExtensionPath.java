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

import java.util.Optional;

public class ExtensionPath<P extends ExtensionAware, E extends ExtensionAware> {

    public static <E extends ExtensionAware> ExtensionPath<ExtensionAware, E> create(String name, Class<E> type) {
        return new ExtensionPath<>(new ExtensionTypeAndName<>(name, type), null);
    }

    private final ExtensionPath<?, P> parent;
    private final ExtensionTypeAndName<E> current;

    public ExtensionPath(ExtensionTypeAndName<E> current, ExtensionPath<?, P>  parent) {
        this.current = current;
        this.parent = parent;
    }

    public <NE extends ExtensionAware> ExtensionPath<E, NE> append(String name, Class<NE> type) {
        return new ExtensionPath<>(new ExtensionTypeAndName<>(name, type), this);
    }

    public ExtensionPath<ExtensionAware, E> asOrphan() {
        return new ExtensionPath<>(current, null);
    }

    public Optional<ExtensionPath<?, P> > getParent() {
        return Optional.ofNullable(parent);
    }

    public ExtensionTypeAndName<E> getCurrent() {
        return current;
    }

    public E get(ExtensionAware container) {
        if (parent == null) {
            return current.getExtensionType().cast(container.getExtensions().getByName(current.getName()));
        }
        return current.getExtensionType().cast(parent.get(container).getExtensions().getByName(current.getName()));
    }

    @Override
    public String toString() {
        if (parent == null) {
            return "/" + current;
        }
        return getParent().toString() + "/" + current;
    }
}
