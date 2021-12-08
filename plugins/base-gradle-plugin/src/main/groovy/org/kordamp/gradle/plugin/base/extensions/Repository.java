package org.kordamp.gradle.plugin.base.extensions;

import org.gradle.api.Named;
import org.gradle.api.provider.Property;

public interface Repository extends Named {

    Property<String> getUrl();
    // Property<Credentials> getCredentials();

    // TODO: merge
    // TODO: isEmpty
}
