/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2020 Andres Almiray.
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
package org.kordamp.gradle.plugin.inline.adapters

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.plugins.JavaApplication
import org.kordamp.gradle.plugin.inline.PropertyAdapter
import org.kordamp.gradle.util.PluginUtils
import org.kordamp.jipsy.ServiceProviderFor

import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * Adapts properties for {@code org.gradle.api.plugins.JavaApplication}.
 *
 * Supported properties:
 * <ul>
 *     <li>application.mainClass - requires Gradle 6.4+</li>
 *     <li>application.mainModule - requires Gradle 6.4+</li>
 *     <li>application.mainClassName</li>
 *     <li>application.applicationDefaultJvmArgs</li>
 * </ul>
 *
 * @author Andres Almiray
 * @since 0.39.0
 */
@CompileStatic
@ServiceProviderFor(PropertyAdapter)
class ApplicationPluginPropertyAdapter implements PropertyAdapter {
    @Override
    void adapt(Project project) {
        if (!project.pluginManager.hasPlugin('application')) return

        JavaApplication application = project.extensions.getByType(JavaApplication)

        String mainClass = System.getProperty('application.mainClass')
        if (isNotBlank(mainClass)) {
            if (PluginUtils.isGradleCompatible('6.4')) {
                project.logger.debug("Setting application.mainClass to '{}'", mainClass)
                application.mainClass.set(mainClass)
            } else {
                project.logger.warn("Setting application.mainClassName to '{}'", mainClass)
                application.mainClassName = mainClass
            }
        }

        String mainClassName = System.getProperty('application.mainClassName')
        if (isNotBlank(mainClassName)) {
            project.logger.warn("Setting application.mainClassName to '{}'", mainClassName)
            application.mainClassName = mainClassName
        }

        String mainModule = System.getProperty('application.mainModule')
        if (isNotBlank(mainModule)) {
            if (PluginUtils.isGradleCompatible('6.4')) {
                project.logger.debug("Setting application.mainModule to '{}'", mainClass)
                application.mainModule.set(mainModule)
            }
        }

        String applicationDefaultJvmArgs = System.getProperty('application.applicationDefaultJvmArgs')
        if (isNotBlank(applicationDefaultJvmArgs)) {
            project.logger.debug("Setting application.applicationDefaultJvmArgs to '{}'", applicationDefaultJvmArgs)
            application.applicationDefaultJvmArgs = Arrays.asList(applicationDefaultJvmArgs.split(','))
        }
    }
}
