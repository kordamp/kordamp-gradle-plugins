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
package org.kordamp.gradle.plugin.profiles.internal

import groovy.transform.CompileStatic
import org.apache.maven.artifact.versioning.ArtifactVersion
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException
import org.apache.maven.artifact.versioning.Restriction
import org.apache.maven.artifact.versioning.VersionRange
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.kordamp.gradle.plugin.profiles.Activation

import javax.inject.Inject

import static org.apache.commons.lang3.StringUtils.isNotBlank
import static org.apache.commons.lang3.StringUtils.split
import static org.apache.commons.lang3.StringUtils.stripEnd

/**
 *
 * @author Andres Almiray
 * @since 0.35.0
 */
@CompileStatic
class ActivationJdk implements Activation {
    final Property<String> version

    @Inject
    ActivationJdk(ObjectFactory objects) {
        version = objects.property(String)
    }

    boolean isActive(Project project) {
        if (!version.present) {
            throw new IllegalStateException("No value for 'jdk' has been set.")
        }

        ArtifactVersion detectedVersion = detectVersion(project)
        String requiredVersionRange = version.get()

        if (isNotBlank(requiredVersionRange)) {
            VersionRange vr = null
            String msg = 'Detected JDK Version: ' + detectedVersion

            // short circuit check if the strings are exactly equal
            if (detectedVersion.toString().equals(requiredVersionRange)) {
                project.logger.info(msg + ' is allowed in the range ' + requiredVersionRange + '.')
            } else {
                try {
                    vr = VersionRange.createFromVersionSpec(requiredVersionRange)

                    if (containsVersion(vr, detectedVersion)) {
                        project.logger.info(msg + ' is allowed in the range ' + requiredVersionRange + '.')
                        return true
                    } else {
                        project.logger.info(msg + ' is not in the allowed range ' + vr + '.')
                        return false
                    }
                } catch (InvalidVersionSpecificationException e) {
                    project.logger.warn('The requested JDK version '
                        + requiredVersionRange + ' is invalid.', e)
                    false
                }
            }
        }

        false
    }

    private ArtifactVersion detectVersion(Project project) {
        String javaVersion = System.getProperty('java.version')

        project.logger.info("Detected Java String: '" + javaVersion + "'")
        javaVersion = normalizeJDKVersion(javaVersion)
        project.logger.info("Normalized Java String: '" + javaVersion + "'")

        ArtifactVersion detectedJdkVersion = new DefaultArtifactVersion(javaVersion)

        project.logger.info("Parsed Version: Major: " + detectedJdkVersion.majorVersion + " Minor: "
            + detectedJdkVersion.minorVersion + " Incremental: " + detectedJdkVersion.incrementalVersion
            + " Build: " + detectedJdkVersion.buildNumber + " Qualifier: " + detectedJdkVersion.qualifier)

        detectedJdkVersion
    }

    static Map<String, String> detectedVersionAsMap() {
        String javaVersion = System.getProperty('java.version')
        javaVersion = normalizeJDKVersion(javaVersion)
        ArtifactVersion detectedJdkVersion = new DefaultArtifactVersion(javaVersion)

        [
            version    : javaVersion,
            major      : String.valueOf(detectedJdkVersion.majorVersion),
            minor      : String.valueOf(detectedJdkVersion.minorVersion),
            incremental: String.valueOf(detectedJdkVersion.incrementalVersion),
            build      : String.valueOf(detectedJdkVersion.buildNumber),
            qualifier  : String.valueOf(detectedJdkVersion.qualifier)
        ]
    }

    private static String normalizeJDKVersion(String theJdkVersion) {
        theJdkVersion = theJdkVersion.replaceAll('_|-', '.')
        String[] tokenArray = split(theJdkVersion, '.')
        List<String> tokens = Arrays.asList(tokenArray)
        StringBuffer buffer = new StringBuffer(theJdkVersion.length())

        Iterator<String> iter = tokens.iterator()
        for (int i = 0; i < tokens.size() && i < 4; i++) {
            String section = iter.next()
            section = section.replaceAll('[^0-9]', '')

            if (isNotBlank(section)) {
                buffer.append(Integer.parseInt(section))

                if (i != 2) {
                    buffer.append('.')
                } else {
                    buffer.append('-')
                }
            }
        }

        String version = buffer.toString()
        version = stripEnd(version, '-')
        return stripEnd(version, '.')
    }

    static boolean containsVersion(VersionRange allowedRange, ArtifactVersion theVersion) {
        boolean matched = false
        ArtifactVersion recommendedVersion = allowedRange.getRecommendedVersion()
        if (recommendedVersion == null) {
            List<Restriction> restrictions = allowedRange.getRestrictions()
            for (Restriction restriction : restrictions) {
                if (restriction.containsVersion(theVersion)) {
                    matched = true
                    break
                }
            }
        } else {
            // only singular versions ever have a recommendedVersion
            @SuppressWarnings('unchecked')
            int compareTo = recommendedVersion.compareTo(theVersion)
            matched = (compareTo <= 0)
        }
        return matched
    }
}
