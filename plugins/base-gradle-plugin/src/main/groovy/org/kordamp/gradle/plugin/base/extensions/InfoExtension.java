/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2021 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    static InfoExtension create(Project project) {
        InfoExtension config = ExtensionUtil.create(project, PATH, (ext, root) -> {
            ext.getName().convention(root.getName());
            ext.getDescription().convention(root.getDescription());
            ext.getInceptionYear().convention(root.getInceptionYear());
            ext.getVendor().convention(root.getVendor());
            ext.getTags().convention(root.getTags());
        });

        OrganizationExtension.create(project);
        LinksExtension.create(project);
        ScmExtension.create(project);
        IssueManagementExtension.create(project);
        CiManagementExtension.create(project);
        SpecificationExtension.create(project);
        ImplementationExtension.create(project);

        return config;
    }

}
