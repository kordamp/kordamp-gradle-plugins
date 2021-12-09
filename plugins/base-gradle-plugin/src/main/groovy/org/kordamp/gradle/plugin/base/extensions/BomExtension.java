package org.kordamp.gradle.plugin.base.extensions;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.kordamp.gradle.plugin.base.util.ExtensionPath;
import org.kordamp.gradle.plugin.base.util.ExtensionUtil;

public interface BomExtension extends ExtensionAware {

    String NAME = "bom";
    ExtensionPath<ConfigExtension, BomExtension> PATH = ConfigExtension.PATH.append(NAME, BomExtension.class);

    SetProperty<String> getExcludes();
    SetProperty<String> getIncludes();

    MapProperty<String, String> getProperties();

    Property<Boolean> getAutoIncludes(); // true

    Property<String> getParent();

    Property<Boolean> getOverwriteInceptionYear();
    Property<Boolean> getOverwriteUrl();
    Property<Boolean> getOverwriteLicenses();
    Property<Boolean> getOverwriteScm();
    Property<Boolean> getOverwriteOrganization();
    Property<Boolean> getOverwriteDevelopers();
    Property<Boolean> getOverwriteContributors();
    Property<Boolean> getOverwriteIssueManagement();
    Property<Boolean> getOverwriteCiManagement();
    Property<Boolean> getOverwriteMailingLists();

    static BomExtension createIfMissing(Project project) {
        return ExtensionUtil.createIfMissing(project, PATH, (ext, root) -> {
            ext.getAutoIncludes().convention(root.getAutoIncludes().convention(true));

            ext.getExcludes().convention(root.getExcludes());
            ext.getIncludes().convention(root.getIncludes());

            ext.getProperties().convention(root.getProperties());

            ext.getParent().convention(root.getParent());

            ext.getOverwriteInceptionYear().convention(root.getOverwriteInceptionYear());
            ext.getOverwriteUrl().convention(root.getOverwriteUrl());
            ext.getOverwriteLicenses().convention(root.getOverwriteLicenses());
            ext.getOverwriteScm().convention(root.getOverwriteScm());
            ext.getOverwriteOrganization().convention(root.getOverwriteOrganization());
            ext.getOverwriteDevelopers().convention(root.getOverwriteDevelopers());
            ext.getOverwriteContributors().convention(root.getOverwriteContributors());
            ext.getOverwriteIssueManagement().convention(root.getOverwriteIssueManagement());
            ext.getOverwriteCiManagement().convention(root.getOverwriteCiManagement());
            ext.getOverwriteMailingLists().convention(root.getOverwriteMailingLists());
        });
    }

}
