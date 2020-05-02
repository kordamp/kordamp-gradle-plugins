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
package org.kordamp.gradle.plugin.base

import groovy.transform.CompileStatic
import org.gradle.api.provider.Provider
import org.kordamp.gradle.plugin.base.resolved.model.ResolvedInformation
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedBintray
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedBom
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedBuildInfo
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedCheckstyle
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedClirr
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedCodenarc
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedCoveralls
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedDetekt
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedErrorProne
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedFeature
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedGroovydoc
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedGuide
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedJacoco
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedJar
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedJavadoc
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedKotlindoc
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedLicensing
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedMinpom
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedPlugin
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedPmd
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedPublishing
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedScaladoc
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedSonar
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedSource
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedSourceHtml
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedSourceXref
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedSpotbugs
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedStats
import org.kordamp.gradle.plugin.base.resolved.plugins.ResolvedTesting

/**
 * @author Andres Almiray
 * @since 0.40.0
 */
@CompileStatic
interface ResolvedProjectConfigurationExtension {
    Provider<Boolean> getRelease()

    ResolvedInformation getInfo()

    ResolvedBom getBom()

    ResolvedBintray getBintray()

    ResolvedBuildInfo getBuildInfo()

    ResolvedClirr getClirr()

    ResolvedLicensing getLicensing()

    ResolvedPlugin getPlugin()

    ResolvedPublishing getPublishing()

    ResolvedStats getStats()

    ResolvedTesting getTesting()

    ResolvedArtifacts getArtifacts()

    ResolvedDocs getDocs()

    ResolvedCoverage getCoverage()

    ResolvedQuality getQuality()

    Map<String, Object> toMap()

    @CompileStatic
    interface ResolvedQuality extends ResolvedFeature {
        ResolvedCheckstyle getCheckstyle()

        ResolvedCodenarc getCodenarc()

        ResolvedDetekt getDetekt()

        ResolvedErrorProne getErrorprone()

        ResolvedPmd getPmd()

        ResolvedSpotbugs getSpotbugs()

        ResolvedSonar getSonar()

        Map<String, Object> toMap()
    }

    @CompileStatic
    interface ResolvedCoverage extends ResolvedFeature {
        ResolvedCoveralls getCoveralls()

        ResolvedJacoco getJacoco()

        Map<String, Object> toMap()
    }

    @CompileStatic
    interface ResolvedDocs extends ResolvedFeature {
        ResolvedGuide getGuide()

        ResolvedGroovydoc getGroovydoc()

        ResolvedKotlindoc getKotlindoc()

        ResolvedJavadoc getJavadoc()

        ResolvedScaladoc getScaladoc()

        ResolvedSourceHtml getSourceHtml()

        ResolvedSourceXref getSourceXref()

        Map<String, Object> toMap()
    }

    @CompileStatic
    interface ResolvedArtifacts extends ResolvedFeature {
        ResolvedJar getJar()

        ResolvedMinpom getMinpom()

        ResolvedSource getSource()

        Map<String, Object> toMap()
    }
}
