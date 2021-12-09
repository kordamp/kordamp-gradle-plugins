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

    static BuildInfoExtension create(Project project) {
        return ExtensionUtil.create(project, PATH, (ext, root) -> {
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
