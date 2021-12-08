package org.kordamp.gradle.plugin.base.extensions;

import org.gradle.api.Named;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

public interface Person extends Named {

    default String getId() {
        return getName();
    };

    // XXX: renamed
    Property<String> getFullName();

    Property<String> getEmail();
    Property<String> getUrl();
    Property<String> getTimezone();
    Property<Organization> getOrganization();
    ListProperty<String> getRoles();
    MapProperty<String, String> getProperties();

}
