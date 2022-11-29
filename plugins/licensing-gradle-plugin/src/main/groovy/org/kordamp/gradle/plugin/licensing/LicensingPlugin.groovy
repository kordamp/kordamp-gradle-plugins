/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2022 Andres Almiray.
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
package org.kordamp.gradle.plugin.licensing

import com.hierynomus.gradle.license.LicenseReportingPlugin
import com.hierynomus.gradle.license.tasks.LicenseCheck
import com.hierynomus.gradle.license.tasks.LicenseFormat
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import nl.javadude.gradle.plugins.license.DownloadLicenses
import nl.javadude.gradle.plugins.license.DownloadLicensesExtension
import nl.javadude.gradle.plugins.license.LicenseExtension
import nl.javadude.gradle.plugins.license.LicenseMetadata
import nl.javadude.gradle.plugins.license.LicensePlugin
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.kordamp.gradle.annotations.DependsOn
import org.kordamp.gradle.listener.AllProjectsEvaluatedListener
import org.kordamp.gradle.listener.ProjectEvaluatedListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.BasePlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.License
import org.kordamp.gradle.plugin.base.model.LicenseId
import org.kordamp.gradle.plugin.base.plugins.Licensing

import javax.inject.Named

import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addAllProjectsEvaluatedListener
import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addProjectEvaluatedListener
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject
import static org.kordamp.gradle.util.PluginUtils.resolveConfig

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
class LicensingPlugin extends AbstractKordampPlugin {
    private static final LicenseMetadata LICENSE_APACHE_TWO = new LicenseMetadata('The Apache Software License, Version 2.0', LicenseId.APACHE_2_0.url())
    private static final LicenseMetadata LICENSE_EPL1 = new LicenseMetadata('Eclipse Public License v1.0', LicenseId.EPL_1_0.url())
    private static final LicenseMetadata LICENSE_EPL2 = new LicenseMetadata('Eclipse Public License v2.0', LicenseId.EPL_2_0.url())
    private static final LicenseMetadata LICENSE_LGPL21 = new LicenseMetadata('GNU Lesser General Public License v2.1 or later', LicenseId.LGPL_2_1_OR_LATER.url())
    private static final LicenseMetadata LICENSE_MIT = new LicenseMetadata('MIT License', LicenseId.MIT.url())
    private static final LicenseMetadata LICENSE_BSD2 = new LicenseMetadata('BSD 2-Clause FreeBSD License', LicenseId.BSD_2_CLAUSE_FREEBSD.url())
    private static final LicenseMetadata LICENSE_BSD3 = new LicenseMetadata('BSD 3-Clause "New" or "Revised" License', LicenseId.BSD_3_CLAUSE.url())

    private static final Map<LicenseId, LicenseMetadata> LICENSES_MAP = [
        (LicenseId.APACHE_2_0)          : LICENSE_APACHE_TWO,
        (LicenseId.EPL_1_0)             : LICENSE_EPL1,
        (LicenseId.EPL_2_0)             : LICENSE_EPL2,
        (LicenseId.LGPL_2_1_OR_LATER)   : LICENSE_LGPL21,
        (LicenseId.MIT)                 : LICENSE_MIT,
        (LicenseId.BSD_2_CLAUSE_FREEBSD): LICENSE_BSD2,
        (LicenseId.BSD_3_CLAUSE)        : LICENSE_BSD3
    ]

    private static final Map<LicenseMetadata, List<String>> DEFAULT_ALIASES = [
        (LICENSE_APACHE_TWO): ['The Apache Software License, Version 2.0',
                               'The Apache Software License, version 2.0',
                               'Apache Software License - Version 2.0',
                               'Apache Software License - version 2.0',
                               'the Apache License, ASL Version 2.0',
                               'The Apache License, Version 2.0',
                               'The Apache License Version 2.0',
                               'Apache License, Version 2.0',
                               'Apache License, version 2.0',
                               'Apache License Version 2.0',
                               'Apache License version 2.0',
                               'The Apache License 2.0',
                               'Apache 2.0 License',
                               'Apache License 2.0',
                               'Apache 2.0',
                               'Apache-2.0',
                               'Apache 2'],
        (LICENSE_EPL1)      : ['Eclipse Public License - Version 1.0',
                               'Eclipse Public License v1.0',
                               'Eclipse Public License 1.0',
                               'Eclipse Public License',
                               'EPL v1.0',
                               'EPL 1.0',
                               'EPL-1.0'],
        (LICENSE_EPL2)      : ['Eclipse Public License v2.0',
                               'Eclipse Public License 2.0',
                               'EPL v2.0',
                               'EPL 2.0',
                               'EPL-2.0'],
        (LICENSE_LGPL21)    : ['GNU Library General Public License v2.1 or later',
                               'GNU Lesser General Public License v2.1 or later',
                               'GNU Lesser General Public License, Version 2.1',
                               'LGPL 2.1',
                               'LGPL-2.1'],
        (LICENSE_MIT)       : ['The MIT License',
                               'The MIT license',
                               'MIT License',
                               'MIT license',
                               'MIT',],
        (LICENSE_BSD2)      : ['BSD 2-Clause FreeBSD License',
                               'The BSD License',
                               'The BSD license'],
        (LICENSE_BSD3)      : ['BSD 3-Clause "New" or "Revised" License',
                               '3-Clause BSD License',
                               '3-Clause BSD license',
                               'Revised BSD License',
                               'Revised BSD license',
                               'BSD Revised License',
                               'BSD Revised license',
                               'New BSD License',
                               'New BSD license',
                               'BSD New License',
                               'BSD New license',
                               'BSD 3-Clause',
                               'BSD 3-clause']
    ]

