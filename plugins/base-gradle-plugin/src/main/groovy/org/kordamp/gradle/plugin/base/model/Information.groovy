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
package org.kordamp.gradle.plugin.base.model

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.util.CollectionUtils
import org.kordamp.gradle.util.ConfigureUtil

import static org.kordamp.gradle.util.StringUtils.isBlank
import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
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
    final IssueManagement issueManagement = new IssueManagement()
    final CiManagement ciManagement = new CiManagement()
    final MailingListSet mailingLists = new MailingListSet()
    final CredentialsSet credentials = new CredentialsSet()

    final Specification specification = new Specification()
    final Implementation implementation = new Implementation()

    private Specification spec = new Specification()
    private Implementation impl = new Implementation()

    protected final ProjectConfigurationExtension config

    Information(ProjectConfigurationExtension config, Project project) {
        this.config = config
        this.project = project
    }

    Map<String, Map<String, Object>> toMap() {
        new LinkedHashMap<String, Map<String, Object>>(['info': new LinkedHashMap<String, Object>([
            name           : getName(),
            description    : description,
            url            : url,
            inceptionYear  : getInceptionYear(),
            copyrightYear  : copyrightYear,
            vendor         : getVendor(),
            authors        : authors,
            organization   : organization.toMap(),
            people         : people.toMap(),
            repositories   : repositories.toMap(),
            links          : links.toMap(),
            scm            : scm.toMap(),
            issueManagement: issueManagement.toMap(),
            ciManagement   : ciManagement.toMap(),
            mailingLists   : mailingLists.toMap(),
            specification  : specification.toMap(),
            implementation : implementation.toMap(),
            credentials    : credentials.toMap()
        ])])
    }

    static void merge(Information o1, Information o2) {
        o2.normalize()
        o1.name = o1.@name ?: o2.name
        o1.description = o1.description ?: o2.description
        o1.inceptionYear = o1.@inceptionYear ?: o2.inceptionYear
        o1.vendor = o1.@vendor ?: o2.vendor
        o1.tags = CollectionUtils.merge(o1.tags, o2.tags, false)
        Specification.merge(o1.spec, o2.spec)
        Implementation.merge(o1.impl, o2.impl)
        Organization.merge(o1.organization, o2.organization)
        PersonSet.merge(o1.people, o2.people)
        RepositorySet.merge(o1.repositories, o2.repositories)
        Links.merge(o1.links, o2.links)
        Scm.merge(o1.scm, o2.scm)
        IssueManagement.merge(o1.issueManagement, o2.issueManagement)
        CiManagement.merge(o1.ciManagement, o2.ciManagement)
        MailingListSet.merge(o1.mailingLists, o2.mailingLists)
        CredentialsSet.merge(o1.credentials, o2.credentials)
        o1.normalize()
    }

    List<String> validate(ProjectConfigurationExtension extension) {
        List<String> errors = []

        if (isBlank(description) &&
            (extension.publishing.enabled || extension.bintray.enabled)) {
            errors << "[${project.name}] Project description is blank".toString()
        }
        if (isBlank(getVendor()) &&
            (extension.publishing.enabled || extension.bintray.enabled)) {
            errors << "[${project.name}] Project vendor is blank".toString()
        }
        if (isBlank(getUrl()) &&
            (extension.publishing.enabled || extension.bintray.enabled)) {
            errors << "[${project.name}] Project organization.url is blank".toString()
        }
        if ((scm.enabled && isBlank(scm.url)) && (links.enabled && isBlank(links.scm)) &&
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

    void issueManagement(Action<? super IssueManagement> action) {
        action.execute(issueManagement)
    }

    void ciManagement(Action<? super CiManagement> action) {
        action.execute(ciManagement)
    }

    void mailingLists(Action<? super MailingListSet> action) {
        action.execute(mailingLists)
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

    void people(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PersonSet) Closure<Void> action) {
        ConfigureUtil.configure(action, people)
    }

    void repositories(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = RepositorySet) Closure<Void> action) {
        ConfigureUtil.configure(action, repositories)
    }

    void organization(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Organization) Closure<Void> action) {
        ConfigureUtil.configure(action, organization)
    }

    void links(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Links) Closure<Void> action) {
        ConfigureUtil.configure(action, links)
    }

    void scm(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Scm) Closure<Void> action) {
        ConfigureUtil.configure(action, scm)
    }

    void issueManagement(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = IssueManagement) Closure<Void> action) {
        ConfigureUtil.configure(action, issueManagement)
    }

    void ciManagement(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CiManagement) Closure<Void> action) {
        ConfigureUtil.configure(action, ciManagement)
    }

    void mailingLists(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = MailingListSet) Closure<Void> action) {
        ConfigureUtil.configure(action, mailingLists)
    }

    void credentials(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CredentialsSet) Closure<Void> action) {
        ConfigureUtil.configure(action, credentials)
    }

    void specification(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Specification) Closure<Void> action) {
        ConfigureUtil.configure(action, spec)
    }

    void implementation(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Specification) Closure<Void> action) {
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

    String resolveScmLink() {
        if (isNotBlank(scm.url)) {
            return scm.url
        }
        return links.scm
    }
}
