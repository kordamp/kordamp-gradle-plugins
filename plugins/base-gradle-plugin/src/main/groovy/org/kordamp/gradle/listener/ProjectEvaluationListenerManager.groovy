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
package org.kordamp.gradle.listener

import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.BuildResult
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.ProjectEvaluationListener
import org.gradle.api.ProjectState
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.invocation.Gradle
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

import static org.kordamp.gradle.util.AnnotationUtils.sortByDependencies

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
@CompileStatic
class ProjectEvaluationListenerManager {
    private static final Logger LOG = LoggerFactory.getLogger(Project)

    private final Map<Project, List<ProjectEvaluatedListener>> projectEvaluatedListeners = new ConcurrentHashMap<>()
    private final List<AllProjectsEvaluatedListener> allProjectsEvaluatedListeners = new CopyOnWriteArrayList<>()
    private final List<TaskGraphReadyListener> taskGraphReadyListeners = new CopyOnWriteArrayList<>()

    private static Map<Gradle, ProjectEvaluationListenerManager> instances = [:]

    static void register(Gradle gradle) {
        if (!instances.get(gradle)) {
            ProjectEvaluationListenerManager instance = new ProjectEvaluationListenerManager()
            instances.put(gradle, instance)
            gradle.addListener(instance.listener)
            gradle.taskGraph.whenReady(new Action<TaskExecutionGraph>() {
                @Override
                void execute(TaskExecutionGraph graph) {
                    instance.fireTaskGraphReadyListener(gradle.rootProject, graph)
                }
            })
        }
    }

    static void addProjectEvaluatedListener(Project project, ProjectEvaluatedListener listener) {
        if (listener) {
            List<ProjectEvaluatedListener> listeners = instances.get(project.rootProject.gradle).projectEvaluatedListeners
                .computeIfAbsent(project, { k -> new CopyOnWriteArrayList<>() })
            if (!listeners.contains(listener)) {
                listeners.add(listener)
            }
        }
    }

    static void addAllProjectsEvaluatedListener(Project rootProject, AllProjectsEvaluatedListener listener) {
        if (listener && !instances.get(rootProject.gradle).allProjectsEvaluatedListeners.contains(listener)) {
            instances.get(rootProject.gradle).allProjectsEvaluatedListeners.add(listener)
        }
    }

    static void addTaskGraphReadyListener(Project rootProject, TaskGraphReadyListener listener) {
        if (listener && !instances.get(rootProject.gradle).taskGraphReadyListeners.contains(listener)) {
            instances.get(rootProject.gradle).taskGraphReadyListeners.add(listener)
        }
    }

    private Listener listener = new Listener()

    private void fireTaskGraphReadyListener(Project rootProject, TaskExecutionGraph graph) {
        Map<String, TaskGraphReadyListener> sortedListeners = sortByDependencies('task-graph', taskGraphReadyListeners, '', 'listener')
        for (TaskGraphReadyListener listener : sortedListeners.values()) {
            LOG.debug('[task-graph] Invoking listener {}', listener)
            listener.taskGraphReady(rootProject, graph)
        }
    }

    private class Listener extends BuildAdapter implements ProjectEvaluationListener {
        @Override
        void beforeEvaluate(Project project) {
            // noop
        }

        @Override
        void afterEvaluate(Project project, ProjectState projectState) {
            List<ProjectEvaluatedListener> listeners = projectEvaluatedListeners.get(project)
            if (!listeners) return
            Map<String, ProjectEvaluatedListener> sortedListeners = sortByDependencies('project-evaluated:' + project.name, listeners, '', 'listener')
            for (ProjectEvaluatedListener listener : sortedListeners.values()) {
                LOG.debug('[project-evaluated:{}] Invoking listener {}', project.name, listener)
                listener.projectEvaluated(project)
            }
        }

        @Override
        void projectsEvaluated(Gradle gradle) {
            Map<String, AllProjectsEvaluatedListener> sortedListeners = sortByDependencies('all-projects-evaluated', allProjectsEvaluatedListeners, '', 'listener')
            for (AllProjectsEvaluatedListener listener : sortedListeners.values()) {
                LOG.debug('[all-projects-evaluated] Invoking listener {}', listener)
                listener.allProjectsEvaluated(gradle.rootProject)
            }
        }

        @Override
        void buildFinished(BuildResult result) {
            projectEvaluatedListeners.clear()
            allProjectsEvaluatedListeners.clear()
            taskGraphReadyListeners.clear()
            instances.remove(result.gradle)
        }
    }
}
