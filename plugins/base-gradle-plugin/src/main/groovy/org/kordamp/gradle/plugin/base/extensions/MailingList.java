package org.kordamp.gradle.plugin.base.extensions;

import org.gradle.api.Named;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

public interface MailingList extends Named {

    Property<String> getSubscribe();
    Property<String> getUnsubscribe();
    Property<String> getPost();
    Property<String> getArchive();
    ListProperty<String> getOtherArchives();

}
