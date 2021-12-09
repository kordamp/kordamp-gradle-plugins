/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2021 Andres Almiray.
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
package org.kordamp.gradle.plugin.base.extensions;

import org.gradle.api.Named;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;


public interface Notifier extends Named {

    default String getId() { return getName(); }

    Property<String> getType();
    Property<Boolean> getSendOnError();
    Property<Boolean> getSendOnFailure();
    Property<Boolean> getSendOnSuccess();
    Property<Boolean> getSendOnWarning();
    MapProperty<String, String> getConfiguration();

    // TODO: merge

}
