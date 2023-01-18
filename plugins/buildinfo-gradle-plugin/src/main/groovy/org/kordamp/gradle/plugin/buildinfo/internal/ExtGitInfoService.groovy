/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2023 Andres Almiray.
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
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.gradle.api.Project

import java.time.LocalDateTime
import java.time.ZoneId
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
            Git git = Git.open(gitDir)

            // Gets the commit info
            RevWalk walk = new RevWalk(git.getRepository())
            ObjectId head = git.getRepository().resolve(Constants.HEAD)
            RevCommit commit = null

            try {
                commit = walk.parseCommit(head)
            } catch (NullPointerException e) {
                throw new IllegalStateException('HEAD commit not found')
            }

            PersonIdent authorIdent = commit.getAuthorIdent()
            Date authorDate = authorIdent.getWhen()
            TimeZone authorTimeZone = authorIdent.getTimeZone()

            ZoneId zoneId = ZoneId.of(authorTimeZone.getID())
            LocalDateTime local = LocalDateTime.ofInstant(authorDate.toInstant(), zoneId)
            return ZonedDateTime.of(local, zoneId)
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
