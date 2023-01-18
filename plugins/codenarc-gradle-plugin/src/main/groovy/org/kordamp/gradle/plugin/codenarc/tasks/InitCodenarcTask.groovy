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
package org.kordamp.gradle.plugin.codenarc.tasks

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
class InitCodenarcTask extends DefaultTask {
    @Input
    final Property<Boolean> overwrite = project.objects.property(Boolean).convention(false)

    @Option(option = 'overwrite', description = 'Overwrite existing configuration file')
    void setOverwrite(boolean overwrite) {
        this.overwrite.set(overwrite)
    }

    @TaskAction
    void init() {
        String baseName = project == project.rootProject ? 'codenarc' : project.name
        File file = project.rootProject.file("config/codenarc/${baseName}.xml")

        if (file.exists() && !overwrite.getOrElse(false)) {
            println "Codenarc config file ${file.absolutePath} already exists. Invoke this task with --overwrite if you want to replace it."
            return
        }

        file.parentFile.mkdirs()
        file.text = '''<ruleset xmlns="http://codenarc.org/ruleset/1.0"
        |         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        |         xsi:schemaLocation="http://codenarc.org/ruleset/1.0 http://codenarc.org/ruleset-schema.xsd"
        |         xsi:noNamespaceSchemaLocation="http://codenarc.org/ruleset-schema.xsd">
        |
        |    <description>Sample rule set</description>
        |
        |    <ruleset-ref path="rulesets/basic.xml"/>
        |    <ruleset-ref path="rulesets/braces.xml">
        |        <rule-config name="IfStatementBraces">
        |            <property name="priority" value="3"/>
        |        </rule-config>
        |    </ruleset-ref>
        |    <ruleset-ref path="rulesets/concurrency.xml"/>
        |    <ruleset-ref path="rulesets/convention.xml"/>
        |    <ruleset-ref path="rulesets/design.xml"/>
        |    <ruleset-ref path="rulesets/dry.xml">
        |        <rule-config name="DuplicateStringLiteral">
        |            <property name="doNotApplyToClassNames" value="*Spec"/>
        |        </rule-config>
        |        <rule-config name="DuplicateNumberLiteral">
        |            <property name="doNotApplyToClassNames" value="*Spec"/>
        |        </rule-config>
        |        <rule-config name="DuplicateMapLiteral">
        |            <property name="doNotApplyToClassNames" value="*Spec"/>
        |        </rule-config>
        |        <rule-config name="DuplicateListLiteral">
        |            <property name="doNotApplyToClassNames" value="*Spec"/>
        |        </rule-config>
        |    </ruleset-ref>
        |    <ruleset-ref path="rulesets/exceptions.xml"/>
        |    <ruleset-ref path="rulesets/formatting.xml">
        |        <rule-config name="LineLength">
        |            <property name="length" value="160"/>
        |        </rule-config>
        |        <exclude name="Indentation"/>
        |        <exclude name="SpaceAroundMapEntryColon"/>
        |    </ruleset-ref>
        |    <ruleset-ref path="rulesets/generic.xml"/>
        |    <ruleset-ref path="rulesets/grails.xml"/>
        |    <ruleset-ref path="rulesets/groovyism.xml"/>
        |    <ruleset-ref path="rulesets/imports.xml">
        |        <rule-config name="NoWildcardImports">
        |            <property name="ignoreStaticImports" value="true"/>
        |        </rule-config>
        |        <exclude name="MisorderedStaticImports"/>
        |    </ruleset-ref>
        |    <ruleset-ref path="rulesets/jdbc.xml"/>
        |    <ruleset-ref path="rulesets/junit.xml">
        |        <rule-config name="JUnitPublicNonTestMethod">
        |            <property name="applyToClassNames" value="Test,Tests,TestCase"/>
        |        </rule-config>
        |        <rule-config name="JUnitPublicProperty">
        |            <property name="applyToClassNames" value="Test,Tests,TestCase"/>
        |        </rule-config>
        |        <rule-config name="JUnitPublicField">
        |            <property name="applyToClassNames" value="Test,Tests,TestCase"/>
        |        </rule-config>
        |        <rule-config name="JUnitTestMethodWithoutAssert">
        |            <property name="applyToClassNames" value="Test,Tests,TestCase"/>
        |        </rule-config>
        |    </ruleset-ref>
        |    <ruleset-ref path="rulesets/logging.xml"/>
        |    <ruleset-ref path="rulesets/naming.xml">
        |        <rule-config name="FieldName">
        |            <property name="ignoreFieldNames" value="log,serialVersionUID"/>
        |        </rule-config>
        |        <rule-config name="MethodName">
        |            <property name="doNotApplyToClassNames" value="*Spec"/>
        |        </rule-config>
        |        <exclude name="FactoryMethodName"/>
        |    </ruleset-ref>
        |    <ruleset-ref path="rulesets/security.xml">
        |        <exclude name="JavaIoPackageAccess"/>
        |    </ruleset-ref>
        |    <ruleset-ref path="rulesets/serialization.xml"/>
        |    <ruleset-ref path="rulesets/size.xml"/>
        |    <ruleset-ref path="rulesets/unnecessary.xml">
        |        <exclude name="UnnecessaryPackageReference"/>
        |        <exclude name="UnnecessaryReturnKeyword"/>
        |    </ruleset-ref>
        |    <ruleset-ref path="rulesets/unused.xml"/>
        |
        |</ruleset>'''.stripMargin('|')
    }
}
