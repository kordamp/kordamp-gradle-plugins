/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.bundling.Jar
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.CollectionUtils
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.impl.ExtStandardJavadocDocletOptions

import static org.kordamp.gradle.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
class Javadoc extends AbstractFeature {
    Set<String> excludes = new LinkedHashSet<>()
    Set<String> includes = new LinkedHashSet<>()
    String title
    final ExtStandardJavadocDocletOptions options = new ExtStandardJavadocDocletOptions()
    final AutoLinks autoLinks = new AutoLinks()

    private final Set<Project> projects = new LinkedHashSet<>()
    private final Set<org.gradle.api.tasks.javadoc.Javadoc> javadocTasks = new LinkedHashSet<>()
    private final Set<Jar> javadocJarTasks = new LinkedHashSet<>()

    Javadoc(ProjectConfigurationExtension config, Project project) {
        super(config, project)
        options.use         = true
        options.splitIndex  = true
        options.encoding    = 'UTF-8'
        options.author      = true
        options.version     = true
        options.windowTitle = "${project.name} ${project.version}"
        options.docTitle    = "${project.name} ${project.version}"
        options.header      = "${project.name} ${project.version}"
        options.links(resolveJavadocLinks(project.findProperty('targetCompatibility')))
    }

    private String resolveJavadocLinks(Object jv) {
        JavaVersion javaVersion = JavaVersion.current()

        if (jv instanceof JavaVersion) {
            javaVersion = (JavaVersion) jv
        } else if (jv != null) {
            javaVersion = JavaVersion.toVersion(jv)
        }

        if (javaVersion.isJava11Compatible()) {
            return "https://docs.oracle.com/en/java/javase/${javaVersion.majorVersion}/docs/api/".toString()
        }
        return "https://docs.oracle.com/javase/${javaVersion.majorVersion}/docs/api/"
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        if (enabled) {
            List<String> links = []
            options.links.each { link ->
                links << link
            }

            map.title = title
            map.excludes = excludes
            map.includes = includes
            map.autoLinks = autoLinks.toMap()
            map.options = new LinkedHashMap<String, Object>([
                windowTitle: options.windowTitle,
                docTitle   : options.docTitle,
                header     : options.header,
                encoding   : options.encoding,
                author     : options.author,
                version    : options.version,
                splitIndex : options.splitIndex,
                use        : options.use,
                links      : links
            ])
        }

        new LinkedHashMap<>(['javadoc': map])
    }

    void postMerge() {
        autoLinks.resolveLinks(project).each { options.links(it) }
    }

    void include(String str) {
        includes << str
    }

    void exclude(String str) {
        excludes << str
    }

    void options(Action<? super ExtStandardJavadocDocletOptions> action) {
        action.execute(options)
    }

    void options(@DelegatesTo(ExtStandardJavadocDocletOptions) Closure action) {
        ConfigureUtil.configure(action, options)
    }

    void copyInto(Javadoc copy) {
        super.copyInto(copy)
        copy.excludes.addAll(excludes)
        copy.includes.addAll(includes)
        copy.title = title
        options.copyInto(copy.options)
        autoLinks.copyInto(copy.autoLinks)
    }

    static void merge(Javadoc o1, Javadoc o2) {
        AbstractFeature.merge(o1, o2)
        CollectionUtils.merge(o1.excludes, o2.excludes)
        CollectionUtils.merge(o1.includes, o2.includes)
        o1.title = o1.title ?: o2.title
        ExtStandardJavadocDocletOptions.merge(o1.options, o2.options)
        o1.projects().addAll(o2.projects())
        o1.javadocTasks().addAll(o2.javadocTasks())
        o1.javadocJarTasks().addAll(o2.javadocJarTasks())
        AutoLinks.merge(o1.autoLinks, o2.autoLinks)
    }

    Set<Project> projects() {
        projects
    }

    Set<org.gradle.api.tasks.javadoc.Javadoc> javadocTasks() {
        javadocTasks
    }

    Set<Jar> javadocJarTasks() {
        javadocJarTasks
    }

    void autoLinks(Action<? super AutoLinks> action) {
        action.execute(autoLinks)
    }

    void autoLinks(@DelegatesTo(AutoLinks) Closure action) {
        ConfigureUtil.configure(action, autoLinks)
    }

    void applyTo(org.gradle.api.tasks.javadoc.Javadoc javadoc) {
        javadoc.title = title
        javadoc.getIncludes().addAll(includes)
        javadoc.getExcludes().addAll(excludes)
        options.applyTo(javadoc.options)
    }

    @CompileStatic
    @Canonical
    static class AutoLinks {
        boolean enabled = true
        Set<String> excludes = new LinkedHashSet<>()
        List<String> configurations = []

        private boolean enabledSet

        protected void doSetEnabled(boolean enabled) {
            this.enabled = enabled
        }

        void setEnabled(boolean enabled) {
            this.enabled = enabled
            this.enabledSet = true
        }

        boolean isEnabledSet() {
            this.enabledSet
        }

        void exclude(String str) {
            excludes << str
        }

        void copyInto(AutoLinks copy) {
            copy.@enabled = this.enabled
            copy.@enabledSet = this.enabledSet
            copy.configurations.addAll(this.configurations)
            copy.excludes.addAll(this.excludes)
        }

        static void merge(AutoLinks o1, AutoLinks o2) {
            o1.setEnabled((boolean) (o1.enabledSet ? o1.enabled : o2.enabled))
            CollectionUtils.merge(o1.excludes, o2?.excludes)
            CollectionUtils.merge(o1.@configurations, o2?.configurations)
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

            if (enabled) {
                List<String> cs = new ArrayList<>(configurations)
                if (!cs) {
                    cs = ['compile', 'compileOnly', 'annotationProcessor', 'runtime']
                }
                map.excludes = excludes
                map.configurations = cs
            }

            map
        }

        List<String> resolveLinks(Project project) {
            List<String> links = []

            if (!enabled) return links

            List<String> cs = new ArrayList<>(configurations)
            if (!cs) {
                cs = ['compile', 'compileOnly', 'annotationProcessor', 'runtime']
            }

            for (String cn : cs) {
                Configuration c = project.configurations.findByName(cn)
                c?.dependencies?.each { Dependency dep ->
                    String artifactName = "${dep.name}-${dep.version}".toString()
                    if (!isExcluded(artifactName) && dep.name != 'unspecified' &&
                        isNotBlank(dep.group) && isNotBlank(dep.version)) {
                        links << calculateJavadocLink(dep.group, dep.name, dep.version)
                    }
                }
            }

            links
        }

        private boolean isExcluded(String artifactName) {
            for (String s : excludes) {
                if (artifactName.matches(s)) {
                    return true
                }
            }
            false
        }

        private String calculateJavadocLink(String group, String name, String version) {
            String normalizedGroup = group.replace('.', '/')
            "https://oss.sonatype.org/service/local/repositories/releases/archive/$normalizedGroup/$name/$version/$name-$version-javadoc.jar/!/".toString()
        }
    }
}
