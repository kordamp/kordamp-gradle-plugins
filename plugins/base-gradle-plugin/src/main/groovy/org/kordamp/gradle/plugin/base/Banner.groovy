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
package org.kordamp.gradle.plugin.base

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

import java.text.MessageFormat

/**
 *
 * @author Andres Almiray
 * @since 0.30.0
 */
@CompileStatic
abstract class Banner implements BuildService<Params> {
    private static final String ORG_KORDAMP_BANNER = 'org.kordamp.banner'

    private String productVersion
    private String productId
    private final List<String> projectNames = []

    interface Params extends BuildServiceParameters {
    }

    void display(Project project) {
        if (checkIfVisited(project)) return

        ResourceBundle bundle = ResourceBundle.getBundle(Banner.name)
        productVersion = bundle.getString('product.version')
        productId = bundle.getString('product.id')
        String productName = bundle.getString('product.name')
        String banner = MessageFormat.format(bundle.getString('product.banner'), productName, productVersion)

        boolean printBanner = null == System.getProperty(ORG_KORDAMP_BANNER) || Boolean.getBoolean(ORG_KORDAMP_BANNER)

        File parent = new File(project.gradle.gradleUserHomeDir, 'caches')
        File markerFile = getMarkerFile(parent)
        if (!markerFile.exists()) {
            markerFile.parentFile.mkdirs()
            markerFile.text = '1'
            if (printBanner) System.err.println(banner)
        } else {
            try {
                int count = Integer.parseInt(markerFile.text)
                if (count < 3) {
                    if (printBanner) System.err.println(banner)
                }
                markerFile.text = (count + 1) + ''
            } catch (NumberFormatException e) {
                markerFile.text = '1'
                if (printBanner) System.err.println(banner)
            }
        }
    }

    private boolean checkIfVisited(Project project) {
        if (projectNames.contains(project.rootProject.name)) {
            return true
        }
        projectNames.add(project.rootProject.name)
        return false
    }

    private File getMarkerFile(File parent) {
        new File(parent,
            'kordamp' +
                File.separator +
                productId +
                File.separator +
                productVersion +
                File.separator +
                'marker.txt')
    }
}
