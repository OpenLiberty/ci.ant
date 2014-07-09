/**
 * (C) Copyright IBM Corporation 2014.
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
package net.wasdev.wlp.ant.install;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;

public class InstallUtils {

    private InstallUtils() {        
    }
    
    private static String readAsString(InputStream in, String encoding) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, encoding));
        String line = null;
        StringBuilder builder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append("\r\n");
        }
        return builder.toString();
    }

    private static String readAsString(File file) throws IOException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            return readAsString(in, "UTF-8");
        } finally {
            InstallUtils.close(in);
        }
    }

    public static List<LibertyInfo> parseYml(File file) throws IOException {
        String yml = readAsString(file);
        Pattern p = Pattern.compile("(.*):\\s*uri:(.*)\\s*license:(.*)");
        Matcher m = p.matcher(yml);

        if (m.find()) {
            List<LibertyInfo> values = new ArrayList<LibertyInfo>();
            m.reset();
            while (m.find()) {
                LibertyInfo info = new LibertyInfo(m.group(1).trim(), m.group(2).trim(), m.group(3).trim());
                values.add(info);
            }
            return values;
        } else {
            throw new RuntimeException("Invalid YML file contents");
        }
    }
    
    public static String getLicenseCode(File file, String regex) throws IOException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            return getLicenseCode(in, "UTF-8", regex);
        } finally {
            InstallUtils.close(in);
        }
    }
    
    public static String getLicenseCode(InputStream in, String encoding, String regex) throws IOException {
        String text = readAsString(in, encoding);
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);

        if (m.find()) {          
            return m.group(1);
        } else {
            return null;
        }
    }
    
    public static LibertyInfo selectVersion(Version version, List<LibertyInfo> versions) {
        List<LibertyInfo> candidates = new ArrayList<LibertyInfo>();
        for (LibertyInfo info : versions) {
            if (version.match(info.getVersion())) {
                candidates.add(info);
            }
        }
       
        if (candidates.size() == 0) {
            throw new BuildException("No candidates found for " + version + " version.");
        } else if (candidates.size() == 1) {
            return candidates.get(0);
        } else {
            Collections.sort(candidates, new Comparator<LibertyInfo> () {
                public int compare(LibertyInfo obj1, LibertyInfo obj2) {
                    return obj2.getVersion().compareTo(obj1.getVersion());
                }                
            });
            return candidates.get(0);
        }
    }
    
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignore) {
            }
        }
    }
    
    public static String getFile(URL url) {
        String path = url.getPath();
        int index = path.lastIndexOf('/');
        String file = index == -1 ? path : path.substring(index + 1);
        return file;
    }
    
    public static void createDirectory(File dir) {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BuildException("Unable to create " + dir + " directory.");
        }        
    }
}
