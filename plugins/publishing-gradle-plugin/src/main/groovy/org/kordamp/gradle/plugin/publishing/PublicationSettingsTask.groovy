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
package org.kordamp.gradle.plugin.publishing

import groovy.transform.CompileStatic
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.kordamp.gradle.plugin.base.tasks.AbstractReportingTask

import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.20.0
 */
@CompileStatic
class PublicationSettingsTask extends AbstractReportingTask {
    private boolean absolute
    private String publication
    private Set<String> publications

    @Option(option = 'absolute', description = "Should paths be printed as absolutes or not. Defaults to 'false'")
    void setAbsolute(boolean absolute) {
        this.absolute = absolute
    }

    @Option(option = 'publication', description = 'The publication to generate the report for.')
    void setPublication(String publication) {
        this.publication = publication
    }

    @Option(option = 'publications', description = 'The publications to generate the report for.')
    void setPublications(String publications) {
        if (isNotBlank(publications)) {
            this.publications = (publications.split(',').collect { it.trim() }) as Set
        }
    }

    @TaskAction
    void report() {
        PublishingExtension publishing = project.extensions.findByType(PublishingExtension)
        if (publishing) {
            if (publications) {
                publications.each { p ->
                    printPublication(publishing.publications.findByName(p))
                }
            } else if (publications) {
                printPublication(publishing.publications.findByName(publication))
            } else {
                publishing.publications.each { Publication p ->
                    printPublication(p)
                }
            }
        }
    }

    private void printPublication(Publication publication) {
        print(publication.name + ':', 0)
        if (publication instanceof MavenPublication) {
            doPrintMapEntry('type', 'Maven', 1)
            MavenPublication p = (MavenPublication) publication
            doPrintMapEntry('groupId', p.groupId, 1)
            doPrintMapEntry('artifactId', p.artifactId, 1)
            doPrintMapEntry('version', p.version, 1)
            if (p.artifacts.size()) {
                print('artifacts:', 1)
                int index = 0
                p.artifacts.each { artifact ->
                    print('artifact ' + (index++) + ':', 2)
                    doPrintMapEntry('file', adjustPath(project.rootProject.projectDir.toString(), artifact.file?.absolutePath), 3)
                    doPrintMapEntry('classifier', artifact.classifier, 3)
                    doPrintMapEntry('extension', artifact.extension, 3)
                }
            }
        }
        println ' '
    }

    private String adjustPath(String rootDir, String path) {
        if (isNotBlank(path) && !absolute) {
            return (path - rootDir)[1..-1]
        }
        return path
    }
}
