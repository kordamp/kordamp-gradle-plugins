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
package org.kordamp.gradle.plugin.checkstyle.tasks

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
class InitCheckstyleTask extends DefaultTask {
    @Input
    final Property<Boolean> overwrite = project.objects.property(Boolean).convention(false)

    @Option(option = 'overwrite', description = 'Overwrite existing configuration file')
    void setOverwrite(boolean overwrite) {
        this.overwrite.set(overwrite)
    }

    @TaskAction
    void init() {
        String baseName = project == project.rootProject ? 'checkstyle' : project.name
        File file = project.rootProject.file("config/checkstyle/${baseName}.xml")

        if (file.exists() && !overwrite.getOrElse(false)) {
            println "Checkstyle config file ${file.absolutePath} already exists. Invoke this task with --overwrite if you want to replace it."
            return
        }

        file.parentFile.mkdirs()
        file.text = '''|<!DOCTYPE module PUBLIC
        |    "-//Puppy Crawl//DTD Check Configuration 1.2//EN"
        |    "http://www.puppycrawl.com/dtds/configuration_1_2.dtd">
        |<module name="Checker">
        |    <module name="TreeWalker">
        |        <!-- Blocks -->
        |        <module name="EmptyBlock">
        |            <property name="option" value="statement"/>
        |            <property name="tokens" value="LITERAL_DO,LITERAL_ELSE,LITERAL_FINALLY,LITERAL_IF,LITERAL_FOR,LITERAL_TRY,LITERAL_WHILE,INSTANCE_INIT,STATIC_INIT"/>
        |        </module>
        |        <module name="EmptyBlock">
        |            <property name="option" value="text"/>
        |            <property name="tokens" value="LITERAL_CATCH"/>
        |        </module>
        |        <module name="AvoidNestedBlocks"/>
        |
        |        <!-- Braces -->
        |        <module name="LeftCurly"/>
        |        <module name="RightCurly"/>
        |        <module name="NeedBraces"/>
        |
        |        <!-- Whitespace -->
        |        <module name="GenericWhitespace"/>
        |        <module name="EmptyForInitializerPad"/>
        |        <module name="EmptyForIteratorPad"/>
        |        <module name="MethodParamPad"/>
        |        <module name="NoWhitespaceBefore"/>
        |        <module name="NoWhitespaceAfter"/>
        |        <module name="OperatorWrap"/>
        |        <module name="ParenPad"/>
        |        <module name="TypecastParenPad"/>
        |        <module name="WhitespaceAfter">
        |            <property name="tokens" value="COMMA, SEMI"/>
        |        </module>
        |
        |        <!-- Coding -->
        |        <module name="CovariantEquals"/>
        |        <module name="DefaultComesLast"/>
        |        <module name="EmptyStatement"/>
        |        <module name="EqualsHashCode"/>
        |        <module name="ExplicitInitialization"/>
        |        <module name="MultipleVariableDeclarations"/>
        |        <module name="NoClone"/>
        |        <module name="NoFinalizer"/>
        |        <!--<module name="RedundantThrows">-->
        |        <!--<property name="allowUnchecked" value="true"/>-->
        |        <!--</module>-->
        |        <module name="SimplifyBooleanExpression"/>
        |        <module name="SimplifyBooleanReturn"/>
        |        <module name="StringLiteralEquality"/>
        |        <module name="UnnecessaryParentheses"/>
        |
        |        <!-- Design -->
        |        <module name="InterfaceIsType"/>
        |
        |        <!-- Imports -->
        |        <module name="RedundantImport"/>
        |        <module name="UnusedImports"/>
        |
        |        <!-- Naming -->
        |        <module name="ConstantName"/>
        |        <module name="LocalFinalVariableName"/>
        |        <module name="LocalVariableName"/>
        |        <module name="MemberName">
        |            <property name="format" value="^[a-z_][a-zA-Z0-9_]*$"/>
        |        </module>
        |        <module name="MethodName">
        |            <property name="format" value="^[a-z_][a-zA-Z0-9_]*$"/>
        |        </module>
        |        <module name="MethodTypeParameterName"/>
        |        <module name="PackageName">
        |            <property name="format" value="^[a-z]+(\\.[a-z][a-z0-9]*)*$"/>
        |        </module>
        |        <module name="ParameterName"/>
        |        <module name="StaticVariableName"/>
        |        <module name="TypeName"/>
        |        <module name="ClassTypeParameterName">
        |            <property name="format" value="^[A-Z]+$"/>
        |        </module>
        |        <module name="InterfaceTypeParameterName">
        |            <property name="format" value="^[A-Z]+$"/>
        |        </module>
        |        <module name="MethodTypeParameterName">
        |            <property name="format" value="^[A-Z]+$"/>
        |        </module>
        |
        |        <!-- allows suppressing using the //CHECKSTYLE:ON //CHECKSTYLE:OFF -->
        |        <module name="SuppressionCommentFilter"/>
        |    </module>
        |    <module name="FileTabCharacter"/>
        |</module>'''.stripMargin('|')
    }
}
