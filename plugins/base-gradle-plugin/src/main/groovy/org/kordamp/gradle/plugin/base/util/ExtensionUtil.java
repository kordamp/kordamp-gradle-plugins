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

import org.gradle.api.Project;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.plugins.ExtensionAware;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

public class ExtensionUtil {

    public static <E extends ExtensionAware, P extends ExtensionAware> E create(Project project, ExtensionPath<P, E> path) {
        return create(project, path, null);
    }

    public static <E extends ExtensionAware, P extends ExtensionAware> E create(Project project, ExtensionPath<P, E> path, BiConsumer<E, E> conventionSetter) {
        return create(project, project.getRootProject(), path, conventionSetter);
    }

    public static <E extends ExtensionAware, P extends ExtensionAware> E create(ExtensionAware parent, ExtensionAware root, ExtensionPath<P, E> path) {
        return create(parent, root, path, null);
    }

    public static <E extends ExtensionAware, P extends ExtensionAware> E create(ExtensionAware parent, ExtensionAware root, ExtensionPath<P, E> path, BiConsumer<E, E> conventionSetter) {
        E extension = createExtension(parent, path);

        if (root != null && !Objects.equals(parent, root)) {
            try {
                E rootExtension = path.get(root);
                if (conventionSetter != null) {
                    conventionSetter.accept(extension, rootExtension);
                }
            } catch (UnknownDomainObjectException e) {
                throw new IllegalStateException("The path " + path + " is missing in root " + root + "! Please, create the extension first in the root!", e);
            }
        }

        return extension;
    }

    private static <E extends ExtensionAware, P extends ExtensionAware> E createExtension(ExtensionAware parent, ExtensionPath<P, E> path) {
        Optional<ExtensionPath<?, P>> maybeParentPath = path.getParent();
        ExtensionTypeAndName<E> current = path.getCurrent();

        if (maybeParentPath.isPresent()) {
            try {
                P grandParent = maybeParentPath.get().get(parent);
                return grandParent.getExtensions().create(current.getName(), current.getExtensionType());
            } catch (UnknownDomainObjectException e) {
                throw new IllegalStateException("The path " + path + " is missing in parent " + parent + "! Please, create the extension first in the parent!", e);
            }
        }

        return parent.getExtensions().create(current.getName(), current.getExtensionType());
    }


}
