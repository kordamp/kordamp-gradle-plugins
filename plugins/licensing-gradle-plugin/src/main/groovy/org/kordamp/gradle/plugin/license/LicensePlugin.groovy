/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kordamp.gradle.plugin.license

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.kordamp.gradle.plugin.licensing.LicensingPlugin

/**
 * @author Andres Almiray
 */
@CompileStatic
class LicensePlugin extends LicensingPlugin{
    @Override
    void apply(Project project) {
        project.logger.warn("Plugin org.kordamp.gradle.license has been deprecated, use org.kordamp.gradle.licensing instead")
        super.apply(project)
    }
}
