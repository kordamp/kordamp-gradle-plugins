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

public interface ConfigExtension extends ExtensionAware {

    // TODO: change to config once migrated
    String NAME = "kordamp";
    ExtensionPath<ExtensionAware, ConfigExtension> PATH = ExtensionPath.create(NAME, ConfigExtension.class);

    Property<Boolean> getRelease();

    static ConfigExtension create(Project project) {
        ConfigExtension config = ExtensionUtil.create(project, project.getRootProject(), PATH, (ext, root) -> {
            ext.getRelease().convention(root.getRelease());
        });

        InfoExtension.create(project);
        BomExtension.create(project);
        BuildInfoExtension.create(project);
        ClirrExtension.create(project);

        return config;
    }

}
