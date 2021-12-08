package org.kordamp.gradle.plugin.base.extensions;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;
import org.kordamp.gradle.plugin.base.util.ExtensionPath;
import org.kordamp.gradle.plugin.base.util.ExtensionUtil;

public interface IssueManagementExtension extends ExtensionAware {

    String NAME = "issueManagement";
    ExtensionPath<InfoExtension, IssueManagementExtension> PATH = InfoExtension.PATH.append(NAME, IssueManagementExtension.class);

    Property<String> getSystem();
    Property<String> getUrl();

    // TODO: isEmpty

    static IssueManagementExtension createIfMissing(Project project) {
        return ExtensionUtil.createIfMissing(project, PATH, (ext, root) -> {
            ext.getSystem().convention(root.getSystem());
            ext.getUrl().convention(root.getUrl());
        });
    }

}
