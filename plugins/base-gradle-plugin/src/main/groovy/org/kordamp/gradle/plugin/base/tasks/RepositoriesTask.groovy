package org.kordamp.gradle.plugin.base.tasks

import groovy.transform.CompileStatic
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.artifacts.repositories.FlatDirectoryArtifactRepository
import org.gradle.api.artifacts.repositories.IvyArtifactRepository
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.tasks.TaskAction

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.11.0
 */
class RepositoriesTask extends AbstractReportingTask {
    @TaskAction
    void report() {
        Map<String, Map<String, Object>> repositories = [:]

        project.repositories.eachWithIndex { repository, index -> repositories.putAll(RepositoriesTask.doReport(repository, index)) }

        doPrint(repositories, 0)
    }

    @CompileStatic
    private static Map<String, Map<String, Object>> doReport(ArtifactRepository repository, int index) {
        Map<String, Object> map = [:]

        if (repository instanceof MavenArtifactRepository) {
            map = maven((MavenArtifactRepository) repository)
        } else if (repository instanceof IvyArtifactRepository) {
            map = ivy((IvyArtifactRepository) repository)
        } else if (repository instanceof FlatDirectoryArtifactRepository) {
            map = flatDir((FlatDirectoryArtifactRepository) repository)
        }

        [('repository ' + index): map]
    }

    private static Map<String, Object> maven(MavenArtifactRepository repository) {
        Map<String, Object> map = [type: 'maven']

        if (!isBlank(repository.name)) {
            map.name = repository.name
        }
        map.url = repository.url
        map.artifactUrls = repository.artifactUrls

        map
    }

    private static Map<String, Object> ivy(IvyArtifactRepository repository) {
        Map<String, Object> map = [type: 'ivy']

        if (!isBlank(repository.name)) {
            map.name = repository.name
        }
        map.url = repository.url

        map
    }

    private static Map<String, Object> flatDir(FlatDirectoryArtifactRepository repository) {
        Map<String, Object> map = [type: 'flatDir']

        if (!isBlank(repository.name)) {
            map.name = repository.name
        }
        map.dirs = repository.dirs

        map
    }
}
