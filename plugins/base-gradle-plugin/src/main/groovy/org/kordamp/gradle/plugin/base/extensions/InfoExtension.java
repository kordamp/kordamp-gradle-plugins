package org.kordamp.gradle.plugin.base.extensions;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.kordamp.gradle.plugin.base.util.ExtensionPath;
import org.kordamp.gradle.plugin.base.util.ExtensionUtil;


public interface InfoExtension extends ExtensionAware {

    String NAME = "info";
    ExtensionPath<ConfigExtension, InfoExtension> PATH = ConfigExtension.PATH.append(NAME, InfoExtension.class);

    Property<String> getName();
    Property<String> getDescription();
    Property<String> getInceptionYear();
    Property<String> getVendor();
    ListProperty<String> getTags();

    NamedDomainObjectContainer<Repository> getRepositories();
    NamedDomainObjectContainer<Person> getPeople();
    NamedDomainObjectContainer<MailingList> getMailingLists();
    NamedDomainObjectContainer<NamedCredentials> getExternalCredentials();

    // TODO: merge containers

    static InfoExtension createIfMissing(Project project) {
        InfoExtension config = ExtensionUtil.createIfMissing(project, PATH, (ext, root) -> {
            ext.getName().convention(root.getName());
            ext.getDescription().convention(root.getDescription());
            ext.getInceptionYear().convention(root.getInceptionYear());
            ext.getVendor().convention(root.getVendor());
            ext.getTags().convention(root.getTags());
        });

        OrganizationExtension.createIfMissing(project);
        LinksExtension.createIfMissing(project);
        ScmExtension.createIfMissing(project);
        IssueManagementExtension.createIfMissing(project);
        CiManagementExtension.createIfMissing(project);
        SpecificationExtension.createIfMissing(project);
        ImplementationExtension.createIfMissing(project);

        return config;
    }

}
