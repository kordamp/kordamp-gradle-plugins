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
package org.kordamp.gradle.plugin.sourcexref

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.apache.maven.jxr.JXR
import org.apache.maven.jxr.log.Log
import org.gradle.api.DefaultTask
import org.gradle.api.JavaVersion
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.Paths

import static org.kordamp.gradle.util.StringUtils.isBlank
import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.7.0
 */
@CacheableTask
@CompileStatic
class JxrTask extends DefaultTask {
    @OutputDirectory File outputDirectory

    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFiles FileCollection sourceDirs

    @Input @Optional String templateDir
    @Input @Optional String inputEncoding
    @Input @Optional String outputEncoding
    @Input @Optional String windowTitle
    @Input @Optional String docTitle
    @Input @Optional String bottom = ''
    @Input @Optional String stylesheet
    @Input @Optional List<String> includes = []
    @Input @Optional List<String> excludes = []
    @Input @Optional JavaVersion javaVersion

    JxrTask() {
        windowTitle = "${project.name} ${project.version} Reference"
        docTitle = "${project.name} ${project.version} Reference"
    }

    String getOutputEncoding() {
        outputEncoding ?: 'UTF-8'
    }

    @TaskAction
    void xref() {
        JXR jxr = new JXR()
        jxr.setDest(Paths.get(outputDirectory.absolutePath))

        if (isBlank(inputEncoding)) {
            String platformEncoding = System.getProperty('file.encoding')
            logger.warn("File encoding has not been set, using platform encoding " + platformEncoding
                + ", i.e. build is platform dependent!")
            jxr.setInputEncoding(platformEncoding)
        } else {
            jxr.setInputEncoding(inputEncoding)
        }

        jxr.outputEncoding = getOutputEncoding()
        jxr.locale = Locale.default
        jxr.log = new LogWrapper()
        jxr.revision = 'HEAD' // TODO

        if (excludes) {
            jxr.excludes = excludes.toArray(new String[excludes.size()])
        }
        if (includes) {
            jxr.includes = includes.toArray(new String[includes.size()])
        }

        ClassLoader contextClassLoader = Thread.currentThread().contextClassLoader
        try {
            Thread.currentThread().contextClassLoader = JxrTask.class.classLoader
            jxr.xref(resolveSourceDirs(), resolveTemplatesDir(), windowTitle, docTitle, bottom)
        }
        finally {
            Thread.currentThread().contextClassLoader = contextClassLoader
        }

        copyResources(outputDirectory.absolutePath)
    }

    @CompileDynamic
    List<String> resolveSourceDirs() {
        sourceDirs.files.flatten().findAll { it.exists() }*.absolutePath
    }

    String resolveTemplatesDir() {
        JavaVersion javadocTemplatesVersion = resolveJavadocTemplatesVersion()
        // Check if overridden
        if (isBlank(templateDir)) {
            if (javadocTemplatesVersion.java8Compatible) {
                return 'templates/jdk8'
            } else if (javadocTemplatesVersion.java7Compatible) {
                return 'templates/jdk7'
            } else if (javadocTemplatesVersion.java5Compatible) {
                return 'templates/jdk4'
            } else {
                logger.warn('Unsupported javadocVersion: ' + javadocTemplatesVersion + '. Fallback to original')
                return 'templates'
            }
        }
        // use value specified by user
        return templateDir
    }

    JavaVersion resolveJavadocTemplatesVersion() {
        javaVersion ?: JavaVersion.current()
    }

    private void copyResources(String dir) {
        if (isNotBlank(stylesheet)) {
            File stylesheetFile = new File(stylesheet)
            File destStylesheetFile = new File(dir, 'stylesheet.css')

            try {
                if (stylesheetFile.isAbsolute()) {
                    Files.copy(Paths.get(stylesheetFile.toURI()), Paths.get(destStylesheetFile.toURI()))
                } else {
                    URL stylesheetUrl = JxrTask.class.classLoader.getResource(stylesheet)
                    destStylesheetFile.parentFile.mkdirs()
                    destStylesheetFile.text = stylesheetUrl.text
                }
            }
            catch (IOException e) {
                logger.warn('An error occured while copying the stylesheet to the target directory', e)
            }
        } else {
            JavaVersion javadocTemplatesVersion = resolveJavadocTemplatesVersion()

            if (javadocTemplatesVersion.java8Compatible) {
                copyResources(dir, 'jdk8/', 'stylesheet.css')
            } else if (javadocTemplatesVersion.java7Compatible) {
                String[] jdk7Resources = [
                    'stylesheet.css',
                    'resources/background.gif',
                    'resources/tab.gif',
                    'resources/titlebar.gif',
                    'resources/titlebar_end.gif'
                ] as String[]
                copyResources(dir, 'jdk7/', jdk7Resources)
            } else if (javadocTemplatesVersion.java6Compatible) {
                copyResources(dir, 'jdk6/', 'stylesheet.css')
            } else if (javadocTemplatesVersion.java5Compatible) {
                copyResources(dir, 'jdk4/', 'stylesheet.css')
            } else {
                // Fallback to the original stylesheet
                copyResources(dir, '', 'stylesheet.css')
            }
        }
    }

    private void copyResources(String dir, String sourceFolder, String... files) {
        try {
            for (String file : files) {
                URL resourceUrl = JxrTask.class.classLoader.getResource(sourceFolder + file)
                File destResourceFile = new File(dir, file)
                destResourceFile.parentFile.mkdirs()
                destResourceFile.text = resourceUrl.text
            }
        }
        catch (IOException e) {
            logger.warn('An error occured while copying the resource to the target directory', e)
        }
    }

    private class LogWrapper implements Log {
        @Override
        void info(String message) {
            logger.info(message)
        }

        @Override
        void debug(String message) {
            logger.debug(message)
        }

        @Override
        void warn(String message) {
            logger.warn(message)
        }

        @Override
        void error(String message) {
            logger.error(message)
        }
    }
}
