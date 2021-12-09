package org.kordamp.gradle.plugin.base.extensions;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;
import org.kordamp.gradle.plugin.base.util.ExtensionPath;
import org.kordamp.gradle.plugin.base.util.ExtensionUtil;

public interface BuildInfoExtension extends ExtensionAware {

    String NAME = "buildInfo";
    ExtensionPath<ConfigExtension, BuildInfoExtension> PATH = ConfigExtension.PATH.append(NAME, BuildInfoExtension.class);

    Property<Boolean> getClearTime();
    Property<Boolean> getSkipBuildBy();
    Property<Boolean> getSkipBuildDate();
    Property<Boolean> getSkipBuildTime();
    Property<Boolean> getSkipBuildRevision();
    Property<Boolean> getSkipBuildJdk();
    Property<Boolean> getSkipBuildOs();
    Property<Boolean> getSkipBuildCreatedBy();
    Property<String> getBuildBy();
    Property<String> getBuildDate();
    Property<String> getBuildTime();
    Property<String> getBuildRevision();
    Property<String> getBuildJdk();
    Property<String> getBuildOs();
    Property<String> getBuildCreatedBy();

    static BuildInfoExtension createIfMissing(Project project) {
        return ExtensionUtil.createIfMissing(project, PATH, (ext, root) -> {
            ext.getClearTime().convention(root.getClearTime());
            ext.getSkipBuildBy().convention(root.getSkipBuildBy());
            ext.getSkipBuildDate().convention(root.getSkipBuildDate());
            ext.getSkipBuildTime().convention(root.getSkipBuildTime());
            ext.getSkipBuildRevision().convention(root.getSkipBuildRevision());
            ext.getSkipBuildJdk().convention(root.getSkipBuildJdk());
            ext.getSkipBuildOs().convention(root.getSkipBuildOs());
            ext.getSkipBuildCreatedBy().convention(root.getSkipBuildCreatedBy());
            ext.getBuildBy().convention(root.getBuildBy());
            ext.getBuildDate().convention(root.getBuildDate());
            ext.getBuildTime().convention(root.getBuildTime());
            ext.getBuildRevision().convention(root.getBuildRevision());
            ext.getBuildJdk().convention(root.getBuildJdk());
            ext.getBuildOs().convention(root.getBuildOs());
            ext.getBuildCreatedBy().convention(root.getBuildCreatedBy());
        });
    }

}