    Project project

    LicensingPlugin() {
        super(Licensing.PLUGIN_ID)
    }

    void apply(Project project) {
        this.project = project

        configureProject(project)
        if (isRootProject(project)) {
            configureRootProject(project)
            project.childProjects.values().each {
                it.pluginManager.apply(LicensingPlugin)
            }
        }
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(LicensingPlugin)) {
            project.pluginManager.apply(LicensingPlugin)
        }
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        BasePlugin.applyIfMissing(project)
        addProjectEvaluatedListener(project, new LicensingProjectEvaluatedListener())

        if (!project.plugins.findPlugin(LicenseReportingPlugin)) {
            project.pluginManager.apply(LicenseReportingPlugin)
        }
        if (!project.plugins.findPlugin(LicensePlugin)) {
            project.pluginManager.apply(LicensePlugin)
        }

        preConfigureDownloadLicensesExtension(project)
    }

    private void configureAdditionalLicenseTasks(Project project) {
        TaskProvider<LicenseCheck> licenseGradle = project.tasks.register('licenseGradle', LicenseCheck, new Action<LicenseCheck>() {
            @Override
            @CompileDynamic
            void execute(LicenseCheck t) {
                t.description = 'Scanning license on Gradle files'
                t.source = project.fileTree(project.projectDir) {
                    include('*.gradle')
                    include('*.gradle.kts')
                    include('gradle/*.gradle')
                    include('gradle/*.gradle.kts')
                    include('buildSrc/**/*.gradle')
                    include('buildSrc/**/*.gradle.kts')
                    include('gradle.properties')
                    include('gradle.yml')
                    include('gradle.toml')
                }
            }
        })
        project.tasks.findByName('license').dependsOn(licenseGradle)

        TaskProvider<LicenseFormat> licenseFormatGradle = project.tasks.register('licenseFormatGradle', LicenseFormat, new Action<LicenseFormat>() {
            @Override
            @CompileDynamic
            void execute(LicenseFormat t) {
                t.description = 'Scanning license on Gradle files'
                t.source = project.fileTree(project.projectDir) {
                    include('*.gradle')
                    include('*.gradle.kts')
                    include('gradle/*.gradle')
                    include('gradle/*.gradle.kts')
                    include('buildSrc/**/*.gradle')
                    include('buildSrc/**/*.gradle.kts')
                    include('gradle.properties')
                    include('gradle.yml')
                    include('gradle.toml')
                }
            }
        })
        project.tasks.findByName('licenseFormat').dependsOn(licenseFormatGradle)

        TaskProvider<LicenseCheck> licenseMaven = project.tasks.register('licenseMaven', LicenseCheck, new Action<LicenseCheck>() {
            @Override
            @CompileDynamic
            void execute(LicenseCheck t) {
                t.description = 'Scanning license on Maven files'
                t.source = project.fileTree(project.projectDir) {
                    include('pom.xml')
                }
            }
        })
        project.tasks.findByName('license').dependsOn(licenseMaven)

        TaskProvider<LicenseFormat> licenseFormatMaven = project.tasks.register('licenseFormatMaven', LicenseFormat, new Action<LicenseFormat>() {
            @Override
            @CompileDynamic
            void execute(LicenseFormat t) {
                t.description = 'Scanning license on Maven files'
                t.source = project.fileTree(project.projectDir) {
                    include('pom.xml')
                }
            }
        })
        project.tasks.findByName('licenseFormat').dependsOn(licenseFormatMaven)
    }

    private void configureRootProject(Project project) {
        addAllProjectsEvaluatedListener(project, new LicensingAllProjectsEvaluatedListener())

        project.tasks.register('aggregateLicenseReport', AggregateLicenseReportTask,
            new Action<AggregateLicenseReportTask>() {
                @Override
                void execute(AggregateLicenseReportTask t) {
                    t.enabled = false
                    t.group = 'Reporting'
                    t.description = 'Generates an aggregate license report.'
                }
            })
    }

    @Named('licensing')
    @DependsOn(['base'])
    private class LicensingProjectEvaluatedListener implements ProjectEvaluatedListener {
        @Override
        void projectEvaluated(Project project) {
            configureAdditionalLicenseTasks(project)
            configureLicenseExtension(project)
            postConfigureDownloadLicensesExtension(project)
        }
    }

    @Named('licensing')
    @DependsOn(['base'])
    private class LicensingAllProjectsEvaluatedListener implements AllProjectsEvaluatedListener {
        @Override
        void allProjectsEvaluated(Project rootProject) {
            configureAggregateLicenseReportTask(rootProject)
        }
    }

    @CompileDynamic
    private void configureLicenseExtension(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)
        setEnabled(config.licensing.enabled)

        if (!enabled || config.licensing.empty) {
            project.tasks.withType(LicenseCheck).each { it.enabled = false }
            project.tasks.withType(LicenseFormat).each { it.enabled = false }
            return
        }

        License lic = config.licensing.allLicenses()[0]
        if (config.licensing.allLicenses().size() > 1) {
            lic = config.licensing.allLicenses().find {
                it.primary
            } ?: config.licensing.allLicenses()[0]
        }

        LicenseExtension licenseExtension = project.extensions.findByType(LicenseExtension)

        licenseExtension.header = project.rootProject.file('gradle/LICENSE_HEADER')
        licenseExtension.strictCheck = true
        licenseExtension.mapping {
            java = 'SLASHSTAR_STYLE'
            groovy = 'SLASHSTAR_STYLE'
            kt = 'SLASHSTAR_STYLE'
            scala = 'SLASHSTAR_STYLE'
            gradle = 'SLASHSTAR_STYLE'
            kts = 'SLASHSTAR_STYLE'
            yml = 'SCRIPT_STYLE'
            toml = 'SCRIPT_STYLE'
        }
        licenseExtension.mapping(config.licensing.mappings)
        licenseExtension.ext.project = project.name
        licenseExtension.ext {
            projectName = config.info.name
            copyrightYear = config.info.copyrightYear
            author = config.info.getAuthors().join(', ')
            license = lic.licenseId?.spdx()
        }
        licenseExtension.exclude '**/*.tpl'
        licenseExtension.exclude '**/*.png'
        licenseExtension.exclude '**/*.gif'
        licenseExtension.exclude '**/*.jpg'
        licenseExtension.exclude '**/*.jpeg'
        licenseExtension.exclude '**/*.json'
        licenseExtension.exclude 'META-INF/services/*'
        licenseExtension.exclude 'META-INF/maven/*'
        licenseExtension.exclude 'META-INF/groovy/*'
        licenseExtension.excludes config.licensing.excludes
        licenseExtension.includes config.licensing.includes

        project.tasks.withType(LicenseCheck, new Action<LicenseCheck>() {
            @Override
            void execute(LicenseCheck t) {
                String sourceSetName = t.name[7..-1].uncapitalize()
                t.enabled = !config.licensing.excludedSourceSets.contains(sourceSetName)
            }
        })

        project.tasks.withType(LicenseFormat, new Action<LicenseFormat>() {
            @Override
            void execute(LicenseFormat t) {
                String sourceSetName = t.name[7..-1].uncapitalize()
                t.enabled = !config.licensing.excludedSourceSets.contains(sourceSetName)
            }
        })
    }

    private void preConfigureDownloadLicensesExtension(Project project) {
        DownloadLicensesExtension extension = project.extensions.findByType(DownloadLicensesExtension)
        extension.dependencyConfiguration = 'runtimeClasspath'
        extension.aliases.clear()
        extension.aliases.putAll(DEFAULT_ALIASES)
    }

    @CompileDynamic
    private void postConfigureDownloadLicensesExtension(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)

        Map<Object, List<Object>> defaultAliases = new LinkedHashMap<>(DEFAULT_ALIASES)
        config.licensing.licenses.forEach { license ->
            if (license.licenseId && license.aliases) {
                LicenseMetadata licenseMetadata = LICENSES_MAP.get(license.licenseId)
                if (!licenseMetadata) {
                    licenseMetadata = new LicenseMetadata(license.name, license.url)
                    LICENSES_MAP.put(license.licenseId, licenseMetadata)
                }

                List<Object> aliases = defaultAliases.get(licenseMetadata)
                if (!aliases) {
                    aliases = []
                    defaultAliases.put(licenseMetadata, aliases)
                }
                List<String> combined = aliases + license.aliases
                combined = combined.unique() - aliases
                aliases.addAll(combined)
            }
        }

        DownloadLicensesExtension extension = project.extensions.findByType(DownloadLicensesExtension)
        extension.aliases = new LinkedHashMap<>(defaultAliases)

        project.tasks.withType(DownloadLicenses) { DownloadLicenses task ->
            task.aliases = defaultAliases
        }
    }

    private void configureAggregateLicenseReportTask(Project project) {
        ProjectConfigurationExtension config = resolveConfig(project)

        if (!config.licensing.enabled) {
            return
        }

        Set<DownloadLicenses> tasks = new LinkedHashSet<>()
        project.subprojects.each { prj ->
            tasks.addAll(prj.tasks.withType(DownloadLicenses))
        }

        project.tasks.named('aggregateLicenseReport', AggregateLicenseReportTask,
            new Action<AggregateLicenseReportTask>() {
                @Override
                void execute(AggregateLicenseReportTask t) {
                    t.dependsOn tasks
                    t.enabled = true
                }
            })
    }
}
