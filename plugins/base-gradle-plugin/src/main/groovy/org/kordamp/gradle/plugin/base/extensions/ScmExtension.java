package org.kordamp.gradle.plugin.base.extensions;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;
import org.kordamp.gradle.plugin.base.util.ExtensionPath;
import org.kordamp.gradle.plugin.base.util.ExtensionUtil;

public interface ScmExtension extends ExtensionAware {

    String NAME = "scm";
    ExtensionPath<InfoExtension, ScmExtension> PATH = InfoExtension.PATH.append(NAME, ScmExtension.class);

    Property<Boolean> getEnabled();
    Property<String> getUrl();
    Property<String> getTag();
    Property<String> getConnection();
    Property<String> getDeveloperConnection();

    // TODO: isEmpty
    // TODO: validate

    static ScmExtension createIfMissing(Project project) {
        return ExtensionUtil.createIfMissing(project, PATH, (ext, root) -> {
            ext.getEnabled().convention(root.getEnabled().convention(true));
            ext.getUrl().convention(root.getUrl());
            ext.getTag().convention(root.getTag());
            ext.getConnection().convention(root.getConnection());
            ext.getDeveloperConnection().convention(root.getDeveloperConnection());
        });
    }

}
