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
package org.kordamp.gradle.util

import static org.kordamp.gradle.util.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.47.0
 */
enum Algorithm {
    MD2,
    MD5,
    SHA_1,
    SHA_256,
    SHA_384,
    SHA_512,
    SHA3_224,
    SHA3_256,
    SHA3_384,
    SHA3_512

    String formatted() {
        if (name().startsWith('SHA3')) {
            return name().toLowerCase().replace('_', '-')
        }
        return name().toLowerCase().replace('_', '')
    }

    static Algorithm of(String str) {
        if (isBlank(str)) return null
        return Algorithm.valueOf(str.toUpperCase().trim())
    }
}
