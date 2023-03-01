/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2023 Andres Almiray.
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

import groovy.transform.CompileStatic

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * @author Andres Almiray
 * @since 0.46.0
 */
@CompileStatic
class ChecksumUtils {
    private static final char[] DIGITS = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'] as char[]

    private ChecksumUtils() {
        // prevent instantiation
    }

    static String sha256(Path path) {
        try {
            MessageDigest digest = MessageDigest.getInstance('SHA-256')
            return encodeHex(digest.digest(Files.readAllBytes(path)));
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new IllegalStateException('Unexpected error', e)
        }
    }

    static String sha256(String str) {
        try {
            MessageDigest digest = MessageDigest.getInstance('SHA-256')
            return encodeHex(digest.digest(str.getBytes(StandardCharsets.UTF_8)))
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new IllegalStateException('Unexpected error', e)
        }
    }

    private static String encodeHex(byte[] data) {
        char[] out = new char[data.length << 1]
        int i = 0
        int j = 0
        while (i < data.length) {
            out[j++] = DIGITS[(0xF0 & data[i]) >>> 4]
            out[j++] = DIGITS[0x0F & data[i]]
            i++
        }
        return new String(out)
    }
}
