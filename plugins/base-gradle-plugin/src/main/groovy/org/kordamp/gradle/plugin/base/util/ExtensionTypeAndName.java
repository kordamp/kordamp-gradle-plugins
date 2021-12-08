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
