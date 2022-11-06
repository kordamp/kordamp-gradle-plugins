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
package org.kordamp.gradle.plugin.base.plugins

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.external.javadoc.MinimalJavadocOptions
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.impl.ExtStandardJavadocDocletOptions
import org.kordamp.gradle.util.CollectionUtils
import org.kordamp.gradle.util.ConfigureUtil

import static org.kordamp.gradle.util.PluginUtils.resolveConfig
import static org.kordamp.gradle.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
class Javadoc extends AbstractFeature {
    static final String PLUGIN_ID = 'org.kordamp.gradle.javadoc'

    Boolean empty
    Boolean copyDocFiles
    Set<String> excludes = new LinkedHashSet<>()
    Set<String> includes = new LinkedHashSet<>()
    String title
    final ExtStandardJavadocDocletOptions options = new ExtStandardJavadocDocletOptions()
    final Aggregate aggregate
    final AutoLinks autoLinks

    Javadoc(ProjectConfigurationExtension config, Project project) {
        super(config, project, PLUGIN_ID)
        options.setVersion(true)
        aggregate           = new Aggregate(config, project)
        autoLinks           = new AutoLinks(config)
        options.use         = true
        options.splitIndex  = true
        options.encoding    = 'UTF-8'
        options.author      = true
        options.windowTitle = "${project.name} ${project.version}"
        options.docTitle    = "${project.name} ${project.version}"
        options.header      = "${project.name} ${project.version}"
    }

    @Override
    protected AbstractFeature getParentFeature() {
        return project.rootProject.extensions.getByType(ProjectConfigurationExtension).docs.javadoc
    }

    private String resolveJavadocLinks(Object jv) {
        JavaVersion javaVersion = JavaVersion.current()

        if (jv instanceof JavaVersion) {
            javaVersion = (JavaVersion) jv
        } else if (jv != null) {
            try {
                javaVersion = JavaVersion.toVersion(jv)
            } catch (Exception ignored) {
                // javaVersion will be JavaVersion.current()
            }
        }

        if (javaVersion.isJava11Compatible()) {
            return "https://docs.oracle.com/en/java/javase/${javaVersion.majorVersion}/docs/api/".toString()
        }
        return "https://docs.oracle.com/javase/${javaVersion.majorVersion}/docs/api/"
    }

    @Override
    Map<String, Map<String, Object>> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

        List<String> links = []
        options.links.each { link ->
            links << link
        }

        map['empty'] = getEmpty()
        map['copyDocFiles'] = getCopyDocFiles()
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

        if (isRoot()) {
            map.putAll(aggregate.toMap())
        }

