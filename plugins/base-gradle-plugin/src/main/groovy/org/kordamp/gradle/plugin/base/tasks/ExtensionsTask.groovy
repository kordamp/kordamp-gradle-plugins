package org.kordamp.gradle.plugin.base.tasks

import org.gradle.api.plugins.ExtensionsSchema
import org.gradle.api.tasks.TaskAction

/**
 * @author Andres Almiray
 * @since 0.11.0
 */
class ExtensionsTask extends AbstractReportingTask {
    @TaskAction
    void report() {
        Map<String, Map<String, Object>> extensions = [:]

        project.extensions.extensionsSchema.elements.eachWithIndex { extension, index -> extensions.putAll(ExtensionsTask.doReport(extension, index)) }

        doPrint(extensions, 0)
    }

    private static Map<String, Map<String, Object>> doReport(ExtensionsSchema.ExtensionSchema extension, int index) {
        Map<String, Object> map = [:]

        map.name = extension.name
        map.type = extension.publicType.toString()

        [('extension ' + index): map]
    }
}
