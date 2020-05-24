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
package org.kordamp.gradle.plugin.cpd

import groovy.transform.CompileStatic
import groovy.transform.Internal
import org.gradle.api.Action
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.internal.project.IsolatedAntBuilder
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.reporting.Reporting
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask
import org.gradle.util.ClosureBackedAction
import org.kordamp.gradle.plugin.cpd.internal.CpdInvoker
import org.kordamp.gradle.plugin.cpd.internal.CpdReportsImpl

import javax.inject.Inject

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
@CacheableTask
class Cpd extends SourceTask implements VerificationTask, Reporting<CpdReports> {
    private FileCollection cpdClasspath
    private final CpdReports reports
    private boolean ignoreFailures

    @Input
    final Property<Integer> minimumTokenCount
    @Input
    final Property<String> encoding
    @Input
    final Property<String> language
    @Input
    final Property<Boolean> ignoreLiterals
    @Input
    final Property<Boolean> ignoreIdentifiers
    @Input
    final Property<Boolean> ignoreAnnotations

    Cpd() {
        ObjectFactory objects = getObjectFactory()
        reports = objects.newInstance(CpdReportsImpl, this)
        minimumTokenCount = project.objects.property(Integer).convention(50)
        encoding = project.objects.property(String).convention('UTF-8')
        language = project.objects.property(String).convention('java')
        ignoreLiterals = project.objects.property(Boolean).convention(false)
        ignoreIdentifiers = project.objects.property(Boolean).convention(false)
        ignoreAnnotations = project.objects.property(Boolean).convention(false)
    }

    @Inject
    protected ObjectFactory getObjectFactory() {
        throw new UnsupportedOperationException()
    }

    @Inject
    @Internal
    IsolatedAntBuilder getAntBuilder() {
        throw new UnsupportedOperationException()
    }

    @TaskAction
    void run() {
        File stylesheet = project.layout.buildDirectory.file(taskIdentity.name + '/cpdhtml.xslt').get().asFile
        stylesheet.parentFile.mkdirs()
        stylesheet.withWriter { writer ->
            writer.write(this.class.classLoader.getResourceAsStream('etc/xslt/cpdhtml.xslt').text)
        }
        CpdInvoker.invoke(this)
    }

    @Override
    CpdReports reports(@DelegatesTo(value = CpdReports.class, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        return reports(new ClosureBackedAction<CpdReports>(closure))
    }

    @Override
    CpdReports reports(Action<? super CpdReports> configureAction) {
        configureAction.execute(reports)
        return reports
    }

    @Override
    @PathSensitive(PathSensitivity.RELATIVE)
    FileTree getSource() {
        return super.getSource()
    }

    @Classpath
    FileCollection getCpdClasspath() {
        return cpdClasspath
    }

    void setCpdClasspath(FileCollection cpdClasspath) {
        this.cpdClasspath = cpdClasspath
    }

    @Override
    @Nested
    final CpdReports getReports() {
        return reports
    }

    @Override
    void setIgnoreFailures(boolean ignoreFailures) {
        this.ignoreFailures = ignoreFailures
    }

    @Override
    @Input
    boolean getIgnoreFailures() {
        ignoreFailures
    }
}
