/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
package org.kordamp.gradle.plugin.plugin

import groovy.transform.CompileStatic
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.plugin.devel.PluginDeclaration
import org.kordamp.gradle.plugin.base.tasks.AbstractReportingTask

/**
 * @author Andres Almiray
 * @since 0.16.0
 */
@CompileStatic
class ListPluginDescriptors extends AbstractReportingTask {
    @Input
    final ListProperty<PluginDeclaration> declarations

    ListPluginDescriptors() {
        this.declarations = project.objects.listProperty(PluginDeclaration)
    }

    @TaskAction
    void listPluginDescriptors() {
        Map<String, Map<String, String>> map = [:]
        for (PluginDeclaration declaration : declarations.get()) {
            map.put(declaration.name, [
                id                 : declaration.id,
                implementationClass: declaration.implementationClass,
                displayName        : declaration.displayName,
                description        : declaration.description
            ])
        }

        doPrint(map, 0)
    }
}
