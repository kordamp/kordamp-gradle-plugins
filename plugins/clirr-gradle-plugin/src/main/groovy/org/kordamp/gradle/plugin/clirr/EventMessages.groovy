/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
package org.kordamp.gradle.plugin.clirr

import net.sf.clirr.core.MessageTranslator

import java.util.jar.JarFile
import java.util.zip.ZipEntry

/**
 *
 * @author Andres Almiray
 * @since 0.12.0
 */
class EventMessages extends PropertyResourceBundle {
    static final String RESOURCE = "org/kordamp/gradle/plugin/clirr/EventMessages.properties"

    EventMessages() throws IOException {
        super(getInputStream())
    }

    EventMessages(Reader reader) throws IOException {
        super(new InputStreamReader(getInputStream()))
    }

    private static InputStream getInputStream() {
        return EventMessages.class.getResourceAsStream(RESOURCE)
    }

    static MessageTranslator getMessageTranslator() {
        // Ugly workaround to "force" loading of this particular RESOURCE

        ClassLoader cl = Thread.currentThread().contextClassLoader
        URL url = cl.getResource(RESOURCE)
        String path = url.toURI().toURL().path
        path = path.substring(0, path.indexOf('!'))
        File file = new File(new URI(path))
        JarFile jar = new JarFile(file)
        ZipEntry entry = jar.getEntry(RESOURCE)
        PropertyResourceBundle bundle = new PropertyResourceBundle(jar.getInputStream(entry))
        MessageTranslator translator = new MessageTranslator()
        translator.@messageText = bundle
        translator
    }
}
