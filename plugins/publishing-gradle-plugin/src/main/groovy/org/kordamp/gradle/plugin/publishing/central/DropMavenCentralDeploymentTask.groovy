/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2025 Andres Almiray.
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
package org.kordamp.gradle.plugin.publishing.central

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import org.gradle.api.tasks.options.Option

/**
 * Task to drop Maven Central deployments
 *
 * @author Andres Almiray
 * @since 0.55.0
 */
@CompileStatic
@UntrackedTask(because = "Not worth tracking")
abstract class DropMavenCentralDeploymentTask extends DefaultTask {
    
    @Input
    abstract Property<MavenCentralBuildService> getBuildService()
    
    @Input
    abstract Property<String> getDeploymentId()
    
    @Option(option = "deployment-id", description = "The deployment ID to drop")
    void setDeploymentId(String deploymentId) {
        this.deploymentId.set(deploymentId)
    }
    
    @TaskAction
    void drop() {
        String id = deploymentId.get()
        if (!id) {
            throw new IllegalArgumentException("Deployment ID is required. Use --deployment-id=<id>")
        }
        
        logger.info("Dropping Maven Central deployment: ${id}")
        
        buildService.get().dropDeployment(id)
        
        logger.info("Maven Central deployment drop completed")
    }
}