package net.wasdev.wlp.ant.types;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class EmbeddedServerInfo {
    
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
        if (this.serverName.equals(info.serverName)
                && this.userDir.getAbsolutePath().equals(info.userDir.getAbsolutePath())
                && this.outputDir.getAbsolutePath().equals(info.outputDir.getAbsolutePath())) {
            return true;
        }
        return false;
    }

}
