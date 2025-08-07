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

import groovy.json.JsonSlurper
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

/**
 * HTTP client for Maven Central Publisher API
 *
 * @author Andres Almiray
 * @since 0.55.0
 */
@CompileStatic
class MavenCentralClient {
    private static final Logger LOGGER = Logging.getLogger(MavenCentralClient)
    private static final String BASE_URL = 'https://central.sonatype.com/api/v1/publisher'
    
    private final String username
    private final String password
    private final String userToken
    private final int timeoutSeconds
    
    MavenCentralClient(String username, String password, int timeoutSeconds = 300) {
        this.username = username
        this.password = password
        this.userToken = Base64.encoder.encodeToString("${username}:${password}".bytes)
        this.timeoutSeconds = timeoutSeconds
    }
    
    /**
     * Upload a deployment bundle to Maven Central
     * @param bundlePath Path to the ZIP bundle file
     * @param deploymentName Optional name for the deployment
     * @param publishingType 'AUTOMATIC' or 'USER_MANAGED'
     * @return Deployment ID
     */
    @CompileDynamic
    String uploadBundle(Path bundlePath, String deploymentName = null, String publishingType = 'USER_MANAGED') {
        LOGGER.info("Uploading bundle: ${bundlePath}")
        
        URL url = new URL("${BASE_URL}/upload")
        HttpURLConnection connection = (HttpURLConnection) url.openConnection()
        
        try {
            connection.setRequestMethod('POST')
            connection.setDoOutput(true)
            connection.setRequestProperty('Authorization', "Bearer ${userToken}")
            connection.setConnectTimeout(timeoutSeconds * 1000)
            connection.setReadTimeout(timeoutSeconds * 1000)
            
            String boundary = "----WebKitFormBoundary${System.currentTimeMillis()}"
            connection.setRequestProperty('Content-Type', "multipart/form-data; boundary=${boundary}")
            
            try (OutputStream outputStream = connection.outputStream;
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, 'UTF-8'), true)) {
                
                // Add bundle file
                writer.println("--${boundary}")
                writer.println('Content-Disposition: form-data; name="bundle"; filename="bundle.zip"')
                writer.println('Content-Type: application/zip')
                writer.println()
                writer.flush()
                
                Files.copy(bundlePath, outputStream)
                outputStream.flush()
                writer.println()
                
                // Add publishingType
                writer.println("--${boundary}")
                writer.println('Content-Disposition: form-data; name="publishingType"')
                writer.println()
                writer.println(publishingType)
                
                // Add deployment name if provided
                if (deploymentName) {
                    writer.println("--${boundary}")
                    writer.println('Content-Disposition: form-data; name="name"')
                    writer.println()
                    writer.println(deploymentName)
                }
                
                writer.println("--${boundary}--")
                writer.flush()
            }
            
            int responseCode = connection.responseCode
            String responseBody = readResponse(connection)
            
            if (responseCode == 201) {
                def response = new JsonSlurper().parseText(responseBody)
                String deploymentId = response.deploymentId
                LOGGER.info("Bundle uploaded successfully. Deployment ID: ${deploymentId}")
                return deploymentId
            } else {
                throw new RuntimeException("Upload failed with code ${responseCode}: ${responseBody}")
            }
            
        } finally {
            connection.disconnect()
        }
    }
    
    /**
     * Get deployment status
     * @param deploymentId The deployment ID
     * @return Deployment status information
     */
    @CompileDynamic
    DeploymentStatus getDeploymentStatus(String deploymentId) {
        LOGGER.info("Getting deployment status for: ${deploymentId}")
        
        URL url = new URL("${BASE_URL}/status?id=${deploymentId}")
        HttpURLConnection connection = (HttpURLConnection) url.openConnection()
        
        try {
            connection.setRequestMethod('GET')
            connection.setRequestProperty('Authorization', "Bearer ${userToken}")
            connection.setConnectTimeout(timeoutSeconds * 1000)
            connection.setReadTimeout(timeoutSeconds * 1000)
            
            int responseCode = connection.responseCode
            String responseBody = readResponse(connection)
            
            if (responseCode == 200) {
                def response = new JsonSlurper().parseText(responseBody)
                return new DeploymentStatus(
                    deploymentId: response.deploymentId,
                    deploymentName: response.deploymentName,
                    deploymentState: response.deploymentState,
                    purls: response.purls as List<String>,
                    errors: response.errors as List<String>
                )
            } else {
                throw new RuntimeException("Status check failed with code ${responseCode}: ${responseBody}")
            }
            
        } finally {
            connection.disconnect()
        }
    }
    
    /**
     * Publish a deployment
     * @param deploymentId The deployment ID
     */
    void publishDeployment(String deploymentId) {
        LOGGER.info("Publishing deployment: ${deploymentId}")
        
        URL url = new URL("${BASE_URL}/deployment/${deploymentId}/publish")
        HttpURLConnection connection = (HttpURLConnection) url.openConnection()
        
        try {
            connection.setRequestMethod('POST')
            connection.setRequestProperty('Authorization', "Bearer ${userToken}")
            connection.setConnectTimeout(timeoutSeconds * 1000)
            connection.setReadTimeout(timeoutSeconds * 1000)
            
            int responseCode = connection.responseCode
            String responseBody = readResponse(connection)
            
            if (responseCode == 204) {
                LOGGER.info("Deployment published successfully")
            } else {
                throw new RuntimeException("Publish failed with code ${responseCode}: ${responseBody}")
            }
            
        } finally {
            connection.disconnect()
        }
    }
    
    /**
     * Drop a deployment
     * @param deploymentId The deployment ID
     */
    void dropDeployment(String deploymentId) {
        LOGGER.info("Dropping deployment: ${deploymentId}")
        
        URL url = new URL("${BASE_URL}/deployment/${deploymentId}/drop")
        HttpURLConnection connection = (HttpURLConnection) url.openConnection()
        
        try {
            connection.setRequestMethod('POST')
            connection.setRequestProperty('Authorization', "Bearer ${userToken}")
            connection.setConnectTimeout(timeoutSeconds * 1000)
            connection.setReadTimeout(timeoutSeconds * 1000)
            
            int responseCode = connection.responseCode
            String responseBody = readResponse(connection)
            
            if (responseCode == 204) {
                LOGGER.info("Deployment dropped successfully")
            } else {
                throw new RuntimeException("Drop failed with code ${responseCode}: ${responseBody}")
            }
            
        } finally {
            connection.disconnect()
        }
    }
    
    private String readResponse(HttpURLConnection connection) {
        InputStream inputStream = connection.responseCode < 400 ? 
            connection.inputStream : connection.errorStream
        
        if (inputStream == null) {
            return ""
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, 'UTF-8'))) {
            StringBuilder response = new StringBuilder()
            String line
            while ((line = reader.readLine()) != null) {
                response.append(line).append('\n')
            }
            return response.toString()
        }
    }
    
    @CompileStatic
    static class DeploymentStatus {
        String deploymentId
        String deploymentName
        String deploymentState
        List<String> purls
        List<String> errors
        
        boolean isValidated() {
            return deploymentState == 'VALIDATED'
        }
        
        boolean isPublished() {
            return deploymentState == 'PUBLISHED'
        }
        
        boolean isFailed() {
            return deploymentState == 'FAILED'
        }
        
        boolean hasPendingValidation() {
            return deploymentState == 'PENDING'
        }
    }
}