/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2022 Andres Almiray.
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
package org.kordamp.gradle.plugin.profiles

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.profiles.internal.ProfilesExtensionImpl
import org.kordamp.gradle.plugin.profiles.tasks.ActiveProfilesTask
import org.kordamp.gradle.plugin.profiles.tasks.DisplayActivationInfoTask
import org.kordamp.gradle.plugin.profiles.tasks.ListProfilesTask

/**
 *
 * @author Andres Almiray
 * @since 0.35.0
 */
@CompileStatic
class ProfilesPlugin extends AbstractKordampPlugin {
    ProfilesPlugin() {
        super('org.kordamp.gradle.profiles')
    }

    void apply(Project project) {
        configureProject(project)
        project.childProjects.values().each {
            configureProject(it)
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(ProfilesPlugin)) {
            project.pluginManager.apply(ProfilesPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        project.extensions.create(ProfilesExtension, 'profiles', ProfilesExtensionImpl, project)

        project.tasks.register('listProfiles', ListProfilesTask,
            new Action<ListProfilesTask>() {
                @Override
                void execute(ListProfilesTask t) {
                    t.group = 'Insight'
                    t.description = "Displays all profiles for project '$project.name'."
                }
            })

        project.tasks.register('activeProfiles', ActiveProfilesTask,
            new Action<ActiveProfilesTask>() {
                @Override
                void execute(ActiveProfilesTask t) {
                    t.group = 'Insight'
                    t.description = "Displays active profiles for project '$project.name'."
                }
            })

        project.tasks.register('displayActivationInfo', DisplayActivationInfoTask,
            new Action<DisplayActivationInfoTask>() {
                @Override
                void execute(DisplayActivationInfoTask t) {
                    t.group = 'Insight'
                    t.description = 'Displays information used for profile activation.'
                }
            })
    }
}
