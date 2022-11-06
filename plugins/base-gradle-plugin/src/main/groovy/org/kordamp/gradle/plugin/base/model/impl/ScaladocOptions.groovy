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
package org.kordamp.gradle.plugin.base.model.impl

import groovy.transform.CompileStatic
import org.gradle.api.tasks.scala.ScalaDoc
import org.kordamp.gradle.util.CollectionUtils

/**
 * @author Andres Almiray
 * @since 0.15.0
 */
@CompileStatic
class ScaladocOptions {
    String bottom
    String top
    String windowTitle
    String docTitle
    String header
    String footer
    List<String> additionalParameters = new ArrayList<>()

    boolean deprecation
    boolean unchecked

    private boolean deprecationSet
    private boolean uncheckedSet

    boolean isIncluncheckedPrivateSet() {
        return incluncheckedPrivateSet
    }

    boolean isUncheckedSet() {
        return uncheckedSet
    }

    void setUnchecked(boolean unchecked) {
        this.unchecked = unchecked
        uncheckedSet = true
    }


    void setDeprecation(boolean deprecation) {
        this.deprecation = deprecation
        deprecationSet = true
    }

    static void merge(ScaladocOptions o1, ScaladocOptions o2) {
        o1.setDeprecation((boolean) (o1.deprecationSet ? o1.deprecation : o2.deprecation))
        o1.setUnchecked((boolean) (o1.uncheckedSet ? o1.unchecked : o2.unchecked))
        o1.setBottom(o1.bottom ?: o2.bottom)
        o1.setTop(o1.top ?: o2.top)
        o1.setWindowTitle(o1.windowTitle ?: o2.windowTitle)
        o1.setDocTitle(o1.docTitle ?: o2.docTitle)
        o1.setHeader(o1.header ?: o2.header)
        o1.setFooter(o1.footer ?: o2.footer)
        o1.additionalParameters = CollectionUtils.merge(o1.additionalParameters, o2?.additionalParameters, false)
    }

    void applyTo(ScalaDoc scaladoc) {
        scaladoc.scalaDocOptions.setDeprecation(deprecation)
        scaladoc.scalaDocOptions.setUnchecked(unchecked)
        scaladoc.scalaDocOptions.setBottom(bottom)
        scaladoc.scalaDocOptions.setTop(top)
        //scaladoc.scalaDocOptions.setWindowTitle(windowTitle)
        scaladoc.scalaDocOptions.setDocTitle(docTitle)
        //scaladoc.scalaDocOptions.setHeader(header)
        //scaladoc.scalaDocOptions.setFooter(footer)
        scaladoc.scalaDocOptions.setAdditionalParameters(additionalParameters)
    }
}
