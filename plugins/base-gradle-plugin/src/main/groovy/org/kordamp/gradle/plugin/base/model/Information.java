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
package org.kordamp.gradle.plugin.base.model;

import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public interface Information {
    String getName();

    String getUrl();

    String getDescription();

    String getInceptionYear();

    String getVendor();

    List<String> getTags();

    PersonSet getPeople();

    Organization getOrganization();

    Links getLinks();

    CredentialsSet getCredentials();

    Specification getSpecification();

    Implementation getImplementation();

    List<String> getAuthors();

    String getCopyrightYear();
}
