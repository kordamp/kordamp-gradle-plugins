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
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.CompileStatic

import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.14.0
 */
@CompileStatic
class Integration {
    static final String PLUGIN_ID = 'org.kordamp.gradle.integration-test'

    boolean logging = true
    boolean aggregate = true
    boolean includeTestOutput = false
    String baseDir

    private boolean loggingSet = false
    private boolean aggregateSet = false
    private boolean includeTestOutputSet = false

    protected final Testing test

    Integration(Testing test) {
        this.test = test
    }

    void setLogging(boolean logging) {
        this.logging = logging
        this.loggingSet = true
    }

    boolean isLoggingSet() {
        this.loggingSet
    }

    void setAggregate(boolean aggregate) {
        this.aggregate = aggregate
        this.aggregateSet = true
    }

    boolean isAggregateSet() {
        this.aggregateSet
    }

    void setIncludeTestOutput(boolean includeTestOutput) {
        this.includeTestOutput = includeTestOutput
        this.includeTestOutputSet = true
    }

    boolean isIncludeTestOutputSet() {
        this.includeTestOutputSet
    }

    Map<String, Object> toMap() {
        new LinkedHashMap<String, Object>([
            logging          : logging,
            aggregate        : aggregate,
            includeTestOutput: includeTestOutput,
            baseDir          : baseDir
        ])
    }

    static void merge(Integration o1, Integration o2) {
        o1.setLogging((boolean) (o1.loggingSet ? o1.logging : (o2.loggingSet ? o2.logging : (o1.test.loggingSet ? o1.test.logging : o2.test.logging))))
        o1.setAggregate((boolean) (o1.aggregateSet ? o1.aggregate : (o2.aggregateSet ? o2.aggregate : (o1.test.aggregateSet ? o1.test.aggregate : o2.test.aggregate))))
        o1.setIncludeTestOutput((boolean) (o1.includeTestOutputSet ? o1.includeTestOutput : o2.includeTestOutput))
        o1.setBaseDir(isNotBlank(o1.baseDir) ? o1.baseDir : o2.baseDir)
    }

    void postMerge() {
        baseDir = baseDir ?: 'src' + File.separator + 'integration-test'
    }
}
