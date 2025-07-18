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
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.build.event.BuildEventsListenerRegistry
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener
import org.gradle.tooling.events.task.TaskFinishEvent
import org.kordamp.gradle.plugin.base.plugins.Publishing

import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Gradle build service for Maven Central publishing
 *
 * @author Andres Almiray
 * @since 0.55.0
 */
@CompileStatic
abstract class MavenCentralBuildService implements BuildService<MavenCentralBuildService.Parameters>, OperationCompletionListener {
    private static final Logger LOGGER = Logging.getLogger(MavenCentralBuildService)
    
    private final Map<String, MavenCentralProject> projects = new ConcurrentHashMap<>()
    private final Queue<String> deploymentsToPublish = new ConcurrentLinkedQueue<>()
    private final Queue<String> deploymentsToDrop = new ConcurrentLinkedQueue<>()
    
    @Lazy
    private MavenCentralClient client = new MavenCentralClient(
        parameters.username.get(),
        parameters.password.get(),
        parameters.timeoutSeconds.get()
    )
    
    interface Parameters extends BuildServiceParameters {
        Property<String> getUsername()
        Property<String> getPassword()
        Property<Integer> getTimeoutSeconds()
        Property<File> getBuildDirectory()
    }
    
    /**
     * Register a project for Maven Central publishing
     * @param project The Gradle project
     * @param publication The Maven publication
     * @param config The publishing configuration
     */
    void registerProject(Project project, MavenPublication publication, Publishing.MavenCentral config) {
        String projectKey = "${project.group}:${project.name}"
        
        MavenCentralProject mcProject = new MavenCentralProject(
            project: project,
            publication: publication,
            config: config
        )
        
        projects.put(projectKey, mcProject)
        LOGGER.info("Registered project for Maven Central publishing: ${projectKey}")
    }
    
    /**
     * Enable automatic publishing for a deployment
     * @param deploymentId The deployment ID
     */
    void enableAutomaticPublishing(String deploymentId) {
        deploymentsToPublish.offer(deploymentId)
        LOGGER.info("Enabled automatic publishing for deployment: ${deploymentId}")
    }
    
    /**
     * Drop a deployment
     * @param deploymentId The deployment ID
     */
    void dropDeployment(String deploymentId) {
        deploymentsToDrop.offer(deploymentId)
        LOGGER.info("Marked deployment for dropping: ${deploymentId}")
    }
    
    @Override
    void onFinish(FinishEvent event) {
        if (event instanceof TaskFinishEvent) {
            TaskFinishEvent taskEvent = (TaskFinishEvent) event
            if (taskEvent.descriptor.name == ':publish' || taskEvent.descriptor.name.endsWith('publish')) {
                runEndOfBuildActions()
            }
        }
    }
    
    /**
     * Run end-of-build actions (upload, publish, drop)
     */
    void runEndOfBuildActions() {
        if (projects.isEmpty()) {
            LOGGER.info("No projects registered for Maven Central publishing")
            return
        }
        
        LOGGER.info("Running Maven Central end-of-build actions")
        
        // First, drop any deployments that need dropping
        processDeploymentsToDrop()
        
        // Then upload and publish projects
        processProjectsForPublishing()
        
        // Finally, publish any deployments that need publishing
        processDeploymentsToPublish()
    }
    
    private void processDeploymentsToDrop() {
        while (!deploymentsToDrop.isEmpty()) {
            String deploymentId = deploymentsToDrop.poll()
            try {
                client.dropDeployment(deploymentId)
                LOGGER.info("Dropped deployment: ${deploymentId}")
            } catch (Exception e) {
                LOGGER.error("Failed to drop deployment ${deploymentId}", e)
            }
        }
    }
    
    private void processProjectsForPublishing() {
        projects.each { String projectKey, MavenCentralProject mcProject ->
            try {
                publishProject(mcProject)
            } catch (Exception e) {
                LOGGER.error("Failed to publish project ${projectKey}", e)
            }
        }
    }
    
    private void processDeploymentsToPublish() {
        while (!deploymentsToPublish.isEmpty()) {
            String deploymentId = deploymentsToPublish.poll()
            try {
                // Wait for validation to complete
                waitForValidation(deploymentId)
                
                // Then publish
                client.publishDeployment(deploymentId)
                LOGGER.info("Published deployment: ${deploymentId}")
            } catch (Exception e) {
                LOGGER.error("Failed to publish deployment ${deploymentId}", e)
            }
        }
    }
    
    private void publishProject(MavenCentralProject mcProject) {
        LOGGER.info("Publishing project: ${mcProject.project.name}")
        
        // Validate artifacts
        MavenCentralBundleCreator bundleCreator = new MavenCentralBundleCreator(mcProject.project)
        List<String> missing = bundleCreator.validateArtifacts(mcProject.publication)
        
        if (!missing.isEmpty()) {
            LOGGER.warn("Missing required artifacts for ${mcProject.project.name}: ${missing.join(', ')}")
            return
        }
        
        // Create bundle
        File bundleDir = new File(parameters.buildDirectory.get(), "maven-central/${mcProject.project.name}")
        bundleDir.mkdirs()
        
        Path bundlePath = bundleCreator.createBundle(mcProject.publication, bundleDir)
        
        // Upload bundle
        String deploymentName = "${mcProject.project.name}-${mcProject.project.version}"
        String deploymentId = client.uploadBundle(bundlePath, deploymentName, mcProject.config.publishingType)
        
        // Handle automatic publishing
        if (mcProject.config.publishingType == 'AUTOMATIC') {
            enableAutomaticPublishing(deploymentId)
        } else {
            LOGGER.info("Deployment ${deploymentId} uploaded. Manual publishing required.")
        }
    }
    
    private void waitForValidation(String deploymentId) {
        LOGGER.info("Waiting for validation of deployment: ${deploymentId}")
        
        int attempts = 0
        int maxAttempts = 60 // 5 minutes with 5-second intervals
        
        while (attempts < maxAttempts) {
            try {
                MavenCentralClient.DeploymentStatus status = client.getDeploymentStatus(deploymentId)
                
                if (status.isValidated()) {
                    LOGGER.info("Deployment ${deploymentId} validated successfully")
                    return
                } else if (status.isFailed()) {
                    String errors = status.errors ? status.errors.join(', ') : 'Unknown error'
                    throw new RuntimeException("Deployment ${deploymentId} failed validation: ${errors}")
                } else if (status.hasPendingValidation()) {
                    LOGGER.info("Deployment ${deploymentId} still validating... (attempt ${attempts + 1}/${maxAttempts})")
                    Thread.sleep(5000) // Wait 5 seconds
                    attempts++
                } else {
                    LOGGER.info("Deployment ${deploymentId} in state: ${status.deploymentState}")
                    Thread.sleep(5000)
                    attempts++
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt()
                throw new RuntimeException("Interrupted while waiting for validation", e)
            }
        }
        
        throw new RuntimeException("Deployment ${deploymentId} validation timed out after ${maxAttempts} attempts")
    }
    
    @CompileStatic
    static class MavenCentralProject {
        Project project
        MavenPublication publication
        Publishing.MavenCentral config
    }
}