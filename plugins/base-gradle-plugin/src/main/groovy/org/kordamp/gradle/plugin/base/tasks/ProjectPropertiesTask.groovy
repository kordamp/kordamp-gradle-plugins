package org.kordamp.gradle.plugin.base.tasks

import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

/**
 * @author Andres Almiray
 * @since 0.11.0
 */
class ProjectPropertiesTask extends AbstractReportingTask {
    private String section

    @Option(option = 'section', description = 'The section to generate the report for.')
    void setSection(String section) {
        this.section = section
    }

    @TaskAction
    void report() {
        Map<String, Object> map = resolveProperties()

        if (section) {
            printSection(map, section)
        } else {
            doPrint(map, 0)
        }
    }

    private void printSection(Map<String, Object> map, String section) {
        if (map.containsKey(section)) {
            println "${section}:"
            doPrint(map[section], 1)
        } else {
            throw new IllegalStateException("Unknown section '$section'")
        }
    }

    private Map<String, Map<String, Object>> resolveProperties() {
        Map<String, Map<String, Object>> props = [:]
        props.project = [
            name        : project.name,
            version     : project.version,
            group       : project.group,
            path        : project.path,
            description : project.description,
            displayName : project.displayName,
            projectDir  : project.projectDir,
            buildFile   : project.buildFile.absolutePath,
            buildDir    : project.buildDir,
            defaultTasks: project.defaultTasks
        ]

        props.ext = new TreeMap<>()
        project.extensions.findByType(ExtraPropertiesExtension).properties.each { key, value ->
            if (key == 'mergedConfiguration' || key =~ /org_kordamp_gradle_plugin_.+_VISITED/) {
                return
            }
            props.ext[key] = value
        }

        props
    }
}
