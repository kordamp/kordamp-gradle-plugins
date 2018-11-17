/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kordamp.gradle.plugin.base.plugins;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public interface Kotlindoc extends Feature {
    String getModuleName();

    List<String> getOutputFormats();

    File getOutputDirectory();

    List<Object> getIncludes();

    List<Object> getSamples();

    int getJdkVersion();

    String getCacheRoot();

    String getLanguageVersion();

    String getApiVersion();

    boolean isIncludeNonPublic();

    boolean isSkipDeprecated();

    boolean isReportUndocumented();

    boolean isSkipEmptyPackages();

    boolean isNoStdlibLink();

    Set<String> getImpliedPlatforms();

    LinkMappingSet getLinkMappings();

    ExternalDocumentationLinkSet getExternalDocumentationLinks();

    PackageOptionSet getPackageOptions();

    boolean isReplaceJavadoc();

    interface LinkMappingSet {
        List<LinkMapping> getLinkMappings();

        List<LinkMapping> resolveLinkMappings();
    }

    interface LinkMapping {
        boolean isEmpty();

        String getDir();

        String getPath();

        String getUrl();

        String getSuffix();
    }

    interface ExternalDocumentationLinkSet {
        List<ExternalDocumentationLink> getExternalDocumentationLinks();

        List<ExternalDocumentationLink> resolveExternalDocumentationLinks();
    }

    interface ExternalDocumentationLink {
        boolean isEmpty();

        String getUrl();

        String getPackageListUrl();
    }

    interface PackageOptionSet {
        List<PackageOption> getPackageOptions();

        List<PackageOption> resolvePackageOptions();
    }

    interface PackageOption {
        boolean isEmpty();

        String getPrefix();

        boolean isIncludeNonPublic();

        boolean isReportUndocumented();

        boolean isSkipDeprecated();

        boolean isSuppress();
    }
}
