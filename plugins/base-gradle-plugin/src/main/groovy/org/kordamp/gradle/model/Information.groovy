/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 the original author or authors.
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
package org.kordamp.gradle.model

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
@Canonical
@EqualsAndHashCode(includes = ['specification', 'implementation'])
@ToString(excludes = ['specification', 'implementation'], includeNames = true)
class Information {
    String name
    String description
    String inceptionYear
    String vendor
    List<String> tags = []

    final Project project
    final LicenseSet licenses = new LicenseSet()
    final PersonSet people = new PersonSet()
    final Organization organization = new Organization()
    final Links links = new Links()
    final CredentialsSet credentials = new CredentialsSet()
    final Bintray bintray
    final Jacoco jacoco
    final Javadoc javadoc
    final Groovydoc groovydoc

    final SpecOrImpl specification = new SpecOrImpl()
    final SpecOrImpl implementation = new SpecOrImpl()

    private SpecOrImpl spec = new SpecOrImpl()
    private SpecOrImpl impl = new SpecOrImpl()

    Information(Project project) {
        this.project = project
        this.bintray = new Bintray(project)
        this.jacoco = new Jacoco(project)
        this.javadoc = new Javadoc(project)
        this.groovydoc = new Groovydoc(project)
    }

    Information copyOf() {
        Information copy = new Information(project)
        copy.name = name
        copy.description = description
        copy.inceptionYear = inceptionYear
        copy.vendor = vendor
        copy.tags.addAll(tags)
        copy.spec = spec.copyOf()
        copy.impl = impl.copyOf()
        organization.copyInto(copy.organization)
        licenses.copyInto(copy.licenses)
        people.copyInto(copy.people)
        links.copyInto(copy.links)
        credentials.copyInto(copy.credentials)
        bintray.copyInto(copy.bintray)
        jacoco.copyInto(copy.jacoco)
        javadoc.copyInto(copy.javadoc)
        groovydoc.copyInto(copy.groovydoc)

        copy.normalize()
    }

    Information merge(Information other) {
        Information copy = new Information(project)
        copy.name = name ?: other.name
        copy.description = description ?: other.description
        copy.inceptionYear = inceptionYear ?: other.inceptionYear
        copy.vendor = vendor ?: other.vendor
        copy.tags.addAll((tags + other.tags).unique())
        copy.spec = spec.merge(other.spec)
        copy.impl = impl.merge(other.impl)
        copy.organization.merge(organization, other.organization)
        copy.licenses.merge(licenses, other.licenses)
        copy.people.merge(people, other.people)
        copy.links.merge(links, other.links)
        copy.credentials.merge(credentials, other.credentials)
        copy.bintray.merge(bintray, other.bintray)
        copy.jacoco.merge(jacoco, other.jacoco)
        copy.javadoc.merge(javadoc, other.javadoc)
        copy.groovydoc.merge(groovydoc, other.groovydoc)

        copy.normalize()
    }

    List<String> validate() {
        List<String> errors = []

        if (isBlank(description)) {
            errors << "[${project.name}] Project description is blank".toString()
        }
        if (isBlank(vendor)) {
            errors << "[${project.name}] Project vendor is blank".toString()
        }
        errors.addAll(licenses.validate(project))
        errors.addAll(links.validate(project))
        errors.addAll(bintray.validate(project))

        errors
    }

    void licenses(Action<? super LicenseSet> action) {
        action.execute(licenses)
    }

    void people(Action<? super PersonSet> action) {
        action.execute(people)
    }

    void organization(Action<? super Organization> action) {
        action.execute(organization)
    }

    void bintray(Action<? super Bintray> action) {
        action.execute(bintray)
    }

    void jacoco(Action<? super Jacoco> action) {
        action.execute(jacoco)
    }

    void links(Action<? super Links> action) {
        action.execute(links)
    }

    void credentials(Action<? super CredentialsSet> action) {
        action.execute(credentials)
    }

    void specification(Action<? super SpecOrImpl> action) {
        action.execute(spec)
    }

    void implementation(Action<? super SpecOrImpl> action) {
        action.execute(impl)
    }

    void javadoc(Action<? super Javadoc> action) {
        action.execute(javadoc)
    }

    void groovydoc(Action<? super Groovydoc> action) {
        action.execute(groovydoc)
    }

    void licenses(@DelegatesTo(LicenseSet) Closure action) {
        ConfigureUtil.configure(action, licenses)
    }

    void people(@DelegatesTo(PersonSet) Closure action) {
        ConfigureUtil.configure(action, people)
    }

    void organization(@DelegatesTo(Organization) Closure action) {
        ConfigureUtil.configure(action, organization)
    }

    void bintray(@DelegatesTo(Bintray) Closure action) {
        ConfigureUtil.configure(action, bintray)
    }

    void jacoco(@DelegatesTo(Jacoco) Closure action) {
        ConfigureUtil.configure(action, jacoco)
    }

    void links(@DelegatesTo(Links) Closure action) {
        ConfigureUtil.configure(action, links)
    }

    void credentials(@DelegatesTo(CredentialsSet) Closure action) {
        ConfigureUtil.configure(action, credentials)
    }

    void specification(@DelegatesTo(SpecOrImpl) Closure action) {
        ConfigureUtil.configure(action, spec)
    }

    void implementation(@DelegatesTo(SpecOrImpl) Closure action) {
        ConfigureUtil.configure(action, impl)
    }

    void javadoc(@DelegatesTo(Javadoc) Closure action) {
        ConfigureUtil.configure(action, javadoc)
    }

    void groovydoc(@DelegatesTo(Groovydoc) Closure action) {
        ConfigureUtil.configure(action, groovydoc)
    }

    String getName() {
        name ?: project.name
    }

    String getVendor() {
        vendor ?: organization?.name
    }

    String getUrl() {
        links.website ?: organization.url
    }

    String getInceptionYear() {
        inceptionYear ?: currentYear()
    }

    Information normalize() {
        specification.title = spec.title ?: project.name
        specification.version = spec.version ?: project.version
        specification.vendor = spec.vendor ?: getVendor()

        implementation.title = impl.title ?: project.name
        implementation.version = impl.version ?: project.version
        implementation.vendor = impl.vendor ?: getVendor()

        this
    }

    List<String> resolveAuthors() {
        List<String> authors = []

        people.forEach { Person person ->
            if ('author' in person.roles*.toLowerCase()) {
                String author = person.name ?: person.id
                if (author) authors << author
            }
        }

        if (!authors && !people.isEmpty()) {
            Person person = people.people[0]
            authors << person.name ?: person.id
        }

        authors
    }

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
