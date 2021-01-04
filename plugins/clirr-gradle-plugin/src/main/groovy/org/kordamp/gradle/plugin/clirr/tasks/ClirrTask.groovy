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
package org.kordamp.gradle.plugin.clirr.tasks

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import net.sf.clirr.core.Checker
import net.sf.clirr.core.CheckerException
import net.sf.clirr.core.ClassFilter
import net.sf.clirr.core.internal.bcel.BcelTypeArrayBuilder
import net.sf.clirr.core.spi.JavaType
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.kordamp.gradle.plugin.base.plugins.Clirr
import org.kordamp.gradle.plugin.clirr.reporters.CountReporter
import org.kordamp.gradle.plugin.clirr.reporters.HtmlReporter
import org.kordamp.gradle.plugin.clirr.reporters.Reporter
import org.kordamp.gradle.plugin.clirr.reporters.XmlReporter
import org.yaml.snakeyaml.Yaml
import uber.org.apache.bcel.classfile.JavaClass

import static net.sf.clirr.core.internal.ClassLoaderUtil.createClassLoader
import static org.kordamp.gradle.util.PluginUtils.resolveConfig

/**
 *
 * @author Andres Almiray
 * @since 0.12.0
 */
@CacheableTask
@CompileStatic
class ClirrTask extends DefaultTask {
    @InputFiles
    @PathSensitive(PathSensitivity.ABSOLUTE)
    FileCollection newClasspath

    @InputFiles
    @PathSensitive(PathSensitivity.ABSOLUTE)
    FileCollection newFiles

    @InputFiles
    @PathSensitive(PathSensitivity.ABSOLUTE)
    FileCollection baseFiles

    @OutputFile
    File xmlReport

    @OutputFile
    File htmlReport

    @TaskAction
    void clirr() {
        Clirr clirr = resolveConfig(project).clirr

        if (!clirr.enabled) {
            logger.info('{}: clirr has been disabled ', project.name)
            return
        }

        xmlReport.parentFile.mkdirs()
        htmlReport.parentFile.mkdirs()

        Checker checker = new Checker()
        JavaType[] origClasses = createClassSet(baseFiles.files as File[])
        JavaType[] newClasses = createClassSet((newClasspath + newFiles).files as File[])

        BufferedListener bufferedListener = newBufferedListener(loadExcludeFilter(clirr.filterFile))
        checker.addDiffListener(bufferedListener)

        try {
            checker.reportDiffs(origClasses, newClasses)
        } catch (CheckerException ex) {
            logger.error("Error executing 'clirr' task. ${ex}")
            if (clirr.failOnException) {
                throw new GradleException("Can't execute 'clirr' task", ex)
            }
        } catch (Exception e) {
            logger.error("Error executing 'clirr' task. ${e}")
            if (clirr.failOnException) {
                throw new GradleException("Can't execute 'clirr' task", e)
            }
        }

        Reporter reporter = new HtmlReporter(project, new FileWriter(htmlReport))
        reporter.report(bufferedListener.differences)

        reporter = new XmlReporter(project, new FileWriter(xmlReport))
        reporter.report(bufferedListener.differences)

        final CountReporter counter = new CountReporter()
        counter.report(bufferedListener.differences)

        if (counter.srcErrors > 0 || counter.srcWarnings > 0 || counter.srcInfos) {
            println("""
            Clirr Report
            ------------
            Infos:    ${counter.srcInfos}
            Warnings: ${counter.srcWarnings}
            Errors:   ${counter.srcErrors}
            """.stripIndent(12))
            println("Please review ${htmlReport.canonicalPath} for more information")
        }

        if (clirr.failOnErrors) {
            if (counter.srcErrors > 0) {
                throw new GradleException("There are several compatibility issues.\nPlease check ${htmlReport.canonicalPath} for more information")
            }
        }
    }

    @CompileDynamic
    private static BufferedListener newBufferedListener(Map map) {
        new BufferedListener(
            map.differenceTypes ?: [],
            map.packages ?: [],
            map.classes ?: [],
            map.members ?: [:]
        )
    }

    private Map loadExcludeFilter(File excludeFilter) {
        if (!excludeFilter) return [:]
        Yaml yaml = new Yaml()
        def data = yaml.load(new FileInputStream(excludeFilter))
        return data as Map
    }

    private JavaType[] createClassSet(File[] files) {
        final ClassLoader classLoader = createClassLoader(files as String[])
        return BcelTypeArrayBuilder.createClassSet(files, classLoader, new ClassSelector())
    }

    private static class ClassSelector implements ClassFilter {
        @Override
        boolean isSelected(final JavaClass javaClass) {
            return true
        }
    }
}