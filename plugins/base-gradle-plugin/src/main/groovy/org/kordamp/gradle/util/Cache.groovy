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

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import org.gradle.api.invocation.Gradle

import java.util.function.Consumer

/**
 * Provides a local file cache.
 *
 * @author Andres Almiray
 * @since 0.41.0
 */
@CompileStatic
final class Cache {
    private static final String KEY_CACHE_LOG_LEVEL = 'org.kordamp.gradle.cache.log.level'

    @Canonical
    static class Key {
        final String value

        private Key(Object input) {
            this.value = ChecksumUtils.checksum(Algorithm.SHA_256, String.valueOf(input).bytes)
        }

        String getAbsolutePath(Gradle gradle) {
            Cache.getInstance().resolveCacheFile(gradle, this).absolutePath
        }
    }

    private static final Cache INSTANCE = new Cache()

    static Cache getInstance() {
        INSTANCE
    }

    /**
     * Creates a {@code Key} instance based on the given input
     * @param input the key's contents
     */
    static Key key(Object input) {
        if (!input) {
            throw new IllegalArgumentException('Input must not be null')
        }
        new Key(input)
    }

    /**
     * Returns the last modification timestamp of the given {@code Key}.
     * Returns {@code Long.MIN_VALUE} if a matching key is not found.
     *
     * @param gradle the {@code Gradle} instance that owns the cache
     * @param key the key that identifies the cache entry
     */
    long lastModified(Gradle gradle, Key key) {
        File file = resolveCacheFile(gradle, key)
        file.exists() ? file.lastModified() : Long.MIN_VALUE
    }

    /**
     * Updates the last modification timestamp of the given {@code Key}.
     * Returns {@code Long.MIN_VALUE} if a matching key is not found.
     *
     * @param gradle the {@code Gradle} instance that owns the cache
     * @param key the key that identifies the cache entry
     */
    long touch(Gradle gradle, Key key) {
        File file = resolveCacheFile(gradle, key)
        if (file.exists()) {
            file.setLastModified(new Date().time)
            return file.lastModified()
        }
        return Long.MIN_VALUE
    }

    /**
     * Queries the existence of a key in the cache.
     * @param gradle the {@code Gradle} instance that owns the cache
     * @param key the key to be searched
     * @return {@code true} if the key is found, {@code false} otherwise.
     */
    boolean has(Gradle gradle, Key key) {
        resolveCacheFile(gradle, key).exists()
    }

    /**
     * Deletes an entry from the cache.
     * @param gradle the {@code Gradle} instance that owns the cache
     * @param key the key that identifies the cache entry
     */
    void delete(Gradle gradle, Key key) {
        resolveCacheFile(gradle, key).delete()
    }

    /**
     * Reads from the cache using an {@code java.io.InputStream}.
     * @param gradle the {@code Gradle} instance that owns the cache
     * @param key the key that identifies the cache entry
     * @param consumer the function that reads from the cache
     * @return {@code true} if the read was successful, {@code false} otherwise.
     */
    boolean get(Gradle gradle, Key key, Consumer<? super InputStream> consumer) {
        File cacheFile = resolveCacheFile(gradle, key)
        if (cacheFile.exists()) {
            try {
                cacheFile.withInputStream { consumer.accept(it) }
                return true
            } catch (IOException e) {
                log(gradle, key, e)
            }
        }
        false
    }

    /**
     * Reads from the cache using a {@code java.io.BufferedReader}.
     * @param gradle the {@code Gradle} instance that owns the cache
     * @param key the key that identifies the cache entry
     * @param consumer the function that reads from the cache
     * @return {@code true} if the read was successful, {@code false} otherwise.
     */
    boolean read(Gradle gradle, Key key, Consumer<? super BufferedReader> consumer) {
        File cacheFile = resolveCacheFile(gradle, key)
        if (cacheFile.exists()) {
            try {
                cacheFile.withReader { consumer.accept(it) }
                return true
            } catch (IOException e) {
                log(gradle, key, e)
            }
        }
        false
    }

    /**
     * Writes to the cache using an {@code java.io.OutputStream}.
     * @param gradle the {@code Gradle} instance that owns the cache
     * @param key the key that identifies the cache entry
     * @param consumer the function that writes to the cache
     * @return {@code true} if the write was successful, {@code false} otherwise.
     */
    boolean put(Gradle gradle, Key key, Consumer<? super OutputStream> consumer) {
        File cacheFile = resolveCacheFile(gradle, key)
        cacheFile.parentFile.mkdirs()
        try {
            cacheFile.withOutputStream { consumer.accept(it) }
            return true
        } catch (IOException e) {
            log(gradle, key, e)
        }
        false
    }

    /**
     * Writes to the cache using a {@code java.io.BufferedWriter}.
     * @param gradle the {@code Gradle} instance that owns the cache
     * @param key the key that identifies the cache entry
     * @param consumer the function that writes to the cache
     * @return {@code true} if the write was successful, {@code false} otherwise.
     */
    boolean write(Gradle gradle, Key key, Consumer<? super BufferedWriter> consumer) {
        File cacheFile = resolveCacheFile(gradle, key)
        cacheFile.parentFile.mkdirs()
        try {
            cacheFile.withWriter { consumer.accept(it) }
            return true
        } catch (IOException e) {
            log(gradle, key, e)
        }
        false
    }

    /**
     * Deletes the whole cache.
     * @param gradle the {@code Gradle} instance that owns the cache
     */
    void clear(Gradle gradle) {
        new File([gradle.gradleUserHomeDir.absolutePath,
                  'caches',
                  'kordamp',
                  'file-cache'].join(File.separator))
            .deleteDir()
    }

    private File resolveCacheFile(Gradle gradle, Key key) {
        new File([gradle.gradleUserHomeDir.absolutePath,
                  'caches',
                  'kordamp',
                  'file-cache',
                  key.value].join(File.separator))
    }

    private void log(Gradle gradle, Key key, Throwable t) {
        try {
            String message = "Cache I/O error key=${key.value}".toString()
            switch (resolveLogLevel()) {
                case LogLevel.DEBUG:
                    gradle.rootProject.logger.debug(message, t)
                    break
                case LogLevel.INFO:
                    gradle.rootProject.logger.info(message, t)
                    break
                case LogLevel.WARN:
                    gradle.rootProject.logger.warn(message, t)
                    break
                case LogLevel.ERROR:
                    gradle.rootProject.logger.error(message, t)
                    break
            }
        } catch (IllegalStateException e) {
            // root project is not available
            System.err.println("Cache I/O error key=${key.value}. ${t}")
        }
    }

    enum LogLevel {
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    static LogLevel resolveLogLevel() {
        String value = System.getProperty(KEY_CACHE_LOG_LEVEL)
        try {
            return LogLevel.valueOf(value.toUpperCase())
        } catch (Exception ignored) {
            return LogLevel.INFO
        }
    }
}
