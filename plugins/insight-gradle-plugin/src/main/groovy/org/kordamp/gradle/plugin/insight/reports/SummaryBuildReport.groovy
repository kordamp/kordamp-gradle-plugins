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
package org.kordamp.gradle.plugin.insight.reports

import groovy.transform.CompileStatic
import org.gradle.api.invocation.Gradle
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.kordamp.gradle.plugin.insight.InsightExtension
import org.kordamp.gradle.plugin.insight.internal.InsightExtensionImpl
import org.kordamp.gradle.plugin.insight.model.Build
import org.kordamp.gradle.plugin.insight.model.BuildReport
import org.kordamp.gradle.plugin.insight.model.Project
import org.kordamp.gradle.plugin.insight.model.Task
import org.kordamp.gradle.util.TimeUtils

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.40.0
 */
@CompileStatic
class SummaryBuildReport implements BuildReport {
    final Property<String> format
    final Property<Boolean> enabled
    final Property<Boolean> zeroPadding
    final Property<Integer> maxProjectPathSize
    final Property<Double> confTimeThreshold
    final Property<Double> execTimeThreshold
    final Property<Integer> displayProjectThreshold

    @Inject
    SummaryBuildReport(ObjectFactory objects) {
        this.format = objects.property(String).convention('short')
        this.enabled = objects.property(Boolean).convention(true)
        this.zeroPadding = objects.property(Boolean).convention(false)
        this.maxProjectPathSize = objects.property(Integer).convention(36)
        this.confTimeThreshold = objects.property(Double).convention(0.5d)
        this.execTimeThreshold = objects.property(Double).convention(120d)
        this.displayProjectThreshold = objects.property(Integer).convention(2)
    }

    @Override
    void report(Gradle gradle, InsightExtension extension, Build build) throws Exception {
        if (build.projects.values().every { it.state == Project.State.HIDDEN }) return

        List<Project> projects = []
        projects.addAll(build.projects.values())

        InsightExtensionImpl x = (InsightExtensionImpl) extension

        int paddingSize = Math.max(13, maxProjectPathSize.get())
        String padding = ' ' * paddingSize
        if (projects.size() >= 1) {
            padding = "${projects.size()} projects".padRight(paddingSize)
        }

        String header = '             CONF        EXEC   '
        if ('long'.equalsIgnoreCase(format.get())) {
            header += ' ' + ['TOT', 'EXE', 'FLD', 'SKP', 'UTD', 'WRK', 'CHD', 'NSR', 'ACT'].join(' ')
        }

        String separator = '-' * (padding.size() + header.size())
        println('\n' + separator)
        println(padding + header)
        println(separator)

        int[] totals = new int[9]
        Arrays.fill(totals, 0)

        int projectExecutedCount = 0
        for (Project project : projects) {
            if (project.state == Project.State.HIDDEN) continue

            projectExecutedCount++

            String confDuration = formatDuration(project.confDuration)
            String execDuration = formatDuration(project.execDuration)
            if (confTimeThreshold.get() < project.confDuration) {
                confDuration = x.colors.failure(confDuration)
            }
            if (execTimeThreshold.get() < project.execDuration) {
                execDuration = x.colors.failure(execDuration)
            }

            String row = (project.path + ' ').padRight(paddingSize, '.') +
                ' ' + x.colors.state(project.state) + ' ' +
                '[' + confDuration + '] ' +
                '[' + execDuration + ']'
            String details = ''

            if ('long'.equalsIgnoreCase(format.get())) {
                int[] values = new int[9]
                Arrays.fill(values, 0)

                for (Task task : project.tasks.values()) {
                    values[0]++
                    values[1] += task.isExecuted() ? 1 : 0
                    values[2] += task.isFailed() ? 1 : 0
                    values[3] += task.isSkipped() ? 1 : 0
                    values[4] += task.isUpToDate() ? 1 : 0
                    values[5] += task.isDidWork() ? 1 : 0
                    values[6] += task.isFromCache() ? 1 : 0
                    values[7] += task.isNoSource() ? 1 : 0
                    values[8] += task.isActionable() ? 1 : 0
                }

                int index = 0
                details = values.collect { v ->
                    index++
                    String.valueOf(v == 0 ? (index == 1 || zeroPadding.get() ? v : ' ') : v).padLeft(3, ' ')
                }.join(' ')

                (0..8).each { i -> totals[i] += values[i] }
            }

            println(row + ' ' + details)
        }

        println(separator)
        if (projectExecutedCount >= displayProjectThreshold.get()) {
            println("${projectExecutedCount} projects executed")
            println(separator)
        }
    }

    private static String formatDuration(double time) {
        TimeUtils.formatDuration(time).padLeft(9, ' ')
    }
}
