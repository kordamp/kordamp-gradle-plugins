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
package org.kordamp.gradle.plugin.cpd

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.internal.ConventionMapping
import org.gradle.api.plugins.quality.CodeQualityExtension
import org.gradle.api.plugins.quality.internal.AbstractCodeQualityPlugin
import org.gradle.api.reporting.SingleFileReport
import org.gradle.api.tasks.SourceSet

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
class BaseCpdPlugin extends AbstractCodeQualityPlugin<Cpd> {
    private CpdExtension extension

    Project getMyProject() {
        project
    }

    @Override
    protected String getToolName() {
        return 'CPD'
    }

    @Override
    protected Class<Cpd> getTaskType() {
        return Cpd.class
    }

    @Override
    protected CodeQualityExtension createExtension() {
        extension = project.getExtensions().create('cpd', CpdExtension.class, project)
        extension.setToolVersion('6.24.0')
        return extension
    }

    @Override
    protected void configureConfiguration(Configuration configuration) {
        configureDefaultDependencies(configuration)
    }

    @Override
    protected void configureTaskDefaults(Cpd task, String baseName) {
        Configuration configuration = project.configurations.getAt(getConfigurationName())
        configureTaskConventionMapping(configuration, task)
        configureReportsConventionMapping(task, baseName)
    }

    private void configureDefaultDependencies(Configuration configuration) {
        configuration.defaultDependencies(new Action<DependencySet>() {
            @Override
            void execute(DependencySet dependencies) {
                String dependency = calculateDefaultDependencyNotation()
                dependencies.add(myProject.dependencies.create(dependency))
            }
        })
    }

    private void configureTaskConventionMapping(Configuration configuration, final Cpd task) {
        ConventionMapping taskMapping = task.conventionMapping
        taskMapping.map('cpdClasspath', { -> configuration })
        taskMapping.map('minimumTokenCount', { -> extension.minimumTokenCount.get() })
        taskMapping.map('encoding', { -> extension.encoding.get() })
        taskMapping.map('language', { -> extension.language.get() })
        taskMapping.map('ignoreFailures', { -> extension.isIgnoreFailures() })
        taskMapping.map('ignoreLiterals', { -> extension.ignoreLiterals.get() })
        taskMapping.map('ignoreIdentifiers', { -> extension.ignoreIdentifiers.get() })
        taskMapping.map('ignoreAnnotations', { -> extension.ignoreAnnotations.get() })
    }

    private void configureReportsConventionMapping(Cpd task, final String baseName) {
        task.reports.all({ SingleFileReport report ->
            report.required.convention(true)
            report.outputLocation.convention(project.layout.projectDirectory.file(project.provider({ ->
                new File(extension.reportsDir, baseName + '.' + report.name).absolutePath
            })))
        })
    }

    protected String calculateDefaultDependencyNotation() {
        return 'net.sourceforge.pmd:pmd-dist:' + extension.toolVersion
    }

    @Override
    protected void configureForSourceSet(final SourceSet sourceSet, final Cpd task) {
        task.group = 'Quality'
        task.description = 'Run CPD analysis for ' + sourceSet.name + ' classes'
        task.source = sourceSet.allJava
    }
}
