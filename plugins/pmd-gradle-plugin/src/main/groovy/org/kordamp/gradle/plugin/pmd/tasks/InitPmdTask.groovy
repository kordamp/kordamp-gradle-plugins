/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2025 Andres Almiray.
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
package org.kordamp.gradle.plugin.pmd.tasks

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

/**
 * @author Andres Almiray
 * @since 0.31.0
 */
@CompileStatic
class InitPmdTask extends DefaultTask {
    @Input
    final Property<Boolean> overwrite = project.objects.property(Boolean).convention(false)

    @Option(option = 'overwrite', description = 'Overwrite existing configuration file')
    void setOverwrite(boolean overwrite) {
        this.overwrite.set(overwrite)
    }

    @TaskAction
    void init() {
        String baseName = project == project.rootProject ? 'pmd' : project.name
        File file = project.rootProject.file("config/pmd/${baseName}.xml")

        if (file.exists() && !overwrite.getOrElse(false)) {
            println "Pmd config file ${file.absolutePath} already exists. Invoke this task with --overwrite if you want to replace it."
            return
        }

        file.parentFile.mkdirs()
        file.text = """<?xml version="1.0"?>
        |<ruleset name="PMD ruleset for ${project.name}"
        |        xmlns="http://pmd.sourceforge.net/ruleset/2.0.0"
        |        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        |        xsi:schemaLocation="http://pmd.sourceforge.net/ruleset/2.0.0
        |                            http://pmd.sourceforge.net/ruleset_2_0_0.xsd">
        |    <description>${project.name}</description>
        |    <rule ref="category/java/bestpractices.xml"/>
        |    <rule ref="category/java/errorprone.xml"/>
        |    <rule ref="category/java/design.xml">
        |        <exclude name="LoosePackageCoupling"/>
        |    </rule>
        |    <rule ref="category/java/codestyle.xml">
        |        <exclude name="AtLeastOneConstructor"/>
        |    </rule>
        |    <rule ref="category/java/codestyle.xml/ClassNamingConventions"/>
        |</ruleset>""".stripMargin('|')
    }
}
