package org.kordamp.gradle.plugin.base.extensions;

import org.gradle.api.provider.Property;

public interface Credentials {

    Property<String> getUsername();
    Property<String> getPassword();

}
