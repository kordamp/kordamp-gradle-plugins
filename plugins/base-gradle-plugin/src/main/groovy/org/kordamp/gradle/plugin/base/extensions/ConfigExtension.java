package org.kordamp.gradle.plugin.base.extensions;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;
import org.kordamp.gradle.plugin.base.util.ExtensionPath;
import org.kordamp.gradle.plugin.base.util.ExtensionUtil;

public interface ConfigExtension extends ExtensionAware {

    // TODO: change to config once migrated
    String NAME = "kordamp";
    ExtensionPath<ExtensionAware, ConfigExtension> PATH = ExtensionPath.create(NAME, ConfigExtension.class);

    Property<Boolean> getRelease();

    static ConfigExtension createIfMissing(Project project) {
        ConfigExtension config = ExtensionUtil.createIfMissing(project, project.getRootProject(), PATH, (ext, root) -> {
            ext.getRelease().convention(root.getRelease());
        });

        InfoExtension.createIfMissing(project);

        return config;
    }

}
