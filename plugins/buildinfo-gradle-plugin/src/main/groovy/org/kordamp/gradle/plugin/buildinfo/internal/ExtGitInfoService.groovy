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
package org.kordamp.gradle.plugin.buildinfo.internal


import net.nemerosa.versioning.VersioningExtension
import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit
import org.gradle.api.GradleException
import org.gradle.api.Project

import java.time.ZonedDateTime

/**
 * @author Andres Almiray
 * @since 0.48.0
 */
class ExtGitInfoService {
    static ZonedDateTime getCommitTimestamp(Project project, VersioningExtension extension) {
        // Is Git enabled?
        boolean hasGit = project.rootProject.file('.git').exists() ||
            project.file('.git').exists() ||
            (extension.gitRepoRootDir != null &&
                new File(extension.gitRepoRootDir, '.git').exists())
        // No Git information
        if (hasGit) {
            // Git directory
            File gitDir = getGitDirectory(extension, project)
            // Open the Git repo
            Grgit grgit = Grgit.open(currentDir: gitDir)

            // Gets the commit info
            List<Commit> commits = grgit.log(maxCommits: 1)
            if (commits.empty) {
                throw new GradleException("No commit available in the repository - cannot compute version")
            }

            Commit lastCommit = commits[0]
            return lastCommit.dateTime
        }

        ZonedDateTime.now()
    }

    /**
     * Gets the actual Git working directory to use.
     * @param extension Extension of the plugin
     * @param project Project
     * @return Directory to use
     */
    private static File getGitDirectory(VersioningExtension extension, Project project) {
        return extension.gitRepoRootDir ?
            new File(extension.gitRepoRootDir) :
            project.projectDir
    }
}
