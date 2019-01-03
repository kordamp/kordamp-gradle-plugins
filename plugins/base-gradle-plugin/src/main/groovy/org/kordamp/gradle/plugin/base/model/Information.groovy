/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
package org.kordamp.gradle.plugin.base.model

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
@EqualsAndHashCode(includes = ['project', 'specification', 'implementation'])
@ToString(excludes = ['project', 'specification', 'implementation'], includeNames = true)
class Information {
    String name
    String description
    String inceptionYear
    String vendor
    List<String> tags = []

    final Project project
    final PersonSet people = new PersonSet()
    final RepositorySet repositories = new RepositorySet()
    final Organization organization = new Organization()
    final Links links = new Links()
    final Scm scm = new Scm()
    final CredentialsSet credentials = new CredentialsSet()

    final Specification specification = new Specification()
    final Implementation implementation = new Implementation()

    private Specification spec = new Specification()
    private Implementation impl = new Implementation()

    Information(Project project) {
        this.project = project
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @CompileDynamic
    Map<String, Map<String, Object>> toMap() {
        ['info': [
            name          : getName(),
            description   : description,
            url           : url,
            inceptionYear : getInceptionYear(),
            copyrightYear : copyrightYear,
            vendor        : getVendor(),
            authors       : authors,
            organization  : organization.toMap(),
            people        : people.toMap(),
            repositories  : repositories.toMap(),
            links         : links.toMap(),
            scm           : scm.toMap(),
            specification : specification.toMap(),
            implementation: implementation.toMap(),
            credentials   : credentials.toMap()
        ]]
    }

    Information copyOf() {
        Information copy = new Information(project)
        copyInto(copy)
        copy
    }

    void copyInto(Information copy) {
        copy.@name = this.@name
        copy.description = description
        copy.@inceptionYear = this.@inceptionYear
        copy.@vendor = this.@vendor
        copy.tags.addAll(tags)
        copy.spec = spec.copyOf()
        copy.impl = impl.copyOf()
        organization.copyInto(copy.organization)
        people.copyInto(copy.people)
        repositories.copyInto(copy.repositories)
        links.copyInto(copy.links)
        scm.copyInto(copy.scm)
        credentials.copyInto(copy.credentials)

        copy.normalize()
    }

    static void merge(Information o1, Information o2) {
        o2.normalize()
        o1.name = o1.@name ?: o2.name
        o1.description = o1.description ?: o2.description
        o1.inceptionYear = o1.@inceptionYear ?: o2.inceptionYear
        o1.vendor = o1.@vendor ?: o2.vendor
        o1.tags.addAll((o1.tags + o2.tags).unique())
        Specification.merge(o1.spec, o2.spec)
        Implementation.merge(o1.impl, o2.impl)
        Organization.merge(o1.organization, o2.organization)
        PersonSet.merge(o1.people, o2.people)
        RepositorySet.merge(o1.repositories, o2.repositories)
        Links.merge(o1.links, o2.links)
        Scm.merge(o1.scm, o2.scm)
        CredentialsSet.merge(o1.credentials, o2.credentials)
        o1.normalize()
    }

    List<String> validate(ProjectConfigurationExtension extension) {
        List<String> errors = []

        if (isBlank(description) &&
            (extension.publishing.enabled || extension.bintray.enabled)) {
            errors << "[${project.name}] Project description is blank".toString()
        }
        if (isBlank(vendor) &&
            (extension.publishing.enabled || extension.bintray.enabled)) {
            errors << "[${project.name}] Project vendor is blank".toString()
        }
        if (isBlank(organization.url) && isBlank(links.website) &&
            (extension.publishing.enabled || extension.bintray.enabled)) {
            errors << "[${project.name}] Project organization.url is blank".toString()
        }
        if (isBlank(scm.url) && isBlank(links.scm) &&
            (extension.publishing.enabled || extension.bintray.enabled)) {
            errors << "[${project.name}] Project scm.url is blank".toString()
        }

        errors
    }

    void people(Action<? super PersonSet> action) {
        action.execute(people)
    }

    void repositories(Action<? super RepositorySet> action) {
        action.execute(repositories)
    }

    void organization(Action<? super Organization> action) {
        action.execute(organization)
    }

    void links(Action<? super Links> action) {
        action.execute(links)
    }

    void scm(Action<? super Scm> action) {
        action.execute(scm)
    }

    void credentials(Action<? super CredentialsSet> action) {
        action.execute(credentials)
    }

    void specification(Action<? super Specification> action) {
        action.execute(spec)
    }

    void implementation(Action<? super Implementation> action) {
        action.execute(impl)
    }

    void people(@DelegatesTo(PersonSet) Closure action) {
        ConfigureUtil.configure(action, people)
    }

    void repositories(@DelegatesTo(RepositorySet) Closure action) {
        ConfigureUtil.configure(action, repositories)
    }

    void organization(@DelegatesTo(Organization) Closure action) {
        ConfigureUtil.configure(action, organization)
    }

    void links(@DelegatesTo(Links) Closure action) {
        ConfigureUtil.configure(action, links)
    }

    void scm(@DelegatesTo(Scm) Closure action) {
        ConfigureUtil.configure(action, scm)
    }

    void credentials(@DelegatesTo(CredentialsSet) Closure action) {
        ConfigureUtil.configure(action, credentials)
    }

    void specification(@DelegatesTo(Specification) Closure action) {
        ConfigureUtil.configure(action, spec)
    }

    void implementation(@DelegatesTo(Specification) Closure action) {
        ConfigureUtil.configure(action, impl)
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

    List<String> getAuthors() {
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
