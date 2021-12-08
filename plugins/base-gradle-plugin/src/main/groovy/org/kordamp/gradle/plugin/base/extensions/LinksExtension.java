package org.kordamp.gradle.plugin.base.extensions;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;
import org.kordamp.gradle.plugin.base.util.ExtensionPath;
import org.kordamp.gradle.plugin.base.util.ExtensionUtil;

public interface LinksExtension extends ExtensionAware {

    String NAME = "links";
    ExtensionPath<InfoExtension, LinksExtension> PATH = InfoExtension.PATH.append(NAME, LinksExtension.class);

    Property<Boolean> getEnabled();
    Property<String> getWebsite();
    Property<String> getIssueTracker();
    Property<String> getScm();

    // TODO: validate

    static LinksExtension createIfMissing(Project project) {
        return ExtensionUtil.createIfMissing(project, PATH, (ext, root) -> {
            ext.getEnabled().convention(root.getEnabled().convention(true));
            ext.getWebsite().convention(root.getWebsite());
            ext.getIssueTracker().convention(root.getIssueTracker());
            ext.getScm().convention(root.getScm());
        });
    }

}