        new LinkedHashMap<>(['javadoc': map])
    }

    boolean getEmpty() {
        this.@empty != null && this.@empty
    }

    boolean getCopyDocFiles() {
        this.@copyDocFiles == null || this.@copyDocFiles
    }

    @Override
    protected boolean hasBasePlugin(Project project) {
        project.pluginManager.hasPlugin('java')
    }

    @Override
    void postMerge() {
        super.postMerge()
        if (autoLinks.enabled) {
            options.links(resolveJavadocLinks(project.findProperty('sourceCompatibility') ||
                project.findProperty('targetCompatibility')))
        }
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

    void options(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ExtStandardJavadocDocletOptions) Closure<Void> action) {
        ConfigureUtil.configure(action, options)
    }

    void aggregate(Action<? super Aggregate> action) {
        action.execute(aggregate)
    }

    void aggregate(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Aggregate) Closure<Void> action) {
        ConfigureUtil.configure(action, aggregate)
    }

    static void merge(Javadoc o1, Javadoc o2) {
        AbstractFeature.merge(o1, o2)
        o1.setEmpty(o1.@empty != null ? o1.getEmpty() : o2.getEmpty())
        o1.setCopyDocFiles(o1.@copyDocFiles != null ? o1.getCopyDocFiles() : o2.getCopyDocFiles())
        o1.excludes = CollectionUtils.merge(o1.excludes, o2.excludes, false)
        o1.includes = CollectionUtils.merge(o1.includes, o2.includes, false)
        o1.title = o1.title ?: o2.title
        ExtStandardJavadocDocletOptions.merge(o1.options, o2.options)
        AutoLinks.merge(o1.autoLinks, o2.autoLinks)
        Aggregate.merge(o1.aggregate, o2.aggregate)
    }

    void autoLinks(Action<? super AutoLinks> action) {
        action.execute(autoLinks)
    }

    void autoLinks(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = AutoLinks) Closure<Void> action) {
        ConfigureUtil.configure(action, autoLinks)
    }

    void applyTo(org.gradle.api.tasks.javadoc.Javadoc javadoc) {
        javadoc.title = title
        javadoc.getIncludes().addAll(includes)
        javadoc.getExcludes().addAll(excludes)
        options.applyTo(javadoc.options)
        autoLinks.applyTo(javadoc.options)
    }

    @CompileStatic
    static class AutoLinks {
        private static final List<String> DEFAULT_CONFIGURATIONS = [
            'api', 'implementation', 'compileOnly', 'annotationProcessor', 'runtimeOnly'
        ]

        Boolean useJavadocIo
        boolean enabled = true
        Set<String> excludes = new LinkedHashSet<>()
        List<String> configurations = []
        Map<String, String> offlineLinks = [:]

        private boolean enabledSet
        private final ProjectConfigurationExtension config

        AutoLinks(ProjectConfigurationExtension config) {
            this.config = config
        }

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

        void offlineLink(String url1, String url2) {
            offlineLinks[url1] = url2
        }

        boolean getUseJavadocIo() {
            null == useJavadocIo || useJavadocIo
        }

        static void merge(AutoLinks o1, AutoLinks o2) {
            o1.setEnabled((boolean) (o1.enabledSet ? o1.enabled : o2.enabled))
            o1.useJavadocIo = o1.useJavadocIo != null ? o1.getUseJavadocIo() : o2.getUseJavadocIo()
            o1.@excludes = CollectionUtils.merge(o1.@excludes, o2?.excludes, false)
            o1.@configurations = CollectionUtils.merge(o1.@configurations, o2?.configurations, false)
            o1.@offlineLinks = CollectionUtils.merge(o1.@offlineLinks, o2?.offlineLinks, false)
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<String, Object>(enabled: enabled)

            if (enabled) {
                List<String> cs = new ArrayList<>(configurations)
                if (!cs) {
                    cs = DEFAULT_CONFIGURATIONS
                }
                map.excludes = excludes
                map.useJavadocIo = getUseJavadocIo()
                map.configurations = cs
                map.offlineLinks = offlineLinks
            }

            map
        }

        List<String> resolveLinks(Project project) {
            List<String> links = []

            if (!enabled) return links

            List<String> cs = new ArrayList<>(configurations)
            if (!cs) {
                cs = DEFAULT_CONFIGURATIONS
            }

            for (String cn : cs) {
                Configuration c = project.configurations.findByName(cn)
                c?.dependencies?.each { Dependency dep ->
                    if (dep instanceof ProjectDependency) {
                        ProjectDependency pdep = (ProjectDependency) dep
                        String packageListLoc = calculateLocalJavadocLink(config.project, pdep.dependencyProject)
                        String extDocUrl = config.release ? calculateRemoteJavadocLink(pdep.group, pdep.name, pdep.version) : packageListLoc
                        offlineLink(extDocUrl, packageListLoc)
                    } else {
                        String artifactName = "${dep.name}-${dep.version}".toString()
                        if (!isExcluded(artifactName) && dep.name != 'unspecified' &&
                            isNotBlank(dep.group) && isNotBlank(dep.version)) {
                            links << calculateRemoteJavadocLink(dep.group, dep.name, dep.version)
                        }
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

        private String calculateRemoteJavadocLink(String group, String name, String version) {
            if (group == 'javax' && name == 'javaee-api' && version.matches('[567]\\..*')) {
                'https://docs.oracle.com/javaee/' + version[0, 1] + '/api/'
            } else if (group == 'javax' && name == 'javaee-api' && version.startsWith('8')) {
                'https://javaee.github.io/javaee-spec/javadocs/'
            } else if (group == 'org.springframework' && name.startsWith('spring-')) {
                'https://docs.spring.io/spring/docs/' + version + '/javadoc-api/'
            } else if (group == 'org.springframework.boot' && name.startsWith('spring-boot')) {
                'https://docs.spring.io/spring-boot/docs/' + version + '/api/'
            } else if (group == 'org.springframework.security' && name.startsWith('spring-security')) {
                'https://docs.spring.io/spring-security/site/docs/' + version + '/api/'
            } else if (group == 'org.springframework.data' && name == 'spring-data-jpa') {
                'https://docs.spring.io/spring-data/jpa/docs/' + version + '/api/'
            } else if (group == 'org.springframework.webflow' && name == 'spring-webflow') {
                'https://docs.spring.io/spring-webflow/docs/' + version + '/api/'
            } else if (group == 'com.squareup.okio' && version.startsWith('1.')) {
                'https://square.github.io/okio/1.x/' + name + '/'
            } else if (group == 'com.squareup.okhttp3') {
                'https://square.github.io/okhttp/3.x/' + name + '/'
            } else if (group == 'org.hibernate' && name == 'hibernate-core') {
                'https://docs.jboss.org/hibernate/orm/' + version[0, 3] + '/javadocs/'
            } else if ((group == 'org.hibernate' || group == 'org.hibernate.validator') && name == 'hibernate-validator') {
                'https://docs.jboss.org/hibernate/validator/' + version[0, 3] + '/api/'
            } else if (group == 'org.eclipse.jetty') {
                'https://www.eclipse.org/jetty/javadoc/' + version + '/'
            } else if (group == 'org.ow2.asm') {
                'https://asm.ow2.io/javadoc/'
            } else if (group.startsWith('org.apache.tomcat')) {
                'https://tomcat.apache.org/tomcat-' + version[0, 3] + '-doc/api/'
            } else if (getUseJavadocIo()) {
                "https://static.javadoc.io/${group}/${name}/${version}/".toString()
            } else {
                String normalizedGroup = group.replace('.', '/')
                "https://oss.sonatype.org/service/local/repositories/releases/archive/$normalizedGroup/$name/$version/$name-$version-javadoc.jar/!/".toString()
            }
        }

        void applyTo(MinimalJavadocOptions options) {
            if (options instanceof StandardJavadocDocletOptions) {
                StandardJavadocDocletOptions soptions = (StandardJavadocDocletOptions) options
                offlineLinks.each { String url1, String url2 -> soptions.linksOffline(url1, url2) }
            }
        }

        @CompileDynamic
        private String calculateLocalJavadocLink(Project dependentProject, Project project) {
            ProjectConfigurationExtension config = resolveConfig(project)

            Task taskDependency = null
            if (config.docs.javadoc.enabled) {
                taskDependency = project.tasks.findByName('javadoc')
            }

            if (config.docs.groovydoc.enabled && config.docs.groovydoc.replaceJavadoc) {
                taskDependency = project.tasks.findByName('groovydoc')
            }

            // Android projects don't have a 'javadoc' task
            if (taskDependency) {
                dependentProject.tasks.findByName('javadoc')?.dependsOn(taskDependency)
                taskDependency.destinationDir.absolutePath.replace('\\', '/')
            }
        }
    }

    @CompileStatic
    static class Aggregate {
        Boolean enabled
        Boolean empty
        Boolean fast
        final Set<Project> excludedProjects = new LinkedHashSet<>()

        private final ProjectConfigurationExtension config
        private final Project project

        Aggregate(ProjectConfigurationExtension config, Project project) {
            this.config = config
            this.project = project
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<String, Object>()

            map.enabled = getEnabled()
            map['empty'] = getEmpty()
            map.fast = getFast()
            map.excludedProjects = excludedProjects

            new LinkedHashMap<>('aggregate': map)
        }

        boolean getEnabled() {
            this.@enabled == null || this.@enabled
        }

        boolean getEmpty() {
            this.@empty != null && this.@empty
        }

        boolean getFast() {
            this.@fast == null || this.@fast
        }

        static Aggregate merge(Aggregate o1, Aggregate o2) {
            o1.enabled = o1.@enabled != null ? o1.getEnabled() : o2.getEnabled()
            o1.empty = o1.@empty != null ? o1.getEmpty() : o2.getEmpty()
            o1.fast = o1.@fast != null ? o1.getFast() : o2.getFast()
            o1
        }
    }
}
