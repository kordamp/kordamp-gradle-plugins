package org.kordamp.gradle.plugin.base.extensions;

import org.gradle.api.provider.Property;

public interface Organization {

    Property<String> getName();
    Property<String> getUrl();

}
