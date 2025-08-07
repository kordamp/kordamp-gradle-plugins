/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2025 Andres Almiray.
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
package org.kordamp.gradle.plugin.publishing.central

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.publish.maven.MavenArtifact
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.tasks.bundling.Zip

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Creates ZIP bundles for Maven Central publishing
 *
 * @author Andres Almiray
 * @since 0.55.0
 */
@CompileStatic
class MavenCentralBundleCreator {
    private static final Logger LOGGER = Logging.getLogger(MavenCentralBundleCreator)
    
    private final Project project
    
    MavenCentralBundleCreator(Project project) {
        this.project = project
    }
    
    /**
     * Create a bundle for a Maven publication
     * @param publication The Maven publication
     * @param outputDirectory Directory where the bundle will be created
     * @return Path to the created bundle
     */
    Path createBundle(MavenPublication publication, File outputDirectory) {
        LOGGER.info("Creating bundle for publication: ${publication.name}")
        
        String bundleName = "${publication.groupId}-${publication.artifactId}-${publication.version}-bundle.zip"
        Path bundlePath = Paths.get(outputDirectory.absolutePath, bundleName)
        
        // Ensure output directory exists
        Files.createDirectories(bundlePath.parent)
        
        // Collect all artifacts
        Map<String, File> artifacts = collectArtifacts(publication)
        
        // Create ZIP bundle
        createZipBundle(bundlePath, artifacts)
        
        LOGGER.info("Bundle created: ${bundlePath}")
        return bundlePath
    }
    
    /**
     * Collect all artifacts from a publication
     * @param publication The Maven publication
     * @return Map of artifact path to file
     */
    private Map<String, File> collectArtifacts(MavenPublication publication) {
        Map<String, File> artifacts = new LinkedHashMap<>()
        
        String groupPath = publication.groupId.replace('.', '/')
        String artifactPath = "${groupPath}/${publication.artifactId}/${publication.version}"
        
        // Add main artifacts
        publication.artifacts.each { MavenArtifact artifact ->
            String fileName = "${publication.artifactId}-${publication.version}"
            if (artifact.classifier) {
                fileName += "-${artifact.classifier}"
            }
            fileName += ".${artifact.extension}"
            
            String artifactRelativePath = "${artifactPath}/${fileName}"
            artifacts.put(artifactRelativePath, artifact.file)
            
            // Add corresponding signature file if it exists
            File signatureFile = new File(artifact.file.parentFile, "${artifact.file.name}.asc")
            if (signatureFile.exists()) {
                String signatureRelativePath = "${artifactPath}/${fileName}.asc"
                artifacts.put(signatureRelativePath, signatureFile)
            }
        }
        
        // Add POM file
        String pomFileName = "${publication.artifactId}-${publication.version}.pom"
        String pomRelativePath = "${artifactPath}/${pomFileName}"
        File pomFile = createPomFile(publication, pomFileName)
        artifacts.put(pomRelativePath, pomFile)
        
        // Add POM signature if signing is enabled
        File pomSignatureFile = new File(pomFile.parentFile, "${pomFile.name}.asc")
        if (pomSignatureFile.exists()) {
            String pomSignatureRelativePath = "${artifactPath}/${pomFileName}.asc"
            artifacts.put(pomSignatureRelativePath, pomSignatureFile)
        }
        
        return artifacts
    }
    
    /**
     * Create a temporary POM file for the publication
     * @param publication The Maven publication
     * @param fileName The POM file name
     * @return The created POM file
     */
    private File createPomFile(MavenPublication publication, String fileName) {
        // Look for existing generated POM file first
        File existingPomFile = findExistingPomFile(publication)
        if (existingPomFile && existingPomFile.exists()) {
            return existingPomFile
        }
        
        // If no existing POM, create a basic one
        File pomFile = new File(project.layout.buildDirectory.asFile.get(), "tmp/maven-central/${fileName}")
        pomFile.parentFile.mkdirs()
        
        String pomContent = generateBasicPomContent(publication)
        pomFile.text = pomContent
        return pomFile
    }
    
    /**
     * Find existing POM file generated by maven-publish plugin
     * @param publication The Maven publication
     * @return Existing POM file or null
     */
    private File findExistingPomFile(MavenPublication publication) {
        // Look in standard Maven publication directories
        File publicationDir = new File(project.layout.buildDirectory.asFile.get(), "publications/${publication.name}")
        File pomFile = new File(publicationDir, "pom-default.xml")
        
        if (pomFile.exists()) {
            return pomFile
        }
        
        // Alternative location
        pomFile = new File(publicationDir, "${publication.name}-${publication.version}.pom")
        if (pomFile.exists()) {
            return pomFile
        }
        
        return null
    }
    
    /**
     * Generate basic POM content for the publication
     * @param publication The Maven publication
     * @return POM XML content
     */
    private String generateBasicPomContent(MavenPublication publication) {
        StringBuilder pom = new StringBuilder()
        pom.append('<?xml version="1.0" encoding="UTF-8"?>\n')
        pom.append('<project xmlns="http://maven.apache.org/POM/4.0.0" ')
        pom.append('xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" ')
        pom.append('xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 ')
        pom.append('http://maven.apache.org/xsd/maven-4.0.0.xsd">\n')
        pom.append('  <modelVersion>4.0.0</modelVersion>\n')
        pom.append("  <groupId>${publication.groupId}</groupId>\n")
        pom.append("  <artifactId>${publication.artifactId}</artifactId>\n")
        pom.append("  <version>${publication.version}</version>\n")
        pom.append('  <packaging>jar</packaging>\n')
        pom.append('</project>\n')
        
        return pom.toString()
    }
    
    /**
     * Create the ZIP bundle
     * @param bundlePath Path where the bundle will be created
     * @param artifacts Map of artifact paths to files
     */
    private void createZipBundle(Path bundlePath, Map<String, File> artifacts) {
        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(bundlePath))) {
            
            artifacts.each { String relativePath, File file ->
                if (!file.exists()) {
                    LOGGER.warn("Artifact file does not exist: ${file.absolutePath}")
                    return
                }
                
                ZipEntry entry = new ZipEntry(relativePath)
                entry.time = file.lastModified()
                zipOut.putNextEntry(entry)
                
                Files.copy(file.toPath(), zipOut)
                zipOut.closeEntry()
                
                LOGGER.debug("Added to bundle: ${relativePath}")
            }
        }
    }
    
    /**
     * Validate that all required artifacts are present
     * @param publication The Maven publication
     * @return List of missing artifacts
     */
    List<String> validateArtifacts(MavenPublication publication) {
        List<String> missing = []
        
        // Check for main JAR (or equivalent)
        boolean hasMainArtifact = publication.artifacts.any { 
            it.classifier == null || it.classifier.isEmpty() 
        }
        if (!hasMainArtifact) {
            missing.add("Main artifact (JAR/WAR/etc)")
        }
        
        // Check for sources JAR
        boolean hasSourcesJar = publication.artifacts.any { 
            it.classifier == 'sources' 
        }
        if (!hasSourcesJar) {
            missing.add("Sources JAR")
        }
        
        // Check for javadoc JAR
        boolean hasJavadocJar = publication.artifacts.any { 
            it.classifier == 'javadoc' 
        }
        if (!hasJavadocJar) {
            missing.add("Javadoc JAR")
        }
        
        return missing
    }
}