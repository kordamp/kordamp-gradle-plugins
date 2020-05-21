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
import org.kordamp.gradle.util.CollectionUtils

/**
 * @author Andres Almiray
 * @since 0.22.0
 */
@CompileStatic
class Notifier {
    String id
    String type
    boolean sendOnError
    boolean sendOnFailure
    boolean sendOnSuccess
    boolean sendOnWarning
    Map<String, String> configuration = [:]

    private boolean sendOnErrorSet
    private boolean sendOnFailureSet
    private boolean sendOnSuccessSet
    private boolean sendOnWarningSet

    Map<String, Object> toMap() {
        new LinkedHashMap<String, Object>([
            id           : id,
            type         : type,
            sendOnError  : sendOnError,
            sendOnFailure: sendOnFailure,
            sendOnSuccess: sendOnSuccess,
            sendOnWarning: sendOnWarning,
            configuration: configuration
        ])
    }

    static Notifier merge(Notifier o1, Notifier o2) {
        o1.id = o1.id ?: o2?.id
        o1.type = o1.type ?: o2?.type
        o1.setSendOnError((boolean) (o1.sendOnErrorSet ? o1.sendOnError : o2.sendOnError))
        o1.setSendOnFailure((boolean) (o1.sendOnFailureSet ? o1.sendOnFailure : o2.sendOnFailure))
        o1.setSendOnSuccess((boolean) (o1.sendOnSuccessSet ? o1.sendOnSuccess : o2.sendOnSuccess))
        o1.setSendOnWarning((boolean) (o1.sendOnWarningSet ? o1.sendOnWarning : o2.sendOnWarning))
        CollectionUtils.merge(o1.configuration, o2?.configuration)
        o1
    }
}
