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
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
interface Information {
    Property<String> getName()

    Property<String> getDescription()

    Property<String> getInceptionYear()

    Property<String> getVendor()

    SetProperty<String> getTags()

    void people(Action<? super PersonSet> action)

    void people(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PersonSet) Closure<Void> action)

    void repositories(Action<? super RepositorySet> action)

    void repositories(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = RepositorySet) Closure<Void> action)

    void organization(Action<? super Organization> action)

    void organization(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Organization) Closure<Void> action)

    void links(Action<? super Links> action)

    void links(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Links) Closure<Void> action)

    void scm(Action<? super Scm> action)

    void scm(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Scm) Closure<Void> action)

    void issueManagement(Action<? super IssueManagement> action)

    void issueManagement(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = IssueManagement) Closure<Void> action)

    void ciManagement(Action<? super CiManagement> action)

    void ciManagement(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CiManagement) Closure<Void> action)

    void mailingLists(Action<? super MailingListSet> action)

    void mailingLists(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = MailingListSet) Closure<Void> action)

    void credentials(Action<? super CredentialsSet> action)

    void credentials(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CredentialsSet) Closure<Void> action)

    void specification(Action<? super Specification> action)

    void specification(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Specification) Closure<Void> action)

    void implementation(Action<? super Implementation> action)

    void implementation(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Specification) Closure<Void> action)
}
