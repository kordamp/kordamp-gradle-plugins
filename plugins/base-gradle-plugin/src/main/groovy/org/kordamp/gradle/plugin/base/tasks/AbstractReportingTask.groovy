package org.kordamp.gradle.plugin.base.tasks

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.11.0
 */
@CompileStatic
abstract class AbstractReportingTask extends DefaultTask {
    protected static void doPrint(Map<String, Object> map, int offset) {
        map.each { key, value ->
            if (value instanceof Map) {
                if (!value.isEmpty()) {
                    println(('    ' * offset) + key + ':')
                    doPrint(value, offset + 1)
                }
            } else if (value instanceof Collection) {
                if (!value.isEmpty()) {
                    println(('    ' * offset) + key + ':')
                    doPrint((Collection) value, offset + 1)
                }
            } else if (isNotNullNorBlank(value)) {
                println(('    ' * offset) + key + ': ' + value)
            }

            if (offset == 0) {
                println(' ')
            }
        }
    }

    protected static void doPrint(Collection<Object> collection, int offset) {
        collection.each { value ->
            if (value instanceof Map) {
                if (!value.isEmpty()) {
                    doPrint(value, offset)
                }
            } else if (value instanceof Collection && !value.empty) {
                if (!value.isEmpty()) {
                    doPrint(value, offset + 1)
                }
            } else if (isNotNullNorBlank(value)) {
                println(('    ' * offset) + value)
            }
        }
    }

    protected static boolean isNotNullNorBlank(value) {
        value != null || (value instanceof CharSequence && !isBlank(String.valueOf(value)))
    }
}
