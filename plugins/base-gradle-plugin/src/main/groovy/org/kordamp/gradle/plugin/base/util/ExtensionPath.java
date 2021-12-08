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

}
