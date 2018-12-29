/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Project

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
class Apidoc extends AbstractFeature {
    boolean replaceJavadoc = false

    private boolean replaceJavadocSet

    Apidoc(Project project) {
        super(project)
    }

    @Override
    String toString() {
        isRoot() ? toMap().toString() : ''
    }

    @Override
    @CompileDynamic
    Map<String, Map<String, Object>> toMap() {
        if (!isRoot()) return [:]

        Map map = [enabled: enabled]

        if (enabled) {
            map.replaceJavadoc = replaceJavadoc
        }

        ['apidoc': map]
    }

    void setReplaceJavadoc(boolean replaceJavadoc) {
        this.replaceJavadoc = replaceJavadoc
        this.replaceJavadocSet = true
    }

    boolean isReplaceJavadocSet() {
        this.replaceJavadocSet
    }

    void copyInto(Apidoc copy) {
        super.copyInto(copy)
        copy.@replaceJavadoc = replaceJavadoc
        copy.@replaceJavadocSet = replaceJavadocSet
    }

    static void merge(Apidoc o1, Apidoc o2) {
        AbstractFeature.merge(o1, o2)
        o1.setReplaceJavadoc((boolean) (o1.replaceJavadocSet ? o1.replaceJavadoc : o2.replaceJavadoc))
    }
}
