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
package org.kordamp.gradle.plugin.base.model.mutable

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.plugin.base.model.Information

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
@EqualsAndHashCode(includes = ['project', 'specification', 'implementation'])
@ToString(excludes = ['project', 'specification', 'implementation'], includeNames = true)
class MutableInformation implements Information {
    String name
    String description
    String inceptionYear
    String vendor
    List<String> tags = []

    final Project project
    final MutablePersonSet people = new MutablePersonSet()
    final MutableOrganization organization = new MutableOrganization()
    final MutableLinks links = new MutableLinks()
    final MutableScm scm = new MutableScm()
    final MutableCredentialsSet credentials = new MutableCredentialsSet()

    final MutableSpecification specification = new MutableSpecification()
    final MutableImplementation implementation = new MutableImplementation()

    private MutableSpecification spec = new MutableSpecification()
    private MutableImplementation impl = new MutableImplementation()

    MutableInformation(Project project) {
        this.project = project
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @CompileDynamic
    Map<String, Map<String, Object>> toMap() {
        ['info': [
            name: getName(),
            description: description,
            url: url,
            inceptionYear: getInceptionYear(),
            copyrightYear: copyrightYear,
            vendor: getVendor(),
            authors: authors,
            organization: organization.toMap(),
            people: people.toMap(),
            links: links.toMap(),
            scm: scm.toMap(),
            specification: specification.toMap(),
            implementation: implementation.toMap(),
            credentials: credentials.toMap()
        ]]
    }

    MutableInformation copyOf() {
        MutableInformation copy = new MutableInformation(project)
        copyInto(copy)
        copy
    }

    void copyInto(MutableInformation copy) {
        copy.@name = this.@name
        copy.description = description
        copy.@inceptionYear = this.@inceptionYear
        copy.@vendor = this.@vendor
        copy.tags.addAll(tags)
        copy.spec = spec.copyOf()
        copy.impl = impl.copyOf()
        organization.copyInto(copy.organization)
        people.copyInto(copy.people)
        links.copyInto(copy.links)
        scm.copyInto(copy.scm)
        credentials.copyInto(copy.credentials)

        copy.normalize()
    }

    static void merge(MutableInformation o1, MutableInformation o2) {
        o2.normalize()
        o1.name = o1.@name ?: o2.name
        o1.description = o1.description ?: o2.description
        o1.inceptionYear = o1.@inceptionYear ?: o2.inceptionYear
        o1.vendor = o1.@vendor ?: o2.vendor
        o1.tags.addAll((o1.tags + o2.tags).unique())
        MutableSpecification.merge(o1.spec, o2.spec)
        MutableImplementation.merge(o1.impl, o2.impl)
        MutableOrganization.merge(o1.organization, o2.organization)
        MutablePersonSet.merge(o1.people, o2.people)
        MutableLinks.merge(o1.links, o2.links)
        MutableScm.merge(o1.scm, o2.scm)
        MutableCredentialsSet.merge(o1.credentials, o2.credentials)
        o1.normalize()
    }

    List<String> validate() {
        List<String> errors = []

        if (isBlank(description)) {
            errors << "[${project.name}] Project description is blank".toString()
        }
        if (isBlank(vendor)) {
            errors << "[${project.name}] Project vendor is blank".toString()
        }

        errors
    }

    void people(Action<? super MutablePersonSet> action) {
        action.execute(people)
    }

    void organization(Action<? super MutableOrganization> action) {
        action.execute(organization)
    }

    void links(Action<? super MutableLinks> action) {
        action.execute(links)
    }

    void scm(Action<? super MutableScm> action) {
        action.execute(scm)
    }

    void credentials(Action<? super MutableCredentialsSet> action) {
        action.execute(credentials)
    }

    void specification(Action<? super MutableSpecification> action) {
        action.execute(spec)
    }

    void implementation(Action<? super MutableImplementation> action) {
        action.execute(impl)
    }

    void people(@DelegatesTo(MutablePersonSet) Closure action) {
        ConfigureUtil.configure(action, people)
    }

    void organization(@DelegatesTo(MutableOrganization) Closure action) {
        ConfigureUtil.configure(action, organization)
    }

    void links(@DelegatesTo(MutableLinks) Closure action) {
        ConfigureUtil.configure(action, links)
    }

    void scm(@DelegatesTo(MutableScm) Closure action) {
        ConfigureUtil.configure(action, scm)
    }

    void credentials(@DelegatesTo(MutableCredentialsSet) Closure action) {
        ConfigureUtil.configure(action, credentials)
    }

    void specification(@DelegatesTo(MutableSpecification) Closure action) {
        ConfigureUtil.configure(action, spec)
    }

    void implementation(@DelegatesTo(MutableSpecification) Closure action) {
        ConfigureUtil.configure(action, impl)
    }

    @Override
    String getName() {
        name ?: project.name
    }

    @Override
    String getVendor() {
        vendor ?: organization?.name
    }

    @Override
    String getUrl() {
        links.website ?: organization.url
    }

    @Override
    String getInceptionYear() {
        inceptionYear ?: currentYear()
    }

    MutableInformation normalize() {
        if (spec.isEnabledSet()) specification.setEnabled(spec.enabled)
        specification.title = spec.title ?: project.name
        specification.version = spec.version ?: project.version
        specification.vendor = spec.vendor ?: getVendor()

        if (impl.isEnabledSet()) implementation.setEnabled(impl.enabled)
        implementation.title = impl.title ?: project.name
        implementation.version = impl.version ?: project.version
        implementation.vendor = impl.vendor ?: getVendor()

        this
    }

    @Override
    List<String> getAuthors() {
        List<String> authors = []

        people.forEach { MutablePerson person ->
            if ('author' in person.roles*.toLowerCase()) {
                String author = person.name ?: person.id
                if (author) authors << author
            }
        }

        if (!authors && !people.isEmpty()) {
            MutablePerson person = people.people[0]
            authors << person.name ?: person.id
        }

        authors
    }

    @Override
    String getCopyrightYear() {
        String initialYear = getInceptionYear()
        String currentYear = currentYear()
        String year = initialYear
        if (initialYear != currentYear) {
            year += '-' + currentYear
        }
        year
    }

    static String currentYear() {
        Date now = new Date()
        Calendar c = Calendar.getInstance()
        c.setTime(now)
        return c.get(Calendar.YEAR).toString()
    }
}
