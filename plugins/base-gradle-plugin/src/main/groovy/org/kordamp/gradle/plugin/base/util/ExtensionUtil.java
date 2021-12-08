package org.kordamp.gradle.plugin.base.util;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

public class ExtensionUtil {

    public static <E extends ExtensionAware, P extends ExtensionAware> E createIfMissing(Project project, ExtensionPath<P, E> path) {
        return createIfMissing(project, path, null);
    }

    public static <E extends ExtensionAware, P extends ExtensionAware> E createIfMissing(Project project, ExtensionPath<P, E> path, BiConsumer<E, E> conventionSetter) {
        return createIfMissing(project, project.getRootProject(), path, conventionSetter);
    }

    public static <E extends ExtensionAware, P extends ExtensionAware> E createIfMissing(ExtensionAware parent, ExtensionAware root, ExtensionPath<P, E> path) {
        return createIfMissing(parent, root, path, null);
    }

    public static <E extends ExtensionAware, P extends ExtensionAware> E createIfMissing(ExtensionAware parent, ExtensionAware root, ExtensionPath<P, E> path, BiConsumer<E, E> conventionSetter) {
        E extension = findOrCreateExtension(parent, path);

        if (root != null && !Objects.equals(parent, root)) {
            E rootExtension = findOrCreateExtension(root, path);
            if (conventionSetter != null) {
                conventionSetter.accept(extension, rootExtension);
            }
        }

        return extension;
    }

    private static <E extends ExtensionAware, P extends ExtensionAware> E findOrCreateExtension(ExtensionAware parent, ExtensionPath<P, E> path) {
        Optional<ExtensionPath<?, P>> maybeParentPath = path.getParent();
        ExtensionTypeAndName<E> current = path.getCurrent();
        if (!maybeParentPath.isPresent()) {
            E extension = parent.getExtensions().findByType(current.getExtensionType());
            if (extension != null) {
                return extension;
            }
            return parent.getExtensions().create(current.getName(), current.getExtensionType());
        }

        ExtensionPath<?, P> parentPath = maybeParentPath.get();
        return findOrCreateExtension(findOrCreateExtension(parent, parentPath), path.asOrphan());
    }


}
