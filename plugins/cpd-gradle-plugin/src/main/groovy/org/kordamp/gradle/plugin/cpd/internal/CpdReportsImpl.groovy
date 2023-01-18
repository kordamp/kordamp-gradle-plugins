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
package org.kordamp.gradle.plugin.cpd.internal

import groovy.transform.CompileStatic
import org.gradle.api.Task
import org.gradle.api.internal.CollectionCallbackActionDecorator
import org.gradle.api.reporting.SingleFileReport
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport
import org.gradle.api.reporting.internal.TaskReportContainer
import org.kordamp.gradle.plugin.cpd.CpdReports

import javax.inject.Inject

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
class CpdReportsImpl extends TaskReportContainer<SingleFileReport> implements CpdReports {
    @Inject
    CpdReportsImpl(Task task, CollectionCallbackActionDecorator callbackActionDecorator) {
        super(SingleFileReport, task, callbackActionDecorator)

        add(TaskGeneratedSingleFileReport, 'html', task)
        add(TaskGeneratedSingleFileReport, 'xml', task)
    }

    @Override
    SingleFileReport getHtml() {
        getByName('html')
    }

    @Override
    SingleFileReport getXml() {
        getByName('xml')
    }
}