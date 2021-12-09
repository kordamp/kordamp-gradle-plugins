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

    static ScmExtension create(Project project) {
        return ExtensionUtil.create(project, PATH, (ext, root) -> {
            ext.getEnabled().convention(root.getEnabled().convention(true));
            ext.getUrl().convention(root.getUrl());
            ext.getTag().convention(root.getTag());
            ext.getConnection().convention(root.getConnection());
            ext.getDeveloperConnection().convention(root.getDeveloperConnection());
        });
    }

}
