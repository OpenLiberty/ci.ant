/**
 * (C) Copyright IBM Corporation 2019.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.wasdev.wlp.ant.types;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class EmbeddedServerInfo {

    private final String serverName;
    private final File userDir;
    private final File outputDir;

    public EmbeddedServerInfo(String serverName, File userDir, File outputDir) {
        this.serverName = serverName;
        this.userDir = userDir;
        this.outputDir = outputDir;
    }

    public String getServerName() {
        return serverName;
    }

    public File getUserDir() {
        return userDir;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public boolean equals(EmbeddedServerInfo info) {
        return 
            this.serverName.equals(info.serverName) &&
            areFilesEqual(this.userDir, info.userDir) &&
            areFilesEqual(this.outputDir, info.outputDir);
    }

    private boolean areFilesEqual(File file1, File file2) {
        if (file1 != null && file2 != null) {
           return file1.getAbsolutePath().equals(file2.getAbsolutePath());
        }
        return (file1 == null && file2 == null);
    }

    public static class EmbeddedServers {

        private static final Map<EmbeddedServerInfo, Object> EMBEDDED_SERVERS = new HashMap<EmbeddedServerInfo, Object>();

        public static Object get(EmbeddedServerInfo info) {
            // Look for the server based on the server info
            for (Entry<EmbeddedServerInfo, Object> entry : EMBEDDED_SERVERS.entrySet()) {
                EmbeddedServerInfo serverInfo = entry.getKey();
                if (serverInfo.equals(info)) {
                    return entry.getValue();
                }
            }
            // Return null if we didn't find it
            return null;
        }

        public static void put(EmbeddedServerInfo info, Object server) {
            EMBEDDED_SERVERS.put(info, server);
        }

    }

    public static class EmbeddedServerClassLoaders {

        private static final Map<URL, URLClassLoader> CLASSLOADERS = new HashMap<URL, URLClassLoader>();

        public static URLClassLoader get(URL url) {
            // Look for the classloader based on the server info
            for (Entry<URL, URLClassLoader> entry : CLASSLOADERS.entrySet()) {
                URL entryUrl = entry.getKey();
                if (url.equals(entryUrl)) {
                    return entry.getValue();
                }
            }
            // Return null if we didn't find it
            return null;
        }

        public static void put(URL url, URLClassLoader classloader) {
            CLASSLOADERS.put(url, classloader);
        }

    }

}
