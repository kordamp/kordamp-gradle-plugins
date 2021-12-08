package org.kordamp.gradle.plugin.base.extensions;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;
import org.kordamp.gradle.plugin.base.util.ExtensionPath;
import org.kordamp.gradle.plugin.base.util.ExtensionUtil;

public interface CiManagementExtension extends ExtensionAware {

    String NAME = "ciManagement";
    ExtensionPath<InfoExtension, CiManagementExtension> PATH = InfoExtension.PATH.append(NAME, CiManagementExtension.class);

    Property<String> getSystem();
    Property<String> getUrl();
    NamedDomainObjectContainer<Notifier> getNotifiers();

    // TODO: isEmpty
    // TODO: mergeNotifiersOnDemand

    static CiManagementExtension createIfMissing(Project project) {
        return ExtensionUtil.createIfMissing(project, PATH, (ext, root) -> {
            ext.getSystem().convention(root.getSystem());
            ext.getUrl().convention(root.getUrl());
        });
    }

}
