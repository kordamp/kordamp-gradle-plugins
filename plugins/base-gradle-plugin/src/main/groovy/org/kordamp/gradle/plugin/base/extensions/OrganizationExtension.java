package org.kordamp.gradle.plugin.base.extensions;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.kordamp.gradle.plugin.base.util.ExtensionPath;
import org.kordamp.gradle.plugin.base.util.ExtensionUtil;

public interface OrganizationExtension extends ExtensionAware, Organization {

    String NAME = "organization";
    ExtensionPath<InfoExtension, OrganizationExtension> PATH = InfoExtension.PATH.append(NAME, OrganizationExtension.class);

    // TODO: isEmpty()

    static OrganizationExtension createIfMissing(Project project) {
        return ExtensionUtil.createIfMissing(project, PATH, (ext, root) -> {
            ext.getName().convention(root.getName());
            ext.getUrl().convention(root.getUrl());
        });
    }

}
