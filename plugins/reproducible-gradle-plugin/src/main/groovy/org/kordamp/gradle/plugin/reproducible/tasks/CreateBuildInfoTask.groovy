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
package org.kordamp.gradle.plugin.reproducible.tasks

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenArtifact
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.util.GradleVersion
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

import javax.inject.Inject
import java.security.DigestInputStream
import java.security.MessageDigest

import static org.kordamp.gradle.util.PluginUtils.resolveConfig
import static org.kordamp.gradle.util.StringUtils.isBlank
import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * Based on https://github.com/apache/maven-artifact-plugin/blob/master/src/main/java/org/apache/maven/plugins/artifact/buildinfo/BuildInfoWriter.java
 * Original author: Herve Boutemy
 *
 * @author Andres Almiray
 * @since 0.43.0
 */
@CompileStatic
class CreateBuildInfoTask extends DefaultTask {
    @OutputFile
    final RegularFileProperty outputFile

    @InputFiles
    @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    final ConfigurableFileCollection additionalArtifacts

    @Input
    @Optional
    final MapProperty<String, Object> additionalProperties

    @Inject
    CreateBuildInfoTask(ObjectFactory objects) {
        outputFile = objects.fileProperty()
        additionalArtifacts = objects.fileCollection()
        additionalProperties = objects.mapProperty(String, Object).convention([:])
    }

    @TaskAction
    void generateFile() {
        Collection<File> files = resolveFiles()

        if (!files) {
            return
        }

        File output = outputFile.asFile.get()
        output.parentFile.mkdirs()

        Writer writer = new PrintWriter(new FileOutputStream(output))
        printHeader(writer)
        printArtifacts(writer, files)
        writer.flush()
        writer.close()
    }

    @CompileDynamic
    private Collection<File> resolveFiles() {
        Collection<File> files = []
        PublishingExtension publishing = project.extensions.findByType(PublishingExtension)

        if (publishing) {
            publishing.publications.each { Publication publication ->
                if (publication instanceof MavenPublication) {
                    MavenPublication mvn = (MavenPublication) publication
                    mvn.artifacts.each { MavenArtifact artifact ->
                        if ('buildinfo' != artifact.extension &&
                            'sources' != artifact.classifier) {
                            files.add(artifact.file)
                        }
                    }
                }
            }
        }

        files.addAll(additionalArtifacts.files)

        files.unique().sort { File f -> f.name }
    }

    private void printHeader(Writer writer) {
        writer.println('# https://reproducible-builds.org/docs/jvm/')
        writer.println('buildinfo.version=1.0-SNAPSHOT')
        writer.println()
        writer.println("name=${project.name}")
        writer.println("group-id=${project.group}")
        writer.println("artifact-id=${project.name}")
        writer.println("version=${project.version}")
        writer.println()
        boolean scmUsed = printSourceInformation(writer)
        writer.println()
        writer.println('# build instructions')
        writer.println('build-tool=gradle')
        writer.println()
        printAdditionalProperties(writer)
        writer.println('# effective build environment information')
        writer.println("java.version=${System.properties['java.version']}")
        writer.println("java.vendor=${System.properties['java.vendor']}")
        writer.println("os.name=${System.properties['os.name']}")
        if (scmUsed) writer.println('source.used=scm')
        writer.println()
        printToolInformation(writer)
    }

    private boolean printSourceInformation(Writer writer) {
        ProjectConfigurationExtension config = resolveConfig(project)

        writer.println('# source information')

        String scmUrl = config.info.scm.connection
        if (isBlank(scmUrl)) scmUrl = config.info.links.scm

        if (isNotBlank(scmUrl)) {
            writer.println("source.scm.uri=${scmUrl}")
            if (isNotBlank(config.info.scm.tag)) {
                writer.println("source.scm.tag=${config.info.scm.tag}")
                if (String.valueOf(project.version).endsWith('-SNAPSHOT')) {
                    project.logger.warn("SCM source tag in buildinfo source.scm.tag=" + config.info.scm.tag
                        + " does not permit rebuilders reproducible source checkout")
                }
            }
            return true
        }

        writer.println("# no scm configured for ${project.path}")
        project.logger.warn("No source information available in buildinfo for rebuilders...")

        false
    }

    private void printAdditionalProperties(Writer writer) {
        Map<String, Object> props = additionalProperties.get()
        if (props) {
            writer.println('# additional properties')
            additionalProperties.get().each { key, value ->
                writer.println("${key}=${value}")
            }
            writer.println()
        }
        project.gradle.startParameter
    }

    private void printToolInformation(Writer writer) {
        writer.println('# Gradle rebuild instructions and effective environment')
        writer.println("gradle.version=${GradleVersion.current().version} (${GradleVersion.current().revision})")
        writer.println()
    }

    private void printArtifacts(Writer writer, Collection<File> files) {
        files.eachWithIndex { File file, int i ->
            writer.println("outputs.${i}.filename=${file.name}")
            writer.println("outputs.${i}.length=${file.length()}")
            writer.println("outputs.${i}.checksums.md5=${calculateHash(file, 'MD5')}")
            writer.println("outputs.${i}.checksums.sha1=${calculateHash(file, 'SHA1')}")
            writer.println("outputs.${i}.checksums.sha256=${calculateHash(file, 'SHA-256')}")
            writer.println("outputs.${i}.checksums.sha512=${calculateHash(file, 'SHA-512')}")
        }
    }

    private static calculateHash(File file, String algorithm) {
        file.withInputStream {
            new DigestInputStream(it, MessageDigest.getInstance(algorithm)).withStream {
                it.eachByte {}
                it.messageDigest.digest().encodeHex() as String
            }
        }
    }
}
