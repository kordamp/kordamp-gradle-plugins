package org.kordamp.gradle.plugin.base.extensions;

import org.gradle.api.Named;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;


public interface Notifier extends Named {

    default String getId() { return getName(); }

    Property<String> getType();
    Property<Boolean> getSendOnError();
    Property<Boolean> getSendOnFailure();
    Property<Boolean> getSendOnSuccess();
    Property<Boolean> getSendOnWarning();
    MapProperty<String, String> getConfiguration();

    // TODO: merge

}
