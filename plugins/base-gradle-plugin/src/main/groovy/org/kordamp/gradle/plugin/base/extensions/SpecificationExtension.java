package org.kordamp.gradle.plugin.base.extensions;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;
import org.kordamp.gradle.plugin.base.util.ExtensionPath;
import org.kordamp.gradle.plugin.base.util.ExtensionUtil;

public interface SpecificationExtension extends ExtensionAware {

    String NAME = "specification";
    ExtensionPath<InfoExtension, SpecificationExtension> PATH = InfoExtension.PATH.append(NAME, SpecificationExtension.class);

    Property<Boolean> getEnabled();
    Property<String> getTitle();
    Property<String> getVersion();
    Property<String> getVendor();

    // TODO: merge

    static SpecificationExtension createIfMissing(Project project) {
        return ExtensionUtil.createIfMissing(project, PATH, (ext, root) -> {
            ext.getEnabled().convention(root.getEnabled().convention(true));
            ext.getTitle().convention(root.getTitle());
            ext.getVersion().convention(root.getVersion());
            ext.getVendor().convention(root.getVendor());
        });
    }

}
