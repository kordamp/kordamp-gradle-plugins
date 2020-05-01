/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2020 Andres Almiray.
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
package org.kordamp.gradle.plugin.base.internal

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.ProjectsSpec

import java.util.function.Function
import java.util.regex.Pattern

/**
 * @author Andres Almiray
 * @since 0.35.0
 */
@CompileStatic
final class ProjectsSpecImpl implements ProjectsSpec {
    final List<ConditionHelper> conditions = []

    @Override
    void condition(Function<? extends Project, Boolean> condition, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Project) Closure<Void> action) {
        if (condition && action) {
            conditions << new ConditionHelper(condition, action)
        }
    }

    @Override
    void dir(String dir, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Project) Closure<Void> action) {
        conditions << new ConditionHelper(dirFunction(dir), action)
    }

    @Override
    void dirs(List<String> dirs, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Project) Closure<Void> action) {
        conditions << new ConditionHelper(dirFunction(dirs), action)
    }

    @Override
    void path(String path, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Project) Closure<Void> action) {
        conditions << new ConditionHelper(pathFunction(path), action)
    }

    @Override
    void paths(List<String> paths, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Project) Closure<Void> action) {
        conditions << new ConditionHelper(pathFunction(paths), action)
    }

    @Override
    void condition(Function<? extends Project, Boolean> condition, Action<? super Project> action) {
        if (condition && action) {
            conditions << new ConditionHelper(condition, action)
        }
    }

    @Override
    void dir(String dir, Action<? super Project> action) {
        conditions << new ConditionHelper(dirFunction(dir), action)
    }

    @Override
    void dirs(List<String> dirs, Action<? super Project> action) {
        conditions << new ConditionHelper(dirFunction(dirs), action)
    }

    @Override
    void path(String path, Action<? super Project> action) {
        conditions << new ConditionHelper(pathFunction(path), action)
    }

    @Override
    void paths(List<String> paths, Action<? super Project> action) {
        conditions << new ConditionHelper(pathFunction(paths), action)
    }

    private Function<? extends Project, Boolean> dirFunction(String dir) {
        new Function<Project, Boolean>() {
            @Override
            Boolean apply(Project project) {
                dir == project.projectDir.parentFile.name
            }
        }
    }

    private Function<? extends Project, Boolean> dirFunction(List<String> dirs) {
        new Function<Project, Boolean>() {
            @Override
            Boolean apply(Project project) {
                for (String dir : dirs) {
                    if (dir == project.projectDir.parentFile.name) {
                        return true
                    }
                }
                false
            }
        }
    }

    private Function<? extends Project, Boolean> pathFunction(String path) {
        new Function<Project, Boolean>() {
            @Override
            Boolean apply(Project project) {
                path == project.path || Pattern.compile(asRegex(path)).matcher(project.path).matches()
            }
        }
    }

    private Function<? extends Project, Boolean> pathFunction(List<String> paths) {
        new Function<Project, Boolean>() {
            @Override
            Boolean apply(Project project) {
                for (String path : paths) {
                    if (path == project.path || Pattern.compile(asRegex(path)).matcher(project.path).matches()) {
                        return true
                    }
                }
                false
            }
        }
    }

    private static String asRegex(String wildcard) {
        StringBuilder result = new StringBuilder(wildcard.length())
        result.append('^')
        for (int index = 0; index < wildcard.length(); index++) {
            char character = wildcard.charAt(index)
            switch (character) {
                case '*':
                    result.append('.*')
                    break;
                case '?':
                    result.append('.')
                    break;
                case '$':
                case '(':
                case ')':
                case '.':
                case '[':
                case '\\':
                case ']':
                case '^':
                case '{':
                case '|':
                case '}':
                    result.append('\\')
                default:
                    result.append(character)
                    break;
            }
        }
        result.append('$')
        return result.toString()
    }

    Action<? super Project> asProjectConfigurer() {
        new Action<Project>() {
            @Override
            void execute(Project project) {
                for (ConditionHelper helper : conditions) {
                    helper.apply(project)
                }
            }
        }
    }

    private class ConditionHelper {
        public final Function<? extends Project, Boolean> condition
        public Closure<Void> closure
        public Action<? super Project> action

        ConditionHelper(Function<? extends Project, Boolean> condition, Closure<Void> closure) {
            this.condition = condition
            this.closure = closure
        }

        ConditionHelper(Function<? extends Project, Boolean> condition, Action<? super Project> action) {
            this.condition = condition
            this.action = action
        }

        void apply(Project project) {
            if (condition.apply(project)) {
                if (closure) {
                    closure.delegate = project
                    closure.call(project)
                } else {
                    action.execute(project)
                }
            }
        }
    }
}
