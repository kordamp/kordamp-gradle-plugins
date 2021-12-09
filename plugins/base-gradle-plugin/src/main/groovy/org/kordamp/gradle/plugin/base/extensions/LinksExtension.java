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
