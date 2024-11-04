/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2024 Andres Almiray.
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
package org.kordamp.gradle.plugin.profiles.internal

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.kordamp.gradle.plugin.profiles.Activation

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.35.0
 */
@PackageScope
@CompileStatic
class ActivationFile implements Activation {
    final RegularFileProperty exists
    final RegularFileProperty missing

    @Inject
    ActivationFile(ObjectFactory objects) {
        exists = objects.fileProperty()
        missing = objects.fileProperty()
    }

    boolean isActive(Project project) {
        if (exists.present) {
            return exists.get().asFile.exists()
        } else if (missing.present) {
            return !missing.get().asFile.exists()
        } else {
            throw new IllegalStateException("No value for 'exists' nor 'missing' has been set.")
        }
    }
}
