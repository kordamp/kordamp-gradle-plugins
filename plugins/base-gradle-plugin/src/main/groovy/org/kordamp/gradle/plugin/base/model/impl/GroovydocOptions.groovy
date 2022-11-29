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
import org.gradle.api.resources.TextResource
import org.gradle.api.tasks.javadoc.Groovydoc
import org.kordamp.gradle.util.CollectionUtils

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
@CompileStatic
class GroovydocOptions {
    String windowTitle
    String docTitle
    String header
    String footer
    TextResource overviewText
    Set<Groovydoc.Link> links = new LinkedHashSet<>()

    boolean noTimestamp
    boolean noVersionStamp
    boolean includePrivate
    boolean use

    private boolean noTimestampSet
    private boolean noVersionStampSet
    private boolean includePrivateSet
    private boolean useSet

    boolean isNoTimestampSet() {
        return noTimestampSet
    }

    boolean isNoVersionStampSet() {
        return noVersionStampSet
    }

    boolean isIncludePrivateSet() {
        return includePrivateSet
    }

    boolean isUseSet() {
        return useSet
    }

    void setUse(boolean use) {
        this.use = use
        useSet = true
    }

    void setNoTimestamp(boolean noTimestamp) {
        this.noTimestamp = noTimestamp
        noTimestampSet = true
    }

    void setNoVersionStamp(boolean noVersionStamp) {
        this.noVersionStamp = noVersionStamp
        noVersionStampSet = true
    }

    void setIncludePrivate(boolean includePrivate) {
        this.includePrivate = includePrivate
        includePrivateSet = true
    }

    void link(String url, String... packages) {
        links.add(new Groovydoc.Link(url, packages))
    }

    void cleanupLinks() {
        Collection<String> urls = links.collect { it.url }.unique()
        List<Groovydoc.Link> copy = []
        copy.addAll(links)
        for (String url : urls) {
            List<Groovydoc.Link> list = copy.findAll { it.url == url }
            list.remove(0)
            links.removeAll(list)
        }
    }

    void linkIfAbsent(String url, String... packages) {
        Groovydoc.Link link = new Groovydoc.Link(url, packages)
        if (links.find { it.url == link.url }) {
            return
        }
        links.add(link)
    }

    static void merge(GroovydocOptions o1, GroovydocOptions o2) {
        o1.setNoTimestamp((boolean) (o1.noTimestampSet ? o1.noTimestamp : o2.noTimestamp))
        o1.setNoVersionStamp((boolean) (o1.noVersionStampSet ? o1.noVersionStamp : o2.noVersionStamp))
        o1.setIncludePrivate((boolean) (o1.includePrivateSet ? o1.includePrivate : o2.includePrivate))
        o1.setUse((boolean) (o1.useSet ? o1.use : o2.use))
        o1.setWindowTitle(o1.windowTitle ?: o2.windowTitle)
        o1.setDocTitle(o1.docTitle ?: o2.docTitle)
        o1.setHeader(o1.header ?: o2.header)
        o1.setFooter(o1.footer ?: o2.footer)
        o1.setOverviewText(o1.overviewText ?: o2.overviewText)
        o1.links = CollectionUtils.merge(o1.links, o2?.links, false)
    }

    void applyTo(Groovydoc groovydoc) {
        groovydoc.setNoTimestamp(noTimestamp)
        groovydoc.setNoVersionStamp(noVersionStamp)
        groovydoc.setIncludePrivate(includePrivate)
        groovydoc.setUse(use)
        groovydoc.setWindowTitle(windowTitle)
        groovydoc.setDocTitle(docTitle)
        groovydoc.setHeader(header)
        groovydoc.setFooter(footer)
        groovydoc.setOverviewText(overviewText)
        groovydoc.setLinks(links)
    }
}
